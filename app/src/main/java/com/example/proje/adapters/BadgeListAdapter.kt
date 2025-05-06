package com.example.proje.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load // Import Coil
import com.example.proje.R
import com.example.proje.model.Badge

class BadgeListAdapter(
    private var badges: List<Badge>, // List of Badge objects
    private val listener: OnItemClickListener // Listener for clicks
) : RecyclerView.Adapter<BadgeListAdapter.BadgeViewHolder>() {

    // Interface for click listener
    interface OnItemClickListener {
        fun onItemClick(badge: Badge)
    }

    // ViewHolder class
    class BadgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewIcon: ImageView = itemView.findViewById(R.id.imageViewBadgeIconItem)
        val textViewName: TextView = itemView.findViewById(R.id.textViewBadgeNameItem)
        val textViewDescription: TextView = itemView.findViewById(R.id.textViewBadgeDescriptionItem)

        fun bind(badge: Badge, clickListener: OnItemClickListener) {
            textViewName.text = badge.badge_name
            textViewDescription.text = badge.description

            // Use Coil to load image from URL
            imageViewIcon.load(badge.icon_url) {
                placeholder(R.drawable.ic_badge_placeholder) // Placeholder while loading
                error(R.drawable.ic_badge_placeholder) // Image to show if loading fails
            }

            itemView.setOnClickListener {
                clickListener.onItemClick(badge)
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_badge_list, parent, false)
        return BadgeViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        val badge = badges[position]
        holder.bind(badge, listener)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int = badges.size

    // Function to update the data in the adapter
    fun updateData(newBadges: List<Badge>) {
        badges = newBadges
        notifyDataSetChanged() // Notify the adapter that the data set has changed
    }
}