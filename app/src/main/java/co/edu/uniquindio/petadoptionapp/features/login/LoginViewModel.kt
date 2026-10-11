package co.edu.uniquindio.petadoptionapp.features.login



import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())

    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = null,
            errorMessage = null
        )
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = null,
            errorMessage = null
        )
    }

    fun onPasswordVisibilityChange() {
        _uiState.value = _uiState.value.copy(
            passwordVisible = !_uiState.value.passwordVisible
        )
    }

    fun login() {

        val currentState = _uiState.value

        val emailError = when {
            currentState.email.isBlank() ->
                "Ingresa tu correo electrónico"

            !currentState.email.contains("@") ->
                "Ingresa un correo válido"

            else -> null
        }

        val passwordError = when {
            currentState.password.isBlank() ->
                "Ingresa tu contraseña"

            else -> null
        }

        _uiState.value = currentState.copy(
            emailError = emailError,
            passwordError = passwordError
        )

        if (emailError != null || passwordError != null) {
            return
        }

        // Más adelante:
        // LoginUseCase → Repository → Backend
    }
}