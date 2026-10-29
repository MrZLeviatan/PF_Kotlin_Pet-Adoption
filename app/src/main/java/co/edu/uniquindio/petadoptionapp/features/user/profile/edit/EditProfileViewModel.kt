package co.edu.uniquindio.petadoptionapp.features.user.profile.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.error.UserException
import co.edu.uniquindio.petadoptionapp.domain.model.location.Location
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.ObserveCurrentUserUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.UpdateProfileUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.UserValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Error de un campo; el texto lo pone la UI con strings.xml.
enum class ProfileFieldError { EMPTY, INVALID, TAKEN }

// Error general al guardar (no pertenece a un campo concreto).
enum class EditProfileError { LOCATION, GENERIC }

data class EditProfileUiState(
    // Solo lectura: identifican a la persona pero no se editan aquí.
    val userName: String = "",
    val email: String = "",
    // Foto de perfil: se cambia con la cámara o la galería.
    val photoUrl: String? = null,
    // Campos editables.
    val fullName: String = "",
    val phone: String = "",
    val city: String = "",
    val address: String = "",
    val nameError: ProfileFieldError? = null,
    val phoneError: ProfileFieldError? = null,
    val cityError: ProfileFieldError? = null,
    val addressError: ProfileFieldError? = null,
    val saveError: EditProfileError? = null,
    val isLoaded: Boolean = false,
    val isSaving: Boolean = false,
    val showSuccessDialog: Boolean = false,
    // Lo que está guardado: sirve para saber si la persona cambió algo.
    val savedFullName: String = "",
    val savedPhone: String = "",
    val savedCity: String = "",
    val savedAddress: String = "",
    val savedPhotoUrl: String? = null,
) {
    // Se compara sin espacios sobrantes: agregar un espacio al final no cuenta como cambio.
    val hasChanges: Boolean
        get() = fullName.trim() != savedFullName.trim() ||
            phone != savedPhone ||
            city.trim() != savedCity.trim() ||
            address.trim() != savedAddress.trim() ||
            photoUrl != savedPhotoUrl

    // "Guardar Cambios" solo se habilita si hay algo nuevo y todo es válido.
    val canSave: Boolean
        get() = isFormValid && hasChanges && !isSaving

    // Sin errores y con todo lleno: habilita "Guardar Cambios".
    val isFormValid: Boolean
        get() = isLoaded &&
            listOf(nameError, phoneError, cityError, addressError).all { it == null } &&
            listOf(fullName, phone, city, address).all { it.isNotBlank() }
}

sealed interface EditProfileEvent {
    // Se guardó y la persona cerró el aviso: la navegación vuelve al perfil.
    data object Saved : EditProfileEvent
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val updateProfile: UpdateProfileUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val _events = Channel<EditProfileEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // Datos que no se muestran pero se deben conservar al guardar.
    private var userId = ""
    private var savedLocation = Location()

    init {
        // Solo la primera vez: si el perfil cambiara mientras escribe, no se pisa lo que lleva escrito.
        viewModelScope.launch {
            val user = observeCurrentUser().filterNotNull().first()
            userId = user.account.id
            savedLocation = user.location
            _uiState.update {
                it.copy(
                    userName = user.account.userName,
                    photoUrl = user.photoUrl,
                    email = user.account.email,
                    fullName = user.name,
                    phone = user.phone,
                    city = user.location.city,
                    address = user.location.address,
                    savedFullName = user.name,
                    savedPhone = user.phone,
                    savedCity = user.location.city,
                    savedAddress = user.location.address,
                    savedPhotoUrl = user.photoUrl,
                    isLoaded = true,
                )
            }
        }
    }

    // Al escribir se valida de inmediato, igual que en el registro.
    // Nueva foto (ya guardada en la app); se aplica al tocar "Guardar Cambios".
    fun onPhotoPicked(url: String) = _uiState.update { it.copy(photoUrl = url, saveError = null) }

    fun onFullNameChange(value: String) =
        _uiState.update { it.copy(fullName = value, nameError = validateName(value), saveError = null) }

    // Solo dígitos y con el máximo del validador: así nunca se escribe un teléfono imposible.
    fun onPhoneChange(value: String) {
        val digits = value.filter(Char::isDigit).take(UserValidator.MAX_PHONE_LENGTH)
        _uiState.update { it.copy(phone = digits, phoneError = validatePhone(digits), saveError = null) }
    }

    fun onCityChange(value: String) =
        _uiState.update { it.copy(city = value, cityError = validateRequired(value), saveError = null) }

    fun onAddressChange(value: String) =
        _uiState.update { it.copy(address = value, addressError = validateRequired(value), saveError = null) }

    fun onSetLocationClick() {
        // TODO (Mapbox): elegir el punto en el mapa y actualizar coordenadas, ciudad y dirección.
    }

    fun onSaveClick() {
        val state = _uiState.value.withAllErrors()
        _uiState.value = state
        // Doble protección: el botón ya se deshabilita, pero conviene blindar el toque doble.
        if (!state.canSave) return

        _uiState.update { it.copy(isSaving = true, saveError = null) }
        viewModelScope.launch {
            // Las coordenadas se conservan hasta que exista el mapa; solo cambian los textos.
            val location = savedLocation.copy(city = state.city.trim(), address = state.address.trim())
            updateProfile(userId, state.fullName, state.phone, state.photoUrl, location)
                .onSuccess {
                    savedLocation = location
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            showSuccessDialog = true,
                            savedFullName = it.fullName,
                            savedPhone = it.phone,
                            savedCity = it.city,
                            savedAddress = it.address,
                            savedPhotoUrl = it.photoUrl,
                        )
                    }
                }
                .onFailure { error -> _uiState.update { it.withFailure(error) } }
        }
    }

    fun onSuccessDialogAccept() {
        _uiState.update { it.copy(showSuccessDialog = false) }
        _events.trySend(EditProfileEvent.Saved)
    }

    private fun EditProfileUiState.withAllErrors() = copy(
        nameError = validateName(fullName),
        phoneError = validatePhone(phone),
        cityError = validateRequired(city),
        addressError = validateRequired(address),
    )

    // Traduce el error del caso de uso al campo que corresponde.
    private fun EditProfileUiState.withFailure(error: Throwable): EditProfileUiState {
        val base = copy(isSaving = false)
        return when (error) {
            UserException.InvalidName -> base.copy(nameError = ProfileFieldError.INVALID)
            UserException.InvalidPhone -> base.copy(phoneError = ProfileFieldError.INVALID)
            UserException.PhoneTaken -> base.copy(phoneError = ProfileFieldError.TAKEN)
            UserException.InvalidLocation -> base.copy(saveError = EditProfileError.LOCATION)
            else -> base.copy(saveError = EditProfileError.GENERIC)
        }
    }

    private companion object {
        fun validateName(name: String): ProfileFieldError? = when {
            name.isBlank() -> ProfileFieldError.EMPTY
            !UserValidator.isNameValid(name) -> ProfileFieldError.INVALID
            else -> null
        }

        // 8 a 15 dígitos (la regla vive en UserValidator).
        fun validatePhone(phone: String): ProfileFieldError? = when {
            phone.isBlank() -> ProfileFieldError.EMPTY
            !UserValidator.isPhoneValid(phone) -> ProfileFieldError.INVALID
            else -> null
        }

        fun validateRequired(value: String): ProfileFieldError? =
            if (value.isBlank()) ProfileFieldError.EMPTY else null
    }
}
