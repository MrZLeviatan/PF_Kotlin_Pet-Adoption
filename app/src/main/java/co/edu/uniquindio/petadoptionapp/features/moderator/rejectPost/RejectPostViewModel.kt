package co.edu.uniquindio.petadoptionapp.features.moderator.rejectpost

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.RejectPostUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.PostValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
ViewModel de la pantalla de rechazo de publicaciones (RejectPostScreen).
Estado de UI (RejectPostUiState): motivo elegido, descripción escrita, si la descripción ya se tocó
(para mostrar su error), si hay un rechazo en curso y si el último intento falló. Expone además
descriptionError e isFormFilled, calculados a partir de esos campos con las reglas de PostValidator.
Eventos de entrada: onReasonSelected, onDescriptionChange y onConfirmClick.
Evento de salida (de un solo uso, por Channel): RejectPostEvent.Rejected, que la pantalla usa para volver atrás.
Caso de uso: RejectPostUseCase, que guarda el motivo "Motivo. Descripción" y notifica al autor.
Navegación: recibe el postId desde los argumentos de la ruta MainRoutes.RejectPost vía SavedStateHandle.
**/

/** Motivos frecuentes de rechazo. La pantalla decide el texto según el tipo. */
enum class RejectReason {
    // Falta información en la publicación.
    INSUFFICIENT_INFO,
    // Las imágenes no son válidas.
    INVALID_IMAGE,
    // El contenido parece sospechoso.
    SUSPICIOUS_CONTENT,
    // Posible venta de animales.
    POSSIBLE_ANIMAL_SALE,
    // La ubicación es incorrecta.
    WRONG_LOCATION,
    // La publicación está duplicada.
    DUPLICATED,
}

/** Error de la descripción; el texto (con el mínimo) lo pone la pantalla. */
enum class DescriptionError { EMPTY, TOO_SHORT }

// Estado inmutable del formulario de rechazo.
data class RejectPostUiState(
    // Motivo seleccionado (null si aún no se elige).
    val selectedReason: RejectReason? = null,
    // Texto de la descripción escrita por el moderador.
    val description: String = "",
    // La descripción ya se tocó: desde ahí se muestra su error (como en los demás formularios).
    val descriptionTouched: Boolean = false,
    // Indica si se está enviando el rechazo.
    val isRejecting: Boolean = false,
    // Indica si el último intento de rechazo falló.
    val hasError: Boolean = false,
) {
    // No vacía (sin contar espacios) y con el mínimo de caracteres del validador.
    private val descriptionValidation: DescriptionError?
        // Calcula el error de la descripción cada vez que se consulta.
        get() = when {
            // Vacía o solo espacios.
            description.isBlank() -> DescriptionError.EMPTY
            // Más corta que el mínimo permitido.
            description.trim().length < PostValidator.MIN_REJECTION_DESCRIPTION_LENGTH -> DescriptionError.TOO_SHORT
            // Válida.
            else -> null
        }

    // Error visible: solo se muestra después de que el usuario tocó la descripción.
    val descriptionError: DescriptionError? get() = if (descriptionTouched) descriptionValidation else null

    // Solo habilita el botón con motivo, descripción válida y sin un envío en curso.
    val isFormFilled: Boolean
        // Combina las tres condiciones.
        get() = selectedReason != null && descriptionValidation == null && !isRejecting

    // Constantes del formulario.
    companion object {
        // Longitud máxima de la descripción.
        const val MAX_DESCRIPTION_LENGTH = 500
    }
}

/** Eventos de un solo uso. */
sealed interface RejectPostEvent {
    // El rechazo se guardó correctamente.
    data object Rejected : RejectPostEvent
}

/**
 Rechazo de una publicación por el moderador que la revisa. El postId llega de la ruta MainRoutes.RejectPost.
 El motivo que guarda RejectPostUseCase (y ve el autor) es "Motivo. Descripción".
 */
// Hilt crea e inyecta este ViewModel.
@HiltViewModel
class RejectPostViewModel @Inject constructor(
    // Argumentos de navegación de la ruta.
    savedStateHandle: SavedStateHandle,
    // Caso de uso que rechaza la publicación y notifica al autor.
    private val rejectPost: RejectPostUseCase,
) : ViewModel() {

    // Id de la publicación a rechazar, leído de la ruta (vacío si no llega).
    private val postId: String = savedStateHandle["postId"] ?: ""

    // Estado mutable interno del formulario.
    private val _uiState = MutableStateFlow(RejectPostUiState())
    // Estado público de solo lectura que observa la UI.
    val uiState: StateFlow<RejectPostUiState> = _uiState.asStateFlow()

    // Canal de eventos de un solo uso (con búfer para no perderlos).
    private val _events = Channel<RejectPostEvent>(Channel.BUFFERED)
    // Flujo de eventos que la pantalla recolecta.
    val events = _events.receiveAsFlow()

    // Guarda el motivo elegido en el estado.
    fun onReasonSelected(reason: RejectReason) = _uiState.update { it.copy(selectedReason = reason) }

    // take: el límite se aplica aquí, así también cubre el pegado de texto largo.
    fun onDescriptionChange(value: String) = _uiState.update {
        // Guarda el texto recortado al máximo y marca la descripción como tocada.
        it.copy(description = value.take(RejectPostUiState.MAX_DESCRIPTION_LENGTH), descriptionTouched = true)
    }

    /** [reasonLabel] es el texto del motivo elegido (lo resuelve la pantalla con strings.xml). */
    fun onConfirmClick(reasonLabel: String) {
        // Toma una foto del estado actual.
        val state = _uiState.value
        // Si el formulario no es válido, no hace nada.
        if (!state.isFormFilled) return
        // Marca el envío en curso y limpia el error previo.
        _uiState.update { it.copy(isRejecting = true, hasError = false) }
        // Ejecuta el rechazo en una corrutina.
        viewModelScope.launch {
            // El caso de uso valida el motivo, rechaza y notifica al autor.
            rejectPost(postId, "$reasonLabel. ${state.description.trim()}")
                // Si salió bien, termina el envío y emite el evento de rechazo.
                .onSuccess {
                    // Quita la bandera de envío.
                    _uiState.update { it.copy(isRejecting = false) }
                    // Avisa a la pantalla que ya puede salir.
                    _events.send(RejectPostEvent.Rejected)
                }
                // Si falló, termina el envío y muestra el error.
                .onFailure { _uiState.update { it.copy(isRejecting = false, hasError = true) } }
        }
    }
}
