package com.example.proje.viewmmodels

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.proje.R // --- IMPORT CHANGED HERE ---
import com.example.proje.network.RetrofitInstance // --- IMPORT CHANGED HERE ---
import com.example.proje.model.BasicResponse // --- IMPORT CHANGED HERE ---
import com.example.proje.model.LoginRequest // --- IMPORT CHANGED HERE ---
import com.example.proje.model.RegisterRequest // --- IMPORT CHANGED HERE ---
import com.example.proje.viewmmodels.DashboardActivity // --- IMPORT CHANGED HERE ---
import com.example.proje.network.SessionManager // --- IMPORT CHANGED HERE ---
import kotlinx.coroutines.launch
import com.google.gson.Gson // Needed to parse error body JSON

class LoginRegistrationActivity : AppCompatActivity() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextFullName: EditText
    private lateinit var buttonLogin: Button
    private lateinit var buttonRegister: Button
    private lateinit var textViewToggleMode: TextView

    private var isLoginMode = true // State to switch between login and registration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_registration)

        // Check if already logged in
        if (SessionManager.isLoggedIn()) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return // Stop onCreate if already logged in
        }


        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextFullName = findViewById(R.id.editTextFullName)
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonRegister = findViewById(R.id.buttonRegister)
        textViewToggleMode = findViewById(R.id.textViewToggleMode)

        updateUiMode()

        buttonLogin.setOnClickListener {
            if (isLoginMode) {
                performLogin()
            } else {
                // Switch to Login mode
                isLoginMode = true
                updateUiMode()
            }
        }

        buttonRegister.setOnClickListener {
            if (!isLoginMode) {
                performRegistration()
            } else {
                // Switch to Register mode
                isLoginMode = false
                updateUiMode()
            }
        }

        textViewToggleMode.setOnClickListener {
            isLoginMode = !isLoginMode
            updateUiMode()
        }
    }

    private fun updateUiMode() {
        if (isLoginMode) {
            buttonLogin.text = "Login"
            buttonRegister.text = "Register"
            textViewToggleMode.text = "Need an account? Register"
            editTextEmail.visibility = View.GONE
            editTextFullName.visibility = View.GONE
        } else {
            buttonLogin.text = "Already have an account? Login" // Optional text change
            buttonRegister.text = "Register"
            textViewToggleMode.text = "Already have an account? Login"
            editTextEmail.visibility = View.VISIBLE
            editTextFullName.visibility = View.VISIBLE
        }
    }

    private fun performLogin() {
        val username = editTextUsername.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
            return
        }

        // Use coroutines to perform network request asynchronously
        lifecycleScope.launch {
            try {
                val requestBody = LoginRequest(username, password)
                val response = RetrofitInstance.api.loginUser(requestBody)

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    val userId = loginResponse?.user_id

                    if (userId != null) {
                        // Login successful
                        Toast.makeText(this@LoginRegistrationActivity, loginResponse.message, Toast.LENGTH_SHORT).show()

                        // --- Save User ID using SessionManager ---
                        SessionManager.saveUserId(userId)
                        // TODO: If using tokens, save the token here too: SessionManager.saveAuthToken(token)

                        // Navigate to Dashboard
                        val intent = Intent(this@LoginRegistrationActivity, DashboardActivity::class.java)
                        // No need to pass user ID via Intent anymore
                        startActivity(intent)
                        finish() // Close login activity

                    } else {
                        // Handle cases where login is successful but user_id is missing (shouldn't happen with current API)
                        Toast.makeText(this@LoginRegistrationActivity, loginResponse?.message ?: "Login failed unexpectedly", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    // Handle login failure (e.g., invalid credentials)
                    val errorBodyString = response.errorBody()?.string()
                    // Attempt to parse error message if Flask returns JSON error {'message': '...'}
                    val errorMessage = try {
                        errorBodyString?.let { Gson().fromJson(it, BasicResponse::class.java).message }
                    } catch (e: Exception) {
                        null // Parsing failed
                    }
                    Toast.makeText(this@LoginRegistrationActivity, errorMessage ?: "Login failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                    // Optional: print the error body for debugging
                    // println("Error Body: $errorBodyString")
                }
            } catch (e: Exception) {
                // Handle network errors or other exceptions
                Toast.makeText(this@LoginRegistrationActivity, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun performRegistration() {
        val username = editTextUsername.text.toString().trim()
        val password = editTextPassword.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val fullName = editTextFullName.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username and password are required", Toast.LENGTH_SHORT).show()
            return
        }
        // TODO: Add more robust validation (email format, password strength)

        lifecycleScope.launch {
            try {
                // Pass null for empty optional fields if the backend expects it
                val requestBody = RegisterRequest(username, password, email.ifEmpty { null }, fullName.ifEmpty { null })
                val response = RetrofitInstance.api.registerUser(requestBody) // Assuming Flask returns BasicResponse

                if (response.isSuccessful) {
                    val registerResponse = response.body()
                    Toast.makeText(this@LoginRegistrationActivity, registerResponse?.message ?: "Registration successful", Toast.LENGTH_SHORT).show()
                    // Optionally switch back to login mode after successful registration
                    isLoginMode = true
                    updateUiMode()
                    // Clear fields?
                    editTextUsername.text.clear()
                    editTextPassword.text.clear()
                    editTextEmail.text.clear()
                    editTextFullName.text.clear()

                } else {
                    val errorBodyString = response.errorBody()?.string()
                    val errorMessage = try {
                        errorBodyString?.let { Gson().fromJson(it, BasicResponse::class.java).message }
                    } catch (e: Exception) { null }
                    Toast.makeText(this@LoginRegistrationActivity, errorMessage ?: "Registration failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                    // Optional: print the error body for debugging
                    // println("Error Body: $errorBodyString")
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginRegistrationActivity, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}