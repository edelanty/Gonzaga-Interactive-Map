package com.example.aeefinalgroupproject

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.util.Log
import android.view.View
import android.widget.ImageButton

class Firebase {
    // Initialize Firestore
    private val db: FirebaseFirestore = Firebase.firestore

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
                    Log.d("Firebase", "No such document")
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
        val favorite = hashMapOf(
            "layoutName" to layoutName,
            "icon" to iconId,
            "bellStatus" to notificationBell
        )

        db.collection("favorites").document(layoutName).set(favorite)
            .addOnSuccessListener {
                Log.d("Firebase", "Favorite successfully written!")
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "Error writing document", e)
            }
    }
    // retrieving a specific favorite (for testing)
    fun getFavorite(layoutName: String, onComplete: (Map<String, Any>?) -> Unit) {
        db.collection("users").document(layoutName).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d("Firebase", "DocumentSnapshot data: ${document.data}")
                    onComplete(document.data)  // Return the document data to the caller
                } else {
                    Log.d("Firebase", "No such document")
                    onComplete(null)
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "Error getting document", e)
                onComplete(null)
            }
    }
    // Get all favorites
    fun getAllFavorites(onComplete: (List<Map<String, Any>>) -> Unit) {
        db.collection("favorites").get()
            .addOnSuccessListener { result ->
                val favoriteList = result.documents.mapNotNull { it.data }
                onComplete(favoriteList)
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "Error getting all favorites", e)
                onComplete(emptyList())
            }
    }
    // Update favorites
    fun updateFavorite(layoutName: String, updates: Map<String, Any>) {
        db.collection("favorites").document(layoutName)
            .update(updates)
            .addOnSuccessListener {
                Log.d("Firebase", "Favorite successfully updated!")
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "Error updating document", e)
            }
    }
    // Remove favorite
    fun removeFavorite(layoutName: String, onComplete: (Boolean) -> Unit) {
        db.collection("favorites").document(layoutName).delete()
            .addOnSuccessListener {
                Log.d("Firebase", "Favorite successfully deleted!")
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
        firebaseHelper.addFavorite("f_college_hall",1, false)

        // retrieve the specific favorite
        getFavorite("f_college_hall") { favoriteData ->
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

