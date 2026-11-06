package co.edu.uniquindio.petadoptionapp.features.dashboard

// Importa la anotación para permitir que las clases sean serializable con Kotlinx Serialization.
import kotlinx.serialization.Serializable

/**
Rutas internas del dashboard (las pantallas que se muestran con la barra de navegación inferior).
Son rutas tipadas de Navigation Compose: cada data object identifica una pantalla y se usa en
DashboardNavigation (composable<...>) y en DashboardDestination (opción de la barra que lleva a ella).
Algunas rutas son exclusivas del usuario, otras del moderador y otras compartidas por ambos roles.
**/
// Anotación que indica que todas las subclases de esta clase sellada pueden serializarse para Jetpack Navigation.
@Serializable
// Define una clase sellada (sealed class) que agrupa las rutas internas de las pantallas del dashboard con barra inferior.
sealed class DashboardRoutes {

    // Rutas exclusivas para el rol de usuario

    // Ruta de la pantalla de inicio del usuario (feed de publicaciones).
    @Serializable data object HomeUser : DashboardRoutes()

    // Ruta de la pantalla de explorar (búsqueda y mapa de publicaciones).
    @Serializable data object Explore : DashboardRoutes()


    // Rutas exclusivas para el rol de Monitor

    // Ruta de la pantalla de inicio del moderador.
    @Serializable data object HomeMonitor : DashboardRoutes()
    // Ruta de las publicaciones revisadas por el moderador.
    @Serializable data object Publications : DashboardRoutes()
    // Dashboard de desempeño del moderador (reemplaza a Mensajes para este rol).
    @Serializable data object ModeratorStats : DashboardRoutes()


    // Rutas compartidas entre ambos roles

    // Ruta de la lista de conversaciones (en la barra solo la ve el usuario).
    @Serializable data object Messages : DashboardRoutes()
    // Ruta del perfil; cada rol ve su propia versión.
    @Serializable data object Profile : DashboardRoutes()
}
