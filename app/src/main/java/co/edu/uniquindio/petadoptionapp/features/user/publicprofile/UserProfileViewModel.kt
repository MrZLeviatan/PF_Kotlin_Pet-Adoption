package co.edu.uniquindio.petadoptionapp.features.user.publicprofile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.error.UserException
import co.edu.uniquindio.petadoptionapp.domain.model.post.Category
import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
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

// Publicación lista para dibujar en la tarjeta resumen.
data class PublicPostUi(
    val id: String,
    val title: String,
    val categoryName: String,
    val photoUrl: String?,
    val ageHours: Int,
    val importantCount: Int,
)

/**
 Estado del perfil de otra persona. Lo que la privacidad oculta llega en `null`
 (nombre, teléfono, nivel); la UI simplemente no dibuja esas partes.
 */
data class UserProfileUiState(
    val userName: String = "",
    val photoUrl: String? = null,
    val isPrivate: Boolean = false,
    val name: String? = null,
    val phone: String? = null,
    // null cuando la reputación está oculta.
    val level: UserLevel? = null,
    val levelNumber: Int = 1,
    val points: Int = 0,
    val currentLevelPoints: Int = 0,
    val nextLevelPoints: Int? = null,
    val badges: List<BadgeUi> = emptyList(),
    val posts: List<PublicPostUi> = emptyList(),
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val hasError: Boolean = false,
) {
    // Mismo cálculo que en Mi Perfil: avance dentro del nivel actual.
    val progress: Float
        get() {
            val next = nextLevelPoints ?: return 1f
            val range = next - currentLevelPoints
            return if (range <= 0) 0f
            else ((points - currentLevelPoints).toFloat() / range).coerceIn(0f, 1f)
        }
}

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeSession: ObserveSessionUseCase,
    private val getUserProfile: GetUserProfileUseCase,
    private val getUserPosts: GetUserPostsUseCase,
    private val observeBadges: ObserveBadgesUseCase,
    private val loadBadges: LoadBadgesUseCase,
    private val observeCategories: ObserveCategoriesUseCase,
    private val loadCatalog: LoadCatalogUseCase,
) : ViewModel() {

    // Llega de la ruta MainRoutes.UserProfile(userId).
    private val userId: String = savedStateHandle["userId"] ?: ""

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    init {
        // Los catálogos se leen una vez por sesión; si otra pantalla ya los cargó, no se repite.
        viewModelScope.launch {
            if (observeBadges().value.isEmpty()) loadBadges()
            if (observeCategories().value.isEmpty()) loadCatalog()
        }
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, hasError = false, notFound = false) }
        viewModelScope.launch {
            // Quien mira se toma de la sesión: la privacidad la decide el dominio.
            val viewerId = observeSession().value?.id.orEmpty()
            val profile = getUserProfile(userId, viewerId).getOrElse { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        notFound = error === UserException.UserNotFound,
                        hasError = error !== UserException.UserNotFound,
                    )
                }
                return@launch
            }
            // Si el perfil es privado, el caso de uso devuelve una lista vacía.
            val posts = getUserPosts(userId).getOrDefault(emptyList())

            // Las insignias y categorías se cruzan con su catálogo; si este cambia, se actualiza solo.
            combine(observeBadges(), observeCategories()) { badgeCatalog, categories ->
                profile.toUiState(posts, badgeCatalog, categories)
            }.collect { state -> _uiState.value = state }
        }
    }

    private fun PublicProfile.toUiState(
        posts: List<Post>,
        badgeCatalog: List<Badge>,
        categories: List<Category>,
    ): UserProfileUiState {
        val level = reputation?.level()
        val next = level?.let { UserLevel.entries.getOrNull(it.ordinal + 1) }
        val now = System.currentTimeMillis()

        val badges = reputation?.unlockedBadges.orEmpty().mapNotNull { unlocked ->
            badgeCatalog.firstOrNull { it.id == unlocked.badgeId }?.let { badge ->
                BadgeUi(badge.id, badge.name, badge.description, badge.requirementType, unlocked.unlockedAt)
            }
        }.sortedByDescending { it.unlockedAt }

        return UserProfileUiState(
            userName = userName,
            photoUrl = photoUrl,
            isPrivate = isPrivate,
            name = name?.takeIf { it.isNotBlank() },
            phone = phone?.takeIf { it.isNotBlank() },
            level = level,
            levelNumber = reputation?.levelNumber() ?: 1,
            points = reputation?.points ?: 0,
            currentLevelPoints = level?.minPoints ?: 0,
            nextLevelPoints = next?.minPoints,
            badges = badges,
            posts = posts.map { post ->
                PublicPostUi(
                    id = post.id,
                    title = post.title,
                    categoryName = categories.firstOrNull { it.id == post.categoryId }?.name.orEmpty(),
                    photoUrl = post.photoUrls.firstOrNull(),
                    ageHours = ((now - post.createdAt) / MILLIS_PER_HOUR).toInt().coerceAtLeast(0),
                    importantCount = post.importantCount,
                )
            },
            isLoading = false,
        )
    }

    private companion object {
        const val MILLIS_PER_HOUR = 60 * 60 * 1000L
    }
}
