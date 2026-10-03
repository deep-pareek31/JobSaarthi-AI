package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.data.local.AppDatabase
import com.example.data.remote.ApiClient
import com.example.data.repository.AuthRepository
import com.example.data.repository.JobRepository
import com.example.data.repository.TokenManager
import com.example.ui.navigation.AppNavHost
import com.example.ui.navigation.Screen
import com.example.ui.screens.auth.AuthViewModel
import com.example.ui.theme.JobSaarthiTheme
import com.example.ui.viewmodel.JobViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val tokenManager = TokenManager(applicationContext)
        val apiService = ApiClient.getApiService(applicationContext)
        val repository = AuthRepository(apiService, database.userDao(), tokenManager)
        AuthViewModel.Factory(repository)
    }

    private val jobViewModel: JobViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val apiService = ApiClient.getApiService(applicationContext)
        val repository = JobRepository(apiService, database)
        JobViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val tokenManager = TokenManager(applicationContext)
        val startDest = if (tokenManager.isLoggedIn()) Screen.Main.route else Screen.Login.route

        setContent {
            JobSaarthiTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    AppNavHost(
                        navController = navController,
                        authViewModel = authViewModel,
                        jobViewModel = jobViewModel,
                        startDestination = startDest
                    )
                }
            }
        }
    }
}
