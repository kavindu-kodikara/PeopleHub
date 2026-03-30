package com.attendance.app.repository

import com.attendance.app.data.EmployeesTable
import com.attendance.app.data.SupabaseService
import com.attendance.app.data.SupabaseSyncTable
import com.attendance.app.domain.Employee
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class EmployeeRepositoryImpl : EmployeeRepository {
    private fun ResultRow.toEmployee() = Employee(
        id = this[EmployeesTable.id].value,
        name = this[EmployeesTable.name],
        email = this[EmployeesTable.email],
        whatsappNumber = this[EmployeesTable.whatsappNumber],
        nicNumber = this[EmployeesTable.nicNumber],
        address = this[EmployeesTable.address],
        googleSheetLink = this[EmployeesTable.googleSheetLink],
        internalComment = this[EmployeesTable.internalComment],
        employeeCode = this[EmployeesTable.employeeCode],
        username = this[EmployeesTable.username],
        password = this[EmployeesTable.password],
        onboardingStatus = this[EmployeesTable.onboardingStatus],
        color = this[EmployeesTable.color],
        createdAt = this[EmployeesTable.createdAt],
        updatedAt = this[EmployeesTable.updatedAt]
    )

    override suspend fun getAllEmployees(): List<Employee> = newSuspendedTransaction {
        EmployeesTable.selectAll().map { it.toEmployee() }
    }

    override suspend fun getEmployeeById(id: Int): Employee? = newSuspendedTransaction {
        EmployeesTable.select { EmployeesTable.id eq id }.map { it.toEmployee() }.singleOrNull()
    }


    /**
     * Updates an employee profile locally in the SQLite database.
     * This method is intentionally local-only and does NOT sync changes to Supabase.
     */
    override suspend fun updateEmployee(employee: Employee): Boolean = newSuspendedTransaction {
        val updatedRows = EmployeesTable.update({ EmployeesTable.id eq employee.id }) {
            it[name] = employee.name
            it[email] = employee.email
            it[whatsappNumber] = employee.whatsappNumber
            it[nicNumber] = employee.nicNumber
            it[address] = employee.address
            it[googleSheetLink] = employee.googleSheetLink
            it[internalComment] = employee.internalComment
            it[employeeCode] = employee.employeeCode
            it[username] = employee.username
            it[password] = employee.password
            it[onboardingStatus] = employee.onboardingStatus
            it[color] = employee.color
            it[updatedAt] = java.time.LocalDateTime.now()
        }
        updatedRows > 0
    }

    override suspend fun deleteEmployee(id: Int): Boolean = newSuspendedTransaction {
        EmployeesTable.deleteWhere { EmployeesTable.id eq id } > 0
    }

    override suspend fun getOnboardingStatusBreakdown(): Map<String, Int> = newSuspendedTransaction {
        EmployeesTable
            .slice(EmployeesTable.onboardingStatus, EmployeesTable.onboardingStatus.count())
            .selectAll()
            .groupBy(EmployeesTable.onboardingStatus)
            .associate { it[EmployeesTable.onboardingStatus] to it[EmployeesTable.onboardingStatus.count()].toInt() }
    }

    override suspend fun getEmployeesByOnboardingStatus(status: String): List<Employee> = newSuspendedTransaction {
        EmployeesTable.select { EmployeesTable.onboardingStatus eq status }.map { it.toEmployee() }
    }

    override suspend fun syncEmployeesFromSupabase(): Result<Unit> = try {
        val supabaseEmployees = SupabaseService.fetchEmployees()
        newSuspendedTransaction {
            supabaseEmployees.forEach { se ->
                // Use employeeCode as unique identifier for matching, or NIC if code is null
                val matchCode = se.employeeCode
                val existingId = if (matchCode != null) {
                    EmployeesTable.select { EmployeesTable.employeeCode eq matchCode }
                        .map { it[EmployeesTable.id].value }
                        .singleOrNull()
                } else {
                    EmployeesTable.select { EmployeesTable.nicNumber eq se.nic }
                        .map { it[EmployeesTable.id].value }
                        .singleOrNull()
                }

                val localId = if (existingId != null) {
                    // Update existing
                    EmployeesTable.update({ EmployeesTable.id eq existingId }) {
                        it[name] = se.fullName
                        it[nicNumber] = se.nic
                        it[whatsappNumber] = se.mobile
                        it[googleSheetLink] = se.sheetLink
                        it[updatedAt] = java.time.LocalDateTime.now()
                    }
                    existingId
                } else {
                    // Insert new
                    EmployeesTable.insertAndGetId {
                        it[name] = se.fullName
                        it[nicNumber] = se.nic
                        it[whatsappNumber] = se.mobile
                        it[employeeCode] = se.employeeCode
                        it[googleSheetLink] = se.sheetLink
                        it[onboardingStatus] = "Default" // Or a sensible default
                    }.value
                }

                // Update Sync Table
                SupabaseSyncTable.replace {
                    it[employeeId] = localId
                    it[supabaseAuthId] = se.id
                    it[syncStatus] = "Synced"
                    it[lastSyncedAt] = java.time.LocalDateTime.now()
                    it[loginStatus] = "Synced"
                }
            }
        }
        Result.success(Unit)
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }
}
