package co.edu.uniquindio.petadoptionapp.features.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import co.edu.uniquindio.petadoptionapp.domain.model.users.UserRole
import co.edu.uniquindio.petadoptionapp.features.user.home.HomeUserScreen
import co.edu.uniquindio.petadoptionapp.features.user.profile.ProfileScreen

// Rutas internas del dashboard según el rol.
@Composable
fun DashboardNavigation(
    navController: NavHostController,
    padding: PaddingValues,
    role: UserRole,
    onPostClick: (String) -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onUserClick: (String) -> Unit = {},
    onMyPostsClick: () -> Unit = {},
    onMyLikesClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        // Cada rol arranca en su propia pantalla de inicio.
        startDestination = when (role) {
            UserRole.USER -> DashboardRoutes.HomeUser
            UserRole.MODERATOR -> DashboardRoutes.HomeMonitor
        }
    ) {
        // Usuario
        composable<DashboardRoutes.HomeUser> {
            HomeUserScreen(padding = padding, onPostClick = onPostClick, onNotificationsClick = onNotificationsClick)
        }
        composable<DashboardRoutes.Explore> { PlaceholderScreen("Explore", padding) }

        // Moderador
        composable<DashboardRoutes.HomeMonitor> { PlaceholderScreen("HomeMonitor", padding) }
        composable<DashboardRoutes.Publications> { PlaceholderScreen("Publications", padding) }

        // Compartidas
        composable<DashboardRoutes.Messages> { PlaceholderScreen("Messages", padding) }
        composable<DashboardRoutes.Profile> { PlaceholderScreen("Profile", padding) }

        composable<DashboardRoutes.Profile> {
            ProfileScreen(
                onMyPostsClick = onMyPostsClick,
                onMyLikesClick = onMyLikesClick,
                onEditProfileClick = onEditProfileClick,
                onSettingsClick = onSettingsClick,
                modifier = Modifier.padding(padding)   // evita que la barra inferior tape el contenido
            )
        }
    }
}

// Pantalla temporal; se elimina cuando existan las reales.
@Composable
private fun PlaceholderScreen(name: String, padding: PaddingValues) {
    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        Text(name)
    }
}