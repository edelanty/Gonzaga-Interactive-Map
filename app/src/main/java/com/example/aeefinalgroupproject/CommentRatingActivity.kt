package com.example.aeefinalgroupproject

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CommentRatingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment_rating)

        //Pin data from intent
        val locationName = intent.getStringExtra("locationName")
        val description = intent.getStringExtra("description")
        val rating = intent.getStringExtra("rating")

        //Setting the data
        val locationNameTextView = findViewById<TextView>(R.id.locationNameTextView)
        val descriptionTextView = findViewById<TextView>(R.id.descriptionTextView)
        val ratingTextView = findViewById<TextView>(R.id.ratingTextView)
        val locationImageView = findViewById<ImageView>(R.id.locationImageView)

        locationNameTextView.text = locationName
        descriptionTextView.text = description
        ratingTextView.text = "Rating: $rating"

        val backButton: ImageButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        //TODO setup images properly
        //TODO literally everything else on this screen
    }
}