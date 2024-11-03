package com.example.aeefinalgroupproject

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class HomeActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private var isFoodChecked = false
    private var isStudySpotsChecked = false
    private var isClassroomsChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Load filter states
        loadFilterStates()

        // Filter button
        val filtersButton: ImageButton = findViewById(R.id.filters_button)
        filtersButton.setOnClickListener {
            showFilterSelection()
        }

        // Favorites button
        val favoritesButton: ImageButton = findViewById(R.id.favorites_button)
        favoritesButton.setOnClickListener {
            val intent = Intent(this, FavoritesActivity::class.java)
            startActivity(intent)
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // Filter Selection popup menu (checkboxes)
    private fun showFilterSelection() {
        val dialogView = layoutInflater.inflate(R.layout.filter_checkboxes, null)

        // Create AlertDialog
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView).setCancelable(true)

        val dialog = dialogBuilder.create()
        dialog.show()

        // Checkboxes and cancel and confirm button
        val checkFilterFood = dialogView.findViewById<CheckBox>(R.id.check_filter_food)
        val checkFilterStudySpots = dialogView.findViewById<CheckBox>(R.id.check_filter_study_spots)
        val checkFilterClassrooms = dialogView.findViewById<CheckBox>(R.id.check_filter_classrooms)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirm_button)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancel_button)
        val clearButton = dialogView.findViewById<Button>(R.id.clear_button)

        // Set checkbox based on vars
        checkFilterFood.isChecked = isFoodChecked
        checkFilterStudySpots.isChecked = isStudySpotsChecked
        checkFilterClassrooms.isChecked = isClassroomsChecked

        // Add logic later (after database and pins)
        confirmButton.setOnClickListener {
            isFoodChecked = checkFilterFood.isChecked
            isStudySpotsChecked = checkFilterStudySpots.isChecked
            isClassroomsChecked = checkFilterClassrooms.isChecked
            // Save states
            saveFilterStates()
            dialog.dismiss()
        }

        // Cancel (close dialog)
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        // Clear options
        clearButton.setOnClickListener {
            // Clear checkboxes but still need to confirm
            checkFilterFood.isChecked = false
            checkFilterStudySpots.isChecked = false
            checkFilterClassrooms.isChecked = false
        }
    }

    // Save filter states (shared preferences)
    private fun saveFilterStates() {
        val sharedPref = getSharedPreferences("filter_prefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("food_filter", isFoodChecked)
        editor.putBoolean("study_spots_filter", isStudySpotsChecked)
        editor.putBoolean("classrooms_filter", isClassroomsChecked)
        editor.apply()
    }

    // Load filter states (shared preferences)
    private fun loadFilterStates() {
        val sharedPref = getSharedPreferences("filter_prefs", Context.MODE_PRIVATE)
        isFoodChecked = sharedPref.getBoolean("food_filter", false)
        isStudySpotsChecked = sharedPref.getBoolean("study_spots_filter", false)
        isClassroomsChecked = sharedPref.getBoolean("classrooms_filter", false)
    }

    // Add marker and move camera and zoom)
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker at Gonzaga and move the camera
        val gonzaga = LatLng(47.667191, -117.402382)
        mMap.addMarker(MarkerOptions().position(gonzaga).title("Marker at Gonzaga"))

        // Zoom in on location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gonzaga, 16f))
    }
}