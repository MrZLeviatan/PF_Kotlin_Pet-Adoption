package co.edu.uniquindio.petadoptionapp.features.dashboard

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import co.edu.uniquindio.petadoptionapp.domain.model.users.UserRole

/**
Pantalla contenedora: lee el rol de la sesión y muestra la barra y las pantallas que le corresponden.
 */
@Composable
fun MainScreen(
    onCreatePost: () -> Unit = {},
    onPostClick: (String) -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onMetricsClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onUserClick: (String) -> Unit = {},
    onMyPostsClick: () -> Unit = {},
    onMyLikesClick: () -> Unit = {},
    onChatClick: (chatId: String) -> Unit = {},

    viewModel: DashboardViewModel = hiltViewModel()
) {
    val role by viewModel.role.collectAsStateWithLifecycle()

    // Sin sesión no se dibuja nada: la navegación principal ya lleva al login.
    role?.let { currentRole ->
        // key(rol): si el rol cambia, se reconstruye la navegación interna desde su pantalla de inicio.
        key(currentRole) {
            MainContent(
                role = currentRole,
                onCreatePost = onCreatePost,
                onPostClick = onPostClick,
                onNotificationsClick = onNotificationsClick,
                onMetricsClick = onMetricsClick,
                onSettingsClick = onSettingsClick,
                onEditProfileClick = onEditProfileClick,
                onUserClick = onUserClick,
                onMyPostsClick = onMyPostsClick,
                onMyLikesClick = onMyLikesClick,
                onChatClick = onChatClick
            )
        }
    }
}

// Solo dibuja: recibe el rol ya resuelto. Es lo que se previsualiza.
@Composable
private fun MainContent(
    role: UserRole,
    onCreatePost: () -> Unit,
    onPostClick: (String) -> Unit,
    onNotificationsClick: () -> Unit,
    onMetricsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onUserClick: (String) -> Unit,
    onMyPostsClick: () -> Unit,
    onMyLikesClick: () -> Unit,
    onChatClick: (String) -> Unit = {},
) {
    // Controlador del NavHost interno (cambia de pantalla al tocar la barra).
    val navController = rememberNavController()

    // Items de la barra: solo los que puede ver este rol.
    val items = DashboardDestination.entries.filter { role in it.roles }

    // Pantalla actual (se actualiza sola al navegar).
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Posición del item que corresponde a la pantalla actual.
    val selectedIndex = items.indexOfFirst { item ->
        item.route?.let { route ->
            currentDestination?.hierarchy?.any { it.hasRoute(route::class) } == true
        } == true
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                items = items,
                selectedIndex = selectedIndex,
                onItemClick = { index ->
                    val route = items[index].route
                    if (route == null) {
                        // route null = botón "+": abre crear publicación.
                        onCreatePost()
                    } else {
                        navController.navigate(route) {
                            // Evita que la pila crezca al tocar items repetidamente.
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { padding ->
        DashboardNavigation(
            navController = navController,
            padding = padding,
            role = role,
            onPostClick = onPostClick,
            onNotificationsClick = onNotificationsClick,
            onMetricsClick = onMetricsClick,
            onSettingsClick = onSettingsClick,
            onEditProfileClick = onEditProfileClick,
            onUserClick = onUserClick,
            onMyPostsClick = onMyPostsClick,
            onMyLikesClick = onMyLikesClick,
            onChatClick = onChatClick
        )
    }
}

@Preview(name = "Usuario", showBackground = true)
@Composable
private fun MainUserPreview() {
    PetAdoptionAppTheme { MainContent(UserRole.USER, {}, {}, {}, {}, {}, {}, {}, {}, {}) }
}

@Preview(name = "Moderador", showBackground = true)
@Composable
private fun MainModeratorPreview() {
    PetAdoptionAppTheme { MainContent(UserRole.MODERATOR, {}, {}, {}, {}, {}, {}, {}, {}, {}) }
}