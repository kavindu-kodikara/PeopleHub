package com.attendance.app.repository

import com.attendance.app.domain.AttendanceRecord
import java.time.LocalDate

interface AttendanceRepository {
    suspend fun getAttendanceByDate(date: LocalDate): List<AttendanceRecord>
    suspend fun getAttendanceByEmployee(employeeId: Int): List<AttendanceRecord>
    suspend fun markAttendance(record: AttendanceRecord): Int
    suspend fun getAttendanceSummary(startDate: LocalDate, endDate: LocalDate): List<AttendanceRecord>
    suspend fun getStatusCountByDate(date: LocalDate, status: String): Long
    suspend fun getDailyTrend(days: Int): Map<LocalDate, Int>
    suspend fun getStatusDistribution(startDate: LocalDate, endDate: LocalDate): Map<String, Int>
    suspend fun getAttendanceRate(startDate: LocalDate, endDate: LocalDate): Double
    suspend fun getRecentActivity(limit: Int): List<com.attendance.app.domain.DashboardActivityItem>
    suspend fun getMonthlyStats(month: Int, year: Int): com.attendance.app.domain.MonthlyStats
    suspend fun getEmployeeStats(employeeId: Int): com.attendance.app.domain.EmployeeAttendanceStats
    suspend fun getEarliestAttendanceDate(): LocalDate?
}
