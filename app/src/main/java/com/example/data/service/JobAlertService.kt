package com.example.data.service

import com.example.data.model.AlertFrequency
import com.example.data.model.JobAlert
import com.example.data.model.JobAlertNotification
import com.example.data.model.JobDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

interface IJobAlertService {
    val alerts: StateFlow<List<JobAlert>>
    val notifications: StateFlow<List<JobAlertNotification>>
    fun createAlert(
        titleQuery: String,
        location: String = "Any Location",
        employmentType: String = "ALL",
        frequency: AlertFrequency = AlertFrequency.DAILY,
        email: String
    ): JobAlert
    fun toggleAlert(alertId: String, isActive: Boolean): Boolean
    fun deleteAlert(alertId: String): Boolean
    fun runCronJobMatch(availableJobs: List<JobDto>): List<JobAlertNotification>
}

/**
 * In-memory Mock Job Alert Service.
 * Designed for full compatibility with an asynchronous backend Celery/Cron-job runner.
 * Handles search query subscription, periodic notification dispatch simulation,
 * and delivery history tracking.
 */
class JobAlertService private constructor() : IJobAlertService {

    companion object {
        @Volatile
        private var instance: JobAlertService? = null

        fun getInstance(): JobAlertService {
            return instance ?: synchronized(this) {
                instance ?: JobAlertService().also { instance = it }
            }
        }
    }

    private val _alerts = MutableStateFlow<List<JobAlert>>(
        listOf(
            JobAlert(
                id = "alert-sde-blr",
                titleQuery = "Software Engineer",
                location = "Bengaluru",
                employmentType = "FULL_TIME",
                frequency = AlertFrequency.DAILY,
                email = "deep.pareek31@gmail.com",
                isActive = true,
                createdAt = System.currentTimeMillis() - 86400000L * 3,
                lastNotifiedAt = System.currentTimeMillis() - 86400000L,
                lastMatchCount = 4
            ),
            JobAlert(
                id = "alert-ml-intern",
                titleQuery = "Machine Learning",
                location = "Any Location",
                employmentType = "INTERNSHIP",
                frequency = AlertFrequency.INSTANT,
                email = "deep.pareek31@gmail.com",
                isActive = true,
                createdAt = System.currentTimeMillis() - 86400000L * 1,
                lastNotifiedAt = System.currentTimeMillis() - 3600000L * 5,
                lastMatchCount = 2
            )
        )
    )
    override val alerts: StateFlow<List<JobAlert>> = _alerts.asStateFlow()

    private val _notifications = MutableStateFlow<List<JobAlertNotification>>(
        listOf(
            JobAlertNotification(
                id = "notif-init-1",
                alertId = "alert-sde-blr",
                query = "Software Engineer",
                recipientEmail = "deep.pareek31@gmail.com",
                matchingJobTitles = listOf(
                    "Software Development Engineer - SDE 1 at Google",
                    "Frontend Engineer - React / Next.js at Razorpay"
                ),
                timestamp = System.currentTimeMillis() - 86400000L,
                isDelivered = true
            )
        )
    )
    override val notifications: StateFlow<List<JobAlertNotification>> = _notifications.asStateFlow()

    override fun createAlert(
        titleQuery: String,
        location: String,
        employmentType: String,
        frequency: AlertFrequency,
        email: String
    ): JobAlert {
        val cleanQuery = titleQuery.trim().ifBlank { "All Tech Roles" }
        val newAlert = JobAlert(
            id = "alert-${UUID.randomUUID().toString().take(8)}",
            titleQuery = cleanQuery,
            location = location.trim().ifBlank { "Any Location" },
            employmentType = employmentType,
            frequency = frequency,
            email = email.trim().ifBlank { "user@jobsaarthi.com" },
            isActive = true,
            createdAt = System.currentTimeMillis(),
            lastNotifiedAt = null,
            lastMatchCount = 0
        )
        _alerts.update { listOf(newAlert) + it }
        return newAlert
    }

    override fun toggleAlert(alertId: String, isActive: Boolean): Boolean {
        var found = false
        _alerts.update { list ->
            list.map {
                if (it.id == alertId) {
                    found = true
                    it.copy(isActive = isActive)
                } else it
            }
        }
        return found
    }

    override fun deleteAlert(alertId: String): Boolean {
        var removed = false
        _alerts.update { list ->
            val updated = list.filter { it.id != alertId }
            removed = updated.size != list.size
            updated
        }
        return removed
    }

    /**
     * Simulates the future backend cron-job execution (e.g. Celery beat / Cloud Scheduler).
     * Compares active search queries against current available jobs, calculates new matches,
     * updates the alert metadata, and generates email delivery receipts.
     */
    override fun runCronJobMatch(availableJobs: List<JobDto>): List<JobAlertNotification> {
        val dispatched = mutableListOf<JobAlertNotification>()
        val currentTime = System.currentTimeMillis()

        _alerts.update { list ->
            list.map { alert ->
                if (!alert.isActive) return@map alert

                val queryWords = alert.titleQuery.lowercase().split(" ", "-", "/").filter { it.length > 2 }
                val matches = availableJobs.filter { job ->
                    val titleMatch = if (queryWords.isEmpty() || alert.titleQuery.equals("All Tech Roles", ignoreCase = true)) {
                        true
                    } else {
                        queryWords.any { word ->
                            job.title.lowercase().contains(word) ||
                            job.department?.lowercase()?.contains(word) == true ||
                            job.company.name.lowercase().contains(word)
                        }
                    }

                    val locMatch = alert.location.equals("Any Location", ignoreCase = true) ||
                            job.location.contains(alert.location, ignoreCase = true) ||
                            (alert.location.equals("Remote", ignoreCase = true) && job.remoteType.equals("REMOTE", ignoreCase = true))

                    val typeMatch = alert.employmentType == "ALL" ||
                            job.employmentType.equals(alert.employmentType, ignoreCase = true)

                    titleMatch && locMatch && typeMatch
                }

                if (matches.isNotEmpty()) {
                    val notif = JobAlertNotification(
                        alertId = alert.id,
                        query = alert.titleQuery,
                        recipientEmail = alert.email,
                        matchingJobTitles = matches.take(5).map { "${it.title} at ${it.company.name}" },
                        timestamp = currentTime,
                        isDelivered = true
                    )
                    dispatched.add(notif)
                }

                alert.copy(
                    lastNotifiedAt = currentTime,
                    lastMatchCount = matches.size
                )
            }
        }

        if (dispatched.isNotEmpty()) {
            _notifications.update { dispatched + it }
        }

        return dispatched
    }
}
