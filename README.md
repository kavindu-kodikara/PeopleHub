# Staff AT

Welcome to **Staff AT**, a clean, modern, and professional desktop employee attendance management system. Staff AT offers a powerful, **hybrid offline-first experience**: it combines the speed and security of a local SQLite database with optional **Cloud Sync** capabilities via Supabase to keep your team data unified across your organization.

---

## ✨ Key Features in v1.6.0

- **Hybrid Cloud Sync**: Securely sync employee records from Supabase while maintaining all detailed attendance data locally.
- **Admin Command Center**: A premium dashboard for high-level insights, staff metrics, and system administration.
- **Customizable API Endpoints**: Easily configure your own Vercel/Supabase backend details directly from the app settings.
- **Dynamic UI**: Beautiful time-based greetings and a sophisticated **Dark Mode** with a fully themed custom window header.
- **Advanced Employee Profiles**: Track attendance history, manage onboarding status, and store private administrative notes or external links.
- **Local Database Management**: Built-in tools to Export and Import backups of your `attendance.db` file safely.

---

## 📥 Download and Installation

### ⚠️ IMPORTANT INSTALLATION WARNING: DO NOT INSTALL IN C:\ DRIVE

Staff AT uses a local SQLite database (`attendance.db`) to store your primary data. Windows strictly restricts read and write permissions in the `C:\` drive (especially inside `C:\Program Files`).

**To ensure the application works correctly, please follow these steps:**

1. Download the latest `Staff-AT-Installer.exe` from the [Releases page](../../releases).
2. Run the installer. When prompted to select the destination folder, **change the installation directory** to a location outside of your protected `C:\` drive (e.g., `D:\StaffAT` or your `Documents` folder).
3. The database file (`attendance.db`) and configuration (`config.properties`) will be generated inside that folder.

---

## 🚀 Getting Started

Once installed, launch Staff AT and log in using your administrator credentials.

Passowrd : admin

### 1. API Configuration (For New Systems)
If you are setting up Staff AT for the first time with a custom backend:
1. Go to the **Dashboard**.
2. Scroll to the bottom and click **Application Settings**.
3. Enter your **API Base URL** and **Admin Token** provided by your system administrator.
4. Save Changes. This allows the app to communicate with your cloud database.

### 2. Syncing Employees
To pull your team directory from the cloud:
1. Navigate to the **Employees** tab.
2. The app will automatically attempt to sync new records, or you can trigger a refresh to ensure all local records are up to date with Supabase.

### 3. Daily Attendance
Tracking presence is quick and easy:
1. Navigate to the **Daily Attendance** tab.
2. Simply click **Present**, **Absent**, or **Leave** next to each employee's name.
3. All marks are saved instantly to your local database for maximum speed.

---

## 💾 Managed Backups

Since your detailed attendance history is stored locally for privacy and speed, we highly recommend regular backups:
- **Export**: Use the **Export Database** button on the Dashboard to save a snapshot of your records.
- **Import**: Use the **Import Database** button to restore your data on a new machine.

---

## ❓ Troubleshooting

- **"Permission Denied" errors or data not saving:** 
  You most likely installed the application in `C:\Program Files`. Please reinstall it in a location with write permissions like `D:\StaffAT`.
  
- **Sync is not working:** 
  Check your **Application Settings** on the Dashboard to ensure your API Base URL and Admin Token are correct and that you have an active internet connection for the sync process.

---
*Empowering managers with privacy-first, cloud-capable team tools.*
