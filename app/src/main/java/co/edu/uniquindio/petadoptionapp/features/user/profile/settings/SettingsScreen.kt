package co.edu.uniquindio.petadoptionapp.features.user.profile.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.window.Dialog
import co.edu.uniquindio.petadoptionapp.core.util.AppLanguage
import co.edu.uniquindio.petadoptionapp.core.util.currentAppLanguage
import co.edu.uniquindio.petadoptionapp.core.util.setAppLanguage
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.button.DangerButton
import co.edu.uniquindio.petadoptionapp.core.component.button.SecondaryButton
import co.edu.uniquindio.petadoptionapp.core.component.dialog.AppConfirmDialog
import co.edu.uniquindio.petadoptionapp.core.component.card.SectionCard
import co.edu.uniquindio.petadoptionapp.core.component.dialog.AppFormDialog
import co.edu.uniquindio.petadoptionapp.core.component.form.PasswordStrengthBar
import co.edu.uniquindio.petadoptionapp.core.component.dialog.AppInfoDialog
import co.edu.uniquindio.petadoptionapp.core.component.form.AppTextField
import co.edu.uniquindio.petadoptionapp.core.component.form.SettingActionRow
import co.edu.uniquindio.petadoptionapp.core.component.form.SettingSwitchRow
import co.edu.uniquindio.petadoptionapp.core.component.header.AppTopBar
import co.edu.uniquindio.petadoptionapp.core.component.icon.IconCircle
import co.edu.uniquindio.petadoptionapp.core.component.text.ErrorText
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
/**
Pantalla "Configuración", accesible desde Mi Perfil (usuarios) y desde el perfil del moderador.
Agrupa en tarjetas: Seguridad (cambiar contraseña y verificación en dos pasos), Privacidad
(perfil público, solo usuarios), Idioma de la app, el botón "Cerrar sesión" y el botón
"Eliminar cuenta" (solo usuarios). Los diálogos de contraseña, eliminación, cierre de sesión,
éxito e idioma se abren según el estado.
El estado (SettingsUiState) y la lógica viven en SettingsViewModel, que usa UpdateSettingsUseCase,
UpdateModeratorTwoFactorUseCase, ChangePasswordUseCase, DeleteAccountUseCase y LogoutUseCase.
La pantalla escucha los eventos SettingsEvent.AccountDeleted y SettingsEvent.LoggedOut y llama a
onAccountDeleted / onLoggedOut para que AppNavigation regrese al login; onBack vuelve atrás.
**/

// Margen horizontal común del contenido de la pantalla.
private val ScreenHorizontalPadding = 24.dp

// Letra de los campos dentro de los diálogos: la misma Rubik Regular de las pantallas, pero a 14 sp en vez de 16,
// porque el diálogo es más angosto. Para otro tamaño basta con cambiar este estilo.
private val DialogFieldTextStyle: TextStyle
    // Getter composable: toma bodyLarge del tema y reduce tamaño e interlineado.
    @Composable get() = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp, lineHeight = 20.sp)

