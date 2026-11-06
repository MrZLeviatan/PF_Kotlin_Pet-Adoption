/**
Archivo de construcción raíz (nivel de proyecto). Aquí se configuran opciones comunes a todos
los subproyectos/módulos. En este proyecto solo declara los plugins de Gradle que usará el
módulo :app, con `apply false`: así se resuelven sus versiones una única vez (desde
gradle/libs.versions.toml) sin aplicarlos al proyecto raíz, y app/build.gradle.kts los activa.
**/
// Bloque que declara los plugins de Gradle disponibles para los módulos del proyecto.
plugins {
    // Plugin de Android para aplicaciones (AGP): compila, empaqueta y firma el APK; se aplica en :app.
    alias(libs.plugins.android.application) apply false
    // Plugin del compilador de Compose para Kotlin: transforma las funciones @Composable; se aplica en :app.
    alias(libs.plugins.kotlin.compose) apply false
    // Plugin de Hilt: genera las clases base de inyección de dependencias en Android; se aplica en :app.
    alias(libs.plugins.hilt.android) apply false
    // Plugin KSP (Kotlin Symbol Processing): ejecuta procesadores de anotaciones como el compilador de Hilt.
    alias(libs.plugins.devtools.ksp) apply false
    // Plugin de Google Services: lee google-services.json para conectar la app con Firebase.
    alias(libs.plugins.google.services) apply false
}