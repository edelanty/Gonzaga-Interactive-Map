package com.example.aeefinalgroupproject

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

class SearchAdapter(
    private val context: Context,
    private var pins: List<Map<String, Any>>
) : RecyclerView.Adapter<SearchAdapter.PinViewHolder>() {

    // ViewHolder class
    inner class PinViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pinButton: Button = itemView.findViewById(R.id.pin_button)
    }

    // Method to update the list of pins and notify the adapter
    fun updatePins(newPins: List<Map<String, Any>>) {
        pins = newPins
        notifyDataSetChanged()
    }

    // Bind the data to the ViewHolder
    override fun onBindViewHolder(holder: PinViewHolder, position: Int) {
        val pin = pins[position]
        val locationName = pin["locationName"] as? String ?: "Unknown Location"

        // Set the button text and click listener
        holder.pinButton.text = locationName
        holder.pinButton.setOnClickListener {
            val intent = Intent(context, CommentRatingActivity::class.java)
            intent.putExtra("locationName", locationName)
            context.startActivity(intent)
        }
    }

    // Inflate the view and return the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PinViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.search_item, parent, false)
        return PinViewHolder(view)
    }

    // Return the size of the list
    override fun getItemCount(): Int {
        return pins.size
    }
}
