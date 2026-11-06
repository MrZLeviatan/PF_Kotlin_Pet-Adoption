package co.edu.uniquindio.petadoptionapp.features.user.publicprofile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.error.UserException
import co.edu.uniquindio.petadoptionapp.domain.model.post.Category
import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.model.users.UserRole
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.Badge
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.UserLevel
import co.edu.uniquindio.petadoptionapp.domain.model.users.profile.PublicProfile
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.ObserveSessionUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.LoadCatalogUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.ObserveCategoriesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.GetUserPostsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.reputation.LoadBadgesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.reputation.ObserveBadgesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.GetUserProfileUseCase
import co.edu.uniquindio.petadoptionapp.features.user.profile.metric.BadgeUi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
ViewModel del perfil público de otra persona (ruta MainRoutes.UserProfile(userId)).
Lee el userId de la ruta mediante SavedStateHandle y, con la sesión actual
(ObserveSessionUseCase) como "quien mira", pide el perfil con GetUserProfileUseCase,
que ya aplica las reglas de privacidad: lo oculto llega en null. Después obtiene las
publicaciones con GetUserPostsUseCase y las cruza con los catálogos de insignias
(ObserveBadgesUseCase / LoadBadgesUseCase) y de categorías (ObserveCategoriesUseCase /
LoadCatalogUseCase) para construir UserProfileUiState. Si quien mira es moderador,
cada publicación incluye además su estado de revisión. No emite eventos: la pantalla
recibe onBack, onPostClick y onCallClick desde AppNavigation.
**/

// Publicación lista para dibujar en la tarjeta resumen.
data class PublicPostUi(
    // Identificador de la publicación (para abrir su detalle).
    val id: String,
    // Título de la publicación.
    val title: String,
    // Nombre de la categoría ya resuelto desde el catálogo.
    val categoryName: String,
    // Primera foto de la publicación, si tiene.
    val photoUrl: String?,
    // Horas transcurridas desde que se publicó.
    val ageHours: Int,
    // Cantidad de personas que la marcaron como importante.
    val importantCount: Int,
    // Solo para un moderador: en qué punto de la revisión está (null para los demás).
    val review: PostReviewState? = null,
)

/**
Revisión de una publicación vista por un moderador: verificada, en revisión por él mismo,
en revisión por otro moderador o sin asignar.
**/
enum class PostReviewState { VERIFIED, IN_REVIEW_BY_ME, IN_REVIEW_BY_OTHER, UNASSIGNED }

/**
Estado del perfil de otra persona. Lo que la privacidad oculta llega en `null`
(nombre, teléfono, nivel); la UI simplemente no dibuja esas partes.
También indica si la carga sigue en curso, si el usuario no existe, si eliminó su cuenta
o si ocurrió un error.
**/
data class UserProfileUiState(
    // Alias del usuario (siempre visible).
    val userName: String = "",
    // Foto de perfil (siempre visible).
    val photoUrl: String? = null,
    // Indica si el perfil es privado.
    val isPrivate: Boolean = false,
    // Mira un moderador: ve todo (también si es privado) y el estado de revisión de cada publicación.
    val viewerIsModerator: Boolean = false,
    // Nombre real; null si la privacidad lo oculta.
    val name: String? = null,
    // Teléfono; null si la privacidad lo oculta.
    val phone: String? = null,
    // null cuando la reputación está oculta.
    val level: UserLevel? = null,
    // Número del nivel.
    val levelNumber: Int = 1,
    // Puntos de reputación.
    val points: Int = 0,
    // Puntos mínimos del nivel actual.
    val currentLevelPoints: Int = 0,
    // Puntos del siguiente nivel; null en el nivel máximo.
    val nextLevelPoints: Int? = null,
    // Insignias desbloqueadas, de la más reciente a la más antigua.
    val badges: List<BadgeUi> = emptyList(),
    // Publicaciones visibles para quien mira.
    val posts: List<PublicPostUi> = emptyList(),
    // Indica que el perfil se está cargando.
    val isLoading: Boolean = true,
    // El usuario no existe.
    val notFound: Boolean = false,
    // La cuenta fue eliminada por su dueño.
    val isDeleted: Boolean = false,
    // Ocurrió un error inesperado al cargar.
    val hasError: Boolean = false,
) {
    // Mismo cálculo que en Mi Perfil: avance dentro del nivel actual.
    val progress: Float
        get() {
            // Sin siguiente nivel, la barra se muestra llena.
            val next = nextLevelPoints ?: return 1f
            // Puntos que abarca el nivel actual.
            val range = next - currentLevelPoints
            // Evita dividir por cero; si no, calcula la fracción limitada entre 0 y 1.
            return if (range <= 0) 0f
            else ((points - currentLevelPoints).toFloat() / range).coerceIn(0f, 1f)
        }
}

