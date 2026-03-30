package com.attendance.app.service

import com.attendance.app.domain.*
import com.attendance.app.repository.*
import java.time.LocalDate
import java.time.DayOfWeek

class AnalysisService(
    private val employeeRepository: EmployeeRepository,
    private val studentResponseRepository: StudentResponseRepository,
    private val registeredStudentRepository: RegisteredStudentRepository
) {
    suspend fun getAnalysisByDate(date: LocalDate): List<EmployeeAnalysis> {
        val employees = employeeRepository.getAllEmployees()
        val firstConfirmations = studentResponseRepository.getAllByDate(date)
        val registeredStudents = registeredStudentRepository.getAllByDate(date)

        val fcByEmployee = firstConfirmations.groupBy { it.employeeId }
        val regByEmployee = registeredStudents.groupBy { it.employeeId }

        return employees.map { emp ->
            val empFC = fcByEmployee[emp.id] ?: emptyList()
            val empReg = regByEmployee[emp.id] ?: emptyList()

            val totalFC = empFC.size
            val totalReg = empReg.size
            val totalSuccess = empReg.count { it.timeDuration >= 30 }

            val conversionRate = if (totalFC > 0) (totalReg.toDouble() / totalFC.toDouble()) * 100.0 else 0.0
            val successRate = if (totalReg > 0) (totalSuccess.toDouble() / totalReg.toDouble()) * 100.0 else 0.0

            EmployeeAnalysis(
                employeeName = emp.name,
                employeeCode = emp.employeeCode,
                totalFirstConfirmations = totalFC,
                totalRegistered = totalReg,
                totalSuccess = totalSuccess,
                conversionRate = conversionRate,
                successRate = successRate
            )
        }.filter { it.totalFirstConfirmations > 0 || it.totalRegistered > 0 }
         .sortedBy { it.employeeCode ?: it.employeeName }
    }

    suspend fun getEmployeeRecruitmentTrends(startDate: LocalDate, endDate: LocalDate): List<EmployeeTrendData> {
        val allEmployees = employeeRepository.getAllEmployees()
        val fcList = studentResponseRepository.getAllByDateRange(startDate, endDate)
        val regList = registeredStudentRepository.getAllByDateRange(startDate, endDate)

        val fcByEmployeeAndDate = fcList.groupBy { it.employeeId to it.importDate }
        val regByEmployeeAndDate = regList.groupBy { it.employeeId to it.importDate }

        val allDates = mutableListOf<LocalDate>()
        var current = startDate
        while (!current.isAfter(endDate)) {
            if (current.dayOfWeek != DayOfWeek.SUNDAY) {
                allDates.add(current)
            }
            current = current.plusDays(1)
        }

        return allEmployees.map { emp ->
            val dailyCounts = allDates.map { date ->
                EmployeeDayCount(
                    date = date,
                    confirmations = fcByEmployeeAndDate[emp.id to date]?.size ?: 0,
                    registrations = regByEmployeeAndDate[emp.id to date]?.size ?: 0
                )
            }
            EmployeeTrendData(
                employeeId = emp.id,
                employeeName = emp.name,
                color = emp.color,
                dailyCounts = dailyCounts
            )
        }.filter { trend -> 
            // Only include employees who have at least one registration or confirmation in the period
            trend.dailyCounts.any { it.confirmations > 0 || it.registrations > 0 }
        }
    }

    suspend fun getWeeklyRecruitmentSummary(date: LocalDate): WeeklyRecruitmentSummary {
        val startOfWeek = date.with(DayOfWeek.MONDAY)
        val endOfWeek = date.with(DayOfWeek.SATURDAY) // Week ends on Saturday as requested
        
        val fcList = studentResponseRepository.getAllByDateRange(startOfWeek, endOfWeek)
        val regList = registeredStudentRepository.getAllByDateRange(startOfWeek, endOfWeek)
        
        val totalFC = fcList.size
        val totalReg = regList.size
        val rate = if (totalFC > 0) (totalReg.toDouble() / totalFC.toDouble()) * 100.0 else 0.0
        
        return WeeklyRecruitmentSummary(totalFC, totalReg, rate)
    }
}
