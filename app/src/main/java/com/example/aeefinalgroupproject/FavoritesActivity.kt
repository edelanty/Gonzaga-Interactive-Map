package com.example.aeefinalgroupproject

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FavoritesActivity : AppCompatActivity() {
    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var favoritesAdapter: FavoritesAdapter
    private lateinit var backButton: ImageButton

    private val firebase = Firebase()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)
        requestNotificationPermission()

        favoritesRecyclerView = findViewById(R.id.favorites_recycler_view)
        backButton = findViewById(R.id.back_button)

        favoritesAdapter = FavoritesAdapter(this, emptyList(),
            onRemove = { favoriteName ->
                removeFavorite(favoriteName)
            },
            onToggleNotification = { favoriteName, isFavorite ->
                toggleNotification(favoriteName, isFavorite)
            })

        favoritesRecyclerView.layoutManager = LinearLayoutManager(this)
        favoritesRecyclerView.adapter = favoritesAdapter

        loadFavorites()

        backButton.setOnClickListener {
            finish()
        }
    }

    // Request permission for notifications
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }
    }

    private fun loadFavorites() {
        firebase.getAllFavorites { favoriteList ->
            //Filter out the inactive favorites (where isFavorite is false)
            Log.d("FavoritesActivity", "Loaded favorites: $favoriteList")
            val activeFavorites = favoriteList.filter { it["isFavorite"] as? Boolean ?: false }

            //Update the adapter with the filtered list of active favorites
            favoritesAdapter.updateFavorites(activeFavorites)
        }
    }

    //Handle the logic for removing a favorite
    private fun removeFavorite(favoriteName: String) {
        val dialogBuilder = android.app.AlertDialog.Builder(this)
        dialogBuilder.setTitle("Remove Favorite")
            .setMessage("Are you sure you want to remove $favoriteName from Favorites?")
            .setPositiveButton("Yes") { dialog, _ ->
                firebase.updateFavorite(favoriteName, mapOf("isFavorite" to false)) { success ->
                    if (success) {
                        Toast.makeText(this, "$favoriteName removed", Toast.LENGTH_SHORT).show()
                        loadFavorites()
                    } else {
                        Toast.makeText(this, "Failed to remove $favoriteName", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        //Show dialog
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    //Handle toggling the notification
    private fun toggleNotification(favoriteName: String, isFavorite: Boolean) {
        firebase.updateFavorite(favoriteName, mapOf("bellStatus" to isFavorite)) { success ->
            if (success) {
                Log.d("FavoritesActivity", "Notification status updated for $favoriteName")
                loadFavorites()
            } else {
                Log.e("FavoritesActivity", "Failed to update notification for $favoriteName")
            }
        }
    }

    // Refresh the data when returning from comment rating activity
    override fun onResume() {
        super.onResume()
        loadFavorites()
    }
}
