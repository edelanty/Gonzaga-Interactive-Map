package com.example.aeefinalgroupproject

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.net.URL

private const val PINS_COLLECTION = "pins"
private const val COMMENTS_COLLECTION = "comments"

class Firebase {
    //Initialize Firestore
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var notificationHelper: NotificationHelper

    // Notifications
    fun initializeNotifications(context: Context) {
        notificationHelper = NotificationHelper(context)
    }

    fun listenForComments() {
        val startListeningTime = System.currentTimeMillis()
        val sentNotifications = mutableSetOf<String>()
        FirebaseFirestore.getInstance()
            .collection("pins")
            .addSnapshotListener { pinsSnapshot, e ->
                if (e != null) {
                    Log.w("Firebase", "Listen failed.", e)
                    return@addSnapshotListener
                }
                pinsSnapshot?.documents?.forEach { pinDoc ->
                    val locationName = pinDoc.id

                    pinDoc.reference.collection("comments")
                        .addSnapshotListener { commentsSnapshot, commentError ->
                            if (commentError != null) {
                                Log.w("Firebase", "Comments listen failed.", commentError)
                                return@addSnapshotListener
                            }

                            commentsSnapshot?.documentChanges?.forEach { change ->
                                if (change.type == DocumentChange.Type.ADDED) {
                                    val comment = change.document.data
                                    val commentText = comment["content"] as? String ?: return@forEach
                                    val username = comment["username"] as? String ?: "Anonymous"
                                    val timestamp = comment["timestamp"] as? Long ?: return@forEach
                                    val commentID = change.document.id
                                    // Limits notifications to only occur during the current app session
                                    if (timestamp > startListeningTime && sentNotifications.add(commentID)) {
                                        checkAndNotify(locationName, commentText, username)
                                    }
                                }
                            }
                        }
                }
            }
    }

    private fun checkAndNotify(locationName: String, commentText: String, userName: String) {
        getAllFavorites { favorites ->
            favorites.forEach { favorite ->
                if (favorite["locationName"] == locationName &&
                    favorite["bellStatus"] == true &&
                    favorite["isFavorite"] == true) {
                    notificationHelper.showCommentNotification(locationName, commentText, userName)
                }
            }
        }
    }

    // Delete image from pin when deleting
    fun deleteImageByUrl(imageURL: String, callback: (Boolean) -> Unit) {
        val storage = FirebaseStorage.getInstance()
        try {
            val decodedUrl = java.net.URLDecoder.decode(imageURL, "UTF-8")
            val filepath = decodedUrl.substringAfter("/o/").substringBefore("?")
            val fileRef = storage.reference.child(filepath)
            fileRef.delete()
                .addOnSuccessListener { Log.d("FirebaseStorage", "File successfully deleted ${filepath}")
                callback(true)
                } .addOnFailureListener { e -> Log.e("FirebaseStorage", "Failed to delete file ${filepath}", e)
                callback(false)
                }
        } catch (e: Exception) {
            Log.e("FirebaseStorage", "Error parsing url", e)
            callback(false)
        }
    }


