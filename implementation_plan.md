# Dashboard Recruitment Analytics Implementation Plan

Enhance the main dashboard with a premium analysis section for Registered Students and First Confirmations, featuring visual charts and custom business logic for holidays and leaves.

## User Review Required

> [!IMPORTANT]
> **Business Logic Confirmation**:
> - **Sundays**: Totally excluded from calculations and the X-axis of the chart.
> - **Weekly Leave**: Employees on leave will be counted as 0.
> - **Date Range**:
>     - **Top Metrics**: Show current week summary.
>     - **Main Chart**: A **Scrollable Line Chart** showing the trend from the start of data (or employee's first record) to yesterday.

## Proposed Changes

### [Component Name] Service Layer

#### [MODIFY] [AnalysisService.kt](file:///d:/Client%20Projects/Attendance/src/main/kotlin/com/attendance/app/service/AnalysisService.kt)
- Add `getRecruitmentTrends(startDate: LocalDate, endDate: LocalDate)`:
    - Fetches data for the specified range.
    - Filters out Sundays.
    - Aggregates daily totals for "First Confirmation" and "Registered".
- Add `getEarliestDataDate()`:
    - Helper to find when to start the analysis (using `AttendanceRepository.getEarliestAttendanceDate()`).

---

### [Component Name] UI Components

#### [MODIFY] [DashboardWidgets.kt](file:///d:/Client%20Projects/Attendance/src/main/kotlin/com/attendance/app/ui/components/DashboardWidgets.kt)
- **[NEW] `ScrollableRecruitmentLineChart`**: A Line Chart (using Compose Canvas) wrapped in a horizontal scrollable container.
    - Two lines: First Confirmations (e.g., Blue) and Registered Students (e.g., Green).
    - Clear labels for dates (excluding Sundays).
- **[NEW] `WeeklyRecruitmentSummary`**: A clean header component with key weekly metrics.

---

### [Component Name] Screens

#### [MODIFY] [DashboardScreen.kt](file:///d:/Client%20Projects/Attendance/src/main/kotlin/com/attendance/app/ui/screens/DashboardScreen.kt)
- Reorder the layout:
    1. **Top**: New Recruitment Analysis section (Weekly Summary + Scrollable Line Chart).
    2. **Middle**: Existing Attendance KPIs and Activity.
    3. **Bottom**: System Administration (Export/Import/Settings).
- Integration with the new `AnalysisService` methods.

## Open Questions

- None. The user has confirmed the "Line Chart" preference and the date range (start to yesterday).

## Verification Plan

### Automated Tests
- Since there are no automated tests currently, I will verify the logic by:
    - Manually checking if Sundays are excluded from the aggregated data.
    - Verifying that employees on leave show 0 in the breakdown.

### Manual Verification
- Deploy the application in the IDE.
- Navigate to the Dashboard.
- Verify the new chart displays correctly with sample data.
- Verify "Export Database", "Import Database", and "Settings" still function as expected.
