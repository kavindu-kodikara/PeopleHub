package com.attendance.app.data

import com.attendance.app.data.AttendanceTable
import com.attendance.app.data.EmployeesTable
import com.attendance.app.data.SupabaseSyncTable
import com.attendance.app.data.RegisteredStudentsTable
import com.attendance.app.data.StudentResponsesTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

object DatabaseFactory {
    fun init() {
        val driverClassName = "org.sqlite.JDBC"
        val jdbcUrl = "jdbc:sqlite:attendance.db"
        Database.connect(jdbcUrl, driverClassName, setupConnection = { connection: Connection ->
            connection.prepareStatement("PRAGMA foreign_keys = ON;").execute()
        })

        transaction {
            SchemaUtils.create(EmployeesTable, AttendanceTable, SupabaseSyncTable, StudentResponsesTable, RegisteredStudentsTable)
            SchemaUtils.createMissingTablesAndColumns(EmployeesTable, AttendanceTable, SupabaseSyncTable, StudentResponsesTable, RegisteredStudentsTable)
        }
    }
}
