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

    @Serializable data object Notifications : MainRoutes()


    // Ruta para la pantalla completa de creación de una nueva publicación (se muestra en pantalla completa sin barra inferior).
    @Serializable
    data object CreatePost : MainRoutes()

    // Detalle de una publicación (pantalla completa, sin barra)
    @Serializable
    data class PostDetail(val postId: String) : MainRoutes()

    @Serializable data class UserProfile(val userId: String) : MainRoutes()

}






