package com.attendance.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.attendance.app.domain.AttendanceRecord
import com.attendance.app.domain.Employee
import com.attendance.app.repository.AttendanceRepositoryImpl
import com.attendance.app.repository.EmployeeRepositoryImpl
import com.attendance.app.ui.components.*
import com.attendance.app.ui.navigation.NavigationState
import com.attendance.app.ui.navigation.Screen
import com.attendance.app.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun EmployeeProfileScreen(employeeId: Int, navigationState: NavigationState) {
    val employeeRepo = remember { EmployeeRepositoryImpl() }
    val attendanceRepo = remember { AttendanceRepositoryImpl() }
    val scope = rememberCoroutineScope()

    var employee by remember { mutableStateOf<Employee?>(null) }
    var attendanceRecords by remember { mutableStateOf<List<AttendanceRecord>>(emptyList()) }
    var stats by remember { mutableStateOf<com.attendance.app.domain.EmployeeAttendanceStats?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showEditDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(employeeId) {
        scope.launch {
            employee = employeeRepo.getEmployeeById(employeeId)
            attendanceRecords = attendanceRepo.getAttendanceByEmployee(employeeId)
            stats = attendanceRepo.getEmployeeStats(employeeId)
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(strokeWidth = 3.dp)
        }
        return
    }

    employee?.let { emp ->
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            SelectionContainer {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.background)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 48.dp, vertical = 32.dp)
                ) {
                    // Top Bar / Navigation Back
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { navigationState.navigateTo(Screen.Employees) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(20.dp))
                            Column {
                                Text(
                                    text = "Employee Profile",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Employee ID: ${emp.employeeCode ?: "Not Assigned"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SecondaryButton(
                                text = "Open Sheet",
                                onClick = {
                                    if (!emp.googleSheetLink.isNullOrBlank()) {
                                        try {
                                            java.awt.Desktop.getDesktop().browse(java.net.URI(emp.googleSheetLink))
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                },
                                icon = Icons.Default.OpenInNew
                            )
                            PrimaryButton(
                                text = "Edit Profile",
                                onClick = { showEditDialog = true },
                                icon = Icons.Default.Edit
                            )
                        }
                    }

                    if (showEditDialog) {
                        EmployeeFormDialog(
                            employeeToEdit = emp,
                            isLoading = isSaving,
                            onDismiss = { if (!isSaving) showEditDialog = false },
                            onSave = { updatedEmp ->
                                scope.launch {
                                    isSaving = true
                                    try {
                                        val success = employeeRepo.updateEmployee(updatedEmp)
                                        if (success) {
                                            employee = employeeRepo.getEmployeeById(employeeId)
                                            snackbarHostState.showSnackbar("Profile updated locally.")
                                            showEditDialog = false
                                        } else {
                                            snackbarHostState.showSnackbar("Failed to update profile.")
                                        }
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Error: ${e.message}")
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Main Content Layout
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(32.dp)) {

                        // LEFT SIDE: Identification & Details
                        Column(modifier = Modifier.weight(1.8f), verticalArrangement = Arrangement.spacedBy(32.dp)) {

                            // Profile Ident Card
                            SaaSCard(padding = 32.dp) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        modifier = Modifier.size(96.dp),
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                emp.name.take(1).uppercase(),
                                                style = MaterialTheme.typography.displayMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(32.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                emp.name,
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            StatusBadge(emp.onboardingStatus)
                                            VerticalDivider(
                                                modifier = Modifier.height(16.dp),
                                                color = MaterialTheme.colorScheme.outlineVariant
                                            )
                                            Icon(
                                                Icons.Default.Schedule,
                                                null,
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                "Updated ${emp.updatedAt.toLocalDate()}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(40.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(modifier = Modifier.height(32.dp))

                                Text(
                                    "Personal Ecosystem",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(24.dp))

                                // Detailed Fields Grid
                                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        ProfileCell(
                                            Modifier.weight(1f),
                                            "Primary Email",
                                            emp.email ?: "Not specified",
                                            Icons.Default.Email
                                        )
                                        ProfileCell(
                                            Modifier.weight(1f),
                                            "WhatsApp Line",
                                            emp.whatsappNumber ?: "Not specified",
                                            Icons.Default.Phone
                                        )
                                    }
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        ProfileCell(
                                            Modifier.weight(1f),
                                            "NIC / Identity",
                                            emp.nicNumber ?: "Not specified",
                                            Icons.Default.Badge
                                        )
                                        ProfileCell(
                                            Modifier.weight(1f),
                                            "Living Address",
                                            emp.address ?: "Not specified",
                                            Icons.Default.Home
                                        )
                                    }
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        ProfileCell(
                                            Modifier.weight(1f),
                                            "Auth Username",
                                            emp.username ?: "No Login Created",
                                            Icons.Default.Person
                                        )
                                        ProfileCell(
                                            Modifier.weight(1f),
                                            "Auth Password",
                                            emp.password ?: "********",
                                            Icons.Default.Lock
                                        )
                                    }
                                    ProfileCell(
                                        Modifier.fillMaxWidth(),
                                        "Asset (Google Sheet)",
                                        emp.googleSheetLink ?: "No secure link attached",
                                        Icons.Default.Storage
                                    )
                                    ProfileCell(
                                        Modifier.fillMaxWidth(),
                                        "Internal Observations",
                                        emp.internalComment ?: "No notes from management",
                                        Icons.Default.Notes
                                    )
                                }
                            }

                            // Attendance History Block
                            Column {
                                Text(
                                    "Attendance History",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                ModernTable(
                                    items = attendanceRecords.take(15),
                                    columns = listOf(
                                        TableColumn("Date & Day", weight = 1.5f) { record ->
                                            Column {
                                                Text(
                                                    record.date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    record.date.format(DateTimeFormatter.ofPattern("EEEE")),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        TableColumn("Status", weight = 1f) { record ->
                                            StatusBadge(record.status)
                                        },
                                        TableColumn("Recordings / Notes", weight = 1.5f) { record ->
                                            if (!record.leaveEmailLink.isNullOrBlank()) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.clickable {
                                                        try {
                                                            java.awt.Desktop.getDesktop().browse(java.net.URI(record.leaveEmailLink))
                                                        } catch (e: Exception) {
                                                            e.printStackTrace()
                                                        }
                                                    }.padding(vertical = 4.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Attachment,
                                                        null,
                                                        modifier = Modifier.size(16.dp),
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        "View Leave Email",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            } else {
                                                Text(
                                                    record.note ?: "-",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    ),
                                    modifier = Modifier.fillMaxWidth().height(400.dp)
                                )

                                if (attendanceRecords.size > 15) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    SecondaryButton(
                                        text = "View All History",
                                        onClick = { /* Full history screen */ },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        // RIGHT SIDE: Stats & Rapid Breakdown
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(32.dp)) {

                            // Reliability Card
                            SaaSCard(color = MaterialTheme.colorScheme.primary, padding = 40.dp, showBorder = false) {
                                Text(
                                    "Presence Efficiency",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "${stats?.attendanceRate?.toInt() ?: 0}%",
                                    style = MaterialTheme.typography.displayLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(32.dp))
                                LinearProgressIndicator(
                                    progress = (stats?.attendanceRate ?: 0.0).toFloat() / 100f,
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                    color = Color.White,
                                    trackColor = Color.White.copy(alpha = 0.2f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Based on ${stats?.totalPresent ?: 0} present days total",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }

                            // Count Breakdown
                            SaaSCard {
                                Text(
                                    "Career Summary",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(24.dp))

                                MetricRow("Presents Recorded", stats?.totalPresent?.toString() ?: "0", color_status_present)
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                MetricRow("Absents Recorded", stats?.totalAbsent?.toString() ?: "0", color_status_absent)
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                MetricRow("Leave Approved", stats?.totalLeave?.toString() ?: "0", color_status_leave)
                            }

                            // Quick Meta Data
                            SaaSCard(padding = 24.dp) {
                                Text(
                                    "Lifecycle Meta",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                MetaRow(
                                    "Registered",
                                    emp.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                                )
                                MetaRow(
                                    "Last Update",
                                    emp.updatedAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileCell(modifier: Modifier = Modifier, label: String, value: String, icon: ImageVector) {
    Row(modifier = modifier, verticalAlignment = Alignment.Top) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StatHighlight(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun ProfileField(label: String, value: String, icon: ImageVector) {
    Row(modifier = Modifier.padding(bottom = 16.dp), verticalAlignment = Alignment.Top) {
        Icon(
            icon,
            null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp).padding(top = 2.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

data class EmployeeAnalytics(
    val totalDays: Int = 0,
    val presents: Int = 0,
    val absents: Int = 0,
    val leaves: Int = 0,
    val attendancePercentage: Int = 0
)