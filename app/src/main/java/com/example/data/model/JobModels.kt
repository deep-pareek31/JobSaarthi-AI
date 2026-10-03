package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CompanyDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "domain") val domain: String? = null,
    @Json(name = "logo_url") val logoUrl: String? = null,
    @Json(name = "careers_page_url") val careersPageUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class JobSourceDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "source_type") val sourceType: String,
    @Json(name = "base_url") val baseUrl: String,
    @Json(name = "is_enabled") val isEnabled: Boolean
)

@JsonClass(generateAdapter = true)
data class JobDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "employment_type") val employmentType: String,
    @Json(name = "department") val department: String? = null,
    @Json(name = "location") val location: String,
    @Json(name = "country") val country: String = "India",
    @Json(name = "city") val city: String? = null,
    @Json(name = "remote_type") val remoteType: String,
    @Json(name = "experience_min") val experienceMin: Double = 0.0,
    @Json(name = "experience_max") val experienceMax: Double? = null,
    @Json(name = "salary_min") val salaryMin: Double? = null,
    @Json(name = "salary_max") val salaryMax: Double? = null,
    @Json(name = "salary_currency") val salaryCurrency: String = "INR",
    @Json(name = "stipend") val stipend: Double? = null,
    @Json(name = "posted_at") val postedAt: String,
    @Json(name = "last_verified_at") val lastVerifiedAt: String,
    @Json(name = "application_deadline") val applicationDeadline: String? = null,
    @Json(name = "deadline_source") val deadlineSource: String? = null,
    @Json(name = "application_url") val applicationUrl: String,
    @Json(name = "source_url") val sourceUrl: String,
    @Json(name = "status") val status: String,
    @Json(name = "company") val company: CompanyDto,
    @Json(name = "source") val source: JobSourceDto,
    @Json(name = "match_percentage") val matchPercentage: Int? = null,
    @Json(name = "why_matches") val whyMatches: List<String> = emptyList(),
    @Json(name = "missing_skills") val missingSkills: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class JobDetailDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "employment_type") val employmentType: String,
    @Json(name = "department") val department: String? = null,
    @Json(name = "location") val location: String,
    @Json(name = "country") val country: String = "India",
    @Json(name = "city") val city: String? = null,
    @Json(name = "remote_type") val remoteType: String,
    @Json(name = "experience_min") val experienceMin: Double = 0.0,
    @Json(name = "experience_max") val experienceMax: Double? = null,
    @Json(name = "salary_min") val salaryMin: Double? = null,
    @Json(name = "salary_max") val salaryMax: Double? = null,
    @Json(name = "salary_currency") val salaryCurrency: String = "INR",
    @Json(name = "stipend") val stipend: Double? = null,
    @Json(name = "posted_at") val postedAt: String,
    @Json(name = "last_verified_at") val lastVerifiedAt: String,
    @Json(name = "application_deadline") val applicationDeadline: String? = null,
    @Json(name = "deadline_source") val deadlineSource: String? = null,
    @Json(name = "application_url") val applicationUrl: String,
    @Json(name = "source_url") val sourceUrl: String,
    @Json(name = "status") val status: String,
    @Json(name = "company") val company: CompanyDto,
    @Json(name = "source") val source: JobSourceDto,
    @Json(name = "match_percentage") val matchPercentage: Int? = null,
    @Json(name = "why_matches") val whyMatches: List<String> = emptyList(),
    @Json(name = "missing_skills") val missingSkills: List<String> = emptyList(),
    @Json(name = "description") val description: String = "",
    @Json(name = "education_requirements") val educationRequirements: List<String> = emptyList(),
    @Json(name = "degree_requirements") val degreeRequirements: List<String> = emptyList(),
    @Json(name = "branch_requirements") val branchRequirements: List<String> = emptyList(),
    @Json(name = "skills") val skills: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PaginatedJobsDto(
    @Json(name = "items") val items: List<JobDto>,
    @Json(name = "total") val total: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "size") val size: Int,
    @Json(name = "pages") val pages: Int
)

@JsonClass(generateAdapter = true)
data class SavedJobResponseDto(
    @Json(name = "saved_id") val savedId: String? = null,
    @Json(name = "job_id") val jobId: String,
    @Json(name = "category") val category: String = "Saved"
)

