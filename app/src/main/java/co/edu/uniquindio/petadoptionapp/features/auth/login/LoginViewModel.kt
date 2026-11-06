package co.edu.uniquindio.petadoptionapp.features.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.error.UserException
import co.edu.uniquindio.petadoptionapp.domain.model.auth.Credentials
import co.edu.uniquindio.petadoptionapp.domain.model.auth.TwoFactorChallenge
import co.edu.uniquindio.petadoptionapp.domain.repository.LoginResult
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.LoginUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.LogoutUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.ResendTwoFactorCodeUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.VerifyTwoFactorCodeUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.AuthValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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
    CODE_NOT_SENT,
    GENERIC
}

/** Errores del diálogo de verificación en dos pasos (A2F). */
enum class TwoFactorError {
    WRONG_CODE,
    EXPIRED,
    TOO_MANY_ATTEMPTS,
    NOT_SENT,
    GENERIC
}

/**
 Estado del diálogo A2F: se muestra cuando el email y la contraseña fueron correctos y falta el código.
 [resendSeconds] cuenta hacia atrás; en 0 se puede pedir otro código.
 */
data class TwoFactorUiState(
    val accountId: String,
    val code: String = "",
    val isVerifying: Boolean = false,
    val isResending: Boolean = false,
    val error: TwoFactorError? = null,
    val resendSeconds: Int = 0,
) {
    val isCodeComplete: Boolean get() = code.length == TwoFactorChallenge.CODE_LENGTH
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
    // Diálogo A2F abierto; null si no hace falta.
    val twoFactor: TwoFactorUiState? = null,
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
    // Sesión abierta (directo, o después de escribir el código A2F en el diálogo).
    data object LoginSuccess : LoginEvent
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val verifyTwoFactorCode: VerifyTwoFactorCodeUseCase,
    private val resendTwoFactorCode: ResendTwoFactorCodeUseCase,
    private val logout: LogoutUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // Registra el historial de interacción del foco para prevenir validaciones prematuras durante la composición inicial antes de que el usuario interactúe con el formulario.
    private var emailHadFocus = false
    private var passwordHadFocus = false

    // Cuenta regresiva para "Reenviar código".
    private var resendTimer: Job? = null

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
                        // El código ya salió al correo: se abre el diálogo para escribirlo.
                        is LoginResult.TwoFactorRequired -> {
                            _uiState.update { it.copy(twoFactor = TwoFactorUiState(accountId = result.accountId)) }
                            startResendTimer()
                        }
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, loginError = error.toLoginError()) }
                }
        }
    }

    // A2F

    fun onTwoFactorCodeChange(value: String) {
        // Solo dígitos y como máximo la longitud del código (también al pegarlo completo).
        val digits = value.filter { it.isDigit() }.take(TwoFactorChallenge.CODE_LENGTH)
        _uiState.update { state ->
            state.copy(twoFactor = state.twoFactor?.copy(code = digits, error = null))
        }
    }

    fun onVerifyTwoFactorClick() {
        val twoFactor = _uiState.value.twoFactor ?: return
        if (!twoFactor.isCodeComplete || twoFactor.isVerifying) return
        updateTwoFactor { it.copy(isVerifying = true, error = null) }

        viewModelScope.launch {
            verifyTwoFactorCode(twoFactor.accountId, twoFactor.code)
                .onSuccess {
                    resendTimer?.cancel()
                    _uiState.update { it.copy(twoFactor = null) }
                    _events.send(LoginEvent.LoginSuccess)
                }
                .onFailure { error ->
                    when (error) {
                        // El perfil no sirve (cuenta eliminada, moderador inactivo): se cierra el diálogo y se avisa en el login.
                        UserException.AccountDeleted, AuthException.InactiveModerator, UserException.ProfileNotFound -> {
                            resendTimer?.cancel()
                            _uiState.update { it.copy(twoFactor = null, loginError = error.toLoginError()) }
                        }
                        else -> updateTwoFactor {
                            // Un código equivocado se borra para escribirlo de nuevo.
                            it.copy(isVerifying = false, code = "", error = error.toTwoFactorError())
                        }
                    }
                }
        }
    }

    fun onResendTwoFactorClick() {
        val twoFactor = _uiState.value.twoFactor ?: return
        if (twoFactor.resendSeconds > 0 || twoFactor.isResending) return
        updateTwoFactor { it.copy(isResending = true, error = null) }

        viewModelScope.launch {
            resendTwoFactorCode(twoFactor.accountId)
                .onSuccess {
                    updateTwoFactor { it.copy(isResending = false, code = "") }
                    startResendTimer()
                }
                .onFailure { error ->
                    updateTwoFactor { it.copy(isResending = false, error = error.toTwoFactorError()) }
                }
        }
    }

    // Cerrar el diálogo cancela el inicio de sesión (Firebase tenía la sesión abierta a la espera del código).
    fun onDismissTwoFactor() {
        resendTimer?.cancel()
        _uiState.update { it.copy(twoFactor = null, password = "") }
        viewModelScope.launch { logout() }
    }

    private fun updateTwoFactor(transform: (TwoFactorUiState) -> TwoFactorUiState) {
        _uiState.update { state -> state.copy(twoFactor = state.twoFactor?.let(transform)) }
    }

    private fun startResendTimer() {
        resendTimer?.cancel()
        resendTimer = viewModelScope.launch {
            for (seconds in RESEND_WAIT_SECONDS downTo 0) {
                updateTwoFactor { it.copy(resendSeconds = seconds) }
                if (seconds > 0) delay(1_000)
            }
        }
    }

    private fun Throwable.toTwoFactorError(): TwoFactorError = when (this) {
        AuthException.WrongCode, AuthException.InvalidCode -> TwoFactorError.WRONG_CODE
        AuthException.CodeExpired -> TwoFactorError.EXPIRED
        AuthException.TooManyAttempts -> TwoFactorError.TOO_MANY_ATTEMPTS
        AuthException.CodeNotSent -> TwoFactorError.NOT_SENT
        else -> TwoFactorError.GENERIC
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
        AuthException.CodeNotSent -> LoginError.CODE_NOT_SENT
        else -> LoginError.GENERIC
    }

    companion object {
        // Espera mínima entre reenvíos del código A2F.
        const val RESEND_WAIT_SECONDS = 30

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