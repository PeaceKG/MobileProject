package com.example.proje.viewmmodels

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load // Use Coil or Glide for image loading
import com.example.proje.R
import com.example.proje.network.RetrofitInstance
import kotlinx.coroutines.launch

class BadgeDetailsActivity : AppCompatActivity() {

    private var badgeId: Int = -1

    private lateinit var imageViewBadgeIcon: ImageView
    private lateinit var textViewBadgeName: TextView
    private lateinit var textViewBadgeDescription: TextView
    private lateinit var textViewBadgeCriteria: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_badge_details)

        badgeId = intent.getIntExtra("BADGE_ID", -1)
        if (badgeId == -1) {
            Toast.makeText(this, "Badge ID not provided.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        imageViewBadgeIcon = findViewById(R.id.imageViewBadgeIcon)
        textViewBadgeName = findViewById(R.id.textViewBadgeName)
        textViewBadgeDescription = findViewById(R.id.textViewBadgeDescription)
        textViewBadgeCriteria = findViewById(R.id.textViewBadgeCriteria)

        fetchBadgeDetails(badgeId)
    }

    private fun fetchBadgeDetails(badgeId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getBadgeDetails(badgeId)

                if (response.isSuccessful) {
                    val badgeDetails = response.body()
                    if (badgeDetails != null) {
                        // Update UI with badge details
                        textViewBadgeName.text = badgeDetails.badge_name
                        textViewBadgeDescription.text = badgeDetails.description
                        textViewBadgeCriteria.text = badgeDetails.criteria

                        // Load badge icon image using Coil or Glide
                        imageViewBadgeIcon.load(badgeDetails.icon_url) {
                            placeholder(R.drawable.ic_badge_placeholder) // Use your placeholder
                            error(R.drawable.ic_badge_placeholder) // Use your error drawable
                        }

                    } else {
                        Toast.makeText(this@BadgeDetailsActivity, "Failed to load badge details", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@BadgeDetailsActivity, "Error fetching badge details: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@BadgeDetailsActivity, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}