/**
Pantalla de configuración: seguridad (contraseña y A2F), privacidad del perfil, idioma,
cerrar sesión y eliminar cuenta.
Conecta el [SettingsViewModel] con la UI; [SettingsContent] solo dibuja.
No decide a dónde navegar: avisa y AppNavigation resuelve el destino.
**/
@Composable
fun SettingsScreen(
    // Callback para volver a la pantalla anterior.
    onBack: () -> Unit,
    // Callback que se invoca tras eliminar la cuenta.
    onAccountDeleted: () -> Unit,
    // Callback que se invoca tras cerrar la sesión.
    onLoggedOut: () -> Unit,
    // ViewModel inyectado por Hilt.
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    // Recolecta el estado respetando el ciclo de vida.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Referencia siempre actualizada al callback de cuenta eliminada.
    val currentOnAccountDeleted by rememberUpdatedState(onAccountDeleted)
    // Referencia siempre actualizada al callback de sesión cerrada.
    val currentOnLoggedOut by rememberUpdatedState(onLoggedOut)

    // Escucha los eventos de navegación del ViewModel.
    LaunchedEffect(viewModel) {
        // Recolecta cada evento emitido.
        viewModel.events.collect { event ->
            // Reacciona según el tipo de evento.
            when (event) {
                // Cuenta eliminada: delega la navegación al login.
                SettingsEvent.AccountDeleted -> currentOnAccountDeleted()
                // Sesión cerrada: delega la navegación al login.
                SettingsEvent.LoggedOut -> currentOnLoggedOut()
            }
        }
    }

    // Dibuja el contenido sin estado con las acciones enlazadas al ViewModel.
    SettingsContent(
        // Estado actual de la pantalla.
        uiState = uiState,
        // Agrupa todas las acciones de la pantalla.
        actions = SettingsActions(
            // Volver atrás.
            onBack = onBack,
            // Interruptor de verificación en dos pasos.
            onTwoFactorChange = viewModel::onTwoFactorChange,
            // Interruptor de perfil público.
            onProfilePublicChange = viewModel::onProfilePublicChange,
            // Abrir el diálogo de cambio de contraseña.
            onChangePasswordClick = viewModel::onChangePasswordClick,
            // Campos del diálogo de contraseña.
            onCurrentPasswordChange = viewModel::onCurrentPasswordChange,
            onNewPasswordChange = viewModel::onNewPasswordChange,
            onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
            // Confirmar o cerrar el diálogo de contraseña.
            onPasswordConfirm = viewModel::onPasswordConfirm,
            onPasswordDialogDismiss = viewModel::onPasswordDialogDismiss,
            // Aceptar el aviso de contraseña cambiada.
            onPasswordSuccessAccept = viewModel::onPasswordSuccessAccept,
            // Flujo de eliminar cuenta.
            onDeleteAccountClick = viewModel::onDeleteAccountClick,
            onDeletePasswordChange = viewModel::onDeletePasswordChange,
            onDeleteConfirm = viewModel::onDeleteConfirm,
            onDeleteDialogDismiss = viewModel::onDeleteDialogDismiss,
            // Flujo de cerrar sesión.
            onLogoutClick = viewModel::onLogoutClick,
            onLogoutConfirm = viewModel::onLogoutConfirm,
            onLogoutDismiss = viewModel::onLogoutDismiss,
        ),
    )
}

