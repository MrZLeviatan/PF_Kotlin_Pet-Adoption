package co.edu.uniquindio.petadoptionapp.features.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.error.UserException
import co.edu.uniquindio.petadoptionapp.domain.model.auth.Credentials
import co.edu.uniquindio.petadoptionapp.domain.repository.LoginResult
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.LoginUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.AuthValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 Define los errores de validación a nivel de campo. La capa de interfaz de usuario
 asocia cada tipo de error con su respectivo mensaje localizado.
 */
enum class EmailError {
    EMPTY,
    INVALID_FORMAT
}

enum class PasswordError {
    EMPTY
}

/**
 Define los errores derivados de los intentos de autenticación (no ligados a un campo específico).
 La capa de UI mapea cada tipo de error a su correspondiente notificación.
 */
enum class LoginError {
    INVALID_EMAIL,
    INVALID_CREDENTIALS,
    INACTIVE_MODERATOR,
    ACCOUNT_DELETED,
    GENERIC
}

/**
 Representa el estado inmutable completo de la interfaz de usuario para la pantalla de inicio de sesión.
 Las modificaciones de estado se realizan mediante el patrón copy para preservar la inmutabilidad.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: EmailError? = null,
    val passwordError: PasswordError? = null,
    val loginError: LoginError? = null,
    val isLoading: Boolean = false,
) {
    /**
     Evalúa la validez del formulario directamente a partir de los valores de entrada, no de los errores actuales.
     Esto evita resaltar errores de forma prematura en campos no interactuados sin comprometer la validación de integridad.
     */
    val isFormValid: Boolean
        get() = LoginViewModel.validateEmail(email) == null &&
                LoginViewModel.validatePassword(password) == null
}

/**
 Representa eventos de un solo uso (efectos secundarios de UI) que no deben reemitirse
 ante cambios de configuración o rotaciones de pantalla.
 */
sealed interface LoginEvent {
    data object LoginSuccess : LoginEvent

    /**
     Notifica que se requiere autenticación de dos factores, proporcionando el identificador
     de cuenta necesario para completar el flujo de verificación.
     */
    data class TwoFactorRequired(val accountId: String) : LoginEvent
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // Registra el historial de interacción del foco para prevenir validaciones prematuras durante la composición inicial antes de que el usuario interactúe con el formulario.
    private var emailHadFocus = false
    private var passwordHadFocus = false

    fun onEmailChange(value: String) {
        // Ejecuta la validación en tiempo real y limpia los errores globales previos de autenticación.
        _uiState.update {
            it.copy(email = value, emailError = validateEmail(value), loginError = null)
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(password = value, passwordError = validatePassword(value), loginError = null)
        }
    }

    // Gestiona la transición del estado de foco en el campo del correo electrónico.
    fun onEmailFocusChange(isFocused: Boolean) {
        if (isFocused) {
            emailHadFocus = true
        } else if (emailHadFocus) {
            _uiState.update { it.copy(emailError = validateEmail(it.email)) }
        }
    }

    // Gestiona la transición del estado de foco en el campo de la contraseña.
    fun onPasswordFocusChange(isFocused: Boolean) {
        if (isFocused) {
            passwordHadFocus = true
        } else if (passwordHadFocus) {
            _uiState.update { it.copy(passwordError = validatePassword(it.password)) }
        }
    }

    fun onLoginClick() {
        val state = _uiState.value
        // Cláusula de guarda: previene la ejecución si el formulario es inválido o si ya hay una petición en curso,
        // mitigando disparos concurrentes desde la acción del teclado virtual ("Hecho/Listo").
        if (!state.isFormValid || state.isLoading) return

        // Modifica sincrónicamente el estado de carga antes de despachar la corrutina para evitar
        // condiciones de carrera derivadas de múltiples toques rápidos sobre la UI.
        _uiState.update { it.copy(isLoading = true, loginError = null) }

        viewModelScope.launch {
            // Remueve espacios en blanco al final, introducidos comúnmente por el autocorrector del teclado móvil.
            val credentials = Credentials(email = state.email.trim(), password = state.password)

            loginUseCase(credentials)
                .onSuccess { result ->
                    _uiState.update { it.copy(isLoading = false) }
                    when (result) {
                        is LoginResult.Success -> _events.send(LoginEvent.LoginSuccess)
                        is LoginResult.TwoFactorRequired ->
                            _events.send(LoginEvent.TwoFactorRequired(result.accountId))
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, loginError = error.toLoginError()) }
                }
        }
    }

    /**
     Mapea excepciones de la capa de dominio a tipos de error de la capa de presentación.
     Las excepciones no reconocidas se asignan por defecto a [LoginError.GENERIC] para no exponer detalles técnicos internos.
     */
    private fun Throwable.toLoginError(): LoginError = when (this) {
        AuthException.InvalidEmail -> LoginError.INVALID_EMAIL
        // Consolida los errores de "correo inexistente" y "contraseña errónea" en un único mensaje genérico
        // para mitigar vulnerabilidades de enumeración de usuarios.
        AuthException.WrongPassword -> LoginError.INVALID_CREDENTIALS
        AuthException.InactiveModerator -> LoginError.INACTIVE_MODERATOR
        UserException.AccountDeleted -> LoginError.ACCOUNT_DELETED
        else -> LoginError.GENERIC
    }

    companion object {
        /**
         Valida el formato del correo electrónico respetando las reglas de la capa de dominio,
         garantizando consistencia entre la capa de presentación y las reglas de negocio.
         */
        fun validateEmail(email: String): EmailError? {
            val value = email.trim()
            return when {
                value.isEmpty() -> EmailError.EMPTY
                !AuthValidator.isEmailValid(value) -> EmailError.INVALID_FORMAT
                else -> null
            }
        }

        /**
         Valida únicamente la presencia de la contraseña. Las restricciones complejas de estructura o longitud
         se delegan al flujo de registro para evitar bloquear usuarios con credenciales legítimas preexistentes.
         */
        fun validatePassword(password: String): PasswordError? =
            if (password.isEmpty()) PasswordError.EMPTY else null
    }
}