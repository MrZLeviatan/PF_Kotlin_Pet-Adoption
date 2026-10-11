package co.edu.uniquindio.petadoptionapp.features.user.post.create

// Importa la clase base ViewModel de Android Architecture Components.
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

// Constante global que define el número máximo de fotos permitidas en la publicación.
const val MAX_PHOTOS = 4

// Clase de datos que almacena todo lo que el formulario necesita recordar y mostrar en la UI.
data class CreatePostUiState(
    // Almacena el número del paso actual del formulario (del 1 al 4).
    val step: Int = 1,

    // Campos correspondientes al Paso 1
    val title: String = "",
    val category: String = "",
    val description: String = "",

    // Campos correspondientes al Paso 2
    val animalType: String = "",
    val breed: String = "",
    val size: String = "",
    val age: String = "",
    val vaccinated: Boolean = false,
    val sterilized: Boolean = false,

    // Campos correspondientes al Paso 3
    // Lista que almacena los enlaces de internet de las fotos agregadas.
    val photos: List<String> = emptyList(),

    // Campos correspondientes al Paso 4
    // Define si usa ubicación actual (true) o selección por mapa (false).
    val useCurrentLocation: Boolean = false,

    // Controla si se debe mostrar el cuadro de diálogo de éxito al finalizar.
    val showSuccessDialog: Boolean = false,

    // Campos que el usuario ya tocó (para mostrar sus errores)
    val touched: Set<String> = emptySet()
) {

    // Reglas de validación

    // Válida que el texto del título tenga al menos 5 caracteres ignorando espacios vacíos.
    val isTitleValid: Boolean get() = title.trim().length >= 5
    // Válida que la descripción tenga al menos 10 caracteres ignorando espacios vacíos.
    val isDescriptionValid: Boolean get() = description.trim().length >= 10
    // Válida que la raza tenga al menos 3 caracteres ignorando espacios vacíos.
    val isBreedValid: Boolean get() = breed.trim().length >= 3
    // Válida que el texto de la edad contenga al menos un dígito numérico.
    val isAgeValid: Boolean get() = age.any { it.isDigit() }


    // Mensajes de error (retornan null si no hay error)

    // Evalúa el estado del título y retorna un mensaje de error si no cumple las reglas.
    val titleError: String? get() = when {
        // Si el campo no ha sido tocado por el usuario, no muestra error.
        "title" !in touched -> null
        // Si está vacío, indica que es obligatorio.
        title.isBlank() -> "El título es obligatorio"
        // Si no cumple la longitud mínima, muestra la restricción.
        !isTitleValid -> "Mínimo 5 caracteres"
        else -> null
    }

    // Evalúa el estado de la descripción y retorna un mensaje de error si corresponde.
    val descriptionError: String? get() = when {
        // Si la descripción no ha sido tocada, no muestra error.
        "description" !in touched -> null
        // Si está vacía, indica que es obligatoria.
        description.isBlank() -> "La descripción es obligatoria"
        // Si no cumple el mínimo de caracteres, avisa al usuario.
        !isDescriptionValid -> "Mínimo 10 caracteres"
        else -> null
    }

    // Evalúa el estado de la raza y retorna un mensaje de error si corresponde.
    val breedError: String? get() = when {
        // Si el campo raza no ha sido tocado, no muestra error.
        "breed" !in touched -> null
        // Si está vacío, muestra que es obligatorio.
        breed.isBlank() -> "La raza es obligatoria"
        // Si es muy corta, indica el límite mínimo.
        !isBreedValid -> "Mínimo 3 caracteres"
        else -> null
    }

    // Evalúa el estado de la edad y retorna un mensaje de error si corresponde.
    val ageError: String? get() = when {
        // Si la edad no ha sido tocada, no muestra error.
        "age" !in touched -> null
        // Si está en blanco, indica que es obligatorio.
        age.isBlank() -> "La edad es obligatoria"
        // Si no contiene un número, muestra un ejemplo de formato correcto.
        !isAgeValid -> "Incluye un número, ej: 2 años"
        else -> null
    }

    // Determina si se puede avanzar en el paso actual (activa o desactiva el botón de siguiente).
    val isStepValid: Boolean
        get() = when (step) {
            // En el paso 1 exige título válido, categoría seleccionada y descripción válida.
            1 -> isTitleValid && category.isNotEmpty() && isDescriptionValid
            // En el paso 2 exige tipo de animal, raza válida, tamaño y edad válida.
            2 -> animalType.isNotEmpty() && isBreedValid && size.isNotEmpty() && isAgeValid
            // En el paso 3 exige que haya al menos una foto cargada.
            3 -> photos.isNotEmpty()
            // El paso 4 no requiere validaciones estrictas y retorna verdadero.
            else -> true
        }
}


