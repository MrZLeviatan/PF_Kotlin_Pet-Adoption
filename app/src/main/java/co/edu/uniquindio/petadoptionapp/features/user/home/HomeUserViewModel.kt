package co.edu.uniquindio.petadoptionapp.features.user.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Modelo temporal (Se cambiara y mejorara en creación del backend)
data class FeedPost(
    val id: String,              // Identificador único de la publicación.
    val imageUrl: String,        // Enlace URL de la fotografía almacenada en Cloudinary.
    val type: String,            // Etiqueta descriptiva del tipo de publicación (ej: "Adopción").
    val distanceKm: Double,      // Distancia calculada en kilómetros respecto al usuario.
    val title: String,           // Título principal de la publicación.
    val species: String,         // Especie de la mascota (ej: "Perro", "Gato").
    val breed: String,           // Raza aproximada de la mascota.
    val size: String,            // Tamaño aproximado de la mascota.
    val vaccinesInfo: String,    // Estado actual de las vacunas.
    val interestedCount: Int     // Cantidad de personas interesadas en la mascota.
)


// Modelo de datos que encapsula todo el estado que la pantalla de inicio necesita para renderizarse.
data class HomeUserUiState(
    val userName: String = "Nicolas",              // Nombre predeterminado del usuario logueado.
    val city: String = "Armenia, Colombia",        // Ciudad y país actual del usuario.
    val notifications: Int = 3,                    // Número de notificaciones pendientes sin leer.
    val query: String = "",                        // Texto actual ingresado en la barra de búsqueda.
    val posts: List<FeedPost> = emptyList()        // Lista de publicaciones que se muestran en pantalla (ya filtradas).
)

// ViewModel encargado de gestionar la lógica de negocio, el estado y el filtrado del feed del usuario.
class HomeUserViewModel : ViewModel() {

    // Lista completa sin filtrar que llega desde fuentes externas. Comienza vacía.
    private var allPosts: List<FeedPost> = emptyList()

    // _uiState es privado y mutable (solo el ViewModel puede modificarlo). uiState es público y de solo lectura (la pantalla solo lo observa).
    private val _uiState = MutableStateFlow(HomeUserUiState())
    val uiState: StateFlow<HomeUserUiState> = _uiState.asStateFlow()

    // Método para recibir y actualizar la lista completa de publicaciones desde una fuente exterior (repositorio o caso de uso).
    fun setPosts(posts: List<FeedPost>) {
        // Almacena la lista completa original.
        allPosts = posts
        // Aplica el filtro manteniendo el texto que estuviera en el buscador.
        applyFilter(_uiState.value.query)
    }

    // Se ejecuta cada vez que el usuario modifica el texto en la barra de búsqueda.
    fun onQueryChange(value: String) {
        // Actualiza el estado con el nuevo texto de búsqueda.
        _uiState.update { it.copy(query = value) }
        // Filtra la lista de publicaciones utilizando el texto ingresado.
        applyFilter(value)
    }

    // Filtra la lista completa por título (ignorando mayúsculas y minúsculas) y actualiza el estado visible.
    private fun applyFilter(query: String) {
        _uiState.update { state ->
            state.copy(posts = allPosts.filter { it.title.contains(query.trim(), ignoreCase = true) })
        }
    }
}