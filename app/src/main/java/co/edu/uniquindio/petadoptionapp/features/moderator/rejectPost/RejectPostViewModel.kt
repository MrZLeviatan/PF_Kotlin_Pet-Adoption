package co.edu.uniquindio.petadoptionapp.features.moderator.rejectpost

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

/** Motivos frecuentes de rechazo. La pantalla decide el texto según el tipo. */
enum class RejectReason {
    INSUFFICIENT_INFO,
    INVALID_IMAGE,
    SUSPICIOUS_CONTENT,
    POSSIBLE_ANIMAL_SALE,
    WRONG_LOCATION,
    DUPLICATED,
}

data class RejectPostUiState(
    val selectedReason: RejectReason? = null,
    val description: String = "",
) {
    // Solo habilita el botón cuando hay motivo y texto. Más validaciones vienen después.
    val isFormFilled: Boolean
        get() = selectedReason != null && description.isNotBlank()

    companion object {
        const val MAX_DESCRIPTION_LENGTH = 500
    }
}

/** Eventos de un solo uso. */
sealed interface RejectPostEvent {
    data object Rejected : RejectPostEvent
}

class RejectPostViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RejectPostUiState())
    val uiState: StateFlow<RejectPostUiState> = _uiState.asStateFlow()

    private val _events = Channel<RejectPostEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onReasonSelected(reason: RejectReason) = _uiState.update { it.copy(selectedReason = reason) }

    // take: el límite se aplica aquí, así también cubre el pegado de texto largo.
    fun onDescriptionChange(value: String) = _uiState.update {
        it.copy(description = value.take(RejectPostUiState.MAX_DESCRIPTION_LENGTH))
    }

    fun onConfirmClick() {
        if (!_uiState.value.isFormFilled) return
        // TEMPORAL: aquí irá el caso de uso de rechazo (motivo + descripción → notificar al usuario).
        _events.trySend(RejectPostEvent.Rejected)
    }
}