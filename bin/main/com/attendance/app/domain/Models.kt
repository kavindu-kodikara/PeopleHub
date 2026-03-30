package com.attendance.app.domain

import java.time.LocalDate
import java.time.LocalDateTime

data class Employee(
    val id: Int = 0,
    val name: String,
    val email: String? = null,
    val whatsappNumber: String? = null,
    val nicNumber: String? = null,
    val address: String? = null,
    val googleSheetLink: String? = null,
    val internalComment: String? = null,
    val employeeCode: String? = null,
    val username: String? = null,
    val password: String? = null,
    val onboardingStatus: String = "pending_office_signing",
    val color: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

data class AttendanceRecord(
    val id: Int = 0,
    val employeeId: Int,
    val date: LocalDate,
    val status: String,
    val leaveEmailLink: String? = null,
    val note: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class AttendanceStatus(val value: String) {
    PRESENT("Present"),
    ABSENT("Absent"),
    LEAVE("Leave"),
    PENDING("Pending")
}

enum class OnboardingStatus(val value: String, val displayName: String) {
    PENDING("pending_office_signing", "Pending Office Signing"),
    SIGNED_IN("signed_in_office", "Signed in Office")
}

data class DashboardActivityItem(
    val title: String,
    val subtitle: String,
    val time: LocalDateTime,
    val type: ActivityType
)

enum class ActivityType {
    ATTENDANCE, EMPLOYEE, SYSTEM
}

data class MonthlyStats(
    val monthName: String,
    val totalPresent: Int,
    val totalAbsent: Int,
    val totalLeave: Int,
    val attendanceRate: Double
)

data class EmployeeAttendanceStats(
    val totalPresent: Int,
    val totalAbsent: Int,
    val totalLeave: Int,
    val attendanceRate: Double
)

data class StudentResponse(
    val id: Int = 0,
    val timestamp: String, // From Excel "Timestamp" column
    val studentName: String,
    val nic: String? = null,
    val address: String? = null,
    val whatsappNumber: String? = null,
    val contactNumber: String? = null,
    val databaseName: String? = null,
    val counselorName: String,
    val employeeId: Int? = null,
    val importDate: LocalDate = LocalDate.now(),
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class RegisteredStudent(
    val id: Int = 0,
    val firstName: String,
    val lastName: String? = null,
    val email: String? = null,
    val nic: String? = null,
    val whatsappNumber: String? = null,
    val contactNumber: String? = null,
    val landlineNumber: String? = null,
    val counselorName: String,
    val timeDuration: Int = 0, // in minutes
    val employeeId: Int? = null,
    val importDate: LocalDate = LocalDate.now(),
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class EmployeeAnalysis(
    val employeeName: String,
    val employeeCode: String?,
    val totalFirstConfirmations: Int,
    val totalRegistered: Int,
    val totalSuccess: Int,
    val conversionRate: Double,
    val successRate: Double
)

data class EmployeeTrendData(
    val employeeId: Int,
    val employeeName: String,
    val color: String?,
    val dailyCounts: List<EmployeeDayCount>
)

data class EmployeeDayCount(
    val date: java.time.LocalDate,
    val confirmations: Int,
    val registrations: Int
)

data class WeeklyRecruitmentSummary(
    val totalFirstConfirmations: Int,
    val totalRegistered: Int,
    val conversionRate: Double
)

