package co.edu.uniquindio.petadoptionapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import co.edu.uniquindio.petadoptionapp.domain.model.enums.UserRole
import co.edu.uniquindio.petadoptionapp.features.dashboard.MainScreen
import co.edu.uniquindio.petadoptionapp.features.user.post.create.CreatePostScreen

// Componente raíz de navegación encargada de configurar las rutas principales de toda la aplicación.
@Composable
fun AppNavigation() {
    // Crea y recuerda la instancia del controlador principal del NavHost para gestionar la pila de pantallas.
    val navController = rememberNavController()

    // Contenedor principal de navegación que define el destino inicial de la app.
    NavHost(
        navController = navController,
        // Define el destino inicial como la pantalla principal (Main), pasando el rol (permite cambiar a MONITOR para pruebas).
        startDestination = MainRoutes.Main(role = UserRole.USUARIO)
    ) {

        // Destino para el Dashboard principal con la barra inferior de navegación.
        composable<MainRoutes.Main> {
            // Lee los argumentos serializados que llegaron en la ruta, específicamente el rol del usuario.
            val args = it.toRoute<MainRoutes.Main>()
            MainScreen(
                role = args.role,
                // Al tocar el botón central "+", navega a la pantalla de crear publicación colocándola encima del dashboard.
                onCreatePost = { navController.navigate(MainRoutes.CreatePost) }
            )
        }

        // Destino para la pantalla de creación de publicaciones (se muestra a pantalla completa sin barra inferior).
        composable<MainRoutes.CreatePost> {
            CreatePostScreen(
                // Configura los tres eventos de salida para regresar al dashboard anterior haciendo pop en la pila.
                onBack = { navController.popBackStack() },
                onViewPost = { navController.popBackStack() },
                onGoHome = { navController.popBackStack() }
            )
        }
    }
}