package co.edu.uniquindio.petadoptionapp.features.user.myposts

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Sufijo "Ui" para no chocar con modelos del dominio que tengan el mismo nombre.
enum class PostTypeUi { ADOPTION, LOST }
enum class PostStatusUi { ACTIVE, PENDING, FINISHED }
enum class PostFilter { ALL, ACTIVE, PENDING, FINISHED }

data class MyPostUi(
    val id: String,
    val type: PostTypeUi,
    val status: PostStatusUi,
    val title: String,
    // Antigüedad en horas: la pantalla la convierte a "hace X horas/días".
    val ageHours: Int,
    val interestedCount: Int = 0,
)

/**
Estado de la pantalla. Los valores por defecto son datos QUEMADOS de prueba.
TODO: reemplazar por las publicaciones reales cuando exista el caso de uso
(ObserveMyPostsUseCase). Las fotos también se cargarán desde una URL.
 */
data class MyPostsUiState(
    val filter: PostFilter = PostFilter.ALL,
    val posts: List<MyPostUi> = listOf(
        MyPostUi("1", PostTypeUi.ADOPTION, PostStatusUi.ACTIVE, "Luna busca un hogar lleno de amor", ageHours = 72, interestedCount = 12),
        MyPostUi("2", PostTypeUi.LOST, PostStatusUi.ACTIVE, "Max, Chihuahua perdido en Usaquén", ageHours = 24, interestedCount = 5),
        MyPostUi("3", PostTypeUi.ADOPTION, PostStatusUi.PENDING, "Simón, gatito juguetón de 8 meses", ageHours = 2),
    ),
) {
    /** Cantidad de publicaciones de cada pestaña. */
    fun countOf(filter: PostFilter): Int = posts.count { it.matches(filter) }

    /** Publicaciones de la pestaña actual, agrupadas por estado y en orden Activas → Pendientes → Finalizadas. */
    val visibleSections: Map<PostStatusUi, List<MyPostUi>>
        get() = posts
            .filter { it.matches(filter) }
            .groupBy { it.status }
            .toSortedMap()
}

// "Todas" no incluye las finalizadas, así el contador coincide con el diseño (2 + 1 = 3).
private fun MyPostUi.matches(filter: PostFilter): Boolean = when (filter) {
    PostFilter.ALL -> status != PostStatusUi.FINISHED
    PostFilter.ACTIVE -> status == PostStatusUi.ACTIVE
    PostFilter.PENDING -> status == PostStatusUi.PENDING
    PostFilter.FINISHED -> status == PostStatusUi.FINISHED
}

class MyPostsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MyPostsUiState())
    val uiState: StateFlow<MyPostsUiState> = _uiState.asStateFlow()

    fun onFilterSelected(filter: PostFilter) = _uiState.update { it.copy(filter = filter) }

    fun onFinishPost(postId: String) {
        // TODO: mostrar el diálogo de confirmación y llamar al caso de uso de finalizar.
    }

    fun onDeletePost(postId: String) {
        // TODO: pedir confirmación y llamar al caso de uso de eliminar.
    }
}