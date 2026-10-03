package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val fullName: String,
    val role: String,
    val isActive: Boolean,
    val isVerified: Boolean,
    val avatarUrl: String?,
    val subscriptionTier: String = "FREE",
    val planName: String = "Free Tier",
    val currentLocation: String? = null,
    val graduationYear: Int? = null,
    val degree: String? = null,
    val branch: String? = null,
    val yearsExperience: Double = 0.0,
    val lastSyncedAt: Long = System.currentTimeMillis()
)
