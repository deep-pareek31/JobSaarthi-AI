package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.applied.AppliedTrackerScreen
import com.example.ui.screens.auth.AuthViewModel
import com.example.ui.screens.discover.DiscoverScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.saved.SavedJobsScreen
import com.example.ui.viewmodel.JobViewModel

sealed class NavigationTab(val index: Int, val label: String, val icon: ImageVector, val tag: String) {
    data object Home : NavigationTab(0, "Home", Icons.Default.Home, "tab_home")
    data object Discover : NavigationTab(1, "Discover", Icons.Default.Explore, "tab_discover")
    data object Saved : NavigationTab(2, "Saved", Icons.Default.Bookmark, "tab_saved")
    data object Applied : NavigationTab(3, "Applied", Icons.AutoMirrored.Filled.Assignment, "tab_applied")
    data object Profile : NavigationTab(4, "Profile", Icons.Default.Person, "tab_profile")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    authViewModel: AuthViewModel,
    jobViewModel: JobViewModel,
    onNavigateToJobDetail: (String) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val savedJobs by jobViewModel.savedJobs.collectAsStateWithLifecycle()
    val appliedJobs by jobViewModel.appliedJobs.collectAsStateWithLifecycle()

    val tabs = listOf(
        NavigationTab.Home,
        NavigationTab.Discover,
        NavigationTab.Saved,
        NavigationTab.Applied,
        NavigationTab.Profile
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Work,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
                        Text(
                            text = "JobSaarthi",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Notifications center */ }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab.index
                    val badgeCount = when (tab) {
                        NavigationTab.Saved -> savedJobs.size
                        NavigationTab.Applied -> appliedJobs.size
                        else -> 0
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab.index },
                        icon = {
                            if (badgeCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge {
                                            Text("$badgeCount")
                                        }
                                    }
                                ) {
                                    Icon(imageVector = tab.icon, contentDescription = tab.label)
                                }
                            } else {
                                Icon(imageVector = tab.icon, contentDescription = tab.label)
                            }
                        },
                        label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag(tab.tag),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    authViewModel = authViewModel,
                    jobViewModel = jobViewModel,
                    onNavigateToJobDetail = onNavigateToJobDetail,
                    onNavigateToTab = { selectedTab = it }
                )
                1 -> DiscoverScreen(
                    viewModel = jobViewModel,
                    onNavigateToJobDetail = onNavigateToJobDetail
                )
                2 -> SavedJobsScreen(
                    viewModel = jobViewModel,
                    onNavigateToJobDetail = onNavigateToJobDetail
                )
                3 -> AppliedTrackerScreen(
                    viewModel = jobViewModel,
                    onNavigateToJobDetail = onNavigateToJobDetail
                )
                4 -> ProfileScreen(
                    authViewModel = authViewModel,
                    jobViewModel = jobViewModel,
                    onLogout = onLogout
                )
            }
        }
    }
}
