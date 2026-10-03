package com.example.data.model

import java.util.UUID

enum class AlertFrequency(val displayName: String, val intervalDescription: String) {
    INSTANT("Instant Alert", "Real-time notification as soon as matching role is indexed"),
    DAILY("Daily Digest", "Consolidated email digest dispatched daily at 9:00 AM IST"),
    WEEKLY("Weekly Summary", "Comprehensive Monday morning career summary")
}

data class JobAlert(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "current_user",
    val titleQuery: String,
    val location: String = "Any Location",
    val employmentType: String = "ALL",
    val frequency: AlertFrequency = AlertFrequency.DAILY,
    val email: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastNotifiedAt: Long? = null,
    val lastMatchCount: Int = 0
)

data class JobAlertNotification(
    val id: String = UUID.randomUUID().toString(),
    val alertId: String,
    val query: String,
    val recipientEmail: String,
    val matchingJobTitles: List<String>,
    val timestamp: Long = System.currentTimeMillis(),
    val isDelivered: Boolean = true
)
