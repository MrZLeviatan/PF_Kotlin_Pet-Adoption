package co.edu.uniquindio.petadoptionapp.navigation

import co.edu.uniquindio.petadoptionapp.domain.model.users.UserRole
import kotlinx.serialization.Serializable

// Anotación que indica que todas las subclases de esta clase sellada pueden serializarse para Jetpack Navigation.
@Serializable
// Define una clase sellada (sealed class) que agrupa las rutas de nivel superior (pantallas principales y modales sin barra inferior).
sealed class MainRoutes {



    // Ahora: sin argumentos, el rol se lee de la sesión.
    @Serializable data object Main : MainRoutes()


    // Pantalla de carga inicial
    @Serializable
    data object Splash : MainRoutes()


    // Pantalla de inicio de sesión (sin barra inferior)
    @Serializable
    data object Login : MainRoutes()

    // Registro de cuenta (dos fases, un solo destino)
    @Serializable
    data object Register : MainRoutes()

    // Recuperar contraseña: pide el correo para enviar el enlace
    @Serializable
    data object RecoverPassword : MainRoutes()

    // Nueva contraseña: se abre desde el enlace del correo
    @Serializable
    data object NewPassword : MainRoutes()


    @Serializable data object Metrics : MainRoutes()

    // Configuración de la cuenta (seguridad, privacidad, eliminar cuenta)
    @Serializable data object Settings : MainRoutes()

    // Editar los datos del perfil (nombre, teléfono, ubicación)
    @Serializable data object EditProfile : MainRoutes()

    @Serializable data object Notifications : MainRoutes()


    // Ruta para la pantalla completa de creación de una nueva publicación (se muestra en pantalla completa sin barra inferior).
    @Serializable
    data object CreatePost : MainRoutes()

    // Editar una publicación propia: los mismos pasos de crear, llenos con sus datos (sin barra)
    @Serializable data class EditPost(val postId: String) : MainRoutes()

    // Detalle de una publicación (pantalla completa, sin barra)
    @Serializable
    data class PostDetail(val postId: String) : MainRoutes()

    // Todos los comentarios de una publicación, para comentar y responder (sin barra)
    // replyToCommentId: si se llegó desde "Responder", el comentario a responder (resaltado).
    @Serializable data class PostComments(val postId: String, val replyToCommentId: String? = null) : MainRoutes()

    // Publicaciones del usuario, agrupadas por estado (sin barra)
    @Serializable data object MyPosts : MainRoutes()

    // Publicaciones que el usuario marcó con "Es importante" (sin barra)
    @Serializable data object MyLikes : MainRoutes()

    @Serializable data class UserProfile(val userId: String) : MainRoutes()

    // Una conversación (sin barra). Se abre desde Mensajes, una notificación o "Me interesa adoptar".
    @Serializable data class Chat(val chatId: String) : MainRoutes()

}