@JsonClass(generateAdapter = true)
data class AppliedJobRequest(
    @Json(name = "job_id") val jobId: String,
    @Json(name = "status") val status: String = "Applied",
    @Json(name = "applied_date") val appliedDate: String = "2026-09-26",
    @Json(name = "notes") val notes: String? = null
)

@JsonClass(generateAdapter = true)
data class AppliedJobUpdate(
    @Json(name = "status") val status: String,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "remarks") val remarks: String? = null
)

@JsonClass(generateAdapter = true)
data class AppliedJobDto(
    @Json(name = "id") val id: String,
    @Json(name = "job_id") val jobId: String,
    @Json(name = "status") val status: String,
    @Json(name = "applied_date") val appliedDate: String,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "job") val job: JobDto
)

@JsonClass(generateAdapter = true)
data class AISearchRequest(
    @Json(name = "query") val query: String,
    @Json(name = "session_id") val sessionId: String = "android_session"
)

@JsonClass(generateAdapter = true)
data class AISearchResponseDto(
    @Json(name = "summary") val summary: String,
    @Json(name = "structured_filters") val structuredFilters: Map<String, Any?> = emptyMap(),
    @Json(name = "results") val results: List<JobDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SubscriptionPlanDto(
    @Json(name = "id") val id: String,
    @Json(name = "plan_tier") val planTier: String,
    @Json(name = "name") val name: String,
    @Json(name = "price") val price: Double,
    @Json(name = "currency") val currency: String = "INR",
    @Json(name = "billing_cycle") val billingCycle: String = "MONTHLY",
    @Json(name = "max_active_jobs") val maxActiveJobs: Int,
    @Json(name = "refresh_interval_days") val refreshIntervalDays: Int,
    @Json(name = "ai_features_enabled") val aiFeaturesEnabled: Boolean,
    @Json(name = "expanded_alerts_enabled") val expandedAlertsEnabled: Boolean,
    @Json(name = "is_active") val isActive: Boolean
)

@JsonClass(generateAdapter = true)
data class SubscribeRequestDto(
    @Json(name = "plan_tier") val planTier: String,
    @Json(name = "external_order_id") val externalOrderId: String? = null
)

@JsonClass(generateAdapter = true)
data class UserSubscriptionDto(
    @Json(name = "id") val id: String,
    @Json(name = "plan") val plan: SubscriptionPlanDto,
    @Json(name = "status") val status: String,
    @Json(name = "starts_at") val startsAt: String,
    @Json(name = "expires_at") val expiresAt: String? = null
)

@JsonClass(generateAdapter = true)
data class DetectedRoleDto(
    @Json(name = "title") val title: String,
    @Json(name = "match_level") val matchLevel: String,
    @Json(name = "confidence_percentage") val confidencePercentage: Int,
    @Json(name = "evidence") val evidence: List<String> = emptyList(),
    @Json(name = "important_skills") val importantSkills: List<String> = emptyList(),
    @Json(name = "missing_skills") val missingSkills: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ResumeAnalysisDto(
    @Json(name = "candidate_name") val candidateName: String,
    @Json(name = "detected_degree") val detectedDegree: String,
    @Json(name = "detected_branch") val detectedBranch: String,
    @Json(name = "detected_graduation_year") val detectedGraduationYear: Int,
    @Json(name = "detected_skills") val detectedSkills: List<String> = emptyList(),
    @Json(name = "recommended_roles") val recommendedRoles: List<DetectedRoleDto> = emptyList(),
    @Json(name = "disclaimer") val disclaimer: String = ""
)

@JsonClass(generateAdapter = true)
data class ResumeAnalyzeRequestDto(
    @Json(name = "resume_text") val resumeText: String,
    @Json(name = "filename") val filename: String = "resume.pdf"
)

@JsonClass(generateAdapter = true)
data class PreferencesDto(
    @Json(name = "preferred_roles") val preferredRoles: List<String> = emptyList(),
    @Json(name = "employment_types") val employmentTypes: List<String> = emptyList(),
    @Json(name = "preferred_locations") val preferredLocations: List<String> = emptyList(),
    @Json(name = "remote_preferences") val remotePreferences: List<String> = emptyList(),
    @Json(name = "preferred_companies") val preferredCompanies: List<String> = emptyList(),
    @Json(name = "min_salary") val minSalary: Double? = null
)
