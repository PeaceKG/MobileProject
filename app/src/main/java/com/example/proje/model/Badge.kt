package com.example.proje.model

// networking/data/User.kt
data class User(
    val user_id: Int,
    val username: String,
    val email: String?,
    val full_name: String?,
    val profile_bio: String?
)

// networking/data/Badge.kt
data class Badge(
    val badge_id: Int,
    val badge_name: String,
    val description: String,
    val icon_url: String?,
    val criteria: String? // Only present in badge details
)

// networking/data/UserBadge.kt
data class UserBadge(
    val badge_id: Int,
    val badge_name: String,
    val description: String,
    val icon_url: String?,
    val earned_date: String // Assuming date is returned as string
)

// networking/data/Certification.kt
data class Certification(
    val cert_id: Int,
    val cert_name: String,
    val description: String,
    val required_badges: String?,
    val status: String, // "In Progress", "Completed"
    val completion_date: String?
)

// networking/data/ProfileResponse.kt (Structure for GET /profile/<user_id>)
data class ProfileResponse(
    val user: User,
    val badges: List<UserBadge>,
    val certifications: List<Certification>
)

// networking/data/LoginRequest.kt
data class LoginRequest(
    val username: String,
    val password: String
)

// networking/data/LoginResponse.kt
data class LoginResponse(
    val message: String,
    val user_id: Int? // Included on successful login
)

// networking/data/RegisterRequest.kt
data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String?,
    val full_name: String?
)

// networking/data/BasicResponse.kt (For simple success/failure messages)
data class BasicResponse(
    val message: String
)

// networking/data/UpdateProfileRequest.kt
data class UpdateProfileRequest(
    val full_name: String?,
    val profile_bio: String?
    // Add other fields for update
)