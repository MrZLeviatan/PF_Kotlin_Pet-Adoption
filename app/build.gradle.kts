import java.util.Properties

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
    // Lee app/google-services.json para conectar la app con el proyecto de Firebase.
    alias(libs.plugins.google.services)
}

// Llaves en local.properties (nunca en git). Se leen aquí y pasan a BuildConfig.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
fun localKey(name: String): String = localProperties.getProperty(name).orEmpty()

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

        // Cloudinary: subida sin firma (el API secret nunca va en la app).
        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"${localKey("CLOUDINARY_CLOUD_NAME")}\"")
        buildConfigField("String", "CLOUDINARY_UPLOAD_PRESET", "\"${localKey("CLOUDINARY_UPLOAD_PRESET")}\"")
        // OpenRouter: IA para analizar publicaciones (OPENROUTER_MODEL es opcional).
        buildConfigField("String", "OPENROUTER_API_KEY", "\"${localKey("OPENROUTER_API_KEY")}\"")
        buildConfigField("String", "OPENROUTER_MODEL", "\"${localKey("OPENROUTER_MODEL")}\"")
        // EmailJS: envía el código A2F al correo (la llave privada es opcional).
        buildConfigField("String", "EMAILJS_SERVICE_ID", "\"${localKey("EMAILJS_SERVICE_ID")}\"")
        buildConfigField("String", "EMAILJS_TEMPLATE_ID", "\"${localKey("EMAILJS_TEMPLATE_ID")}\"")
        buildConfigField("String", "EMAILJS_PUBLIC_KEY", "\"${localKey("EMAILJS_PUBLIC_KEY")}\"")
        buildConfigField("String", "EMAILJS_PRIVATE_KEY", "\"${localKey("EMAILJS_PRIVATE_KEY")}\"")
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
        buildConfig = true
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

    // Idioma de la app (español, inglés, francés) también en Android < 13.
    implementation(libs.androidx.appcompat)
    // Subida de imágenes a Cloudinary (API REST).
    implementation(libs.okhttp)
    // Mapas: MapLibre (código abierto, sin tokens) con el estilo gratuito de OpenFreeMap.
    implementation(libs.maplibre.android)

    // Firebase: Authentication (cuentas) y Firestore (base de datos). El BoM fija versiones compatibles.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    // `await()` para usar las tareas de Firebase desde corrutinas.
    implementation(libs.kotlinx.coroutines.play.services)

    // Pruebas
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}