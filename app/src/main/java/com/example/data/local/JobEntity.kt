package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_jobs")
data class JobEntity(
    @PrimaryKey val id: String,
    val title: String,
    val companyName: String,
    val companyLogoUrl: String?,
    val employmentType: String,
    val location: String,
    val remoteType: String,
    val experienceMin: Double,
    val experienceMax: Double?,
    val salaryMin: Double?,
    val salaryMax: Double?,
    val stipend: Double?,
    val postedAt: String,
    val applicationDeadline: String?,
    val deadlineSource: String?,
    val applicationUrl: String,
    val sourceName: String,
    val matchPercentage: Int?,
    val isToday: Boolean = false,
    val isRecommended: Boolean = false,
    val lastCachedAt: Long = System.currentTimeMillis()
)
