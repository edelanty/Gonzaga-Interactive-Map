package com.example.aeefinalgroupproject

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FavoritesActivity : AppCompatActivity() {
    private var notificationsEnabled = false
    private lateinit var removeFavoriteButton: ImageButton
    private lateinit var favoriteRow: View
    private lateinit var notificationBell: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        // Back button (to home activity)
        val backButton: ImageButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Buttons and views
        removeFavoriteButton = findViewById(R.id.remove_favorite)
        favoriteRow = findViewById(R.id.favorite_row)
        notificationBell = findViewById(R.id.notification_bell)

        // Check if the favorite was removed in a previous session
        if (isFavoriteRemoved()) {
            favoriteRow.visibility = View.GONE
        }

        // Handle remove favorite (heart) button
        removeFavoriteButton.setOnClickListener {
            showRemoveFavoriteConfirmation()
        }

        // Handle notification bell toggle
        notificationBell.setOnClickListener {
            toggleNotifications(notificationBell)
        }

        // Check if notifications were enabled or disabled
        if (areNotificationsEnabled()) {
            notificationBell.setImageResource(R.drawable.notifications_active) // Set bell to black (enabled)
            notificationsEnabled = true
        } else {
            notificationBell.setImageResource(R.drawable.notifications_none) // Set bell to white (disabled)
            notificationsEnabled = false
        }
    }

    // Remove Favorite confirmation method (cancel or confirm)
    private fun showRemoveFavoriteConfirmation() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Remove Favorite")
        builder.setMessage("Are you sure you want to remove from your favorites?")
        builder.setPositiveButton("Confirm") { dialog: DialogInterface, _: Int ->
            removeFavoriteItem()
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    // Check if the favorite was removed
    private fun isFavoriteRemoved(): Boolean {
        val sharedPref = getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("college_hall_removed", false)
    }

    // Remove favorite and store state
    private fun removeFavoriteItem() {
        favoriteRow.visibility = View.GONE
        val sharedPref = getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("college_hall_removed", true)
        editor.apply()
        Toast.makeText(this, "Favorite removed", Toast.LENGTH_SHORT).show()
    }

    // Toggle notifications on/off
    private fun toggleNotifications(bell: ImageButton) {
        notificationsEnabled = !notificationsEnabled
        val sharedPref = getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        if (notificationsEnabled) {
            bell.setImageResource(R.drawable.notifications_active)
            Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show()
            editor.putBoolean("college_hall_notifications", true)
        } else {
            bell.setImageResource(R.drawable.notifications_none)
            Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show()
            editor.putBoolean("college_hall_notifications", false)
        }
        editor.apply()
    }

    // Check if notifications are enabled
    private fun areNotificationsEnabled(): Boolean {
        val sharedPref = getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("college_hall_notifications", false)
    }
}