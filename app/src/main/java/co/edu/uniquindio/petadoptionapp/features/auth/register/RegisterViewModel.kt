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

/** Campos de la fase 1. Sirve para avisar cuál ganó o perdió el foco. */
enum class RegisterField { NAME, EMAIL, PHONE, PASSWORD, CONFIRM_PASSWORD }

/** Errores de un campo. La pantalla decide el texto según el campo y el tipo. */
enum class FieldError { EMPTY, INVALID, TAKEN, WEAK, MISMATCH }

/** Errores del intento de registro que no pertenecen a un campo concreto. */
enum class RegisterError { LOCATION, GENERIC }

/** Estado completo del registro. Un solo ViewModel para ambas fases: no se pierde nada al avanzar. */
data class RegisterUiState(
    val step: Int = 1,
    // Fase 1
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: FieldError? = null,
    val emailError: FieldError? = null,
    val phoneError: FieldError? = null,
    val passwordError: FieldError? = null,
    val confirmPasswordError: FieldError? = null,
    // Fase 2
    val city: String = "",
    val address: String = "",
    val termsAccepted: Boolean = false,
    val registerError: RegisterError? = null,
    val isLoading: Boolean = false,
    val showSuccessDialog: Boolean = false,
) {
    // Se calcula con la misma regla del Use Case: "Baja" significa que sería rechazada.
    val passwordStrength: PasswordStrength?
        get() = RegisterViewModel.strengthOf(password)

    // Se calcula a partir de los valores, no de los errores: un campo que el usuario
    // aún no ha tocado no muestra error, pero tampoco debe contar como válido.
    val isPersonalDataValid: Boolean
        get() = RegisterViewModel.validateName(fullName) == null &&
                RegisterViewModel.validateEmail(email) == null &&
                RegisterViewModel.validatePhone(phone) == null &&
                RegisterViewModel.validatePassword(password) == null &&
                RegisterViewModel.validateConfirmPassword(password, confirmPassword) == null

    val isLocationDataFilled: Boolean
        get() = city.isNotBlank() && address.isNotBlank() && termsAccepted

    companion object {
        const val TOTAL_STEPS = 2
    }
}

/** Eventos de un solo uso. */
sealed interface RegisterEvent {
    data object RegisterSuccess : RegisterEvent
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _events = Channel<RegisterEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // Campos que llegaron a tener el foco. Sin esto, el error saldría apenas se dibuja
    // la pantalla, porque Compose avisa "sin foco" al inicio.
    private val fieldsThatHadFocus = mutableSetOf<RegisterField>()

