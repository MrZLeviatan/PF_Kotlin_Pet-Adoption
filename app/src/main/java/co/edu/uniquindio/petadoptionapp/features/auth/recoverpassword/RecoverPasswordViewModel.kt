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

/**
ViewModel de la pantalla "Olvidé mi contraseña".
Estado de UI (RecoverPasswordUiState): correo escrito, bandera de envío en curso, error a mostrar
y si debe aparecer el diálogo de éxito.
Evento de un solo uso (RecoverPasswordEvent.EmailSent): se emite cuando el usuario acepta el
diálogo de éxito; la pantalla lo traduce en onEmailSent y AppNavigation regresa al login.
Usa el caso de uso SendPasswordResetUseCase, que valida el formato del correo y pide a Firebase
el envío del enlace de restablecimiento.
**/

// Errores al pedir el correo de recuperación.
enum class RecoverPasswordError {
    // El correo no tiene un formato válido.
    INVALID_EMAIL,
    // Cualquier otro fallo (red, servidor, etc.).
    GENERIC
}

// Estado de la pantalla de recuperación de contraseña.
data class RecoverPasswordUiState(
    // Correo escrito por el usuario.
    val email: String = "",
    // true mientras se envía la solicitud.
    val isSending: Boolean = false,
    // Error a mostrar bajo el campo; null si no hay.
    val error: RecoverPasswordError? = null,
    // true para mostrar el diálogo de "correo enviado".
    val showSuccessDialog: Boolean = false,
) {
    // Solo habilita el botón cuando hay algo escrito. El formato lo revisa el caso de uso.
    val isEmailFilled: Boolean get() = email.isNotBlank()
}

// Eventos de un solo uso.
sealed interface RecoverPasswordEvent {
    // El correo se envió y el usuario aceptó el diálogo: hay que salir de la pantalla.
    data object EmailSent : RecoverPasswordEvent
}

/**
"Olvidé mi contraseña": Firebase envía al correo un enlace para escribir una contraseña nueva.
Por seguridad, la pantalla dice lo mismo exista o no la cuenta.
**/
// Marca la clase como ViewModel inyectable por Hilt.
@HiltViewModel
// Declara el ViewModel y pide el caso de uso por constructor.
class RecoverPasswordViewModel @Inject constructor(
    // Caso de uso que envía el correo de restablecimiento.
    private val sendPasswordReset: SendPasswordResetUseCase,
) : ViewModel() {

    // Estado mutable interno.
    private val _uiState = MutableStateFlow(RecoverPasswordUiState())
    // Estado de solo lectura que observa la pantalla.
    val uiState: StateFlow<RecoverPasswordUiState> = _uiState.asStateFlow()

    // Canal con búfer para los eventos de un solo uso.
    private val _events = Channel<RecoverPasswordEvent>(Channel.BUFFERED)
    // Flujo de eventos que recolecta la pantalla.
    val events = _events.receiveAsFlow()

    // Actualiza el correo escrito y borra el error anterior.
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, error = null) }

    // Envía la solicitud de restablecimiento.
    fun onSendClick() {
        // Toma una foto del estado actual.
        val state = _uiState.value
        // Evita enviar con el campo vacío o si ya hay un envío en curso.
        if (!state.isEmailFilled || state.isSending) return
        // Activa el estado de envío y limpia errores.
        _uiState.update { it.copy(isSending = true, error = null) }

        // Ejecuta el envío en una corrutina del ViewModel.
        viewModelScope.launch {
            // Llama al caso de uso con el correo escrito.
            sendPasswordReset(state.email)
                // Éxito: termina el envío y muestra el diálogo.
                .onSuccess { _uiState.update { it.copy(isSending = false, showSuccessDialog = true) } }
                // Fallo: traduce el error del dominio a un error de UI.
                .onFailure { error ->
                    // Correo inválido tiene su propio mensaje; el resto es genérico.
                    val uiError = if (error === AuthException.InvalidEmail) RecoverPasswordError.INVALID_EMAIL
                    else RecoverPasswordError.GENERIC
                    // Termina el envío y muestra el error.
                    _uiState.update { it.copy(isSending = false, error = uiError) }
                }
        }
    }

    // El usuario aceptó el diálogo de éxito.
    fun onDialogAccept() {
        // Oculta el diálogo.
        _uiState.update { it.copy(showSuccessDialog = false) }
        // Emite el evento para que la pantalla navegue.
        _events.trySend(RecoverPasswordEvent.EmailSent)
    }
}
