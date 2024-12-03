package com.example.aeefinalgroupproject

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var commentInput: EditText
    private lateinit var submitButton: ImageButton
    private lateinit var commentsAdapter: CommentsAdapter
    private val commentsList = mutableListOf<Comment>() // Using the Comment data class
    private lateinit var backButton: ImageButton
    private val firebase = Firebase()
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        // Retrieve locationName from Intent
        val locationName = intent.getStringExtra("locationName")!!

        recyclerView = findViewById(R.id.comments_recycler_view)
        commentInput = findViewById(R.id.comment_input)
        submitButton = findViewById(R.id.comment_submit_button)
        backButton = findViewById(R.id.back_button)

        commentsAdapter = CommentsAdapter(commentsList)
        recyclerView.adapter = commentsAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        backButton.setOnClickListener {
            finish()
        }

        // Fetch the current logged-in user's username from Firebase Firestore
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            val userIdString = it.uid
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(userIdString)

            userRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    username = document.getString("username") ?: "Anonymous"
                } else {
                    username = "Anonymous" // If no username found, use default
                }
            }.addOnFailureListener {
                // Handle failure if needed
                Toast.makeText(this, "Failed to load username", Toast.LENGTH_SHORT).show()
                username = "Anonymous"
            }
        }

        // Fetch and listen for real-time comment updates
        firebase.listenForComments(locationName) { updatedComments ->
            // Clear the existing comments in the list
            commentsList.clear()

            // Map each comment (map) to a Comment object
            val mappedComments = updatedComments.mapNotNull { commentMap ->
                val content = commentMap["content"] as? String
                val timestamp = commentMap["timestamp"] as? Long
                val username = commentMap["username"] as? String ?: "Anonymous" // Get the username from Firebase data

                // Ensure the required fields are not null
                if (content != null && timestamp != null) {
                    Comment(username, content, timestamp)
                } else {
                    null
                }
            }

            // Add the mapped comments to the list
            commentsList.addAll(mappedComments)

            // Notify the adapter that data has changed
            commentsAdapter.notifyDataSetChanged()
        }

        // Submit a new comment
        submitButton.setOnClickListener {
            val content = commentInput.text.toString().trim()
            if (content.isBlank()) {
                Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Use the logged-in username for the new comment
            firebase.addComment(locationName, username, content) { success ->
                if (success) {
                    // Increment the comment count in the database
                    firebase.incrementCommentCount(locationName) { incrementSuccess ->
                        if (incrementSuccess) {
                            Toast.makeText(this, "Comment added", Toast.LENGTH_SHORT).show()
                            commentInput.text.clear()
                        } else {
                            Toast.makeText(this, "Failed to update comment count", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Failed to add comment", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

data class Comment(
    val username: String,
    val content: String,
    val timestamp: Long
)

class CommentsAdapter(private val commentsList: List<Comment>) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_item, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentsList[position]
        holder.userNameTextView.text = comment.username
        holder.contentTextView.text = comment.content
        // Format the timestamp if needed
        val timestamp = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault()).format(Date(comment.timestamp))
        holder.timestampTextView.text = timestamp
    }

    override fun getItemCount(): Int {
        return commentsList.size
    }

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userNameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
        val contentTextView: TextView = itemView.findViewById(R.id.contentTextView)
        val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
    }
}