// Marca la clase para que Hilt la construya e inyecte sus dependencias.
@HiltViewModel
class UserProfileViewModel @Inject constructor(
    // Acceso a los argumentos de la ruta de navegación.
    savedStateHandle: SavedStateHandle,
    // Sesión actual: identifica a quien mira y su rol.
    private val observeSession: ObserveSessionUseCase,
    // Obtiene el perfil aplicando las reglas de privacidad.
    private val getUserProfile: GetUserProfileUseCase,
    // Obtiene las publicaciones visibles del usuario.
    private val getUserPosts: GetUserPostsUseCase,
    // Catálogo de insignias en memoria.
    private val observeBadges: ObserveBadgesUseCase,
    // Carga el catálogo de insignias.
    private val loadBadges: LoadBadgesUseCase,
    // Catálogo de categorías en memoria.
    private val observeCategories: ObserveCategoriesUseCase,
    // Carga el catálogo de categorías.
    private val loadCatalog: LoadCatalogUseCase,
) : ViewModel() {

    // Llega de la ruta MainRoutes.UserProfile(userId).
    private val userId: String = savedStateHandle["userId"] ?: ""

    // Estado mutable interno.
    private val _uiState = MutableStateFlow(UserProfileUiState())
    // Estado público de solo lectura.
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    // Bloque de inicialización: asegura los catálogos y carga el perfil.
    init {
        // Los catálogos se leen una vez por sesión; si otra pantalla ya los cargó, no se repite.
        viewModelScope.launch {
            // Carga las insignias solo si el catálogo está vacío.
            if (observeBadges().value.isEmpty()) loadBadges()
            // Carga las categorías solo si el catálogo está vacío.
            if (observeCategories().value.isEmpty()) loadCatalog()
        }
        // Primera carga del perfil.
        load()
    }

    // Carga (o recarga) el perfil y sus publicaciones.
    fun load() {
        // Reinicia las banderas y muestra el estado de carga.
        _uiState.update { it.copy(isLoading = true, hasError = false, notFound = false, isDeleted = false) }
        // Ejecuta la carga en una corrutina del ViewModel.
        viewModelScope.launch {
            // Quien mira se toma de la sesión: la privacidad la decide el dominio.
            val viewerId = observeSession().value?.id.orEmpty()
            // Pide el perfil; si falla, actualiza el estado según el error y termina.
            val profile = getUserProfile(userId, viewerId).getOrElse { error ->
                _uiState.update {
                    it.copy(
                        // Termina la carga.
                        isLoading = false,
                        // El usuario no existe.
                        notFound = error === UserException.UserNotFound,
                        // El usuario eliminó su cuenta.
                        isDeleted = error === UserException.UserDeleted,
                        // Cualquier otro error se considera inesperado.
                        hasError = error !== UserException.UserNotFound && error !== UserException.UserDeleted,
                    )
                }
                return@launch
            }
            // Si el perfil es privado, el caso de uso devuelve una lista vacía.
            val posts = getUserPosts(userId).getOrDefault(emptyList())

            // Las insignias y categorías se cruzan con su catálogo; si este cambia, se actualiza solo.
            val session = observeSession().value
            // Id del moderador que mira, o null si quien mira no es moderador.
            val moderatorId = session?.id?.takeIf { session.role == UserRole.MODERATOR }
            // Combina ambos catálogos y reconstruye el estado cada vez que alguno emite.
            combine(observeBadges(), observeCategories()) { badgeCatalog, categories ->
                profile.toUiState(posts, badgeCatalog, categories, moderatorId)
            // Publica cada estado calculado.
            }.collect { state -> _uiState.value = state }
        }
    }

    // Calcula el estado de revisión de una publicación desde el punto de vista del moderador.
    private fun Post.reviewStateFor(moderatorId: String): PostReviewState = when (verification.status) {
        // Ya verificada.
        VerificationStatus.VERIFIED -> PostReviewState.VERIFIED
        // En proceso: distingue si la revisa este moderador u otro.
        VerificationStatus.IN_PROCESS ->
            if (verification.reviewedBy == moderatorId) PostReviewState.IN_REVIEW_BY_ME else PostReviewState.IN_REVIEW_BY_OTHER
        // Cualquier otro estado: sin asignar.
        else -> PostReviewState.UNASSIGNED
    }

    // Convierte el perfil público del dominio en el estado de la UI.
    private fun PublicProfile.toUiState(
        // Publicaciones visibles.
        posts: List<Post>,
        // Catálogo de insignias.
        badgeCatalog: List<Badge>,
        // Catálogo de categorías.
        categories: List<Category>,
        // Id del moderador que mira (null si no es moderador).
        moderatorId: String?,
    ): UserProfileUiState {
        // Nivel actual, o null si la reputación está oculta.
        val level = reputation?.level()
        // Siguiente nivel en el enum, si existe.
        val next = level?.let { UserLevel.entries.getOrNull(it.ordinal + 1) }
        // Momento actual para calcular la antigüedad de cada publicación.
        val now = System.currentTimeMillis()

        // Cruza las insignias desbloqueadas con su ficha del catálogo, omitiendo las desconocidas.
        val badges = reputation?.unlockedBadges.orEmpty().mapNotNull { unlocked ->
            badgeCatalog.firstOrNull { it.id == unlocked.badgeId }?.let { badge ->
                BadgeUi(badge.id, badge.name, badge.description, badge.requirementType, unlocked.unlockedAt)
            }
        // Ordena de la más reciente a la más antigua.
        }.sortedByDescending { it.unlockedAt }

        // Construye el estado final.
        return UserProfileUiState(
            userName = userName,
            photoUrl = photoUrl,
            isPrivate = isPrivate,
            // Es moderador si se recibió su id.
            viewerIsModerator = moderatorId != null,
            // Nombre y teléfono vacíos se tratan como ocultos (null).
            name = name?.takeIf { it.isNotBlank() },
            phone = phone?.takeIf { it.isNotBlank() },
            level = level,
            // Valores por defecto cuando la reputación está oculta.
            levelNumber = reputation?.levelNumber() ?: 1,
            points = reputation?.points ?: 0,
            currentLevelPoints = level?.minPoints ?: 0,
            nextLevelPoints = next?.minPoints,
            badges = badges,
            // Convierte cada publicación del dominio en su modelo de UI.
            posts = posts.map { post ->
                PublicPostUi(
                    id = post.id,
                    title = post.title,
                    // Busca el nombre de la categoría en el catálogo (vacío si no se encuentra).
                    categoryName = categories.firstOrNull { it.id == post.categoryId }?.name.orEmpty(),
                    // Usa la primera foto como portada.
                    photoUrl = post.photoUrls.firstOrNull(),
                    // Horas desde la creación, nunca negativas.
                    ageHours = ((now - post.createdAt) / MILLIS_PER_HOUR).toInt().coerceAtLeast(0),
                    importantCount = post.importantCount,
                    // Estado de revisión solo si quien mira es moderador.
                    review = moderatorId?.let { post.reviewStateFor(it) },
                )
            },
            // La carga terminó.
            isLoading = false,
        )
    }

    // Constantes privadas del ViewModel.
    private companion object {
        // Milisegundos que tiene una hora.
        const val MILLIS_PER_HOUR = 60 * 60 * 1000L
    }
}
