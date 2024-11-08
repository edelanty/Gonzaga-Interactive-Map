package com.example.aeefinalgroupproject

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import org.w3c.dom.Text

class FavoritesActivity : AppCompatActivity() {
    private lateinit var favoritesContainer: LinearLayout
    private val firebase = Firebase()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        // initialize view for favorites container
        favoritesContainer = findViewById(R.id.favorites_container)

        // Back button (to home activity)
        val backButton: ImageButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        //Load up Favorites
        loadFaves()
    }

    // load favorites from Firebase
    private fun loadFaves() {
        firebase.getAllFavorites { favoriteList ->
            favoriteList.forEach { favoriteData ->
                addFavoriteRow(favoriteData)
            }
        }
    }
    // add Favorite row
    private fun addFavoriteRow(favoriteData: Map<String, Any>) {
        // get name of xml layout file to open (also doubles as key for row)
        val layoutName = favoriteData["layoutName"] as? String ?: "default_layout" // add a default layout in case of error
        val layoutResId = resources.getIdentifier(layoutName, "layout", packageName)

        val rowView = LayoutInflater.from(this)
            .inflate(layoutResId, favoritesContainer, false)

        // get buttons
        val removeFavoriteButton = rowView.findViewById<ImageButton>(R.id.remove_favorite)
        val notificationBell = rowView.findViewById<ImageButton>(R.id.notification_bell)

        // Set data from Firebase to row views
        var notificationEnabled = favoriteData["bellStatus"] as? Boolean ?: false
        notificationBell.setImageResource(
            if (notificationEnabled) R.drawable.notifications_active
            else R.drawable.notifications_none)

        // set on Click listener for the remove favorite button
        removeFavoriteButton.setOnClickListener {
            removeFaveRow(layoutName, rowView) // go ahead and remove it
        }

        // set on click listener for the notification bell
        notificationBell.setOnClickListener {
            notificationEnabled = !notificationEnabled
            notificationBell.setImageResource(
                if (notificationEnabled) R.drawable.notifications_active
                else R.drawable.notifications_none)
            // set new value in database and this rows data
            firebase.updateFavorite(layoutName, mapOf("bellStatus" to notificationEnabled))
        }

        // add row to container
        favoritesContainer.addView(rowView)
    }

    // Removes row from database and UI
    private fun removeFaveRow(name: String, rowView: View) {
        // Pop up to ask if they are sure
        val builder = AlertDialog.Builder(this)
        val favoriteName = rowView.findViewById<TextView>(R.id.favorite_name).text
        builder.setTitle("Remove Favorite")
        builder.setMessage("Are you sure you want to remove $favoriteName from your favorites?")
        builder.setPositiveButton("Confirm") { dialog: DialogInterface, _: Int ->
            // Remove row from the UI
            favoritesContainer.removeView(rowView)
            // Remove favorite from the database
            firebase.removeFavorite(name) { success ->
                if (success) {
                    Toast.makeText(this, "Favorite removed", Toast.LENGTH_SHORT).show()
                    rowView.findViewById<ImageButton>(R.id.remove_favorite)?.setOnClickListener(null)
                    rowView.findViewById<ImageButton>(R.id.notification_bell)?.setOnClickListener(null)
                } else {
                    Toast.makeText(this, "Failed to remove favorite", Toast.LENGTH_SHORT).show()
                    favoritesContainer.addView(rowView)
                }
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }
}