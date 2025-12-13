    plugins {
        alias(libs.plugins.android.application)
        alias(libs.plugins.kotlin.android)
        alias(libs.plugins.kotlin.compose)
        alias(libs.plugins.google.services)

        id("com.google.devtools.ksp") version "2.2.21-2.0.4"
    }

    android {
        namespace = "com.example.tradeconnect"
        compileSdk = 36

        defaultConfig {
            applicationId = "com.example.tradeconnect"
            minSdk = 24
            targetSdk = 36
            versionCode = 1
            versionName = "1.0"

            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        buildTypes {
            release {
                isMinifyEnabled = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
        buildFeatures {
            compose = true
        }
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    dependencies {

        /* ---------- CORE / ANDROID ---------- */
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.activity.compose)

        /* ---------- COMPOSE ---------- */
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.compose.ui)
        implementation(libs.androidx.compose.ui.graphics)
        implementation(libs.androidx.compose.ui.tooling.preview)
        implementation(libs.androidx.compose.material3)
        implementation(libs.androidx.compose.foundation)
        implementation(libs.androidx.compose.ui.text)
        implementation(libs.compose.material.icons)
        implementation(libs.navigation.compose)
        implementation(libs.lifecycle.viewmodel.compose)

        /* ---------- FIREBASE ---------- */
        implementation(platform(libs.firebase.bom))
        implementation(libs.firebase.auth)
        implementation(libs.firebase.firestore)
        implementation(libs.firebase.storage)
        implementation("com.google.firebase:firebase-database-ktx")

        /* ---------- GOOGLE AUTH / CREDENTIALS ---------- */
        implementation(libs.androidxCredentials)
        implementation(libs.androidxCredentialsPlayServicesAuth)
        implementation(libs.googleid)



        /* ---------- DATASTORE ---------- */
        implementation(libs.datastore.preferences)
        implementation(libs.ads.mobile.sdk)
        implementation(libs.androidx.foundation)

        /* ---------- ROOM (DATABASE) ---------- */
        val roomVersion = "2.8.4"
        implementation("androidx.room:room-runtime:$roomVersion")
        implementation("androidx.room:room-ktx:$roomVersion")
        ksp("androidx.room:room-compiler:$roomVersion")

        /* ---------- COROUTINES ---------- */
        implementation(libs.coroutines.android)

        /* ---------- IMAGE / MEDIA ---------- */
        implementation(libs.ucrop)
        implementation("io.coil-kt:coil-compose:2.5.0")

        /* ---------- WORK MANAGER ---------- */
        implementation("androidx.work:work-runtime-ktx:2.9.0")

        /* ---------- TEST ---------- */
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.androidx.compose.ui.test.junit4)
        debugImplementation(libs.androidx.compose.ui.tooling)
        debugImplementation(libs.androidx.compose.ui.test.manifest)
    }