// Agrupa las acciones para no pasar diecisiete lambdas sueltas (mismo patrón que Registro).
private data class SettingsActions(
    // Volver atrás.
    val onBack: () -> Unit = {},
    // Cambio del interruptor de verificación en dos pasos.
    val onTwoFactorChange: (Boolean) -> Unit = {},
    // Cambio del interruptor de perfil público.
    val onProfilePublicChange: (Boolean) -> Unit = {},
    // Toque en "Cambiar contraseña".
    val onChangePasswordClick: () -> Unit = {},
    // Escritura en el campo de contraseña actual.
    val onCurrentPasswordChange: (String) -> Unit = {},
    // Escritura en el campo de nueva contraseña.
    val onNewPasswordChange: (String) -> Unit = {},
    // Escritura en el campo de confirmación.
    val onConfirmPasswordChange: (String) -> Unit = {},
    // Confirmar el cambio de contraseña.
    val onPasswordConfirm: () -> Unit = {},
    // Cerrar el diálogo de contraseña.
    val onPasswordDialogDismiss: () -> Unit = {},
    // Aceptar el aviso de éxito de contraseña.
    val onPasswordSuccessAccept: () -> Unit = {},
    // Toque en "Eliminar cuenta".
    val onDeleteAccountClick: () -> Unit = {},
    // Escritura de la contraseña en el diálogo de eliminación.
    val onDeletePasswordChange: (String) -> Unit = {},
    // Confirmar la eliminación.
    val onDeleteConfirm: () -> Unit = {},
    // Cerrar el diálogo de eliminación.
    val onDeleteDialogDismiss: () -> Unit = {},
    // Toque en "Cerrar sesión".
    val onLogoutClick: () -> Unit = {},
    // Confirmar el cierre de sesión.
    val onLogoutConfirm: () -> Unit = {},
    // Cancelar el cierre de sesión.
    val onLogoutDismiss: () -> Unit = {},
)

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun SettingsContent(
    // Estado de la pantalla.
    uiState: SettingsUiState,
    // Acciones de la pantalla.
    actions: SettingsActions,
) {
    // Columna raíz: barra superior y contenido desplazable.
    Column(
        // Ocupa toda la pantalla con el fondo del tema y respeta las barras del sistema.
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .systemBarsPadding(),
    ) {
        // Barra superior con título y flecha atrás.
        AppTopBar(
            title = stringResource(R.string.profile_settings),
            onBack = actions.onBack,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        // Contenido desplazable con las secciones de ajustes.
        Column(
            // Ocupa el espacio restante, permite scroll y aplica márgenes.
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
            // Separa las secciones 16 dp.
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Sección "Seguridad".
            SectionCard(
                title = stringResource(R.string.settings_security),
                icon = Icons.Outlined.Lock,
            ) {
                // Fila que abre el diálogo de cambio de contraseña.
                SettingActionRow(
                    icon = Icons.Outlined.Key,
                    title = stringResource(R.string.settings_change_password),
                    onClick = actions.onChangePasswordClick,
                )
                // Interruptor de verificación en dos pasos.
                SettingSwitchRow(
                    icon = Icons.Outlined.Shield,
                    title = stringResource(R.string.settings_two_factor),
                    checked = uiState.twoFactorEnabled,
                    onCheckedChange = actions.onTwoFactorChange,
                )
            }

            // Los moderadores no tienen perfil público ni privado.
            if (!uiState.isModerator) {
                // Sección "Privacidad".
                SectionCard(
                    title = stringResource(R.string.settings_privacy),
                    icon = Icons.Outlined.Visibility,
                ) {
                    // Interruptor de perfil público.
                    SettingSwitchRow(
                        icon = Icons.Outlined.Person,
                        title = stringResource(R.string.settings_public_profile),
                        checked = uiState.isProfilePublic,
                        onCheckedChange = actions.onProfilePublicChange,
                    )
                }
            }

            // Idioma de la app (usuario y moderador): español, inglés o francés.
            var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
            // Sección "Idioma".
            SectionCard(
                title = stringResource(R.string.settings_language),
                icon = Icons.Outlined.Language,
            ) {
                // Fila que muestra el idioma actual y abre el selector.
                SettingActionRow(
                    icon = Icons.Outlined.Translate,
                    title = stringResource(R.string.settings_language_current, languageName(currentAppLanguage())),
                    onClick = { showLanguageDialog = true },
                )
            }
            // Muestra el selector de idioma si se pidió.
            if (showLanguageDialog) {
                // Al cerrarse, oculta el diálogo.
                LanguageDialog(onDismiss = { showLanguageDialog = false })
            }

            // Si falló el guardado de algún interruptor, muestra el aviso.
            if (uiState.settingsError) {
                // Texto de error a todo el ancho.
                ErrorText(
                    text = stringResource(R.string.settings_error_save),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Cerrar sesión va arriba de "Eliminar cuenta": es la salida normal, no destructiva.
            SecondaryButton(
                text = stringResource(R.string.settings_logout),
                icon = Icons.AutoMirrored.Outlined.Logout,
                onClick = actions.onLogoutClick,
                // Separación extra respecto a las tarjetas.
                modifier = Modifier.padding(top = 8.dp),
            )

            // Un moderador no elimina su cuenta desde la app (se gestiona aparte, como su alta).
            if (!uiState.isModerator) {
                // Botón rojo de eliminar cuenta.
                DangerButton(
                    text = stringResource(R.string.settings_delete_account),
                    onClick = actions.onDeleteAccountClick,
                )

                // Advertencia de que la eliminación es permanente.
                Text(
                    text = stringResource(R.string.settings_delete_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.stateError,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    // Si el diálogo de contraseña está abierto (no es null), lo dibuja.
    uiState.passwordDialog?.let { dialog ->
        ChangePasswordDialog(dialog = dialog, actions = actions)
    }

    // Si el diálogo de eliminar cuenta está abierto, lo dibuja.
    uiState.deleteDialog?.let { dialog ->
        DeleteAccountDialog(dialog = dialog, actions = actions)
    }

    // Diálogo de confirmación de cierre de sesión.
    if (uiState.showLogoutDialog) {
        // Diálogo con confirmar/cancelar, marcado como destructivo.
        AppConfirmDialog(
            title = stringResource(R.string.settings_logout_title),
            message = stringResource(R.string.settings_logout_message),
            confirmText = stringResource(R.string.settings_logout),
            cancelText = stringResource(R.string.common_cancel),
            onConfirm = actions.onLogoutConfirm,
            onDismiss = actions.onLogoutDismiss,
            // Pinta el botón de confirmar con estilo de acción peligrosa.
            isDestructive = true,
        )
    }

    // Aviso de contraseña cambiada con éxito.
    if (uiState.showPasswordSuccess) {
        // Diálogo informativo con un escudo como icono.
        AppInfoDialog(
            title = stringResource(R.string.settings_password_success_title),
            message = stringResource(R.string.settings_password_success_message),
            confirmText = stringResource(R.string.settings_password_success_button),
            onConfirm = actions.onPasswordSuccessAccept,
            // Icono superior del diálogo.
            icon = {
                // Círculo con el icono de escudo.
                IconCircle(
                    icon = Icons.Outlined.Shield,
                    size = 56.dp,
                    containerColor = AppTheme.colors.surfaceTag,
                )
            },
        )
    }
}

// Diálogo con el formulario para cambiar la contraseña (actual, nueva y confirmación).
@Composable
private fun ChangePasswordDialog(dialog: PasswordDialogState, actions: SettingsActions) {
    // Diálogo de formulario genérico de la app.
    AppFormDialog(
        title = stringResource(R.string.settings_change_password),
        confirmText = stringResource(R.string.settings_password_save),
        onConfirm = actions.onPasswordConfirm,
        onDismiss = actions.onPasswordDialogDismiss,
        // Error devuelto por el caso de uso, traducido a texto.
        errorMessage = passwordErrorMessage(dialog.error),
        // El botón de guardar solo se habilita si todos los campos son válidos.
        confirmEnabled = dialog.isFilled,
        // Muestra el indicador de carga mientras se guarda.
        isLoading = dialog.isLoading,
    ) {
        // Campo de contraseña actual.
        AppTextField(
            value = dialog.currentPassword,
            onValueChange = actions.onCurrentPasswordChange,
            label = stringResource(R.string.settings_current_password),
            leadingIcon = Icons.Outlined.Lock,
            errorMessage = fieldErrorMessage(dialog.currentError, PasswordField.CURRENT),
            // Oculta los caracteres y muestra el botón de ver/ocultar.
            isPassword = true,
            // Letra más pequeña dentro del diálogo.
            textStyle = DialogFieldTextStyle,
        )
        // Campo de nueva contraseña.
        AppTextField(
            value = dialog.newPassword,
            onValueChange = actions.onNewPasswordChange,
            label = stringResource(R.string.settings_new_password),
            leadingIcon = Icons.Outlined.Key,
            errorMessage = fieldErrorMessage(dialog.newError, PasswordField.NEW),
            isPassword = true,
            textStyle = DialogFieldTextStyle,
        )
        // Igual que al crear la cuenta: aparece apenas hay algo escrito.
        dialog.strength?.let { PasswordStrengthBar(strength = it) }
        // Campo de confirmación; "Listo" en el teclado envía el formulario.
        AppTextField(
            value = dialog.confirmPassword,
            onValueChange = actions.onConfirmPasswordChange,
            label = stringResource(R.string.settings_confirm_password),
            leadingIcon = Icons.Outlined.Key,
            errorMessage = fieldErrorMessage(dialog.confirmError, PasswordField.CONFIRM),
            isPassword = true,
            textStyle = DialogFieldTextStyle,
            // Acción "Listo" del teclado.
            imeAction = ImeAction.Done,
            // Al pulsar "Listo" intenta guardar.
            keyboardActions = KeyboardActions(onDone = { actions.onPasswordConfirm() }),
        )
    }
}

// Diálogo que pide la contraseña para confirmar la eliminación de la cuenta.
@Composable
private fun DeleteAccountDialog(dialog: DeleteDialogState, actions: SettingsActions) {
    // Diálogo de formulario marcado como destructivo.
    AppFormDialog(
        title = stringResource(R.string.settings_delete_title),
        message = stringResource(R.string.settings_delete_message),
        confirmText = stringResource(R.string.settings_delete_confirm),
        onConfirm = actions.onDeleteConfirm,
        onDismiss = actions.onDeleteDialogDismiss,
        // Error traducido a texto.
        errorMessage = deleteErrorMessage(dialog.error),
        // Solo se puede confirmar si se escribió la contraseña.
        confirmEnabled = dialog.password.isNotBlank(),
        isLoading = dialog.isLoading,
        // Estilo de acción peligrosa en el botón de confirmar.
        isDestructive = true,
    ) {
        // Campo de contraseña; "Listo" en el teclado confirma.
        AppTextField(
            value = dialog.password,
            onValueChange = actions.onDeletePasswordChange,
            label = stringResource(R.string.settings_current_password),
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            textStyle = DialogFieldTextStyle,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { actions.onDeleteConfirm() }),
        )
    }
}

// Cada idioma se muestra en su propio idioma, así se reconoce aunque la app esté en otro.
private fun languageName(language: AppLanguage): String = when (language) {
    // Español.
    AppLanguage.SPANISH -> "Español"
    // Inglés.
    AppLanguage.ENGLISH -> "English"
    // Francés.
    AppLanguage.FRENCH -> "Français"
}

// Elegir el idioma: al tocar uno se aplica de inmediato (la app se vuelve a dibujar en ese idioma).
@Composable
private fun LanguageDialog(onDismiss: () -> Unit) {
    // Idioma activo actualmente, para marcar su opción.
    val current = currentAppLanguage()
    // Diálogo base de Compose que se cierra al tocar fuera.
    Dialog(onDismissRequest = onDismiss) {
        // Contenedor del diálogo con fondo redondeado.
        Column(
            // Ancho completo, fondo de superficie con esquinas redondeadas y relleno interno.
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.surface, RoundedCornerShape(24.dp))
                .padding(24.dp),
            // Separa los elementos 8 dp.
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Título del diálogo.
            Text(
                text = stringResource(R.string.settings_language),
                style = MaterialTheme.typography.titleLarge,
                color = AppTheme.colors.textPrimary,
            )
            // Pequeño espacio bajo el título.
            Spacer(Modifier.height(4.dp))
            // Una fila por cada idioma disponible.
            AppLanguage.entries.forEach { language ->
                // Fila seleccionable con radio y nombre del idioma.
                Row(
                    // Toda la fila es seleccionable como un RadioButton accesible.
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .selectable(selected = language == current, role = Role.RadioButton) {
                            // Cierra el diálogo al elegir.
                            onDismiss()
                            // Solo cambia el idioma si es distinto del actual.
                            if (language != current) setAppLanguage(language)
                        }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    // Alinea verticalmente el radio y el texto.
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Radio visual; onClick null porque el clic lo maneja la fila.
                    RadioButton(
                        selected = language == current,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(selectedColor = AppTheme.colors.actionSecondary),
                    )
                    // Espacio entre el radio y el texto.
                    Spacer(Modifier.width(12.dp))
                    // Nombre del idioma en su propio idioma.
                    Text(languageName(language), style = MaterialTheme.typography.bodyLarge, color = AppTheme.colors.textPrimary)
                }
            }
            // Espacio antes del botón cancelar.
            Spacer(Modifier.height(8.dp))
            // Botón para cerrar sin cambiar el idioma.
            SecondaryButton(text = stringResource(R.string.common_cancel), onClick = onDismiss)
        }
    }
}

// Textos de los errores por campo: los mismos de Registro.
@Composable
private fun fieldErrorMessage(error: PasswordFieldError?, field: PasswordField): String? = when (error) {
    // Sin error.
    null -> null
    // Vacío: el mensaje cambia si es el campo de confirmación.
    PasswordFieldError.EMPTY -> stringResource(
        if (field == PasswordField.CONFIRM) R.string.register_error_confirm_empty else R.string.register_error_password_empty
    )
    // No cumple la política de seguridad.
    PasswordFieldError.WEAK -> stringResource(R.string.register_error_password_weak)
    // Igual a la contraseña actual.
    PasswordFieldError.SAME -> stringResource(R.string.settings_error_same_password)
    // La confirmación no coincide.
    PasswordFieldError.MISMATCH -> stringResource(R.string.register_error_password_mismatch)
}

// Traducen el tipo de error (lógica) al texto del recurso (presentación).
// Error general del diálogo de cambio de contraseña.
@Composable
private fun passwordErrorMessage(error: PasswordDialogError?): String? = when (error) {
    // Sin error.
    null -> null
    // Contraseña actual incorrecta.
    PasswordDialogError.WRONG_PASSWORD -> stringResource(R.string.settings_error_wrong_password)
    // Nueva contraseña débil.
    PasswordDialogError.WEAK_PASSWORD -> stringResource(R.string.register_error_password_weak)
    // Nueva igual a la actual.
    PasswordDialogError.SAME_PASSWORD -> stringResource(R.string.settings_error_same_password)
    // Error inesperado.
    PasswordDialogError.GENERIC -> stringResource(R.string.settings_error_generic)
}

// Error del diálogo de eliminar cuenta.
@Composable
private fun deleteErrorMessage(error: DeleteDialogError?): String? = when (error) {
    // Sin error.
    null -> null
    // Contraseña incorrecta.
    DeleteDialogError.WRONG_PASSWORD -> stringResource(R.string.settings_error_wrong_password)
    // Error inesperado.
    DeleteDialogError.GENERIC -> stringResource(R.string.settings_error_generic)
}

// Vista previa de la pantalla sin diálogos abiertos.
@Preview(name = "Configuración", showBackground = true, heightDp = 800)
@Composable
private fun SettingsContentPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // A2F apagada y perfil público.
        SettingsContent(
            uiState = SettingsUiState(twoFactorEnabled = false, isProfilePublic = true),
            actions = SettingsActions(),
        )
    }
}

// Vista previa con el diálogo de cambio de contraseña abierto y un error.
@Preview(name = "Cambiar contraseña", showBackground = true, heightDp = 800)
@Composable
private fun SettingsPasswordDialogPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Diálogo con la contraseña actual escrita y el error de contraseña incorrecta.
        SettingsContent(
            uiState = SettingsUiState(
                passwordDialog = PasswordDialogState(
                    currentPassword = "Secreta1",
                    error = PasswordDialogError.WRONG_PASSWORD,
                ),
            ),
            actions = SettingsActions(),
        )
    }
}

// Vista previa con el diálogo de eliminar cuenta abierto.
@Preview(name = "Eliminar cuenta", showBackground = true, heightDp = 800)
@Composable
private fun SettingsDeleteDialogPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Estado con el diálogo de eliminación vacío.
        SettingsContent(
            uiState = SettingsUiState(deleteDialog = DeleteDialogState()),
            actions = SettingsActions(),
        )
    }
}
