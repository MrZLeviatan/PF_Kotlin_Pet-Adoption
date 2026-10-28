package co.edu.uniquindio.petadoptionapp.features.user.post.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.error.CatalogException
import co.edu.uniquindio.petadoptionapp.domain.error.PostException
import co.edu.uniquindio.petadoptionapp.domain.model.location.Location
import co.edu.uniquindio.petadoptionapp.domain.model.post.AnimalSize
import co.edu.uniquindio.petadoptionapp.domain.model.post.AnimalType
import co.edu.uniquindio.petadoptionapp.domain.model.post.Category
import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.ObserveSessionUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.LoadCatalogUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.ObserveAnimalTypesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.ObserveCategoriesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.CreatePostUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.GetPostDetailUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.UpdatePostUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.ObserveCurrentUserUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.PostValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Cantidad de pasos del formulario.
const val CREATE_POST_STEPS = 4

// Unidad en la que la persona escribe la edad; el dominio la guarda siempre en meses.
enum class AgeUnit(val months: Int) { MONTHS(1), YEARS(12) }

// Error de un campo; el texto (con sus límites) lo pone la UI con strings.xml.
enum class FieldError { EMPTY, TOO_SHORT, TOO_LONG, INVALID }

// Error al publicar que no pertenece a un campo concreto.
// NOT_EDITABLE: al editar, la publicación dejó de poder editarse (finalizada, rechazada o ajena).
enum class PublishError { LOCATION, CATALOG, NOT_EDITABLE, GENERIC }

// Campos que ya se tocaron: sus errores se muestran desde ese momento.
enum class CreatePostField { TITLE, DESCRIPTION, BREED, AGE }

/**
 Estado del formulario por pasos. Las reglas de formato salen de [PostValidator]
 (las mismas que aplican CreatePostUseCase y UpdatePostUseCase), así lo que la pantalla deja pasar el dominio lo acepta.
 El mismo estado sirve para crear y para editar ([isEditing]).
 */
data class CreatePostUiState(
    val step: Int = 1,

    // Paso 1: información de la publicación.
    val title: String = "",
    val categoryId: String? = null,
    val description: String = "",

    // Paso 2: datos de la mascota.
    val animalTypeId: String? = null,
    val breed: String = "",
    val size: AnimalSize? = null,
    val age: String = "",
    val ageUnit: AgeUnit = AgeUnit.MONTHS,
    val vaccinated: Boolean = false,
    val neutered: Boolean = false,

    // Paso 3: fotos (direcciones de las imágenes).
    val photos: List<String> = emptyList(),

    // Paso 4: ubicación. Por ahora es la del perfil; el mapa se agrega con Mapbox.
    val useCurrentLocation: Boolean = true,
    val profileLocation: Location = Location(),

    // Catálogos activos (los define el moderador).
    val categories: List<Category> = emptyList(),
    val animalTypes: List<AnimalType> = emptyList(),

    val touched: Set<CreatePostField> = emptySet(),
    val isPublishing: Boolean = false,
    val publishError: PublishError? = null,
    // Id de la publicación creada o editada: mientras exista se muestra el diálogo de éxito.
    val savedPostId: String? = null,

    // --- Solo al editar ---
    val isEditing: Boolean = false,
    // Mientras se cargan los datos de la publicación.
    val isLoading: Boolean = false,
    // No se pudo abrir para editar (no existe, no es propia, finalizada o rechazada).
    val loadError: Boolean = false,
    // La publicación tal como se cargó; contra ella se mide si hubo cambios.
    val originalPost: Post? = null,
) {
    // Edad convertida a meses (null si no es un número).
    val ageInMonths: Int? get() = age.toIntOrNull()?.let { it * ageUnit.months }

    val titleError: FieldError? get() = shown(CreatePostField.TITLE, validateLength(title, PostValidator.MIN_TITLE_LENGTH, PostValidator.MAX_TITLE_LENGTH))
    val descriptionError: FieldError? get() = shown(CreatePostField.DESCRIPTION, validateLength(description, PostValidator.MIN_DESCRIPTION_LENGTH, PostValidator.MAX_DESCRIPTION_LENGTH))
    val breedError: FieldError? get() = shown(CreatePostField.BREED, validateLength(breed, MIN_BREED_LENGTH, Int.MAX_VALUE))
    val ageError: FieldError? get() = shown(CreatePostField.AGE, validateAge())

    // La ubicación sirve si tiene coordenadas (regla de la publicación).
    val hasUsableLocation: Boolean get() = useCurrentLocation && profileLocation.hasCoordinates()

    // Al crear siempre hay algo que enviar; al editar, solo si cambió algún dato.
    val hasChanges: Boolean get() = originalPost == null || toPost(originalPost) != originalPost

    /**
     Los datos del formulario sobre [base]: una publicación vacía al crear, la original al editar
     (así conserva su id, autor, fechas y contadores).
     */
    fun toPost(base: Post = Post()): Post = base.copy(
        title = title.trim(),
        categoryId = categoryId.orEmpty(),
        description = description.trim(),
        animalTypeId = animalTypeId,
        breed = breed.trim(),
        size = size,
        ageInMonths = ageInMonths,
        vaccinated = vaccinated,
        neutered = neutered,
        photoUrls = photos,
        location = profileLocation,
    )

    /** Habilita "Continuar"/"Publicar" según el paso. */
    val isStepValid: Boolean
        get() = when (step) {
            1 -> validateLength(title, PostValidator.MIN_TITLE_LENGTH, PostValidator.MAX_TITLE_LENGTH) == null &&
                categoryId != null &&
                validateLength(description, PostValidator.MIN_DESCRIPTION_LENGTH, PostValidator.MAX_DESCRIPTION_LENGTH) == null
            2 -> animalTypeId != null && size != null && validateLength(breed, MIN_BREED_LENGTH, Int.MAX_VALUE) == null &&
                validateAge() == null
            3 -> photos.isNotEmpty() && photos.size <= PostValidator.MAX_PHOTOS
            else -> hasUsableLocation && !isPublishing && hasChanges
        }

    private fun shown(field: CreatePostField, error: FieldError?) = if (field in touched) error else null

    private fun validateLength(value: String, min: Int, max: Int): FieldError? {
        val clean = value.trim()
        return when {
            clean.isEmpty() -> FieldError.EMPTY
            clean.length < min -> FieldError.TOO_SHORT
            clean.length > max -> FieldError.TOO_LONG
            else -> null
        }
    }

    private fun validateAge(): FieldError? = when {
        age.isBlank() -> FieldError.EMPTY
        ageInMonths == null || ageInMonths!! !in 0..PostValidator.MAX_AGE_IN_MONTHS -> FieldError.INVALID
        else -> null
    }

    companion object {
        // La raza es libre, pero se pide algo más que una letra.
        const val MIN_BREED_LENGTH = 3
    }
}

