package com.example.aeefinalgroupproject

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.w3c.dom.Text

class CommentRatingActivity : AppCompatActivity() {
    private var isFavorited = false
    private val firebase = Firebase()
    private var xmlName = ""

    //For view changes
    private lateinit var locationNameTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var ratingTextView: TextView
    private lateinit var locationImageView: ImageView
    private lateinit var userNameTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment_rating)

        //Location name from Intent to use to query the pin from database
        val locationName = intent.getStringExtra("locationName")!!

        locationNameTextView = findViewById(R.id.locationNameTextView)
        descriptionTextView = findViewById(R.id.descriptionTextView)
        ratingTextView = findViewById(R.id.ratingTextView)
        locationImageView = findViewById(R.id.locationImageView)
        userNameTextView = findViewById(R.id.userNameTextView)

        updateView(locationName)

        val backButton: ImageButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        setDB(locationName)
    }

    //Updates the information shown on the view for a clicked on pin
    @SuppressLint("SetTextI18n")
    private fun updateView(locationName: String) {
        firebase.getPin(locationName) { pinData ->
            if (pinData != null) {
                val description = pinData["description"] as? String
                val rating = pinData["rating"] as? Double ?: 0.0
                var userName = pinData["userName"] as? String

                //Just get the name from the email
                userName = userName?.substringBefore("@")

                //Update the view with the retrieved data
                userNameTextView.text = "$userName left this pin!"
                locationNameTextView.text = locationName
                descriptionTextView.text = description
                ratingTextView.text = "Rating: $rating"
            } else {
                Log.e("Firebase", "Failed to fetch pin data")
            }
        }
    }

    private fun setDB(locationName: String) {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = View.VISIBLE

        firebase.getPin(locationName) { data ->
            progressBar.visibility = View.GONE

            if (data != null) {
                // Retrieve `xmlName` for the pin
                xmlName = data["xmlName"] as? String ?: "default_view"

                // Check if the pin is favorited
                firebase.getFavorite(xmlName) { favoriteData ->
                    if (favoriteData == null) {
                        // If no favorite data exists, create a new favorite entry
                        firebase.addFavorite(xmlName, 1, false)
                        isFavorited = false // New entry is not active
                    } else {
                        // Set `isFavorited` based on the database value
                        isFavorited = favoriteData["isActive"] as? Boolean ?: false
                    }
                    // Now that `isFavorited` is updated, set the favorite button
                    setFavoriteButton()
                }
            } else {
                Log.e("Firebase", "Failed to retrieve pin data for $locationName")
            }
        }
    }

    private fun setFavoriteButton() {
        //Switches out the drawable displayed depending on whether the pin is a favorite or not
        val favoriteButton: ImageButton = findViewById(R.id.favorite_button)

        // set icon to start
        val icon = if (isFavorited) R.drawable.heart_filled else R.drawable.favorites_heart
        favoriteButton.setImageResource(icon)

        // set onClick Listener
        favoriteButton.setOnClickListener {
            isFavorited = !isFavorited
            val icon = if (isFavorited) R.drawable.heart_filled else R.drawable.favorites_heart
            favoriteButton.setImageResource(icon)
            Toast.makeText(
                this,
                if (isFavorited) "Added to favorites" else "Removed from favorites",
                Toast.LENGTH_SHORT
            ).show()

            // update database
            firebase.updateFavorite(xmlName, mapOf("isActive" to isFavorited)) { success ->
                if (success) {
                    Log.d("Firebase", "Successfully updated isActive to $isFavorited for $xmlName")
                } else {
                    Log.e("Firebase", "Failed to update isActive for $xmlName")
                }
            }
        }
    }
    //TODO setup images properly, allow for deletions by a user with the same UID on a pin, and comments/upvote
}