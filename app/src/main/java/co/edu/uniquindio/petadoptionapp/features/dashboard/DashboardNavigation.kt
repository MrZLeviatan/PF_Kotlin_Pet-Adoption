package co.edu.uniquindio.petadoptionapp.features.dashboard

import co.edu.uniquindio.petadoptionapp.features.moderator.home.ModeratorHomeScreen
import co.edu.uniquindio.petadoptionapp.features.moderator.profile.ModeratorProfileScreen
import co.edu.uniquindio.petadoptionapp.features.moderator.stats.ModeratorStatsScreen
import androidx.navigation.NavGraph.Companion.findStartDestination
import co.edu.uniquindio.petadoptionapp.features.moderator.reviews.MyReviewsScreen
import co.edu.uniquindio.petadoptionapp.features.user.chat.list.ChatListScreen
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
    onSettingsClick: () -> Unit = {},
    onMetricsClick: () -> Unit,
    onChatClick: (chatId: String) -> Unit = {},
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
            HomeUserScreen(
                padding = padding,
                onPostClick = onPostClick,
                onUserClick = onUserClick,
                onNotificationsClick = onNotificationsClick
            )
        }
        composable<DashboardRoutes.Explore> { PlaceholderScreen("Explore", padding) }

        // Moderador
        composable<DashboardRoutes.HomeMonitor> {
            ModeratorHomeScreen(
                padding = padding,
                onPostClick = onPostClick,
                onUserClick = onUserClick,
                onNotificationsClick = onNotificationsClick
            )
        }
        composable<DashboardRoutes.Publications> { MyReviewsScreen(padding = padding, onPostClick = onPostClick) }

        // Compartidas
        composable<DashboardRoutes.Messages> { ChatListScreen(padding = padding, onChatClick = onChatClick) }
        // Moderador: su dashboard de desempeño ocupa el lugar de Mensajes.
        composable<DashboardRoutes.ModeratorStats> { ModeratorStatsScreen(padding = padding) }

        // Cada rol tiene su perfil: el moderador no tiene editar perfil, me gusta ni publicaciones.
        composable<DashboardRoutes.Profile> {
            when (role) {
                UserRole.USER -> ProfileScreen(
                    onMyPostsClick = onMyPostsClick,
                    onMyLikesClick = onMyLikesClick,
                    onEditProfileClick = onEditProfileClick,
                    onSettingsClick = onSettingsClick,
                    onMetricsClick = onMetricsClick,

                    modifier = Modifier.padding(padding)   // evita que la barra inferior tape el contenido
                )
                UserRole.MODERATOR -> ModeratorProfileScreen(
                    // Lleva a la pestaña del dashboard, igual que tocarla en la barra.
                    onStatsClick = {
                        navController.navigate(DashboardRoutes.ModeratorStats) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onSettingsClick = onSettingsClick,
                    modifier = Modifier.padding(padding)
                )
            }
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