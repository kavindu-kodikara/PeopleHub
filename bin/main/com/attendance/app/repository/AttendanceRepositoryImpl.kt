package com.attendance.app.repository

import com.attendance.app.data.AttendanceTable
import com.attendance.app.domain.AttendanceRecord
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDate

class AttendanceRepositoryImpl : AttendanceRepository {
    private fun ResultRow.toAttendanceRecord() = AttendanceRecord(
        id = this[AttendanceTable.id].value,
        employeeId = this[AttendanceTable.employeeId].value,
        date = this[AttendanceTable.date],
        status = this[AttendanceTable.status],
        leaveEmailLink = this[AttendanceTable.leaveEmailLink],
        note = this[AttendanceTable.note],
        createdAt = this[AttendanceTable.createdAt],
        updatedAt = this[AttendanceTable.updatedAt]
    )

    override suspend fun getAttendanceByDate(date: LocalDate): List<AttendanceRecord> = newSuspendedTransaction {
        AttendanceTable.select { AttendanceTable.date eq date }.map { it.toAttendanceRecord() }
    }

    override suspend fun getAttendanceByEmployee(employeeId: Int): List<AttendanceRecord> = newSuspendedTransaction {
        AttendanceTable.select { AttendanceTable.employeeId eq employeeId }
            .orderBy(AttendanceTable.date to SortOrder.DESC)
            .map { it.toAttendanceRecord() }
    }

    override suspend fun markAttendance(record: AttendanceRecord): Int = newSuspendedTransaction {
        val existingId = AttendanceTable
            .select { (AttendanceTable.employeeId eq record.employeeId) and (AttendanceTable.date eq record.date) }
            .map { it[AttendanceTable.id].value }
            .singleOrNull()

        if (existingId != null) {
            AttendanceTable.update({ AttendanceTable.id eq existingId }) {
                it[status] = record.status
                it[leaveEmailLink] = record.leaveEmailLink
                it[note] = record.note
                it[updatedAt] = java.time.LocalDateTime.now()
            }
            existingId
        } else {
            AttendanceTable.insertAndGetId {
                it[employeeId] = record.employeeId
                it[date] = record.date
                it[status] = record.status
                it[leaveEmailLink] = record.leaveEmailLink
                it[note] = record.note
            }.value
        }
    }

    override suspend fun getAttendanceSummary(startDate: LocalDate, endDate: LocalDate): List<AttendanceRecord> = newSuspendedTransaction {
        (AttendanceTable innerJoin com.attendance.app.data.EmployeesTable)
            .select {
                (AttendanceTable.date greaterEq startDate) and (AttendanceTable.date lessEq endDate)
            }.map { it.toAttendanceRecord() }
    }

    override suspend fun getStatusCountByDate(date: LocalDate, status: String): Long = newSuspendedTransaction {
        AttendanceTable.select { (AttendanceTable.date eq date) and (AttendanceTable.status eq status) }.count()
    }

    override suspend fun getDailyTrend(days: Int): Map<LocalDate, Int> = newSuspendedTransaction {
        val startDate = LocalDate.now().minusDays(days.toLong())
        (AttendanceTable innerJoin com.attendance.app.data.EmployeesTable)
            .slice(AttendanceTable.date, AttendanceTable.status.count())
            .select { (AttendanceTable.date greaterEq startDate) and (AttendanceTable.status eq "Present") }
            .groupBy(AttendanceTable.date)
            .orderBy(AttendanceTable.date to SortOrder.ASC)
            .associate { it[AttendanceTable.date] to it[AttendanceTable.status.count()].toInt() }
    }

    override suspend fun getStatusDistribution(startDate: LocalDate, endDate: LocalDate): Map<String, Int> = newSuspendedTransaction {
        (AttendanceTable innerJoin com.attendance.app.data.EmployeesTable)
            .slice(AttendanceTable.status, AttendanceTable.status.count())
            .select { (AttendanceTable.date greaterEq startDate) and (AttendanceTable.date lessEq endDate) }
            .groupBy(AttendanceTable.status)
            .associate { it[AttendanceTable.status] to it[AttendanceTable.status.count()].toInt() }
    }

