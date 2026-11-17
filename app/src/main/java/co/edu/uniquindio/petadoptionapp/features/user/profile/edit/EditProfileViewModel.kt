package co.edu.uniquindio.petadoptionapp.features.user.profile.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.error.UserException
import co.edu.uniquindio.petadoptionapp.domain.model.location.Location
import co.edu.uniquindio.petadoptionapp.domain.usecase.location.ReverseGeocodeUseCase
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

/**
ViewModel de la pantalla "Editar perfil".
Carga una sola vez los datos del usuario en sesión con ObserveCurrentUserUseCase y los
expone en EditProfileUiState junto con una copia de "lo guardado" para detectar cambios.
Valida cada campo mientras la persona escribe (con UserValidator, las mismas reglas del
registro) y, al tocar "Guardar Cambios", persiste nombre, teléfono, foto y ubicación con
UpdateProfileUseCase. Si el guardado sale bien muestra un diálogo de éxito; al aceptarlo
emite el evento EditProfileEvent.Saved, que la pantalla traduce en el callback onSaved
para que AppNavigation regrese al perfil. Los errores del caso de uso se traducen a
errores de campo (ProfileFieldError) o a un error general (EditProfileError).
**/

// Error de un campo; el texto lo pone la UI con strings.xml.
enum class ProfileFieldError { EMPTY, INVALID, TAKEN }

// Error general al guardar (no pertenece a un campo concreto).
enum class EditProfileError { LOCATION, GENERIC }

/**
Estado completo del formulario de edición de perfil.
Guarda los valores que la persona está escribiendo, el error de cada campo, banderas de
carga/guardado/diálogo y los valores "saved*" (lo último persistido) para saber si hay
cambios pendientes. Las propiedades calculadas hasChanges, canSave e isFormValid
deciden si el botón "Guardar Cambios" se habilita.
**/
data class EditProfileUiState(
    // Solo lectura: identifican a la persona pero no se editan aquí.
    val userName: String = "",
    // Correo de la cuenta, visible pero de solo lectura.
    val email: String = "",
    // Foto de perfil: se cambia con la cámara o la galería.
    val photoUrl: String? = null,
    // Campos editables.
    // Nombre completo que la persona está escribiendo.
    val fullName: String = "",
    // Teléfono (solo dígitos).
    val phone: String = "",
    // Error actual del campo nombre (null si es válido).
    val nameError: ProfileFieldError? = null,
    // Error actual del campo teléfono.
    val phoneError: ProfileFieldError? = null,
    // Error general al guardar, que no pertenece a un campo concreto.
    val saveError: EditProfileError? = null,
    // Indica si ya se cargaron los datos iniciales del usuario.
    val isLoaded: Boolean = false,
    // Indica si hay un guardado en curso (bloquea el botón).
    val isSaving: Boolean = false,
    // Controla la visibilidad del diálogo de "cambios guardados".
    val showSuccessDialog: Boolean = false,
    // Lo que está guardado: sirve para saber si la persona cambió algo.
    // Nombre guardado.
    val savedFullName: String = "",
    // Teléfono guardado.
    val savedPhone: String = "",
    // Foto guardada.
    val savedPhotoUrl: String? = null,
    // Ubicación guardada en el perfil (con coordenadas): la referencia para saber si cambió.
    val savedLocation: Location = Location(),
    // true si la persona eligió "Ubicación actual"; false si la elige en el mapa (ciudad y dirección).
    val useCurrentLocation: Boolean = false,
    // Ubicación leída del GPS y su dirección; null hasta que se pida.
    val currentLocation: Location? = null,
    // true mientras se pide el permiso y se lee el GPS.
    val isLocating: Boolean = false,
    // true si no se pudo obtener la ubicación (permiso negado o GPS apagado).
    val locationUnavailable: Boolean = false,
    // Ubicación elegida en el mapa; arranca con la guardada para que el selector la muestre.
    val mapLocation: Location? = null,
    // true si al guardar no hay una ubicación válida elegida.
    val locationError: Boolean = false,
) {
    // Ubicación que se guardará, según el modo elegido.
    val selectedLocation: Location? get() = if (useCurrentLocation) currentLocation else mapLocation

    // ¿La ubicación elegida es distinta de la guardada? (punto, ciudad o dirección)
    val locationChanged: Boolean
        get() = selectedLocation?.let {
            it.latitude != savedLocation.latitude || it.longitude != savedLocation.longitude ||
                it.city.trim() != savedLocation.city.trim() || it.address.trim() != savedLocation.address.trim()
        } ?: false

    // Hay ubicación válida: con coordenadas y ciudad.
    val hasValidLocation: Boolean
        get() = selectedLocation?.let { it.hasCoordinates() && it.city.isNotBlank() } == true

    // Se compara sin espacios sobrantes: agregar un espacio al final no cuenta como cambio.
    val hasChanges: Boolean
        // Hay cambios si cualquiera de los campos difiere de su valor guardado.
        get() = fullName.trim() != savedFullName.trim() ||
            phone != savedPhone ||
            locationChanged ||
            photoUrl != savedPhotoUrl

    // "Guardar Cambios" solo se habilita si hay algo nuevo y todo es válido.
    val canSave: Boolean
        // Formulario válido, con cambios y sin un guardado en curso.
        get() = isFormValid && hasChanges && !isSaving

    // Sin errores y con todo lleno: habilita "Guardar Cambios".
    val isFormValid: Boolean
        // Requiere datos cargados, ningún error de campo y ningún campo vacío.
        get() = isLoaded &&
            listOf(nameError, phoneError).all { it == null } &&
            listOf(fullName, phone).all { it.isNotBlank() } &&
            hasValidLocation
}

