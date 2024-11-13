package com.example.aeefinalgroupproject

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
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

    private val firebase = Firebase()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Set up the drawer layout (hamburger menu)
        drawerLayout = findViewById(R.id.drawer_layout)
        val menuButton: ImageButton = findViewById(R.id.menu_button)

        // remove default_layout in case it got added
//        firebase.removeFavorite("default_view") { success ->
//            if (success) {
//                Log.d("Firebase", "Favorite successfully removed.")
//            } else {
//                Log.e("Firebase", "Failed to remove favorite.")
//            }
//        }

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
        setupPinListeners(mMap)

        // Add a marker at Gonzaga and move the camera
        val gonzaga = LatLng(47.667191, -117.402382)

        // Zoom in on location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gonzaga, 16f))

        // load pins type s
        loadPins(mMap)
    }

    private fun loadPins(mMap: GoogleMap) {
        // rn gonna add a bunch to the database
//        val pinData = hashMapOf( // create a bunch of pins
//            "latitude" to latLng.latitude,
//            "longitude" to latLng.longitude,
//            "locationName" to locationName,
//            "description" to description,
//            "rating" to rating,
//        )
//        firebase.addPin(locationName, pinData)

        val pinData1 = hashMapOf( // example adding college_hall
            "xmlName" to "f_college_hall",
            "latitude" to 47.66811,
            "longitude" to -117.40255,
            "locationName" to "College_Hall",
            "description" to "This building is the main building for classrooms " +
                    "and classes. Many core classes are held here and important " +
                    "offices like Office of the Registrar, etc.",
            "rating" to 0
        )
        firebase.addPin("College_Hall", pinData1)
        val latLnggg = LatLng(47.66811, -117.40255)
        val descriptionn = "This building is the main building for classrooms " +
                "and classes. Many core classes are held here and important " +
                "offices like Office of the Registrar, etc.";

        // Add the pin to the map
        mMap.addMarker(
            MarkerOptions().position(latLnggg).title("College_Hall").snippet(descriptionn)
        )
        val pinData = hashMapOf( // example adding hemm
            "xmlName" to "f_hemmingson",
            "latitude" to 47.66711,
            "longitude" to -117.39914,
            "locationName" to "Hemmingson",
            "description" to "This building is the center of campus activity. It " +
                    "holds the dining hall (COG), many office rooms, and is a great" +
                    " place for students to hang out and study.",
            "rating" to 0
        )
        firebase.addPin("Hemmingson", pinData) // "Hemmingson" is the key in db
        val latLngg = LatLng(47.66711, -117.39914)
        val descriptionnn = "This building is the center of campus activity. It " +
                "holds the dining hall (COG), many office rooms, and is a great" +
                " place for students to hang out and study.";

        // Add the pin to the map
        mMap.addMarker(
            MarkerOptions().position(latLngg).title("Hemmingson").snippet(descriptionnn)
        )


        firebase.getAllPins { pinList ->
            // Iterate through the list of pins and add them to the map
            for (pinData in pinList) {
                val latitude = pinData["latitude"] as? Double ?: continue
                val longitude = pinData["longitude"] as? Double ?: continue
                val locationName = pinData["locationName"] as? String ?: "Unknown"
                val description = pinData["description"] as? String ?: ""

                val latLng = LatLng(latitude, longitude)

                // Add the pin to the map
                mMap.addMarker(
                    MarkerOptions().position(latLng).title(locationName).snippet(description)
                )
            }
        }
    }

    private fun setupPinListeners(mMap: GoogleMap) {
        //Long Press to add a pin
        mMap.setOnMapLongClickListener { latLng ->
            //HARDCODED FOR NOW--------------------------
            //firebase.addFavorite("f_college_hall", 1, false)
            //firebase.addFavorite("f_hemmingson", 1, true)
            //MOVE THIS TO SOMEWHERE ELSE
            showAddPinDialog(latLng)
        }

        //If a user presses on a pin
        mMap.setOnMarkerClickListener { marker ->
            val intent = Intent(this, CommentRatingActivity::class.java)
            intent.putExtra("locationName", marker.title)
            intent.putExtra("description", marker.snippet)
            intent.putExtra("rating", "?")
            startActivity(intent)
            true
        }
    }

    private fun showAddPinDialog(latLng: LatLng) {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_pin, null)

        dialogBuilder.setView(dialogView)
        val locationNameInput = dialogView.findViewById<EditText>(R.id.pin_name_input)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.pin_description_input)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.pin_rating_bar)

        if (!markerIsInValidRange(latLng)) {
            Toast.makeText(this, "Invalid Pin", Toast.LENGTH_LONG).show()
            return
        }

        dialogBuilder.setPositiveButton("Add Pin") { _, _ ->
            //All need to be stored somewhere i.e. a database or if we just go local for time
            val locationName = locationNameInput.text.toString()
            val description = descriptionInput.text.toString()
            val rating = ratingBar.rating

            //Checking for empty attributes
            if (locationName.isBlank() || description.isBlank()) {
                Toast.makeText(this, "Entry Blank", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            mMap.addMarker(
                MarkerOptions().position(latLng).title(locationName).snippet(description)
            )

            //TODO Send pin data to eventual database
        }
        dialogBuilder.setNegativeButton("Cancel", null)
        dialogBuilder.create().show()
    }

    //Determines if a long press is in the valid range of the campus
    private fun markerIsInValidRange(latLng: LatLng): Boolean {
        return (latLng.latitude in 47.66174..47.67063) && (latLng.longitude in -117.41117..-117.394903)
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
