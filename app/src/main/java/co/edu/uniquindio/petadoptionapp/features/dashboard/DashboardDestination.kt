package co.edu.uniquindio.petadoptionapp.features.dashboard

// Importa el contenedor principal de iconos de Material Design.
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.ui.graphics.vector.ImageVector
import co.edu.uniquindio.petadoptionapp.domain.model.enums.UserRole

// Crea una lista fija (Enum) para definir las opciones del menú de navegación.
enum class DashboardDestination(
    // Propiedad para guardar el texto que se muestra debajo del ícono.
    val label: String,
    // Propiedad para guardar el ícono correspondiente a la opción
    val icon: ImageVector,
    // Roles que pueden ver este item
    val roles: Set<UserRole>,
    // Pantalla a la que lleva. null = no es una pantalla (es el botón "+")
    val route: DashboardRoutes?

) {

    // Lista de opciones para el navigation bar para Usuario
    HOME("Inicio", Icons.Outlined.Home, setOf(UserRole.USUARIO), DashboardRoutes.HomeUser),
    EXPLORE("Explorar", Icons.Outlined.Explore, setOf(UserRole.USUARIO), DashboardRoutes.Explore),
    ADD("", Icons.Filled.Add, setOf(UserRole.USUARIO), null),

    // Lista de opciones para el navigation bar para Monitor
    DASHBOARD("Dashboard", Icons.Outlined.Dashboard, setOf(UserRole.MONITOR), DashboardRoutes.HomeMonitor),
    PUBLICATIONS("Feeds", Icons.Outlined.Storage, setOf(UserRole.MONITOR), DashboardRoutes.Publications),

    // Lista de opciones compartida
    MESSAGES("Mensajes", Icons.Outlined.ChatBubbleOutline, UserRole.entries.toSet(), DashboardRoutes.Messages),
    PROFILE("Perfil", Icons.Outlined.AccountCircle, UserRole.entries.toSet(), DashboardRoutes.Profile)
}