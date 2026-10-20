package co.edu.uniquindio.petadoptionapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import co.edu.uniquindio.petadoptionapp.domain.model.enums.UserRole
import co.edu.uniquindio.petadoptionapp.features.dashboard.MainScreen
import co.edu.uniquindio.petadoptionapp.features.user.post.create.CreatePostScreen
import co.edu.uniquindio.petadoptionapp.features.user.post.detail.PostDetailScreen
import co.edu.uniquindio.petadoptionapp.features.splash.SplashScreen

// Componente raíz de navegación encargada de configurar las rutas principales de toda la aplicación.
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MainRoutes.Splash
    ) {

        // Pantalla de carga
        composable<MainRoutes.Splash> {
            SplashScreen(
                onFinished = {
                    navController.navigate(MainRoutes.Main(role = UserRole.USUARIO)) {
                        popUpTo<MainRoutes.Splash> { inclusive = true }
                    }
                }
            )
        }   // ← AQUÍ se cierra Splash

        // Dashboard con la barra inferior (ahora al mismo nivel que Splash)
        composable<MainRoutes.Main> {
            val args = it.toRoute<MainRoutes.Main>()
            MainScreen(
                role = args.role,
                onCreatePost = { navController.navigate(MainRoutes.CreatePost) },
                onPostClick = { postId -> navController.navigate(MainRoutes.PostDetail(postId)) }
            )
        }

        // Crear publicación (sin barra)
        composable<MainRoutes.CreatePost> {
            CreatePostScreen(
                onBack = { navController.popBackStack() },
                onViewPost = { navController.popBackStack() },
                onGoHome = { navController.popBackStack() }
            )
        }

        // Detalle de la publicación (sin barra)
        composable<MainRoutes.PostDetail> {
            val args = it.toRoute<MainRoutes.PostDetail>()
            PostDetailScreen(
                postId = args.postId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}