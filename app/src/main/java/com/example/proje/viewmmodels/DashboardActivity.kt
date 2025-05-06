package com.example.proje.viewmmodels

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proje.R
import com.example.proje.viewmmodels.CertificationProgressActivity
import com.example.proje.viewmmodels.SettingsActivity
import com.example.proje.network.SessionManager
import com.example.proje.viewmmodels.ProfileActivity
import com.google.gson.Gson // Needed for potential error body parsing
import kotlinx.coroutines.launch
import com.example.proje.network.RetrofitInstance // Import RetrofitInstance
import com.example.proje.model.BasicResponse // Needed for potential error body parsing
import com.example.proje.model.ProfileResponse //
import androidx.lifecycle.lifecycleScope

class DashboardActivity : AppCompatActivity() {

    private var currentUserId: Int = -1 // Store logged-in user ID

    private lateinit var textViewWelcome: TextView
    private lateinit var buttonViewProfile: Button
    private lateinit var buttonBrowseBadges: Button
    private lateinit var buttonViewCertProgress: Button
    private lateinit var buttonSettings: Button
    private lateinit var buttonLogout: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // --- Get User ID from SessionManager ---
        currentUserId = SessionManager.getUserId()

        if (currentUserId == -1) {
            // Should not happen if MainActivity works, but safety first
            Toast.makeText(this, "Session expired. Please log in.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginRegistrationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear back stack
            startActivity(intent)
            finish()
            return // Stop onCreate execution
        }

        // --- Find Views ---
        textViewWelcome = findViewById(R.id.textViewWelcome)
        buttonViewProfile = findViewById(R.id.buttonViewProfile)
        buttonBrowseBadges = findViewById(R.id.buttonBrowseBadges)
        buttonViewCertProgress = findViewById(R.id.buttonViewCertProgress)
        buttonSettings = findViewById(R.id.buttonSettings)
        buttonLogout = findViewById(R.id.buttonLogout)

        // --- Fetch and Display Username ---
        // Call the function to fetch the user's profile and update the welcome message
        fetchAndDisplayUsername(currentUserId)


        // --- Set Button Click Listeners ---
        buttonViewProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        buttonBrowseBadges.setOnClickListener {
            val intent = Intent(this, BadgeListingActivity::class.java)
            startActivity(intent)
        }

        buttonViewCertProgress.setOnClickListener {
            val intent = Intent(this, CertificationProgressActivity::class.java)
            startActivity(intent)
        }

        buttonSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }


        buttonLogout.setOnClickListener {
            // --- Clear Session using SessionManager ---
            SessionManager.clearSession()
            Log.d("Logout", "Session cleared. User ID is now: ${SessionManager.getUserId()}") // Log to confirm clear

            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()

            // --- Navigate back to LoginRegistrationActivity ---
            val intent = Intent(this, LoginRegistrationActivity::class.java)
            // These flags are crucial:
            // FLAG_ACTIVITY_NEW_TASK: Start the activity in a new task.
            // FLAG_ACTIVITY_CLEAR_TASK: Clear any existing task associated with this activity,
            //                            effectively removing all activities (including Dashboard)
            //                            from the current task stack.
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            finish() // Finish the Dashboard activity so it's not on the back stack
        }
    }

    // Function to fetch user profile and display the username
    private fun fetchAndDisplayUsername(userId: Int) {
        Log.d("DashboardActivity", "Fetching username for user ID: $userId")
        lifecycleScope.launch {
            try {
                // Make the API call to get the user's profile
                val response = RetrofitInstance.api.getUserProfile(userId)

                if (response.isSuccessful) {
                    val profileData = response.body()
                    if (profileData != null) {
                        // Extract the username from the fetched data
                        val username = profileData.user.username
                        Log.d("DashboardActivity", "Fetched username: $username")
                        // Update the welcome TextView with the username
                        textViewWelcome.text = "Welcome, $username!"
                    } else {
                        Log.e("DashboardActivity", "Failed to fetch username: profileData is null")
                        // Fallback to user ID or a generic message if data is null
                        textViewWelcome.text = "Welcome, User $userId!"
                    }
                } else {
                    // Handle API error
                    val errorBodyString = response.errorBody()?.string()
                    val errorMessage = try {
                        errorBodyString?.let { Gson().fromJson(it, BasicResponse::class.java).message }
                    } catch (e: Exception) { null }
                    Log.e("DashboardActivity", "Error fetching username: ${response.code()}, Message: $errorMessage")
                    // Fallback to user ID or a generic message on error
                    textViewWelcome.text = "Welcome, User $userId!"
                    Toast.makeText(this@DashboardActivity, "Could not load username.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Handle network error
                Log.e("DashboardActivity", "Network error fetching username", e)
                textViewWelcome.text = "Welcome, User $userId!" // Fallback on network error
                Toast.makeText(this@DashboardActivity, "Network error loading username.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}