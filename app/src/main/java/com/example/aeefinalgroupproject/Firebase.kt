package com.example.aeefinalgroupproject

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.util.Log

class Firebase {
    // Initialize Firestore
    private val db: FirebaseFirestore = Firebase.firestore

    // Method to add data to Firestore
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
    }
}

