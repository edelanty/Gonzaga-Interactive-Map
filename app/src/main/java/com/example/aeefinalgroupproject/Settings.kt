package com.example.aeefinalgroupproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ArrayAdapter
import android.widget.AdapterView

class Settings : AppCompatActivity() {

    private lateinit var spinner: Spinner
    private var pinType: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        // Handle system insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Retrieve pin type from SharedPreferences
        pinType = getPinStyleFromPreferences()

        // Set up the Home button
        val homeButton: ImageButton = findViewById(R.id.home_button)
        homeButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("pinType", pinType)
            startActivity(intent)
            finish()
        }

        // Initialize the spinner for pin styles
        spinner = findViewById(R.id.pin_style)
        setupSpinner()

        // Placeholder notification toggle logic
        setupNotificationToggle()
    }

    /**
     * Handles the spinner setup for selecting pin styles.
     */
    private fun setupSpinner() {
        val pinStyles = listOf("Default", "Thin", "Full")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, pinStyles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Set the spinner's selection based on saved preferences
        spinner.setSelection(pinType)

        // Listener for spinner item selection
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                pinType = position // Update pinType based on selection
                savePinStyleToPreferences(pinType)
                Toast.makeText(this@Settings, "Selected: ${pinStyles[position]}", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    /**
     * Saves the selected pin style to SharedPreferences.
     * @param pinType The selected pin style index.
     */
    private fun savePinStyleToPreferences(pinType: Int) {
        val sharedPreferences = getSharedPreferences("SettingsPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("pinStyle", pinType)
        editor.apply()
    }

    /**
     * Retrieves the saved pin style from SharedPreferences.
     * Defaults to 0 (Default style) if no value is saved.
     * @return The saved pin style index.
     */
    private fun getPinStyleFromPreferences(): Int {
        val sharedPreferences = getSharedPreferences("SettingsPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("pinStyle", 0) // Default to 0 if not set
    }

    // Placeholder notification toggle logic
    private fun setupNotificationToggle() {
        // Add code to handle notification toggle when implemented
    }
}
