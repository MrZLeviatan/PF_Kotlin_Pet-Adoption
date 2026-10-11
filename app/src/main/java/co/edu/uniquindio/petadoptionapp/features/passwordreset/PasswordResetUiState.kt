package co.edu.uniquindio.petadoptionapp.features.passwordreset



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.core.util.RequestResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PasswordResetViewModel : ViewModel() {

    private val _uiState =
        MutableStateFlow(PasswordResetUiState())

    val uiState: StateFlow<PasswordResetUiState> =
        _uiState.asStateFlow()

    // Actualizar un dígito del código

    fun onVerificationDigitChange(
        index: Int,
        value: String
    ) {
        if (index !in 0..4) return

        // Acepta un solo dígito o un campo vacío.
        if (value.isNotEmpty() &&
            (value.length != 1 || !value[0].isDigit())
        ) {
            return
        }

        _uiState.update { current ->
            val updatedCode =
                current.verificationCode.toMutableList()

            updatedCode[index] = value

            current.copy(
                verificationCode = updatedCode,
                codeError = validateCode(updatedCode),
                resetResult = null
            )
        }
    }

    // Actualizar la nueva contraseña

    fun onNewPasswordChange(value: String) {
        _uiState.update { current ->
            current.copy(
                newPassword = value,
                newPasswordError = validatePassword(value),
                confirmPasswordError = validateConfirmation(
                    password = value,
                    confirmation = current.confirmPassword
                ),
                resetResult = null
            )
        }
    }

    // Actualizar la confirmación

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { current ->
            current.copy(
                confirmPassword = value,
                confirmPasswordError = validateConfirmation(
                    password = current.newPassword,
                    confirmation = value
                ),
                resetResult = null
            )
        }
    }

    // Mostrar u ocultar contraseñas

    fun togglePasswordVisibility() {
        _uiState.update {
            it.copy(
                passwordVisible = !it.passwordVisible
            )
        }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update {
            it.copy(
                confirmPasswordVisible =
                    !it.confirmPasswordVisible
            )
        }
    }

    // Validaciones

    private fun validateCode(
        code: List<String>
    ): String? {
        return if (
            code.size != 5 ||
            code.any { it.length != 1 || !it[0].isDigit() }
        ) {
            "Ingresa los 5 dígitos del código"
        } else {
            null
        }
    }

    private fun validatePassword(
        password: String
    ): String? {
        return when {
            password.isBlank() ->
                "La nueva contraseña es obligatoria"

            password.length < 8 ->
                "Debe tener al menos 8 caracteres"

            else -> null
        }
    }

    private fun validateConfirmation(
        password: String,
        confirmation: String
    ): String? {
        return when {
            confirmation.isBlank() ->
                "Confirma la nueva contraseña"

            password != confirmation ->
                "Las contraseñas no coinciden"

            else -> null
        }
    }

    // Validar el formulario completo

    private fun validateForm(): Boolean {
        _uiState.update { current ->
            current.copy(
                codeError = validateCode(
                    current.verificationCode
                ),
                newPasswordError = validatePassword(
                    current.newPassword
                ),
                confirmPasswordError = validateConfirmation(
                    current.newPassword,
                    current.confirmPassword
                )
            )
        }

        return _uiState.value.isFormValid
    }

    // Comprobar datos localmente

    fun submitPasswordReset() {
        if (!validateForm()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    resetResult = RequestResult.Success(
                        message =
                            "Los datos son válidos. " +
                                    "Todavía no se ha cambiado la contraseña, " +
                                    "porque falta conectar el servicio de autenticación."
                    )
                )
            }
        }
    }
}