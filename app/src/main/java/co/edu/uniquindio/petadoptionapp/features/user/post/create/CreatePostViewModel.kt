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
import co.edu.uniquindio.petadoptionapp.domain.usecase.location.ReverseGeocodeUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.PostValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
ViewModel del formulario por pasos para crear o editar una publicación (CreatePostScreen).
Estado (CreatePostUiState): el paso actual (1 a 4), los campos de cada paso (información,
mascota, fotos y ubicación), los catálogos activos, los campos tocados, las banderas de
publicación/carga y el id guardado que dispara el diálogo de éxito.
Eventos: cambios de cada campo, selección en catálogos, agregar/quitar fotos, obtener
ubicación actual o elegirla en el mapa, avanzar/retroceder de paso y publicar.
Casos de uso: ObserveSessionUseCase, ObserveCategoriesUseCase, ObserveAnimalTypesUseCase,
LoadCatalogUseCase, CreatePostUseCase, GetPostDetailUseCase, UpdatePostUseCase y
ReverseGeocodeUseCase.
Navegación: si la ruta trae "postId" (ruta EditPost) entra en modo edición; si no, crea una
publicación nueva. Al guardar, la pantalla muestra el diálogo de éxito y decide a dónde ir.
**/

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
**/
data class CreatePostUiState(
    // Paso actual del asistente (de 1 a CREATE_POST_STEPS).
    val step: Int = 1,

    // Paso 1: título de la publicación.
    val title: String = "",
    // Paso 1: id de la categoría elegida (null si no se ha elegido).
    val categoryId: String? = null,
    // Paso 1: descripción de la publicación.
    val description: String = "",

    // Paso 2: id del tipo de animal elegido.
    val animalTypeId: String? = null,
    // Paso 2: raza escrita por la persona.
    val breed: String = "",
    // Paso 2: tamaño del animal.
    val size: AnimalSize? = null,
    // Paso 2: edad escrita como texto (solo dígitos).
    val age: String = "",
    // Paso 2: unidad en la que se escribió la edad.
    val ageUnit: AgeUnit = AgeUnit.MONTHS,
    // Paso 2: indica si está vacunado.
    val vaccinated: Boolean = false,
    // Paso 2: indica si está esterilizado.
    val neutered: Boolean = false,

    // Paso 3: fotos (direcciones de las imágenes).
    val photos: List<String> = emptyList(),

    // Paso 4: true = "Ubicación actual" (permiso y GPS); false = un punto elegido en el mapa.
    val useCurrentLocation: Boolean = true,
    // Paso 4: ubicación actual obtenida del GPS con su dirección.
    val currentLocation: Location? = null,
    // Paso 4: indica si se está obteniendo la ubicación actual.
    val isLocating: Boolean = false,
    // No se pudo obtener la ubicación actual (permiso negado o sin señal).
    val locationUnavailable: Boolean = false,
    // Paso 4: ubicación elegida en el mapa.
    val mapLocation: Location? = null,

    // Categorías activas del catálogo (las define el moderador).
    val categories: List<Category> = emptyList(),
    // Tipos de animal activos del catálogo.
    val animalTypes: List<AnimalType> = emptyList(),

    // Campos que ya se tocaron (para mostrar sus errores).
    val touched: Set<CreatePostField> = emptySet(),
    // Indica si se está guardando la publicación.
    val isPublishing: Boolean = false,
    // Error general al publicar, si lo hubo.
    val publishError: PublishError? = null,
    // Id de la publicación creada o editada: mientras exista se muestra el diálogo de éxito.
    val savedPostId: String? = null,

    // --- Solo al editar ---
    // Indica si el formulario está en modo edición.
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

    // Error del título, visible solo si el campo ya se tocó.
    val titleError: FieldError? get() = shown(CreatePostField.TITLE, validateLength(title, PostValidator.MIN_TITLE_LENGTH, PostValidator.MAX_TITLE_LENGTH))
    // Error de la descripción, visible solo si el campo ya se tocó.
    val descriptionError: FieldError? get() = shown(CreatePostField.DESCRIPTION, validateLength(description, PostValidator.MIN_DESCRIPTION_LENGTH, PostValidator.MAX_DESCRIPTION_LENGTH))
    // Error de la raza, visible solo si el campo ya se tocó.
    val breedError: FieldError? get() = shown(CreatePostField.BREED, validateLength(breed, MIN_BREED_LENGTH, Int.MAX_VALUE))
    // Error de la edad, visible solo si el campo ya se tocó.
    val ageError: FieldError? get() = shown(CreatePostField.AGE, validateAge())

    // La ubicación de la publicación según la opción elegida.
    val postLocation: Location? get() = if (useCurrentLocation) currentLocation else mapLocation

    // La ubicación sirve si tiene coordenadas (regla de la publicación).
    val hasUsableLocation: Boolean get() = postLocation?.hasCoordinates() == true

    // Al crear siempre hay algo que enviar; al editar, solo si cambió algún dato.
    val hasChanges: Boolean get() = originalPost == null || toPost(originalPost) != originalPost

    /**
    Los datos del formulario sobre [base]: una publicación vacía al crear, la original al editar
    (así conserva su id, autor, fechas y contadores).
    **/
    fun toPost(base: Post = Post()): Post = base.copy(
        // Título sin espacios sobrantes.
        title = title.trim(),
        // Categoría elegida (vacía si no hay).
        categoryId = categoryId.orEmpty(),
        // Descripción sin espacios sobrantes.
        description = description.trim(),
        // Tipo de animal elegido.
        animalTypeId = animalTypeId,
        // Raza sin espacios sobrantes.
        breed = breed.trim(),
        // Tamaño elegido.
        size = size,
        // Edad en meses.
        ageInMonths = ageInMonths,
        // Vacunado.
        vaccinated = vaccinated,
        // Esterilizado.
        neutered = neutered,
        // Fotos elegidas.
        photoUrls = photos,
        // Ubicación elegida o una vacía si no hay.
        location = postLocation ?: Location(),
    )

    /**
    Habilita "Continuar"/"Publicar" según el paso: cada paso exige que sus campos sean válidos;
    el último además exige ubicación con coordenadas, que no se esté publicando y que haya cambios.
    **/
    val isStepValid: Boolean
        // Calcula la validez según el paso actual.
        get() = when (step) {
            // Paso 1: título y descripción válidos y categoría elegida.
            1 -> validateLength(title, PostValidator.MIN_TITLE_LENGTH, PostValidator.MAX_TITLE_LENGTH) == null &&
                categoryId != null &&
                validateLength(description, PostValidator.MIN_DESCRIPTION_LENGTH, PostValidator.MAX_DESCRIPTION_LENGTH) == null
            // Paso 2: tipo, tamaño, raza y edad válidos.
            2 -> animalTypeId != null && size != null && validateLength(breed, MIN_BREED_LENGTH, Int.MAX_VALUE) == null &&
                validateAge() == null
            // Paso 3: al menos una foto y sin pasar del máximo.
            3 -> photos.isNotEmpty() && photos.size <= PostValidator.MAX_PHOTOS
            // Paso 4: ubicación usable, no publicando y con cambios.
            else -> hasUsableLocation && !isPublishing && hasChanges
        }

    // Devuelve el error solo si el campo ya fue tocado; si no, null.
    private fun shown(field: CreatePostField, error: FieldError?) = if (field in touched) error else null

    // Valida la longitud de un texto entre un mínimo y un máximo.
    private fun validateLength(value: String, min: Int, max: Int): FieldError? {
        // Quita espacios al inicio y al final.
        val clean = value.trim()
        // Devuelve el error correspondiente o null si es válido.
        return when {
            // Vacío.
            clean.isEmpty() -> FieldError.EMPTY
            // Más corto que el mínimo.
            clean.length < min -> FieldError.TOO_SHORT
            // Más largo que el máximo.
            clean.length > max -> FieldError.TOO_LONG
            // Válido.
            else -> null
        }
    }

    // Valida que la edad exista y esté dentro del rango permitido.
    private fun validateAge(): FieldError? = when {
        // Vacía.
        age.isBlank() -> FieldError.EMPTY
        // No numérica o fuera del rango de 0 al máximo de meses.
        ageInMonths == null || ageInMonths!! !in 0..PostValidator.MAX_AGE_IN_MONTHS -> FieldError.INVALID
        // Válida.
        else -> null
    }

    // Constantes del estado.
    companion object {
        // La raza es libre, pero se pide algo más que una letra.
        const val MIN_BREED_LENGTH = 3
    }
}

