package com.attendance.app.data

import java.io.File
import java.util.*

object AppConfig {
    private val props = Properties()
    private val configFile = File("config.properties")

    init {
        if (configFile.exists()) {
            configFile.inputStream().use { props.load(it) }
        } else {
            // Default values for fresh installation
            props.setProperty("API_BASE_URL", "")
            props.setProperty("API_ADMIN_TOKEN", "")
            save()
        }
    }

    val apiBaseUrl: String
        get() = props.getProperty("API_BASE_URL", "")

    val apiAdminToken: String
        get() = props.getProperty("API_ADMIN_TOKEN", "")

    fun isConfigured(): Boolean {
        return apiBaseUrl.isNotEmpty() && apiAdminToken.isNotEmpty()
    }

    val isDarkMode: Boolean
        get() = props.getProperty("IS_DARK_MODE", "false").toBoolean()

    fun updateConfig(baseUrl: String, token: String) {
        props.setProperty("API_BASE_URL", baseUrl)
        props.setProperty("API_ADMIN_TOKEN", token)
        save()
    }

    fun updateTheme(isDark: Boolean) {
        props.setProperty("IS_DARK_MODE", isDark.toString())
        save()
    }

    private fun save() {
        configFile.outputStream().use { 
            props.store(it, "Staff AT Configuration")
        }
    }
}
