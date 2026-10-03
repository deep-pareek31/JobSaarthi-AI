package com.example.ui.screens.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CompanyDto
import com.example.data.model.JobDto
import com.example.data.model.JobSourceDto
import com.example.ui.components.JobCard
import com.example.ui.screens.auth.AuthViewModel
import com.example.ui.viewmodel.JobViewModel

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    jobViewModel: JobViewModel,
    onNavigateToJobDetail: (String) -> Unit,
    onNavigateToTab: (Int) -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val todayJobs by jobViewModel.todayJobs.collectAsStateWithLifecycle()
    val recommendedJobs by jobViewModel.recommendedJobs.collectAsStateWithLifecycle()
    val savedJobs by jobViewModel.savedJobs.collectAsStateWithLifecycle()
    val appliedJobs by jobViewModel.appliedJobs.collectAsStateWithLifecycle()
    val uiState by jobViewModel.uiState.collectAsStateWithLifecycle()

    val displayName = currentUser?.fullName?.split(" ")?.firstOrNull() ?: "Candidate"
    val planTier = currentUser?.subscriptionTier ?: "FREE"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. Welcome & Greeting Banner
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Good morning, $displayName 👋",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Your job preferences: Software Engineer • Data Scientist",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = planTier,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // 2. Application Tracker Summary Stats
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "APPLICATION TRACKER",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        TextButton(
                            onClick = { onNavigateToTab(3) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("View All", style = MaterialTheme.typography.labelSmall)
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TrackerStatItem(
                            label = "Applied",
                            count = "${uiState.stats["total_applied"] ?: appliedJobs.size}",
                            icon = Icons.AutoMirrored.Filled.Send,
                            modifier = Modifier.weight(1f)
                        )
                        TrackerStatItem(
                            label = "Interviews",
                            count = "${uiState.stats["interviews"] ?: 0}",
                            icon = Icons.Default.VerifiedUser,
                            modifier = Modifier.weight(1f)
                        )
                        TrackerStatItem(
                            label = "Saved",
                            count = "${savedJobs.size}",
                            icon = Icons.Default.Bookmark,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 3. Today's Openings Section
        item {
            SectionHeader(
                title = "TODAY'S OPENINGS",
                subtitle = "Newly detected and recently verified from official sources",
                onSeeAll = { onNavigateToTab(1) }
            )
        }

        if (todayJobs.isEmpty()) {
            item {
                EmptyStatePlaceholder(text = "Scanning sources for today's active openings...")
            }
        } else {
            items(todayJobs.take(3), key = { "today_${it.id}" }) { job ->
                val isSaved = savedJobs.any { it.jobId == job.id }
                val isApplied = appliedJobs.any { it.jobId == job.id }

                JobCard(
                    title = job.title,
                    company = job.companyName,
                    employmentType = job.employmentType,
                    location = job.location,
                    remoteType = job.remoteType,
                    postedDate = "Posted: Today",
                    deadline = job.deadlineSource ?: job.applicationDeadline,
                    sourceName = job.sourceName,
                    matchPercentage = job.matchPercentage ?: 88,
                    isSaved = isSaved,
                    isApplied = isApplied,
                    onClick = { onNavigateToJobDetail(job.id) },
                    onSaveToggle = {
                        val dto = JobDto(
                            id = job.id,
                            title = job.title,
                            employmentType = job.employmentType,
                            location = job.location,
                            remoteType = job.remoteType,
                            postedAt = job.postedAt,
                            lastVerifiedAt = job.postedAt,
                            applicationDeadline = job.applicationDeadline,
                            deadlineSource = job.deadlineSource,
                            applicationUrl = job.applicationUrl,
                            sourceUrl = job.applicationUrl,
                            status = "ACTIVE",
                            company = CompanyDto(id = "comp", name = job.companyName),
                            source = JobSourceDto(id = "src", name = job.sourceName, sourceType = "ATS", baseUrl = "", isEnabled = true)
                        )
                        if (isSaved) jobViewModel.unsaveJob(job.id) else jobViewModel.saveJob(dto)
                    },
                    onMarkApplied = {
                        jobViewModel.markApplied(job.id)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        // 4. Best Matches For You (Quota enforced: 3 for Free, 20 for Pro)
        item {
            Spacer(modifier = Modifier.height(12.dp))
            SectionHeader(
                title = "RECOMMENDED FOR YOU",
                subtitle = if (planTier == "FREE") "Personalized to your resume (Free plan: 3 active matches)" else "Top matches for your profile",
                onSeeAll = { onNavigateToTab(1) }
            )
        }

        if (recommendedJobs.isEmpty()) {
            item {
                EmptyStatePlaceholder(text = "Calculating profile matches from active database...")
            }
        } else {
            items(recommendedJobs.take(3), key = { "rec_${it.id}" }) { job ->
                val isSaved = savedJobs.any { it.jobId == job.id }
                val isApplied = appliedJobs.any { it.jobId == job.id }

                JobCard(
                    title = job.title,
                    company = job.companyName,
                    employmentType = job.employmentType,
                    location = job.location,
                    remoteType = job.remoteType,
                    postedDate = "Posted: Recently",
                    deadline = job.deadlineSource ?: job.applicationDeadline,
                    sourceName = job.sourceName,
                    matchPercentage = job.matchPercentage ?: 92,
                    isSaved = isSaved,
                    isApplied = isApplied,
                    onClick = { onNavigateToJobDetail(job.id) },
                    onSaveToggle = {
                        val dto = JobDto(
                            id = job.id,
                            title = job.title,
                            employmentType = job.employmentType,
                            location = job.location,
                            remoteType = job.remoteType,
                            postedAt = job.postedAt,
                            lastVerifiedAt = job.postedAt,
                            applicationDeadline = job.applicationDeadline,
                            deadlineSource = job.deadlineSource,
                            applicationUrl = job.applicationUrl,
                            sourceUrl = job.applicationUrl,
                            status = "ACTIVE",
                            company = CompanyDto(id = "comp", name = job.companyName),
                            source = JobSourceDto(id = "src", name = job.sourceName, sourceType = "ATS", baseUrl = "", isEnabled = true)
                        )
                        if (isSaved) jobViewModel.unsaveJob(job.id) else jobViewModel.saveJob(dto)
                    },
                    onMarkApplied = {
                        jobViewModel.markApplied(job.id)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        // 5. Legal Disclaimer Notice
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Legal disclaimer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "JobSaarthi is a job discovery and tracking platform. Job information is collected from third-party sources and may change. Users should verify details and eligibility on the employer's official application page before applying.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    onSeeAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TextButton(onClick = onSeeAll, contentPadding = PaddingValues(0.dp)) {
            Text("See All", style = MaterialTheme.typography.labelSmall)
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
private fun TrackerStatItem(
    label: String,
    count: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyStatePlaceholder(text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
