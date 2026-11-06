package co.edu.uniquindio.petadoptionapp.features.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.core.component.form.PasswordStrength
import co.edu.uniquindio.petadoptionapp.core.component.form.passwordStrengthOf
import co.edu.uniquindio.petadoptionapp.core.util.UserNameGenerator
import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.error.UserException
import co.edu.uniquindio.petadoptionapp.domain.model.auth.Credentials
import co.edu.uniquindio.petadoptionapp.domain.model.location.Location
import co.edu.uniquindio.petadoptionapp.domain.model.users.Account
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.CheckRegistrationAvailabilityUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.RegisterUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.AuthValidator
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.UserValidator
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
ViewModel del registro de usuarios, compartido por las dos fases del formulario.
Fase 1 (datos personales): nombre, correo, teléfono, contraseña y confirmación, con validación en
vivo y al perder el foco. Antes de avanzar se consulta CheckRegistrationAvailabilityUseCase para
saber si el correo y el teléfono están libres.
Fase 2 (ubicación): punto elegido en el mapa (ciudad y dirección) y aceptación de términos; al pulsar
"Crear cuenta" se llama RegisterUseCase con un alias generado (UserNameGenerator), reintentando si
el alias ya existe.
Estado de UI: RegisterUiState (paso actual, valores, errores por campo, error general, cargas y diálogo
de éxito). Evento de un solo uso: RegisterEvent.RegisterSuccess, que RegisterScreen traduce en
onRegisterSuccess para que AppNavigation continúe tras crear la cuenta.
**/

// Campos de la fase 1. Sirve para avisar cuál ganó o perdió el foco.
enum class RegisterField { NAME, EMAIL, PHONE, PASSWORD, CONFIRM_PASSWORD }

// Errores de un campo. La pantalla decide el texto según el campo y el tipo.
enum class FieldError { EMPTY, INVALID, TAKEN, WEAK, MISMATCH }

// Errores del intento de registro que no pertenecen a un campo concreto.
// LOCATION: ubicación inválida; TAKEN: correo o teléfono ya usados (se revisa al pasar al segundo paso); GENERIC: otro fallo.
enum class RegisterError { LOCATION, TAKEN, GENERIC }

// Estado completo del registro. Un solo ViewModel para ambas fases: no se pierde nada al avanzar.
data class RegisterUiState(
    // Paso actual del formulario (1 o 2).
    val step: Int = 1,
    // Fase 1
    // Nombre completo.
    val fullName: String = "",
    // Correo electrónico.
    val email: String = "",
    // Teléfono (solo dígitos).
    val phone: String = "",
    // Contraseña.
    val password: String = "",
    // Confirmación de la contraseña.
    val confirmPassword: String = "",
    // Error del nombre.
    val nameError: FieldError? = null,
    // Error del correo.
    val emailError: FieldError? = null,
    // Error del teléfono.
    val phoneError: FieldError? = null,
    // Error de la contraseña.
    val passwordError: FieldError? = null,
    // Error de la confirmación.
    val confirmPasswordError: FieldError? = null,
    // Fase 2
    // Ciudad (sale del punto elegido).
    val city: String = "",
    // Dirección (sale del punto elegido).
    val address: String = "",
    // Punto elegido en el mapa (coordenadas); ciudad y dirección salen de aquí.
    val pickedLocation: Location? = null,
    // true si el usuario aceptó los términos.
    val termsAccepted: Boolean = false,
    // Error general del registro.
    val registerError: RegisterError? = null,
    // true mientras se crea la cuenta.
    val isLoading: Boolean = false,
    // Revisando si el correo y el teléfono están libres antes del segundo paso.
    val isCheckingAvailability: Boolean = false,
    // true para mostrar el diálogo de cuenta creada.
    val showSuccessDialog: Boolean = false,
) {
    // Se calcula con la misma regla del Use Case: "Baja" significa que sería rechazada.
    val passwordStrength: PasswordStrength?
        // Delega el cálculo en el ViewModel.
        get() = RegisterViewModel.strengthOf(password)

    // Se calcula a partir de los valores, no de los errores: un campo que el usuario
    // aún no ha tocado no muestra error, pero tampoco debe contar como válido.
    val isPersonalDataValid: Boolean
        // Todos los validadores de la fase 1 deben devolver null.
        get() = RegisterViewModel.validateName(fullName) == null &&
                RegisterViewModel.validateEmail(email) == null &&
                RegisterViewModel.validatePhone(phone) == null &&
                RegisterViewModel.validatePassword(password) == null &&
                RegisterViewModel.validateConfirmPassword(password, confirmPassword) == null

    // Hace falta un punto elegido en el mapa (con su dirección) y aceptar los términos.
    val isLocationDataFilled: Boolean
        // Coordenadas válidas, ciudad y dirección no vacías y términos aceptados.
        get() = pickedLocation?.hasCoordinates() == true && city.isNotBlank() && address.isNotBlank() && termsAccepted

    // Constantes del estado.
    companion object {
        // Cantidad total de pasos del registro.
        const val TOTAL_STEPS = 2
    }
}

