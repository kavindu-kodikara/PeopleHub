package com.attendance.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.attendance.app.data.AppConfig
import com.attendance.app.ui.components.SaaSCard
import com.attendance.app.ui.navigation.NavigationState
import com.attendance.app.ui.navigation.Screen
import com.attendance.app.ui.theme.chart_color_1

@Composable
fun SettingsScreen(navigationState: NavigationState) {
    var baseUrl by remember { mutableStateOf(AppConfig.apiBaseUrl) }
    var adminToken by remember { mutableStateOf(AppConfig.apiAdminToken) }
    var showMessage by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "System Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Configure application endpoints and security tokens.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Button(
                onClick = { navigationState.navigateTo(Screen.Dashboard) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back to Dashboard", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        SaaSCard(modifier = Modifier.fillMaxWidth(0.7f), padding = 32.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Text(
                    "API Configuration",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "API Base URL",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = { baseUrl = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("https://your-app.vercel.app/") },
                        singleLine = true
                    )
                    Text(
                        "The root URL of your Next.js backend application.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Admin Authorization Token",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    OutlinedTextField(
                        value = adminToken,
                        onValueChange = { adminToken = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter your secret token") },
                        singleLine = true
                    )
                    Text(
                        "This token must match the API_SECRET set in your backend environment variables.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        AppConfig.updateConfig(baseUrl, adminToken)
                        showMessage = "Settings saved successfully!"
                    },
                    modifier = Modifier.align(Alignment.End),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = chart_color_1)
                ) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Changes")
                }
                
                showMessage?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
