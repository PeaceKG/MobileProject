package com.example.proje.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proje.R // --- IMPORT CHANGED HERE ---
import com.example.proje.model.Certification // --- IMPORT CHANGED HERE --- // Data class for certification progress
import java.text.ParseException // Import for date parsing error handling
import java.text.SimpleDateFormat // Import for date formatting
import java.util.Locale // Import for locale


// Adapter for displaying a list of user's certification progress
class CertificationProgressAdapter(
    private var certifications: List<Certification> // List of Certification objects
) : RecyclerView.Adapter<CertificationProgressAdapter.CertificationViewHolder>() {

    // ViewHolder class - Holds references to the UI elements for one list item
    class CertificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewCertName: TextView = itemView.findViewById(R.id.textViewCertName)
        val textViewCertDescription: TextView = itemView.findViewById(R.id.textViewCertDescription)
        val textViewCertStatus: TextView = itemView.findViewById(R.id.textViewCertStatus)
        val textViewCertCompletionDate: TextView = itemView.findViewById(R.id.textViewCertCompletionDate)
        // val progressBarCert: ProgressBar = itemView.findViewById(R.id.progressBarCert) // Uncomment if adding progress bar
        // val textViewRequiredBadges: TextView = itemView.findViewById(R.id.textViewRequiredBadges) // Uncomment if adding required badges list


        // Function to bind data to the views
        fun bind(certification: Certification) {
            textViewCertName.text = certification.cert_name
            textViewCertDescription.text = certification.description
            textViewCertStatus.text = "Status: ${certification.status}"

            // Show/hide completion date based on status
            if (certification.status == "Completed" && certification.completion_date != null) {
                textViewCertCompletionDate.visibility = View.VISIBLE
                 // Basic date formatting
            } else {
                textViewCertCompletionDate.visibility = View.GONE
            }

            // TODO: Update progress bar or display required badges based on 'required_badges' and user's earned badges
            // This requires more complex logic, potentially fetching user_badges here or passing them down
            // progressBarCert.progress = calculateProgress(certification)
            // textViewRequiredBadges.text = formatRequiredBadges(certification.required_badges)

            // TODO: Add click listener for details or sharing if desired
            // itemView.setOnClickListener { /* Handle item click */ }
        }

        // Example placeholder function for calculating progress (needs earned badge data)
        // private fun calculateProgress(certification: Certification): Int { /* ... */ return 0 }

        // Example placeholder function for formatting required badges string
        // private fun formatRequiredBadges(requiredBadges: String?): String { /* ... */ return "" }
    }

    // Called when RecyclerView needs a new ViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CertificationViewHolder {
        // Inflate the item layout
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_certification_progress, parent, false) // Using item_certification_progress.xml
        return CertificationViewHolder(view)
    }

    // Called to display the data at the specified position.
    override fun onBindViewHolder(holder: CertificationViewHolder, position: Int) {
        val certification = certifications[position]
        holder.bind(certification)
    }

    // Returns the total number of items.
    override fun getItemCount(): Int = certifications.size

    // Helper function to update the list data and notify the adapter
    fun updateData(newCertifications: List<Certification>) {
        certifications = newCertifications
        notifyDataSetChanged() // Informs the adapter that the data set has changed
        // Consider DiffUtil for better performance
    }
}