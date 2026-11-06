/**
Archivo de configuración de Gradle: se evalúa antes que cualquier build.gradle.kts.
Define de qué repositorios se descargan los plugins y las librerías, el nombre del proyecto
y qué módulos lo componen (aquí solo :app). Las versiones concretas de cada dependencia
están centralizadas en gradle/libs.versions.toml (catálogo de versiones "libs").
**/
// Configura cómo y desde dónde Gradle resuelve los plugins que usan los build.gradle.kts.
pluginManagement {
    // Lista de repositorios donde se buscan los plugins, en orden de prioridad.
    repositories {
        // Repositorio Maven de Google, donde están el plugin de Android, Firebase y AndroidX.
        google {
            // Restringe qué grupos se buscan en el repositorio de Google para acelerar la resolución.
            content {
                // Solo busca aquí los artefactos cuyo grupo empieza por com.android (por ejemplo, el AGP).
                includeGroupByRegex("com\\.android.*")
                // Solo busca aquí los artefactos de Google (Firebase, google-services, etc.).
                includeGroupByRegex("com\\.google.*")
                // Solo busca aquí las librerías de AndroidX.
                includeGroupByRegex("androidx.*")
            }
        }
        // Maven Central: repositorio público general (Kotlin, KSP, Dagger, etc.).
        mavenCentral()
        // Portal oficial de plugins de Gradle, para los plugins que no están en los anteriores.
        gradlePluginPortal()
    }
}
// Plugins que se aplican a la configuración (settings) del proyecto.
plugins {
    // Permite que Gradle descargue automáticamente el JDK requerido por la toolchain si no está instalado.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
// Configura la resolución de las dependencias (librerías) de todos los módulos.
dependencyResolutionManagement {
    // Obliga a declarar los repositorios solo aquí; falla si un módulo intenta declarar los suyos.
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    // Repositorios desde los que se descargan las librerías de la app.
    repositories {
        // Repositorio de Google: AndroidX, Compose, Firebase, Material.
        google()
        // Maven Central: Coil, OkHttp, MapLibre, Kotlinx, Hilt, JUnit, etc.
        mavenCentral()
    }
}

// Nombre del proyecto raíz que se muestra en Android Studio y en los reportes de Gradle.
rootProject.name = "PetAdoptionApp"
// Incluye el módulo :app (la aplicación Android) en la construcción.
include(":app")
