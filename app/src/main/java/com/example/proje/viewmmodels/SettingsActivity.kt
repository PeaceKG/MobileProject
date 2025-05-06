package com.example.proje.viewmmodels

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.proje.R
import com.example.proje.model.BasicResponse
import com.example.proje.model.UpdateProfileRequest
import com.example.proje.network.RetrofitInstance
import com.google.gson.Gson
import kotlinx.coroutines.launch
import android.content.Intent
import com.example.proje.network.SessionManager
import android.util.Log

class SettingsActivity : AppCompatActivity() {

    // Variable to store the ID of the currently logged-in user
    private var currentUserId: Int = -1

    // UI elements from the layout file
    private lateinit var editTextSettingsFullName: EditText
    private lateinit var editTextSettingsBio: EditText
    private lateinit var buttonSaveSettings: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the activity's layout from activity_settings.xml
        setContentView(R.layout.activity_settings)

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
        // Link the UI elements declared in the layout file to variables in the code
        editTextSettingsFullName = findViewById(R.id.editTextSettingsFullName)
        editTextSettingsBio = findViewById(R.id.editTextSettingsBio)
        buttonSaveSettings = findViewById(R.id.buttonSaveSettings)

        // Fetch current profile data to pre-fill the fields using the user ID from SessionManager
        fetchCurrentProfileSettings(currentUserId)

        // --- Set Button Click Listener ---
        // Set the click listener for the Save Changes button
        buttonSaveSettings.setOnClickListener {
            // Call the function to save the profile settings
            saveProfileSettings(currentUserId)
        }
    }

    // Function to fetch the current user profile settings
    private fun fetchCurrentProfileSettings(userId: Int) {
        Log.d("SettingsActivity", "Fetching current profile settings for user ID: $userId") // Log start
        // Launch a coroutine in the lifecycle scope to perform the network request
        lifecycleScope.launch {
            try {
                // --- Make API call using Retrofit ---
                // Reuse the get_user_profile endpoint to get current data
                val response = RetrofitInstance.api.getUserProfile(userId)

                // Check if the API response was successful
                if (response.isSuccessful) {
                    // Get the response body (ProfileResponse data)
                    val profileData = response.body()
                    // Check if the response body is not null
                    if (profileData != null) {
                        Log.d("SettingsActivity", "Current profile settings fetched successfully.") // Log success
                        // Populate the EditText fields with the fetched data
                        editTextSettingsFullName.setText(profileData.user.full_name)
                        editTextSettingsBio.setText(profileData.user.profile_bio)
                    } else {
                        // Handle case where response is successful but body is null
                        Log.e("SettingsActivity", "Failed to load current settings: profileData is null")
                        Toast.makeText(this@SettingsActivity, "Failed to load current settings", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Handle API errors (non-2xx status codes)
                    val errorBodyString = response.errorBody()?.string()
                    // Attempt to parse the error message from the response body
                    val errorMessage = try {
                        errorBodyString?.let { Gson().fromJson(it, BasicResponse::class.java).message }
                    } catch (e: Exception) { null }
                    Log.e("SettingsActivity", "Error fetching settings: ${response.code()}, Message: $errorMessage, Body: $errorBodyString") // Log API error details
                    Toast.makeText(this@SettingsActivity, errorMessage ?: "Error fetching settings: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Handle network errors
                Log.e("SettingsActivity", "Network error fetching settings", e) // Log network error with stack trace
                Toast.makeText(this@SettingsActivity, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to save the updated profile settings
    private fun saveProfileSettings(userId: Int) {
        // Get the updated values from the EditText fields
        val fullName = editTextSettingsFullName.text.toString().trim()
        val bio = editTextSettingsBio.text.toString().trim()

        Log.d("SettingsActivity", "Attempting to save profile settings for user ID: $userId") // Log start
        // Launch a coroutine to perform the network request
        lifecycleScope.launch {
            try {
                // Create the request body with the new data
                // Passing null for empty strings assumes your backend handles that by not updating the field
                val requestBody = UpdateProfileRequest(
                    full_name = fullName.ifEmpty { null }, // Pass null if empty
                    profile_bio = bio.ifEmpty { null } // Pass null if empty
                    // Add other fields from settings UI here
                )

                // --- Make API call to update profile ---
                val response = RetrofitInstance.api.updateUserProfile(userId, requestBody)

                // Check if the API response was successful
                if (response.isSuccessful) {
                    val basicResponse = response.body()
                    Log.d("SettingsActivity", "Profile settings saved successfully: ${basicResponse?.message}") // Log success
                    Toast.makeText(this@SettingsActivity, basicResponse?.message ?: "Settings saved successfully", Toast.LENGTH_SHORT).show()
                    // Optionally refresh UI elements that might show updated profile info (e.g., in Dashboard or Profile)
                    // Or finish this activity to go back to the previous screen
                } else {
                    // Handle API errors
                    val errorBodyString = response.errorBody()?.string()
                    val errorMessage = try {
                        errorBodyString?.let { Gson().fromJson(it, BasicResponse::class.java).message }
                    } catch (e: Exception) { null }
                    Log.e("SettingsActivity", "Failed to save settings: ${response.code()}, Message: $errorMessage, Body: $errorBodyString") // Log API error details
                    Toast.makeText(this@SettingsActivity, errorMessage ?: "Failed to save settings: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Handle network errors
                Log.e("SettingsActivity", "Network error saving settings", e) // Log network error with stack trace
                Toast.makeText(this@SettingsActivity, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}