package com.example.ui.screens.applied

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AppliedJobEntity
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRed
import com.example.ui.viewmodel.JobViewModel

@Composable
fun AppliedTrackerScreen(
    viewModel: JobViewModel,
    onNavigateToJobDetail: (String) -> Unit
) {
    val appliedJobs by viewModel.appliedJobs.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var updatingApp by remember { mutableStateOf<AppliedJobEntity?>(null) }
    var selectedNewStatus by remember { mutableStateOf("Interview") }
    var updateRemarks by remember { mutableStateOf("") }

    val filteredList = if (selectedStatusFilter == "ALL") {
        appliedJobs
    } else {
        appliedJobs.filter { it.status.equals(selectedStatusFilter, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Application Dashboard Stats
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "APPLICATION PIPELINE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PipelineStatBox(
                        title = "Applied",
                        count = "${appliedJobs.size}",
                        icon = Icons.AutoMirrored.Filled.Send,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    PipelineStatBox(
                        title = "Interviews",
                        count = "${appliedJobs.count { it.status.contains("Interview", ignoreCase = true) }}",
                        icon = Icons.Default.VerifiedUser,
                        color = Color(0xFF8B5CF6),
                        modifier = Modifier.weight(1f)
                    )
                    PipelineStatBox(
                        title = "Offers",
                        count = "${appliedJobs.count { it.status.equals("Offer", ignoreCase = true) }}",
                        icon = Icons.Default.CheckCircle,
                        color = AccentEmerald,
                        modifier = Modifier.weight(1f)
                    )
                    PipelineStatBox(
                        title = "Pending",
                        count = "${appliedJobs.count { it.status in listOf("Applied", "Assessment") }}",
                        icon = Icons.Default.HourglassEmpty,
                        color = AccentAmber,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Status Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val statusOptions = listOf("ALL", "Applied", "Assessment", "Interview", "Offer", "Rejected")
            statusOptions.forEach { st ->
                FilterChip(
                    selected = selectedStatusFilter == st,
                    onClick = { selectedStatusFilter = st },
                    label = { Text(st, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        // Applications List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No applications in '${selectedStatusFilter}'",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "When you click 'I Applied' on any job, it automatically moves to this tracker.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { app ->
                    ApplicationTrackerCard(
                        app = app,
                        onClick = { onNavigateToJobDetail(app.jobId) },
                        onUpdateStatus = {
                            updatingApp = app
                            selectedNewStatus = app.status
                            updateRemarks = ""
                        }
                    )
                }
            }
        }
    }

    // Update Status Dialog
    if (updatingApp != null) {
        val app = updatingApp!!
        val statuses = listOf(
            "Applied", "Assessment", "Interview", "Technical Interview",
            "HR Interview", "Offer", "Rejected", "Withdrawn", "No Response"
        )
        var showDropdown by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { updatingApp = null },
            title = { Text("Update Application Status", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(text = "${app.title} at ${app.companyName}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Current Status: ${app.status}", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    Box {
                        OutlinedButton(
                            onClick = { showDropdown = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("New Status: $selectedNewStatus", fontWeight = FontWeight.Bold)
                        }

                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false }
                        ) {
                            statuses.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s) },
                                    onClick = {
                                        selectedNewStatus = s
                                        showDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = updateRemarks,
                        onValueChange = { updateRemarks = it },
                        label = { Text("Remarks (e.g. Cleared round 1)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateApplicationStatus(
                            id = app.id,
                            status = selectedNewStatus,
                            remarks = updateRemarks.ifBlank { null }
                        )
                        updatingApp = null
                    }
                ) {
                    Text("Save Status")
                }
            },
            dismissButton = {
                TextButton(onClick = { updatingApp = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PipelineStatBox(
    title: String,
    count: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = count, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = color)
            Text(text = title, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ApplicationTrackerCard(
    app: AppliedJobEntity,
    onClick: () -> Unit,
    onUpdateStatus: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("applied_job_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = app.companyName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                StatusBadge(status = app.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = app.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Applied Date: ${app.appliedDate} • Location: ${app.location}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!app.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Notes: ${app.notes}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
                    Text("View Job Post", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = onUpdateStatus,
                    modifier = Modifier.height(34.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Update Status", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (bgColor, textColor) = when {
        status.contains("Offer", ignoreCase = true) -> AccentEmerald.copy(alpha = 0.2f) to AccentEmerald
        status.contains("Interview", ignoreCase = true) -> Color(0xFF8B5CF6).copy(alpha = 0.2f) to Color(0xFF8B5CF6)
        status.contains("Reject", ignoreCase = true) -> AccentRed.copy(alpha = 0.2f) to AccentRed
        status.contains("Assessment", ignoreCase = true) -> AccentAmber.copy(alpha = 0.2f) to AccentAmber
        else -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
