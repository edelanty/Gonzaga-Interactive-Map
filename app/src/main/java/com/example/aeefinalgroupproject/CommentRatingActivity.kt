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

class CommentRatingActivity : AppCompatActivity() {
    private var isFavorited = false // will set in onCreate
    private val firebase = Firebase()
    private var xmlName = "" // will set in onCreate

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment_rating)

        //Pin data from intent
        // will add pin id so you can get this from database
        val locationName = intent.getStringExtra("locationName") ?: "unknown"
        val description = intent.getStringExtra("description")
        val rating = intent.getStringExtra("rating")


        //Setting the data
        val locationNameTextView = findViewById<TextView>(R.id.locationNameTextView)
        val descriptionTextView = findViewById<TextView>(R.id.descriptionTextView)
        val ratingTextView = findViewById<TextView>(R.id.ratingTextView)
        val locationImageView = findViewById<ImageView>(R.id.locationImageView)


        //TODO instead of using these locally found ones through the intent just query the database
        locationNameTextView.text = locationName
        descriptionTextView.text = description
        ratingTextView.text = "Rating: $rating"

        val backButton: ImageButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }


        setDB(locationName)
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
        //TODO setup images properly
        //TODO literally everything else on this screen

}