// Eventos de una sola vez que el ViewModel envía a la pantalla (no forman parte del estado).
sealed interface EditProfileEvent {
    // Se guardó y la persona cerró el aviso: la navegación vuelve al perfil.
    data object Saved : EditProfileEvent
}

// Marca la clase para que Hilt la construya e inyecte sus dependencias.
@HiltViewModel
class EditProfileViewModel @Inject constructor(
    // Caso de uso que emite el usuario en sesión.
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    // Caso de uso que valida y guarda los cambios del perfil.
    private val updateProfile: UpdateProfileUseCase,
    // Caso de uso que convierte las coordenadas del GPS en ciudad y dirección.
    private val reverseGeocode: ReverseGeocodeUseCase,
) : ViewModel() {

    // Estado mutable interno del formulario.
    private val _uiState = MutableStateFlow(EditProfileUiState())
    // Estado público de solo lectura que observa la pantalla.
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    // Canal con búfer para eventos de una sola vez (no se pierden si la UI aún no escucha).
    private val _events = Channel<EditProfileEvent>(Channel.BUFFERED)
    // Flujo público de eventos que la pantalla recolecta.
    val events = _events.receiveAsFlow()

    // Datos que no se muestran pero se deben conservar al guardar.
    // Identificador del usuario, necesario para actualizar su documento.
    private var userId = ""

    // Bloque de inicialización: carga los datos del usuario al crear el ViewModel.
    init {
        // Solo la primera vez: si el perfil cambiara mientras escribe, no se pisa lo que lleva escrito.
        viewModelScope.launch {
            // Espera el primer usuario no nulo y deja de observar.
            val user = observeCurrentUser().filterNotNull().first()
            // Guarda el id de la cuenta para el guardado posterior.
            userId = user.account.id
            // Rellena el formulario y los valores "guardados" con los datos actuales.
            _uiState.update {
                it.copy(
                    // Datos de solo lectura.
                    userName = user.account.userName,
                    photoUrl = user.photoUrl,
                    email = user.account.email,
                    // Valores iniciales de los campos editables.
                    fullName = user.name,
                    phone = user.phone,
                    // El selector del mapa arranca en la ubicación guardada.
                    mapLocation = user.location,
                    // Copia de lo guardado para detectar cambios.
                    savedFullName = user.name,
                    savedPhone = user.phone,
                    savedLocation = user.location,
                    savedPhotoUrl = user.photoUrl,
                    // Marca el formulario como cargado.
                    isLoaded = true,
                )
            }
        }
    }

    // Al escribir se valida de inmediato, igual que en el registro.
    // Nueva foto (ya guardada en la app); se aplica al tocar "Guardar Cambios".
    fun onPhotoPicked(url: String) = _uiState.update { it.copy(photoUrl = url, saveError = null) }

    // Actualiza el nombre, lo valida al instante y limpia el error general.
    fun onFullNameChange(value: String) =
        _uiState.update { it.copy(fullName = value, nameError = validateName(value), saveError = null) }

    // Solo dígitos y con el máximo del validador: así nunca se escribe un teléfono imposible.
    fun onPhoneChange(value: String) {
        // Filtra caracteres no numéricos y recorta a la longitud máxima permitida.
        val digits = value.filter(Char::isDigit).take(UserValidator.MAX_PHONE_LENGTH)
        // Guarda el teléfono depurado con su validación y limpia el error general.
        _uiState.update { it.copy(phone = digits, phoneError = validatePhone(digits), saveError = null) }
    }

    // Cambia entre "Ubicación actual" y "Seleccionar mapa".
    fun onUseCurrentLocationChange(value: Boolean) =
        _uiState.update { it.copy(useCurrentLocation = value, locationError = false, saveError = null) }

    // La pantalla pidió el permiso y está leyendo el GPS.
    fun onLocatingStarted() = _uiState.update { it.copy(isLocating = true, locationUnavailable = false) }

    /**
    Ubicación actual del teléfono (o null si no se pudo leer): se le busca la ciudad y la dirección.
    **/
    fun onCurrentLocationResult(latitude: Double?, longitude: Double?) {
        // Sin coordenadas (permiso negado o GPS apagado): se avisa y se termina.
        if (latitude == null || longitude == null) {
            // Termina la localización con el aviso de ubicación no disponible.
            _uiState.update { it.copy(isLocating = false, locationUnavailable = true) }
            // No hay nada más que hacer.
            return
        }
        // Busca la dirección en segundo plano.
        viewModelScope.launch {
            // Convierte las coordenadas en una ubicación con ciudad y dirección.
            val location = reverseGeocode(latitude, longitude)
            // Guarda la ubicación actual y termina la localización.
            _uiState.update { it.copy(isLocating = false, currentLocation = location, locationError = false, saveError = null) }
        }
    }

    // Punto elegido en el mapa, con la ciudad del selector y la dirección buscada o tocada.
    fun onMapLocationPicked(location: Location) =
        _uiState.update { it.copy(mapLocation = location, locationError = false, saveError = null) }

    // Valida todo el formulario y, si es posible, guarda los cambios del perfil.
    fun onSaveClick() {
        // Calcula los errores de todos los campos a la vez (por si alguno no se tocó).
        val state = _uiState.value.withAllErrors()
        // Publica el estado con los errores para que la UI los muestre.
        _uiState.value = state
        // Doble protección: el botón ya se deshabilita, pero conviene blindar el toque doble.
        if (!state.canSave) return

        // Marca el inicio del guardado y limpia el error general previo.
        _uiState.update { it.copy(isSaving = true, saveError = null) }
        // Lanza el guardado en una corrutina ligada al ViewModel.
        viewModelScope.launch {
            // La ubicación elegida (actual o del mapa), con sus textos limpios; isFormValid garantiza que existe.
            val location = state.selectedLocation!!.let { it.copy(city = it.city.trim(), address = it.address.trim()) }
            // Invoca el caso de uso con los datos del formulario.
            updateProfile(userId, state.fullName, state.phone, state.photoUrl, location)
                // Si se guardó correctamente:
                .onSuccess {
                    // Termina el guardado, muestra el diálogo y sincroniza los valores "guardados".
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            showSuccessDialog = true,
                            savedFullName = it.fullName,
                            savedPhone = it.phone,
                            // La recién guardada pasa a ser la referencia; el mapa queda en ella.
                            savedLocation = location,
                            mapLocation = location,
                            savedPhotoUrl = it.photoUrl,
                        )
                    }
                }
                // Si falla, traduce el error al campo correspondiente.
                .onFailure { error -> _uiState.update { it.withFailure(error) } }
        }
    }

    // Se llama al aceptar el diálogo de éxito.
    fun onSuccessDialogAccept() {
        // Oculta el diálogo.
        _uiState.update { it.copy(showSuccessDialog = false) }
        // Emite el evento para que la pantalla navegue de vuelta al perfil.
        _events.trySend(EditProfileEvent.Saved)
    }

    // Devuelve una copia del estado con la validación de todos los campos aplicada.
    private fun EditProfileUiState.withAllErrors() = copy(
        nameError = validateName(fullName),
        phoneError = validatePhone(phone),
        // Sin una ubicación válida elegida se muestra el aviso bajo el selector.
        locationError = !hasValidLocation,
    )

    // Traduce el error del caso de uso al campo que corresponde.
    private fun EditProfileUiState.withFailure(error: Throwable): EditProfileUiState {
        // Base común: el guardado terminó.
        val base = copy(isSaving = false)
        // Asigna el error al campo o al mensaje general según su tipo.
        return when (error) {
            // Nombre con formato inválido.
            UserException.InvalidName -> base.copy(nameError = ProfileFieldError.INVALID)
            // Teléfono con formato inválido.
            UserException.InvalidPhone -> base.copy(phoneError = ProfileFieldError.INVALID)
            // Teléfono ya registrado por otra cuenta.
            UserException.PhoneTaken -> base.copy(phoneError = ProfileFieldError.TAKEN)
            // Ubicación inválida: error general de ubicación.
            UserException.InvalidLocation -> base.copy(saveError = EditProfileError.LOCATION)
            // Cualquier otro error: mensaje genérico.
            else -> base.copy(saveError = EditProfileError.GENERIC)
        }
    }

    // Funciones de validación reutilizables, sin acceso al estado del ViewModel.
    private companion object {
        // Valida el nombre: vacío o con formato inválido según UserValidator.
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
    }
}
