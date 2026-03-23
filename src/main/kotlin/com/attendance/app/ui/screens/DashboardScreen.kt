package com.attendance.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.attendance.app.repository.AttendanceRepositoryImpl
import com.attendance.app.repository.EmployeeRepositoryImpl
import com.attendance.app.ui.components.*
import com.attendance.app.ui.navigation.NavigationState
import com.attendance.app.ui.navigation.Screen
import com.attendance.app.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.attendance.app.data.DatabaseBackupService
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import kotlin.system.exitProcess
@Composable
fun DashboardScreen(navigationState: NavigationState) {
    val employeeRepo = remember { EmployeeRepositoryImpl() }
    val attendanceRepo = remember { AttendanceRepositoryImpl() }
    val scope = rememberCoroutineScope()
    
    var totalEmployees by remember { mutableStateOf(0) }
    var todayStats by remember { mutableStateOf(Triple(0, 0, 0)) } // Present, Absent, Leave
    var pendingOnboarding by remember { mutableStateOf<List<com.attendance.app.domain.Employee>>(emptyList()) }
    var recentActivity by remember { mutableStateOf<List<com.attendance.app.domain.DashboardActivityItem>>(emptyList()) }
    var monthlyStats by remember { mutableStateOf<com.attendance.app.domain.MonthlyStats?>(null) }
    var todayAttendanceDetails by remember { mutableStateOf<List<Pair<com.attendance.app.domain.Employee, String?>>>(emptyList()) }
    var attendanceRateToday by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }
    
    var showImportConfirmDialog by remember { mutableStateOf<File?>(null) }
    var showImportSuccessDialog by remember { mutableStateOf(false) }
    var showMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        scope.launch {
            val today = LocalDate.now()
            val employees = employeeRepo.getAllEmployees()
            totalEmployees = employees.size
            
            val present = attendanceRepo.getStatusCountByDate(today, "Present").toInt()
            val absent = attendanceRepo.getStatusCountByDate(today, "Absent").toInt()
            val leave = attendanceRepo.getStatusCountByDate(today, "Leave").toInt()
            todayStats = Triple(present, absent, leave)
            
            attendanceRateToday = if (present + absent > 0) (present.toDouble() / (present + absent).toDouble()) * 100.0 else 0.0
            
            pendingOnboarding = employeeRepo.getEmployeesByOnboardingStatus("pending_office_signing")
            recentActivity = attendanceRepo.getRecentActivity(10)
            monthlyStats = attendanceRepo.getMonthlyStats(today.monthValue, today.year)
            
            // Get today's attendance for list
            val todayAttendance = attendanceRepo.getAttendanceByDate(today)
            todayAttendanceDetails = employees.map { emp ->
                val record = todayAttendance.find { it.employeeId == emp.id }
                emp to record?.status
            }
            
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 24.dp).verticalScroll(rememberScrollState())) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Staff AT Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Welcome back! Here's what's happening today.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { navigationState.navigateTo(Screen.Employees) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = chart_color_1)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("New Employee")
                }
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Spacer(modifier = Modifier.height(24.dp))

            // 1. KPI Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                KPICard(
                    title = "Total Scale",
                    value = totalEmployees.toString(),
                    icon = Icons.Default.Groups,
                    color = chart_color_1,
                    modifier = Modifier.weight(1f),
                    subtitle = "Registered staff"
                )
                KPICard(
                    title = "Present Today",
                    value = todayStats.first.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = color_status_present,
                    modifier = Modifier.weight(1f),
                    subtitle = "Currently active"
                )
                KPICard(
                    title = "Absent Today",
                    value = todayStats.second.toString(),
                    icon = Icons.Default.Error,
                    color = color_status_absent,
                    modifier = Modifier.weight(1f),
                    subtitle = "Missing marks"
                )
                KPICard(
                    title = "Success Rate",
                    value = "${attendanceRateToday.toInt()}%",
                    icon = Icons.Default.TrendingUp,
                    color = color_status_present,
                    modifier = Modifier.weight(1f),
                    subtitle = "Daily targets"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Main Content Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left Column (2/3 width)
                Column(modifier = Modifier.weight(2f), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    // Attendance Overview
                    AttendanceOverviewBlock(
                        present = todayStats.first,
                        absent = todayStats.second,
                        leave = todayStats.third
                    )
                    
                    // Today's Attendance List
                    TodayAttendanceList(
                        attendanceDetails = todayAttendanceDetails,
                        onViewProfile = { id -> navigationState.navigateTo(Screen.EmployeeProfile, id) }
                    )
                }
                
                // Right Column (1/3 width)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    // Quick Actions
                    QuickActionGrid(
                        onAddEmployee = { navigationState.navigateTo(Screen.Employees) },
                        onMarkAttendance = { navigationState.navigateTo(Screen.Attendance) },
                        onViewReports = { navigationState.navigateTo(Screen.Reports) },
                        onExportCSV = { /* Export */ }
                    )
                    
                    // Monthly Summary
                    monthlyStats?.let {
                        MonthlySummaryPanel(it)
                    }
                    
                    // Onboarding
                    OnboardingPanel(
                        pendingEmployees = pendingOnboarding
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Bottom Row: Recent Activity & Alerts
            Row(
                modifier = Modifier.fillMaxWidth().height(400.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                RecentActivityPanel(
                    activities = recentActivity,
                    modifier = Modifier.weight(1f)
                )
                
                SaaSCard(modifier = Modifier.weight(1f), padding = 20.dp) {
                    Column {
                        Text("Smart Insights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        DashboardAlert(
                            message = "${pendingOnboarding.size} employees haven't signed into office yet.",
                            color = chart_color_5,
                            icon = Icons.Default.Warning
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        DashboardAlert(
                            message = "Monthly attendance rate is ${monthlyStats?.attendanceRate?.toInt() ?: 0}%.",
                            color = if ((monthlyStats?.attendanceRate ?: 0.0) > 80) color_status_present else color_status_absent,
                            icon = Icons.Default.Info
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Admin & Database Tools
            SaaSCard(modifier = Modifier.fillMaxWidth(), padding = 24.dp) {
                Column {
                    Text("System Administration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Manage your application data and settings securely.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    showMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        PrimaryButton(text = "Export Database", icon = Icons.Default.Download, onClick = {
                            val dialog = FileDialog(null as Frame?, "Export Backup", FileDialog.SAVE)
                            dialog.file = DatabaseBackupService.getDefaultExportFileName()
                            dialog.isVisible = true
                            
                            if (dialog.directory != null && dialog.file != null) {
                                val file = File(dialog.directory, dialog.file)
                                val success = DatabaseBackupService.exportDatabase(file)
                                showMessage = if (success) "Successfully exported database to ${file.name}" else "Failed to export database."
                            }
                        })
                        
                        SecondaryButton(text = "Import Database", icon = Icons.Default.Upload, onClick = {
                            val dialog = FileDialog(null as Frame?, "Restore Backup", FileDialog.LOAD)
                            dialog.isVisible = true
                            if (dialog.directory != null && dialog.file != null) {
                                val file = File(dialog.directory, dialog.file)
                                if (file.extension.equals("db", ignoreCase = true)) {
                                    showImportConfirmDialog = file
                                } else {
                                    showMessage = "Invalid file format. Please select a .db backup file."
                                }
                            }
                        })
                        
                        SecondaryButton(text = "Application Settings", icon = Icons.Default.Settings, onClick = {
                            navigationState.navigateTo(Screen.Settings)
                        })
                    }
                }
            }

            if (showImportConfirmDialog != null) {
                AlertDialog(
                    onDismissRequest = { showImportConfirmDialog = null },
                    title = { Text("Restore Database") },
                    text = { Text("Are you sure you want to restore the database from this backup? Your current data will be entirely overwritten. This action cannot be undone.") },
                    confirmButton = {
                        Button(onClick = {
                            val success = DatabaseBackupService.importDatabase(showImportConfirmDialog!!)
                            showImportConfirmDialog = null
                            if (success) {
                                showImportSuccessDialog = true
                            } else {
                                showMessage = "Failed to restore database. Ensure the file is valid and accessible."
                            }
                        }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                            Text("Restore Now", color = MaterialTheme.colorScheme.onError)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showImportConfirmDialog = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (showImportSuccessDialog) {
                AlertDialog(
                    onDismissRequest = { },
                    title = { Text("Database Restored") },
                    text = { Text("The database backup was restored successfully. The application will now close to safely apply the new data. Please restart it manually.") },
                    confirmButton = {
                        Button(onClick = { exitProcess(0) }) {
                            Text("Exit Application")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DashboardAlert(message: String, color: Color, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}
