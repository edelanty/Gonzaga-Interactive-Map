package com.example.aeefinalgroupproject

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.CheckBox
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.material.navigation.NavigationView


class HomeActivity : AppCompatActivity(), OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {
    private lateinit var mMap: GoogleMap
    private var isFoodChecked = false
    private var isStudySpotsChecked = false
    private var isClassroomsChecked = false
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Set up the drawer layout (hamburger menu)
        drawerLayout = findViewById(R.id.drawer_layout)
        val menuButton: ImageButton = findViewById(R.id.menu_button)

        // Listen for drawer open/close
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        // Open navigation drawer
        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Enable drawer icon in the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Navigation view item selection listener
        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener(this)

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

        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val zoomInButton: ImageButton = findViewById(R.id.zoom_in_button)
        val zoomOutButton: ImageButton = findViewById(R.id.zoom_out_button)

        zoomInButton.setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.zoomIn())
        }

        zoomOutButton.setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.zoomOut())
        }
    }

    // Navigation menu options
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }
            R.id.nav_settings -> {
                return true
            }
            R.id.nav_login -> {
                return true
            }
        }
        return false
    }

    // Opening/closing the drawer when icon is clicked
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
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

        limitMapBounds(mMap)
        drawRestrictedShape(mMap)

        // Add a marker at Gonzaga and move the camera
        val gonzaga = LatLng(47.667191, -117.402382)
        mMap.addMarker(MarkerOptions().position(gonzaga).title("Marker at Gonzaga"))

        // Zoom in on location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gonzaga, 16f))
    }

    private fun drawRestrictedShape(mMap: GoogleMap) {
        val outerBounds = listOf(
            LatLng(47.700, -117.440), // top-left corner
            LatLng(47.700, -117.360), // top-right corner
            LatLng(47.630, -117.360), // bottom-right corner
            LatLng(47.630, -117.440)  // bottom-left corner
        )

        //Define inner boundary, campus and freshman housing
        val innerBounds = listOf(
            LatLng(47.67063, -117.41117), // top-left of campus
            LatLng(47.67063, -117.394903), // top-right of campus
            LatLng(47.66174, -117.394903), // bottom-right of campus
            LatLng(47.66174, -117.41117)  // bottom-left of campus

        )

        //Creating the shape
        val restrictedArea = mMap.addPolygon(
            PolygonOptions()
                .addAll(outerBounds) // Outer boundary (visible)
                .addHole(innerBounds) // Inner boundary (hollow)
                .strokeColor(0xFFFF0000.toInt()) // Red outline
                .strokeWidth(5f) // Border width
                .fillColor(0x44FF0000.toInt()) // Transparent red fill
        )
    }

    //Limits the scrolling bounds for the user
    private fun limitMapBounds(mMap: GoogleMap) {
        val southWest = LatLng(47.66174, -117.41117)
        val northEast = LatLng(47.67063, -117.394903)
        val gonzagaBounds = LatLngBounds(southWest, northEast)

        mMap.setLatLngBoundsForCameraTarget(gonzagaBounds)

        //Limits zooming
        mMap.setMinZoomPreference(15f)
        mMap.setMaxZoomPreference(19f)
    }
}
