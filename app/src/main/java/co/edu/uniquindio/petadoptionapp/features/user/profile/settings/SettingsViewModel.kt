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

/**
ViewModel de la pantalla "Configuración", compartida por usuarios y moderadores.
Gestiona cuatro bloques:
1. Interruptores de verificación en dos pasos y de perfil público: se guardan de forma
   optimista con UpdateSettingsUseCase (o UpdateModeratorTwoFactorUseCase para moderadores)
   y se revierten si el guardado falla.
2. Diálogo "Cambiar contraseña": valida los campos con AuthValidator y llama a ChangePasswordUseCase.
3. Diálogo "Cerrar sesión": confirma y ejecuta LogoutUseCase.
4. Diálogo "Eliminar cuenta": pide la contraseña y ejecuta DeleteAccountUseCase.
Los datos iniciales llegan de ObserveCurrentUserUseCase (usuario) o GetCurrentModeratorUseCase
(moderador); el rol se toma de ObserveSessionUseCase. Al cerrar sesión o eliminar la cuenta
emite SettingsEvent.LoggedOut o SettingsEvent.AccountDeleted, que la pantalla traduce en
callbacks para que AppNavigation regrese al login limpiando la pila de pantallas.
**/

// Errores del diálogo de cambio de contraseña; el texto lo pone la UI con strings.xml.
enum class PasswordDialogError { WRONG_PASSWORD, WEAK_PASSWORD, SAME_PASSWORD, GENERIC }

// Errores del diálogo de eliminar cuenta.
enum class DeleteDialogError { WRONG_PASSWORD, GENERIC }

// Campos del diálogo "Cambiar contraseña": su error se muestra desde que se tocan.
enum class PasswordField { CURRENT, NEW, CONFIRM }

// Error de un campo (igual que en Registro); el texto lo pone la pantalla.
enum class PasswordFieldError { EMPTY, WEAK, SAME, MISMATCH }

/**
Estado del diálogo "Cambiar contraseña". Solo existe mientras el diálogo está abierto
(en SettingsUiState vale null cuando está cerrado). Guarda los tres valores escritos,
qué campos ya se tocaron (para no mostrar errores antes de tiempo), el error devuelto
por el caso de uso y si hay una petición en curso. Las validaciones se calculan a partir
de los valores y solo se exponen como error visible cuando el campo fue tocado.
**/
data class PasswordDialogState(
    // Contraseña actual escrita por la persona.
    val currentPassword: String = "",
    // Nueva contraseña.
    val newPassword: String = "",
    // Confirmación de la nueva contraseña.
    val confirmPassword: String = "",
    // Campos que la persona ya editó; solo ellos muestran su error.
    val touched: Set<PasswordField> = emptySet(),
    // Error que devuelve el caso de uso (contraseña actual incorrecta, etc.).
    val error: PasswordDialogError? = null,
    // Indica si el cambio de contraseña está en curso.
    val isLoading: Boolean = false,
) {
    // Validación de la contraseña actual: solo exige que no esté vacía.
    private val currentValidation: PasswordFieldError?
        get() = if (currentPassword.isEmpty()) PasswordFieldError.EMPTY else null

    // Misma regla que al crear la cuenta (AuthValidator) y distinta de la actual.
    private val newValidation: PasswordFieldError?
        get() = when {
            // Vacía.
            newPassword.isEmpty() -> PasswordFieldError.EMPTY
            // No cumple la política de seguridad.
            !AuthValidator.isPasswordValid(newPassword) -> PasswordFieldError.WEAK
            // Igual a la actual.
            newPassword == currentPassword -> PasswordFieldError.SAME
            // Válida.
            else -> null
        }

    // Validación de la confirmación: no vacía y coincidente con la nueva.
    private val confirmValidation: PasswordFieldError?
        get() = when {
            confirmPassword.isEmpty() -> PasswordFieldError.EMPTY
            confirmPassword != newPassword -> PasswordFieldError.MISMATCH
            else -> null
        }

    // Error visible del campo "actual": solo si ya se tocó.
    val currentError: PasswordFieldError? get() = currentValidation.takeIf { PasswordField.CURRENT in touched }
    // Error visible del campo "nueva": solo si ya se tocó.
    val newError: PasswordFieldError? get() = newValidation.takeIf { PasswordField.NEW in touched }
    // Error visible del campo "confirmar": solo si ya se tocó.
    val confirmError: PasswordFieldError? get() = confirmValidation.takeIf { PasswordField.CONFIRM in touched }

    // Barra de seguridad de la nueva contraseña (como en Registro).
    val strength: PasswordStrength? get() = passwordStrengthOf(newPassword)

    // Se calcula con los valores, no con los errores visibles: un campo sin tocar tampoco cuenta como válido.
    val isFilled: Boolean
        get() = currentValidation == null && newValidation == null && confirmValidation == null
}