    override suspend fun getAttendanceRate(startDate: LocalDate, endDate: LocalDate): Double = newSuspendedTransaction {
        val distribution = getStatusDistribution(startDate, endDate)
        val presents = distribution["Present"] ?: 0
        val absents = distribution["Absent"] ?: 0
        val total = presents + absents
        if (total > 0) (presents.toDouble() / total.toDouble()) * 100.0 else 0.0
    }

    override suspend fun getRecentActivity(limit: Int): List<com.attendance.app.domain.DashboardActivityItem> = newSuspendedTransaction {
        val attendanceActivity = (AttendanceTable innerJoin com.attendance.app.data.EmployeesTable)
            .slice(AttendanceTable.status, AttendanceTable.updatedAt, com.attendance.app.data.EmployeesTable.name)
            .selectAll()
            .orderBy(AttendanceTable.updatedAt to SortOrder.DESC)
            .limit(limit)
            .map {
                com.attendance.app.domain.DashboardActivityItem(
                    title = it[com.attendance.app.data.EmployeesTable.name],
                    subtitle = "Marked as ${it[AttendanceTable.status]}",
                    time = it[AttendanceTable.updatedAt],
                    type = com.attendance.app.domain.ActivityType.ATTENDANCE
                )
            }

        val employeeActivity = com.attendance.app.data.EmployeesTable
            .selectAll()
            .orderBy(com.attendance.app.data.EmployeesTable.createdAt to SortOrder.DESC)
            .limit(limit)
            .map {
                com.attendance.app.domain.DashboardActivityItem(
                    title = it[com.attendance.app.data.EmployeesTable.name],
                    subtitle = "New employee registered",
                    time = it[com.attendance.app.data.EmployeesTable.createdAt],
                    type = com.attendance.app.domain.ActivityType.EMPLOYEE
                )
            }

        (attendanceActivity + employeeActivity).sortedByDescending { it.time }.take(limit)
    }

    override suspend fun getMonthlyStats(month: Int, year: Int): com.attendance.app.domain.MonthlyStats = newSuspendedTransaction {
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.plusMonths(1).minusDays(1)
        
        val distribution = getStatusDistribution(startDate, endDate)
        val present = distribution["Present"] ?: 0
        val absent = distribution["Absent"] ?: 0
        val leave = distribution["Leave"] ?: 0
        
        val rate = if (present + absent > 0) (present.toDouble() / (present + absent).toDouble()) * 100.0 else 0.0
        
        com.attendance.app.domain.MonthlyStats(
            monthName = startDate.month.name.lowercase().replaceFirstChar { it.uppercase() },
            totalPresent = present,
            totalAbsent = absent,
            totalLeave = leave,
            attendanceRate = rate
        )
    }
    override suspend fun getEmployeeStats(employeeId: Int): com.attendance.app.domain.EmployeeAttendanceStats = newSuspendedTransaction {
        val distribution = AttendanceTable
            .slice(AttendanceTable.status, AttendanceTable.status.count())
            .select { AttendanceTable.employeeId eq employeeId }
            .groupBy(AttendanceTable.status)
            .associate { it[AttendanceTable.status] to it[AttendanceTable.status.count()].toInt() }
            
        val present = distribution["Present"] ?: 0
        val absent = distribution["Absent"] ?: 0
        val leave = distribution["Leave"] ?: 0
        
        val rate = if (present + absent > 0) (present.toDouble() / (present + absent).toDouble()) * 100.0 else 0.0
        
        com.attendance.app.domain.EmployeeAttendanceStats(
            totalPresent = present,
            totalAbsent = absent,
            totalLeave = leave,
            attendanceRate = rate
        )
    }

    override suspend fun getEarliestAttendanceDate(): LocalDate? = newSuspendedTransaction {
        AttendanceTable
            .slice(AttendanceTable.date.min())
            .selectAll()
            .map { it[AttendanceTable.date.min()] }
            .singleOrNull()
    }
}
