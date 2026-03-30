package com.attendance.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.attendance.app.domain.ActivityType
import com.attendance.app.domain.DashboardActivityItem
import com.attendance.app.ui.theme.*
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import com.attendance.app.domain.WeeklyRecruitmentSummary
import com.attendance.app.domain.EmployeeTrendData
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import org.jetbrains.skia.Paint
import org.jetbrains.skia.TextLine
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.Font
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border

@Composable
fun KPICard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    SaaSCard(
        modifier = modifier,
        padding = 20.dp,
        elevation = 2.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.8f))
                }
            }
        }
    }
}

@Composable
fun AttendanceOverviewBlock(
    present: Int,
    absent: Int,
    leave: Int,
    modifier: Modifier = Modifier
) {
    val total = (present + absent + leave).coerceAtLeast(1)
    val presentRate = (present.toFloat() / total)
    
    SaaSCard(modifier = modifier, padding = 20.dp) {
        Column {
            Text("Attendance Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatusTile("Present", present.toString(), color_status_present, Modifier.weight(1f))
                StatusTile("Absent", absent.toString(), color_status_absent, Modifier.weight(1f))
                StatusTile("Leave", leave.toString(), color_status_leave, Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Daily Presence Rate", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = presentRate,
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                color = color_status_present,
                trackColor = color_status_present.copy(alpha = 0.1f)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${(presentRate * 100).toInt()}% Attendance", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text("$present / $total Employees", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun StatusTile(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(color.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun RecentActivityPanel(
    activities: List<DashboardActivityItem>,
    modifier: Modifier = Modifier
) {
    SaaSCard(modifier = modifier, padding = 20.dp) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Recent Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            if (activities.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No recent activity", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    activities.take(6).forEach { activity ->
                        ActivityItemRow(activity)
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityItemRow(activity: DashboardActivityItem) {
    Row(verticalAlignment = Alignment.Top) {
        val icon = when (activity.type) {
            ActivityType.ATTENDANCE -> Icons.Default.CheckCircle
            ActivityType.EMPLOYEE -> Icons.Default.PersonAdd
            ActivityType.SYSTEM -> Icons.Default.Settings
        }
        val color = when (activity.type) {
            ActivityType.ATTENDANCE -> color_status_present
            ActivityType.EMPLOYEE -> chart_color_1
            ActivityType.SYSTEM -> Color.Gray
        }
        
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(activity.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(activity.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                activity.time.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun QuickActionGrid(
    onAddEmployee: () -> Unit,
    onMarkAttendance: () -> Unit,
    onViewReports: () -> Unit,
    onExportCSV: () -> Unit,
    modifier: Modifier = Modifier
) {
    SaaSCard(modifier = modifier, padding = 20.dp) {
        Column {
            Text("Quick Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionCard("Add Employee", Icons.Default.PersonAdd, chart_color_1, Modifier.weight(1f), onAddEmployee)
                ActionCard("Attendance", Icons.Default.HowToReg, color_status_present, Modifier.weight(1f), onMarkAttendance)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionCard("Analytics", Icons.Default.BarChart, chart_color_2, Modifier.weight(1f), onViewReports)
                ActionCard("Export CSV", Icons.Default.FileDownload, chart_color_3, Modifier.weight(1f), onExportCSV)
            }
        }
    }
}

@Composable
fun ActionCard(label: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.05f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OnboardingPanel(
    pendingEmployees: List<com.attendance.app.domain.Employee>,
    modifier: Modifier = Modifier
) {
    SaaSCard(modifier = modifier, padding = 20.dp) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PendingActions, null, tint = chart_color_5, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pending Onboarding", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                ColoredBadge(pendingEmployees.size.toString(), chart_color_5)
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            if (pendingEmployees.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text("All employees signed in!", color = color_status_present, fontWeight = FontWeight.Medium)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    pendingEmployees.take(5).forEach { employee ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(8.dp).background(chart_color_5, CircleShape))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(employee.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.weight(1f))
                            Text("Pending", style = MaterialTheme.typography.labelSmall, color = chart_color_5)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodayAttendanceList(
    attendanceDetails: List<Pair<com.attendance.app.domain.Employee, String?>>,
    onViewProfile: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    SaaSCard(modifier = modifier, padding = 20.dp) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Today, null, tint = color_status_present, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Today's Presence", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            if (attendanceDetails.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text("No employees registered", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    attendanceDetails.take(10).forEach { (employee, status) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(paddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(employee.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Text(employee.email ?: "No email", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            
                            val (statusLabel, statusColor) = when(status) {
                                "Present" -> "Present" to color_status_present
                                "Absent" -> "Absent" to color_status_absent
                                "Leave" -> "Leave" to color_status_leave
                                else -> "Not Marked" to Color.Gray
                            }
                            
                            ColoredBadge(statusLabel, statusColor)
                            
                            IconButton(onClick = { onViewProfile(employee.id) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlySummaryPanel(
    monthStats: com.attendance.app.domain.MonthlyStats,
    modifier: Modifier = Modifier
) {
    SaaSCard(modifier = modifier, padding = 20.dp) {
        Column {
            Text("${monthStats.monthName} Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryMetric("Total Present", monthStats.totalPresent.toString(), color_status_present)
                SummaryMetric("Total Absent", monthStats.totalAbsent.toString(), color_status_absent)
                SummaryMetric("Total Leave", monthStats.totalLeave.toString(), color_status_leave)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Monthly Efficiency", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${monthStats.attendanceRate.toInt()}%", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Text("Overall Attendance Rate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun SummaryMetric(label: String, value: String, color: Color) {
    Column {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ColoredBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(100.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun WeeklyRecruitmentSummaryHeader(
    summary: WeeklyRecruitmentSummary,
    modifier: Modifier = Modifier
) {
    SaaSCard(modifier = modifier, padding = 20.dp) {
        Column {
            Text("Weekly Recruitment Performance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("First Confirmations", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(summary.totalFirstConfirmations.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = chart_color_1)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Registered", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(summary.totalRegistered.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = chart_color_2)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Conversion Rate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(String.format("%.1f%%", summary.conversionRate), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color_status_present)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun TabbedEmployeeRecruitmentChart(
    employeeTrends: List<com.attendance.app.domain.EmployeeTrendData>,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("FC" to "First Confirmation", "RS" to "Registered Student")
    val scrollState = rememberScrollState()
    var hoveredPoint by remember { mutableStateOf<Pair<Int, Int>?>(null) } // empIndex, dayIndex
    
    SaaSCard(modifier = modifier, padding = 0.dp) {
        Column {
            // Header with TabRow
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrendingUp, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Recruitment Analytics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {},
                    indicator = { tabPositions ->
                        secondaryTabIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab])
                        )
                    },
                    modifier = Modifier.width(180.dp) // Narrower for short names
                ) {
                    tabs.forEachIndexed { index, pair ->
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                            tooltip = {
                                PlainTooltip {
                                    Text(pair.second)
                                }
                            },
                            state = rememberTooltipState()
                        ) {
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { 
                                    Text(
                                        pair.first, 
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                    ) 
                                }
                            )
                        }
                    }
                }
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            
            if (employeeTrends.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    Text("No recruitment activity recorded for current period", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Spacer(modifier = Modifier.height(24.dp))
                
                Box(modifier = Modifier.fillMaxWidth().height(300.dp).horizontalScroll(scrollState).padding(horizontal = 20.dp)) {
                    // Calculate consistent width based on dates
                    val allDates = employeeTrends.firstOrNull()?.dailyCounts?.map { it.date } ?: emptyList()
                    val chartWidth = (allDates.size * 100).dp.coerceAtLeast(800.dp)
                    
                    Canvas(
                        modifier = Modifier
                            .width(chartWidth)
                            .fillMaxHeight()
                            .padding(bottom = 40.dp, top = 20.dp, start = 40.dp, end = 40.dp)
                            .onPointerEvent(PointerEventType.Move) { event ->
                                val pos = event.changes.first().position
                                val maxVal = employeeTrends.flatMap { it.dailyCounts }.maxOfOrNull { 
                                    if (selectedTab == 0) it.confirmations else it.registrations 
                                }?.coerceAtLeast(10) ?: 10
                                
                                val spacePerDay = size.width / (allDates.size - 1).coerceAtLeast(1)
                                val heightScale = size.height / maxVal
                                
                                var found = false
                                for (eIdx in employeeTrends.indices) {
                                    val emp = employeeTrends[eIdx]
                                    for (dIdx in emp.dailyCounts.indices) {
                                        val x = dIdx * spacePerDay
                                        val count = if (selectedTab == 0) emp.dailyCounts[dIdx].confirmations else emp.dailyCounts[dIdx].registrations
                                        val y = size.height - (count * heightScale)
                                        
                                        val dx = pos.x - x
                                        val dy = pos.y - y
                                        if (dx * dx + dy * dy < 30 * 30) { // Increased hit radius for better sensitivity
                                            hoveredPoint = eIdx to dIdx
                                            found = true
                                            break
                                        }
                                    }
                                    if (found) break
                                }
                                if (!found) hoveredPoint = null
                            }
                            .onPointerEvent(PointerEventType.Exit) {
                                hoveredPoint = null
                            }
                    ) {
                        // Find max value in current tab
                        val maxVal = employeeTrends.flatMap { it.dailyCounts }.maxOfOrNull { 
                            if (selectedTab == 0) it.confirmations else it.registrations 
                        }?.coerceAtLeast(10) ?: 10
                        
                        val spacePerDay = size.width / (allDates.size - 1).coerceAtLeast(1)
                        val heightScale = size.height / maxVal
                        
                        val textFont = Font(Typeface.makeDefault(), 11f)
                        val textPaint = Paint().apply { color = 0xFF808080.toInt() }
                        val tooltipPaint = Paint().apply { color = 0xFFFFFFFF.toInt() }
                        val tooltipBgPaint = Paint().apply { color = 0xFF333333.toInt() }
                        
                        var hoveredCoords: androidx.compose.ui.geometry.Offset? = null
                        var hoveredValue: Int = 0
                        var hoveredColor: Color = Color.Gray

                        // Draw Y-axis labels and grid lines
                        for (i in 0..4) {
                            val y = size.height - (i * (size.height / 4f))
                            val value = (i * (maxVal / 4f)).toInt()
                            
                            // Grid line
                            drawLine(Color.LightGray.copy(alpha = 0.2f), androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y))
                            
                            // Y-axis label
                            drawContext.canvas.nativeCanvas.drawTextLine(
                                TextLine.make(value.toString(), textFont),
                                -35f, // Positioned in the left padding area
                                y + 5f,
                                textPaint
                            )
                        }
                        
                        employeeTrends.forEachIndexed { empIndex, employee ->
                            val colorHex = employee.color ?: "#4285F4"
                            val lineColor = Color(java.lang.Long.parseLong(colorHex.removePrefix("#"), 16) or 0xFF000000L)
                            val trendPath = Path()
                            
                            employee.dailyCounts.forEachIndexed { index, dayCount ->
                                val x = index * spacePerDay
                                val count = if (selectedTab == 0) dayCount.confirmations else dayCount.registrations
                                val y = size.height - (count * heightScale)
                                val currentPoint = androidx.compose.ui.geometry.Offset(x, y)
                                
                                if (index == 0) {
                                    trendPath.moveTo(x, y)
                                } else {
                                    trendPath.lineTo(x, y)
                                }
                                
                                // Draw points
                                drawCircle(color = lineColor, radius = 4f, center = currentPoint)
                                
                                // Tooltip detection
                                hoveredPoint?.let { (hEmpIdx, hDayIdx) ->
                                    if (hEmpIdx == empIndex && hDayIdx == index) {
                                        hoveredCoords = currentPoint
                                        hoveredValue = count
                                        hoveredColor = lineColor
                                    }
                                }

                                // Draw X-axis labels only for the first employee to avoid overlap
                                if (employee == employeeTrends.first()) {
                                    drawContext.canvas.nativeCanvas.drawTextLine(
                                        TextLine.make(dayCount.date.format(DateTimeFormatter.ofPattern("dd MMM")), textFont),
                                        x - 20f,
                                        size.height + 30f,
                                        textPaint
                                    )
                                }
                            }
                            
                            drawPath(path = trendPath, color = lineColor, style = Stroke(width = 3f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
                        }

                        // Draw the tooltip if active
                        hoveredCoords?.let { coords ->
                            drawCircle(color = hoveredColor, radius = 8f, center = coords, style = Stroke(width = 2f))
                            drawCircle(color = Color.White, radius = 4f, center = coords)
                            
                            val toolTipText = hoveredValue.toString()
                            val textLine = TextLine.make(toolTipText, Font(Typeface.makeDefault(), 12f))
                            val rectWidth = textLine.width + 16f
                            val rectHeight = 24f
                            
                            drawRoundRect(
                                color = Color(0xFF333333),
                                topLeft = androidx.compose.ui.geometry.Offset(coords.x - rectWidth / 2, coords.y - 40f),
                                size = androidx.compose.ui.geometry.Size(rectWidth, rectHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                            )
                            
                            drawContext.canvas.nativeCanvas.drawTextLine(
                                textLine,
                                coords.x - textLine.width / 2,
                                coords.y - 24f,
                                tooltipPaint
                            )
                        }
                    }
                }
                
                // Legend - Wrapped Flow-like for many employees
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(top = 16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Employees: ", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            employeeTrends.forEach { emp ->
                                val colorHex = emp.color ?: "#4285F4"
                                LegendItem(emp.employeeName, Color(java.lang.Long.parseLong(colorHex.removePrefix("#"), 16) or 0xFF000000L))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun secondaryTabIndicator(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    Box(
        modifier
            .fillMaxWidth()
            .height(3.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
            .background(color)
    )
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape).border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
    }
}
