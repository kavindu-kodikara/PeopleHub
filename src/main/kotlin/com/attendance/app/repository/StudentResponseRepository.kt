package com.attendance.app.repository

import com.attendance.app.domain.StudentResponse
import java.time.LocalDate

interface StudentResponseRepository {
    suspend fun save(response: StudentResponse): Int
    suspend fun getAllByDate(date: LocalDate): List<StudentResponse>
    suspend fun getByEmployeeId(employeeId: Int, date: LocalDate? = null): List<StudentResponse>
    suspend fun exists(timestamp: String, studentName: String, counselorName: String): Boolean
    suspend fun getAllByDateRange(startDate: LocalDate, endDate: LocalDate): List<StudentResponse>
}
