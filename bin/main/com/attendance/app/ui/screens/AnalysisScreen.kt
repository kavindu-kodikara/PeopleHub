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
import com.attendance.app.domain.EmployeeAnalysis
import com.attendance.app.repository.EmployeeRepositoryImpl
import com.attendance.app.repository.RegisteredStudentRepositoryImpl
import com.attendance.app.repository.StudentResponseRepositoryImpl
import com.attendance.app.service.AnalysisService
import com.attendance.app.ui.components.*
import com.attendance.app.ui.navigation.NavigationState
import com.attendance.app.ui.theme.*
import java.time.LocalDate

@Composable
fun AnalysisScreen(navigationState: NavigationState) {
    val employeeRepository = remember { EmployeeRepositoryImpl() }
    val studentResponseRepository = remember { StudentResponseRepositoryImpl() }
    val registeredStudentRepository = remember { RegisteredStudentRepositoryImpl() }
    val analysisService = remember { AnalysisService(employeeRepository, studentResponseRepository, registeredStudentRepository) }
    
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var analysisData by remember { mutableStateOf(emptyList<EmployeeAnalysis>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(selectedDate) {
        isLoading = true
        analysisData = analysisService.getAnalysisByDate(selectedDate)
        isLoading = false
    }

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
                    "Recruitment Analysis",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Conversion and success rates per employee.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        SaaSCard(modifier = Modifier.width(300.dp)) {
            DateSelector(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it }
            )
        }


        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (analysisData.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Text("No data available for this date.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            SaaSCard(padding = 0.dp) {
                ModernTable(
                    items = analysisData,
                    modifier = Modifier.fillMaxWidth().heightIn(max = 1000.dp),
                    columns = listOf(
                        TableColumn("Counselor", weight = 2f) { analysis ->
                            Column {
                                Text(analysis.employeeName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                Text(analysis.employeeCode ?: "-", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        TableColumn("Calls (F.C)", weight = 1f) { analysis ->
                            Text(analysis.totalFirstConfirmations.toString(), style = MaterialTheme.typography.bodyMedium)
                        },
                        TableColumn("Registered", weight = 1f) { analysis ->
                            Text(analysis.totalRegistered.toString(), style = MaterialTheme.typography.bodyMedium)
                        },
                        TableColumn("Success (>=30m)", weight = 1.2f) { analysis ->
                            Text(analysis.totalSuccess.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        },
                        TableColumn("Conversion %", weight = 1.2f) { analysis ->
                            RateIndicator(analysis.conversionRate)
                        },
                        TableColumn("Success %", weight = 1.2f) { analysis ->
                            RateIndicator(analysis.successRate)
                        }
                    )
                )
            }
        }
    }
}

@Composable
fun RateIndicator(rate: Double) {
    val color = when {
        rate >= 70.0 -> Color(0xFF2E7D32)
        rate >= 40.0 -> Color(0xFFFBC02D)
        else -> MaterialTheme.colorScheme.error
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            String.format("%.1f%%", rate),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun DateSelector(selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    Column {
        Text("Select Day", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onDateSelected(selectedDate.minusDays(1)) }) { Icon(Icons.Default.ChevronLeft, null) }
            Text(selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = { onDateSelected(selectedDate.plusDays(1)) }) { Icon(Icons.Default.ChevronRight, null) }
        }
    }
}
