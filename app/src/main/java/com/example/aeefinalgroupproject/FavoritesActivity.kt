package com.example.aeefinalgroupproject

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
        firebase.updateFavorite(favoriteName, mapOf("isFavorite" to false)) { success ->
            if (success) {
                Toast.makeText(this, "$favoriteName removed", Toast.LENGTH_SHORT).show()
                //Refresh list
                loadFavorites()
            } else {
                Toast.makeText(this, "Failed to remove $favoriteName", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Handle toggling the notification (bellStatus)
    private fun toggleNotification(favoriteName: String, isFavorite: Boolean) {
        firebase.updateFavorite(favoriteName, mapOf("bellStatus" to isFavorite)) { success ->
            if (success) {
                Log.d("FavoritesActivity", "Notification status updated for $favoriteName")
            } else {
                Log.e("FavoritesActivity", "Failed to update notification for $favoriteName")
            }
        }
    }
}
