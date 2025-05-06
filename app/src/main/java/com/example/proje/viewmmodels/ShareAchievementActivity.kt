package com.example.proje.viewmmodels

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proje.R // --- IMPORT CHANGED HERE ---

class ShareAchievementActivity : AppCompatActivity() {

    // Define keys for the Intent extras
    companion object {
        const val EXTRA_ACHIEVEMENT_NAME = "achievement_name"
        const val EXTRA_ACHIEVEMENT_DETAILS = "achievement_details"
        const val EXTRA_ACHIEVEMENT_URL = "achievement_url" // Optional URL
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_achievement) // Set the minimal layout

        // Retrieve achievement details from the Intent extras
        val achievementName = intent.getStringExtra(EXTRA_ACHIEVEMENT_NAME)
        val achievementDetails = intent.getStringExtra(EXTRA_ACHIEVEMENT_DETAILS)
        val achievementUrl = intent.getStringExtra(EXTRA_ACHIEVEMENT_URL) // Get optional URL

        // Check if required data is available
        if (achievementName == null || achievementDetails == null) {
            Toast.makeText(this, "Error: Achievement details missing.", Toast.LENGTH_SHORT).show()
            finish() // Finish if data is missing
            return
        }

        // Build the share text
        var shareText = "I just earned the '$achievementName' badge in the Digital Badge System! 🎉\n\n" +
                "$achievementDetails"

        // Add the URL if it was provided
        if (!achievementUrl.isNullOrEmpty()) {
            shareText += "\n\nCheck it out here: $achievementUrl"
        }

        // Create the sharing Intent
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain" // Set the MIME type
            putExtra(Intent.EXTRA_SUBJECT, "My Digital Badge Achievement: $achievementName") // Optional subject
            putExtra(Intent.EXTRA_TEXT, shareText) // The content to share
        }

        // Check if there's any app to handle the intent and start the chooser
        if (shareIntent.resolveActivity(packageManager) != null) {
            startActivity(Intent.createChooser(shareIntent, "Share Achievement via"))
        } else {
            Toast.makeText(this, "No apps installed to handle sharing.", Toast.LENGTH_SHORT).show()
        }

        // Finish this activity immediately after launching the chooser
        // We don't want this activity to stay on the back stack
        finish()
    }
}
