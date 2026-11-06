package co.edu.uniquindio.petadoptionapp.features.auth.newpassword

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

/**
ViewModel de la pantalla para definir una nueva contraseña.
Estado de UI (NewPasswordUiState): la contraseña, su confirmación y si se muestra el diálogo de éxito.
Evento de un solo uso (NewPasswordEvent.PasswordUpdated): se emite al aceptar el diálogo; la pantalla
lo traduce en onPasswordUpdated para que AppNavigation regrese al login.
Por ahora no llama ningún caso de uso ni se inyecta con Hilt: el cambio real de contraseña lo hace
Firebase desde el enlace del correo, y aquí solo se simula el flujo visual (ver TEMPORAL abajo).
**/

// Estado de la pantalla de nueva contraseña.
data class NewPasswordUiState(
    // Nueva contraseña escrita.
    val password: String = "",
    // Repetición de la contraseña para confirmarla.
    val confirmPassword: String = "",
    // true para mostrar el diálogo de "contraseña actualizada".
    val showSuccessDialog: Boolean = false,
) {
    // Solo habilita el botón cuando hay algo escrito. Las validaciones reales vienen después.
    val isFormFilled: Boolean
        // Ambos campos deben tener contenido.
        get() = password.isNotBlank() && confirmPassword.isNotBlank()
}

// Eventos de un solo uso.
sealed interface NewPasswordEvent {
    // La contraseña se actualizó y el usuario aceptó el diálogo.
    data object PasswordUpdated : NewPasswordEvent
}

// ViewModel sin dependencias (se crea con viewModel(), no con Hilt).
class NewPasswordViewModel : ViewModel() {

    // Estado mutable interno.
    private val _uiState = MutableStateFlow(NewPasswordUiState())
    // Estado de solo lectura que observa la pantalla.
    val uiState: StateFlow<NewPasswordUiState> = _uiState.asStateFlow()

    // Canal con búfer para los eventos de un solo uso.
    private val _events = Channel<NewPasswordEvent>(Channel.BUFFERED)
    // Flujo de eventos que recolecta la pantalla.
    val events = _events.receiveAsFlow()

    // Actualiza la nueva contraseña.
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value) }
    // Actualiza la confirmación de la contraseña.
    fun onConfirmPasswordChange(value: String) = _uiState.update { it.copy(confirmPassword = value) }

    // Se ejecuta al pulsar el botón de confirmar.
    fun onConfirmClick() {
        // TEMPORAL: aquí irá el caso de uso de cambio de contraseña.
        // Por ahora muestra directamente el diálogo de éxito.
        _uiState.update { it.copy(showSuccessDialog = true) }
    }

    // El usuario aceptó el diálogo de éxito.
    fun onDialogAccept() {
        // Oculta el diálogo.
        _uiState.update { it.copy(showSuccessDialog = false) }
        // Emite el evento para que la pantalla navegue.
        _events.trySend(NewPasswordEvent.PasswordUpdated)
    }
}
