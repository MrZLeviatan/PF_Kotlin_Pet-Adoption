package co.edu.uniquindio.petadoptionapp.navigation

import android.R.attr.padding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import co.edu.uniquindio.petadoptionapp.features.auth.login.LoginScreen
import co.edu.uniquindio.petadoptionapp.features.auth.newpassword.NewPasswordScreen
import co.edu.uniquindio.petadoptionapp.features.auth.recoverpassword.RecoverPasswordScreen
import co.edu.uniquindio.petadoptionapp.features.auth.register.RegisterScreen
import co.edu.uniquindio.petadoptionapp.features.dashboard.DashboardRoutes
import co.edu.uniquindio.petadoptionapp.features.dashboard.MainScreen
import co.edu.uniquindio.petadoptionapp.features.notification.NotificationsScreen
import co.edu.uniquindio.petadoptionapp.features.splash.SplashScreen
import co.edu.uniquindio.petadoptionapp.features.user.post.create.CreatePostScreen
import co.edu.uniquindio.petadoptionapp.features.user.post.detail.PostDetailScreen
import co.edu.uniquindio.petadoptionapp.features.user.profile.ProfileScreen

// Componente raíz de navegación encargada de configurar las rutas principales de toda la aplicación.
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MainRoutes.Splash
    ) {

        // Pantalla de carga: al terminar, va al login.
        composable<MainRoutes.Splash> {
            SplashScreen(
                onFinished = {
                    navController.navigate(MainRoutes.Login) {
                        // Saca el splash de la pila: "atrás" no debe volver a él.
                        popUpTo<MainRoutes.Splash> { inclusive = true }
                    }
                }
            )
        }



        composable<MainRoutes.Login> {
            LoginScreen(
                onLoginSuccess = {
                    // El rol (usuario o moderador) lo lee MainScreen de la sesión.
                    navController.navigate(MainRoutes.Main) {
                        // Saca el login de la pila: "atrás" no debe volver a él.
                        popUpTo<MainRoutes.Login> { inclusive = true }
                    }
                },
                // TODO: cuando exista la ruta de A2F → navController.navigate(MainRoutes.TwoFactor(accountId))
                onTwoFactorRequired = { accountId -> },
                onForgotPasswordClick = { navController.navigate(MainRoutes.RecoverPassword) },
                onRegisterClick = { navController.navigate(MainRoutes.Register) },
            )
        }

        composable<MainRoutes.Register> {
            RegisterScreen(
                onBackToLogin = { navController.popBackStack() },
                // Tras registrarse vuelve al login (el Login sigue en la pila).
                onRegisterSuccess = { navController.popBackStack() },
                onTermsClick = { /* pendiente: pantalla de términos */ },
                onPoliciesClick = { /* pendiente: pantalla de políticas */ },
            )
        }

        composable<MainRoutes.Notifications> {
            NotificationsScreen(
                onBack = { navController.popBackStack() },
                onOpenPost = { postId -> navController.navigate(MainRoutes.PostDetail(postId)) }
            )
        }

        composable<MainRoutes.RecoverPassword> {
            RecoverPasswordScreen(
                onBack = { navController.popBackStack() },
                // El correo ya salió: vuelve al login y el usuario sigue desde su bandeja.
                onEmailSent = { navController.popBackStack() },
            )
        }



        composable<MainRoutes.NewPassword> {
            NewPasswordScreen(
                onBack = { navController.popBackStack() },
                onPasswordUpdated = {
                    navController.navigate(MainRoutes.Login) {
                        // Limpia la pila: "atrás" no debe volver a esta pantalla.
                        popUpTo<MainRoutes.NewPassword> { inclusive = true }
                    }
                },
            )
        }

        // Dashboard con la barra inferior: el rol sale de la sesión.
        composable<MainRoutes.Main> {
            MainScreen(
                onCreatePost = { navController.navigate(MainRoutes.CreatePost) },
                onPostClick = { postId -> navController.navigate(MainRoutes.PostDetail(postId)) },
                onNotificationsClick = { navController.navigate(MainRoutes.Notifications) }
            )
        }

        // Crear publicación (sin barra).
        composable<MainRoutes.CreatePost> {
            CreatePostScreen(
                onBack = { navController.popBackStack() },
                onViewPost = { navController.popBackStack() },
                onGoHome = { navController.popBackStack() }
            )
        }

        // Detalle de la publicación (sin barra).
        composable<MainRoutes.PostDetail> {
            val args = it.toRoute<MainRoutes.PostDetail>()
            PostDetailScreen(
                postId = args.postId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}