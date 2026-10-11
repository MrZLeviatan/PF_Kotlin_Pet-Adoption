package co.edu.uniquindio.petadoptionapp.features.dashboard

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

// Importación de librerías para la navegación
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState

import androidx.compose.ui.tooling.preview.Preview
import co.edu.uniquindio.petadoptionapp.domain.model.enums.UserRole


// Pantalla contenedora: junta el contenido con la barra de navegación inferior
@Composable
fun MainScreen(
    // Rol del usuario. Por defecto Usuario
    role: UserRole = UserRole.USUARIO,
    // Se ejecuta al tocar el "+". Lo resuelve AppNavigation
    onCreatePost: () -> Unit = {}
) {

    // Controlador del NavHost interno (el que cambia de pantalla al tocar la barra)
    val navController = rememberNavController()

    // Items de la barra: solo los que puede ver este rol
    val items = DashboardDestination.entries.filter { role in it.roles }

    // Pantalla en la que está el usuario ahora mismo (se actualiza sola al navegar)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Posición del item que corresponde a la pantalla actual
    val selectedIndex = items.indexOfFirst { item ->
        item.route?.let { route ->
            currentDestination?.hierarchy?.any { it.hasRoute(route::class) } == true
        } == true
    }

    // Scaffold = esqueleto de la pantalla (barra inferior + contenido)
    Scaffold(
        // Aquí va la barra inferior
        bottomBar = {
            BottomNavigationBar(
                items = items,
                selectedIndex = selectedIndex,
                // Al tocar un item, lo marca como seleccionado
                onItemClick = { index ->
                    val route = items[index].route

                    if (route == null) {
                        // route null = es el botón "+": abre crear publicación
                        onCreatePost()
                    } else {
                        // Es un item normal: navega a su pantalla
                        navController.navigate(route) {
                            // Evita que la pila crezca cada vez que tocas un item
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true   // No crea otra copia si ya estás en esa pantalla
                            restoreState = true      // Recupera el estado al volver a una pantalla
                        }
                    }
                }
            )
        }
    ) { padding ->
        // Contenido que cambia según el item tocado
        DashboardNavigation(
            navController = navController,
            padding = padding,
            role = role
        )
    }
}

// Preview para ver la pantalla completa en Android Studio
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}