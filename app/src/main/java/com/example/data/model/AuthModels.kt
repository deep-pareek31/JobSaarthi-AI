package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: T? = null,
    @Json(name = "error") val error: String? = null
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "phone") val phone: String? = null
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class GoogleAuthRequest(
    @Json(name = "id_token") val idToken: String
)

@JsonClass(generateAdapter = true)
data class RefreshTokenRequest(
    @Json(name = "refresh_token") val refreshToken: String
)

@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "id") val id: String,
    @Json(name = "email") val email: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "role") val role: String,
    @Json(name = "is_active") val isActive: Boolean,
    @Json(name = "is_verified") val isVerified: Boolean,
    @Json(name = "avatar_url") val avatarUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class TokenPairDto(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "refresh_token") val refreshToken: String,
    @Json(name = "token_type") val tokenType: String = "bearer",
    @Json(name = "expires_in") val expiresIn: Int = 3600
)

@JsonClass(generateAdapter = true)
data class AuthSuccessDto(
    @Json(name = "user") val user: UserDto,
    @Json(name = "tokens") val tokens: TokenPairDto
)

@JsonClass(generateAdapter = true)
data class ProfileDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "current_location") val currentLocation: String? = null,
    @Json(name = "education_level") val educationLevel: String? = null,
    @Json(name = "degree") val degree: String? = null,
    @Json(name = "branch") val branch: String? = null,
    @Json(name = "institution") val institution: String? = null,
    @Json(name = "graduation_year") val graduationYear: Int? = null,
    @Json(name = "is_student") val isStudent: Boolean = false,
    @Json(name = "years_experience") val yearsExperience: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class UserDetailDto(
    @Json(name = "id") val id: String,
    @Json(name = "email") val email: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "role") val role: String,
    @Json(name = "is_active") val isActive: Boolean,
    @Json(name = "is_verified") val isVerified: Boolean,
    @Json(name = "avatar_url") val avatarUrl: String? = null,
    @Json(name = "profile") val profile: ProfileDto? = null,
    @Json(name = "subscription_tier") val subscriptionTier: String = "FREE",
    @Json(name = "plan_name") val planName: String = "Free Tier"
)
