package co.edu.uniquindio.petadoptionapp.navigation

import android.R.attr.padding
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.coroutines.launch
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.util.shareImage
import co.edu.uniquindio.petadoptionapp.features.auth.login.LoginScreen
import co.edu.uniquindio.petadoptionapp.features.auth.newpassword.NewPasswordScreen
import co.edu.uniquindio.petadoptionapp.features.auth.recoverpassword.RecoverPasswordScreen
import co.edu.uniquindio.petadoptionapp.features.auth.register.RegisterScreen
import co.edu.uniquindio.petadoptionapp.features.dashboard.DashboardRoutes
import co.edu.uniquindio.petadoptionapp.features.dashboard.MainScreen
import co.edu.uniquindio.petadoptionapp.features.notification.NotificationsScreen
import co.edu.uniquindio.petadoptionapp.features.splash.SplashScreen
import co.edu.uniquindio.petadoptionapp.features.user.post.create.CreatePostScreen
import co.edu.uniquindio.petadoptionapp.features.user.myposts.MyPostsScreen
import co.edu.uniquindio.petadoptionapp.features.user.post.comments.CommentsScreen
import co.edu.uniquindio.petadoptionapp.features.user.post.detail.PostDetailScreen
import co.edu.uniquindio.petadoptionapp.features.user.profile.ProfileScreen
import co.edu.uniquindio.petadoptionapp.features.user.profile.metric.MetricsScreen
import co.edu.uniquindio.petadoptionapp.features.user.profile.edit.EditProfileScreen
import co.edu.uniquindio.petadoptionapp.features.user.profile.settings.SettingsScreen
import co.edu.uniquindio.petadoptionapp.features.user.publicprofile.UserProfileScreen

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

        composable<MainRoutes.Metrics> {
            val context = LocalContext.current
            val shareText = stringResource(R.string.metrics_share_text)
            val scope = rememberCoroutineScope()
            MetricsScreen(
                onBack = { navController.popBackStack() },
                onShare = { screenshot ->
                    if (screenshot != null) {
                        // Captura de las métricas + el texto de invitación.
                        scope.launch { context.shareImage(screenshot, "mis_metricas", shareText) }
                    } else {
                        // Sin captura: se comparte solo el texto.
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, null))
                    }
                }
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
                onNotificationsClick = { navController.navigate(MainRoutes.Notifications) },
                onMetricsClick = { navController.navigate(MainRoutes.Metrics) },
                onSettingsClick = { navController.navigate(MainRoutes.Settings) },
                onEditProfileClick = { navController.navigate(MainRoutes.EditProfile) },
                onUserClick = { userId -> navController.navigate(MainRoutes.UserProfile(userId)) },
                onMyPostsClick = { navController.navigate(MainRoutes.MyPosts) }
            )
        }

        // Perfil de otra persona (sin barra). El userId lo lee el ViewModel de la ruta.
        composable<MainRoutes.UserProfile> {
            val context = LocalContext.current
            UserProfileScreen(
                onBack = { navController.popBackStack() },
                onPostClick = { postId -> navController.navigate(MainRoutes.PostDetail(postId)) },
                onCallClick = { phone ->
                    // ACTION_DIAL solo abre el marcador con el número: no llama ni pide permisos.
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                }
            )
        }

        // Editar perfil (sin barra): al guardar vuelve al perfil, que se actualiza solo.
        composable<MainRoutes.EditProfile> {
            EditProfileScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        // Configuración (sin barra).
        composable<MainRoutes.Settings> {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onAccountDeleted = {
                    navController.navigate(MainRoutes.Login) {
                        // Sin sesión no se puede volver a ninguna pantalla anterior: se limpia toda la pila.
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
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
        // El postId lo lee el ViewModel de la ruta. Cambia según sea del usuario o de otra persona.
        composable<MainRoutes.PostDetail> {
            val context = LocalContext.current
            PostDetailScreen(
                onBack = { navController.popBackStack() },
                onShare = { title ->
                    // Por ahora se comparte solo un texto con el título de la publicación.
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, context.getString(R.string.post_share_text, title))
                    }
                    context.startActivity(Intent.createChooser(intent, null))
                },
                onOpenComments = { postId -> navController.navigate(MainRoutes.PostComments(postId)) },
                // TODO: pantalla de editar publicación (UpdatePostUseCase ya existe).
                onEditPost = { }
            )
        }

        // Todos los comentarios (sin barra).
        composable<MainRoutes.PostComments> {
            CommentsScreen(onBack = { navController.popBackStack() })
        }

        // Mis publicaciones (sin barra).
        composable<MainRoutes.MyPosts> {
            MyPostsScreen(
                onBack = { navController.popBackStack() },
                onPostClick = { postId -> navController.navigate(MainRoutes.PostDetail(postId)) }
            )
        }
    }
}