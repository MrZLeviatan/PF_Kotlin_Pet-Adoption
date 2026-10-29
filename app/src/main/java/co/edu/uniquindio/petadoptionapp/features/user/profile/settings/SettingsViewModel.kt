package co.edu.uniquindio.petadoptionapp.features.user.profile.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.error.UserException
import co.edu.uniquindio.petadoptionapp.domain.model.users.profile.ProfileVisibility
import co.edu.uniquindio.petadoptionapp.domain.model.users.profile.UserSettings
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.ChangePasswordUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.LogoutUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.DeleteAccountUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.ObserveCurrentUserUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.UpdateSettingsUseCase
import co.edu.uniquindio.petadoptionapp.core.component.form.PasswordStrength
import co.edu.uniquindio.petadoptionapp.core.component.form.passwordStrengthOf
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.AuthValidator
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.ObserveSessionUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.GetCurrentModeratorUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.UpdateModeratorTwoFactorUseCase
import co.edu.uniquindio.petadoptionapp.domain.model.users.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Errores del diálogo de cambio de contraseña; el texto lo pone la UI con strings.xml.
enum class PasswordDialogError { WRONG_PASSWORD, WEAK_PASSWORD, SAME_PASSWORD, GENERIC }

// Errores del diálogo de eliminar cuenta.
enum class DeleteDialogError { WRONG_PASSWORD, GENERIC }

// Campos del diálogo "Cambiar contraseña". Solo existe mientras el diálogo está abierto.
// Campos del diálogo "Cambiar contraseña": su error se muestra desde que se tocan.
enum class PasswordField { CURRENT, NEW, CONFIRM }

// Error de un campo (igual que en Registro); el texto lo pone la pantalla.
enum class PasswordFieldError { EMPTY, WEAK, SAME, MISMATCH }

data class PasswordDialogState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val touched: Set<PasswordField> = emptySet(),
    // Error que devuelve el caso de uso (contraseña actual incorrecta, etc.).
    val error: PasswordDialogError? = null,
    val isLoading: Boolean = false,
) {
    private val currentValidation: PasswordFieldError?
        get() = if (currentPassword.isEmpty()) PasswordFieldError.EMPTY else null

    // Misma regla que al crear la cuenta (AuthValidator) y distinta de la actual.
    private val newValidation: PasswordFieldError?
        get() = when {
            newPassword.isEmpty() -> PasswordFieldError.EMPTY
            !AuthValidator.isPasswordValid(newPassword) -> PasswordFieldError.WEAK
            newPassword == currentPassword -> PasswordFieldError.SAME
            else -> null
        }

    private val confirmValidation: PasswordFieldError?
        get() = when {
            confirmPassword.isEmpty() -> PasswordFieldError.EMPTY
            confirmPassword != newPassword -> PasswordFieldError.MISMATCH
            else -> null
        }

    val currentError: PasswordFieldError? get() = currentValidation.takeIf { PasswordField.CURRENT in touched }
    val newError: PasswordFieldError? get() = newValidation.takeIf { PasswordField.NEW in touched }
    val confirmError: PasswordFieldError? get() = confirmValidation.takeIf { PasswordField.CONFIRM in touched }

    // Barra de seguridad de la nueva contraseña (como en Registro).
    val strength: PasswordStrength? get() = passwordStrengthOf(newPassword)

    // Se calcula con los valores, no con los errores visibles: un campo sin tocar tampoco cuenta como válido.
    val isFilled: Boolean
        get() = currentValidation == null && newValidation == null && confirmValidation == null
}

// Campo del diálogo "Eliminar cuenta": se confirma con la contraseña.
data class DeleteDialogState(
    val password: String = "",
    val error: DeleteDialogError? = null,
    val isLoading: Boolean = false,
)

