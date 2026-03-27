package com.attendance.app.service

import com.attendance.app.domain.EmployeeAnalysis
import com.attendance.app.repository.EmployeeRepository
import com.attendance.app.repository.StudentResponseRepository
import com.attendance.app.repository.RegisteredStudentRepository
import java.time.LocalDate

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

            // Matching logic: Count unique registered students that were also in First Confirmations
            // However, the requirement is:
            // Total Calls (First Confirmation)
            // Total Registered
            // Total Success (>= 30 min)
            // Conversion Rate: Registered / First Confirmation
            // Success Rate: Success / Registered

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
}
