package com.attendance.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WindowScope.CustomTitleBar(
    onClose: () -> Unit,
    onMinimize: () -> Unit,
    onMaximize: () -> Unit
) {
    WindowDraggableArea {
        Surface(
            modifier = Modifier.fillMaxWidth().height(32.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Icon & Title
                Image(
                    painter = painterResource("icon.png"),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Staff AT",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Window Controls
                Row(modifier = Modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
                    // Minimize
                    WindowControlButton(
                        icon = Icons.Default.Remove,
                        onClick = onMinimize
                    )
                    
                    // Maximize / Restore
                    WindowControlButton(
                        icon = Icons.Default.CropSquare,
                        onClick = onMaximize
                    )
                    
                    // Close
                    WindowControlButton(
                        icon = Icons.Default.Close,
                        hoverColor = Color(0xFFE81123),
                        iconHoverColor = Color.White,
                        onClick = onClose
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun WindowControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    hoverColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
    iconHoverColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(46.dp)
            .background(if (isHovered) hoverColor else Color.Transparent)
            .onPointerEvent(PointerEventType.Enter) { isHovered = true }
            .onPointerEvent(PointerEventType.Exit) { isHovered = false }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (isHovered) iconHoverColor else MaterialTheme.colorScheme.onSurface
        )
    }
}
