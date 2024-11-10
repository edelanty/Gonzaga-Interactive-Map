package com.example.aeefinalgroupproject

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CommentRatingActivity : AppCompatActivity() {
    private var isFavorited = false

    @SuppressLint("MissingInflatedId")
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

        //TODO instead of using these locally found ones through the intent just query the database
        locationNameTextView.text = locationName
        descriptionTextView.text = description
        ratingTextView.text = "Rating: $rating"

        val backButton: ImageButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        //Switches out the drawable displayed depending on whether the pin is a favorite or not
        val favoriteButton: ImageButton = findViewById(R.id.favorite_button)
        favoriteButton.setOnClickListener {
            isFavorited = !isFavorited
            val icon = if (isFavorited) R.drawable.heart_filled else R.drawable.favorites_heart
            favoriteButton.setImageResource(icon)
            Toast.makeText(this, if (isFavorited) "Added to favorites" else "Removed from favorites", Toast.LENGTH_SHORT).show()

            //TODO add the boolean isFavorited to the database and use that instead of the one declared at the top
        }

        //TODO setup images properly
        //TODO literally everything else on this screen
    }
}