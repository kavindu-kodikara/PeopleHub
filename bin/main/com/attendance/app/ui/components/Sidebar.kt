package com.attendance.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.attendance.app.ui.navigation.NavigationState
import com.attendance.app.ui.navigation.Screen

@Composable
fun Sidebar(currentScreen: Screen, navigationState: NavigationState) {
    Surface(
        modifier = Modifier.fillMaxHeight().width(280.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            // App Logo / Identity
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 40.dp, start = 8.dp)
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource("icon.png"),
                    contentDescription = "Staff AT Logo",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    "Staff AT",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Items
            SidebarItem("Dashboard", Icons.Default.Dashboard, currentScreen == Screen.Dashboard) {
                navigationState.navigateTo(Screen.Dashboard)
            }
            SidebarItem("Employees", Icons.Default.People, currentScreen == Screen.Employees) {
                navigationState.navigateTo(Screen.Employees)
            }
            SidebarItem("Daily Attendance", Icons.Default.AssignmentTurnedIn, currentScreen == Screen.Attendance) {
                navigationState.navigateTo(Screen.Attendance)
            }
            SidebarItem("Reports", Icons.Default.Assessment, currentScreen == Screen.Reports) {
                navigationState.navigateTo(Screen.Reports)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer Item
            SidebarItem("Sign Out", Icons.Default.Logout, false) {
                /* Sign out */
            }
        }
    }
}

@Composable
private fun SidebarItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp).padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}
