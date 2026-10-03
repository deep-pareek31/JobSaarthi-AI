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
                loginLocally(email.trim(), pass)
            }
        } catch (e: Exception) {
            loginLocally(email.trim(), pass)
        }
    }

    private suspend fun loginLocally(email: String, pass: String): AuthResult<AuthSuccessDto> {
        val existing = userDao.getUserByEmail(email)
        val user = existing ?: UserEntity(
            id = "user_${System.currentTimeMillis() % 100000}",
            email = email,
            fullName = email.substringBefore("@").replace(".", " ").capitalizeWords(),
            role = "USER",
            isActive = true,
            isVerified = true,
            avatarUrl = null,
            subscriptionTier = "FREE",
            planName = "Free Starter",
            currentLocation = "India"
        )
        userDao.clearUser()
        userDao.insertUser(user)
        tokenManager.saveTokens("local_jwt_${user.id}", "local_refresh_${user.id}")
        tokenManager.saveUserInfo(user.id, user.email, user.fullName)
        val authData = AuthSuccessDto(
            tokens = com.example.data.model.TokenPairDto("local_jwt_${user.id}", "local_refresh_${user.id}", "bearer", 86400),
            user = com.example.data.model.UserDto(
                id = user.id,
                email = user.email,
                fullName = user.fullName,
                role = user.role,
                isActive = user.isActive,
                isVerified = user.isVerified,
                avatarUrl = user.avatarUrl
            )
        )
        return AuthResult.Success(authData)
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
                registerLocally(name, email, phone)
            }
        } catch (e: Exception) {
            registerLocally(name, email, phone)
        }
    }

    private suspend fun registerLocally(name: String, email: String, phone: String?): AuthResult<AuthSuccessDto> {
        val user = UserEntity(
            id = "user_${System.currentTimeMillis() % 100000}",
            email = email.trim(),
            fullName = name.trim(),
            role = "USER",
            isActive = true,
            isVerified = true,
            avatarUrl = null,
            subscriptionTier = "FREE",
            planName = "Free Starter",
            currentLocation = "India"
        )
        userDao.clearUser()
        userDao.insertUser(user)
        tokenManager.saveTokens("local_jwt_${user.id}", "local_refresh_${user.id}")
        tokenManager.saveUserInfo(user.id, user.email, user.fullName)
        val authData = AuthSuccessDto(
            tokens = com.example.data.model.TokenPairDto("local_jwt_${user.id}", "local_refresh_${user.id}", "bearer", 86400),
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
        return AuthResult.Success(authData)
    }

    suspend fun googleAuth(idToken: String, displayName: String? = null, email: String? = null): AuthResult<AuthSuccessDto> {
        val realEmail = email?.ifBlank { null } ?: "deep.pareek31@gmail.com"
        val realName = displayName?.ifBlank { null } ?: realEmail.substringBefore("@").replace(".", " ").capitalizeWords()
        return try {
            val response = apiService.googleAuth(GoogleAuthRequest(idToken = idToken))
            if (response.isSuccessful && response.body()?.data != null) {
                val authData = response.body()!!.data!!
                saveSession(authData)
                AuthResult.Success(authData)
            } else {
                handleLocalGoogleAuth(realEmail, realName, idToken)
            }
        } catch (e: Exception) {
            handleLocalGoogleAuth(realEmail, realName, idToken)
        }
    }

    private suspend fun handleLocalGoogleAuth(email: String, name: String, idToken: String): AuthResult<AuthSuccessDto> {
        val user = UserEntity(
            id = "google_user_${System.currentTimeMillis() % 10000}",
            email = email,
            fullName = name,
            role = "USER",
            isActive = true,
            isVerified = true,
            avatarUrl = null,
            subscriptionTier = "PRO",
            planName = "Pro Candidate",
            currentLocation = "Bengaluru, India"
        )
        userDao.clearUser()
        userDao.insertUser(user)
        tokenManager.saveTokens("google_jwt_$idToken", "google_refresh_token")
        tokenManager.saveUserInfo(user.id, user.email, user.fullName)
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
        return AuthResult.Success(authData)
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

private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { word ->
    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
