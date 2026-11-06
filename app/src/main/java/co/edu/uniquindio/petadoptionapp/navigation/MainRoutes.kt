package co.edu.uniquindio.petadoptionapp.navigation

import co.edu.uniquindio.petadoptionapp.domain.model.users.UserRole
import kotlinx.serialization.Serializable

/**
Catálogo de rutas de nivel superior de la aplicación (navegación con tipos seguros de
Navigation Compose). Cada objeto o clase representa un destino del NavHost definido en
AppNavigation: los `data object` son pantallas sin argumentos y las `data class` llevan los
argumentos que la pantalla necesita (por ejemplo, el id de una publicación).
Gracias a @Serializable, Navigation convierte cada ruta en una cadena para la pila de
navegación y la reconstruye después; los ViewModels leen sus argumentos con
SavedStateHandle.toRoute(). Las pestañas internas de la barra inferior viven aparte, en
DashboardRoutes (features/dashboard); aquí están las pantallas completas sin barra inferior.
**/
// Anotación que indica que todas las subclases de esta clase sellada pueden serializarse para Jetpack Navigation.
@Serializable
// Define una clase sellada (sealed class) que agrupa las rutas de nivel superior (pantallas principales y modales sin barra inferior).
sealed class MainRoutes {



    // Pantalla principal con la barra inferior (dashboard); no lleva argumentos porque el rol se lee de la sesión.
    @Serializable data object Main : MainRoutes()


    // Pantalla de carga inicial: decide si hay sesión guardada para ir al inicio o al login.
    @Serializable
    data object Splash : MainRoutes()


    // Pantalla de inicio de sesión (sin barra inferior).
    @Serializable
    data object Login : MainRoutes()

    // Registro de cuenta (dos fases, un solo destino).
    @Serializable
    data object Register : MainRoutes()

    // Recuperar contraseña: pide el correo para enviar el enlace.
    @Serializable
    data object RecoverPassword : MainRoutes()

    // Nueva contraseña: se abre desde el enlace del correo.
    @Serializable
    data object NewPassword : MainRoutes()


    // Dashboard de métricas del usuario (reputación, insignias y niveles), sin barra inferior.
    @Serializable data object Metrics : MainRoutes()

    // Configuración de la cuenta (seguridad, privacidad, eliminar cuenta).
    @Serializable data object Settings : MainRoutes()

    // Editar los datos del perfil (nombre, teléfono, ubicación).
    @Serializable data object EditProfile : MainRoutes()

    // Bandeja de notificaciones del usuario (comentarios, mensajes, insignias), sin barra inferior.
    @Serializable data object Notifications : MainRoutes()


    // Ruta para la pantalla completa de creación de una nueva publicación (se muestra en pantalla completa sin barra inferior).
    @Serializable
    data object CreatePost : MainRoutes()

    // Editar una publicación propia: los mismos pasos de crear, llenos con sus datos (sin barra); recibe el id de la publicación.
    @Serializable data class EditPost(val postId: String) : MainRoutes()

    // Detalle de una publicación (pantalla completa, sin barra); recibe el id de la publicación a mostrar.
    @Serializable
    data class PostDetail(val postId: String) : MainRoutes()

    // Todos los comentarios de una publicación, para comentar y responder (sin barra).
    // replyToCommentId: si se llegó desde "Responder", el comentario a responder (resaltado); es nulo por defecto.
    @Serializable data class PostComments(val postId: String, val replyToCommentId: String? = null) : MainRoutes()

    // Publicaciones del usuario, agrupadas por estado (sin barra).
    @Serializable data object MyPosts : MainRoutes()

    // Publicaciones que el usuario marcó con "Es importante" (sin barra).
    @Serializable data object MyLikes : MainRoutes()

    // Perfil público de otra persona (sin barra); recibe el id del usuario a consultar.
    @Serializable data class UserProfile(val userId: String) : MainRoutes()

    // Moderador: motivo para rechazar la publicación que tiene en revisión (sin barra).
    @Serializable data class RejectPost(val postId: String) : MainRoutes()

    // Una conversación (sin barra). Se abre desde Mensajes, una notificación o "Me interesa adoptar".
    @Serializable data class Chat(val chatId: String) : MainRoutes()

}






