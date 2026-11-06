package co.edu.uniquindio.petadoptionapp.features.auth.recoverpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.SendPasswordResetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Errores al pedir el correo de recuperación. */
enum class RecoverPasswordError {
    INVALID_EMAIL,
    GENERIC
}

data class RecoverPasswordUiState(
    val email: String = "",
    val isSending: Boolean = false,
    val error: RecoverPasswordError? = null,
    val showSuccessDialog: Boolean = false,
) {
    // Solo habilita el botón cuando hay algo escrito. El formato lo revisa el caso de uso.
    val isEmailFilled: Boolean get() = email.isNotBlank()
}

/** Eventos de un solo uso. */
sealed interface RecoverPasswordEvent {
    data object EmailSent : RecoverPasswordEvent
}

/**
 "Olvidé mi contraseña": Firebase envía al correo un enlace para escribir una contraseña nueva.
 Por seguridad, la pantalla dice lo mismo exista o no la cuenta.
 */
@HiltViewModel
class RecoverPasswordViewModel @Inject constructor(
    private val sendPasswordReset: SendPasswordResetUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecoverPasswordUiState())
    val uiState: StateFlow<RecoverPasswordUiState> = _uiState.asStateFlow()

    private val _events = Channel<RecoverPasswordEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, error = null) }

    fun onSendClick() {
        val state = _uiState.value
        if (!state.isEmailFilled || state.isSending) return
        _uiState.update { it.copy(isSending = true, error = null) }

        viewModelScope.launch {
            sendPasswordReset(state.email)
                .onSuccess { _uiState.update { it.copy(isSending = false, showSuccessDialog = true) } }
                .onFailure { error ->
                    val uiError = if (error === AuthException.InvalidEmail) RecoverPasswordError.INVALID_EMAIL
                    else RecoverPasswordError.GENERIC
                    _uiState.update { it.copy(isSending = false, error = uiError) }
                }
        }
    }

    fun onDialogAccept() {
        _uiState.update { it.copy(showSuccessDialog = false) }
        _events.trySend(RecoverPasswordEvent.EmailSent)
    }
}
