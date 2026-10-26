plugins {
    // Habilita este módulo como una aplicación de Android.
    alias(libs.plugins.android.application)
    // Activa Jetpack Compose para la interfaz gráfica con Kotlin.
    alias(libs.plugins.kotlin.compose)
    // Agrega el plugin de serialización de Kotlin (usa la misma versión de Kotlin del proyecto).
    alias(libs.plugins.kotlinx.serialization)
    // Agrega Hilt para la inyección de dependencias.
    alias(libs.plugins.hilt.android)
    // Agrega KSP, el procesador que genera el código de Hilt.
    alias(libs.plugins.devtools.ksp)
}

android {
    namespace = "co.edu.uniquindio.petadoptionapp"
    compileSdk {
        // Versión del SDK con la que se compilará la aplicación.
        version = release(37)
    }

    defaultConfig {
        applicationId = "co.edu.uniquindio.petadoptionapp"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
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

dependencies {
    // Base de Compose y Android
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Librerías implementadas:

    // Analiza y serializa objetos a formato JSON.
    implementation(libs.kotlinx.serialization.json)
    // Sistema de navegación entre pantallas usando Compose.
    implementation(libs.androidx.navigation.compose)
    // Para usar viewModel() y observar estados en Compose.
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    // Iconos extendidos de Material Design.
    implementation(libs.material.icons.extended)

    // Coil para cargar imágenes desde URL: las muestra en Compose y las descarga de internet.
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Hilt: inyección de dependencias.
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // DataStore: guardado local de preferencias (se usará después, para la sesión).
    implementation(libs.data.store)

    // Pruebas
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}