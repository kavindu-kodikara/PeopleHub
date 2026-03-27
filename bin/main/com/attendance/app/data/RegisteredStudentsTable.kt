package com.attendance.app.data

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDate
import java.time.LocalDateTime

object RegisteredStudentsTable : IntIdTable("registered_students") {
    val firstName = varchar("first_name", 255)
    val lastName = varchar("last_name", 255).nullable()
    val email = varchar("email", 255).nullable()
    val nic = varchar("nic", 50).nullable()
    val whatsappNumber = varchar("whatsapp_number", 50).nullable()
    val contactNumber = varchar("contact_number", 50).nullable()
    val landlineNumber = varchar("landline_number", 50).nullable()
    val counselorName = varchar("counselor_name", 255)
    val timeDuration = integer("time_duration").default(0)
    val employeeId = reference("employee_id", EmployeesTable).nullable()
    val importDate = date("import_date").clientDefault { LocalDate.now() }
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}
