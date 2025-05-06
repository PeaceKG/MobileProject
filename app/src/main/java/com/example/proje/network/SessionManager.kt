package com.example.proje.network

import android.content.Context
import android.content.SharedPreferences

object SessionManager {

    private const val PREF_NAME = "UserSession"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_AUTH_TOKEN = "auth_token" // Placeholder for future token

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    // Call this from your Application class (MyApplication.kt)
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }

    fun saveUserId(userId: Int) {
        editor.putInt(KEY_USER_ID, userId)
        editor.apply() // Use apply for async save, commit() for sync
    }

    // Placeholder for saving token (implement when adding JWT)
    fun saveAuthToken(token: String) {
        editor.putString(KEY_AUTH_TOKEN, token)
        editor.apply()
    }

    fun getUserId(): Int {
        // Return -1 or another indicator if no user ID is found
        return sharedPreferences.getInt(KEY_USER_ID, -1)
    }

    // Placeholder for getting token
    fun getAuthToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }

    fun clearSession() {
        editor.remove(KEY_USER_ID) // Remove the user ID key
        editor.remove(KEY_AUTH_TOKEN) // Remove the token key if you add it
        editor.apply() // Apply the changes
        // Alternatively, editor.clear() removes ALL keys, which is also fine for logout
        // editor.clear()
        // editor.apply()
    }


    // Optional: Check if a user is currently logged in
    fun isLoggedIn(): Boolean { // <-- This is the function you are calling
        return getUserId() != -1 // Or check if token is present and not expired
    }
}