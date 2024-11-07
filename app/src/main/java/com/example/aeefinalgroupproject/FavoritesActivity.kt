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
    private var notificationsEnabled = false // might del
    private lateinit var removeFavoriteButton: ImageButton // might del
    private lateinit var favoriteRow: View // might del
    private lateinit var notificationBell: ImageButton // might del

    private lateinit var favoritesContainer: LinearLayout
    private val firebase = Firebase()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        //HARDCODED FOR NOW, WE JUST ADD COLLEGE HALL HERE
        //firebase.addFavorite("f_college_hall", 1, false)
        //firebase.addFavorite("f_hemmingson", 1, true)
        //MOVE THIS TO SOMEWHERE ELSE

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

//        // Buttons and views
//        removeFavoriteButton = findViewById(R.id.remove_favorite)
//        favoriteRow = findViewById(R.id.favorite_row)
//        notificationBell = findViewById(R.id.notification_bell)
//
//        // Check if the favorite was removed in a previous session
//        if (isFavoriteRemoved()) {
//            favoriteRow.visibility = View.GONE
//        }
//
//        // Handle remove favorite (heart) button
//        removeFavoriteButton.setOnClickListener {
//            showRemoveFavoriteConfirmation()
//        }
//
//        // Handle notification bell toggle
//        notificationBell.setOnClickListener {
//            toggleNotifications(notificationBell)
//        }
//
//        // Check if notifications were enabled or disabled
//        if (areNotificationsEnabled()) {
//            notificationBell.setImageResource(R.drawable.notifications_active) // Set bell to black (enabled)
//            notificationsEnabled = true
//        } else {
//            notificationBell.setImageResource(R.drawable.notifications_none) // Set bell to white (disabled)
//            notificationsEnabled = false
//        }
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
            // pop up to ask if they are sure
            val builder = AlertDialog.Builder(this)
            val name = rowView.findViewById<TextView>(R.id.favorite_name).text
            builder.setTitle("Remove Favorite")
            builder.setMessage("Are you sure you want to remove $name from your favorites?")
            builder.setPositiveButton("Confirm") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
                removeFaveRow(layoutName, rowView) // go ahead and remove it
            }
            builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            val alertDialog = builder.create()
            alertDialog.show()
        }

        // set on click listener for the notification bell
        notificationBell.setOnClickListener {
            notificationsEnabled = !notificationsEnabled
            notificationBell.setImageResource(
                if (notificationsEnabled) R.drawable.notifications_active
                else R.drawable.notifications_none)
            // set new value in database and this rows data
            firebase.updateFavorite(layoutName, mapOf("bellStatus" to notificationsEnabled))
        }

        // add row to container
        favoritesContainer.addView(rowView)
    }

    private fun removeFaveRow(name: String, rowView: View) {
        // Remove favorite logic here
        firebase.removeFavorite(name) { success ->
            if (success) {
                // Remove the row from the UI if deletion from Firestore was successful
                favoritesContainer.removeView(rowView)
                Toast.makeText(this, "Favorite removed", Toast.LENGTH_SHORT).show()

                // clean up for memory optimization
                rowView.findViewById<ImageButton>(R.id.remove_favorite)?.setOnClickListener(null)
                rowView.findViewById<ImageButton>(R.id.notification_bell)?.setOnClickListener(null)

                //reload faves without the view
                onCreate(null)
            } else {
                Toast.makeText(this, "Failed to remove favorite", Toast.LENGTH_SHORT).show()
            }
        }
    }


//    // Remove Favorite confirmation method (cancel or confirm)
//    private fun showRemoveFavoriteConfirmation() {
//        val builder = AlertDialog.Builder(this)
//        builder.setTitle("Remove Favorite")
//        builder.setMessage("Are you sure you want to remove from your favorites?")
//        builder.setPositiveButton("Confirm") { dialog: DialogInterface, _: Int ->
//            removeFavoriteItem()
//            dialog.dismiss()
//        }
//        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
//            dialog.dismiss()
//        }
//        val alertDialog = builder.create()
//        alertDialog.show()
//    }

//    // Check if the favorite was removed
//    private fun isFavoriteRemoved(): Boolean {
//        val sharedPref = getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)
//        return sharedPref.getBoolean("college_hall_removed", false)
//    }

//    // Remove favorite and store state   KEEP*******************
//    private fun removeFavoriteItem() {
//        favoriteRow.visibility = View.GONE
//        val sharedPref = getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)
//        val editor = sharedPref.edit()
//        editor.putBoolean("college_hall_removed", true)
//        editor.apply()
//        Toast.makeText(this, "Favorite removed", Toast.LENGTH_SHORT).show()
//    } *******************************************************KEEP

//    // Toggle notifications on/off
//    private fun toggleNotifications(bell: ImageButton) {
//        notificationsEnabled = !notificationsEnabled
//        val sharedPref = getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)
//        val editor = sharedPref.edit()
//
//        if (notificationsEnabled) {
//            bell.setImageResource(R.drawable.notifications_active)
//            Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show()
//            editor.putBoolean("college_hall_notifications", true)
//        } else {
//            bell.setImageResource(R.drawable.notifications_none)
//            Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show()
//            editor.putBoolean("college_hall_notifications", false)
//        }
//        editor.apply()
//    }

//    // Check if notifications are enabled
//    private fun areNotificationsEnabled(): Boolean {
//        val sharedPref = getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)
//        return sharedPref.getBoolean("college_hall_notifications", false)
//    }
}