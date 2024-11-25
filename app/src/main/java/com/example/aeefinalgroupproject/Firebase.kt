package com.example.aeefinalgroupproject

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.RatingBar
import com.google.firebase.auth.FirebaseAuth

class Firebase {
    // Initialize Firestore
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun testAddPin() {
        val pinData = mapOf(
            "xmlName" to "test_pin",
            "latitude" to 47.6666,
            "longitude" to -117.3999,
            "locationName" to "Test Location",
            "description" to "This is a test description.",
            "rating" to 5
        )
        addPin("TestPin", pinData)
    }

    fun testFirestore() {
        db.collection("testCollection").document("testDocument")
            .set(mapOf("testField" to "Hello Firestore"))
            .addOnSuccessListener {
                Log.d("FirebaseTest", "Test write succeeded!")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseTest", "Test write failed: ${e.message}")
            }
    }

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

//    data class Favorite(  maybe have these as classes
//        val layoutName: String,
//        val icon: Int? = null,
//        val bellStatus: Boolean,
//        val isActive: Boolean = false
//    )
//
//    data class Pin(
//        val xmlName: String,
//        val latitude: Double,
//        val longitude: Double,
//        val locationName: String,
//        val description: String,
//        val rating: Int
//    )

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

    // ---------Adding a pin--------------
    fun addPin(pinName: String, pin: Map<String, Any>) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            Log.d("Firebase", "addPin: user id success")
        } else {
            Log.e("Firebase", "addPin: user id failed")
            return
        }
        db.collection("users").document(userId).collection("pins").document(pinName).set(pin)
            .addOnSuccessListener {
                Log.d("Firebase", "Pin successfully written!")
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "Error writing document", e)
            }
    }
    // retrieving a specific pin
    fun getPin(pinName: String, onComplete: (Map<String, Any>?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            Log.d("Firebase", "getPin: user id success")
        } else {
            Log.e("Firebase", "getPin: user id failed")
            onComplete(null)
            return
        }
        db.collection("users").document(userId).collection("pins").document(pinName).get()
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
    // Get all pins
    fun getAllPins(onComplete: (List<Map<String, Any>>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            Log.d("Firebase", "getAllPin: user id success")
        } else {
            Log.e("Firebase", "getAllPin: user id failed")
            onComplete(emptyList())
            return
        }
        db.collection("users").document(userId).collection("pins").get()
            .addOnSuccessListener { result ->
                val pinList = result.documents.mapNotNull { it.data }
                onComplete(pinList)
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "Error getting all pins", e)
                onComplete(emptyList())
            }
    }
    // Update pins
    fun updatePin(pinName: String, updates: Map<String, Any>) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            Log.d("Firebase", "updatePin: user id success")
        } else {
            Log.e("Firebase", "updatePin: user id failed")
            return
        }
        db.collection("users").document(userId).collection("pins").document(pinName)
            .update(updates)
            .addOnSuccessListener {
                Log.d("Firebase", "Pin successfully updated!")
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "Error updating document", e)
            }
    }
    // Remove pin
    fun removePin(pinName: String, onComplete: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            Log.d("Firebase", "updatePin: user id success")
        } else {
            Log.e("Firebase", "updatePin: user id failed")
            onComplete(false)
            return
        }
        db.collection("users").document(userId).collection("pins").document(pinName).delete()
            .addOnSuccessListener {
                Log.d("Firebase", "Pin successfully deleted!")
                onComplete(true) // Indicate success
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "Error deleting document", e)
                onComplete(false) // Indicate failure
            }
    }






    fun exampleUsage() {
        val firebaseHelper = Firebase()

        // Add a user
        firebaseHelper.addUser("01", "Ethan", 23)

        // Retrieve a user
        firebaseHelper.getUser("01") { userData ->
            if (userData != null) {
                val name = userData["name"]
                val age = userData["age"]
                Log.d("MainActivity", "User Name: $name, Age: $age")
            } else {
                Log.d("MainActivity", "User not found.")
            }
        }

        // Add a Favorite
        //firebaseHelper.addFavorite("College_Hall","f_college_hall",1, false) never run this please

        // retrieve the specific favorite
        getFavorite("College_Hall") { favoriteData ->
            if (favoriteData != null) {
                val iconResource = favoriteData["iconResource"] as? Int
                val notificationEnabled = favoriteData["notificationEnabled"] as? Boolean

                // Use these properties to update the UI as needed

                Log.d("MainActivity", "Successfully retrieved \"Favorite\" data")
            } else {
                Log.d("MainActivity", "Error retrieving \"Favorite\" data")
            }
        }
    }
}

