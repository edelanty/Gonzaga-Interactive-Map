package com.example.aeefinalgroupproject

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CommentRatingActivity : AppCompatActivity() {
    private var isFavorited = false
    private val firebase = Firebase()

    //For view changes
    private lateinit var locationNameTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var ratingTextView: TextView
    private lateinit var locationImageView: ImageView
    private lateinit var userNameTextView: TextView
    private lateinit var favoriteButton: ImageButton
    private lateinit var backButton: ImageButton

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
        favoriteButton = findViewById(R.id.favorite_button)
        backButton = findViewById(R.id.back_button)

        updateView(locationName)

        backButton.setOnClickListener {
            finish()
        }

        favoriteButton.setOnClickListener {
            onFavoriteButtonClicked(locationName)
        }
    }

    private fun onFavoriteButtonClicked(locationName: String) {
        //Toggle the favorite status
        val isFavorite = !isFavorited
        val bellStatus = false

        //Call the method to update Firestore
        firebase.updateFavoriteStatus(locationName, isFavorite, bellStatus)

        //Update the UI based on new favorite status
        isFavorited = isFavorite
        updateFavoriteButtonUI()
    }

    //Method to update the favorite button icon in the UI
    private fun updateFavoriteButtonUI() {
        val icon = if (isFavorited) R.drawable.heart_filled else R.drawable.favorites_heart
        favoriteButton.setImageResource(icon)

        //Toast message
        Toast.makeText(
            this,
            if (isFavorited) "Added to favorites" else "Removed from favorites",
            Toast.LENGTH_SHORT
        ).show()
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
//TODO setup images properly, allow for deletions by a user with the same UID on a pin, and comments/upvote
}