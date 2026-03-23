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
            // Default values for development
            props.setProperty("API_BASE_URL", "https://staff-at-web.vercel.app/")
            props.setProperty("API_ADMIN_TOKEN", "a68cb2d148d5400f8686a4bd8450e8ce")
            save()
        }
    }

    val apiBaseUrl: String
        get() = props.getProperty("API_BASE_URL", "https://staff-at-web.vercel.app/")

    val apiAdminToken: String
        get() = props.getProperty("API_ADMIN_TOKEN", "a68cb2d148d5400f8686a4bd8450e8ce")

    fun updateConfig(baseUrl: String, token: String) {
        props.setProperty("API_BASE_URL", baseUrl)
        props.setProperty("API_ADMIN_TOKEN", token)
        save()
    }

    private fun save() {
        configFile.outputStream().use { 
            props.store(it, "Staff AT Configuration")
        }
    }
}
