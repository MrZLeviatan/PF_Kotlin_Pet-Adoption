package co.edu.uniquindio.petadoptionapp.navigation

import co.edu.uniquindio.petadoptionapp.domain.model.enums.UserRole
import kotlinx.serialization.Serializable

// Anotación que indica que todas las subclases de esta clase sellada pueden serializarse para Jetpack Navigation.
@Serializable
// Define una clase sellada (sealed class) que agrupa las rutas de nivel superior (pantallas principales y modales sin barra inferior).
sealed class MainRoutes {

    // Ruta del dashboard principal que contiene la barra de navegación inferior. Recibe obligatoriamente el rol del usuario como parámetro.
    @Serializable
    data class Main(val role: UserRole) : MainRoutes()

    // Ruta para la pantalla completa de creación de una nueva publicación (se muestra en pantalla completa sin barra inferior).
    @Serializable
    data object CreatePost : MainRoutes()

    // Detalle de una publicación (pantalla completa, sin barra)
    @Serializable
    data class PostDetail(val postId: String) : MainRoutes()

}