    // Al escribir se valida de inmediato: si borra todo, el error aparece.
    fun onFullNameChange(value: String) =
        _uiState.update { it.copy(fullName = value, nameError = validateName(value)) }

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, emailError = validateEmail(value)) }

    // Solo dígitos y con el máximo del validador: así nunca se escribe un teléfono imposible.
    fun onPhoneChange(value: String) {
        val digits = value.filter(Char::isDigit).take(UserValidator.MAX_PHONE_LENGTH)
        _uiState.update { it.copy(phone = digits, phoneError = validatePhone(digits)) }
    }

    fun onPasswordChange(value: String) = _uiState.update {
        it.copy(
            password = value,
            passwordError = validatePassword(value),
            // Si ya había confirmación escrita, se revalida contra la contraseña nueva.
            confirmPasswordError = if (it.confirmPassword.isEmpty()) it.confirmPasswordError
            else validateConfirmPassword(value, it.confirmPassword),
        )
    }

    fun onConfirmPasswordChange(value: String) = _uiState.update {
        it.copy(
            confirmPassword = value,
            confirmPasswordError = validateConfirmPassword(it.password, value),
        )
    }

    /** Se llama cada vez que un campo gana o pierde el foco. */
    fun onFocusChange(field: RegisterField, isFocused: Boolean) {
        if (isFocused) {
            fieldsThatHadFocus += field
        } else if (field in fieldsThatHadFocus) {
            // Salió del campo sin escribir (o dejándolo inválido): se muestra el error.
            _uiState.update { it.withErrorFor(field) }
        }
    }

    fun onCityChange(value: String) = _uiState.update { it.copy(city = value) }
    fun onAddressChange(value: String) = _uiState.update { it.copy(address = value) }
    fun onTermsChange(value: Boolean) = _uiState.update { it.copy(termsAccepted = value) }

    /** Valida toda la fase 1 y solo avanza si es correcta; si no, muestra todos los errores. */
    fun onNextStep() = _uiState.update { state ->
        val validated = state.withAllPersonalDataErrors()
        if (validated.isPersonalDataValid) validated.copy(step = 2) else validated
    }

    fun onPreviousStep() = _uiState.update { it.copy(step = (it.step - 1).coerceAtLeast(1)) }

    fun onSetLocationClick() {
        // TODO (Mapbox): obtener la ubicación y rellenar ciudad y dirección.
    }

    fun onCreateAccountClick() {
        val state = _uiState.value
        // Doble protección: el botón ya se deshabilita, pero conviene blindar el toque doble.
        if (!state.isLocationDataFilled || state.isLoading) return

        // Se activa ANTES de lanzar la corrutina: un segundo toque ya encuentra isLoading = true.
        _uiState.update { it.copy(isLoading = true, registerError = null) }

        viewModelScope.launch {
            registerWithUniqueUserName(state)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, showSuccessDialog = true) }
                }
                .onFailure { error ->
                    _uiState.update { it.withFailure(error) }
                }
        }
    }

    fun onSuccessDialogAccept() {
        _uiState.update { it.copy(showSuccessDialog = false) }
        _events.trySend(RegisterEvent.RegisterSuccess)
    }

    // El alias es aleatorio, así que puede chocar con uno existente. El Use Case lo comprueba
    // ANTES de crear la cuenta, por lo que reintentar con otro alias no deja nada a medias.
    private suspend fun registerWithUniqueUserName(state: RegisterUiState): Result<Account> {
        val credentials = Credentials(email = state.email.trim(), password = state.password)

        // TODO (Mapbox): reemplazar las coordenadas fijas por las del punto elegido en el mapa.
        val location = Location(
            latitude = DEFAULT_LATITUDE,
            longitude = DEFAULT_LONGITUDE,
            city = state.city.trim(),
            country = DEFAULT_COUNTRY,
            address = state.address.trim(),
        )

        var result: Result<Account> = Result.failure(UserException.UserNameTaken)
        repeat(MAX_USERNAME_ATTEMPTS) {
            result = registerUseCase(
                credentials = credentials,
                userName = UserNameGenerator.generate(state.fullName),
                name = state.fullName,
                phone = state.phone,
                location = location,
            )
            if (result.exceptionOrNull() != UserException.UserNameTaken) return result
        }
        return result
    }

    // Muestra el error de un solo campo (al perder el foco).
    private fun RegisterUiState.withErrorFor(field: RegisterField) = when (field) {
        RegisterField.NAME -> copy(nameError = validateName(fullName))
        RegisterField.EMAIL -> copy(emailError = validateEmail(email))
        RegisterField.PHONE -> copy(phoneError = validatePhone(phone))
        RegisterField.PASSWORD -> copy(passwordError = validatePassword(password))
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
        val base = copy(isLoading = false)
        return when (error) {
            AuthException.InvalidEmail -> base.copy(emailError = FieldError.INVALID, step = 1)
            AuthException.EmailAlreadyInUse -> base.copy(emailError = FieldError.TAKEN, step = 1)
            AuthException.WeakPassword -> base.copy(passwordError = FieldError.WEAK, step = 1)
            UserException.InvalidName -> base.copy(nameError = FieldError.INVALID, step = 1)
            UserException.InvalidPhone -> base.copy(phoneError = FieldError.INVALID, step = 1)
            UserException.PhoneTaken -> base.copy(phoneError = FieldError.TAKEN, step = 1)
            UserException.InvalidLocation -> base.copy(registerError = RegisterError.LOCATION)
            else -> base.copy(registerError = RegisterError.GENERIC)
        }
    }

    companion object {
        private const val MAX_USERNAME_ATTEMPTS = 5

        // TEMPORAL (Mapbox): centro de Armenia, el mismo de SeedData.
        private const val DEFAULT_LATITUDE = 4.5339
        private const val DEFAULT_LONGITUDE = -75.6811
        private const val DEFAULT_COUNTRY = "Colombia"

        // Las mismas reglas que aplica el Use Case: la pantalla nunca acepta algo que él va a rechazar.
        fun validateName(name: String): FieldError? = when {
            name.isBlank() -> FieldError.EMPTY
            !UserValidator.isNameValid(name) -> FieldError.INVALID
            else -> null
        }

        fun validateEmail(email: String): FieldError? = when {
            email.isBlank() -> FieldError.EMPTY
            !AuthValidator.isEmailValid(email) -> FieldError.INVALID
            else -> null
        }

        fun validatePhone(phone: String): FieldError? = when {
            phone.isBlank() -> FieldError.EMPTY
            !UserValidator.isPhoneValid(phone) -> FieldError.INVALID
            else -> null
        }

        fun validatePassword(password: String): FieldError? = when {
            password.isEmpty() -> FieldError.EMPTY
            !AuthValidator.isPasswordValid(password) -> FieldError.WEAK
            else -> null
        }

        fun validateConfirmPassword(password: String, confirmPassword: String): FieldError? = when {
            confirmPassword.isEmpty() -> FieldError.EMPTY
            confirmPassword != password -> FieldError.MISMATCH
            else -> null
        }

        // La regla vive junto a la barra (la comparte Cambiar contraseña).
        fun strengthOf(password: String): PasswordStrength? = passwordStrengthOf(password)
    }
}