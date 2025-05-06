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
import com.example.proje.adapters.CertificationProgressAdapter // Reuse adapter
import kotlinx.coroutines.launch
import android.util.Log
import android.view.View
import android.widget.TextView
import com.example.proje.model.BasicResponse // Needed for potential error body parsing
import com.example.proje.viewmmodels.LoginRegistrationActivity // Import LoginRegistrationActivity for redirection
import com.example.proje.network.SessionManager // Import SessionManager
import com.google.gson.Gson // Needed for potential error body parsing
import kotlinx.coroutines.launch


class CertificationProgressActivity : AppCompatActivity() {

    private var currentUserId: Int = -1

    private lateinit var recyclerViewMyCertProgress: RecyclerView
    private lateinit var certAdapter: CertificationProgressAdapter
    private lateinit var textViewNoCerts: TextView // Added TextView for no certs


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_certification_progress)

        // --- Get User ID from SessionManager ---
        // Get the logged-in user's ID directly from SessionManager
        currentUserId = SessionManager.getUserId()

        // Check if the user ID is valid (i.e., user is logged in)
        if (currentUserId == -1) {
            // If no user ID is found, the user is not logged in.
            Toast.makeText(this, "Please log in.", Toast.LENGTH_SHORT).show()
            // Redirect the user back to the Login/Registration screen
            val intent = Intent(this, LoginRegistrationActivity::class.java)
            // Clear the activity stack
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish() // Finish the current activity
            return // Stop further execution of onCreate
        }

        // --- Find Views ---
        recyclerViewMyCertProgress = findViewById(R.id.recyclerViewMyCertProgress)
        textViewNoCerts = findViewById(R.id.textViewNoCerts) // Find the "no data" TextView


        // --- Initialize Adapter ---
        // You MUST create CertificationProgressAdapter class
        certAdapter = CertificationProgressAdapter(emptyList()) // Pass empty list initially
        recyclerViewMyCertProgress.layoutManager = LinearLayoutManager(this)
        recyclerViewMyCertProgress.adapter = certAdapter

        // Fetch certification progress using the user ID from SessionManager
        fetchCertificationProgress(currentUserId)
    }

    private fun fetchCertificationProgress(userId: Int) {
        Log.d("CertProgress", "Fetching certification progress for user ID: $userId") // Log start
        lifecycleScope.launch {
            try {
                // Reusing get_user_profile endpoint for simplicity,
                // as it already returns certification data.
                val response = RetrofitInstance.api.getUserProfile(userId)

                if (response.isSuccessful) {
                    val profileData = response.body()
                    if (profileData != null) {
                        Log.d("CertProgress", "Profile data fetched successfully.") // Log success
                        if (profileData.certifications.isNotEmpty()) {
                            Log.d("CertProgress", "Found ${profileData.certifications.size} certifications.") // Log count
                            // Update RecyclerView adapter with certification progress
                            certAdapter.updateData(profileData.certifications)
                            // Hide "no data" message and show RecyclerView
                            textViewNoCerts.visibility = View.GONE
                            recyclerViewMyCertProgress.visibility = View.VISIBLE
                        } else {
                            Log.d("CertProgress", "No certifications found for this user.") // Log empty list
                            Toast.makeText(this@CertificationProgressActivity, "No certification progress found.", Toast.LENGTH_SHORT).show()
                            certAdapter.updateData(emptyList()) // Ensure adapter is updated with empty list
                            // Show "no data" message and hide RecyclerView
                            textViewNoCerts.visibility = View.VISIBLE
                            recyclerViewMyCertProgress.visibility = View.GONE
                        }
                    } else {
                        Log.e("CertProgress", "Failed to load certification progress: profileData is null") // Log error
                        Toast.makeText(this@CertificationProgressActivity, "Failed to load certification progress", Toast.LENGTH_SHORT).show()
                        // Update adapter with empty list and show "no data" message
                        certAdapter.updateData(emptyList())
                        textViewNoCerts.visibility = View.VISIBLE
                        recyclerViewMyCertProgress.visibility = View.GONE
                    }
                } else {
                    // Handle API errors
                    val errorBodyString = response.errorBody()?.string()
                    val errorMessage = try {
                        errorBodyString?.let { Gson().fromJson(it, BasicResponse::class.java).message }
                    } catch (e: Exception) { null }
                    Log.e("CertProgress", "Error fetching certification progress: ${response.code()}, Message: $errorMessage, Body: $errorBodyString") // Log API error details
                    Toast.makeText(this@CertificationProgressActivity, errorMessage ?: "Error fetching certification progress: ${response.code()}", Toast.LENGTH_SHORT).show()
                    // Update adapter with empty list and show "no data" message
                    certAdapter.updateData(emptyList())
                    textViewNoCerts.visibility = View.VISIBLE
                    recyclerViewMyCertProgress.visibility = View.GONE
                }
            } catch (e: Exception) {
                // Handle network errors
                Log.e("CertProgress", "Network error fetching certification progress", e) // Log network error
                Toast.makeText(this@CertificationProgressActivity, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
                // Update adapter with empty list and show "no data" message
                certAdapter.updateData(emptyList())
                textViewNoCerts.visibility = View.VISIBLE
                recyclerViewMyCertProgress.visibility = View.GONE
            }
        }
    }
}