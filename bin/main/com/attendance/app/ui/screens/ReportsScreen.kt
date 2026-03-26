package com.attendance.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.attendance.app.domain.AttendanceRecord
import com.attendance.app.domain.Employee
import com.attendance.app.repository.AttendanceRepositoryImpl
import com.attendance.app.repository.EmployeeRepositoryImpl
import com.attendance.app.ui.components.*
import com.attendance.app.ui.navigation.NavigationState
import com.attendance.app.ui.theme.*
import com.attendance.app.service.ExcelExportService
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.io.File

@Composable
fun ReportsScreen(navigationState: NavigationState) {
    val employeeRepo = remember { EmployeeRepositoryImpl() }
    val attendanceRepo = remember { AttendanceRepositoryImpl() }
    val scope = rememberCoroutineScope()
    
    var employees by remember { mutableStateOf<List<Employee>>(emptyList()) }
    var allRecordsInRange by remember { mutableStateOf<List<AttendanceRecord>>(emptyList()) }
    var exportMessage by remember { mutableStateOf("") }
    
    var startDate by remember { mutableStateOf(LocalDate.now().minusDays(30)) }
    var endDate by remember { mutableStateOf(LocalDate.now()) }
    var searchQuery by remember { mutableStateOf("") }
    
    fun loadData() {
        scope.launch {
            employees = employeeRepo.getAllEmployees()
            allRecordsInRange = attendanceRepo.getAttendanceSummary(startDate, endDate)
        }
    }

    LaunchedEffect(startDate, endDate) {
        loadData()
    }

    val excelExportService = remember { ExcelExportService(employeeRepo, attendanceRepo) }

    fun exportGlobalExcel() {
        scope.launch {
            try {
                val dialog = java.awt.FileDialog(null as java.awt.Frame?, "Save Attendance Report", java.awt.FileDialog.SAVE)
                dialog.file = "Attendance_Report_Exported_${LocalDate.now()}.xlsx"
                dialog.isVisible = true
                
                val directory = dialog.directory
                val fileName = dialog.file
                
                if (directory != null && fileName != null) {
                    val fullPath = File(directory, fileName).absolutePath
                    val result = excelExportService.exportAttendanceToExcel(fullPath)
                    result.onSuccess { path ->
                        exportMessage = "Report exported to $path"
                    }.onFailure { e ->
                        exportMessage = "Export failed: ${e.message}"
                    }
                }
            } catch (e: Exception) {
                exportMessage = "Export failed: ${e.message}"
            }
        }
    }

    val filteredEmployees = remember(employees, searchQuery) {
        employees.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }
    
    Column(modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState())) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Staff AT Reports",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "View and export attendance summaries across the organization",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            PrimaryButton(
                text = "Export to Excel",
                icon = Icons.Default.FileDownload,
                onClick = { exportGlobalExcel() }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth().height(300.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            SaaSCard(modifier = Modifier.weight(1.5f)) {
                Column {
                    Text("Report Parameters", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        DateSelector("Start Date", startDate) { startDate = it }
                        DateSelector("End Date", endDate) { endDate = it }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    SaaSOutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = "Search by employee name...",
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, null) }
                    )
                }
            }
            
            SaaSCard(modifier = Modifier.weight(1f)) {
                Column {
                    Text("Range Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val total = allRecordsInRange.size
                    val present = allRecordsInRange.count { it.status == "Present" }
                    val absent = allRecordsInRange.count { it.status == "Absent" }
                    val leave = allRecordsInRange.count { it.status == "Leave" }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatusTile("Present", present.toString(), color_status_present, Modifier.weight(1f))
                        StatusTile("Absent", absent.toString(), color_status_absent, Modifier.weight(1f))
                        StatusTile("Leave", leave.toString(), color_status_leave, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Total Records: $total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        if (exportMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            SaaSCard(color = color_status_present.copy(alpha = 0.05f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = color_status_present, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(exportMessage, color = color_status_present, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { exportMessage = "" }) { Icon(Icons.Default.Close, null, modifier = Modifier.size(20.dp)) }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Detailed Summary", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(16.dp))

        SaaSCard(padding = 0.dp) {
            ModernTable(
                items = filteredEmployees,
                modifier = Modifier.fillMaxWidth().heightIn(min = 400.dp, max = 800.dp),
                columns = listOf(
                    TableColumn("Employee Name", weight = 2f) { emp ->
                        Text(emp.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    },
                    TableColumn("Present", weight = 1f) { emp ->
                        val count = allRecordsInRange.count { it.employeeId == emp.id && it.status == "Present" }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(count.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color_status_present)
                            Text("days", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    TableColumn("Absent", weight = 1f) { emp ->
                        val count = allRecordsInRange.count { it.employeeId == emp.id && it.status == "Absent" }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(count.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color_status_absent)
                            Text("days", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    TableColumn("Leave", weight = 1f) { emp ->
                        val count = allRecordsInRange.count { it.employeeId == emp.id && it.status == "Leave" }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(count.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color_status_leave)
                            Text("days", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    TableColumn("Perf Score", weight = 1.2f) { emp ->
                        val total = allRecordsInRange.count { it.employeeId == emp.id }
                        val presents = allRecordsInRange.count { it.employeeId == emp.id && it.status == "Present" }
                        val percent = if (total > 0) (presents * 100) / total else 0
                        val scoreLabel = if (percent >= 90) "Premium" else if (percent >= 75) "Standard" else "Low"
                        val scoreColor = if (percent >= 90) color_status_present else if (percent >= 75) chart_color_2 else color_status_absent
                        ColoredBadge(scoreLabel, scoreColor)
                    }
                )
            )
        }
    }
}

@Composable
private fun DateSelector(label: String, date: LocalDate, onDateChange: (LocalDate) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onDateChange(date.minusDays(1)) }) { Icon(Icons.Default.ChevronLeft, null) }
            Text(date.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = { onDateChange(date.plusDays(1)) }) { Icon(Icons.Default.ChevronRight, null) }
        }
    }
}
