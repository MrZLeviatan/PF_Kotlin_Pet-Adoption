package co.edu.uniquindio.petadoptionapp.features.user.myposts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.model.post.Category
import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.ObserveSessionUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.LoadCatalogUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.ObserveCategoriesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.DeletePostUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.FinishPostUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.GetMyPostsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
ViewModel de la pantalla "Mis publicaciones" (MyPostsScreen).
Estado (MyPostsUiState): la pestaña/filtro elegido, las publicaciones del autor ya traducidas
a MyPostUi (categoría, estado visible, foto, antigüedad, interesados y motivo de rechazo), las
banderas de carga y error, y la acción pendiente de confirmar (finalizar o eliminar).
Eventos: recargar, cambiar de filtro, pedir finalizar/eliminar, confirmar o cancelar el diálogo.
Casos de uso: ObserveSessionUseCase, GetMyPostsUseCase, FinishPostUseCase, DeletePostUseCase,
ObserveCategoriesUseCase y LoadCatalogUseCase.
Navegación: no navega por sí mismo; la pantalla avisa a AppNavigation al tocar una publicación
(para abrir su detalle) o al volver.
**/

// Sufijo "Ui" para no chocar con modelos del dominio que tengan el mismo nombre.
// El orden define el de las secciones: Activas → Pendientes → Rechazadas → Finalizadas.
enum class PostStatusUi { ACTIVE, PENDING, REJECTED, FINISHED }
// Pestañas de filtro de la pantalla: Todas, Activas, Pendientes y Finalizadas.
enum class PostFilter { ALL, ACTIVE, PENDING, FINISHED }

/**
Modelo de presentación de una publicación propia, listo para dibujar en su tarjeta.
**/
data class MyPostUi(
    // Identificador de la publicación.
    val id: String,
    // Nombre de la categoría (sale del catálogo).
    val categoryName: String,
    // Estado visible para el autor.
    val status: PostStatusUi,
    // Título de la publicación.
    val title: String,
    // URL de la primera foto; null si no tiene.
    val photoUrl: String?,
    // Antigüedad en horas: la pantalla la convierte a "hace X horas/días".
    val ageHours: Int,
    // Cantidad de personas interesadas.
    val interestedCount: Int = 0,
    // Motivo del rechazo dado por el moderador, si fue rechazada.
    val rejectionReason: String? = null,
)

// Acción que espera confirmación en el diálogo.
sealed interface PendingPostAction {
    // Id de la publicación sobre la que se hará la acción.
    val postId: String
    // Acción de finalizar la publicación.
    data class Finish(override val postId: String) : PendingPostAction
    // Acción de eliminar la publicación.
    data class Delete(override val postId: String) : PendingPostAction
}

/**
Estado inmutable de la pantalla "Mis publicaciones".
**/
data class MyPostsUiState(
    // Pestaña de filtro seleccionada.
    val filter: PostFilter = PostFilter.ALL,
    // Todas las publicaciones del autor.
    val posts: List<MyPostUi> = emptyList(),
    // Indica si se está cargando la lista.
    val isLoading: Boolean = true,
    // Indica si falló la carga.
    val hasError: Boolean = false,
    // Diálogo abierto (null = cerrado).
    val pendingAction: PendingPostAction? = null,
    // Indica si la acción confirmada está en curso.
    val isProcessing: Boolean = false,
    // Indica si la última acción (finalizar/eliminar) falló.
    val actionError: Boolean = false,
) {
    /**
    Cantidad de publicaciones de cada pestaña.
    **/
    fun countOf(filter: PostFilter): Int = posts.count { it.matches(filter) }

    /**
    Publicaciones de la pestaña actual, agrupadas por estado y en el orden del enum.
    **/
    val visibleSections: Map<PostStatusUi, List<MyPostUi>>
        // Filtra por la pestaña, agrupa por estado y ordena según el enum.
        get() = posts
            .filter { it.matches(filter) }
            .groupBy { it.status }
            .toSortedMap()
}

// Indica si una publicación pertenece a una pestaña.
// "Todas" incluye todas las publicaciones, también las finalizadas (van en su propia sección al final).
private fun MyPostUi.matches(filter: PostFilter): Boolean = when (filter) {
    // Todas: siempre coincide.
    PostFilter.ALL -> true
    // Activas.
    PostFilter.ACTIVE -> status == PostStatusUi.ACTIVE
    // Pendientes.
    PostFilter.PENDING -> status == PostStatusUi.PENDING
    // Finalizadas.
    PostFilter.FINISHED -> status == PostStatusUi.FINISHED
}

