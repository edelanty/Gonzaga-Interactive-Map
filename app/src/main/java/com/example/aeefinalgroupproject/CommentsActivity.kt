package com.example.aeefinalgroupproject

import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        // Retrieve locationName and userName from Intent
        val locationName = intent.getStringExtra("locationName")!!
        val userName = intent.getStringExtra("userName") ?: "Anonymous"

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

        // Fetch and listen for real-time comment updates
        firebase.listenForComments(locationName) { updatedComments ->
            // Clear the existing comments in the list
            commentsList.clear()

            // Map each comment (map) to a Comment object
            val mappedComments = updatedComments.mapNotNull { commentMap ->
                val userName = commentMap["userName"] as? String
                val content = commentMap["content"] as? String
                val timestamp = commentMap["timestamp"] as? Long

                // Ensure the required fields are not null
                if (userName != null && content != null && timestamp != null) {
                    Comment(userName, content, timestamp)
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

            firebase.addComment(locationName, userName, content) { success ->
                if (success) {
                    Toast.makeText(this, "Comment added", Toast.LENGTH_SHORT).show()
                    commentInput.text.clear()
                } else {
                    Toast.makeText(this, "Failed to add comment", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

data class Comment(
    val userName: String,
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
        holder.userNameTextView.text = comment.userName
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

