package com.attendance.app.repository

import com.attendance.app.data.RegisteredStudentsTable
import com.attendance.app.domain.RegisteredStudent
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDate

class RegisteredStudentRepositoryImpl : RegisteredStudentRepository {

    override suspend fun save(student: RegisteredStudent): Int = newSuspendedTransaction {
        RegisteredStudentsTable.insertAndGetId {
            it[firstName] = student.firstName
            it[lastName] = student.lastName
            it[email] = student.email
            it[nic] = student.nic
            it[whatsappNumber] = student.whatsappNumber
            it[contactNumber] = student.contactNumber
            it[landlineNumber] = student.landlineNumber
            it[counselorName] = student.counselorName
            it[timeDuration] = student.timeDuration
            it[employeeId] = student.employeeId
            it[importDate] = student.importDate
        }.value
    }

    override suspend fun getAllByDate(date: LocalDate): List<RegisteredStudent> = newSuspendedTransaction {
        RegisteredStudentsTable.select { RegisteredStudentsTable.importDate eq date }
            .map { toRegisteredStudent(it) }
    }

    override suspend fun getByEmployeeId(employeeId: Int, date: LocalDate?): List<RegisteredStudent> = newSuspendedTransaction {
        RegisteredStudentsTable.select {
            if (date != null) {
                (RegisteredStudentsTable.employeeId eq employeeId) and (RegisteredStudentsTable.importDate eq date)
            } else {
                RegisteredStudentsTable.employeeId eq employeeId
            }
        }.map { toRegisteredStudent(it) }
    }

    override suspend fun exists(firstName: String, counselorName: String, importDate: LocalDate): Boolean = newSuspendedTransaction {
        RegisteredStudentsTable.select {
            (RegisteredStudentsTable.firstName eq firstName) and 
            (RegisteredStudentsTable.counselorName eq counselorName) and 
            (RegisteredStudentsTable.importDate eq importDate)
        }.count() > 0
    }

    private fun toRegisteredStudent(row: ResultRow) = RegisteredStudent(
        id = row[RegisteredStudentsTable.id].value,
        firstName = row[RegisteredStudentsTable.firstName],
        lastName = row[RegisteredStudentsTable.lastName],
        email = row[RegisteredStudentsTable.email],
        nic = row[RegisteredStudentsTable.nic],
        whatsappNumber = row[RegisteredStudentsTable.whatsappNumber],
        contactNumber = row[RegisteredStudentsTable.contactNumber],
        landlineNumber = row[RegisteredStudentsTable.landlineNumber],
        counselorName = row[RegisteredStudentsTable.counselorName],
        timeDuration = row[RegisteredStudentsTable.timeDuration],
        employeeId = row[RegisteredStudentsTable.employeeId]?.value,
        importDate = row[RegisteredStudentsTable.importDate],
        createdAt = row[RegisteredStudentsTable.createdAt]
    )
}
