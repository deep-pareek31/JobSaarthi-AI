package com.example.ui.screens.discover

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.RealCompanyPortals
import com.example.ui.components.JobCard
import com.example.ui.viewmodel.JobViewModel

@Composable
fun DiscoverScreen(
    viewModel: JobViewModel,
    onNavigateToJobDetail: (String) -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val savedJobs by viewModel.savedJobs.collectAsStateWithLifecycle()
    val appliedJobs by viewModel.appliedJobs.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterChip by remember { mutableStateOf("ALL") }
    var selectedSort by remember { mutableStateOf("newest") }
    var showSortMenu by remember { mutableStateOf(false) }
    var showCreateAlertDialog by remember { mutableStateOf(false) }
    var showJobAlertsModal by remember { mutableStateOf(false) }

    val alertService = remember { com.example.data.service.JobAlertService.getInstance() }
    val alerts by alertService.alerts.collectAsStateWithLifecycle()

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search & AI Bar Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Search: 'Software Engineer', 'Google', 'Python'...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                        IconButton(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.runAiSearch(searchQuery)
                            },
                            modifier = Modifier.testTag("ai_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI natural language search",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        if (searchQuery.split(" ").size > 3) {
                            viewModel.runAiSearch(searchQuery)
                        } else {
                            viewModel.search(keyword = searchQuery, sortBy = selectedSort)
                        }
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_text_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Filter Chips Row, Live Web Search & Sort Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Live Web Jobs Chip
                    FilterChip(
                        selected = false,
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.searchRealWebJobs(searchQuery)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.TravelExplore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            Text(
                                "Live Web Jobs",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier.testTag("live_web_jobs_chip")
                    )

                    // Save Search as Job Alert Chip
                    FilterChip(
                        selected = false,
                        onClick = { showCreateAlertDialog = true },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            Text(
                                "Save Alert",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.testTag("save_alert_chip")
                    )

                    // View Job Alerts Manager Chip
                    FilterChip(
                        selected = false,
                        onClick = { showJobAlertsModal = true },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            Text("Alerts (${alerts.count { it.isActive }})", style = MaterialTheme.typography.labelSmall)
                        },
                        modifier = Modifier.testTag("view_alerts_chip")
                    )

                    val chips = listOf(
                        "ALL" to "All",
                        "INTERNSHIP" to "Internships",
                        "FULL_TIME" to "Full Time",
                        "REMOTE" to "Remote",
                        "HYBRID" to "Hybrid",
                        "BENGALURU" to "Bengaluru"
                    )

                    chips.forEach { (key, label) ->
                        FilterChip(
                            selected = selectedFilterChip == key,
                            onClick = {
                                selectedFilterChip = key
                                val emp = if (key in listOf("INTERNSHIP", "FULL_TIME")) key else null
                                val rem = if (key in listOf("REMOTE", "HYBRID")) key else null
                                val loc = if (key == "BENGALURU") "Bengaluru" else null
                                viewModel.search(
                                    keyword = searchQuery.ifBlank { null },
                                    employmentType = emp,
                                    remoteType = rem,
                                    location = loc,
                                    sortBy = selectedSort
                                )
                            },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                // Sort Dropdown Menu
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort jobs",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Newest Posted") },
                            onClick = {
                                selectedSort = "newest"
                                showSortMenu = false
                                viewModel.search(keyword = searchQuery.ifBlank { null }, sortBy = "newest")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Deadline Soon") },
                            onClick = {
                                selectedSort = "deadline_soon"
                                showSortMenu = false
                                viewModel.search(keyword = searchQuery.ifBlank { null }, sortBy = "deadline_soon")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Company Name") },
                            onClick = {
                                selectedSort = "company"
                                showSortMenu = false
                                viewModel.search(keyword = searchQuery.ifBlank { null }, sortBy = "company")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Highest Salary") },
                            onClick = {
                                selectedSort = "salary"
                                showSortMenu = false
                                viewModel.search(keyword = searchQuery.ifBlank { null }, sortBy = "salary")
                            }
                        )
                    }
                }
            }
        }

        // Official Company Career Portals Hub (Google, Microsoft, Amazon, etc.)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Official Career Portals",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "1-Tap Direct Search",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RealCompanyPortals.portals.forEach { portal ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        ),
                        onClick = {
                            val url = portal.urlBuilder(searchQuery)
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.testTag("portal_${portal.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(portal.brandColorHex), shape = RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = portal.name,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // AI Search Summary Banner (if active)
        if (uiState.aiSearchSummary != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = uiState.aiSearchSummary ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Job Listings Results
        if (uiState.isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Searching verified opportunities...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (uiState.searchResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No open jobs matched your criteria. Try widening filters or search query.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.searchResults, key = { it.id }) { job ->
                    val isSaved = savedJobs.any { it.jobId == job.id }
                    val isApplied = appliedJobs.any { it.jobId == job.id }

                    JobCard(
                        title = job.title,
                        company = job.company.name,
                        employmentType = job.employmentType,
                        location = job.location,
                        remoteType = job.remoteType,
                        postedDate = "Posted recently",
                        deadline = job.deadlineSource ?: job.applicationDeadline,
                        sourceName = job.source.name,
                        matchPercentage = job.matchPercentage ?: 85,
                        isSaved = isSaved,
                        isApplied = isApplied,
                        onClick = { onNavigateToJobDetail(job.id) },
                        onSaveToggle = {
                            if (isSaved) viewModel.unsaveJob(job.id) else viewModel.saveJob(job)
                        },
                        onMarkApplied = {
                            viewModel.markApplied(job.id)
                        }
                    )
                }
            }
        }
    }

    if (showCreateAlertDialog) {
        com.example.ui.screens.alerts.CreateJobAlertDialog(
            initialQuery = searchQuery,
            initialLocation = if (selectedFilterChip == "BENGALURU") "Bengaluru" else "Any Location",
            userEmail = "deep.pareek31@gmail.com",
            onDismiss = { showCreateAlertDialog = false },
            onCreate = { query, loc, type, freq, email ->
                alertService.createAlert(query, loc, type, freq, email)
                showCreateAlertDialog = false
            }
        )
    }

    if (showJobAlertsModal) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showJobAlertsModal = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                com.example.ui.screens.alerts.JobAlertsScreen(
                    alertService = alertService,
                    availableJobs = if (uiState.searchResults.isNotEmpty()) uiState.searchResults else com.example.data.repository.DefaultJobData.curatedJobDetails.map { com.example.data.repository.DefaultJobData.toJobDto(it) },
                    userEmail = "deep.pareek31@gmail.com",
                    onBack = { showJobAlertsModal = false }
                )
            }
        }
    }
}
