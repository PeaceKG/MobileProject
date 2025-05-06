package com.example.proje

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proje.viewmmodels.DashboardActivity // --- IMPORT CHANGED HERE ---
import com.example.proje.viewmmodels.LoginRegistrationActivity // --- IMPORT CHANGED HERE ---
import com.example.proje.network.SessionManager // --- IMPORT CHANGED HERE ---

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set a minimal content view while the check happens
        setContentView(R.layout.activity_main) // Using the new main layout

        // Check if the user is already logged in
        if (SessionManager.isLoggedIn()) {
            // User is logged in, go to Dashboard
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        } else {
            // User is NOT logged in, go to Login/Registration
            val intent = Intent(this, LoginRegistrationActivity::class.java)
            startActivity(intent)
        }

        // Finish MainActivity so the user can't press Back to get to it
        finish()
    }
}