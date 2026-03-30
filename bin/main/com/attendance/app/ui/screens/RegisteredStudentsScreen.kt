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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.selection.SelectionContainer
import com.attendance.app.domain.RegisteredStudent
import com.attendance.app.repository.EmployeeRepositoryImpl
import com.attendance.app.repository.RegisteredStudentRepositoryImpl
import com.attendance.app.repository.StudentResponseRepositoryImpl
import com.attendance.app.service.ExcelImportService
import com.attendance.app.ui.components.*
import com.attendance.app.ui.navigation.NavigationState
import com.attendance.app.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun RegisteredStudentsScreen(navigationState: NavigationState) {
    val scope = rememberCoroutineScope()
    val employeeRepository = remember { EmployeeRepositoryImpl() }
    val studentResponseRepository = remember { StudentResponseRepositoryImpl() }
    val registeredStudentRepository = remember { RegisteredStudentRepositoryImpl() }
    val excelImportService = remember { ExcelImportService(employeeRepository, studentResponseRepository, registeredStudentRepository) }
    
    var selectedDate by remember { mutableStateOf(LocalDate.now().minusDays(1)) }
    var students by remember { mutableStateOf(emptyList<RegisteredStudent>()) }

    var employees by remember { mutableStateOf(emptyList<com.attendance.app.domain.Employee>()) }
    var isLoading by remember { mutableStateOf(true) }
    var importMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importDate by remember { mutableStateOf(LocalDate.now().minusDays(1)) }

    LaunchedEffect(selectedDate) {
        isLoading = true
        students = registeredStudentRepository.getAllByDate(selectedDate)
        employees = employeeRepository.getAllEmployees()
        isLoading = false
    }

    if (showImportDialog) {
        SaaSModal(
            title = "Import Registered Students",
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
                        DateSelector(importDate) { importDate = it }
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
                            val dialog = java.awt.FileDialog(null as java.awt.Frame?, "Select Registered Students Excel", java.awt.FileDialog.LOAD)
                            dialog.file = "*.xlsx"
                            dialog.isVisible = true
                            
                            val directory = dialog.directory
                            val fileName = dialog.file
                            
                            if (directory != null && fileName != null) {
                                val fullPath = java.io.File(directory, fileName).absolutePath
                                scope.launch {
                                    isLoading = true
                                    val importResult = excelImportService.importRegisteredStudents(fullPath, importDate)
                                    importResult.fold(
                                        onSuccess = {
                                            importMessage = "Imported: ${it.imported}, Skipped: ${it.skipped}, Duplicates: ${it.duplicates}"
                                            isError = false
                                            selectedDate = importDate // Switch to the imported date to see results
                                            students = registeredStudentRepository.getAllByDate(selectedDate)
                                        },
                                        onFailure = {
                                            importMessage = "Error: ${it.message}"
                                            isError = true
                                        }
                                    )
                                    isLoading = false
                                }
                                showImportDialog = false
                            }
                        }
                    )
                }
            }
        }
    }

    SelectionContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Registered Students",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Manage and view students who have registered for the degree.",
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

            // Filters and Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SaaSCard(modifier = Modifier.weight(1f)) {
                    DateSelector(
                        selectedDate = selectedDate,
                        onDateSelected = { selectedDate = it }
                    )
                }

                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatusTile(
                        label = "Total Registered",
                        value = students.size.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatusTile(
                        label = "Successful",
                        value = students.count { it.timeDuration >= 30 }.toString(),
                        color = color_status_present,
                        modifier = Modifier.weight(1f)
                    )
                }
            }


            if (importMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = if (isError) MaterialTheme.colorScheme.errorContainer else Color(0xFFE8F5E9),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isError) Icons.Default.Error else Icons.Default.CheckCircle,
                            null,
                            tint = if (isError) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            importMessage!!,
                            color = if (isError) MaterialTheme.colorScheme.onErrorContainer else Color(0xFF1B5E20)
                        )
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { importMessage = null }) {
                            Icon(Icons.Default.Close, null)
                        }
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (students.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No registered students found for this date.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val responsesByEmployee = remember(students) {
                    students.groupBy { it.employeeId }
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
                            val successCount = empResponses.count { it.timeDuration >= 30 }
                            Text(
                                "$successCount / ${empResponses.size} Success", 
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
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (res.timeDuration >= 30) {
                                                Icon(Icons.Default.CheckCircle, "Success", tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(8.dp))
                                            }
                                            Column {
                                                Text("${res.firstName} ${res.lastName ?: ""}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                                Text(res.nic ?: "No NIC", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    },
                                    TableColumn("Contacts", weight = 1.5f) { res ->
                                        Column {
                                            Text(res.email ?: "", style = MaterialTheme.typography.bodySmall)
                                            Text(res.whatsappNumber ?: res.contactNumber ?: "", style = MaterialTheme.typography.bodySmall)
                                        }
                                    },
                                    TableColumn("Duration", weight = 1f) { res ->
                                        Surface(
                                            color = if (res.timeDuration >= 30) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant,
                                            shape = MaterialTheme.shapes.small
                                        ) {
                                            Text(
                                                "${res.timeDuration} min", 
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = if (res.timeDuration >= 30) FontWeight.Bold else FontWeight.Normal,
                                                color = if (res.timeDuration >= 30) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    TableColumn("Import Date", weight = 1.2f) { res ->
                                        Text(res.importDate.format(DateTimeFormatter.ISO_LOCAL_DATE), style = MaterialTheme.typography.bodySmall)
                                    }
                                )
                            )
                        }
                    }
                }

                val unmatchedResponses = students.filter { it.employeeId == null }
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
                                    Text("${res.firstName} ${res.lastName ?: ""}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                },
                                TableColumn("Counselor Original", weight = 1.5f) { res ->
                                    Text(res.counselorName, style = MaterialTheme.typography.bodyMedium)
                                },
                                TableColumn("Duration", weight = 1f) { res ->
                                    Text("${res.timeDuration} min", style = MaterialTheme.typography.bodySmall)
                                }
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DateSelector(selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    Column {
        Text("Select Day", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onDateSelected(selectedDate.minusDays(1)) }) { Icon(Icons.Default.ChevronLeft, null) }
            Text(selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = { onDateSelected(selectedDate.plusDays(1)) }) { Icon(Icons.Default.ChevronRight, null) }
        }
    }
}