// Eventos de un solo uso.
sealed interface RegisterEvent {
    // La cuenta se creó y el usuario aceptó el diálogo.
    data object RegisterSuccess : RegisterEvent
}

// Marca la clase como ViewModel inyectable por Hilt.
@HiltViewModel
// Declara el ViewModel y pide a Hilt los casos de uso por constructor.
class RegisterViewModel @Inject constructor(
    // Crea la cuenta y el perfil del usuario.
    private val registerUseCase: RegisterUseCase,
    // Revisa si el correo y el teléfono están disponibles.
    private val checkAvailability: CheckRegistrationAvailabilityUseCase,
) : ViewModel() {

    // Estado mutable interno.
    private val _uiState = MutableStateFlow(RegisterUiState())
    // Estado de solo lectura que observa la pantalla.
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // Canal con búfer para los eventos de un solo uso.
    private val _events = Channel<RegisterEvent>(Channel.BUFFERED)
    // Flujo de eventos que recolecta la pantalla.
    val events = _events.receiveAsFlow()

    // Campos que llegaron a tener el foco. Sin esto, el error saldría apenas se dibuja
    // la pantalla, porque Compose avisa "sin foco" al inicio.
    private val fieldsThatHadFocus = mutableSetOf<RegisterField>()

    // Al escribir se valida de inmediato: si borra todo, el error aparece.
    fun onFullNameChange(value: String) =
        _uiState.update { it.copy(fullName = value, nameError = validateName(value)) }

    // Guarda y valida el correo; si había aviso de "ya utilizado", lo quita.
    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, emailError = validateEmail(value), registerError = it.registerError.clearedIfTaken()) }

    // Solo dígitos y con el máximo del validador: así nunca se escribe un teléfono imposible.
    fun onPhoneChange(value: String) {
        // Filtra los dígitos y recorta a la longitud máxima.
        val digits = value.filter(Char::isDigit).take(UserValidator.MAX_PHONE_LENGTH)
        // Guarda, valida y quita el aviso de "ya utilizado".
        _uiState.update { it.copy(phone = digits, phoneError = validatePhone(digits), registerError = it.registerError.clearedIfTaken()) }
    }

    // Guarda y valida la contraseña.
    fun onPasswordChange(value: String) = _uiState.update {
        it.copy(
            // Nuevo valor.
            password = value,
            // Valida la fortaleza mínima.
            passwordError = validatePassword(value),
            // Si ya había confirmación escrita, se revalida contra la contraseña nueva.
            confirmPasswordError = if (it.confirmPassword.isEmpty()) it.confirmPasswordError
            else validateConfirmPassword(value, it.confirmPassword),
        )
    }

    // Guarda la confirmación y verifica que coincida.
    fun onConfirmPasswordChange(value: String) = _uiState.update {
        it.copy(
            // Nuevo valor.
            confirmPassword = value,
            // Compara contra la contraseña actual.
            confirmPasswordError = validateConfirmPassword(it.password, value),
        )
    }

    // Se llama cada vez que un campo gana o pierde el foco.
    fun onFocusChange(field: RegisterField, isFocused: Boolean) {
        // Si gana el foco, se registra como tocado.
        if (isFocused) {
            fieldsThatHadFocus += field
        // Si lo pierde y ya había sido tocado, se valida.
        } else if (field in fieldsThatHadFocus) {
            // Salió del campo sin escribir (o dejándolo inválido): se muestra el error.
            _uiState.update { it.withErrorFor(field) }
        }
    }

    // Ubicación elegida en el mapa (ciudad del selector, dirección buscada o del punto tocado).
    fun onLocationPicked(location: Location) = _uiState.update {
        // Guarda el punto, copia ciudad y dirección y limpia el error general.
        it.copy(pickedLocation = location, city = location.city, address = location.address, registerError = null)
    }
    // Marca o desmarca la aceptación de términos.
    fun onTermsChange(value: Boolean) = _uiState.update { it.copy(termsAccepted = value) }

    /**
    Valida toda la fase 1; si el formato es correcto, revisa que el correo y el teléfono no los use otra
    cuenta y solo entonces avanza. Si alguno está ocupado, se marca su campo y se avisa debajo.
    **/
    fun onNextStep() {
        // Calcula los errores de todos los campos de la fase 1.
        val validated = _uiState.value.withAllPersonalDataErrors()
        // Publica el estado con los errores visibles.
        _uiState.value = validated
        // Si algo es inválido o ya se está revisando, no continúa.
        if (!validated.isPersonalDataValid || validated.isCheckingAvailability) return

        // Activa la revisión de disponibilidad y limpia el error general.
        _uiState.update { it.copy(isCheckingAvailability = true, registerError = null) }
        // Consulta la disponibilidad en segundo plano.
        viewModelScope.launch {
            // Llama al caso de uso con el correo y el teléfono.
            checkAvailability(validated.email, validated.phone)
                // La consulta respondió.
                .onSuccess { availability ->
                    // Actualiza el estado según la respuesta.
                    _uiState.update {
                        // Si ambos están libres, avanza al paso 2.
                        if (availability.isAvailable) {
                            it.copy(isCheckingAvailability = false, step = 2)
                        // Si alguno está ocupado, marca el campo correspondiente y el aviso general.
                        } else {
                            it.copy(
                                // Termina la revisión.
                                isCheckingAvailability = false,
                                // Marca el correo como ocupado si aplica.
                                emailError = if (availability.emailTaken) FieldError.TAKEN else it.emailError,
                                // Marca el teléfono como ocupado si aplica.
                                phoneError = if (availability.phoneTaken) FieldError.TAKEN else it.phoneError,
                                // Aviso general bajo el formulario.
                                registerError = RegisterError.TAKEN,
                            )
                        }
                    }
                }
                // Si la consulta falla, muestra el error genérico.
                .onFailure { _uiState.update { it.copy(isCheckingAvailability = false, registerError = RegisterError.GENERIC) } }
        }
    }

    // Vuelve al paso anterior sin bajar de 1.
    fun onPreviousStep() = _uiState.update { it.copy(step = (it.step - 1).coerceAtLeast(1)) }

    // Crea la cuenta con todos los datos del formulario.
    fun onCreateAccountClick() {
        // Toma una foto del estado actual.
        val state = _uiState.value
        // Doble protección: el botón ya se deshabilita, pero conviene blindar el toque doble.
        if (!state.isLocationDataFilled || state.isLoading) return

        // Se activa ANTES de lanzar la corrutina: un segundo toque ya encuentra isLoading = true.
        _uiState.update { it.copy(isLoading = true, registerError = null) }

        // Ejecuta el registro en segundo plano.
        viewModelScope.launch {
            // Registra reintentando si el alias generado ya existe.
            registerWithUniqueUserName(state)
                // Éxito: termina la carga y muestra el diálogo.
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, showSuccessDialog = true) }
                }
                // Fallo: traduce el error al campo o aviso correspondiente.
                .onFailure { error ->
                    _uiState.update { it.withFailure(error) }
                }
        }
    }

    // El usuario aceptó el diálogo de cuenta creada.
    fun onSuccessDialogAccept() {
        // Oculta el diálogo.
        _uiState.update { it.copy(showSuccessDialog = false) }
        // Emite el evento para que la pantalla navegue.
        _events.trySend(RegisterEvent.RegisterSuccess)
    }

    // El alias es aleatorio, así que puede chocar con uno existente. El Use Case lo comprueba
    // ANTES de crear la cuenta, por lo que reintentar con otro alias no deja nada a medias.
    private suspend fun registerWithUniqueUserName(state: RegisterUiState): Result<Account> {
        // Arma las credenciales, quitando espacios sobrantes del correo.
        val credentials = Credentials(email = state.email.trim(), password = state.password)

        // El punto elegido en el mapa (isLocationDataFilled garantiza que tiene coordenadas).
        val picked = state.pickedLocation ?: Location()
        // Completa la ubicación con ciudad y dirección limpias y un país por defecto si falta.
        val location = picked.copy(
            city = state.city.trim(),
            country = picked.country.ifBlank { DEFAULT_COUNTRY },
            address = state.address.trim(),
        )

        // Resultado inicial por si no se alcanza a intentar ninguna vez.
        var result: Result<Account> = Result.failure(UserException.UserNameTaken)
        // Intenta hasta el máximo permitido.
        repeat(MAX_USERNAME_ATTEMPTS) {
            // Llama al caso de uso con un alias nuevo generado a partir del nombre.
            result = registerUseCase(
                credentials = credentials,
                userName = UserNameGenerator.generate(state.fullName),
                name = state.fullName,
                phone = state.phone,
                location = location,
            )
            // Si el error no es "alias ocupado" (éxito u otro fallo), devuelve de inmediato.
            if (result.exceptionOrNull() != UserException.UserNameTaken) return result
        }
        // Agotó los intentos: devuelve el último resultado.
        return result
    }

    // Al cambiar el correo o el teléfono, el aviso de "ya utilizado" deja de aplicar.
    private fun RegisterError?.clearedIfTaken(): RegisterError? = takeUnless { it == RegisterError.TAKEN }

    // Muestra el error de un solo campo (al perder el foco).
    private fun RegisterUiState.withErrorFor(field: RegisterField) = when (field) {
        // Valida el nombre.
        RegisterField.NAME -> copy(nameError = validateName(fullName))
        // Valida el correo.
        RegisterField.EMAIL -> copy(emailError = validateEmail(email))
        // Valida el teléfono.
        RegisterField.PHONE -> copy(phoneError = validatePhone(phone))
        // Valida la contraseña.
        RegisterField.PASSWORD -> copy(passwordError = validatePassword(password))
        // Valida la confirmación.
        RegisterField.CONFIRM_PASSWORD ->
            copy(confirmPasswordError = validateConfirmPassword(password, confirmPassword))
    }

    // Muestra los errores de todos los campos (al pulsar "Siguiente").
    private fun RegisterUiState.withAllPersonalDataErrors() = copy(
        nameError = validateName(fullName),
        emailError = validateEmail(email),
        phoneError = validatePhone(phone),
        passwordError = validatePassword(password),
        confirmPasswordError = validateConfirmPassword(password, confirmPassword),
    )

    // Traduce la excepción del dominio al campo que corresponde. Si el error es de la fase 1,
    // se regresa a esa fase para que el usuario lo vea. Lo no reconocido cae en GENERIC:
    // nunca se muestra el mensaje técnico.
    private fun RegisterUiState.withFailure(error: Throwable): RegisterUiState {
        // Siempre se quita el indicador de carga.
        val base = copy(isLoading = false)
        // Elige el error según la excepción.
        return when (error) {
            // Correo inválido.
            AuthException.InvalidEmail -> base.copy(emailError = FieldError.INVALID, step = 1)
            // Además del campo, el aviso general: al volver a la fase 1 el campo puede quedar fuera de la vista.
            AuthException.EmailAlreadyInUse -> base.copy(emailError = FieldError.TAKEN, registerError = RegisterError.TAKEN, step = 1)
            // Contraseña débil.
            AuthException.WeakPassword -> base.copy(passwordError = FieldError.WEAK, step = 1)
            // Nombre inválido.
            UserException.InvalidName -> base.copy(nameError = FieldError.INVALID, step = 1)
            // Teléfono inválido.
            UserException.InvalidPhone -> base.copy(phoneError = FieldError.INVALID, step = 1)
            // Teléfono ya usado.
            UserException.PhoneTaken -> base.copy(phoneError = FieldError.TAKEN, registerError = RegisterError.TAKEN, step = 1)
            // Ubicación inválida (se queda en la fase 2).
            UserException.InvalidLocation -> base.copy(registerError = RegisterError.LOCATION)
            // Cualquier otro error.
            else -> base.copy(registerError = RegisterError.GENERIC)
        }
    }

    // Constantes y validadores compartidos con RegisterUiState.
    companion object {
        // Máximo de intentos para encontrar un alias libre.
        private const val MAX_USERNAME_ATTEMPTS = 5

        // País por defecto si el geocodificador no lo devuelve.
        private const val DEFAULT_COUNTRY = "Colombia"

        // Las mismas reglas que aplica el Use Case: la pantalla nunca acepta algo que él va a rechazar.
        // Valida el nombre completo.
        fun validateName(name: String): FieldError? = when {
            // Vacío.
            name.isBlank() -> FieldError.EMPTY
            // No cumple el formato del dominio.
            !UserValidator.isNameValid(name) -> FieldError.INVALID
            // Válido.
            else -> null
        }

        // Valida el correo.
        fun validateEmail(email: String): FieldError? = when {
            // Vacío.
            email.isBlank() -> FieldError.EMPTY
            // Formato inválido.
            !AuthValidator.isEmailValid(email) -> FieldError.INVALID
            // Válido.
            else -> null
        }

        // Valida el teléfono.
        fun validatePhone(phone: String): FieldError? = when {
            // Vacío.
            phone.isBlank() -> FieldError.EMPTY
            // Longitud o formato inválido.
            !UserValidator.isPhoneValid(phone) -> FieldError.INVALID
            // Válido.
            else -> null
        }

        // Valida la contraseña.
        fun validatePassword(password: String): FieldError? = when {
            // Vacía.
            password.isEmpty() -> FieldError.EMPTY
            // No cumple las reglas de seguridad.
            !AuthValidator.isPasswordValid(password) -> FieldError.WEAK
            // Válida.
            else -> null
        }

        // Valida la confirmación de la contraseña.
        fun validateConfirmPassword(password: String, confirmPassword: String): FieldError? = when {
            // Vacía.
            confirmPassword.isEmpty() -> FieldError.EMPTY
            // No coincide.
            confirmPassword != password -> FieldError.MISMATCH
            // Coincide.
            else -> null
        }

        // La regla vive junto a la barra (la comparte Cambiar contraseña).
        fun strengthOf(password: String): PasswordStrength? = passwordStrengthOf(password)
    }
}
