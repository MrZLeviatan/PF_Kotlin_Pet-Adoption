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
ViewModel de la pantalla de inicio de sesión (incluye el diálogo de verificación en dos pasos, A2F).
Estado de UI (LoginUiState): correo, contraseña, errores por campo (EmailError, PasswordError), error
general de autenticación (LoginError), bandera de carga y, si aplica, el estado del diálogo A2F
(TwoFactorUiState: código, verificación, reenvío, error y cuenta regresiva).
Eventos de un solo uso (LoginEvent.LoginSuccess): se emiten cuando la sesión queda abierta, ya sea
directamente o después de validar el código A2F; LoginScreen los traduce en onLoginSuccess y
AppNavigation lleva al dashboard.
Casos de uso: LoginUseCase (credenciales y, si hace falta, envío del código), VerifyTwoFactorCodeUseCase,
ResendTwoFactorCodeUseCase y LogoutUseCase (al cancelar el diálogo A2F).
Los errores del dominio (AuthException, UserException) se traducen a enums de UI para que la pantalla
muestre el mensaje localizado correspondiente sin exponer detalles técnicos.
**/

/**
Define los errores de validación a nivel de campo. La capa de interfaz de usuario
asocia cada tipo de error con su respectivo mensaje localizado.
**/
// Errores posibles del campo de correo.
enum class EmailError {
    // El campo está vacío.
    EMPTY,
    // El texto no tiene formato de correo.
    INVALID_FORMAT
}

// Errores posibles del campo de contraseña.
enum class PasswordError {
    // El campo está vacío.
    EMPTY
}

/**
Define los errores derivados de los intentos de autenticación (no ligados a un campo específico).
La capa de UI mapea cada tipo de error a su correspondiente notificación.
**/
enum class LoginError {
    // Correo rechazado por el dominio.
    INVALID_EMAIL,
    // Correo o contraseña incorrectos (mensaje unificado).
    INVALID_CREDENTIALS,
    // Moderador desactivado: no puede entrar.
    INACTIVE_MODERATOR,
    // La cuenta fue eliminada.
    ACCOUNT_DELETED,
    // No se pudo enviar el código A2F.
    CODE_NOT_SENT,
    // Cualquier otro error.
    GENERIC
}

// Errores del diálogo de verificación en dos pasos (A2F).
enum class TwoFactorError {
    // El código escrito no coincide.
    WRONG_CODE,
    // El código ya venció.
    EXPIRED,
    // Se superó el número de intentos permitidos.
    TOO_MANY_ATTEMPTS,
    // No se pudo enviar el código.
    NOT_SENT,
    // Cualquier otro error.
    GENERIC
}

/**
Estado del diálogo A2F: se muestra cuando el email y la contraseña fueron correctos y falta el código.
[resendSeconds] cuenta hacia atrás; en 0 se puede pedir otro código.
**/
data class TwoFactorUiState(
    // Id de la cuenta que está verificando el código.
    val accountId: String,
    // Código escrito hasta el momento.
    val code: String = "",
    // true mientras se verifica el código.
    val isVerifying: Boolean = false,
    // true mientras se pide un nuevo código.
    val isResending: Boolean = false,
    // Error a mostrar en el diálogo; null si no hay.
    val error: TwoFactorError? = null,
    // Segundos que faltan para poder reenviar el código.
    val resendSeconds: Int = 0,
) {
    // true cuando el código tiene la longitud exacta requerida.
    val isCodeComplete: Boolean get() = code.length == TwoFactorChallenge.CODE_LENGTH
}

/**
Representa el estado inmutable completo de la interfaz de usuario para la pantalla de inicio de sesión.
Las modificaciones de estado se realizan mediante el patrón copy para preservar la inmutabilidad.
**/
data class LoginUiState(
    // Correo escrito.
    val email: String = "",
    // Contraseña escrita.
    val password: String = "",
    // Error del campo de correo; null si es válido o aún no se valida.
    val emailError: EmailError? = null,
    // Error del campo de contraseña.
    val passwordError: PasswordError? = null,
    // Error general del intento de inicio de sesión.
    val loginError: LoginError? = null,
    // true mientras se procesa el inicio de sesión.
    val isLoading: Boolean = false,
    // Diálogo A2F abierto; null si no hace falta.
    val twoFactor: TwoFactorUiState? = null,
) {
    /**
    Evalúa la validez del formulario directamente a partir de los valores de entrada, no de los errores actuales.
    Esto evita resaltar errores de forma prematura en campos no interactuados sin comprometer la validación de integridad.
    **/
    val isFormValid: Boolean
        // Ambos validadores deben devolver null (sin error).
        get() = LoginViewModel.validateEmail(email) == null &&
                LoginViewModel.validatePassword(password) == null
}

