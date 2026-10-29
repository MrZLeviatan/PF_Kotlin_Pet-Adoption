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

/** Motivos frecuentes de rechazo. La pantalla decide el texto según el tipo. */
enum class RejectReason {
    INSUFFICIENT_INFO,
    INVALID_IMAGE,
    SUSPICIOUS_CONTENT,
    POSSIBLE_ANIMAL_SALE,
    WRONG_LOCATION,
    DUPLICATED,
}

/** Error de la descripción; el texto (con el mínimo) lo pone la pantalla. */
enum class DescriptionError { EMPTY, TOO_SHORT }

data class RejectPostUiState(
    val selectedReason: RejectReason? = null,
    val description: String = "",
    // La descripción ya se tocó: desde ahí se muestra su error (como en los demás formularios).
    val descriptionTouched: Boolean = false,
    val isRejecting: Boolean = false,
    val hasError: Boolean = false,
) {
    // No vacía (sin contar espacios) y con el mínimo de caracteres del validador.
    private val descriptionValidation: DescriptionError?
        get() = when {
            description.isBlank() -> DescriptionError.EMPTY
            description.trim().length < PostValidator.MIN_REJECTION_DESCRIPTION_LENGTH -> DescriptionError.TOO_SHORT
            else -> null
        }

    val descriptionError: DescriptionError? get() = if (descriptionTouched) descriptionValidation else null

    // Solo habilita el botón con motivo, descripción válida y sin un envío en curso.
    val isFormFilled: Boolean
        get() = selectedReason != null && descriptionValidation == null && !isRejecting

    companion object {
        const val MAX_DESCRIPTION_LENGTH = 500
    }
}

/** Eventos de un solo uso. */
sealed interface RejectPostEvent {
    data object Rejected : RejectPostEvent
}

/**
 Rechazo de una publicación por el moderador que la revisa. El postId llega de la ruta MainRoutes.RejectPost.
 El motivo que guarda RejectPostUseCase (y ve el autor) es "Motivo. Descripción".
 */
@HiltViewModel
class RejectPostViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val rejectPost: RejectPostUseCase,
) : ViewModel() {

    private val postId: String = savedStateHandle["postId"] ?: ""

    private val _uiState = MutableStateFlow(RejectPostUiState())
    val uiState: StateFlow<RejectPostUiState> = _uiState.asStateFlow()

    private val _events = Channel<RejectPostEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onReasonSelected(reason: RejectReason) = _uiState.update { it.copy(selectedReason = reason) }

    // take: el límite se aplica aquí, así también cubre el pegado de texto largo.
    fun onDescriptionChange(value: String) = _uiState.update {
        it.copy(description = value.take(RejectPostUiState.MAX_DESCRIPTION_LENGTH), descriptionTouched = true)
    }

    /** [reasonLabel] es el texto del motivo elegido (lo resuelve la pantalla con strings.xml). */
    fun onConfirmClick(reasonLabel: String) {
        val state = _uiState.value
        if (!state.isFormFilled) return
        _uiState.update { it.copy(isRejecting = true, hasError = false) }
        viewModelScope.launch {
            // El caso de uso valida el motivo, rechaza y notifica al autor.
            rejectPost(postId, "$reasonLabel. ${state.description.trim()}")
                .onSuccess {
                    _uiState.update { it.copy(isRejecting = false) }
                    _events.send(RejectPostEvent.Rejected)
                }
                .onFailure { _uiState.update { it.copy(isRejecting = false, hasError = true) } }
        }
    }
}