// Marca la clase como ViewModel inyectable por Hilt.
@HiltViewModel
// Declara el ViewModel con sus casos de uso inyectados.
class MyPostsViewModel @Inject constructor(
    // Caso de uso que expone la sesión actual.
    private val observeSession: ObserveSessionUseCase,
    // Caso de uso que obtiene las publicaciones del autor.
    private val getMyPosts: GetMyPostsUseCase,
    // Caso de uso que finaliza una publicación.
    private val finishPost: FinishPostUseCase,
    // Caso de uso que elimina una publicación.
    private val deletePost: DeletePostUseCase,
    // Caso de uso que emite las categorías del catálogo.
    private val observeCategories: ObserveCategoriesUseCase,
    // Caso de uso que carga el catálogo.
    private val loadCatalog: LoadCatalogUseCase,
) : ViewModel() {

    // Estado mutable interno.
    private val _uiState = MutableStateFlow(MyPostsUiState())
    // Estado de solo lectura que observa la pantalla.
    val uiState: StateFlow<MyPostsUiState> = _uiState.asStateFlow()

    // Últimas publicaciones leídas: se vuelven a traducir si cambia el catálogo.
    private var lastPosts: List<Post> = emptyList()

    // Inicialización del ViewModel.
    init {
        // El nombre de cada categoría sale del catálogo; si llega después, las etiquetas se actualizan.
        viewModelScope.launch {
            // Carga el catálogo si aún está vacío.
            if (observeCategories().value.isEmpty()) loadCatalog()
            // Recolecta cada cambio de las categorías.
            observeCategories().collect { categories ->
                // Vuelve a traducir las publicaciones con los nombres de categoría actuales.
                _uiState.update { it.copy(posts = lastPosts.map { post -> post.toUi(categories) }) }
            }
        }
    }

    /**
    Recarga la lista; la pantalla lo llama al aparecer (por si se creó o cambió algo).
    **/
    fun refresh() {
        // Obtiene la sesión; sin sesión no hace nada.
        val session = observeSession().value ?: return
        // Lanza una corrutina para leer las publicaciones.
        viewModelScope.launch {
            // Pide las publicaciones del autor.
            getMyPosts(session.id)
                // Si salió bien.
                .onSuccess { posts ->
                    // Guarda las publicaciones crudas.
                    lastPosts = posts
                    // Toma las categorías actuales.
                    val categories = observeCategories().value
                    // Publica la lista traducida y quita carga y error.
                    _uiState.update {
                        // Copia del estado con las publicaciones.
                        it.copy(posts = posts.map { post -> post.toUi(categories) }, isLoading = false, hasError = false)
                    }
                }
                // Si falló, marca el error.
                .onFailure { _uiState.update { it.copy(isLoading = false, hasError = true) } }
        }
    }

    // Cambia la pestaña de filtro.
    fun onFilterSelected(filter: PostFilter) = _uiState.update { it.copy(filter = filter) }

    // Abre el diálogo de finalizar; la acción real ocurre en onConfirmAction.
    fun onFinishPost(postId: String) =
        // Guarda la acción pendiente y limpia el error.
        _uiState.update { it.copy(pendingAction = PendingPostAction.Finish(postId), actionError = false) }

    // Abre el diálogo de eliminar; la acción real ocurre en onConfirmAction.
    fun onDeletePost(postId: String) =
        // Guarda la acción pendiente y limpia el error.
        _uiState.update { it.copy(pendingAction = PendingPostAction.Delete(postId), actionError = false) }

    // Cierra el diálogo, salvo que la acción ya esté en curso.
    fun onDismissAction() = _uiState.update { if (it.isProcessing) it else it.copy(pendingAction = null) }

    // Ejecuta la acción confirmada en el diálogo.
    fun onConfirmAction() {
        // Toma el estado actual.
        val state = _uiState.value
        // Obtiene la acción pendiente; si no hay, sale.
        val action = state.pendingAction ?: return
        // Evita ejecutar dos veces.
        if (state.isProcessing) return
        // Obtiene la sesión; sin sesión sale.
        val session = observeSession().value ?: return

        // Marca la acción como en curso.
        _uiState.update { it.copy(isProcessing = true) }
        // Lanza una corrutina para ejecutar la acción.
        viewModelScope.launch {
            // Llama al caso de uso que corresponda.
            val result = when (action) {
                // Finalizar la publicación.
                is PendingPostAction.Finish -> finishPost(session, action.postId)
                // Eliminar la publicación.
                is PendingPostAction.Delete -> deletePost(session.id, action.postId)
            }
            // Cierra el diálogo y marca error si falló.
            _uiState.update {
                // Copia del estado tras la acción.
                it.copy(isProcessing = false, pendingAction = null, actionError = result.isFailure)
            }
            // Si salió bien, recarga la lista.
            if (result.isSuccess) refresh()
        }
    }

    // Traduce una publicación del dominio al modelo de la pantalla.
    private fun Post.toUi(categories: List<Category>): MyPostUi {
        // Calcula las horas transcurridas desde la creación (sin negativos).
        val ageHours = ((System.currentTimeMillis() - createdAt) / MILLIS_PER_HOUR).toInt().coerceAtLeast(0)
        // Construye el modelo de presentación.
        return MyPostUi(
            // Id.
            id = id,
            // Nombre de la categoría buscado en el catálogo.
            categoryName = categories.firstOrNull { it.id == categoryId }?.name.orEmpty(),
            // Estado visible para el autor.
            status = statusUi(),
            // Título.
            title = title,
            // Primera foto.
            photoUrl = photoUrls.firstOrNull(),
            // Antigüedad en horas.
            ageHours = ageHours,
            // Interesados (marcas de "importante").
            interestedCount = importantCount,
            // Motivo del rechazo.
            rejectionReason = verification.rejectionReason,
        )
    }

    // Une el ciclo de vida y la verificación en el estado que ve el autor.
    private fun Post.statusUi(): PostStatusUi = when {
        // Finalizada tiene prioridad.
        status == PostStatus.FINISHED -> PostStatusUi.FINISHED
        // Verificada: activa.
        verification.status == VerificationStatus.VERIFIED -> PostStatusUi.ACTIVE
        // Rechazada.
        verification.status == VerificationStatus.REJECTED -> PostStatusUi.REJECTED
        // En otro caso: pendiente de revisión.
        else -> PostStatusUi.PENDING
    }

    // Constantes privadas.
    private companion object {
        // Milisegundos de una hora.
        const val MILLIS_PER_HOUR = 60 * 60 * 1000L
    }
}
