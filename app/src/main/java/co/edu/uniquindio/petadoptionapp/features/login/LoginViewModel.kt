package co.edu.uniquindio.petadoptionapp.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Errores posibles de cada campo. La pantalla decide el texto según el tipo. */
enum class EmailError { EMPTY, INVALID_FORMAT }
enum class PasswordError { EMPTY }

/** Estado completo de la pantalla. Inmutable: cada cambio crea una copia. */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: EmailError? = null,
    val passwordError: PasswordError? = null,
    val isLoading: Boolean = false,
) {
    // Se calcula a partir de los valores, no de los errores: un campo que el usuario
    // aún no ha tocado no muestra error, pero tampoco debe contar como válido.
    val isFormValid: Boolean
        get() = LoginViewModel.validateEmail(email) == null &&
                LoginViewModel.validatePassword(password) == null
}

/** Eventos de un solo uso (no deben repetirse al rotar la pantalla). */
sealed interface LoginEvent {
    data object LoginSuccess : LoginEvent
}

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // Recuerdan si el campo llegó a tener el foco. Sin esto, el error saldría
    // apenas se dibuja la pantalla, porque Compose avisa "sin foco" al inicio.
    private var emailHadFocus = false
    private var passwordHadFocus = false

    fun onEmailChange(value: String) {
        // Al escribir se valida de inmediato: si borra todo, el error aparece.
        _uiState.update { it.copy(email = value, emailError = validateEmail(value)) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = validatePassword(value)) }
    }

    /** Se llama cada vez que el campo gana o pierde el foco. */
    fun onEmailFocusChange(isFocused: Boolean) {
        if (isFocused) {
            emailHadFocus = true
        } else if (emailHadFocus) {
            // Salió del campo sin escribir (o dejándolo inválido): se muestra el error.
            _uiState.update { it.copy(emailError = validateEmail(it.email)) }
        }
    }

    fun onPasswordFocusChange(isFocused: Boolean) {
        if (isFocused) {
            passwordHadFocus = true
        } else if (passwordHadFocus) {
            _uiState.update { it.copy(passwordError = validatePassword(it.password)) }
        }
    }

    fun onLoginClick() {
        val state = _uiState.value
        // Doble protección: el botón ya está deshabilitado, pero el teclado
        // ("Listo") también puede disparar esta acción.
        if (!state.isFormValid || state.isLoading) return

        // TEMPORAL: aquí irá el caso de uso (isLoading = true → llamada → resultado).
        // Ejemplo futuro: val user = loginUserUseCase(state.email.trim(), state.password)
        viewModelScope.launch { _events.send(LoginEvent.LoginSuccess) }
    }

    companion object {
        // Formato básico: texto@dominio.extensión.
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

        fun validateEmail(email: String): EmailError? {
            // trim: el autocorrector suele dejar un espacio al final.
            val value = email.trim()
            return when {
                value.isEmpty() -> EmailError.EMPTY
                !EMAIL_REGEX.matches(value) -> EmailError.INVALID_FORMAT
                else -> null
            }
        }

        // En login solo se exige que no esté vacía. Las reglas de longitud o
        // complejidad pertenecen al registro: aplicarlas aquí bloquearía a
        // usuarios que ya tienen una contraseña válida.
        fun validatePassword(password: String): PasswordError? =
            if (password.isEmpty()) PasswordError.EMPTY else null
    }
}