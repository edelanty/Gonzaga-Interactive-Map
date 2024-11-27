package com.example.aeefinalgroupproject

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.util.Log
import com.google.firebase.auth.FirebaseAuth

private const val PINS_COLLECTION = "pins"

class Firebase {
    //Initialize Firestore
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

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
}

