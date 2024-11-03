package com.example.aeefinalgroupproject

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
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

        // Filter button
        val filtersButton: ImageButton = findViewById(R.id.filters_button)
        filtersButton.setOnClickListener {
            showFilterSelection(it)
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // Filter Selection popup menu (checkboxes)
    private fun showFilterSelection(view: View) {
        val dialogView = layoutInflater.inflate(R.layout.filter_checkboxes, null)

        // Create AlertDialog
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)

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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val gonzaga = LatLng(47.667191, -117.402382)
        mMap.addMarker(MarkerOptions().position(gonzaga).title("Marker at Gonzaga"))

        // Zoom in on the Gonzaga location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gonzaga, 16f))
    }
}