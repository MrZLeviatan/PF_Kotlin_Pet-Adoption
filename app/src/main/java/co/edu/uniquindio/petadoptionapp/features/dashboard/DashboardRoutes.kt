package co.edu.uniquindio.petadoptionapp.features.dashboard

// Importa la anotación para permitir que las clases sean serializable con Kotlinx Serialization.
import kotlinx.serialization.Serializable

// Anotación que indica que todas las subclases de esta clase sellada pueden serializarse para Jetpack Navigation.
@Serializable
// Define una clase sellada (sealed class) que agrupa las rutas internas de las pantallas del dashboard con barra inferior.
sealed class DashboardRoutes {

    // Rutas exclusivas para el rol de usuario

    @Serializable data object HomeUser : DashboardRoutes()

    @Serializable data object Explore : DashboardRoutes()


    // Rutas exclusivas para el rol de Monitor

    @Serializable data object HomeMonitor : DashboardRoutes()
    @Serializable data object Publications : DashboardRoutes()
    // Dashboard de desempeño del moderador (reemplaza a Mensajes para este rol).
    @Serializable data object ModeratorStats : DashboardRoutes()


    // Rutas compartidas entre ambos roles

    @Serializable data object Messages : DashboardRoutes()
    @Serializable data object Profile : DashboardRoutes()
}