// ViewModel encargado de gestionar la lógica y el estado del formulario de creación de publicaciones.
class CreatePostViewModel : ViewModel() {

    // Flujo privado mutable para gestionar los cambios internos de estado en el ViewModel.
    private val _uiState = MutableStateFlow(CreatePostUiState())
    // Flujo público de solo lectura para exponer el estado a la interfaz de usuario.
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()


    // Métodos de actualización del Paso 1

    // Actualiza el título limitándolo a 60 caracteres y añade "title" al conjunto de campos tocados.
    fun onTitleChange(value: String) =
        _uiState.update { it.copy(title = value.take(60), touched = it.touched + "title") }

    // Actualiza la categoría seleccionada en el formulario.
    fun onCategoryChange(value: String) = _uiState.update { it.copy(category = value) }

    // Actualiza la descripción limitándola a 500 caracteres y añade "description" a los tocados.
    fun onDescriptionChange(value: String) =
        _uiState.update {
            it.copy(
                description = value.take(500),
                touched = it.touched + "description"
            )
        }


    // Métodos de actualización del Paso 2

    // Actualiza el tipo de animal seleccionado.
    fun onAnimalTypeChange(value: String) = _uiState.update { it.copy(animalType = value) }

    // Actualiza la raza ingresada y añade "breed" al conjunto de campos tocados.
    fun onBreedChange(value: String) =
        _uiState.update { it.copy(breed = value, touched = it.touched + "breed") }

    // Actualiza el tamaño de la mascota.
    fun onSizeChange(value: String) = _uiState.update { it.copy(size = value) }

    // Actualiza la edad ingresada y añade "age" al conjunto de campos tocados.
    fun onAgeChange(value: String) =
        _uiState.update { it.copy(age = value, touched = it.touched + "age") }

    // Actualiza el estado booleano de vacunación.
    fun onVaccinatedChange(value: Boolean) = _uiState.update { it.copy(vaccinated = value) }
    // Actualiza el estado booleano de esterilización.
    fun onSterilizedChange(value: Boolean) = _uiState.update { it.copy(sterilized = value) }


    // Paso 3: Agrega una foto aleatoria de internet simulada (Por el momento)
    fun addRandomPhoto() {
        _uiState.update { state ->
            // Si la lista ya alcanzó o superó el límite máximo de fotos, no hace nada.
            if (state.photos.size >= MAX_PHOTOS) state
            else state.copy(
                // Añade una URL de imagen aleatoria generada con un número entero random.
                photos = state.photos + "https://picsum.photos/seed/${
                    Random.nextInt(
                        10000
                    )
                }/400/400"
            )
        }
    }

    // Remueve una foto específica de la lista según el índice de posición recibido.
    fun removePhoto(index: Int) {
        _uiState.update { it.copy(photos = it.photos.filterIndexed { i, _ -> i != index }) }
    }


    // Método de actualización del Paso 4

    // Actualiza la preferencia para usar la ubicación actual o seleccionar en el mapa.
    fun onUseCurrentLocationChange(value: Boolean) =
        _uiState.update { it.copy(useCurrentLocation = value) }


    // Control de navegación entre los pasos del formulario
    fun onNextClick() {
        val state = _uiState.value
        // Si los datos del paso actual no son válidos, detiene la ejecución.
        if (!state.isStepValid) return
        // Si no está en el último paso, incrementa el número de paso en 1.
        if (state.step < 4) _uiState.update { it.copy(step = it.step + 1) }
        // Si se encuentra en el último paso, activa la bandera para mostrar el diálogo de éxito.
        else _uiState.update { it.copy(showSuccessDialog = true) }
    }

    // Retrocede al paso anterior si el paso actual es mayor a 1.
    fun previousStep() {
        _uiState.update { if (it.step > 1) it.copy(step = it.step - 1) else it }
    }

    // Restablece el formulario completo volviendo a su estado inicial vacío.
    fun resetForm() {
        _uiState.value = CreatePostUiState()
    }
}