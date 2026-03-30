package com.attendance.app.repository

import com.attendance.app.data.StudentResponsesTable
import com.attendance.app.domain.StudentResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDate

class StudentResponseRepositoryImpl : StudentResponseRepository {

    override suspend fun save(response: StudentResponse): Int = newSuspendedTransaction {
        StudentResponsesTable.insertAndGetId {
            it[timestamp] = response.timestamp
            it[studentName] = response.studentName
            it[nic] = response.nic
            it[address] = response.address
            it[whatsappNumber] = response.whatsappNumber
            it[contactNumber] = response.contactNumber
            it[databaseName] = response.databaseName
            it[counselorName] = response.counselorName
            it[employeeId] = response.employeeId
            it[importDate] = response.importDate
        }.value
    }

    override suspend fun getAllByDate(date: LocalDate): List<StudentResponse> = newSuspendedTransaction {
        StudentResponsesTable.select { StudentResponsesTable.importDate eq date }
            .map { toStudentResponse(it) }
    }

    override suspend fun getByEmployeeId(employeeId: Int, date: LocalDate?): List<StudentResponse> = newSuspendedTransaction {
        StudentResponsesTable.select {
            if (date != null) {
                (StudentResponsesTable.employeeId eq employeeId) and (StudentResponsesTable.importDate eq date)
            } else {
                StudentResponsesTable.employeeId eq employeeId
            }
        }.map { toStudentResponse(it) }
    }

    override suspend fun exists(timestamp: String, studentName: String, counselorName: String): Boolean = newSuspendedTransaction {
        StudentResponsesTable.select {
            (StudentResponsesTable.timestamp eq timestamp) and 
            (StudentResponsesTable.studentName eq studentName) and 
            (StudentResponsesTable.counselorName eq counselorName)
        }.count() > 0
    }

    override suspend fun getAllByDateRange(startDate: LocalDate, endDate: LocalDate): List<StudentResponse> = newSuspendedTransaction {
        StudentResponsesTable.select {
            (StudentResponsesTable.importDate greaterEq startDate) and (StudentResponsesTable.importDate lessEq endDate)
        }.map { toStudentResponse(it) }
    }

    private fun toStudentResponse(row: ResultRow) = StudentResponse(
        id = row[StudentResponsesTable.id].value,
        timestamp = row[StudentResponsesTable.timestamp],
        studentName = row[StudentResponsesTable.studentName],
        nic = row[StudentResponsesTable.nic],
        address = row[StudentResponsesTable.address],
        whatsappNumber = row[StudentResponsesTable.whatsappNumber],
        contactNumber = row[StudentResponsesTable.contactNumber],
        databaseName = row[StudentResponsesTable.databaseName],
        counselorName = row[StudentResponsesTable.counselorName],
        employeeId = row[StudentResponsesTable.employeeId]?.value,
        importDate = row[StudentResponsesTable.importDate],
        createdAt = row[StudentResponsesTable.createdAt]
    )
}
