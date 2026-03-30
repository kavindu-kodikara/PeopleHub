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
import com.attendance.app.domain.Employee
import com.attendance.app.domain.StudentResponse
import com.attendance.app.domain.RegisteredStudent
import com.attendance.app.repository.EmployeeRepositoryImpl
import com.attendance.app.repository.StudentResponseRepositoryImpl
import com.attendance.app.repository.RegisteredStudentRepositoryImpl
import com.attendance.app.service.ExcelImportService
import com.attendance.app.ui.components.*
import com.attendance.app.ui.navigation.NavigationState
import com.attendance.app.ui.theme.*
import androidx.compose.foundation.text.selection.SelectionContainer
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

@Composable
fun StudentResponsesScreen(navigationState: NavigationState) {
    val employeeRepo = remember { EmployeeRepositoryImpl() }
    val studentResponseRepo = remember { StudentResponseRepositoryImpl() }
    val registeredStudentRepo = remember { RegisteredStudentRepositoryImpl() }
    val excelImportService = remember { ExcelImportService(employeeRepo, studentResponseRepo, registeredStudentRepo) }
    val scope = rememberCoroutineScope()

    var employees by remember { mutableStateOf<List<Employee>>(emptyList()) }
    var responses by remember { mutableStateOf<List<StudentResponse>>(emptyList()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now().minusDays(1)) }
    var searchQuery by remember { mutableStateOf("") }
    var importStatus by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importDate by remember { mutableStateOf(LocalDate.now().minusDays(1)) }

    fun loadData() {
        scope.launch {
            isLoading = true
            employees = employeeRepo.getAllEmployees()
            responses = studentResponseRepo.getAllByDate(selectedDate)
            isLoading = false
        }
    }

    LaunchedEffect(selectedDate) {
        loadData()
    }

    val filteredResponses = remember(responses, searchQuery) {
        responses.filter { 
            it.studentName.contains(searchQuery, ignoreCase = true) || 
            it.counselorName.contains(searchQuery, ignoreCase = true)
        }
    }

    fun triggerImport(filePath: String, date: LocalDate) {
        scope.launch {
            try {
                isLoading = true
                val result = excelImportService.importStudentResponses(filePath, date)
                result.onSuccess { res ->
                    importStatus = "Import successful: ${res.imported} imported, ${res.duplicates} duplicates, ${res.skipped} skipped"
                    selectedDate = date // Switch to the imported date to see results
                    loadData()
                }.onFailure { e ->
                    importStatus = "Import failed: ${e.message}"
                }
                isLoading = false
            } catch (e: Exception) {
                importStatus = "Error: ${e.message}"
                isLoading = false
            }
        }
    }

    if (showImportDialog) {
        SaaSModal(
            title = "Import Student Responses",
            onDismissRequest = { showImportDialog = false }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Text(
                    "Select the date for which you are importing the Excel data. All records in the sheet will be assigned to this date.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                SaaSCard(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DateSelector("Import Date", importDate) { importDate = it }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SecondaryButton("Cancel", onClick = { showImportDialog = false })
                    Spacer(Modifier.width(12.dp))
                    PrimaryButton(
                        text = "Select Excel File",
                        icon = Icons.Default.FileOpen,
                        onClick = {
                            val dialog = java.awt.FileDialog(null as java.awt.Frame?, "Select Student Response Excel", java.awt.FileDialog.LOAD)
                            dialog.isVisible = true
                            val directory = dialog.directory
                            val fileName = dialog.file
                            
                            if (directory != null && fileName != null) {
                                val fullPath = java.io.File(directory, fileName).absolutePath
                                triggerImport(fullPath, importDate)
                                showImportDialog = false
                            }
                        }
                    )
                }
            }
        }
    }

    SelectionContainer {
        Column(modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState())) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "First Confirmation",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "View and import student enrollment data collected by counselors",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                PrimaryButton(
                    text = "Import Excel",
                    icon = Icons.Default.FileUpload,
                    onClick = { 
                        importDate = selectedDate // Default to current view date
                        showImportDialog = true 
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                SaaSCard(modifier = Modifier.weight(1f)) {
                    Column {
                        Text("Filter Controls", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            DateSelector("Select Day", selectedDate) { selectedDate = it }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        SaaSOutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = "Search student or counselor...",
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Search, null) }
                        )
                    }
                }
                
                SaaSCard(modifier = Modifier.weight(1f)) {
                    Column {
                        Text("Daily Stats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatusTile("Total Responses", responses.size.toString(), chart_color_1, Modifier.weight(1f))
                            StatusTile("Counselors", responses.groupBy { it.counselorName }.size.toString(), chart_color_2, Modifier.weight(1f))
                        }
                    }
                }
            }

            if (importStatus.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                SaaSCard(color = color_status_present.copy(alpha = 0.05f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = color_status_present, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(importStatus, color = color_status_present, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { importStatus = "" }) { Icon(Icons.Default.Close, null, modifier = Modifier.size(20.dp)) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Detailed Records", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
            } else if (filteredResponses.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No student responses found for this date.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            val responsesByEmployee = remember(filteredResponses) {
                filteredResponses.groupBy { it.employeeId }
            }

            val sortedEmployees = remember(employees) {
                employees.sortedBy { it.employeeCode ?: it.name }
            }

            sortedEmployees.forEach { employee ->
                val empResponses = responsesByEmployee[employee.id]
                if (empResponses != null) {
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "${employee.name} (${employee.employeeCode ?: "No ID"})", 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "${empResponses.size} students", 
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    SaaSCard(padding = 0.dp) {
                        ModernTable(
                            items = empResponses,
                            modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp),
                            columns = listOf(
                                TableColumn("Student Name", weight = 2f) { res ->
                                    Column {
                                        Text(res.studentName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                        Text(res.nic ?: "No NIC", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                TableColumn("Contacts", weight = 1.5f) { res ->
                                    Column {
                                        Text(res.whatsappNumber ?: "", style = MaterialTheme.typography.bodySmall)
                                        Text(res.contactNumber ?: "", style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                TableColumn("Database", weight = 1f) { res ->
                                    Text(res.databaseName ?: "-", style = MaterialTheme.typography.bodySmall)
                                },
                                TableColumn("Timestamp", weight = 1.2f) { res ->
                                    Text(res.timestamp, style = MaterialTheme.typography.bodySmall)
                                }
                            )
                        )
                    }
                }
            }

            // Also show records that didn't match any employee (if any)
            val unmatchedResponses = filteredResponses.filter { it.employeeId == null }
            if (unmatchedResponses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Text("Unmatched Records", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
                SaaSCard(padding = 0.dp) {
                    ModernTable(
                        items = unmatchedResponses,
                        modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp),
                        columns = listOf(
                            TableColumn("Student Name", weight = 2f) { res ->
                                Text(res.studentName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            },
                            TableColumn("Counselor Original", weight = 1.5f) { res ->
                                Text(res.counselorName, style = MaterialTheme.typography.bodyMedium)
                            },
                            TableColumn("Contacts", weight = 1.5f) { res ->
                                Text(res.contactNumber ?: "", style = MaterialTheme.typography.bodySmall)
                            }
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun DateSelector(label: String, date: LocalDate, onDateChange: (LocalDate) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onDateChange(date.minusDays(1)) }) { Icon(Icons.Default.ChevronLeft, null) }
            Text(date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = { onDateChange(date.plusDays(1)) }) { Icon(Icons.Default.ChevronRight, null) }
        }
    }
}
