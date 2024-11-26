package com.example.aeefinalgroupproject

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.util.Log
import com.google.firebase.auth.FirebaseAuth

private const val PINS_COLLECTION = "pins"

class Firebase {
    // Initialize Firestore
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Method to add user favorites
    fun addUserFavorites(layoutName: String, iconId: Int, notificationBell: Boolean) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            Log.d("Firebase", "addUserFavorites: user id success")
        } else {
            Log.e("Firebase", "addUserFavorites: user id failed")
            return
        }
        // Data to add **NOTE: change later to suit needs
        val favorite = hashMapOf(
            "layoutName" to layoutName,
            "icon" to iconId,
            "bellStatus" to notificationBell,
            "isActive" to false
        )

        db.collection("users").document(userId).collection("favorites").document(layoutName)
            .set(favorite)
            .addOnSuccessListener {
                Log.d("Firebase", "Favorite successfully written for user $userId!")
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "Error writing favorite for user $userId", e)
            }
    }

    // Method to add a user to Firestore
    fun addUser(userId: String, name: String, age: Int) {
        // Data to add **NOTE: change later to suit needs
        val user = hashMapOf(
            "name" to name,
            "age" to age
        )

        db.collection("users") // "users" is the collection name
            .document(userId)   // userId is the document ID
            .set(user)          // Add data to Firestore
            .addOnSuccessListener {
                Log.d("Firebase", "DocumentSnapshot successfully written!")
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "Error writing document", e)
            }
    }
    // Method to retrieve a user by userId
    fun getUser(userId: String, onComplete: (Map<String, Any>?) -> Unit) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d("Firebase", "DocumentSnapshot data: ${document.data}")
                    onComplete(document.data)  // Return the document data to the caller
                } else {
                    Log.d("Firebase", "Get User: No such document")
                    onComplete(null)
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "Error getting document", e)
                onComplete(null)
            }
    }

    // ---------Adding a favorite-----------
    fun addFavorite(layoutName: String, iconId: Int, notificationBell: Boolean) { // iconId might del
//        val favorite = hashMapOf(
//            "layoutName" to layoutName,
//            "icon" to iconId,
//            "bellStatus" to notificationBell,
//            "isActive" to false
//        )
//
//        db.collection("favorites").document(layoutName).set(favorite)
//            .addOnSuccessListener {
//                Log.d("Firebase", "Favorite successfully written!")
//            }
//            .addOnFailureListener { e ->
//                Log.w("Firebase", "Error writing document", e)
//            }
        val userId = auth.currentUser?.uid
        if (userId != null) {
            Log.d("Firebase", "addUserFavorites: user id success")
        } else {
            Log.e("Firebase", "addUserFavorites: user id failed")
            return
        }
        // Data to add **NOTE: change later to suit needs
        val favorite = hashMapOf(
            "layoutName" to layoutName,
            "icon" to iconId,
            "bellStatus" to notificationBell,
            "isActive" to false
        )

        db.collection("users").document(userId).collection("favorites").document(layoutName)
            .set(favorite)
            .addOnSuccessListener {
                Log.d("Firebase", "Favorite successfully written for user $userId!")
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "Error writing favorite for user $userId", e)
            }
    }
    // retrieving a specific favorite (for testing)
    fun getFavorite(layoutName: String, onComplete: (Map<String, Any>?) -> Unit) {
//        db.collection("favorites").document(layoutName).get()
//            .addOnSuccessListener { document ->
//                if (document.exists()) {
//                    Log.d("Firebase", "DocumentSnapshot data: ${document.data}")
//                    onComplete(document.data)  // Return the document data to the caller
//                } else {
//                    Log.d("Firebase", "Get Fav: No such document")
//                    onComplete(null)
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.w("Firebase", "Error getting document", e)
//                onComplete(null)
//            }
        val userId = auth.currentUser?.uid
        if (userId != null) {
            Log.d("Firebase", "getAllFavs: user id success")
        } else {
            Log.e("Firebase", "getAllFavs: user id failed")
            onComplete(null)
            return
        }

        db.collection("users").document(userId).collection("favorites")
            .document(layoutName)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d("Firebase", "Snapshot data ${document.data}")
                    onComplete(document.data)
                } else {
                    Log.d("Firebase", "No such document")
                    onComplete(null)
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "Error getting all favorites for user $userId", e)
                onComplete(null)
            }
    }
    // Get all favorites
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
    // Update favorites
    fun updateFavorite(layoutName: String, updates: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            Log.d("Firebase", "updateFavs: user id success")
        } else {
            Log.e("Firebase", "updateFavs: user id failed")
            onComplete(false)
            return
        }
        db.collection("users").document(userId).collection("favorites").document(layoutName)
            .update(updates)
            .addOnSuccessListener {
                Log.d("Firebase", "Favorite successfully updated!")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "Error updating document", e)
                onComplete(false)
            }
    }
    // Remove favorite
    fun removeFavorite(layoutName: String, onComplete: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            Log.d("Firebase", "removeFavs: user id success")
        } else {
            Log.e("Firebase", "removeFavs: user id failed")
            onComplete(false)
            return
        }
        db.collection("users").document(userId).collection("favorites").document(layoutName).delete()
            .addOnSuccessListener {
                Log.d("Firebase", "Favorite successfully deleted!")
                onComplete(true) // Indicate success
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "Error deleting document", e)
                onComplete(false) // Indicate failure
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
                    onComplete(document.data)  // Return the document data to the caller
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

    fun updatePin(pinName: String, updates: Map<String, Any>) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            Log.d("Firebase", "updatePin: user id success")
        } else {
            Log.e("Firebase", "updatePin: user id failed")
            return
        }

        //TODO logic for checking if valid user (if their UID match)

        db.collection("users").document(userId).collection(PINS_COLLECTION).document(pinName)
            .update(updates)
            .addOnSuccessListener {
                Log.d("Firebase", "Pin successfully updated!")
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "Error updating document", e)
            }
    }

    fun removePin(pinName: String, onComplete: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            Log.d("Firebase", "updatePin: user id success")
        } else {
            Log.e("Firebase", "updatePin: user id failed")
            onComplete(false)
            return
        }

        //Logic for deletion
//        if (userId != db.collection(PINS_COLLECTION).document().get("userId").toString()) {
//
//        }

        db.collection(PINS_COLLECTION).document(pinName).delete()
            .addOnSuccessListener {
                Log.d("Firebase", "Pin successfully deleted!")
                onComplete(true) // Indicate success
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "Error deleting document", e)
                onComplete(false) // Indicate failure
            }
    }
}

