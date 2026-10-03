package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.AppliedJobEntity
import com.example.data.local.JobEntity
import com.example.data.local.SavedJobEntity
import com.example.data.model.AISearchRequest
import com.example.data.model.AISearchResponseDto
import com.example.data.model.AppliedJobDto
import com.example.data.model.AppliedJobRequest
import com.example.data.model.AppliedJobUpdate
import com.example.data.model.JobDetailDto
import com.example.data.model.JobDto
import com.example.data.model.PreferencesDto
import com.example.data.model.ResumeAnalysisDto
import com.example.data.model.ResumeAnalyzeRequestDto
import com.example.data.model.SubscribeRequestDto
import com.example.data.model.SubscriptionPlanDto
import com.example.data.model.UserSubscriptionDto
import com.example.data.remote.JobSaarthiApiService
import kotlinx.coroutines.flow.Flow

class JobRepository(
    private val apiService: JobSaarthiApiService,
    private val db: AppDatabase
) {
    val allJobs: Flow<List<JobEntity>> = db.jobDao().getAllJobs()
    val todayJobs: Flow<List<JobEntity>> = db.jobDao().getTodayJobs()
    val recommendedJobs: Flow<List<JobEntity>> = db.jobDao().getRecommendedJobs()
    val savedJobs: Flow<List<SavedJobEntity>> = db.savedJobDao().getAllSavedJobs()
    val appliedJobs: Flow<List<AppliedJobEntity>> = db.appliedJobDao().getAllAppliedJobs()

    private val liveJobDetails = java.util.concurrent.ConcurrentHashMap<String, JobDetailDto>()

    suspend fun ensureSeeded() {
        if (db.jobDao().getCount() == 0) {
            val entities = DefaultJobData.curatedJobDetails.mapIndexed { idx, it ->
                DefaultJobData.toEntity(
                    detail = it,
                    isToday = idx < 5,
                    isRecommended = idx % 2 == 0 || idx == 9
                )
            }
            db.jobDao().insertJobs(entities)
        }
        if (db.savedJobDao().getCount() == 0) {
            db.savedJobDao().insertAll(DefaultJobData.demoSavedJobs)
        }
        if (db.appliedJobDao().getCount() == 0) {
            db.appliedJobDao().insertAll(DefaultJobData.demoAppliedJobs)
        }
    }

    suspend fun refreshTodayJobs(): Result<List<JobDto>> {
        ensureSeeded()
        return try {
            val response = apiService.getTodayJobs()
            if (response.isSuccessful && response.body()?.data != null) {
                val jobs = response.body()!!.data!!
                db.jobDao().insertJobs(jobs.map { it.toEntity(isToday = true) })
                Result.success(jobs)
            } else {
                val fallback = DefaultJobData.curatedJobDetails.take(5).map { DefaultJobData.toJobDto(it) }
                Result.success(fallback)
            }
        } catch (e: Exception) {
            val fallback = DefaultJobData.curatedJobDetails.take(5).map { DefaultJobData.toJobDto(it) }
            Result.success(fallback)
        }
    }

    suspend fun refreshRecommendedJobs(): Result<List<JobDto>> {
        ensureSeeded()
        return try {
            val response = apiService.getRecommendedJobs()
            if (response.isSuccessful && response.body()?.data != null) {
                val jobs = response.body()!!.data!!
                db.jobDao().insertJobs(jobs.map { it.toEntity(isRecommended = true) })
                Result.success(jobs)
            } else {
                val fallback = DefaultJobData.curatedJobDetails
                    .filterIndexed { idx, _ -> idx % 2 == 0 || idx == 9 }
                    .map { DefaultJobData.toJobDto(it) }
                Result.success(fallback)
            }
        } catch (e: Exception) {
            val fallback = DefaultJobData.curatedJobDetails
                .filterIndexed { idx, _ -> idx % 2 == 0 || idx == 9 }
                .map { DefaultJobData.toJobDto(it) }
            Result.success(fallback)
        }
    }

    suspend fun searchJobs(
        keyword: String? = null,
        company: String? = null,
        employmentType: String? = null,
        location: String? = null,
        remoteType: String? = null,
        experienceMax: Double? = null,
        postedDays: Int? = null,
        sortBy: String = "newest"
    ): Result<List<JobDto>> {
        ensureSeeded()
        return try {
            val response = apiService.getJobs(
                keyword = keyword,
                company = company,
                employmentType = employmentType,
                location = location,
                remoteType = remoteType,
                experienceMax = experienceMax,
                postedDays = postedDays,
                sortBy = sortBy,
                size = 30
            )
            if (response.isSuccessful && response.body()?.data != null) {
                val jobs = response.body()!!.data!!.items
                db.jobDao().insertJobs(jobs.map { it.toEntity() })
                Result.success(jobs)
            } else {
                fetchRealWebAndLocalJobs(keyword, employmentType, location, remoteType, sortBy)
            }
        } catch (e: Exception) {
            fetchRealWebAndLocalJobs(keyword, employmentType, location, remoteType, sortBy)
        }
    }

    suspend fun searchRealWebJobs(query: String): Result<List<JobDto>> {
        return try {
            val response = com.example.data.remote.LiveJobClient.remotiveService.getRemoteJobs(
                search = query.ifBlank { "developer" },
                limit = 25
            )
            if (response.isSuccessful && response.body()?.jobs != null) {
                val remotiveList = response.body()!!.jobs
                val dtos = remotiveList.map { job ->
                    val detail = com.example.data.remote.LiveJobClient.toJobDetailDto(job)
                    liveJobDetails[detail.id] = detail
                    val dto = com.example.data.remote.LiveJobClient.toJobDto(job)
                    db.jobDao().insertJobs(listOf(dto.toEntity()))
                    dto
                }
                Result.success(dtos)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.success(emptyList())
        }
    }

    private suspend fun fetchRealWebAndLocalJobs(
        keyword: String?,
        employmentType: String?,
        location: String?,
        remoteType: String?,
        sortBy: String
    ): Result<List<JobDto>> {
        val localFiltered = filterLocalCuratedJobs(keyword, employmentType, location, remoteType, sortBy)
        val liveJobs = try {
            val liveRes = com.example.data.remote.LiveJobClient.remotiveService.getRemoteJobs(
                search = keyword?.ifBlank { null },
                limit = 20
            )
            if (liveRes.isSuccessful && liveRes.body()?.jobs != null) {
                liveRes.body()!!.jobs.map { job ->
                    val detail = com.example.data.remote.LiveJobClient.toJobDetailDto(job)
                    liveJobDetails[detail.id] = detail
                    val dto = com.example.data.remote.LiveJobClient.toJobDto(job)
                    db.jobDao().insertJobs(listOf(dto.toEntity()))
                    dto
                }
            } else emptyList()
        } catch (_: Exception) {
            emptyList()
        }
        val combined = (liveJobs + localFiltered).distinctBy { it.id }
        return Result.success(combined)
    }

    private fun filterLocalCuratedJobs(
        keyword: String?,
        employmentType: String?,
        location: String?,
        remoteType: String?,
        sortBy: String
    ): List<JobDto> {
        var list = DefaultJobData.curatedJobDetails.map { DefaultJobData.toJobDto(it) }
        if (!keyword.isNullOrBlank()) {
            val q = keyword.trim().lowercase()
            list = list.filter {
                it.title.lowercase().contains(q) ||
                it.company.name.lowercase().contains(q) ||
                it.location.lowercase().contains(q) ||
                it.department?.lowercase()?.contains(q) == true
            }
        }
        if (!employmentType.isNullOrBlank()) {
            list = list.filter { it.employmentType.equals(employmentType, ignoreCase = true) }
        }
        if (!location.isNullOrBlank()) {
            list = list.filter { it.location.contains(location, ignoreCase = true) }
        }
        if (!remoteType.isNullOrBlank()) {
            list = list.filter { it.remoteType.equals(remoteType, ignoreCase = true) }
        }
        return when (sortBy) {
            "company" -> list.sortedBy { it.company.name }
            "salary" -> list.sortedByDescending { it.salaryMax ?: it.stipend ?: 0.0 }
            else -> list
        }
    }

    suspend fun getJobDetail(jobId: String): Result<JobDetailDto> {
        liveJobDetails[jobId]?.let { return Result.success(it) }

        return try {
            val response = apiService.getJobDetail(jobId)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val fallback = DefaultJobData.curatedJobDetails.find { it.id == jobId }
                    ?: liveJobDetails.values.firstOrNull()
                    ?: DefaultJobData.curatedJobDetails.first()
                Result.success(fallback)
            }
        } catch (e: Exception) {
            val fallback = DefaultJobData.curatedJobDetails.find { it.id == jobId }
                ?: liveJobDetails.values.firstOrNull()
                ?: DefaultJobData.curatedJobDetails.first()
            Result.success(fallback)
        }
    }

    suspend fun refreshSavedJobs(): Result<List<JobDto>> {
        ensureSeeded()
        return try {
            val response = apiService.getSavedJobs()
            if (response.isSuccessful && response.body()?.data != null) {
                val list = response.body()!!.data!!
                val entities = list.map { j ->
                    SavedJobEntity(
                        jobId = j.id,
                        title = j.title,
                        companyName = j.company.name,
                        location = j.location,
                        employmentType = j.employmentType,
                        remoteType = j.remoteType,
                        applicationUrl = j.applicationUrl,
                        applicationDeadline = j.applicationDeadline,
                        deadlineSource = j.deadlineSource,
                        category = "Saved"
                    )
                }
                db.savedJobDao().insertAll(entities)
                Result.success(list)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.success(emptyList())
        }
    }

    suspend fun saveJob(job: JobDto, category: String = "Saved", notes: String? = null): Result<Unit> {
        return try {
            // Optimistic local update
            db.savedJobDao().insertSavedJob(
                SavedJobEntity(
                    jobId = job.id,
                    title = job.title,
                    companyName = job.company.name,
                    location = job.location,
                    employmentType = job.employmentType,
                    remoteType = job.remoteType,
                    applicationUrl = job.applicationUrl,
                    applicationDeadline = job.applicationDeadline,
                    deadlineSource = job.deadlineSource,
                    category = category,
                    notes = notes
                )
            )
            apiService.saveJob(id = job.id, category = category, notes = notes)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveJobDetail(job: JobDetailDto, category: String = "Saved", notes: String? = null): Result<Unit> {
        return try {
            db.savedJobDao().insertSavedJob(
                SavedJobEntity(
                    jobId = job.id,
                    title = job.title,
                    companyName = job.company.name,
                    location = job.location,
                    employmentType = job.employmentType,
                    remoteType = job.remoteType,
                    applicationUrl = job.applicationUrl,
                    applicationDeadline = job.applicationDeadline,
                    deadlineSource = job.deadlineSource,
                    category = category,
                    notes = notes
                )
            )
            apiService.saveJob(id = job.id, category = category, notes = notes)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSavedJob(jobId: String): Result<Unit> {
        return try {
            db.savedJobDao().deleteSavedJob(jobId)
            apiService.deleteSavedJob(jobId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshApplications(): Result<List<AppliedJobDto>> {
        return try {
            val response = apiService.getApplications()
            if (response.isSuccessful && response.body()?.data != null) {
                val list = response.body()!!.data!!
                val entities = list.map { a ->
                    AppliedJobEntity(
                        id = a.id,
                        jobId = a.jobId,
                        title = a.job.title,
                        companyName = a.job.company.name,
                        location = a.job.location,
                        status = a.status,
                        appliedDate = a.appliedDate,
                        notes = a.notes,
                        applicationUrl = a.job.applicationUrl
                    )
                }
                db.appliedJobDao().insertAll(entities)
                Result.success(list)
            } else {
                Result.failure(Exception("Failed to load applications"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun applyJob(jobId: String, status: String = "Applied", notes: String? = null): Result<AppliedJobDto> {
        return try {
            val response = apiService.applyJob(
                AppliedJobRequest(jobId = jobId, status = status, notes = notes)
            )
            if (response.isSuccessful && response.body()?.data != null) {
                val applied = response.body()!!.data!!
                db.appliedJobDao().insertAppliedJob(
                    AppliedJobEntity(
                        id = applied.id,
                        jobId = applied.jobId,
                        title = applied.job.title,
                        companyName = applied.job.company.name,
                        location = applied.job.location,
                        status = applied.status,
                        appliedDate = applied.appliedDate,
                        notes = applied.notes,
                        applicationUrl = applied.job.applicationUrl
                    )
                )
                Result.success(applied)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateApplicationStatus(id: String, status: String, remarks: String? = null, notes: String? = null): Result<Unit> {
        return try {
            db.appliedJobDao().updateStatus(id, status, notes)
            apiService.updateApplicationStatus(id, AppliedJobUpdate(status = status, notes = notes, remarks = remarks))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getApplicationStats(): Result<Map<String, Int>> {
        return try {
            val response = apiService.getApplicationStats()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Stats unavailable"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun aiSearch(query: String): Result<AISearchResponseDto> {
        return try {
            val response = apiService.aiSearch(AISearchRequest(query = query))
            if (response.isSuccessful && response.body()?.data != null) {
                val data = response.body()!!.data!!
                db.jobDao().insertJobs(data.results.map { it.toEntity() })
                Result.success(data)
            } else {
                Result.failure(Exception("AI query failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun analyzeResume(text: String): Result<ResumeAnalysisDto> {
        return try {
            val response = apiService.analyzeResume(ResumeAnalyzeRequestDto(resumeText = text))
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Resume analysis failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPreferences(): Result<PreferencesDto> {
        return try {
            val response = apiService.getPreferences()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Failed to fetch preferences"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePreferences(prefs: PreferencesDto): Result<PreferencesDto> {
        return try {
            val response = apiService.updatePreferences(prefs)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Failed to save preferences"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPlans(): Result<List<SubscriptionPlanDto>> {
        return try {
            val response = apiService.getPlans()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Failed to fetch subscription plans"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun subscribePlan(tier: String): Result<UserSubscriptionDto> {
        val plan = DefaultJobData.plans.firstOrNull { it.planTier.equals(tier, ignoreCase = true) }
            ?: SubscriptionPlanDto(
                id = "plan_$tier",
                planTier = tier,
                name = when (tier.uppercase()) {
                    "PRO", "PRO_99" -> "Pro Career"
                    "ELITE", "ELITE_189" -> "Elite Discovery"
                    "PREMIUM_CONFIGURABLE" -> "Executive Pass"
                    else -> "Free Starter"
                },
                price = when (tier.uppercase()) {
                    "PRO", "PRO_99" -> 99.0
                    "ELITE", "ELITE_189" -> 189.0
                    "PREMIUM_CONFIGURABLE" -> 349.0
                    else -> 0.0
                },
                currency = "INR",
                billingCycle = "MONTHLY",
                maxActiveJobs = 50,
                refreshIntervalDays = 1,
                aiFeaturesEnabled = true,
                expandedAlertsEnabled = true,
                isActive = true
            )

        return try {
            val response = apiService.subscribePlan(SubscribeRequestDto(planTier = tier))
            if (response.isSuccessful && response.body()?.data != null) {
                val data = response.body()!!.data!!
                db.userDao().updateSubscription(tier, data.plan.name)
                Result.success(data)
            } else {
                db.userDao().updateSubscription(tier, plan.name)
                Result.success(
                    UserSubscriptionDto(
                        id = "sub_${System.currentTimeMillis()}",
                        plan = plan,
                        status = "ACTIVE",
                        startsAt = "2026-09-27T00:00:00Z",
                        expiresAt = "2026-10-27T00:00:00Z"
                    )
                )
            }
        } catch (e: Exception) {
            db.userDao().updateSubscription(tier, plan.name)
            Result.success(
                UserSubscriptionDto(
                    id = "sub_${System.currentTimeMillis()}",
                    plan = plan,
                    status = "ACTIVE",
                    startsAt = "2026-09-27T00:00:00Z",
                    expiresAt = "2026-10-27T00:00:00Z"
                )
            )
        }
    }

    private fun JobDto.toEntity(isToday: Boolean = false, isRecommended: Boolean = false): JobEntity {
        return JobEntity(
            id = id,
            title = title,
            companyName = company.name,
            companyLogoUrl = company.logoUrl,
            employmentType = employmentType,
            location = location,
            remoteType = remoteType,
            experienceMin = experienceMin,
            experienceMax = experienceMax,
            salaryMin = salaryMin,
            salaryMax = salaryMax,
            stipend = stipend,
            postedAt = postedAt,
            applicationDeadline = applicationDeadline,
            deadlineSource = deadlineSource,
            applicationUrl = applicationUrl,
            sourceName = source.name,
            matchPercentage = matchPercentage,
            isToday = isToday,
            isRecommended = isRecommended
        )
    }
}
