package com.example.proje.network

import com.example.proje.model.Badge
import com.example.proje.model.BasicResponse
import com.example.proje.model.LoginRequest
import com.example.proje.model.LoginResponse
import com.example.proje.model.ProfileResponse
import com.example.proje.model.RegisterRequest
import com.example.proje.model.UpdateProfileRequest
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<BasicResponse> // Adjust response based on Flask output

    @GET("/profile/{user_id}")
    suspend fun getUserProfile(@Path("user_id") userId: Int): Response<ProfileResponse>

    @GET("/badges")
    suspend fun getAllBadges(): Response<List<Badge>>

    @GET("/badges/{badge_id}")
    suspend fun getBadgeDetails(@Path("badge_id") badgeId: Int): Response<Badge> // Using Badge data class

    @PUT("/profile/{user_id}")
    suspend fun updateUserProfile(@Path("user_id") userId: Int, @Body request: UpdateProfileRequest): Response<BasicResponse>

    // Add other endpoints as needed (e.g., for sharing if backend assists)
}