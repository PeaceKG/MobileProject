package com.example.proje.viewmmodels

import android.content.Intent
import android.os.Bundle
import android.util.Log // Import Log for logging
import android.view.View // Import View for visibility
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proje.R // Import R for resources
import com.example.proje.network.RetrofitInstance // Import RetrofitInstance
import com.example.proje.model.BasicResponse // Needed for potential error body parsing
import com.example.proje.model.UserBadge // Import UserBadge data class
import com.example.proje.adapters.BadgeAdapter // Import the updated BadgeAdapter
import com.example.proje.adapters.CertificationProgressAdapter // Import CertificationProgressAdapter (you need to create this)
import com.example.proje.viewmmodels.LoginRegistrationActivity // Import LoginRegistrationActivity for redirection
import com.example.proje.viewmmodels.ShareAchievementActivity // --- IMPORT THE NEW SHARE ACTIVITY ---
import com.example.proje.network.SessionManager // Import SessionManager
import com.google.gson.Gson // Needed for potential error body parsing
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    // Variable to store the ID of the currently logged-in user
    private var currentUserId: Int = -1

    // UI elements from the layout file
    private lateinit var textViewProfileName: TextView
    private lateinit var textViewProfileUsername: TextView
    private lateinit var textViewProfileBio: TextView
    private lateinit var recyclerViewEarnedBadges: RecyclerView
    private lateinit var recyclerViewCertProgress: RecyclerView
    // TextViews to show when there is no data in the respective lists
    private lateinit var textViewNoBadges: TextView
    private lateinit var textViewNoCerts: TextView


    // Adapters for the RecyclerViews
    private lateinit var badgeAdapter: BadgeAdapter // Adapter for displaying earned badges
    private lateinit var certAdapter: CertificationProgressAdapter // Adapter for displaying certification progress


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the activity's layout from activity_profile.xml
        setContentView(R.layout.activity_profile)

        // --- Get User ID from SessionManager ---
        // Retrieve the logged-in user's ID using our SessionManager utility
        currentUserId = SessionManager.getUserId()

        // Check if the user ID is valid (i.e., user is logged in)
        if (currentUserId == -1) {
            // If no user ID is found, the user is not logged in.
            Toast.makeText(this, "Please log in.", Toast.LENGTH_SHORT).show()
            // Redirect the user back to the Login/Registration screen
            val intent = Intent(this, LoginRegistrationActivity::class.java)
            // Clear the activity stack so the user can't navigate back to this screen
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish() // Finish the current activity
            return // Stop further execution of onCreate
        }

        // --- Find Views by ID ---
        // Link the UI elements declared in the layout file to variables in the code
        textViewProfileName = findViewById(R.id.textViewProfileName)
        textViewProfileUsername = findViewById(R.id.textViewProfileUsername)
        textViewProfileBio = findViewById(R.id.textViewProfileBio)
        recyclerViewEarnedBadges = findViewById(R.id.recyclerViewEarnedBadges)
        recyclerViewCertProgress = findViewById(R.id.recyclerViewCertProgress)
        // Find the "no data" TextViews (assuming you added them to activity_profile.xml)
        textViewNoBadges = findViewById(R.id.textViewNoBadges)
        textViewNoCerts = findViewById(R.id.textViewNoCerts)


        // --- Initialize Adapters ---
        // You MUST create CertificationProgressAdapter class separately
        // Initialize the Badge Adapter. It takes an initial empty list and a lambda function
        // that defines what should happen when a share icon on a badge item is clicked.
        badgeAdapter = BadgeAdapter(emptyList()) { clickedBadge ->
            // This lambda is executed when a share icon within an earned badge item is clicked.
            // 'clickedBadge' is the UserBadge object associated with the clicked item.

            // Prepare the data to share from the clickedBadge object
            val achievementName = clickedBadge.badge_name
            // Use the description from the badge data for sharing details
            val achievementDetails = clickedBadge.description ?: "Digital Badge earned." // Provide a default if description is null

            // TODO: Generate or use a real URL if you have a public web view for achievements.
            // This URL would link to a public page showing the badge details, useful for portfolios/LinkedIn.
            val achievementUrl: String? = null // Replace with actual URL if available (e.g., "http://yourwebsite.com/achievements/${clickedBadge.user_badge_id}")

            // --- Start the new ShareAchievementActivity ---
            // Create an Intent to launch the ShareAchievementActivity
            val shareIntent = Intent(this, ShareAchievementActivity::class.java).apply {
                // Put the achievement details as extras in the Intent
                putExtra(ShareAchievementActivity.EXTRA_ACHIEVEMENT_NAME, achievementName)
                putExtra(ShareAchievementActivity.EXTRA_ACHIEVEMENT_DETAILS, achievementDetails)
                putExtra(ShareAchievementActivity.EXTRA_ACHIEVEMENT_URL, achievementUrl) // Pass the optional URL
            }
            startActivity(shareIntent) // Launch the ShareAchievementActivity
        }
        // Set up the RecyclerView with a LinearLayoutManager and the badge adapter
        recyclerViewEarnedBadges.layoutManager = LinearLayoutManager(this)
        recyclerViewEarnedBadges.adapter = badgeAdapter

        // Initialize the Certification Progress Adapter with an initial empty list
        // (No share listener added here, but you could add one if needed for certs)
        certAdapter = CertificationProgressAdapter(emptyList()) // Pass empty list initially
        // Set up the RecyclerView with a LinearLayoutManager and the cert adapter
        recyclerViewCertProgress.layoutManager = LinearLayoutManager(this)
        recyclerViewCertProgress.adapter = certAdapter

        // --- Fetch Profile Data ---
        // Call the function to fetch the user's profile data from the backend
        fetchUserProfile(currentUserId)
    }

    // Function to fetch the user's profile data asynchronously
    private fun fetchUserProfile(userId: Int) {
        // Log the start of the data fetching process
        Log.d("ProfileActivity", "Fetching profile for user ID: $userId")
        // Launch a coroutine in the lifecycle scope to perform the network request
        lifecycleScope.launch {
            try {
                // --- Make API call using Retrofit ---
                // Call the getUserProfile endpoint from the ApiService
                val response = RetrofitInstance.api.getUserProfile(userId)

                // Check if the API response was successful (HTTP status 2xx)
                if (response.isSuccessful) {
                    // Get the response body (ProfileResponse data)
                    val profileData = response.body()
                    // Check if the response body is not null
                    if (profileData != null) {
                        Log.d("ProfileActivity", "Profile data fetched successfully.") // Log success

                        // --- Update UI with User Data ---
                        // Set the user's name (use full name if available, otherwise username)
                        textViewProfileName.text = profileData.user.full_name ?: profileData.user.username
                        // Set the username
                        textViewProfileUsername.text = "@${profileData.user.username}"
                        // Set the profile bio (provide a default if null)
                        textViewProfileBio.text = "Bio: ${profileData.user.profile_bio ?: "Not set"}"

                        // --- Update RecyclerView Adapters ---
                        // Update the badge adapter with the list of earned badges
                        badgeAdapter.updateData(profileData.badges)
                        // Update the certification adapter with the list of certification progress
                        certAdapter.updateData(profileData.certifications)

                        // --- Show/Hide "No data" TextViews ---
                        // If the list of earned badges is empty, show the "no badges" TextView and hide the RecyclerView
                        if (profileData.badges.isEmpty()) {
                            textViewNoBadges.visibility = View.VISIBLE
                            recyclerViewEarnedBadges.visibility = View.GONE
                        } else {
                            // If there are badges, hide the "no badges" TextView and show the RecyclerView
                            textViewNoBadges.visibility = View.GONE
                            recyclerViewEarnedBadges.visibility = View.VISIBLE
                        }

                        // If the list of certifications is empty, show the "no certs" TextView and hide the RecyclerView
                        if (profileData.certifications.isEmpty()) {
                            textViewNoCerts.visibility = View.VISIBLE
                            recyclerViewCertProgress.visibility = View.GONE
                        } else {
                            // If there are certifications, hide the "no certs" TextView and show the RecyclerView
                            textViewNoCerts.visibility = View.GONE
                            recyclerViewCertProgress.visibility = View.VISIBLE
                        }


                    } else {
                        // Handle case where response is successful but body is null
                        Log.e("ProfileActivity", "Failed to load profile data: profileData is null")
                        Toast.makeText(this@ProfileActivity, "Failed to load profile data", Toast.LENGTH_SHORT).show()
                        // Update adapters with empty lists and show "No data" TextViews
                        badgeAdapter.updateData(emptyList())
                        certAdapter.updateData(emptyList())
                        textViewNoBadges.visibility = View.VISIBLE
                        recyclerViewEarnedBadges.visibility = View.GONE
                        textViewNoCerts.visibility = View.VISIBLE
                        recyclerViewCertProgress.visibility = View.GONE
                    }
                } else {
                    // Handle API errors (non-2xx status codes)
                    val errorBodyString = response.errorBody()?.string()
                    // Attempt to parse the error message from the response body (assuming Flask returns JSON like {'message': '...'})
                    val errorMessage = try {
                        errorBodyString?.let { Gson().fromJson(it, BasicResponse::class.java).message }
                    } catch (e: Exception) { null } // If parsing fails, errorMessage remains null
                    Log.e("ProfileActivity", "Error fetching profile: ${response.code()}, Message: $errorMessage, Body: $errorBodyString") // Log API error details
                    Toast.makeText(this@ProfileActivity, errorMessage ?: "Error fetching profile: ${response.code()}", Toast.LENGTH_SHORT).show()
                    // Update adapters with empty lists and show "No data" TextViews in case of API errors
                    badgeAdapter.updateData(emptyList())
                    certAdapter.updateData(emptyList())
                    textViewNoBadges.visibility = View.VISIBLE
                    recyclerViewEarnedBadges.visibility = View.GONE
                    textViewNoCerts.visibility = View.VISIBLE
                    recyclerViewCertProgress.visibility = View.GONE
                }
            } catch (e: Exception) {
                // Handle network errors (e.g., no internet connection, server unreachable)
                Log.e("ProfileActivity", "Network error fetching profile", e) // Log network error with stack trace
                Toast.makeText(this@ProfileActivity, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
                // Update adapters with empty lists and show "No data" TextViews in case of network errors
                badgeAdapter.updateData(emptyList())
                certAdapter.updateData(emptyList())
                textViewNoBadges.visibility = View.VISIBLE
                recyclerViewEarnedBadges.visibility = View.GONE
                textViewNoCerts.visibility = View.VISIBLE
                recyclerViewCertProgress.visibility = View.GONE
            }
        }
    }

    // The private fun shareAchievement(...) function has been removed from here
    // as sharing is now handled by launching the ShareAchievementActivity.

}
