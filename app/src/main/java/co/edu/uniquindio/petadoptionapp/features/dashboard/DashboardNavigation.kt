package co.edu.uniquindio.petadoptionapp.features.dashboard

import co.edu.uniquindio.petadoptionapp.features.moderator.home.ModeratorHomeScreen
import co.edu.uniquindio.petadoptionapp.features.moderator.profile.ModeratorProfileScreen
import co.edu.uniquindio.petadoptionapp.features.moderator.stats.ModeratorStatsScreen
import androidx.navigation.NavGraph.Companion.findStartDestination
import co.edu.uniquindio.petadoptionapp.features.moderator.reviews.MyReviewsScreen
import co.edu.uniquindio.petadoptionapp.features.user.chat.list.ChatListScreen
import co.edu.uniquindio.petadoptionapp.features.user.explore.ExploreScreen
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

/**
Grafo de navegación interno del dashboard (las pantallas que viven dentro de la barra inferior).
Recibe el NavHostController creado en MainScreen, el padding del Scaffold (para no quedar tapado
por la barra) y el rol de la sesión, que define la pantalla de inicio y la versión del perfil.
No tiene estado propio ni usa casos de uso: cada pantalla registrada tiene su propio ViewModel.
Los callbacks (onPostClick, onNotificationsClick, onChatClick, etc.) se reenvían hacia arriba para
que AppNavigation navegue a pantallas que están fuera del dashboard (detalle, chat, ajustes...).
**/
// Rutas internas del dashboard según el rol.
@Composable
fun DashboardNavigation(
    // Controlador del NavHost interno, compartido con la barra inferior.
    navController: NavHostController,
    // Espacio reservado por el Scaffold (barra inferior) que deben respetar las pantallas.
    padding: PaddingValues,
    // Rol de la sesión: decide la pantalla de inicio y el perfil a mostrar.
    role: UserRole,
    // Evento al abrir una publicación (recibe su id).
    onPostClick: (String) -> Unit = {},
    // Evento al abrir las notificaciones.
    onNotificationsClick: () -> Unit = {},
    // Evento al abrir el perfil de otro usuario (recibe su id).
    onUserClick: (String) -> Unit = {},
    // Evento al abrir "mis publicaciones".
    onMyPostsClick: () -> Unit = {},
    // Evento al abrir "mis me gusta".
    onMyLikesClick: () -> Unit = {},
    // Evento al abrir editar perfil.
    onEditProfileClick: () -> Unit = {},
    // Evento al abrir ajustes.
    onSettingsClick: () -> Unit = {},
    // Evento al abrir las métricas del usuario.
    onMetricsClick: () -> Unit,
    // Evento al abrir una conversación (recibe el id del chat).
    onChatClick: (chatId: String) -> Unit = {},
) {
    // Contenedor de navegación que intercambia las pantallas internas.
    NavHost(
        // Controlador que gestiona la pila de pantallas.
        navController = navController,
        // Cada rol arranca en su propia pantalla de inicio.
        startDestination = when (role) {
            // El usuario empieza en su feed.
            UserRole.USER -> DashboardRoutes.HomeUser
            // El moderador empieza en su pantalla de inicio.
            UserRole.MODERATOR -> DashboardRoutes.HomeMonitor
        }
    ) {
        // Usuario

        // Registra la pantalla de inicio del usuario.
        composable<DashboardRoutes.HomeUser> {
            // Dibuja el feed y reenvía sus eventos de navegación.
            HomeUserScreen(
                padding = padding,
                onPostClick = onPostClick,
                onUserClick = onUserClick,
                onNotificationsClick = onNotificationsClick
            )
        }
        // Registra la pantalla de explorar.
        composable<DashboardRoutes.Explore> { ExploreScreen(padding = padding, onPostClick = onPostClick) }

        // Moderador

        // Registra la pantalla de inicio del moderador.
        composable<DashboardRoutes.HomeMonitor> {
            // Dibuja el inicio del moderador y reenvía sus eventos de navegación.
            ModeratorHomeScreen(
                padding = padding,
                onPostClick = onPostClick,
                onUserClick = onUserClick,
                onNotificationsClick = onNotificationsClick
            )
        }
        // Registra la pantalla de publicaciones revisadas por el moderador.
        composable<DashboardRoutes.Publications> { MyReviewsScreen(padding = padding, onPostClick = onPostClick) }

        // Compartidas

        // Registra la lista de conversaciones.
        composable<DashboardRoutes.Messages> { ChatListScreen(padding = padding, onChatClick = onChatClick) }
        // Moderador: su dashboard de desempeño ocupa el lugar de Mensajes.
        composable<DashboardRoutes.ModeratorStats> { ModeratorStatsScreen(padding = padding) }

        // Cada rol tiene su perfil: el moderador no tiene editar perfil, me gusta ni publicaciones.
        composable<DashboardRoutes.Profile> {
            // Elige la pantalla de perfil según el rol.
            when (role) {
                // Perfil del usuario con todas sus opciones.
                UserRole.USER -> ProfileScreen(
                    onMyPostsClick = onMyPostsClick,
                    onMyLikesClick = onMyLikesClick,
                    onEditProfileClick = onEditProfileClick,
                    onSettingsClick = onSettingsClick,
                    onMetricsClick = onMetricsClick,

                    // Evita que la barra inferior tape el contenido.
                    modifier = Modifier.padding(padding)
                )
                // Perfil reducido del moderador.
                UserRole.MODERATOR -> ModeratorProfileScreen(
                    // Lleva a la pestaña del dashboard, igual que tocarla en la barra.
                    onStatsClick = {
                        // Navega a la ruta de estadísticas dentro del NavHost interno.
                        navController.navigate(DashboardRoutes.ModeratorStats) {
                            // Vuelve a la pantalla de inicio guardando el estado de la pestaña actual.
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            // Evita apilar dos copias de la misma pantalla.
                            launchSingleTop = true
                            // Restaura el estado guardado de la pestaña destino.
                            restoreState = true
                        }
                    },
                    // Reenvía el evento de abrir ajustes.
                    onSettingsClick = onSettingsClick,
                    // Evita que la barra inferior tape el contenido.
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

// Pantalla temporal; se elimina cuando existan las reales.
@Composable
private fun PlaceholderScreen(name: String, padding: PaddingValues) {
    // Caja que ocupa toda la pantalla, respeta el padding y centra su contenido.
    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        // Muestra el nombre de la pantalla pendiente.
        Text(name)
    }
}