// Estado de la pantalla. Un diálogo en null significa que está cerrado.
data class SettingsUiState(
    // Moderador: solo cambiar contraseña, verificación en dos pasos y cerrar sesión (sin privacidad ni eliminar cuenta).
    val isModerator: Boolean = false,
    val twoFactorEnabled: Boolean = false,
    val isProfilePublic: Boolean = true,
    val settingsError: Boolean = false,
    val passwordDialog: PasswordDialogState? = null,
    val showPasswordSuccess: Boolean = false,
    val deleteDialog: DeleteDialogState? = null,
    // Confirmación de "Cerrar sesión".
    val showLogoutDialog: Boolean = false,
)

// Eventos de un solo uso.
sealed interface SettingsEvent {
    // La cuenta se eliminó (o quedó sin sesión): la navegación vuelve al login.
    data object AccountDeleted : SettingsEvent

    // Se cerró la sesión: la navegación vuelve al login y limpia las pantallas abiertas.
    data object LoggedOut : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
    observeSession: ObserveSessionUseCase,
    getCurrentModerator: GetCurrentModeratorUseCase,
    private val updateModeratorTwoFactor: UpdateModeratorTwoFactorUseCase,
    private val updateSettings: UpdateSettingsUseCase,
    private val changePassword: ChangePasswordUseCase,
    private val deleteAccount: DeleteAccountUseCase,
    private val logout: LogoutUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // Últimos ajustes guardados: base para cada cambio y para revertir si falla.
    private var userId = ""
    private var savedSettings = UserSettings()

    // La pantalla es la misma para ambos roles; el moderador guarda su A2F en su propio perfil.
    private val isModerator = observeSession().value?.role == UserRole.MODERATOR

    init {
        if (isModerator) {
            _uiState.update { it.copy(isModerator = true) }
            viewModelScope.launch {
                getCurrentModerator().onSuccess { moderator -> _uiState.update { it.copy(twoFactorEnabled = moderator.twoFactorEnabled) } }
            }
        }
        // Si el perfil cambia, los interruptores muestran lo que realmente está guardado.
        viewModelScope.launch {
            observeCurrentUser().collect { user ->
                if (user == null) return@collect
                userId = user.account.id
                savedSettings = user.settings
                _uiState.update {
                    it.copy(
                        twoFactorEnabled = user.settings.twoFactorEnabled,
                        isProfilePublic = user.settings.hasProfilePublic(),
                    )
                }
            }
        }
    }

    // --- Interruptores ---

    fun onTwoFactorChange(enabled: Boolean) {
        if (!isModerator) return saveSettings { it.copy(twoFactorEnabled = enabled) }
        // Moderador: mismo cambio optimista, guardado en su perfil de moderador.
        _uiState.update { it.copy(twoFactorEnabled = enabled, settingsError = false) }
        viewModelScope.launch {
            updateModeratorTwoFactor(enabled).onFailure {
                _uiState.update { it.copy(twoFactorEnabled = !enabled, settingsError = true) }
            }
        }
    }

    fun onProfilePublicChange(isPublic: Boolean) = saveSettings {
        it.copy(profileVisibility = if (isPublic) ProfileVisibility.PUBLIC else ProfileVisibility.PRIVATE)
    }

    // Cambio optimista: el switch se mueve al instante y vuelve atrás si no se pudo guardar.
    private fun saveSettings(change: (UserSettings) -> UserSettings) {
        val previous = savedSettings
        val updated = change(previous)
        _uiState.update { it.withSettings(updated).copy(settingsError = false) }
        viewModelScope.launch {
            updateSettings(userId, updated)
                .onSuccess { savedSettings = updated }
                .onFailure { _uiState.update { it.withSettings(previous).copy(settingsError = true) } }
        }
    }

    private fun SettingsUiState.withSettings(settings: UserSettings) = copy(
        twoFactorEnabled = settings.twoFactorEnabled,
        isProfilePublic = settings.hasProfilePublic(),
    )

    // --- Cambiar contraseña ---

    fun onChangePasswordClick() = _uiState.update { it.copy(passwordDialog = PasswordDialogState()) }

