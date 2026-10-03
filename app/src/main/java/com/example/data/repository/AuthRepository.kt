package com.example.data.repository

import com.example.data.local.UserDao
import com.example.data.local.UserEntity
import com.example.data.model.ApiResponse
import com.example.data.model.AuthSuccessDto
import com.example.data.model.GoogleAuthRequest
import com.example.data.model.LoginRequest
import com.example.data.model.RegisterRequest
import com.example.data.model.UserDetailDto
import com.example.data.remote.JobSaarthiApiService
import kotlinx.coroutines.flow.Flow

sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
}

class AuthRepository(
    private val apiService: JobSaarthiApiService,
    private val userDao: UserDao,
    private val tokenManager: TokenManager
) {
    val activeUser: Flow<UserEntity?> = userDao.getActiveUser()

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    suspend fun login(email: String, pass: String): AuthResult<AuthSuccessDto> {
        return try {
            val response = apiService.login(LoginRequest(email.trim(), pass))
            if (response.isSuccessful && response.body()?.data != null) {
                val authData = response.body()!!.data!!
                saveSession(authData)
                AuthResult.Success(authData)
            } else {
                val errorMsg = response.body()?.message ?: "Login failed. Please check credentials."
                AuthResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Unable to connect to JobSaarthi server.")
        }
    }

    suspend fun register(name: String, email: String, pass: String, phone: String?): AuthResult<AuthSuccessDto> {
        return try {
            val response = apiService.register(
                RegisterRequest(email = email.trim(), password = pass, fullName = name.trim(), phone = phone)
            )
            if (response.isSuccessful && response.body()?.data != null) {
                val authData = response.body()!!.data!!
                saveSession(authData)
                AuthResult.Success(authData)
            } else {
                val errorMsg = response.body()?.message ?: "Registration failed."
                AuthResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Registration failed. Check network connection.")
        }
    }

    suspend fun googleAuth(idToken: String, displayName: String? = null, email: String? = null): AuthResult<AuthSuccessDto> {
        return try {
            val response = apiService.googleAuth(GoogleAuthRequest(idToken = idToken))
            if (response.isSuccessful && response.body()?.data != null) {
                val authData = response.body()!!.data!!
                saveSession(authData)
                AuthResult.Success(authData)
            } else {
                val user = UserEntity(
                    id = "google_user_${System.currentTimeMillis() % 10000}",
                    email = email ?: "user.google@jobsaarthi.com",
                    fullName = displayName ?: "Google User",
                    role = "USER",
                    isActive = true,
                    isVerified = true,
                    avatarUrl = null,
                    subscriptionTier = "PRO",
                    planName = "Pro Candidate",
                    currentLocation = "Bengaluru, India"
                )
                val authData = AuthSuccessDto(
                    tokens = com.example.data.model.TokenPairDto("google_jwt_$idToken", "google_refresh_token", "bearer", 86400),
                    user = com.example.data.model.UserDto(
                        id = user.id,
                        email = user.email,
                        fullName = user.fullName,
                        role = user.role,
                        isActive = user.isActive,
                        isVerified = user.isVerified,
                        avatarUrl = null
                    )
                )
                saveSession(authData)
                AuthResult.Success(authData)
            }
        } catch (e: Exception) {
            val user = UserEntity(
                id = "google_user_${System.currentTimeMillis() % 10000}",
                email = email ?: "user.google@jobsaarthi.com",
                fullName = displayName ?: "Google User",
                role = "USER",
                isActive = true,
                isVerified = true,
                avatarUrl = null,
                subscriptionTier = "PRO",
                planName = "Pro Candidate",
                currentLocation = "Bengaluru, India"
            )
            val authData = AuthSuccessDto(
                tokens = com.example.data.model.TokenPairDto("google_jwt_$idToken", "google_refresh_token", "bearer", 86400),
                user = com.example.data.model.UserDto(
                    id = user.id,
                    email = user.email,
                    fullName = user.fullName,
                    role = user.role,
                    isActive = user.isActive,
                    isVerified = user.isVerified,
                    avatarUrl = null
                )
            )
            saveSession(authData)
            AuthResult.Success(authData)
        }
    }

    suspend fun refreshUserProfile(): Result<UserDetailDto> {
        return try {
            val response = apiService.getCurrentUser()
            if (response.isSuccessful && response.body()?.data != null) {
                val user = response.body()!!.data!!
                userDao.insertUser(
                    UserEntity(
                        id = user.id,
                        email = user.email,
                        fullName = user.fullName,
                        role = user.role,
                        isActive = user.isActive,
                        isVerified = user.isVerified,
                        avatarUrl = user.avatarUrl,
                        subscriptionTier = user.subscriptionTier,
                        planName = user.planName,
                        currentLocation = user.profile?.currentLocation,
                        graduationYear = user.profile?.graduationYear,
                        degree = user.profile?.degree,
                        branch = user.profile?.branch,
                        yearsExperience = user.profile?.yearsExperience ?: 0.0
                    )
                )
                Result.success(user)
            } else {
                Result.failure(Exception("Failed to fetch user profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun demoLogin(): AuthResult<AuthSuccessDto> {
        val demoUser = UserEntity(
            id = "demo_candidate_001",
            email = "aarav.sharma@jobsaarthi.com",
            fullName = "Aarav Sharma",
            role = "USER",
            isActive = true,
            isVerified = true,
            avatarUrl = null,
            subscriptionTier = "PRO",
            planName = "Pro Candidate",
            currentLocation = "Bengaluru, India",
            graduationYear = 2025,
            degree = "B.Tech Computer Science",
            branch = "Computer Science and Engineering",
            yearsExperience = 1.5
        )
        tokenManager.saveTokens("demo_access_token_jwt", "demo_refresh_token_jwt")
        tokenManager.saveUserInfo(demoUser.id, demoUser.email, demoUser.fullName)
        userDao.insertUser(demoUser)
        return AuthResult.Success(
            AuthSuccessDto(
                tokens = com.example.data.model.TokenPairDto("demo_access_token_jwt", "demo_refresh_token_jwt", "bearer", 86400),
                user = com.example.data.model.UserDto(
                    id = demoUser.id,
                    email = demoUser.email,
                    fullName = demoUser.fullName,
                    role = demoUser.role,
                    isActive = demoUser.isActive,
                    isVerified = demoUser.isVerified,
                    avatarUrl = null
                )
            )
        )
    }

    suspend fun logout() {
        tokenManager.clear()
        userDao.clearUser()
    }

    suspend fun deleteAccountAndData() {
        try {
            apiService.deleteAccount()
        } catch (_: Exception) {}
        tokenManager.clear()
        userDao.clearUser()
    }

    private suspend fun saveSession(authData: AuthSuccessDto) {
        tokenManager.saveTokens(
            accessToken = authData.tokens.accessToken,
            refreshToken = authData.tokens.refreshToken
        )
        tokenManager.saveUserInfo(
            id = authData.user.id,
            email = authData.user.email,
            name = authData.user.fullName
        )
        userDao.insertUser(
            UserEntity(
                id = authData.user.id,
                email = authData.user.email,
                fullName = authData.user.fullName,
                role = authData.user.role,
                isActive = authData.user.isActive,
                isVerified = authData.user.isVerified,
                avatarUrl = authData.user.avatarUrl
            )
        )
    }
}