/**
Crea una publicación nueva o, si la ruta trae "postId" (EditPost), edita esa publicación:
la carga, llena los pasos con sus datos y al final guarda con UpdatePostUseCase.
**/
@HiltViewModel
class CreatePostViewModel @Inject constructor(
    // Argumentos de navegación (postId opcional).
    savedStateHandle: SavedStateHandle,
    // Caso de uso que expone la sesión actual.
    private val observeSession: ObserveSessionUseCase,
    // Caso de uso que emite las categorías del catálogo.
    private val observeCategories: ObserveCategoriesUseCase,
    // Caso de uso que emite los tipos de animal del catálogo.
    private val observeAnimalTypes: ObserveAnimalTypesUseCase,
    // Caso de uso que carga el catálogo desde el repositorio.
    private val loadCatalog: LoadCatalogUseCase,
    // Caso de uso que crea una publicación.
    private val createPost: CreatePostUseCase,
    // Caso de uso que obtiene el detalle de una publicación.
    private val getPostDetail: GetPostDetailUseCase,
    // Caso de uso que actualiza una publicación existente.
    private val updatePost: UpdatePostUseCase,
    // Caso de uso que convierte coordenadas en dirección.
    private val reverseGeocode: ReverseGeocodeUseCase,
) : ViewModel() {

    // Publicación a editar; null = crear una nueva.
    private val editingPostId: String? = savedStateHandle["postId"]

    // Estado interno; en modo edición arranca cargando.
    private val _uiState = MutableStateFlow(CreatePostUiState(isEditing = editingPostId != null, isLoading = editingPostId != null))
    // Estado de solo lectura que observa la pantalla.
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    // Inicialización del ViewModel.
    init {
        // Carga el catálogo si aún no se ha cargado.
        viewModelScope.launch { if (observeCategories().value.isEmpty()) loadCatalog() }

        // Solo las opciones activas: las desactivadas no se ofrecen para publicaciones nuevas.
        viewModelScope.launch {
            // Combina categorías y tipos de animal en un solo flujo, filtrando los activos.
            combine(observeCategories(), observeAnimalTypes()) { categories, types ->
                // Devuelve el par de listas filtradas.
                categories.filter { it.active } to types.filter { it.active }
            // Recolecta cada combinación.
            }.collect { (categories, types) ->
                // Actualiza los catálogos en el estado.
                _uiState.update { it.copy(categories = categories, animalTypes = types) }
            }
        }

        // La ubicación de una publicación nueva se toma en el paso 4 (actual o en el mapa); al editar se carga la publicación.
        if (editingPostId != null) loadPostToEdit(editingPostId)
    }

    /**
    Carga la publicación y llena el formulario. Revisa lo mismo que UpdatePostUseCase
    (propia, en proceso y aún sin verificar) para no dejar llenar un formulario que no se va a poder guardar.
    Al editar se conserva la ubicación de la publicación.
    **/
    private fun loadPostToEdit(postId: String) {
        // Obtiene la sesión actual.
        val session = observeSession().value
        // Sin sesión no se puede editar.
        if (session == null) {
            // Marca el error de carga.
            _uiState.update { it.copy(isLoading = false, loadError = true) }
            // Termina.
            return
        }
        // Lanza una corrutina para cargar la publicación.
        viewModelScope.launch {
            // Obtiene la publicación (null si falla).
            val post = getPostDetail(session, postId).getOrNull()
            // Es editable si existe, es propia, está en proceso y aún no se verifica.
            val editable = post != null &&
                post.authorId == session.id &&
                post.status == PostStatus.IN_PROGRESS &&
                post.verification.status == VerificationStatus.UNVERIFIED
            // Si no es editable, marca error y termina.
            if (!editable) {
                // Marca el error de carga.
                _uiState.update { it.copy(isLoading = false, loadError = true) }
                // Sale de la corrutina.
                return@launch
            }
            // Años si la edad es exacta en años; si no, meses (el dominio la guarda en meses).
            val months = post.ageInMonths
            // Indica si la edad se puede expresar en años exactos.
            val inYears = months != null && months >= AgeUnit.YEARS.months && months % AgeUnit.YEARS.months == 0
            // Llena el formulario con los datos de la publicación.
            _uiState.update { state ->
                // Copia del estado con todos los campos llenos.
                val filled = state.copy(
                    // Termina la carga.
                    isLoading = false,
                    // Título.
                    title = post.title,
                    // Categoría.
                    categoryId = post.categoryId,
                    // Descripción.
                    description = post.description,
                    // Tipo de animal.
                    animalTypeId = post.animalTypeId,
                    // Raza.
                    breed = post.breed,
                    // Tamaño.
                    size = post.size,
                    // Edad como texto en años o meses.
                    age = months?.let { if (inYears) (it / AgeUnit.YEARS.months).toString() else it.toString() }.orEmpty(),
                    // Unidad de la edad.
                    ageUnit = if (inYears) AgeUnit.YEARS else AgeUnit.MONTHS,
                    // Vacunado (si no se sabe, "no").
                    vaccinated = post.vaccinated ?: false,
                    // Esterilizado (si no se sabe, "no").
                    neutered = post.neutered ?: false,
                    // Fotos.
                    photos = post.photoUrls,
                    // Al editar se parte del punto guardado, en el mapa.
                    useCurrentLocation = false,
                    // Ubicación guardada.
                    mapLocation = post.location,
                )
                // La referencia pasa por el formulario: así un "no se sabe" (null) que el formulario
                // muestra como "no" no cuenta como cambio.
                filled.copy(originalPost = filled.toPost(post))
            }
        }
    }

    // --- Paso 1 ---

    // Actualiza el título (recortado al máximo) y lo marca como tocado.
    fun onTitleChange(value: String) = _uiState.update {
        // Copia con el nuevo título.
        it.copy(title = value.take(PostValidator.MAX_TITLE_LENGTH), touched = it.touched + CreatePostField.TITLE)
    }

    // Guarda el id de la categoría según la posición elegida.
    fun onCategorySelected(index: Int) = _uiState.update { it.copy(categoryId = it.categories.getOrNull(index)?.id) }

    // Actualiza la descripción (recortada al máximo) y la marca como tocada.
    fun onDescriptionChange(value: String) = _uiState.update {
        // Copia con la nueva descripción.
        it.copy(description = value.take(PostValidator.MAX_DESCRIPTION_LENGTH), touched = it.touched + CreatePostField.DESCRIPTION)
    }

    // --- Paso 2 ---

    // Guarda el id del tipo de animal según la posición elegida.
    fun onAnimalTypeSelected(index: Int) = _uiState.update { it.copy(animalTypeId = it.animalTypes.getOrNull(index)?.id) }

    // Actualiza la raza y la marca como tocada.
    fun onBreedChange(value: String) = _uiState.update { it.copy(breed = value, touched = it.touched + CreatePostField.BREED) }

    // Guarda el tamaño según la posición elegida.
    fun onSizeSelected(index: Int) = _uiState.update { it.copy(size = AnimalSize.entries.getOrNull(index)) }

    // Solo dígitos (máximo 3): así nunca queda una edad imposible de convertir.
    fun onAgeChange(value: String) = _uiState.update {
        // Copia con la edad filtrada y marcada como tocada.
        it.copy(age = value.filter(Char::isDigit).take(3), touched = it.touched + CreatePostField.AGE)
    }

    // Guarda la unidad de edad elegida (meses por defecto).
    fun onAgeUnitSelected(index: Int) = _uiState.update { it.copy(ageUnit = AgeUnit.entries.getOrElse(index) { AgeUnit.MONTHS }) }

    // Actualiza si está vacunado.
    fun onVaccinatedChange(value: Boolean) = _uiState.update { it.copy(vaccinated = value) }

    // Actualiza si está esterilizado.
    fun onNeuteredChange(value: Boolean) = _uiState.update { it.copy(neutered = value) }

    // --- Paso 3 ---

    /**
    Agrega las fotos elegidas con la cámara o la galería (ya guardadas en la app), sin pasar del máximo.
    TODO (Cloudinary): subirlas y guardar su URL en lugar de la dirección local.
    **/
    fun onAddPhotos(urls: List<String>) = _uiState.update { state ->
        // Une las fotos y recorta al máximo permitido.
        state.copy(photos = (state.photos + urls).take(PostValidator.MAX_PHOTOS))
    }

    // Quita la foto en la posición indicada.
    fun onRemovePhoto(index: Int) = _uiState.update { it.copy(photos = it.photos.filterIndexed { i, _ -> i != index }) }

    // --- Paso 4 ---

    // Cambia entre ubicación actual y mapa, y limpia el error de publicación.
    fun onUseCurrentLocationChange(value: Boolean) = _uiState.update { it.copy(useCurrentLocation = value, publishError = null) }

    // La pantalla pidió el permiso y está leyendo el GPS.
    fun onLocatingStarted() = _uiState.update { it.copy(isLocating = true, locationUnavailable = false) }

    /**
    Ubicación actual del teléfono (o null si no se pudo): se le busca la dirección.
    **/
    fun onCurrentLocationResult(latitude: Double?, longitude: Double?) {
        // Si no hay coordenadas, marca que la ubicación no está disponible.
        if (latitude == null || longitude == null) {
            // Termina la localización con el aviso.
            _uiState.update { it.copy(isLocating = false, locationUnavailable = true) }
            // Termina.
            return
        }
        // Lanza una corrutina para buscar la dirección.
        viewModelScope.launch {
            // Convierte las coordenadas en una ubicación con dirección.
            val location = reverseGeocode(latitude, longitude)
            // Guarda la ubicación actual y termina la localización.
            _uiState.update { it.copy(isLocating = false, currentLocation = location, publishError = null) }
        }
    }

    // Guarda el punto elegido en el mapa y limpia el error de publicación.
    fun onMapLocationPicked(location: Location) = _uiState.update { it.copy(mapLocation = location, publishError = null) }

    // --- Navegación entre pasos y publicación ---

    // Avanza al siguiente paso o publica si es el último.
    fun onNextClick() {
        // Toma el estado actual.
        val state = _uiState.value
        // Si el paso no es válido no avanza.
        if (!state.isStepValid) {
            // Muestra los errores de los campos del paso aunque no se hayan tocado.
            _uiState.update { it.copy(touched = it.touched + CreatePostField.entries) }
            // Termina.
            return
        }
        // Si no es el último paso, avanza.
        if (state.step < CREATE_POST_STEPS) {
            // Incrementa el paso.
            _uiState.update { it.copy(step = it.step + 1) }
        } else {
            // En el último paso, publica.
            publish(state)
        }
    }

    // Retrocede un paso (si no está en el primero) y limpia el error de publicación.
    fun previousStep() = _uiState.update { if (it.step > 1) it.copy(step = it.step - 1, publishError = null) else it }

    // Crea o actualiza la publicación con los datos del formulario.
    private fun publish(state: CreatePostUiState) {
        // Obtiene la sesión; sin sesión no publica.
        val session = observeSession().value ?: return
        // Marca que está publicando y limpia errores.
        _uiState.update { it.copy(isPublishing = true, publishError = null) }

        // Lanza una corrutina para guardar.
        viewModelScope.launch {
            // Publicación original (null si se está creando).
            val original = state.originalPost
            // Crear: el caso de uso fuerza el resto (sin verificar, en proceso, contadores en cero).
            // Editar: el caso de uso reinicia la verificación, así vuelve a revisión.
            val result = if (original == null) {
                // Crea y devuelve el id de la nueva publicación.
                createPost(session.id, state.toPost()).map { it.id }
            } else {
                // Actualiza y devuelve el id de la original.
                updatePost(session.id, state.toPost(original)).map { original.id }
            }
            // Procesa el resultado.
            result
                // Si salió bien, guarda el id (abre el diálogo de éxito).
                .onSuccess { postId -> _uiState.update { it.copy(isPublishing = false, savedPostId = postId) } }
                // Si falló, traduce el error.
                .onFailure { error ->
                    // Convierte la excepción del dominio en un error de la pantalla.
                    val publishError = when (error) {
                        // Ubicación no válida.
                        PostException.InvalidLocation -> PublishError.LOCATION
                        // Categoría o tipo de animal inválido.
                        is CatalogException -> PublishError.CATALOG
                        // La publicación ya no se puede editar.
                        PostException.NotAllowed, PostException.PostNotActive,
                        PostException.AlreadyReviewed, PostException.PostNotFound -> PublishError.NOT_EDITABLE
                        // Cualquier otro error.
                        else -> PublishError.GENERIC
                    }
                    // Quita la bandera de publicación y guarda el error.
                    _uiState.update { it.copy(isPublishing = false, publishError = publishError) }
                }
        }
    }
}
