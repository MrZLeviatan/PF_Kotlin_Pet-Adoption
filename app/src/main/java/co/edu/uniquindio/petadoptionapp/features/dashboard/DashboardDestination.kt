package co.edu.uniquindio.petadoptionapp.features.dashboard

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.ui.graphics.vector.ImageVector
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.domain.model.users.UserRole

// Opciones de la barra de navegación inferior y qué rol puede ver cada una.
enum class DashboardDestination(
    // Id del texto en strings.xml que se muestra bajo el ícono.
    @StringRes val labelRes: Int,
    // Ícono de la opción.
    val icon: ImageVector,
    // Roles que pueden ver este item.
    val roles: Set<UserRole>,
    // Pantalla a la que lleva; null = no es una pantalla (botón "+").
    val route: DashboardRoutes?
) {
    // Solo usuario
    HOME(R.string.nav_home, Icons.Outlined.Home, setOf(UserRole.USER), DashboardRoutes.HomeUser),
    EXPLORE(R.string.nav_explore, Icons.Outlined.Explore, setOf(UserRole.USER), DashboardRoutes.Explore),
    ADD(R.string.nav_create_description, Icons.Filled.Add, setOf(UserRole.USER), null),

    // Solo moderador
    DASHBOARD(R.string.nav_dashboard, Icons.Outlined.Dashboard, setOf(UserRole.MODERATOR), DashboardRoutes.HomeMonitor),
    PUBLICATIONS(R.string.nav_feeds, Icons.Outlined.Storage, setOf(UserRole.MODERATOR), DashboardRoutes.Publications),

    // Mensajes es del usuario; en su lugar el moderador tiene su dashboard de desempeño.
    MESSAGES(R.string.nav_messages, Icons.Outlined.ChatBubbleOutline, setOf(UserRole.USER), DashboardRoutes.Messages),
    STATS(R.string.nav_stats, Icons.Outlined.Insights, setOf(UserRole.MODERATOR), DashboardRoutes.ModeratorStats),

    // Compartidas
    PROFILE(R.string.nav_profile, Icons.Outlined.AccountCircle, UserRole.entries.toSet(), DashboardRoutes.Profile)
}