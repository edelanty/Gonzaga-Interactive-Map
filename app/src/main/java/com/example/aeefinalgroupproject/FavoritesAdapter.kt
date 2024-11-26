package com.example.aeefinalgroupproject

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FavoritesAdapter(
    private val context: Context,
    private var favoriteList: List<Map<String, Any>>,
    private val onRemove: (String) -> Unit,
    private val onToggleNotification: (String, Boolean) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.favorite_row, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val favorite = favoriteList[position]

        //Extract the location name and the isFavorite and bellStatus fields
        val locationName = favorite["locationName"] as? String ?: "Unknown"
        val isFavorite = favorite["isFavorite"] as? Boolean ?: false
        val bellStatus = favorite["bellStatus"] as? Boolean ?: false

        //Bind the location name and the bell status
        holder.favoriteName.text = locationName
        holder.notificationBell.setImageResource(
            if (bellStatus) R.drawable.notifications_active else R.drawable.notifications_none
        )

        //Handle favorite removal (set isFavorite to false)
        holder.removeFavorite.setOnClickListener {
            onRemove(locationName)
        }

        //Handle toggling the bell status (notifications on/off)
        holder.notificationBell.setOnClickListener {
            val newStatus = !bellStatus
            onToggleNotification(locationName, newStatus)
        }
    }

    override fun getItemCount(): Int = favoriteList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateFavorites(newList: List<Map<String, Any>>) {
        favoriteList = newList
        notifyDataSetChanged()
    }

    //ViewHolder for the favorite item row
    class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val favoriteName: TextView = itemView.findViewById(R.id.favorite_name)
        val removeFavorite: ImageButton = itemView.findViewById(R.id.remove_favorite)
        val notificationBell: ImageButton = itemView.findViewById(R.id.notification_bell)
    }
}
