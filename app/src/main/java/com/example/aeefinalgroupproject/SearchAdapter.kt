package com.example.aeefinalgroupproject

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SearchAdapter(private val context: Context, var pins: List<Map<String, Any>>) : RecyclerView.Adapter<SearchAdapter.PinViewHolder>() {

    // ViewHolder class
    inner class PinViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val locationNameTextView: TextView = itemView.findViewById(R.id.locationNameTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
    }

    // Method to update the list of pins and notify adapter
    fun updatePins(newPins: List<Map<String, Any>>) {
        pins = newPins
        notifyDataSetChanged()
    }

    // Bind the data to the ViewHolder
    override fun onBindViewHolder(holder: PinViewHolder, position: Int) {
        val pin = pins[position]
        val locationName = pin["locationName"] as? String ?: "Unknown Location"
        val description = pin["description"] as? String ?: "No description available"

        holder.locationNameTextView.text = locationName
        holder.descriptionTextView.text = description
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
