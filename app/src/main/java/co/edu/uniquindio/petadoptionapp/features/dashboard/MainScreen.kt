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
Pantalla contenedora del dashboard: lee el rol de la sesión y muestra la barra y las pantallas que le corresponden.
Estado: observa DashboardViewModel.role (que proviene de ObserveSessionUseCase) con
collectAsStateWithLifecycle. Si no hay rol (sin sesión) no dibuja nada.
Eventos: los callbacks que recibe (crear publicación, abrir publicación, notificaciones, métricas,
ajustes, chat, etc.) los entrega AppNavigation para navegar a pantallas fuera del dashboard.
La navegación entre pestañas se hace con un NavHost interno (DashboardNavigation) controlado
por la barra inferior (BottomNavigationBar).
**/
// Marca la función como composable.
@Composable
fun MainScreen(
    // Evento del botón "+": abrir la pantalla de crear publicación.
    onCreatePost: () -> Unit = {},
    // Evento al abrir una publicación (recibe su id).
    onPostClick: (String) -> Unit = {},
    // Evento al abrir las notificaciones.
    onNotificationsClick: () -> Unit = {},
    // Evento al abrir las métricas del usuario.
    onMetricsClick: () -> Unit,
    // Evento al abrir ajustes.
    onSettingsClick: () -> Unit = {},
    // Evento al abrir editar perfil.
    onEditProfileClick: () -> Unit = {},
    // Evento al abrir el perfil de otro usuario (recibe su id).
    onUserClick: (String) -> Unit = {},
    // Evento al abrir "mis publicaciones".
    onMyPostsClick: () -> Unit = {},
    // Evento al abrir "mis me gusta".
    onMyLikesClick: () -> Unit = {},
    // Evento al abrir una conversación (recibe el id del chat).
    onChatClick: (chatId: String) -> Unit = {},

    // ViewModel inyectado por Hilt que expone el rol de la sesión.
    viewModel: DashboardViewModel = hiltViewModel()
) {
    // Observa el rol respetando el ciclo de vida (deja de recolectar en segundo plano).
    val role by viewModel.role.collectAsStateWithLifecycle()

    // Sin sesión no se dibuja nada: la navegación principal ya lleva al login.
    role?.let { currentRole ->
        // key(rol): si el rol cambia, se reconstruye la navegación interna desde su pantalla de inicio.
        key(currentRole) {
            // Dibuja el contenido con el rol ya resuelto y reenvía todos los eventos.
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
    // Rol de la sesión.
    role: UserRole,
    // Evento del botón "+".
    onCreatePost: () -> Unit,
    // Evento al abrir una publicación.
    onPostClick: (String) -> Unit,
    // Evento al abrir notificaciones.
    onNotificationsClick: () -> Unit,
    // Evento al abrir métricas.
    onMetricsClick: () -> Unit,
    // Evento al abrir ajustes.
    onSettingsClick: () -> Unit,
    // Evento al abrir editar perfil.
    onEditProfileClick: () -> Unit,
    // Evento al abrir el perfil de otro usuario.
    onUserClick: (String) -> Unit,
    // Evento al abrir "mis publicaciones".
    onMyPostsClick: () -> Unit,
    // Evento al abrir "mis me gusta".
    onMyLikesClick: () -> Unit,
    // Evento al abrir una conversación.
    onChatClick: (String) -> Unit = {},
) {
    // Controlador del NavHost interno (cambia de pantalla al tocar la barra).
    val navController = rememberNavController()

    // Items de la barra: solo los que puede ver este rol.
    val items = DashboardDestination.entries.filter { role in it.roles }

    // Pantalla actual (se actualiza sola al navegar).
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    // Destino actual dentro del grafo interno (null mientras no hay ninguno).
    val currentDestination = navBackStackEntry?.destination

    // Posición del item que corresponde a la pantalla actual.
    val selectedIndex = items.indexOfFirst { item ->
        // Solo los items con ruta pueden estar seleccionados; el botón "+" queda fuera.
        item.route?.let { route ->
            // Comprueba si el destino actual (o alguno de sus padres) corresponde a la ruta del item.
            currentDestination?.hierarchy?.any { it.hasRoute(route::class) } == true
        } == true
    }

    // Estructura base de Material con la barra inferior.
    Scaffold(
        // Ranura de la barra inferior.
        bottomBar = {
            // Dibuja la barra con los items del rol y el seleccionado.
            BottomNavigationBar(
                items = items,
                selectedIndex = selectedIndex,
                // Maneja el toque de un item de la barra.
                onItemClick = { index ->
                    // Obtiene la ruta del item tocado.
                    val route = items[index].route
                    // Comprueba si el item no tiene ruta interna.
                    if (route == null) {
                        // route null = botón "+": abre crear publicación.
                        onCreatePost()
                    } else {
                        // Navega a la pestaña correspondiente dentro del NavHost interno.
                        navController.navigate(route) {
                            // Evita que la pila crezca al tocar items repetidamente.
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            // No crea otra copia si ya está en esa pestaña.
                            launchSingleTop = true
                            // Restaura el estado (scroll, etc.) de la pestaña destino.
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { padding ->
        // Contenido: el grafo interno con el padding del Scaffold para no quedar bajo la barra.
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

// Vista previa del dashboard con el rol usuario y callbacks vacíos.
@Preview(name = "Usuario", showBackground = true)
@Composable
private fun MainUserPreview() {
    // Aplica el tema y dibuja el contenido sin ViewModel.
    PetAdoptionAppTheme { MainContent(UserRole.USER, {}, {}, {}, {}, {}, {}, {}, {}, {}) }
}

// Vista previa del dashboard con el rol moderador y callbacks vacíos.
@Preview(name = "Moderador", showBackground = true)
@Composable
private fun MainModeratorPreview() {
    // Aplica el tema y dibuja el contenido sin ViewModel.
    PetAdoptionAppTheme { MainContent(UserRole.MODERATOR, {}, {}, {}, {}, {}, {}, {}, {}, {}) }
}
