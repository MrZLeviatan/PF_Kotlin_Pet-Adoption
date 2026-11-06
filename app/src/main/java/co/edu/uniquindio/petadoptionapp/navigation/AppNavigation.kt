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
import co.edu.uniquindio.petadoptionapp.features.user.likes.MyLikesScreen
import co.edu.uniquindio.petadoptionapp.features.user.myposts.MyPostsScreen
import co.edu.uniquindio.petadoptionapp.features.user.post.comments.CommentsScreen
import co.edu.uniquindio.petadoptionapp.features.user.post.detail.PostDetailScreen
import co.edu.uniquindio.petadoptionapp.features.user.profile.ProfileScreen
import co.edu.uniquindio.petadoptionapp.features.user.profile.metric.MetricsScreen
import co.edu.uniquindio.petadoptionapp.features.moderator.rejectpost.RejectPostScreen
import co.edu.uniquindio.petadoptionapp.features.user.chat.conversation.ChatScreen
import co.edu.uniquindio.petadoptionapp.features.user.profile.edit.EditProfileScreen
import co.edu.uniquindio.petadoptionapp.features.user.profile.settings.SettingsScreen
import co.edu.uniquindio.petadoptionapp.features.user.publicprofile.UserProfileScreen

/**
Grafo de navegación raíz de la aplicación. Lo monta MainActivity dentro del tema y es quien
decide qué pantalla de la capa de presentación (features/) se muestra en cada momento.
Registra en un NavHost cada ruta de MainRoutes con su pantalla Compose y conecta los
callbacks de cada pantalla (volver, abrir detalle, abrir chat...) con acciones del
NavController. Las pantallas no se conocen entre sí: solo emiten eventos y aquí se traducen
en navegación. Los argumentos de las rutas (postId, userId, chatId) no se pasan a las
pantallas: sus ViewModels los leen directamente de la ruta mediante SavedStateHandle.
También resuelve acciones que salen de la app (compartir con ACTION_SEND, marcar con ACTION_DIAL).
**/
// Componente raíz de navegación encargada de configurar las rutas principales de toda la aplicación.
@Composable
fun AppNavigation() {
    // Crea y recuerda el controlador de navegación, que guarda la pila de pantallas y sobrevive a recomposiciones.
    val navController = rememberNavController()

    // Contenedor que muestra la pantalla de la ruta actual y define el grafo de destinos.
    NavHost(
        // Controlador que gobierna este grafo.
        navController = navController,
        // La app siempre arranca en el splash, que decide a dónde ir según la sesión.
        startDestination = MainRoutes.Splash
    ) {

        // Pantalla de carga: con sesión guardada va directo al inicio; si no, al login.
        composable<MainRoutes.Splash> {
            // Muestra el splash y espera a que termine de comprobar la sesión.
            SplashScreen(
                // Recibe si existe una sesión activa cuando el splash termina.
                onFinished = { hasSession ->
                    // Elige el destino: el dashboard si hay sesión o el login si no la hay.
                    val destination: MainRoutes = if (hasSession) MainRoutes.Main else MainRoutes.Login
                    // Navega al destino elegido.
                    navController.navigate(destination) {
                        // Saca el splash de la pila: "atrás" no debe volver a él.
                        popUpTo<MainRoutes.Splash> { inclusive = true }
                    }
                }
            )
        }



        // Pantalla de inicio de sesión.
        composable<MainRoutes.Login> {
            // Muestra el formulario de login y define a dónde lleva cada acción.
            LoginScreen(
                // Se ejecuta cuando las credenciales (y la A2F, si aplica) son correctas.
                onLoginSuccess = {
                    // El rol (usuario o moderador) lo lee MainScreen de la sesión.
                    navController.navigate(MainRoutes.Main) {
                        // Saca el login de la pila: "atrás" no debe volver a él.
                        popUpTo<MainRoutes.Login> { inclusive = true }
                    }
                },
                // Abre la pantalla para recuperar la contraseña.
                onForgotPasswordClick = { navController.navigate(MainRoutes.RecoverPassword) },
                // Abre la pantalla de registro de una cuenta nueva.
                onRegisterClick = { navController.navigate(MainRoutes.Register) },
            )
        }

        // Pantalla de registro de cuenta.
        composable<MainRoutes.Register> {
            // Muestra el formulario de registro.
            RegisterScreen(
                // Vuelve al login quitando el registro de la pila.
                onBackToLogin = { navController.popBackStack() },
                // Tras registrarse vuelve al login (el Login sigue en la pila).
                onRegisterSuccess = { navController.popBackStack() },
                // Enlace a términos: aún no tiene pantalla, por eso no hace nada.
                onTermsClick = { /* pendiente: pantalla de términos */ },
                // Enlace a políticas: aún no tiene pantalla, por eso no hace nada.
                onPoliciesClick = { /* pendiente: pantalla de políticas */ },
            )
        }

        // Bandeja de notificaciones.
        composable<MainRoutes.Notifications> {
            // Muestra las notificaciones y abre el destino adecuado según su tipo.
            NotificationsScreen(
                // Regresa a la pantalla anterior.
                onBack = { navController.popBackStack() },
                // Las notificaciones de publicaciones (comentarios, aprobaciones) abren su detalle.
                onOpenPost = { postId -> navController.navigate(MainRoutes.PostDetail(postId)) },
                // Mensajes y solicitudes de adopción abren el chat asociado.
                onOpenChat = { chatId -> navController.navigate(MainRoutes.Chat(chatId)) },
                // Insignias y niveles se ven en el dashboard de métricas.
                onOpenMetrics = { navController.navigate(MainRoutes.Metrics) }
            )
        }

        // Recuperación de contraseña.
        composable<MainRoutes.RecoverPassword> {
            // Muestra el formulario que pide el correo para enviar el enlace de recuperación.
            RecoverPasswordScreen(
                // Regresa al login.
                onBack = { navController.popBackStack() },
                // El correo ya salió: vuelve al login y el usuario sigue desde su bandeja.
                onEmailSent = { navController.popBackStack() },
            )
        }

        // Dashboard de métricas del usuario, con opción de compartirlas.
        composable<MainRoutes.Metrics> {
            // Obtiene el Context actual, necesario para lanzar Intents de compartir.
            val context = LocalContext.current
            // Carga el texto de invitación traducido al idioma de la app.
            val shareText = stringResource(R.string.metrics_share_text)
            // Ámbito de corrutinas atado a esta pantalla para guardar la captura sin bloquear la interfaz.
            val scope = rememberCoroutineScope()
            // Muestra las métricas.
            MetricsScreen(
                // Regresa a la pantalla anterior.
                onBack = { navController.popBackStack() },
                // Recibe la captura de pantalla de las métricas (puede ser nula si falló).
                onShare = { screenshot ->
                    // Comprueba si se logró obtener la captura.
                    if (screenshot != null) {
                        // Captura de las métricas + el texto de invitación.
                        scope.launch { context.shareImage(screenshot, "mis_metricas", shareText) }
                    } else {
                        // Sin captura: se comparte solo el texto.
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            // Indica que el contenido es texto plano.
                            type = "text/plain"
                            // Adjunta el texto de invitación.
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        // Abre el selector del sistema para que el usuario elija con qué app compartir.
                        context.startActivity(Intent.createChooser(intent, null))
                    }
                }
            )
        }



        // Nueva contraseña (llega desde el enlace del correo de recuperación).
        composable<MainRoutes.NewPassword> {
            // Muestra el formulario para escribir la nueva contraseña.
            NewPasswordScreen(
                // Regresa a la pantalla anterior.
                onBack = { navController.popBackStack() },
                // Tras actualizar la contraseña lleva al login para entrar con ella.
                onPasswordUpdated = {
                    // Navega al login.
                    navController.navigate(MainRoutes.Login) {
                        // Limpia la pila: "atrás" no debe volver a esta pantalla.
                        popUpTo<MainRoutes.NewPassword> { inclusive = true }
                    }
                },
            )
        }

        // Dashboard con la barra inferior: el rol sale de la sesión.
        composable<MainRoutes.Main> {
            // Muestra el contenedor con pestañas; cada callback abre una pantalla completa fuera de la barra.
            MainScreen(
                // Abre el formulario para crear una publicación.
                onCreatePost = { navController.navigate(MainRoutes.CreatePost) },
                // Abre el detalle de la publicación tocada.
                onPostClick = { postId -> navController.navigate(MainRoutes.PostDetail(postId)) },
                // Abre la bandeja de notificaciones.
                onNotificationsClick = { navController.navigate(MainRoutes.Notifications) },
                // Abre el dashboard de métricas.
                onMetricsClick = { navController.navigate(MainRoutes.Metrics) },
                // Abre la configuración de la cuenta.
                onSettingsClick = { navController.navigate(MainRoutes.Settings) },
                // Abre la edición del perfil propio.
                onEditProfileClick = { navController.navigate(MainRoutes.EditProfile) },
                // Abre el perfil público de otro usuario.
                onUserClick = { userId -> navController.navigate(MainRoutes.UserProfile(userId)) },
                // Abre la lista de publicaciones propias.
                onMyPostsClick = { navController.navigate(MainRoutes.MyPosts) },
                // Abre las publicaciones marcadas como importantes.
                onMyLikesClick = { navController.navigate(MainRoutes.MyLikes) },
                // Abre una conversación desde la pestaña de mensajes.
                onChatClick = { chatId -> navController.navigate(MainRoutes.Chat(chatId)) }
            )
        }

        // Conversación (sin barra). El chatId lo lee el ViewModel de la ruta.
        composable<MainRoutes.Chat> {
            // Muestra los mensajes del chat.
            ChatScreen(
                // Regresa a la pantalla anterior.
                onBack = { navController.popBackStack() },
                // Abre el perfil del otro participante.
                onUserClick = { userId -> navController.navigate(MainRoutes.UserProfile(userId)) },
                // Abre la publicación sobre la que trata el chat.
                onPostClick = { postId -> navController.navigate(MainRoutes.PostDetail(postId)) }
            )
        }

        // Perfil de otra persona (sin barra). El userId lo lee el ViewModel de la ruta.
        composable<MainRoutes.UserProfile> {
            // Obtiene el Context para poder abrir el marcador telefónico.
            val context = LocalContext.current
            // Muestra el perfil público.
            UserProfileScreen(
                // Regresa a la pantalla anterior.
                onBack = { navController.popBackStack() },
                // Abre una de las publicaciones del perfil.
                onPostClick = { postId -> navController.navigate(MainRoutes.PostDetail(postId)) },
                // Recibe el teléfono del usuario cuando se toca "Llamar".
                onCallClick = { phone ->
                    // ACTION_DIAL solo abre el marcador con el número: no llama ni pide permisos.
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                }
            )
        }

        // Editar perfil (sin barra): al guardar vuelve al perfil, que se actualiza solo.
        composable<MainRoutes.EditProfile> {
            // Muestra el formulario de edición del perfil.
            EditProfileScreen(
                // Regresa sin guardar.
                onBack = { navController.popBackStack() },
                // Tras guardar regresa al perfil.
                onSaved = { navController.popBackStack() }
            )
        }

        // Configuración (sin barra).
        composable<MainRoutes.Settings> {
            // Sin sesión no se puede volver a ninguna pantalla anterior: se limpia toda la pila
            // (y con ella los datos de la sesión que guardaban sus ViewModels).
            val backToLogin = {
                // Navega al login.
                navController.navigate(MainRoutes.Login) {
                    // Elimina todo el grafo de la pila, incluido su destino inicial.
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            }
            // Muestra la configuración de la cuenta.
            SettingsScreen(
                // Regresa a la pantalla anterior.
                onBack = { navController.popBackStack() },
                // Tras eliminar la cuenta se vuelve al login con la pila limpia.
                onAccountDeleted = backToLogin,
                // Tras cerrar sesión se vuelve al login con la pila limpia.
                onLoggedOut = backToLogin
            )
        }

        // Crear publicación (sin barra).
        composable<MainRoutes.CreatePost> {
            // Muestra el asistente por pasos para crear una publicación.
            CreatePostScreen(
                // Cancela y regresa a la pantalla anterior.
                onBack = { navController.popBackStack() },
                // Abre la publicación recién creada; "atrás" desde ahí vuelve al inicio, no al formulario.
                onViewPost = { postId ->
                    // Navega al detalle de la nueva publicación.
                    navController.navigate(MainRoutes.PostDetail(postId)) {
                        // Quita el formulario de crear de la pila.
                        popUpTo<MainRoutes.CreatePost> { inclusive = true }
                    }
                },
                // Vuelve al inicio tras publicar.
                onGoHome = { navController.popBackStack() }
            )
        }

        // Editar publicación (sin barra): la misma pantalla de crear; el postId lo lee el ViewModel de la ruta.
        // Al guardar se vuelve al detalle, que se recarga solo al reaparecer.
        composable<MainRoutes.EditPost> {
            // Reutiliza la pantalla de crear en modo edición.
            CreatePostScreen(
                // Cancela y regresa al detalle.
                onBack = { navController.popBackStack() },
                // Tras guardar regresa al detalle (que ya estaba en la pila) en vez de abrir uno nuevo.
                onViewPost = { navController.popBackStack() },
                // Vuelve directamente al dashboard, quitando de la pila todo lo que esté encima de él.
                onGoHome = { navController.popBackStack(MainRoutes.Main, inclusive = false) }
            )
        }

        // Detalle de la publicación (sin barra).
        // El postId lo lee el ViewModel de la ruta. Cambia según sea del usuario o de otra persona.
        composable<MainRoutes.PostDetail> {
            // Obtiene el Context para lanzar el Intent de compartir.
            val context = LocalContext.current
            // Muestra el detalle de la publicación.
            PostDetailScreen(
                // Regresa a la pantalla anterior.
                onBack = { navController.popBackStack() },
                // Recibe el título de la publicación para compartirlo.
                onShare = { title ->
                    // Por ahora se comparte solo un texto con el título de la publicación.
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        // Indica que el contenido es texto plano.
                        type = "text/plain"
                        // Arma el texto a compartir con el título, usando el recurso traducido.
                        putExtra(Intent.EXTRA_TEXT, context.getString(R.string.post_share_text, title))
                    }
                    // Abre el selector del sistema para elegir con qué app compartir.
                    context.startActivity(Intent.createChooser(intent, null))
                },
                // Abre todos los comentarios; si viene replyTo, resalta el comentario a responder.
                onOpenComments = { postId, replyTo -> navController.navigate(MainRoutes.PostComments(postId, replyTo)) },
                // Abre la edición de la publicación (solo disponible para su autor).
                onEditPost = { postId -> navController.navigate(MainRoutes.EditPost(postId)) },
                // Abre el perfil público del autor.
                onAuthorClick = { userId -> navController.navigate(MainRoutes.UserProfile(userId)) },
                // Abre el chat con el autor (por ejemplo, tras "Me interesa adoptar").
                onOpenChat = { chatId -> navController.navigate(MainRoutes.Chat(chatId)) },
                // Moderador: abre la pantalla para escribir el motivo del rechazo.
                onRejectPost = { postId -> navController.navigate(MainRoutes.RejectPost(postId)) },
                // Abre otra publicación (por ejemplo, una relacionada).
                onOpenPost = { postId -> navController.navigate(MainRoutes.PostDetail(postId)) }
            )
        }

        // Rechazar publicación (moderador). Al terminar vuelve al detalle, que se recarga y muestra el motivo.
        composable<MainRoutes.RejectPost> {
            // Muestra el formulario del motivo de rechazo.
            RejectPostScreen(
                // Cancela y regresa al detalle.
                onBack = { navController.popBackStack() },
                // Tras rechazar regresa al detalle.
                onRejected = { navController.popBackStack() }
            )
        }

        // Todos los comentarios (sin barra).
        composable<MainRoutes.PostComments> {
            // Muestra la lista de comentarios y el campo para comentar.
            CommentsScreen(
                // Regresa al detalle.
                onBack = { navController.popBackStack() },
                // Abre el perfil del autor de un comentario.
                onAuthorClick = { userId -> navController.navigate(MainRoutes.UserProfile(userId)) }
            )
        }

        // Me gusta (sin barra).
        composable<MainRoutes.MyLikes> {
            // Muestra las publicaciones marcadas como importantes.
            MyLikesScreen(
                // Regresa a la pantalla anterior.
                onBack = { navController.popBackStack() },
                // Abre el detalle de la publicación tocada.
                onPostClick = { postId -> navController.navigate(MainRoutes.PostDetail(postId)) }
            )
        }

        // Mis publicaciones (sin barra).
        composable<MainRoutes.MyPosts> {
            // Muestra las publicaciones propias agrupadas por estado.
            MyPostsScreen(
                // Regresa a la pantalla anterior.
                onBack = { navController.popBackStack() },
                // Abre el detalle de la publicación tocada.
                onPostClick = { postId -> navController.navigate(MainRoutes.PostDetail(postId)) }
            )
        }
    }
}