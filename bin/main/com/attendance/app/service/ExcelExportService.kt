package com.attendance.app.service

import com.attendance.app.domain.AttendanceRecord
import com.attendance.app.domain.AttendanceStatus
import com.attendance.app.domain.Employee
import com.attendance.app.repository.AttendanceRepository
import com.attendance.app.repository.EmployeeRepository
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExcelExportService(
    private val employeeRepository: EmployeeRepository,
    private val attendanceRepository: AttendanceRepository
) {
    suspend fun exportAttendanceToExcel(filePath: String): Result<String> {
        return try {
            val employees = employeeRepository.getAllEmployees()
            val earliestDate = attendanceRepository.getEarliestAttendanceDate() ?: LocalDate.now()
            val today = LocalDate.now()

            val dateList = mutableListOf<LocalDate>()
            var currentDate = earliestDate
            while (!currentDate.isAfter(today)) {
                dateList.add(currentDate)
                currentDate = currentDate.plusDays(1)
            }

            val allAttendance = attendanceRepository.getAttendanceSummary(earliestDate, today)
            val attendanceMap = allAttendance.groupBy { it.employeeId }
                .mapValues { (_, records) -> records.associateBy { it.date } }

            val workbook: Workbook = XSSFWorkbook()
            val sheet: Sheet = workbook.createSheet("Attendance Report")

            // Styles
            val headerStyle = createHeaderStyle(workbook)
            val presentStyle = createColoredStyle(workbook, IndexedColors.LIGHT_GREEN.index)
            val absentStyle = createColoredStyle(workbook, IndexedColors.RED.index)
            val leaveStyle = createColoredStyle(workbook, IndexedColors.YELLOW.index)
            val defaultStyle = workbook.createCellStyle().apply {
                borderBottom = BorderStyle.THIN
                borderTop = BorderStyle.THIN
                borderLeft = BorderStyle.THIN
                borderRight = BorderStyle.THIN
                alignment = HorizontalAlignment.CENTER
            }

            // Headers
            val headerRow: Row = sheet.createRow(0)
            val mainHeaders = listOf("Name", "NIC", "Mobile Number")
            mainHeaders.forEachIndexed { index, title ->
                val cell: Cell = headerRow.createCell(index)
                cell.setCellValue(title)
                cell.setCellStyle(headerStyle)
            }

            val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
            dateList.forEachIndexed { index, date ->
                val cell: Cell = headerRow.createCell(mainHeaders.size + index)
                cell.setCellValue(date.format(dateFormatter))
                cell.setCellStyle(headerStyle)
            }

            // Data Rows
            employees.forEachIndexed { rowIndex, employee ->
                val row: Row = sheet.createRow(rowIndex + 1)
                row.createCell(0).apply { setCellValue(employee.name); setCellStyle(defaultStyle) }
                row.createCell(1).apply { setCellValue(employee.nicNumber ?: ""); setCellStyle(defaultStyle) }
                row.createCell(2).apply { setCellValue(employee.whatsappNumber ?: ""); setCellStyle(defaultStyle) }

                val empAttendance = attendanceMap[employee.id] ?: emptyMap()

                dateList.forEachIndexed { colIndex, date ->
                    val cell: Cell = row.createCell(mainHeaders.size + colIndex)
                    val record = empAttendance[date]
                    if (record != null) {
                        when (record.status) {
                            AttendanceStatus.PRESENT.value -> {
                                cell.setCellValue("P")
                                cell.setCellStyle(presentStyle)
                            }
                            AttendanceStatus.ABSENT.value -> {
                                cell.setCellValue("A")
                                cell.setCellStyle(absentStyle)
                            }
                            AttendanceStatus.LEAVE.value -> {
                                cell.setCellValue("L")
                                cell.setCellStyle(leaveStyle)
                            }
                            else -> {
                                cell.setCellStyle(defaultStyle)
                            }
                        }
                    } else {
                        cell.setCellStyle(defaultStyle)
                    }
                }
            }

            // Auto-size columns for main headers
            for (i in 0 until (mainHeaders.size + dateList.size)) {
                sheet.autoSizeColumn(i)
            }

            FileOutputStream(filePath).use { workbook.write(it) }
            workbook.close()

            Result.success(filePath)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun createHeaderStyle(workbook: Workbook): CellStyle {
        val style = workbook.createCellStyle()
        val font = workbook.createFont()
        font.bold = true
        style.setFont(font)
        style.fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
        style.fillPattern = FillPatternType.SOLID_FOREGROUND
        style.alignment = HorizontalAlignment.CENTER
        style.borderBottom = BorderStyle.THIN
        style.borderTop = BorderStyle.THIN
        style.borderLeft = BorderStyle.THIN
        style.borderRight = BorderStyle.THIN
        return style
    }

    private fun createColoredStyle(workbook: Workbook, colorIndex: Short): CellStyle {
        val style = workbook.createCellStyle()
        style.fillForegroundColor = colorIndex
        style.fillPattern = FillPatternType.SOLID_FOREGROUND
        style.alignment = HorizontalAlignment.CENTER
        style.borderBottom = BorderStyle.THIN
        style.borderTop = BorderStyle.THIN
        style.borderLeft = BorderStyle.THIN
        style.borderRight = BorderStyle.THIN
        return style
    }
}
