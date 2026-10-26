package co.edu.uniquindio.petadoptionapp.features.newpassword

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

data class NewPasswordUiState(
    val password: String = "",
    val confirmPassword: String = "",
    val showSuccessDialog: Boolean = false,
) {
    // Solo habilita el botón cuando hay algo escrito. Las validaciones reales vienen después.
    val isFormFilled: Boolean
        get() = password.isNotBlank() && confirmPassword.isNotBlank()
}

/** Eventos de un solo uso. */
sealed interface NewPasswordEvent {
    data object PasswordUpdated : NewPasswordEvent
}

class NewPasswordViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NewPasswordUiState())
    val uiState: StateFlow<NewPasswordUiState> = _uiState.asStateFlow()

    private val _events = Channel<NewPasswordEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value) }
    fun onConfirmPasswordChange(value: String) = _uiState.update { it.copy(confirmPassword = value) }

    fun onConfirmClick() {
        // TEMPORAL: aquí irá el caso de uso de cambio de contraseña.
        _uiState.update { it.copy(showSuccessDialog = true) }
    }

    fun onDialogAccept() {
        _uiState.update { it.copy(showSuccessDialog = false) }
        _events.trySend(NewPasswordEvent.PasswordUpdated)
    }
}