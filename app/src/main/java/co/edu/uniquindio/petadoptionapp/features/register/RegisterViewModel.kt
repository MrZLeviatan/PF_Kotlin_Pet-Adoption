package co.edu.uniquindio.petadoptionapp.features.register

import androidx.lifecycle.ViewModel
import co.edu.uniquindio.petadoptionapp.core.component.form.PasswordStrength
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

/** Estado completo del registro. Un solo ViewModel para ambas fases: no se pierde nada al avanzar. */
data class RegisterUiState(
    val step: Int = 1,
    // Fase 1
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    // Sin lógica por ahora: queda en null y la barra no se muestra.
    val passwordStrength: PasswordStrength? = null,
    // Fase 2
    val city: String = "",
    val address: String = "",
    val termsAccepted: Boolean = false,
    val showSuccessDialog: Boolean = false,
) {
    // Solo habilita los botones cuando hay algo escrito. Las validaciones reales vienen después.
    val isPersonalDataFilled: Boolean
        get() = listOf(fullName, email, phone, password, confirmPassword).all { it.isNotBlank() }

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

class RegisterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _events = Channel<RegisterEvent>(BUFFERED)
    val events = _events.receiveAsFlow()

    fun onFullNameChange(value: String) = _uiState.update { it.copy(fullName = value) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value) }
    fun onPhoneChange(value: String) = _uiState.update { it.copy(phone = value) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value) }
    fun onConfirmPasswordChange(value: String) = _uiState.update { it.copy(confirmPassword = value) }
    fun onCityChange(value: String) = _uiState.update { it.copy(city = value) }
    fun onAddressChange(value: String) = _uiState.update { it.copy(address = value) }
    fun onTermsChange(value: Boolean) = _uiState.update { it.copy(termsAccepted = value) }

    fun onNextStep() = _uiState.update { it.copy(step = (it.step + 1).coerceAtMost(RegisterUiState.TOTAL_STEPS)) }
    fun onPreviousStep() = _uiState.update { it.copy(step = (it.step - 1).coerceAtLeast(1)) }

    fun onSetLocationClick() {
        // TODO (Mapbox): obtener la ubicación y rellenar ciudad y dirección.
    }

    fun onCreateAccountClick() {
        // TEMPORAL: aquí irá el caso de uso de registro (isLoading → llamada → resultado).
        _uiState.update { it.copy(showSuccessDialog = true) }
    }

    fun onSuccessDialogAccept() {
        _uiState.update { it.copy(showSuccessDialog = false) }
        _events.trySend(RegisterEvent.RegisterSuccess)
    }
}