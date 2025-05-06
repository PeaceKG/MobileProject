package com.example.proje

import android.app.Application
import com.example.proje.network.SessionManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionManager.init(this) // Initialize SessionManager here
    }
}