// Campo del diálogo "Eliminar cuenta": se confirma con la contraseña.
data class DeleteDialogState(
    // Contraseña escrita para confirmar la eliminación.
    val password: String = "",
    // Error devuelto al intentar eliminar.
    val error: DeleteDialogError? = null,
    // Indica si la eliminación está en curso.
    val isLoading: Boolean = false,
)

// Estado de la pantalla. Un diálogo en null significa que está cerrado.
data class SettingsUiState(
    // Moderador: solo cambiar contraseña, verificación en dos pasos y cerrar sesión (sin privacidad ni eliminar cuenta).
    val isModerator: Boolean = false,
    // Estado del interruptor de verificación en dos pasos.
    val twoFactorEnabled: Boolean = false,
    // Estado del interruptor de perfil público.
    val isProfilePublic: Boolean = true,
    // Indica que falló el guardado de algún interruptor.
    val settingsError: Boolean = false,
    // Diálogo de cambio de contraseña (null si está cerrado).
    val passwordDialog: PasswordDialogState? = null,
    // Muestra el aviso de contraseña cambiada con éxito.
    val showPasswordSuccess: Boolean = false,
    // Diálogo de eliminar cuenta (null si está cerrado).
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

// Marca la clase para que Hilt la construya e inyecte sus dependencias.
@HiltViewModel
class SettingsViewModel @Inject constructor(
    // Emite el usuario en sesión (ajustes de usuario normal).
    observeCurrentUser: ObserveCurrentUserUseCase,
    // Expone la sesión actual para conocer el rol.
    observeSession: ObserveSessionUseCase,
    // Obtiene el perfil del moderador en sesión.
    getCurrentModerator: GetCurrentModeratorUseCase,
    // Guarda la verificación en dos pasos del moderador.
    private val updateModeratorTwoFactor: UpdateModeratorTwoFactorUseCase,
    // Guarda los ajustes del usuario.
    private val updateSettings: UpdateSettingsUseCase,
    // Cambia la contraseña verificando la actual.
    private val changePassword: ChangePasswordUseCase,
    // Elimina la cuenta tras confirmar la contraseña.
    private val deleteAccount: DeleteAccountUseCase,
    // Cierra la sesión.
    private val logout: LogoutUseCase,
) : ViewModel() {

    // Estado mutable interno.
    private val _uiState = MutableStateFlow(SettingsUiState())
    // Estado público de solo lectura.
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // Canal con búfer para eventos de navegación de un solo uso.
    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    // Flujo público de eventos que recolecta la pantalla.
    val events = _events.receiveAsFlow()

    // Últimos ajustes guardados: base para cada cambio y para revertir si falla.
    // Identificador del usuario dueño de los ajustes.
    private var userId = ""
    // Copia de los ajustes que están realmente persistidos.
    private var savedSettings = UserSettings()

    // La pantalla es la misma para ambos roles; el moderador guarda su A2F en su propio perfil.
    private val isModerator = observeSession().value?.role == UserRole.MODERATOR

    // Bloque de inicialización: carga los ajustes según el rol.
    init {
        // Rama exclusiva del moderador.
        if (isModerator) {
            // Indica a la UI que oculte las opciones no disponibles para moderadores.
            _uiState.update { it.copy(isModerator = true) }
            // Carga el estado de la verificación en dos pasos desde el perfil del moderador.
            viewModelScope.launch {
                getCurrentModerator().onSuccess { moderator -> _uiState.update { it.copy(twoFactorEnabled = moderator.twoFactorEnabled) } }
            }
        }
        // Si el perfil cambia, los interruptores muestran lo que realmente está guardado.
        viewModelScope.launch {
            // Observa el usuario en sesión.
            observeCurrentUser().collect { user ->
                // Sin usuario (por ejemplo, moderador o sesión cerrada) no hay nada que cargar.
                if (user == null) return@collect
                // Guarda el id para futuros guardados.
                userId = user.account.id
                // Guarda los ajustes persistidos como referencia.
                savedSettings = user.settings
                // Refleja los ajustes en los interruptores.
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

    // Activa o desactiva la verificación en dos pasos.
    fun onTwoFactorChange(enabled: Boolean) {
        // Usuario normal: guarda el cambio en sus ajustes y termina.
        if (!isModerator) return saveSettings { it.copy(twoFactorEnabled = enabled) }
        // Moderador: mismo cambio optimista, guardado en su perfil de moderador.
        _uiState.update { it.copy(twoFactorEnabled = enabled, settingsError = false) }
        // Persiste el cambio de forma asíncrona.
        viewModelScope.launch {
            // Si falla, revierte el interruptor y muestra el error.
            updateModeratorTwoFactor(enabled).onFailure {
                _uiState.update { it.copy(twoFactorEnabled = !enabled, settingsError = true) }
            }
        }
    }

    // Cambia la visibilidad del perfil entre pública y privada.
    fun onProfilePublicChange(isPublic: Boolean) = saveSettings {
        it.copy(profileVisibility = if (isPublic) ProfileVisibility.PUBLIC else ProfileVisibility.PRIVATE)
    }

    // Cambio optimista: el switch se mueve al instante y vuelve atrás si no se pudo guardar.
    private fun saveSettings(change: (UserSettings) -> UserSettings) {
        // Ajustes previos, por si hay que revertir.
        val previous = savedSettings
        // Ajustes nuevos aplicando el cambio solicitado.
        val updated = change(previous)
        // Muestra el cambio de inmediato y limpia el error anterior.
        _uiState.update { it.withSettings(updated).copy(settingsError = false) }
        // Guarda en segundo plano.
        viewModelScope.launch {
            updateSettings(userId, updated)
                // Si se guardó, los nuevos ajustes pasan a ser la referencia.
                .onSuccess { savedSettings = updated }
                // Si falla, vuelve a los ajustes previos y marca el error.
                .onFailure { _uiState.update { it.withSettings(previous).copy(settingsError = true) } }
        }
    }

    // Copia el estado reflejando los valores de unos ajustes concretos.
    private fun SettingsUiState.withSettings(settings: UserSettings) = copy(
        twoFactorEnabled = settings.twoFactorEnabled,
        isProfilePublic = settings.hasProfilePublic(),
    )

    // --- Cambiar contraseña ---

    // Abre el diálogo de cambio de contraseña con un estado vacío.
    fun onChangePasswordClick() = _uiState.update { it.copy(passwordDialog = PasswordDialogState()) }

    // Cierra el diálogo descartando lo escrito.
    fun onPasswordDialogDismiss() = _uiState.update { it.copy(passwordDialog = null) }

    // Actualiza la contraseña actual, marca el campo como tocado y limpia el error del servidor.
    fun onCurrentPasswordChange(value: String) =
        updatePasswordDialog { it.copy(currentPassword = value, touched = it.touched + PasswordField.CURRENT, error = null) }

    // Actualiza la nueva contraseña y marca el campo como tocado.
    fun onNewPasswordChange(value: String) =
        updatePasswordDialog { it.copy(newPassword = value, touched = it.touched + PasswordField.NEW, error = null) }

    // Actualiza la confirmación y marca el campo como tocado.
    fun onConfirmPasswordChange(value: String) =
        updatePasswordDialog { it.copy(confirmPassword = value, touched = it.touched + PasswordField.CONFIRM, error = null) }

    // Valida y envía el cambio de contraseña.
    fun onPasswordConfirm() {
        // Si el diálogo no está abierto, no hay nada que hacer.
        val dialog = _uiState.value.passwordDialog ?: return
        // Evita enviar dos veces mientras hay una petición en curso.
        if (dialog.isLoading) return
        // Si algo falta o no cumple, se muestran los errores de todos los campos (como en Registro).
        if (!dialog.isFilled) {
            // Marca todos los campos como tocados para que aparezcan sus errores.
            updatePasswordDialog { it.copy(touched = PasswordField.entries.toSet()) }
            return
        }
        // Activa el indicador de carga del diálogo.
        updatePasswordDialog { it.copy(isLoading = true) }
        // Ejecuta el cambio de contraseña.
        viewModelScope.launch {
            changePassword(dialog.currentPassword, dialog.newPassword)
                // Éxito: cierra el diálogo y muestra el aviso de confirmación.
                .onSuccess {
                    _uiState.update { it.copy(passwordDialog = null, showPasswordSuccess = true) }
                }
                // Fallo: traduce la excepción a un error del diálogo.
                .onFailure { error ->
                    val dialogError = when (error) {
                        // La contraseña actual no es correcta.
                        AuthException.WrongPassword -> PasswordDialogError.WRONG_PASSWORD
                        // La nueva no cumple la política.
                        AuthException.WeakPassword -> PasswordDialogError.WEAK_PASSWORD
                        // La nueva es igual a la actual.
                        AuthException.SamePassword -> PasswordDialogError.SAME_PASSWORD
                        // Cualquier otro error.
                        else -> PasswordDialogError.GENERIC
                    }
                    // Quita la carga y muestra el error.
                    updatePasswordDialog { it.copy(isLoading = false, error = dialogError) }
                }
        }
    }

    // Cierra el aviso de contraseña cambiada.
    fun onPasswordSuccessAccept() = _uiState.update { it.copy(showPasswordSuccess = false) }

    // Aplica un cambio al diálogo de contraseña solo si está abierto.
    private fun updatePasswordDialog(change: (PasswordDialogState) -> PasswordDialogState) =
        _uiState.update { state -> state.copy(passwordDialog = state.passwordDialog?.let(change)) }

    // --- Cerrar sesión ---

    // Muestra el diálogo de confirmación de cierre de sesión.
    fun onLogoutClick() = _uiState.update { it.copy(showLogoutDialog = true) }

    // Oculta el diálogo sin cerrar sesión.
    fun onLogoutDismiss() = _uiState.update { it.copy(showLogoutDialog = false) }

    // Confirma el cierre de sesión.
    fun onLogoutConfirm() {
        // Cierra el diálogo.
        _uiState.update { it.copy(showLogoutDialog = false) }
        // Ejecuta el cierre y avisa a la pantalla.
        viewModelScope.launch {
            // Cierra la sesión en el repositorio.
            logout()
            // Emite el evento para navegar al login.
            _events.send(SettingsEvent.LoggedOut)
        }
    }

    // --- Eliminar cuenta ---

    // Abre el diálogo de eliminar cuenta con un estado vacío.
    fun onDeleteAccountClick() = _uiState.update { it.copy(deleteDialog = DeleteDialogState()) }

    // Cierra el diálogo de eliminar cuenta.
    fun onDeleteDialogDismiss() = _uiState.update { it.copy(deleteDialog = null) }

    // Actualiza la contraseña escrita en el diálogo y limpia el error previo.
    fun onDeletePasswordChange(value: String) =
        _uiState.update { state -> state.copy(deleteDialog = state.deleteDialog?.copy(password = value, error = null)) }

    // Confirma la eliminación de la cuenta.
    fun onDeleteConfirm() {
        // Si el diálogo no está abierto, no hace nada.
        val dialog = _uiState.value.deleteDialog ?: return
        // Requiere contraseña y evita envíos duplicados.
        if (dialog.password.isBlank() || dialog.isLoading) return
        // Activa el indicador de carga.
        _uiState.update { it.copy(deleteDialog = dialog.copy(isLoading = true)) }
        // Ejecuta la eliminación.
        viewModelScope.launch {
            deleteAccount(userId, dialog.password)
                // Éxito: avisa a la pantalla para volver al login.
                .onSuccess { _events.send(SettingsEvent.AccountDeleted) }
                // Fallo: decide según el tipo de error.
                .onFailure { error ->
                    when (error) {
                        // El caso de uso ya cerró la sesión: no hay nada más que hacer aquí.
                        UserException.DeletionIncomplete -> _events.send(SettingsEvent.AccountDeleted)
                        // Otros errores se muestran en el diálogo.
                        else -> {
                            // Distingue contraseña incorrecta de un error genérico.
                            val dialogError = if (error === AuthException.WrongPassword) {
                                DeleteDialogError.WRONG_PASSWORD
                            } else {
                                DeleteDialogError.GENERIC
                            }
                            // Quita la carga y muestra el error en el diálogo.
                            _uiState.update { state ->
                                state.copy(deleteDialog = state.deleteDialog?.copy(isLoading = false, error = dialogError))
                            }
                        }
                    }
                }
        }
    }
}
