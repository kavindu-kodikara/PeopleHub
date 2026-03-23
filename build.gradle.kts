import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.9.23"
    id("org.jetbrains.compose") version "1.6.10"
    kotlin("plugin.serialization") version "1.9.23"
}

group = "com.attendance"
version = "1.0.0"

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    // Compose
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.ui)
    implementation(compose.foundation)
    implementation(compose.materialIconsExtended)

    // SQLite JDBC
    implementation("org.xerial:sqlite-jdbc:3.45.2.0")

    // Exposed ORM
    implementation("org.jetbrains.exposed:exposed-core:0.49.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.49.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.49.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.49.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")

    // API & JSON
    implementation("io.ktor:ktor-client-cio:2.3.10")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.10")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.10")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    
}

compose.desktop {
    application {
        mainClass = "com.attendance.app.MainKt"
        nativeDistributions {

            modules("java.instrument", "java.sql", "jdk.unsupported")


            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Deb)
            packageName = "Staff AT"
            packageVersion = "1.5.1"
            vendor = "Kavindu Kodikara"
            description = "Employee Attendance & Management Dashboard"
            
            windows {
                iconFile.set(project.file("src/main/resources/icon.ico"))
                shortcut = true
                menuGroup = "Staff AT"
            }
        }
    }
}
