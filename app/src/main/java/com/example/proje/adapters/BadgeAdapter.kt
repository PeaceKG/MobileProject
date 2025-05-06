package com.example.proje.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load // Import Coil
import com.example.proje.R // --- IMPORT CHANGED HERE ---
import com.example.proje.model.UserBadge // --- IMPORT CHANGED HERE --- // Data class for earned badges

/// Adapter for displaying a list of EARNED badges (e.g., on Profile page)
class BadgeAdapter(
    private var earnedBadges: List<UserBadge>, // First argument: List of UserBadge objects
    // Second argument: A lambda function that takes a UserBadge and returns Unit (nothing)
    private val shareClickListener: (UserBadge) -> Unit
) : RecyclerView.Adapter<BadgeAdapter.EarnedBadgeViewHolder>() {

    // ViewHolder class - Holds references to the UI elements for one list item
    class EarnedBadgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewIcon: ImageView = itemView.findViewById(R.id.imageViewEarnedBadgeIcon)
        val textViewName: TextView = itemView.findViewById(R.id.textViewEarnedBadgeName)
        val textViewEarnedDate: TextView = itemView.findViewById(R.id.textViewEarnedDate)
        val imageViewShare: ImageView = itemView.findViewById(R.id.imageViewShareBadge) // Find the share icon


        // Function to bind data to the views
        // This bind function now also accepts the share click listener lambda
        fun bind(userBadge: UserBadge, shareClickListener: (UserBadge) -> Unit) {
            textViewName.text = userBadge.badge_name

            // Check and Format Earned Date
            val earnedDateString = userBadge.earned_date

            if (!earnedDateString.isNullOrEmpty()) {
                val earnedDateFormatted = earnedDateString.split("T").getOrNull(0)

                if (!earnedDateFormatted.isNullOrEmpty()) {
                    textViewEarnedDate.text = "Earned on: $earnedDateFormatted"
                    textViewEarnedDate.visibility = View.VISIBLE
                } else {
                    textViewEarnedDate.visibility = View.GONE
                }
            } else {
                textViewEarnedDate.visibility = View.GONE
            }

            // Use Coil to load image from URL
            imageViewIcon.load(userBadge.icon_url) {
                placeholder(R.drawable.ic_badge_placeholder)
                error(R.drawable.ic_badge_placeholder)
            }

            // --- Set click listener for the share icon ---
            // When the share icon is clicked, execute the shareClickListener lambda
            imageViewShare.setOnClickListener {
                shareClickListener(userBadge) // Pass the current userBadge object to the lambda
            }

            // Optional: Add a click listener for the entire item view if needed
            // itemView.setOnClickListener { /* Handle item click */ }
        }
    }

    // Called when RecyclerView needs a new ViewHolder of the given type.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EarnedBadgeViewHolder {
        // Inflate the item layout (item_badge.xml)
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_badge, parent, false)
        return EarnedBadgeViewHolder(view)
    }

    // Called by RecyclerView to display the data at the specified position.
    override fun onBindViewHolder(holder: EarnedBadgeViewHolder, position: Int) {
        val userBadge = earnedBadges[position]
        // --- Pass the userBadge object AND the share click listener to the ViewHolder's bind function ---
        holder.bind(userBadge, shareClickListener)
    }

    // Returns the total number of items in the data set held by the adapter.
    override fun getItemCount(): Int = earnedBadges.size

    // Helper function to update the list data and notify the adapter
    fun updateData(newEarnedBadges: List<UserBadge>) {
        earnedBadges = newEarnedBadges
        notifyDataSetChanged() // Informs the adapter that the data set has changed
        // Consider DiffUtil for better performance on large lists
    }
}