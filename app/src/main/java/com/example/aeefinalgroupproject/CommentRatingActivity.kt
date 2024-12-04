package com.example.aeefinalgroupproject

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

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

    interface DeletePinCallback {
        fun onPinDeleted(success: Boolean)
    }

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
            //Makes sure we don't delete no matter user response in dialogue
            deletePin(object : DeletePinCallback {
                override fun onPinDeleted(success: Boolean) {
                    if (success) {
                        // Notify the HomeActivity about the deletion
                        val resultIntent = Intent()
                        resultIntent.putExtra("deletedPinName", locationName)
                        setResult(RESULT_OK, resultIntent)

                        // Close the CommentRatingActivity
                        finish()
                    } else {
                        Toast.makeText(this@CommentRatingActivity, "Failed to delete pin", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }


        val count = intent.getIntExtra("commentCount", -1)
        if(count != -1) {
            likeCountTextView.text = count.toString()
        }
    }

    private fun liked() {
        firebase.getUserLikeStatus(locationName) { likedStatus, dislikedStatus ->
            val newLikedStatus = !likedStatus
            val newDislikedStatus = false

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
                        }
                    }
                }
            }
        }
    }

    private fun disliked() {
        firebase.getUserLikeStatus(locationName) { likedStatus, dislikedStatus ->
            val newLikedStatus = false
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
                        }
                    }
                }
            }
        }
    }

    //TODO comments
    private fun comments() {
        val intent = Intent(this, CommentsActivity::class.java)
        intent.putExtra("locationName", locationName)
        startActivity(intent)
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
    //Removes a pin given a specified locationName
    private fun deletePin(callback: DeletePinCallback) {
        val dialogBuilder = android.app.AlertDialog.Builder(this)
        dialogBuilder.setTitle("Delete Pin")
            .setMessage("Are you sure you want to delete your $locationName pin?")
            .setPositiveButton("Yes") { dialog, _ ->
                var imageUrl = ""

                firebase.getPin(locationName) { pinData ->
                    if (pinData != null) {
                        imageUrl = pinData["imageUrl"].toString()
                    } else {
                        Log.e("Firebase", "Failed to fetch pin data")
                    }
                }

                //If there is an image
                if (imageUrl.isNotEmpty()) {
                    firebase.removePinWithImage(locationName) { success ->
                        if (success) {
                            Toast.makeText(this, "$locationName pin has been deleted!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Failed to delete $locationName pin.", Toast.LENGTH_SHORT).show()
                        }
                        callback.onPinDeleted(success)
                    }
                } else { //No image
                    firebase.removePin(locationName) { success ->
                        if (success) {
                            Toast.makeText(this, "$locationName pin has been deleted!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Failed to delete $locationName pin.", Toast.LENGTH_SHORT).show()
                        }
                        callback.onPinDeleted(success)
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                callback.onPinDeleted(false)
                dialog.dismiss()
            }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
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
                val imageUrl = pinData["imageUrl"] as? String
                val description = pinData["description"] as? String
                val rating = pinData["rating"] as? Double ?: 0.0
                val userName = pinData["userName"] as? String
                val likeCount = pinData["likeCount"] as? Number
                val dislikeCount = pinData["dislikeCount"] as? Number
                val commentCount = pinData["commentCount"] as? Number

                //Update the view with the retrieved data
                userNameTextView.text = "$userName left this pin!"
                locationNameTextView.text = locationName
                descriptionTextView.text = description
                ratingTextView.text = "Rating: $rating"
                //All the numbers
                likeCountTextView.text = likeCount.toString()
                dislikeCountTextView.text = dislikeCount.toString()
                commentCountTextView.text = commentCount.toString()

                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(this).load(imageUrl).into(locationImageView)
                    locationImageView.visibility = View.VISIBLE
                }
            } else {
                Log.e("Firebase", "Failed to fetch pin data")
            }
        }
    }
    // Refresh the data when returning from comments activity
    override fun onResume() {
        super.onResume()
        updateView()
    }
}