/**
 Crea una publicación nueva o, si la ruta trae "postId" (EditPost), edita esa publicación:
 la carga, llena los pasos con sus datos y al final guarda con UpdatePostUseCase.
 */
@HiltViewModel
class CreatePostViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeSession: ObserveSessionUseCase,
    observeCurrentUser: ObserveCurrentUserUseCase,
    private val observeCategories: ObserveCategoriesUseCase,
    private val observeAnimalTypes: ObserveAnimalTypesUseCase,
    private val loadCatalog: LoadCatalogUseCase,
    private val createPost: CreatePostUseCase,
    private val getPostDetail: GetPostDetailUseCase,
    private val updatePost: UpdatePostUseCase,
) : ViewModel() {

    // Publicación a editar; null = crear una nueva.
    private val editingPostId: String? = savedStateHandle["postId"]

    private val _uiState = MutableStateFlow(CreatePostUiState(isEditing = editingPostId != null, isLoading = editingPostId != null))
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { if (observeCategories().value.isEmpty()) loadCatalog() }

        // Solo las opciones activas: las desactivadas no se ofrecen para publicaciones nuevas.
        viewModelScope.launch {
            combine(observeCategories(), observeAnimalTypes()) { categories, types ->
                categories.filter { it.active } to types.filter { it.active }
            }.collect { (categories, types) ->
                _uiState.update { it.copy(categories = categories, animalTypes = types) }
            }
        }

        if (editingPostId != null) {
            loadPostToEdit(editingPostId)
        } else {
            // La ubicación de una publicación nueva es, por ahora, la del perfil.
            viewModelScope.launch {
                observeCurrentUser().collect { user ->
                    _uiState.update { it.copy(profileLocation = user?.location ?: Location()) }
                }
            }
        }
    }

    /**
     Carga la publicación y llena el formulario. Revisa lo mismo que UpdatePostUseCase
     (propia, en proceso y aún sin verificar) para no dejar llenar un formulario que no se va a poder guardar.
     Al editar se conserva la ubicación de la publicación.
     */
    private fun loadPostToEdit(postId: String) {
        val session = observeSession().value
        if (session == null) {
            _uiState.update { it.copy(isLoading = false, loadError = true) }
            return
        }
        viewModelScope.launch {
            val post = getPostDetail(session, postId).getOrNull()
            val editable = post != null &&
                post.authorId == session.id &&
                post.status == PostStatus.IN_PROGRESS &&
                post.verification.status == VerificationStatus.UNVERIFIED
            if (!editable) {
                _uiState.update { it.copy(isLoading = false, loadError = true) }
                return@launch
            }
            // Años si la edad es exacta en años; si no, meses (el dominio la guarda en meses).
            val months = post.ageInMonths
            val inYears = months != null && months >= AgeUnit.YEARS.months && months % AgeUnit.YEARS.months == 0
            _uiState.update { state ->
                val filled = state.copy(
                    isLoading = false,
                    title = post.title,
                    categoryId = post.categoryId,
                    description = post.description,
                    animalTypeId = post.animalTypeId,
                    breed = post.breed,
                    size = post.size,
                    age = months?.let { if (inYears) (it / AgeUnit.YEARS.months).toString() else it.toString() }.orEmpty(),
                    ageUnit = if (inYears) AgeUnit.YEARS else AgeUnit.MONTHS,
                    vaccinated = post.vaccinated ?: false,
                    neutered = post.neutered ?: false,
                    photos = post.photoUrls,
                    profileLocation = post.location,
                )
                // La referencia pasa por el formulario: así un "no se sabe" (null) que el formulario
                // muestra como "no" no cuenta como cambio.
                filled.copy(originalPost = filled.toPost(post))
            }
        }
    }

    // --- Paso 1 ---

    fun onTitleChange(value: String) = _uiState.update {
        it.copy(title = value.take(PostValidator.MAX_TITLE_LENGTH), touched = it.touched + CreatePostField.TITLE)
    }

    fun onCategorySelected(index: Int) = _uiState.update { it.copy(categoryId = it.categories.getOrNull(index)?.id) }

    fun onDescriptionChange(value: String) = _uiState.update {
        it.copy(description = value.take(PostValidator.MAX_DESCRIPTION_LENGTH), touched = it.touched + CreatePostField.DESCRIPTION)
    }

    // --- Paso 2 ---

    fun onAnimalTypeSelected(index: Int) = _uiState.update { it.copy(animalTypeId = it.animalTypes.getOrNull(index)?.id) }

    fun onBreedChange(value: String) = _uiState.update { it.copy(breed = value, touched = it.touched + CreatePostField.BREED) }

    fun onSizeSelected(index: Int) = _uiState.update { it.copy(size = AnimalSize.entries.getOrNull(index)) }

    // Solo dígitos: así nunca queda una edad imposible de convertir.
    fun onAgeChange(value: String) = _uiState.update {
        it.copy(age = value.filter(Char::isDigit).take(3), touched = it.touched + CreatePostField.AGE)
    }

    fun onAgeUnitSelected(index: Int) = _uiState.update { it.copy(ageUnit = AgeUnit.entries.getOrElse(index) { AgeUnit.MONTHS }) }

    fun onVaccinatedChange(value: Boolean) = _uiState.update { it.copy(vaccinated = value) }

    fun onNeuteredChange(value: Boolean) = _uiState.update { it.copy(neutered = value) }

    // --- Paso 3 ---

    /**
     Agrega una foto. TODO (Cloudinary): abrir la cámara o la galería, subir la imagen y guardar su URL.
     Mientras tanto se usa una imagen de prueba distinta cada vez.
     */
    fun onAddPhoto() = _uiState.update { state ->
        if (state.photos.size >= PostValidator.MAX_PHOTOS) state
        else state.copy(photos = state.photos + "https://picsum.photos/seed/${Random.nextInt(10_000)}/400/400")
    }

    fun onRemovePhoto(index: Int) = _uiState.update { it.copy(photos = it.photos.filterIndexed { i, _ -> i != index }) }

    // --- Paso 4 ---

    fun onUseCurrentLocationChange(value: Boolean) = _uiState.update { it.copy(useCurrentLocation = value, publishError = null) }

    // --- Navegación entre pasos y publicación ---

    fun onNextClick() {
        val state = _uiState.value
        if (!state.isStepValid) {
            // Muestra los errores de los campos del paso aunque no se hayan tocado.
            _uiState.update { it.copy(touched = it.touched + CreatePostField.entries) }
            return
        }
        if (state.step < CREATE_POST_STEPS) {
            _uiState.update { it.copy(step = it.step + 1) }
        } else {
            publish(state)
        }
    }

    fun previousStep() = _uiState.update { if (it.step > 1) it.copy(step = it.step - 1, publishError = null) else it }

    private fun publish(state: CreatePostUiState) {
        val session = observeSession().value ?: return
        _uiState.update { it.copy(isPublishing = true, publishError = null) }

        viewModelScope.launch {
            val original = state.originalPost
            // Crear: el caso de uso fuerza el resto (sin verificar, en proceso, contadores en cero).
            // Editar: el caso de uso reinicia la verificación, así vuelve a revisión.
            val result = if (original == null) {
                createPost(session.id, state.toPost()).map { it.id }
            } else {
                updatePost(session.id, state.toPost(original)).map { original.id }
            }
            result
                .onSuccess { postId -> _uiState.update { it.copy(isPublishing = false, savedPostId = postId) } }
                .onFailure { error ->
                    val publishError = when (error) {
                        PostException.InvalidLocation -> PublishError.LOCATION
                        is CatalogException -> PublishError.CATALOG
                        PostException.NotAllowed, PostException.PostNotActive,
                        PostException.AlreadyReviewed, PostException.PostNotFound -> PublishError.NOT_EDITABLE
                        else -> PublishError.GENERIC
                    }
                    _uiState.update { it.copy(isPublishing = false, publishError = publishError) }
                }
        }
    }
}
