package com.example.proje.viewmmodels

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proje.R
import com.example.proje.network.RetrofitInstance
import com.example.proje.model.Badge
import com.example.proje.adapters.BadgeListAdapter
import kotlinx.coroutines.launch

class BadgeListingActivity : AppCompatActivity(), BadgeListAdapter.OnItemClickListener {

    private lateinit var recyclerViewAllBadges: RecyclerView
    private lateinit var badgeListAdapter: BadgeListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_badge_listing)

        recyclerViewAllBadges = findViewById(R.id.recyclerViewAllBadges)

        badgeListAdapter = BadgeListAdapter(emptyList(), this) // Pass empty list and listener
        recyclerViewAllBadges.layoutManager = LinearLayoutManager(this)
        recyclerViewAllBadges.adapter = badgeListAdapter

        fetchAllBadges()
    }

    private fun fetchAllBadges() {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getAllBadges()

                if (response.isSuccessful) {
                    val badges = response.body()
                    if (badges != null) {
                        badgeListAdapter.updateData(badges)
                    } else {
                        Toast.makeText(this@BadgeListingActivity, "Failed to load badges", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@BadgeListingActivity, "Error fetching badges: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@BadgeListingActivity, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    // Implementation of the OnItemClickListener interface
    override fun onItemClick(badge: Badge) {
        // Navigate to Badge Details page
        val intent = Intent(this, BadgeDetailsActivity::class.java)
        intent.putExtra("BADGE_ID", badge.badge_id)
        startActivity(intent)
    }
}