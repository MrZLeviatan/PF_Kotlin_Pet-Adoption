import java.util.Properties

/**
Archivo de construcción del módulo :app, la aplicación Android. Define qué plugins se aplican,
la configuración de compilación (SDK, identificador, versiones, Java, funcionalidades),
las llaves secretas que se exponen al código mediante BuildConfig y todas las librerías
que usa la app. Las versiones se toman del catálogo gradle/libs.versions.toml (accesor `libs`).
Las llaves de servicios externos (Cloudinary, OpenRouter, EmailJS) se leen de local.properties,
que no se sube a git, y llegan a la capa de datos (data/repository/remote) como BuildConfig.*.
**/
// Bloque de plugins que se aplican a este módulo.
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
    // Ubica el archivo local.properties en la raíz del proyecto.
    val file = rootProject.file("local.properties")
    // Si el archivo existe, lo abre, carga sus pares clave=valor y cierra el flujo automáticamente.
    if (file.exists()) file.inputStream().use { load(it) }
}
// Devuelve el valor de una llave de local.properties o una cadena vacía si no está definida (evita romper la compilación).
fun localKey(name: String): String = localProperties.getProperty(name).orEmpty()

// Configuración específica de Android para este módulo.
android {
    // Paquete base de las clases generadas (R, BuildConfig).
    namespace = "co.edu.uniquindio.petadoptionapp"
    // Configura el SDK de compilación.
    compileSdk {
        // Versión del SDK con la que se compilará la aplicación.
        version = release(37)
    }

    // Valores por defecto que aplican a todas las variantes de compilación.
    defaultConfig {
        // Identificador único de la app en el dispositivo y en Google Play.
        applicationId = "co.edu.uniquindio.petadoptionapp"
        // Versión mínima de Android soportada (Android 7.0).
        minSdk = 24
        // Versión de Android para la que se prueba y optimiza el comportamiento de la app.
        targetSdk = 37
        // Número interno de versión, se incrementa en cada publicación.
        versionCode = 1
        // Versión visible para el usuario.
        versionName = "1.0"

        // Ejecutor usado para las pruebas instrumentadas en dispositivo/emulador.
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Cloudinary: nombre de la cuenta para la subida sin firma (el API secret nunca va en la app).
        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"${localKey("CLOUDINARY_CLOUD_NAME")}\"")
        // Cloudinary: preset de subida sin firma configurado en el panel de Cloudinary.
        buildConfigField("String", "CLOUDINARY_UPLOAD_PRESET", "\"${localKey("CLOUDINARY_UPLOAD_PRESET")}\"")
        // OpenRouter: llave de la API de IA para analizar publicaciones.
        buildConfigField("String", "OPENROUTER_API_KEY", "\"${localKey("OPENROUTER_API_KEY")}\"")
        // OpenRouter: modelo de IA a usar (opcional; si está vacío se usa el modelo por defecto del código).
        buildConfigField("String", "OPENROUTER_MODEL", "\"${localKey("OPENROUTER_MODEL")}\"")
        // EmailJS: servicio de correo que envía el código A2F.
        buildConfigField("String", "EMAILJS_SERVICE_ID", "\"${localKey("EMAILJS_SERVICE_ID")}\"")
        // EmailJS: plantilla del correo con el código A2F.
        buildConfigField("String", "EMAILJS_TEMPLATE_ID", "\"${localKey("EMAILJS_TEMPLATE_ID")}\"")
        // EmailJS: llave pública de la cuenta.
        buildConfigField("String", "EMAILJS_PUBLIC_KEY", "\"${localKey("EMAILJS_PUBLIC_KEY")}\"")
        // EmailJS: llave privada (opcional), solo se envía si en EmailJS se activó "Use Private Key".
        buildConfigField("String", "EMAILJS_PRIVATE_KEY", "\"${localKey("EMAILJS_PRIVATE_KEY")}\"")
    }

    // Configuración por tipo de compilación (debug, release).
    buildTypes {
        // Ajustes de la versión de publicación.
        release {
            // Optimización del código para release (reducción y ofuscación con R8).
            optimization {
                // Activa la optimización para que el APK sea más pequeño y rápido.
                enable = true
                // Limita la optimización agresiva a los paquetes de librerías (AndroidX, Kotlin, Kotlinx).
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    // Versión de Java usada para compilar el código fuente.
    compileOptions {
        // Nivel de lenguaje Java del código fuente.
        sourceCompatibility = JavaVersion.VERSION_11
        // Nivel del bytecode generado.
        targetCompatibility = JavaVersion.VERSION_11
    }
    // Funcionalidades opcionales del plugin de Android.
    buildFeatures {
        // Activa Jetpack Compose en el módulo.
        compose = true
        // Genera la clase BuildConfig, necesaria para los buildConfigField de las llaves.
        buildConfig = true
    }
}

// Librerías que usa la app.
dependencies {
    // Base de Compose y Android.
    // BoM de Compose: fija versiones compatibles para todas las librerías de Compose.
    implementation(platform(libs.androidx.compose.bom))
    // setContent para montar Compose en MainActivity.
    implementation(libs.androidx.activity.compose)
    // Componentes visuales de Material Design 3.
    implementation(libs.androidx.compose.material3)
    // Núcleo de la interfaz de Compose.
    implementation(libs.androidx.compose.ui)
    // Gráficos de Compose (colores, formas).
    implementation(libs.androidx.compose.ui.graphics)
    // Anotación @Preview para previsualizar pantallas en Android Studio.
    implementation(libs.androidx.compose.ui.tooling.preview)
    // Extensiones de Kotlin para las APIs base de Android.
    implementation(libs.androidx.core.ktx)
    // Corrutinas ligadas al ciclo de vida.
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Librerías implementadas:

    // Analiza y serializa objetos a formato JSON.
    implementation(libs.kotlinx.serialization.json)
    // Sistema de navegación entre pantallas usando Compose.
    implementation(libs.androidx.navigation.compose)
    // Para usar viewModel() y observar estados en Compose.
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // collectAsStateWithLifecycle(): observa flujos solo mientras la pantalla está visible.
    implementation(libs.androidx.lifecycle.runtime.compose)
    // Iconos extendidos de Material Design.
    implementation(libs.material.icons.extended)

    // Coil para cargar imágenes desde URL: las muestra en Compose.
    implementation(libs.coil.compose)
    // Módulo de red de Coil: descarga las imágenes de internet usando OkHttp.
    implementation(libs.coil.network.okhttp)

    // Hilt: inyección de dependencias en tiempo de ejecución.
    implementation(libs.hilt.android)
    // Compilador de Hilt, ejecutado por KSP para generar el código de inyección.
    ksp(libs.hilt.compiler)
    // hiltViewModel(): obtiene ViewModels inyectados desde las pantallas de Compose.
    implementation(libs.androidx.hilt.navigation.compose)

    // DataStore: guardado local de preferencias (se usará después, para la sesión).
    implementation(libs.data.store)

    // Idioma de la app (español, inglés, francés) también en Android < 13.
    implementation(libs.androidx.appcompat)
    // Cliente HTTP para las APIs REST: subida de imágenes a Cloudinary, IA de OpenRouter y correos de EmailJS.
    implementation(libs.okhttp)
    // Mapas: MapLibre (código abierto, sin tokens) con el estilo gratuito de OpenFreeMap.
    implementation(libs.maplibre.android)

    // Firebase: el BoM fija versiones compatibles entre sus librerías.
    implementation(platform(libs.firebase.bom))
    // Firebase Authentication: cuentas de usuario.
    implementation(libs.firebase.auth)
    // Cloud Firestore: base de datos de la app.
    implementation(libs.firebase.firestore)
    // `await()` para usar las tareas de Firebase desde corrutinas.
    implementation(libs.kotlinx.coroutines.play.services)

    // Pruebas.
    // JUnit para pruebas unitarias locales (incluye la exportación de datos semilla).
    testImplementation(libs.junit)
    // BoM de Compose también para las pruebas instrumentadas.
    androidTestImplementation(platform(libs.androidx.compose.bom))
    // Reglas de JUnit 4 para probar interfaces Compose.
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    // Espresso para pruebas de interfaz instrumentadas.
    androidTestImplementation(libs.androidx.espresso.core)
    // Extensiones de JUnit para AndroidX Test.
    androidTestImplementation(libs.androidx.junit)
    // Manifiesto auxiliar para lanzar pruebas de Compose (solo en debug).
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    // Herramientas de inspección de Compose (solo en debug).
    debugImplementation(libs.androidx.compose.ui.tooling)
}