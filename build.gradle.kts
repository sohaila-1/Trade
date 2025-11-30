plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// build.gradle.kts (project-level)
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.13.1")
        classpath("com.google.gms:google-services:4.4.4") // <-- Firebase plugin
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.57.2") // Hilt plugin
        classpath(kotlin("gradle-plugin", version = "1.9.10"))

    }

}