    fun onPasswordDialogDismiss() = _uiState.update { it.copy(passwordDialog = null) }

    fun onCurrentPasswordChange(value: String) =
        updatePasswordDialog { it.copy(currentPassword = value, touched = it.touched + PasswordField.CURRENT, error = null) }

    fun onNewPasswordChange(value: String) =
        updatePasswordDialog { it.copy(newPassword = value, touched = it.touched + PasswordField.NEW, error = null) }

    fun onConfirmPasswordChange(value: String) =
        updatePasswordDialog { it.copy(confirmPassword = value, touched = it.touched + PasswordField.CONFIRM, error = null) }

    fun onPasswordConfirm() {
        val dialog = _uiState.value.passwordDialog ?: return
        if (dialog.isLoading) return
        // Si algo falta o no cumple, se muestran los errores de todos los campos (como en Registro).
        if (!dialog.isFilled) {
            updatePasswordDialog { it.copy(touched = PasswordField.entries.toSet()) }
            return
        }
        updatePasswordDialog { it.copy(isLoading = true) }
        viewModelScope.launch {
            changePassword(dialog.currentPassword, dialog.newPassword)
                .onSuccess {
                    _uiState.update { it.copy(passwordDialog = null, showPasswordSuccess = true) }
                }
                .onFailure { error ->
                    val dialogError = when (error) {
                        AuthException.WrongPassword -> PasswordDialogError.WRONG_PASSWORD
                        AuthException.WeakPassword -> PasswordDialogError.WEAK_PASSWORD
                        AuthException.SamePassword -> PasswordDialogError.SAME_PASSWORD
                        else -> PasswordDialogError.GENERIC
                    }
                    updatePasswordDialog { it.copy(isLoading = false, error = dialogError) }
                }
        }
    }

    fun onPasswordSuccessAccept() = _uiState.update { it.copy(showPasswordSuccess = false) }

    private fun updatePasswordDialog(change: (PasswordDialogState) -> PasswordDialogState) =
        _uiState.update { state -> state.copy(passwordDialog = state.passwordDialog?.let(change)) }

    // --- Cerrar sesión ---

    fun onLogoutClick() = _uiState.update { it.copy(showLogoutDialog = true) }

    fun onLogoutDismiss() = _uiState.update { it.copy(showLogoutDialog = false) }

    fun onLogoutConfirm() {
        _uiState.update { it.copy(showLogoutDialog = false) }
        viewModelScope.launch {
            logout()
            _events.send(SettingsEvent.LoggedOut)
        }
    }

    // --- Eliminar cuenta ---

    fun onDeleteAccountClick() = _uiState.update { it.copy(deleteDialog = DeleteDialogState()) }

    fun onDeleteDialogDismiss() = _uiState.update { it.copy(deleteDialog = null) }

    fun onDeletePasswordChange(value: String) =
        _uiState.update { state -> state.copy(deleteDialog = state.deleteDialog?.copy(password = value, error = null)) }

    fun onDeleteConfirm() {
        val dialog = _uiState.value.deleteDialog ?: return
        if (dialog.password.isBlank() || dialog.isLoading) return
        _uiState.update { it.copy(deleteDialog = dialog.copy(isLoading = true)) }
        viewModelScope.launch {
            deleteAccount(userId, dialog.password)
                .onSuccess { _events.send(SettingsEvent.AccountDeleted) }
                .onFailure { error ->
                    when (error) {
                        // El caso de uso ya cerró la sesión: no hay nada más que hacer aquí.
                        UserException.DeletionIncomplete -> _events.send(SettingsEvent.AccountDeleted)
                        else -> {
                            val dialogError = if (error === AuthException.WrongPassword) {
                                DeleteDialogError.WRONG_PASSWORD
                            } else {
                                DeleteDialogError.GENERIC
                            }
                            _uiState.update { state ->
                                state.copy(deleteDialog = state.deleteDialog?.copy(isLoading = false, error = dialogError))
                            }
                        }
                    }
                }
        }
    }
}
