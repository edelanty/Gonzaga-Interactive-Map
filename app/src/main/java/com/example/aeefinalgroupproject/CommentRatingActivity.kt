package com.example.aeefinalgroupproject

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
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
    private lateinit var deleteLayout: LinearLayout
    private lateinit var deleteImageButton: ImageButton

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
        deleteLayout = findViewById(R.id.delete_layout)
        deleteImageButton = findViewById(R.id.delete_pin_button)

        checkFavoriteStatus(locationName)
        updateView(locationName)

        backButton.setOnClickListener {
            finish()
        }

        favoriteButton.setOnClickListener {
            onFavoriteButtonClicked(locationName)
        }

        deleteImageButton.setOnClickListener {
            deletePin(locationName)
            //TODO there is a bug where the marker remains on the map, I've tried fixing but we are probably going to have to pass
            //in the locationName through the intent to get to this activity, remove the marker before we go here, and then if there is no
            //deletion, query all the information about that locationName marker and add it back to the map before we return... (I don't want to do this right now)
            finish()
        }

        setDeleteVisibilityForOwner(locationName)
    }

    //Makes the delete button visible only for the creator of the pin
    private fun setDeleteVisibilityForOwner(locationName: String) {
        val currentUser = firebase.getCurrentUserId()

        firebase.getPin(locationName) { pinData ->
            if (pinData != null) {
                val userId = pinData["userId"] as? String

                if (currentUser == userId) {
                    deleteLayout.visibility = LinearLayout.VISIBLE
                }
            } else {
                Log.e("Firebase", "Failed to fetch pin data")
            }
        }
    }

    //Removes a pin given a specified locationName
    private fun deletePin(locationName: String) {
        firebase.removePin(locationName) { success ->
            if (success) {
                Toast.makeText(this, "$locationName has been deleted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to delete $locationName", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkFavoriteStatus(locationName: String) {
        firebase.isFavorite(locationName) { isFavorite ->
            isFavorited = isFavorite

            //Change icon depending on query
            val icon = if (isFavorited) R.drawable.heart_filled else R.drawable.favorites_heart
            favoriteButton.setImageResource(icon)
        }
    }

    private fun onFavoriteButtonClicked(locationName: String) {
        //Toggle the favorite status
        val newFavoriteStatus = !isFavorited
        val bellStatus = false

        firebase.updateFavoriteStatus(locationName, newFavoriteStatus, bellStatus) { success ->
            if (success) {
                isFavorited = newFavoriteStatus
                updateFavoriteButtonUI()
            } else {
                Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Method to update the favorite button icon in the UI
    private fun updateFavoriteButtonUI() {
        val icon = if (isFavorited) R.drawable.heart_filled else R.drawable.favorites_heart
        favoriteButton.setImageResource(icon)
        Toast.makeText(this, if (isFavorited) "Added to favorites" else "Removed from favorites", Toast.LENGTH_SHORT).show()
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
                userName = userName?.replaceFirstChar { it.uppercaseChar() }

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