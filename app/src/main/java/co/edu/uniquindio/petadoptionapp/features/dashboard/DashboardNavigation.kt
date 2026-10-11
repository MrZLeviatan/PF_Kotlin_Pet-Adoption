package co.edu.uniquindio.petadoptionapp.features.dashboard

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import co.edu.uniquindio.petadoptionapp.domain.model.enums.UserRole
import co.edu.uniquindio.petadoptionapp.features.user.home.HomeUserScreen

// Componente Composable encargado de gestionar las rutas y pantallas internas del dashboard según el rol del usuario.
@Composable
fun DashboardNavigation(
    // Controlador de navegación principal que permite cambiar entre las pantallas del dashboard.
    navController: NavHostController,
    // Espacio de relleno (padding) proporcionado para evitar que el contenido se solape con la barra inferior.
    padding: PaddingValues,
    // Rol actual del usuario logueado para determinar su pantalla de inicio.
    role: UserRole
) {
    // Componente contenedor que configura la navegación basada en destinos y rutas serializables.
    NavHost(
        navController = navController,
        // Define la pantalla inicial (punto de partida) evaluando dinámicamente el rol del usuario.
        startDestination = when (role) {
            // Si el rol es usuario estándar, arranca en la pantalla principal de usuario (HomeUser).
            UserRole.USUARIO -> DashboardRoutes.HomeUser
            // Si el rol es monitor/moderador, arranca en la pantalla principal de monitor (HomeMonitor).
            UserRole.MONITOR -> DashboardRoutes.HomeMonitor
        }
    ) {
        // Pantalla de inicio para Usuario
        composable<DashboardRoutes.HomeUser> {
            HomeUserScreen(padding = padding)
        }
    }
}