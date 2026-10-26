package co.edu.uniquindio.petadoptionapp.features.recoverpassword

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

data class RecoverPasswordUiState(
    val email: String = "",
    val showSuccessDialog: Boolean = false,
) {
    // Solo habilita el botón cuando hay algo escrito. La validación real viene después.
    val isEmailFilled: Boolean get() = email.isNotBlank()
}

/** Eventos de un solo uso. */
sealed interface RecoverPasswordEvent {
    data object EmailSent : RecoverPasswordEvent
}

class RecoverPasswordViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RecoverPasswordUiState())
    val uiState: StateFlow<RecoverPasswordUiState> = _uiState.asStateFlow()

    private val _events = Channel<RecoverPasswordEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value) }

    fun onSendClick() {
        // TEMPORAL: aquí irá el caso de uso (isLoading → llamada → resultado).
        _uiState.update { it.copy(showSuccessDialog = true) }
    }

    fun onDialogAccept() {
        _uiState.update { it.copy(showSuccessDialog = false) }
        _events.trySend(RecoverPasswordEvent.EmailSent)
    }
}