/**
Representa eventos de un solo uso (efectos secundarios de UI) que no deben reemitirse
ante cambios de configuración o rotaciones de pantalla.
**/
sealed interface LoginEvent {
    // Sesión abierta (directo, o después de escribir el código A2F en el diálogo).
    data object LoginSuccess : LoginEvent
}

// Marca la clase como ViewModel inyectable por Hilt.
@HiltViewModel
// Declara el ViewModel y pide a Hilt los casos de uso por constructor.
class LoginViewModel @Inject constructor(
    // Inicia sesión con correo y contraseña.
    private val loginUseCase: LoginUseCase,
    // Verifica el código A2F.
    private val verifyTwoFactorCode: VerifyTwoFactorCodeUseCase,
    // Reenvía el código A2F.
    private val resendTwoFactorCode: ResendTwoFactorCodeUseCase,
    // Cierra la sesión pendiente si se cancela el A2F.
    private val logout: LogoutUseCase,
) : ViewModel() {

    // Estado mutable interno.
    private val _uiState = MutableStateFlow(LoginUiState())
    // Estado de solo lectura que observa la pantalla.
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Canal con búfer para los eventos de un solo uso.
    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    // Flujo de eventos que recolecta la pantalla.
    val events = _events.receiveAsFlow()

    // Registra el historial de interacción del foco para prevenir validaciones prematuras durante la composición inicial antes de que el usuario interactúe con el formulario.
    // true cuando el campo de correo ya recibió el foco al menos una vez.
    private var emailHadFocus = false
    // true cuando el campo de contraseña ya recibió el foco al menos una vez.
    private var passwordHadFocus = false

    // Cuenta regresiva para "Reenviar código".
    private var resendTimer: Job? = null

    // Se llama cada vez que cambia el texto del correo.
    fun onEmailChange(value: String) {
        // Ejecuta la validación en tiempo real y limpia los errores globales previos de autenticación.
        _uiState.update {
            it.copy(email = value, emailError = validateEmail(value), loginError = null)
        }
    }

    // Se llama cada vez que cambia el texto de la contraseña.
    fun onPasswordChange(value: String) {
        // Guarda el valor, lo valida y limpia el error general.
        _uiState.update {
            it.copy(password = value, passwordError = validatePassword(value), loginError = null)
        }
    }

    // Gestiona la transición del estado de foco en el campo del correo electrónico.
    fun onEmailFocusChange(isFocused: Boolean) {
        // Si el campo gana el foco, recuerda que ya fue tocado.
        if (isFocused) {
            emailHadFocus = true
        // Si lo pierde después de haberlo tenido, valida el correo.
        } else if (emailHadFocus) {
            _uiState.update { it.copy(emailError = validateEmail(it.email)) }
        }
    }

    // Gestiona la transición del estado de foco en el campo de la contraseña.
    fun onPasswordFocusChange(isFocused: Boolean) {
        // Si el campo gana el foco, recuerda que ya fue tocado.
        if (isFocused) {
            passwordHadFocus = true
        // Si lo pierde después de haberlo tenido, valida la contraseña.
        } else if (passwordHadFocus) {
            _uiState.update { it.copy(passwordError = validatePassword(it.password)) }
        }
    }

    // Inicia el proceso de inicio de sesión al pulsar el botón (o "Listo" en el teclado).
    fun onLoginClick() {
        // Toma una foto del estado actual.
        val state = _uiState.value
        // Cláusula de guarda: previene la ejecución si el formulario es inválido o si ya hay una petición en curso,
        // mitigando disparos concurrentes desde la acción del teclado virtual ("Hecho/Listo").
        if (!state.isFormValid || state.isLoading) return

        // Modifica sincrónicamente el estado de carga antes de despachar la corrutina para evitar
        // condiciones de carrera derivadas de múltiples toques rápidos sobre la UI.
        _uiState.update { it.copy(isLoading = true, loginError = null) }

        // Ejecuta el inicio de sesión en una corrutina del ViewModel.
        viewModelScope.launch {
            // Remueve espacios en blanco al final, introducidos comúnmente por el autocorrector del teclado móvil.
            val credentials = Credentials(email = state.email.trim(), password = state.password)

            // Llama al caso de uso de login con las credenciales.
            loginUseCase(credentials)
                // Éxito: el resultado indica si la sesión quedó abierta o falta el A2F.
                .onSuccess { result ->
                    // Quita el indicador de carga.
                    _uiState.update { it.copy(isLoading = false) }
                    // Decide el siguiente paso según el resultado.
                    when (result) {
                        // Sesión abierta: emite el evento para navegar.
                        is LoginResult.Success -> _events.send(LoginEvent.LoginSuccess)
                        // El código ya salió al correo: se abre el diálogo para escribirlo.
                        is LoginResult.TwoFactorRequired -> {
                            // Crea el estado del diálogo con el id de la cuenta.
                            _uiState.update { it.copy(twoFactor = TwoFactorUiState(accountId = result.accountId)) }
                            // Arranca la cuenta regresiva para reenviar.
                            startResendTimer()
                        }
                    }
                }
                // Fallo: quita la carga y traduce el error del dominio a un error de UI.
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, loginError = error.toLoginError()) }
                }
        }
    }

    // A2F

    // Actualiza el código escrito en el diálogo A2F.
    fun onTwoFactorCodeChange(value: String) {
        // Solo dígitos y como máximo la longitud del código (también al pegarlo completo).
        val digits = value.filter { it.isDigit() }.take(TwoFactorChallenge.CODE_LENGTH)
        // Guarda el código limpio y borra el error del diálogo (si el diálogo existe).
        _uiState.update { state ->
            state.copy(twoFactor = state.twoFactor?.copy(code = digits, error = null))
        }
    }

    // Verifica el código A2F escrito.
    fun onVerifyTwoFactorClick() {
        // Obtiene el estado del diálogo; si no está abierto, no hace nada.
        val twoFactor = _uiState.value.twoFactor ?: return
        // Evita verificar si el código está incompleto o ya se está verificando.
        if (!twoFactor.isCodeComplete || twoFactor.isVerifying) return
        // Activa el estado de verificación y limpia el error.
        updateTwoFactor { it.copy(isVerifying = true, error = null) }

        // Ejecuta la verificación en segundo plano.
        viewModelScope.launch {
            // Llama al caso de uso con la cuenta y el código.
            verifyTwoFactorCode(twoFactor.accountId, twoFactor.code)
                // Código correcto: termina el flujo A2F.
                .onSuccess {
                    // Detiene la cuenta regresiva.
                    resendTimer?.cancel()
                    // Cierra el diálogo.
                    _uiState.update { it.copy(twoFactor = null) }
                    // Emite el evento de sesión iniciada.
                    _events.send(LoginEvent.LoginSuccess)
                }
                // Fallo: decide si cerrar el diálogo o mostrar el error dentro de él.
                .onFailure { error ->
                    // Clasifica el error recibido.
                    when (error) {
                        // El perfil no sirve (cuenta eliminada, moderador inactivo): se cierra el diálogo y se avisa en el login.
                        UserException.AccountDeleted, AuthException.InactiveModerator, UserException.ProfileNotFound -> {
                            // Detiene la cuenta regresiva.
                            resendTimer?.cancel()
                            // Cierra el diálogo y muestra el error en la pantalla de login.
                            _uiState.update { it.copy(twoFactor = null, loginError = error.toLoginError()) }
                        }
                        // Cualquier otro error se muestra dentro del diálogo.
                        else -> updateTwoFactor {
                            // Un código equivocado se borra para escribirlo de nuevo.
                            it.copy(isVerifying = false, code = "", error = error.toTwoFactorError())
                        }
                    }
                }
        }
    }

    // Pide un nuevo código A2F.
    fun onResendTwoFactorClick() {
        // Obtiene el estado del diálogo; si no está abierto, no hace nada.
        val twoFactor = _uiState.value.twoFactor ?: return
        // Solo permite reenviar cuando terminó la cuenta regresiva y no hay otro reenvío en curso.
        if (twoFactor.resendSeconds > 0 || twoFactor.isResending) return
        // Activa el estado de reenvío y limpia el error.
        updateTwoFactor { it.copy(isResending = true, error = null) }

        // Ejecuta el reenvío en segundo plano.
        viewModelScope.launch {
            // Llama al caso de uso con el id de la cuenta.
            resendTwoFactorCode(twoFactor.accountId)
                // Éxito: limpia el código anterior y reinicia la cuenta regresiva.
                .onSuccess {
                    updateTwoFactor { it.copy(isResending = false, code = "") }
                    startResendTimer()
                }
                // Fallo: muestra el error dentro del diálogo.
                .onFailure { error ->
                    updateTwoFactor { it.copy(isResending = false, error = error.toTwoFactorError()) }
                }
        }
    }

    // Cerrar el diálogo cancela el inicio de sesión (Firebase tenía la sesión abierta a la espera del código).
    fun onDismissTwoFactor() {
        // Detiene la cuenta regresiva.
        resendTimer?.cancel()
        // Cierra el diálogo y borra la contraseña escrita.
        _uiState.update { it.copy(twoFactor = null, password = "") }
        // Cierra la sesión pendiente en segundo plano.
        viewModelScope.launch { logout() }
    }

    // Aplica una transformación al estado del diálogo A2F solo si está abierto.
    private fun updateTwoFactor(transform: (TwoFactorUiState) -> TwoFactorUiState) {
        // Si twoFactor es null, se mantiene null; si no, se transforma.
        _uiState.update { state -> state.copy(twoFactor = state.twoFactor?.let(transform)) }
    }

    // Inicia (o reinicia) la cuenta regresiva para el botón "Reenviar código".
    private fun startResendTimer() {
        // Cancela cualquier cuenta regresiva anterior.
        resendTimer?.cancel()
        // Lanza una nueva corrutina y guarda su Job para poder cancelarla.
        resendTimer = viewModelScope.launch {
            // Recorre los segundos desde el máximo hasta 0.
            for (seconds in RESEND_WAIT_SECONDS downTo 0) {
                // Publica los segundos restantes en el estado del diálogo.
                updateTwoFactor { it.copy(resendSeconds = seconds) }
                // Espera un segundo entre cada paso (excepto al llegar a 0).
                if (seconds > 0) delay(1_000)
            }
        }
    }

    // Traduce las excepciones del dominio a errores del diálogo A2F.
    private fun Throwable.toTwoFactorError(): TwoFactorError = when (this) {
        // Código incorrecto o con formato inválido.
        AuthException.WrongCode, AuthException.InvalidCode -> TwoFactorError.WRONG_CODE
        // Código vencido.
        AuthException.CodeExpired -> TwoFactorError.EXPIRED
        // Demasiados intentos.
        AuthException.TooManyAttempts -> TwoFactorError.TOO_MANY_ATTEMPTS
        // No se pudo enviar el código.
        AuthException.CodeNotSent -> TwoFactorError.NOT_SENT
        // Cualquier otro error.
        else -> TwoFactorError.GENERIC
    }

    /**
    Mapea excepciones de la capa de dominio a tipos de error de la capa de presentación.
    Las excepciones no reconocidas se asignan por defecto a [LoginError.GENERIC] para no exponer detalles técnicos internos.
    **/
    private fun Throwable.toLoginError(): LoginError = when (this) {
        // Correo inválido.
        AuthException.InvalidEmail -> LoginError.INVALID_EMAIL
        // Consolida los errores de "correo inexistente" y "contraseña errónea" en un único mensaje genérico
        // para mitigar vulnerabilidades de enumeración de usuarios.
        AuthException.WrongPassword -> LoginError.INVALID_CREDENTIALS
        // Moderador inactivo.
        AuthException.InactiveModerator -> LoginError.INACTIVE_MODERATOR
        // Cuenta eliminada.
        UserException.AccountDeleted -> LoginError.ACCOUNT_DELETED
        // No se pudo enviar el código A2F.
        AuthException.CodeNotSent -> LoginError.CODE_NOT_SENT
        // Cualquier otro error.
        else -> LoginError.GENERIC
    }

    // Miembros estáticos compartidos con LoginUiState.
    companion object {
        // Espera mínima entre reenvíos del código A2F.
        const val RESEND_WAIT_SECONDS = 30

        /**
        Valida el formato del correo electrónico respetando las reglas de la capa de dominio,
        garantizando consistencia entre la capa de presentación y las reglas de negocio.
        **/
        fun validateEmail(email: String): EmailError? {
            // Ignora espacios al inicio y al final.
            val value = email.trim()
            // Devuelve el primer error encontrado o null si es válido.
            return when {
                // Campo vacío.
                value.isEmpty() -> EmailError.EMPTY
                // Formato inválido según el validador del dominio.
                !AuthValidator.isEmailValid(value) -> EmailError.INVALID_FORMAT
                // Sin errores.
                else -> null
            }
        }

        /**
        Valida únicamente la presencia de la contraseña. Las restricciones complejas de estructura o longitud
        se delegan al flujo de registro para evitar bloquear usuarios con credenciales legítimas preexistentes.
        **/
        fun validatePassword(password: String): PasswordError? =
            // Solo verifica que no esté vacía.
            if (password.isEmpty()) PasswordError.EMPTY else null
    }
}