    //Method to update a favorite used for deletion and setting notifications
    fun updateFavorite(locationName: String, updates: Map<String, Any>, callback: (Boolean) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).collection("favorites").document(locationName)
                .update(updates)
                .addOnSuccessListener {
                    callback(true)
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Error updating favorite for $locationName: ", e)
                    callback(false)
                }
        } else {
            Log.e("Firebase", "User not logged in")
            callback(false)
        }
    }

    fun getCurrentUserId(): String {
        return auth.currentUser?.uid!!
    }

    //Initially used to add a favorite
    fun updateFavoriteStatus(locationName: String, isFavorite: Boolean, bellStatus: Boolean, callback: (Boolean) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val favoriteData = hashMapOf(
                "isFavorite" to isFavorite,
                "bellStatus" to bellStatus,
                "locationName" to locationName
            )

            db.collection("users").document(userId).collection("favorites").document(locationName).set(favoriteData)
                .addOnSuccessListener {
                    Log.d("Firebase", "Favorite status updated for $locationName")
                    callback(true)
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Error updating favorite status for $locationName", e)
                    callback(false)
                }
        } else {
            Log.e("Firebase", "User not logged in")
        }
    }

    fun isFavorite(locationName: String, callback: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val favoriteRef = db.collection("users").document(userId).collection("favorites").document(locationName)

            favoriteRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        callback(document.getBoolean("isFavorite") ?: false)
                    } else {
                        callback(false) // Default to not favorited if document doesn't exist
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Firebase", "Failed to check favorite status", exception)
                    callback(false) // Default to false on failure
                }
        }
    }

    fun getUserLikeStatus(locationName: String, callback: (Boolean, Boolean) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).collection("likes").document(locationName).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val liked = document.getBoolean("liked") ?: false
                        val disliked = document.getBoolean("disliked") ?: false
                        callback(liked, disliked)
                    } else {
                        callback(false, false)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Firebase", "Failed to check like/dislike status", exception)
                    callback(false, false)
                }
        } else {
            Log.e("Firebase", "User not logged in")
            callback(false, false)
        }
    }

    fun updateUserLikeStatus(
        locationName: String,
        liked: Boolean,
        disliked: Boolean,
        callback: (Boolean) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val likeData = mapOf(
                "liked" to liked,
                "disliked" to disliked
            )

            db.collection("users").document(userId).collection("likes").document(locationName).set(likeData)
                .addOnSuccessListener {
                    Log.d("Firebase", "Like/Dislike status updated for $locationName")
                    callback(true)
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Error updating like/dislike status for $locationName", e)
                    callback(false)
                }
        } else {
            Log.e("Firebase", "User not logged in")
            callback(false)
        }
    }

    fun updatePinLikeDislikeCounts(
        locationName: String,
        likeDelta: Int,
        dislikeDelta: Int,
        callback: (Boolean) -> Unit
    ) {
        val pinRef = db.collection(PINS_COLLECTION).document(locationName)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(pinRef)
            val currentLikes = snapshot.getLong("likeCount") ?: 0
            val currentDislikes = snapshot.getLong("dislikeCount") ?: 0

            transaction.update(pinRef, "likeCount", (currentLikes + likeDelta).coerceAtLeast(0))
            transaction.update(pinRef, "dislikeCount", (currentDislikes + dislikeDelta).coerceAtLeast(0))
        }.addOnSuccessListener {
            Log.d("Firebase", "Pin like/dislike counts updated for $locationName")
            callback(true)
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Failed to update pin counts for $locationName", e)
            callback(false)
        }
    }

    fun getAllFavorites(onComplete: (List<Map<String, Any>>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            Log.d("Firebase", "getAllFavs: user id success")
        } else {
            Log.e("Firebase", "getAllFavs: user id failed")
            onComplete(emptyList())
            return
        }

        db.collection("users").document(userId).collection("favorites")
            .get()
            .addOnSuccessListener { result ->
                val favoriteList = result.documents.mapNotNull { it.data }
                onComplete(favoriteList)
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "Error getting all favorites for user $userId", e)
                onComplete(emptyList())
            }
    }

    //Adding pins to the database
    fun addGlobalPin(pinName: String, pinData: Map<String, Any>) {
        val pinRef = db.collection(PINS_COLLECTION).document(pinName)

        pinRef.set(pinData)
            .addOnSuccessListener {
                Log.d("Firebase", "Pin added!")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error adding pin...", e)
            }
    }

    //Getting global pins
    fun getGlobalPins(onComplete: (List<Map<String, Any>>) -> Unit) {
        db.collection(PINS_COLLECTION)
            .get()
            .addOnSuccessListener { result ->
                val pinList = result.documents.mapNotNull { it.data }
                onComplete(pinList)
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error getting global pins", e)
                onComplete(emptyList())
            }
    }

    //Retrieving a specific pin
    fun getPin(pinName: String, onComplete: (Map<String, Any>?) -> Unit) {
        db.collection(PINS_COLLECTION).document(pinName).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d("Firebase", "DocumentSnapshot data: ${document.data}")
                    onComplete(document.data)
                } else {
                    Log.d("Firebase", "Get Pin: No such document")
                    onComplete(null)
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "Error getting document", e)
                onComplete(null)
            }
    }

    //Removes pin from the database
    fun removePin(pinName: String, onComplete: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            Log.d("Firebase", "updatePin: user id success")
        } else {
            Log.e("Firebase", "updatePin: user id failed")
            onComplete(false)
            return
        }

        db.collection(PINS_COLLECTION).document(pinName).delete()
            .addOnSuccessListener {
                Log.d("Firebase", "Pin successfully deleted!")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "Error deleting document", e)
                onComplete(false)
            }
    }

    // Remove pin with image
    fun removePinWithImage(pinName: String, callback: (Boolean) -> Unit) {
        getPin(pinName) { pinData ->
            if (pinData != null) {
                val imageUrl = pinData["imageUrl"] as? String ?: ""
                if (imageUrl.isNotEmpty()) {
                    deleteImageByUrl(imageUrl) { imageDeleted ->
                        if (imageDeleted) {
                            removePin(pinName) { pinDeleted ->
                                if (pinDeleted ) {
                                    Log.d("Firebase", "Pin and image successful deleted")
                                    callback(true)
                                } else {
                                    Log.e("Firebase", "Failed to delete pin")
                                    callback(false)
                                }
                            }
                        } else {
                            Log.e("Firebase", "Failed to delete image")
                            callback(false)
                        }
                    }
                }
            }
        }
    }


    // Function to add a comment to a specific pin
    fun addComment(
        pinName: String,
        username: String,
        content: String,
        callback: (Boolean) -> Unit
    ) {
        val commentData = hashMapOf(
            "username" to username,
            "content" to content,
            "timestamp" to System.currentTimeMillis() // Unix timestamp for sorting
        )

        db.collection(PINS_COLLECTION).document(pinName)
            .collection(COMMENTS_COLLECTION)
            .add(commentData)
            .addOnSuccessListener {
                Log.d("Firebase", "Comment added for pin: $pinName")
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error adding comment for pin: $pinName", e)
                callback(false)
            }
    }

    // Function to increment comment count
    fun incrementCommentCount(pinName: String, callback: (Boolean) -> Unit) {
        val pinRef = db.collection(PINS_COLLECTION).document(pinName)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(pinRef)
            val currentCommentCount = snapshot.getLong("commentCount") ?: 0
            transaction.update(pinRef, "commentCount", currentCommentCount + 1)
        }.addOnSuccessListener {
            Log.d("Firebase", "Comment count incremented for pin: $pinName")
            callback(true)
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Failed to increment comment count for pin: $pinName", e)
            callback(false)
        }
    }


    // Function to retrieve all comments for a specific pin
    fun getComments(pinName: String, onComplete: (List<Map<String, Any>>) -> Unit) {
        db.collection(PINS_COLLECTION).document(pinName)
            .collection(COMMENTS_COLLECTION)
            .orderBy("timestamp", Query.Direction.ASCENDING) // Sort by time, oldest to newest
            .get()
            .addOnSuccessListener { result ->
                val comments = result.documents.mapNotNull { it.data }
                onComplete(comments)
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error retrieving comments for pin: $pinName", e)
                onComplete(emptyList())
            }
    }

    // Function to listen for real-time updates to comments for a specific pin
    fun listenForComments(pinName: String, onCommentUpdate: (List<Map<String, Any>>) -> Unit) {
        db.collection(PINS_COLLECTION).document(pinName)
            .collection(COMMENTS_COLLECTION)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firebase", "Error listening for comments: ", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val comments = snapshot.documents.mapNotNull { it.data }
                    onCommentUpdate(comments)
                } else {
                    onCommentUpdate(emptyList())
                }
            }
    }

    // Function to delete a specific comment by its document ID
    fun deleteComment(pinName: String, commentId: String, callback: (Boolean) -> Unit) {
        db.collection(PINS_COLLECTION).document(pinName)
            .collection(COMMENTS_COLLECTION).document(commentId)
            .delete()
            .addOnSuccessListener {
                Log.d("Firebase", "Comment deleted for pin: $pinName")
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error deleting comment for pin: $pinName", e)
                callback(false)
            }
    }
}

