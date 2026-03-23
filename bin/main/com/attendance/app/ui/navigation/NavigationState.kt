package com.attendance.app.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

sealed class Screen(val title: String) {
    object Login : Screen("Login")
    object Dashboard : Screen("Dashboard")
    object Employees : Screen("Employees")
    object EmployeeProfile : Screen("Employee Profile")
    object Attendance : Screen("Attendance")
    object Reports : Screen("Reports")
    object Settings : Screen("System Settings")
}

class NavigationState {
    var currentScreen by mutableStateOf<Screen>(Screen.Login)
    var selectedEmployeeId by mutableStateOf<Int?>(null)
    
    fun navigateTo(screen: Screen, employeeId: Int? = null) {
        currentScreen = screen
        if (employeeId != null) {
            selectedEmployeeId = employeeId
        } else if (screen !is Screen.EmployeeProfile) {
            selectedEmployeeId = null
        }
    }
}
