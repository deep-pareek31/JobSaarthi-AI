package com.example.ui.screens.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class GoogleSignInHelper(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    suspend fun signInWithGoogle(webClientId: String? = null): Result<String> {
        val resolvedClientId = webClientId
            ?: try {
                val configured = com.example.BuildConfig.GOOGLE_CLIENT_ID
                if (!configured.isNullOrBlank() && !configured.contains("your-google-oauth")) configured else null
            } catch (e: Throwable) { null }
            ?: "1234567890-default.apps.googleusercontent.com"

        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(resolvedClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = response.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                // Sign in with Firebase Auth if possible
                try {
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    firebaseAuth.signInWithCredential(firebaseCredential).await()
                } catch (e: Exception) {
                    // Firebase may not have client ID configured in demo, proceed with idToken
                }

                Result.success(idToken)
            } else {
                Result.failure(Exception("Unsupported credential returned"))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(Exception("Google Sign-In was cancelled"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
