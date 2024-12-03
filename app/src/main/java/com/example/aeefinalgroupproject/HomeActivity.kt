package com.example.aeefinalgroupproject

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

class HomeActivity : AppCompatActivity(), OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {
    private lateinit var mMap: GoogleMap
    private var isFoodChecked = false
    private var isStudySpotsChecked = false
    private var isClassroomsChecked = false
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var navigationHeaderUserName: TextView
    private lateinit var navigationView: NavigationView
    private var pinStyle = 0
    private var currentUsername = "Guest"
    private var selectedImageUri: Uri? = null
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private val firebase = Firebase()
    private lateinit var auth: FirebaseAuth
    private lateinit var currentImagePreview: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Login
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Firebase app check for image
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance())

        // Notifications
        firebase.initializeNotifications(this)
        firebase.listenForComments()

        // Add Image Launcher
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                selectedImageUri = result.data?.data
                // Image uri
                currentImagePreview.setImageURI(selectedImageUri)
                currentImagePreview.visibility = View.VISIBLE
                Toast.makeText(this, "Image selected success", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Didn't select image", Toast.LENGTH_SHORT).show()
            }
        }

        // Ensure only logged-in users can access this activity
        checkUser()

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
        navigationView = findViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener(this)

        displayUserNameOnNavigation()

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

    //Displays the logged in users name on the navigation bar
    private fun displayUserNameOnNavigation() {
        val headerView = navigationView.getHeaderView(0)

        //Find the TextView within the header layout
        navigationHeaderUserName = headerView.findViewById(R.id.user_name_header)

        val currentUser = auth.currentUser

        // Check if the user is logged in
        if (currentUser != null) {
            val userId = currentUser.uid

            // Reference to the Firebase Firestore or Realtime Database
            val db = FirebaseFirestore.getInstance()  // Firestore instance (or FirebaseDatabase if using Realtime Database)
            val userRef = db.collection("users").document(userId)  // Assuming you're storing usernames in a 'users' collection

            // Fetch the username from Firestore
            userRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    currentUsername =
                        document.getString("username").toString()  // Assuming 'username' field in Firestore
                    navigationHeaderUserName.text = currentUsername // Default to guest if username is null
                } else {
                    Log.d("Firebase", "User not found in database.")
                }
            }.addOnFailureListener { exception ->
                Log.d("Firebase", "Error fetching user data", exception)
            }
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
                val intent = Intent(this, Settings::class.java)
                startActivity(intent)
                finish()
                return true
            }
            R.id.nav_logout -> {
                auth.signOut()
                navigateToLogin()
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

            //Only display pins with proper filter attributes
            filterPins(mMap)

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

        //Add a marker at Gonzaga and move the camera
        val gonzaga = LatLng(47.667191, -117.402382)

        //Zoom in on location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gonzaga, 16f))

        // Retrieve the pin style from SharedPreferences
        pinStyle = getPinStyleFromPreferences()

        //load pins type s
        loadPins(mMap)
    }

    private fun getPinStyleFromPreferences(): Int {
        val sharedPreferences: SharedPreferences = getSharedPreferences("SettingsPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("pinStyle", 0)  // Default is 0 (Default Pin Style)
    }

    private fun getMarkerIconFromDrawable(drawableId: Int, width: Int, height: Int): BitmapDescriptor {
        val drawable = ContextCompat.getDrawable(this, drawableId) ?: return BitmapDescriptorFactory.defaultMarker()
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        // Scale the bitmap to the desired width and height
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)

        // Return a BitmapDescriptor created from the scaled bitmap
        return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
    }

    private fun filterPins(mMap: GoogleMap) {
        //Remove all the pins from the map after filter state is changed
        mMap.clear()

        drawRestrictedShape(mMap)

        firebase.getGlobalPins { pinList ->
            //Check if the pin list is empty
            if (pinList.isEmpty()) {
                Log.d("LoadPins", "No global pins found.")
                return@getGlobalPins
            }

            //Iterate through the list of global pins
            for (pinData in pinList) {
                val latitude = pinData["latitude"] as? Double ?: continue
                val longitude = pinData["longitude"] as? Double ?: continue
                val locationName = pinData["locationName"] as? String ?: "Unknown"
                val description = pinData["description"] as? String ?: ""
                val isPinFoodSpot = pinData["isFoodCheck"] as? Boolean ?: false
                val isPinStudySpot = pinData["isStudySpot"] as? Boolean ?: false
                val isPinClassroomSpot = pinData["isClassroom"] as? Boolean?: false

                val latLng = LatLng(latitude, longitude)

                //Only add the pins with the correct filters
                if ((isFoodChecked && isPinFoodSpot || !isFoodChecked) && (isStudySpotsChecked && isPinStudySpot || !isStudySpotsChecked) && (isClassroomsChecked && isPinClassroomSpot || !isClassroomsChecked)) {
                    // Set marker icon based on the selected pin style
                    val markerIcon = when (pinStyle) {
                        1 -> getMarkerIconFromDrawable(R.drawable.pin, 80, 80)  // Pin Style
                        2 -> getMarkerIconFromDrawable(R.drawable.full_pin, 80, 80)  // Circle Style
                        else -> getMarkerIconFromDrawable(R.drawable.default_pin, 80, 80)  // Default Style
                    }
                    mMap.addMarker(
                        MarkerOptions().position(latLng).title(locationName).snippet(description).icon(markerIcon)
                    )
                }
            }
        }
    }

    private fun loadPins(mMap: GoogleMap) {
        firebase.getGlobalPins { pinList ->
            //Check if the pin list is empty
            if (pinList.isEmpty()) {
                Log.d("LoadPins", "No global pins found.")
                return@getGlobalPins
            }

            //Iterate through the list of global pins and add them to the map
            for (pinData in pinList) {
                val latitude = pinData["latitude"] as? Double ?: continue
                val longitude = pinData["longitude"] as? Double ?: continue
                val locationName = pinData["locationName"] as? String ?: "Unknown"
                val description = pinData["description"] as? String ?: ""

                val latLng = LatLng(latitude, longitude)

                // Set marker icon based on the selected pin style
                val markerIcon = when (pinStyle) {
                    1 -> getMarkerIconFromDrawable(R.drawable.pin, 80, 80)  // Pin Style
                    2 -> getMarkerIconFromDrawable(R.drawable.full_pin, 80, 80)  // Circle Style
                    else -> getMarkerIconFromDrawable(R.drawable.default_pin, 80, 80)  // Default Style
                }
                mMap.addMarker(
                    MarkerOptions().position(latLng).title(locationName).snippet(description).icon(markerIcon)
                )
            }
        }
    }

    // used to register commentRatingActivity for a callback to see if pin deleted
    private val commentRatingLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val deletedPinName = data?.getStringExtra("deletedPinName")
            if (deletedPinName != null) {
                // Refresh the map to reflect the deletion
                refreshPins()
            }
        }
    }

    private fun setupPinListeners(mMap: GoogleMap) {
        //Long Press to add a pin
        mMap.setOnMapLongClickListener { latLng ->
            showAddPinDialog(latLng)
        }

        //If a user presses on a pin
        mMap.setOnMarkerClickListener { marker ->
            val intent = Intent(this, CommentRatingActivity::class.java)
            intent.putExtra("locationName", marker.title)
            commentRatingLauncher.launch(intent)
            true
        }
    }

    // for deletion of pins
    private fun refreshPins() {
        // Logic to reload the pins on the map
        mMap.clear()
        drawRestrictedShape(mMap)
        loadPins(mMap)
    }

    // Add pin dialog box
    private fun showAddPinDialog(latLng: LatLng) {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_pin, null)

        dialogBuilder.setView(dialogView)
        val locationNameInput = dialogView.findViewById<EditText>(R.id.pin_name_input)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.pin_description_input)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.pin_rating_bar)
        val foodCheckBox = dialogView.findViewById<CheckBox>(R.id.checkbox_food)
        val studySpotCheckBox = dialogView.findViewById<CheckBox>(R.id.checkbox_study_spot)
        val classroomCheckBox = dialogView.findViewById<CheckBox>(R.id.checkbox_classroom)
        val selectImageButton = dialogView.findViewById<Button>(R.id.select_image_button)

        // Image uri
        selectedImageUri = null
        val imagePreview = dialogView.findViewById<ImageView>(R.id.image_preview)
        currentImagePreview = dialogView.findViewById<ImageView>(R.id.image_preview)
        currentImagePreview.visibility = View.GONE
        imagePreview.visibility = View.GONE

        // Image button
        selectImageButton.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            imagePickerLauncher.launch(intent)
        }

        if (!markerIsInValidRange(latLng)) {
            Toast.makeText(this, "Invalid Pin", Toast.LENGTH_LONG).show()
            return
        }

        dialogBuilder.setPositiveButton("Add Pin") { _, _ ->
            val locationName = locationNameInput.text.toString()
            val description = descriptionInput.text.toString()
            val rating = ratingBar.rating
            val foodFilter = foodCheckBox.isChecked
            val studySpotFilter = studySpotCheckBox.isChecked
            val classroomFilter = classroomCheckBox.isChecked

            //Checking for empty attributes
            if (locationName.isBlank() || description.isBlank()) {
                Toast.makeText(this, "Entry Blank", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            // Check for selected image
            if (selectedImageUri != null) {
                uploadImageToFirebase(selectedImageUri!!) {imageUrl ->
                    savePinToFirebase(latLng, locationName, description, rating, foodFilter, studySpotFilter, classroomFilter, imageUrl)
                }
            } else {
                savePinToFirebase(latLng, locationName, description, rating, foodFilter, studySpotFilter, classroomFilter, null)
            }

            // Set marker icon based on the selected pin style
            val markerIcon = when (pinStyle) {
                1 -> getMarkerIconFromDrawable(R.drawable.pin, 80, 80)  // Pin Style
                2 -> getMarkerIconFromDrawable(R.drawable.full_pin, 80, 80)  // Circle Style
                else -> getMarkerIconFromDrawable(R.drawable.default_pin, 80, 80)  // Default Style
            }
            mMap.addMarker(
                MarkerOptions().position(latLng).title(locationName).snippet(description).icon(markerIcon)
            )
        }

        dialogBuilder.setNegativeButton("Cancel", null)
        dialogBuilder.create().show()
    }

    // Upload image to firebase to use in rating activity
    private fun uploadImageToFirebase(imageUri: Uri, callback:(String?) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("pin_images/${UUID.randomUUID()}.jpg")
        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    callback(downloadUri.toString())
                }.addOnFailureListener { exception ->
                    Log.e("Firebase", "Failed to get download URL: ${exception.message}")
                    Toast.makeText(this, "Failed to get image URL", Toast.LENGTH_SHORT).show()
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Failed to upload image: ${exception.message}")
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                callback(null)
            }
    }

    // Save pin to firebase with pin information
    private fun savePinToFirebase(latLng: LatLng, locationName: String, description: String, rating: Float, foodFilter: Boolean, studySpotFilter: Boolean, classroomFilter: Boolean, imageUrl: String?) {
        val userIdString = auth.currentUser?.uid.toString()
        val userName = navigationHeaderUserName.text.toString()
        val pinData = mapOf(
                "latitude" to latLng.latitude,
                "longitude" to latLng.longitude,
                "locationName" to locationName,
                "description" to description,
                "rating" to rating,
                "userId" to userIdString,
                "userName" to userName,
                "isFoodCheck" to foodFilter,
                "isStudySpot" to studySpotFilter,
                "isClassroom" to classroomFilter,
                "likeCount" to 0,
                "dislikeCount" to 0,
                "commentCount" to 0,
                "imageUrl" to (imageUrl?: "")
            )
        firebase.addGlobalPin(locationName, pinData)
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

    // LOGIN FUNCTIONS
    private fun checkUser() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.d("FirebaseAuth", "No user logged in, redirecting to login")
            navigateToLogin()
        } else {
            Log.d("FirebaseAuth", "Welcome ${currentUser.email}")
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, EmailPasswordActivity::class.java)
        startActivity(intent)
        finish()
    }
}
