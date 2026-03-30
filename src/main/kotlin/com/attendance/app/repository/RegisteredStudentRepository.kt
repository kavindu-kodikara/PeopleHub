package com.attendance.app.repository

import com.attendance.app.domain.RegisteredStudent
import java.time.LocalDate

interface RegisteredStudentRepository {
    suspend fun save(student: RegisteredStudent): Int
    suspend fun getAllByDate(date: LocalDate): List<RegisteredStudent>
    suspend fun getByEmployeeId(employeeId: Int, date: LocalDate? = null): List<RegisteredStudent>
    suspend fun getAllByDateRange(startDate: LocalDate, endDate: LocalDate): List<RegisteredStudent>
    suspend fun exists(firstName: String, counselorName: String, importDate: LocalDate): Boolean
}
