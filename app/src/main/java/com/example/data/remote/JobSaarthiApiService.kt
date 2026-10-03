package com.example.data.remote

import com.example.data.model.AISearchRequest
import com.example.data.model.AISearchResponseDto
import com.example.data.model.ApiResponse
import com.example.data.model.AppliedJobDto
import com.example.data.model.AppliedJobRequest
import com.example.data.model.AppliedJobUpdate
import com.example.data.model.AuthSuccessDto
import com.example.data.model.GoogleAuthRequest
import com.example.data.model.JobDetailDto
import com.example.data.model.JobDto
import com.example.data.model.LoginRequest
import com.example.data.model.PaginatedJobsDto
import com.example.data.model.PreferencesDto
import com.example.data.model.RefreshTokenRequest
import com.example.data.model.RegisterRequest
import com.example.data.model.ResumeAnalysisDto
import com.example.data.model.ResumeAnalyzeRequestDto
import com.example.data.model.SavedJobResponseDto
import com.example.data.model.SubscribeRequestDto
import com.example.data.model.SubscriptionPlanDto
import com.example.data.model.TokenPairDto
import com.example.data.model.UserDetailDto
import com.example.data.model.UserSubscriptionDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface JobSaarthiApiService {
    // Auth
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthSuccessDto>>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthSuccessDto>>

    @POST("auth/google")
    suspend fun googleAuth(@Body request: GoogleAuthRequest): Response<ApiResponse<AuthSuccessDto>>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<ApiResponse<TokenPairDto>>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    @DELETE("users/me")
    suspend fun deleteAccount(): Response<ApiResponse<Unit>>

    @GET("users/me")
    suspend fun getCurrentUser(): Response<ApiResponse<UserDetailDto>>

    // Jobs
    @GET("jobs")
    suspend fun getJobs(
        @Query("keyword") keyword: String? = null,
        @Query("company") company: String? = null,
        @Query("employment_type") employmentType: String? = null,
        @Query("location") location: String? = null,
        @Query("remote_type") remoteType: String? = null,
        @Query("experience_max") experienceMax: Double? = null,
        @Query("posted_days") postedDays: Int? = null,
        @Query("sort_by") sortBy: String = "newest",
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PaginatedJobsDto>>

    @GET("jobs/today")
    suspend fun getTodayJobs(): Response<ApiResponse<List<JobDto>>>

    @GET("jobs/recommended")
    suspend fun getRecommendedJobs(): Response<ApiResponse<List<JobDto>>>

    @GET("jobs/{id}")
    suspend fun getJobDetail(@Path("id") id: String): Response<ApiResponse<JobDetailDto>>

    @POST("jobs/{id}/save")
    suspend fun saveJob(
        @Path("id") id: String,
        @Query("category") category: String = "Saved",
        @Query("notes") notes: String? = null
    ): Response<ApiResponse<SavedJobResponseDto>>

    @DELETE("jobs/{id}/save")
    suspend fun deleteSavedJob(@Path("id") id: String): Response<ApiResponse<Map<String, Any>>>

    @GET("jobs/saved")
    suspend fun getSavedJobs(@Query("category") category: String? = null): Response<ApiResponse<List<JobDto>>>

    // Applications Tracker
    @POST("applications")
    suspend fun applyJob(@Body request: AppliedJobRequest): Response<ApiResponse<AppliedJobDto>>

    @GET("applications")
    suspend fun getApplications(@Query("status") status: String? = null): Response<ApiResponse<List<AppliedJobDto>>>

    @GET("applications/stats")
    suspend fun getApplicationStats(): Response<ApiResponse<Map<String, Int>>>

    @PATCH("applications/{id}")
    suspend fun updateApplicationStatus(
        @Path("id") id: String,
        @Body request: AppliedJobUpdate
    ): Response<ApiResponse<AppliedJobDto>>

    // AI Natural Language Search
    @POST("ai/search")
    suspend fun aiSearch(@Body request: AISearchRequest): Response<ApiResponse<AISearchResponseDto>>

    // Resume & Preferences
    @POST("resume/analyze")
    suspend fun analyzeResume(@Body request: ResumeAnalyzeRequestDto): Response<ApiResponse<ResumeAnalysisDto>>

    @GET("preferences")
    suspend fun getPreferences(): Response<ApiResponse<PreferencesDto>>

    @PATCH("preferences")
    suspend fun updatePreferences(@Body request: PreferencesDto): Response<ApiResponse<PreferencesDto>>

    // Subscriptions
    @GET("plans")
    suspend fun getPlans(): Response<ApiResponse<List<SubscriptionPlanDto>>>

    @POST("subscriptions/subscribe")
    suspend fun subscribePlan(@Body request: SubscribeRequestDto): Response<ApiResponse<UserSubscriptionDto>>

    // Health
    @GET("health")
    suspend fun healthCheck(): Response<Map<String, Any>>
}
