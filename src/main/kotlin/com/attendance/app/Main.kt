package com.attendance.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.runtime.remember
import com.attendance.app.data.DatabaseFactory
import com.attendance.app.ui.components.AppShell
import com.attendance.app.ui.navigation.NavigationState
import com.attendance.app.ui.navigation.Screen
import com.attendance.app.ui.screens.*
import com.attendance.app.ui.theme.AppTheme
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.res.painterResource

fun main() = application {
    DatabaseFactory.init()

    Window(
        onCloseRequest = ::exitApplication, 
        title = "Staff AT",
        icon = painterResource("icon.png"),
        state = WindowState(
            placement = WindowPlacement.Maximized,
            size = DpSize(1280.dp, 720.dp)
        )
    ) {
        val navigationState = remember { NavigationState() }

        AppTheme {
            AppShell(navigationState = navigationState) {
                when (navigationState.currentScreen) {
                    is Screen.Login -> LoginScreen(
                        onLoginSuccess = { navigationState.navigateTo(Screen.Dashboard) }
                    )
                    is Screen.Dashboard -> DashboardScreen(
                        navigationState = navigationState
                    )
                    is Screen.Employees -> EmployeesScreen(
                        navigationState = navigationState
                    )
                    is Screen.EmployeeProfile -> EmployeeProfileScreen(
                        employeeId = navigationState.selectedEmployeeId ?: 0,
                        navigationState = navigationState
                    )
                    is Screen.Attendance -> AttendanceScreen()
                    is Screen.Reports -> ReportsScreen(
                        navigationState = navigationState
                    )
                }
            }
        }
    }
}
