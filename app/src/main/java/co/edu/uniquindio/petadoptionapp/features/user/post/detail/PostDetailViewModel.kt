package co.edu.uniquindio.petadoptionapp.features.user.post.detail

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Un comentario de la publicación
data class PostComment(
    val id: String,
    val author: String,       // Nombre de quien comentó
    val initials: String,     // Iniciales para el círculo del avatar
    val timeAgo: String,      // Ej: "hace 2h"
    val text: String,         // Texto del comentario
    val likes: Int            // Cantidad de "me gusta"
)

// Toda la información de la publicación
data class PostDetailInfo(
    val id: String,
    val imageUrls: List<String>,   // Fotos de la galería (links de Cloudinary)
    val type: String,              // Etiqueta (ej: "Adopción")
    val verified: Boolean,         // Si lleva la etiqueta "Verificado"
    val title: String,
    val location: String,          // Ej: "Bogotá, Chapinero • 1.2 km"
    val publishedAgo: String,      // Ej: "Publicado hace 3 días"
    val animalType: String,        // Tipo: Perro, Gato
    val breed: String,
    val size: String,
    val vaccines: String,
    val descriptionTitle: String,  // Ej: "Sobre Luna"
    val description: String,
    val approxLocation: String,    // Ej: "Chapinero, Bogotá • Colombia"
    val totalComments: Int,        // Total de comentarios (puede ser mayor a los que se ven)
    val comments: List<PostComment>
)

// Lo que la pantalla necesita para dibujarse
data class PostDetailUiState(
    val post: PostDetailInfo? = null,            // null = aún no ha llegado la publicación
    val comments: List<PostComment> = emptyList(),
    val totalComments: Int = 0,
    val commentText: String = "",                // Texto escrito en la barra de comentarios
    val userInitials: String = "UA"              // Iniciales del usuario actual
)

class PostDetailViewModel : ViewModel() {

    // _uiState es privado (solo el ViewModel lo cambia). uiState es público (la pantalla solo lo lee)
    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    // Aquí se recibe la publicación desde afuera
    fun setPost(post: PostDetailInfo) {
        _uiState.update {
            it.copy(post = post, comments = post.comments, totalComments = post.totalComments)
        }
    }

    // Se llama cada vez que el usuario escribe en la barra de comentarios
    fun onCommentTextChange(value: String) {
        _uiState.update { it.copy(commentText = value) }
    }

    // Se llama al tocar la flecha de enviar
    fun onSendComment() {
        val state = _uiState.value
        if (state.commentText.isBlank()) return    // No envía comentarios vacíos

        val newComment = PostComment(
            id = System.currentTimeMillis().toString(),   // Id único simple
            author = "Tú",
            initials = state.userInitials,
            timeAgo = "ahora",
            text = state.commentText.trim(),
            likes = 0
        )
        _uiState.update {
            it.copy(
                comments = it.comments + newComment,      // Agrega el comentario al final de la lista
                totalComments = it.totalComments + 1,     // Suma 1 al contador "Comentarios (N)"
                commentText = ""                          // Limpia el campo
            )
        }
    }
}