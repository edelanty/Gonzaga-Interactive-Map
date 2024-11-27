package com.example.aeefinalgroupproject

import android.annotation.SuppressLint
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
    private var isLiked = false
    private var isDisliked = false

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
    private lateinit var commentImageButton: ImageButton
    private lateinit var likeImageButton: ImageButton
    private lateinit var dislikeImageButton: ImageButton
    private lateinit var likeCountTextView: TextView
    private lateinit var dislikeCountTextView: TextView
    private lateinit var commentCountTextView: TextView
    private lateinit var locationName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment_rating)

        //Location name from Intent to use to query the pin from database
        locationName = intent.getStringExtra("locationName")!!

        //Top Half
        locationNameTextView = findViewById(R.id.locationNameTextView)
        descriptionTextView = findViewById(R.id.descriptionTextView)
        ratingTextView = findViewById(R.id.ratingTextView)
        locationImageView = findViewById(R.id.locationImageView)
        userNameTextView = findViewById(R.id.userNameTextView)
        backButton = findViewById(R.id.back_button)

        //Bottom Half
        favoriteButton = findViewById(R.id.favorite_button)
        commentImageButton = findViewById(R.id.comment_button)
        commentCountTextView = findViewById(R.id.comment_count)
        likeImageButton = findViewById(R.id.like_button)
        dislikeImageButton = findViewById(R.id.dislike_button)
        deleteLayout = findViewById(R.id.delete_layout)
        deleteImageButton = findViewById(R.id.delete_pin_button)
        likeCountTextView = findViewById(R.id.like_count)
        dislikeCountTextView = findViewById(R.id.dislike_count)

        checkLikeDislikeStatus()
        checkFavoriteStatus()
        updateView()
        createListeners()
        setDeleteVisibilityForOwner()
    }

    //Sets all the listeners
    private fun createListeners() {
        likeImageButton.setOnClickListener {
            liked()
        }

        dislikeImageButton.setOnClickListener {
            disliked()
        }

        commentImageButton.setOnClickListener {
            comments()
        }

        backButton.setOnClickListener {
            finish()
        }

        favoriteButton.setOnClickListener {
            onFavoriteButtonClicked()
        }

        deleteImageButton.setOnClickListener {
            deletePin()
            //TODO there is a bug where the marker remains on the map, I've tried fixing but we are probably going to have to pass
            //in the locationName through the intent to get to this activity, remove the marker before we go here, and then if there is no
            //deletion, query all the information about that locationName marker and add it back to the map before we return... (I don't want to do this right now)
            finish()
        }
    }

    private fun liked() {
        firebase.getUserLikeStatus(locationName) { likedStatus, dislikedStatus ->
            val newLikedStatus = !likedStatus
            val newDislikedStatus = false // Cannot dislike if liked

            firebase.updateUserLikeStatus(locationName, newLikedStatus, newDislikedStatus) { userUpdateSuccess ->
                if (userUpdateSuccess) {
                    firebase.updatePinLikeDislikeCounts(
                        locationName,
                        likeDelta = if (newLikedStatus) 1 else -1,
                        dislikeDelta = if (dislikedStatus) -1 else 0
                    ) { pinUpdateSuccess ->
                        if (pinUpdateSuccess) {
                            isLiked = newLikedStatus
                            isDisliked = newDislikedStatus
                            updateLikeDislikeUI()
                        } else {
                            Toast.makeText(this, "Failed to update pin counts.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Failed to update like status.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun disliked() {
        firebase.getUserLikeStatus(locationName) { likedStatus, dislikedStatus ->
            val newLikedStatus = false // Cannot like if disliked
            val newDislikedStatus = !dislikedStatus

            firebase.updateUserLikeStatus(locationName, newLikedStatus, newDislikedStatus) { userUpdateSuccess ->
                if (userUpdateSuccess) {
                    firebase.updatePinLikeDislikeCounts(
                        locationName,
                        likeDelta = if (likedStatus) -1 else 0,
                        dislikeDelta = if (newDislikedStatus) 1 else -1
                    ) { pinUpdateSuccess ->
                        if (pinUpdateSuccess) {
                            isLiked = newLikedStatus
                            isDisliked = newDislikedStatus
                            updateLikeDislikeUI()
                        } else {
                            Toast.makeText(this, "Failed to update pin counts.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Failed to update dislike status.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun comments() {

    }

    //Switches the UI depending on liked or disliked
    private fun updateLikeDislikeUI() {
        firebase.getPin(locationName) { pinData ->
            if (pinData != null) {
                val likeCount = pinData["likeCount"] as? Number
                val dislikeCount = pinData["dislikeCount"] as? Number
                val commentCount = pinData["commentCount"] as? Number

                //Update TextViews and buttons
                likeCountTextView.text = likeCount.toString()
                dislikeCountTextView.text = dislikeCount.toString()
                commentCountTextView.text = commentCount.toString()
                likeImageButton.setImageResource(if (isLiked) R.drawable.like_icon_foreground else R.drawable.like_outline_foreground)
                dislikeImageButton.setImageResource(if (isDisliked) R.drawable.dislike_icon_foreground else R.drawable.dislike_outline_foreground)
            } else {
                Log.e("Firebase", "Failed to fetch pin data")
            }
        }
    }

    //Makes the delete button visible only for the creator of the pin
    private fun setDeleteVisibilityForOwner() {
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
    private fun deletePin() {
        firebase.removePin(locationName) { success ->
            if (success) {
                Toast.makeText(this, "$locationName has been deleted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to delete $locationName", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Updates UI depending on the like/dislike status
    private fun checkLikeDislikeStatus() {
        // Fetch the current like/dislike status for the pin
        firebase.getUserLikeStatus(locationName) { likedStatus, dislikedStatus ->
            // Update the local variables for UI control
            isLiked = likedStatus
            isDisliked = dislikedStatus

            // Update the UI based on the retrieved statuses
            updateLikeDislikeUI()
        }
    }

    //Updates UI depending on the favorite status
    private fun checkFavoriteStatus() {
        firebase.isFavorite(locationName) { isFavorite ->
            isFavorited = isFavorite

            //Change icon depending on query
            val icon = if (isFavorited) R.drawable.heart_filled else R.drawable.favorites_heart
            favoriteButton.setImageResource(icon)
        }
    }

    private fun onFavoriteButtonClicked() {
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
    private fun updateView() {
        firebase.getPin(locationName) { pinData ->
            if (pinData != null) {
                val description = pinData["description"] as? String
                val rating = pinData["rating"] as? Double ?: 0.0
                var userName = pinData["userName"] as? String
                val likeCount = pinData["likeCount"] as? Number
                val dislikeCount = pinData["dislikeCount"] as? Number
                val commentCount = pinData["commentCount"] as? Number

                //Just get the name from the email
                userName = userName?.substringBefore("@")
                userName = userName?.replaceFirstChar { it.uppercaseChar() }

                //Update the view with the retrieved data
                userNameTextView.text = "$userName left this pin!"
                locationNameTextView.text = locationName
                descriptionTextView.text = description
                ratingTextView.text = "Rating: $rating"
                //All the numbers
                likeCountTextView.text = likeCount.toString()
                dislikeCountTextView.text = dislikeCount.toString()
                commentCountTextView.text = commentCount.toString()
            } else {
                Log.e("Firebase", "Failed to fetch pin data")
            }
        }
    }
//TODO setup images properly, allow for deletions by a user with the same UID on a pin, and comments/upvote
}