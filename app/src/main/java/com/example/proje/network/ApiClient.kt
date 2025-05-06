package com.example.proje.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    // IMPORTANT: Replace with your Flask server's IP address and port
    // If using emulator, 10.0.2.2 usually points to your host machine's localhost
    // If using a physical device, use your local network IP address (e.g., 192.168.1.10)
    private const val BASE_URL = "http://192.168.1.20:5000/" // Or "http://YOUR_LOCAL_IP:5000/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}