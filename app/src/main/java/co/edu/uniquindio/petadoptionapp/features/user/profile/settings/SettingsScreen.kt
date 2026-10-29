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

private val ScreenHorizontalPadding = 24.dp

// Letra de los campos dentro de los diálogos: la misma Rubik Regular de las pantallas, pero a 14 sp en vez de 16,
// porque el diálogo es más angosto. Para otro tamaño basta con cambiar este estilo.
private val DialogFieldTextStyle: TextStyle
    @Composable get() = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp, lineHeight = 20.sp)

/**
Pantalla de configuración: seguridad (contraseña y A2F), privacidad del perfil y eliminar cuenta.
Conecta el [SettingsViewModel] con la UI; [SettingsContent] solo dibuja.
No decide a dónde navegar: avisa y AppNavigation resuelve el destino.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onAccountDeleted: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnAccountDeleted by rememberUpdatedState(onAccountDeleted)
    val currentOnLoggedOut by rememberUpdatedState(onLoggedOut)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                SettingsEvent.AccountDeleted -> currentOnAccountDeleted()
                SettingsEvent.LoggedOut -> currentOnLoggedOut()
            }
        }
    }

    SettingsContent(
        uiState = uiState,
        actions = SettingsActions(
            onBack = onBack,
            onTwoFactorChange = viewModel::onTwoFactorChange,
            onProfilePublicChange = viewModel::onProfilePublicChange,
            onChangePasswordClick = viewModel::onChangePasswordClick,
            onCurrentPasswordChange = viewModel::onCurrentPasswordChange,
            onNewPasswordChange = viewModel::onNewPasswordChange,
            onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
            onPasswordConfirm = viewModel::onPasswordConfirm,
            onPasswordDialogDismiss = viewModel::onPasswordDialogDismiss,
            onPasswordSuccessAccept = viewModel::onPasswordSuccessAccept,
            onDeleteAccountClick = viewModel::onDeleteAccountClick,
            onDeletePasswordChange = viewModel::onDeletePasswordChange,
            onDeleteConfirm = viewModel::onDeleteConfirm,
            onDeleteDialogDismiss = viewModel::onDeleteDialogDismiss,
            onLogoutClick = viewModel::onLogoutClick,
            onLogoutConfirm = viewModel::onLogoutConfirm,
            onLogoutDismiss = viewModel::onLogoutDismiss,
        ),
    )
}

// Agrupa las acciones para no pasar catorce lambdas sueltas (mismo patrón que Registro).
private data class SettingsActions(
    val onBack: () -> Unit = {},
    val onTwoFactorChange: (Boolean) -> Unit = {},
    val onProfilePublicChange: (Boolean) -> Unit = {},
    val onChangePasswordClick: () -> Unit = {},
    val onCurrentPasswordChange: (String) -> Unit = {},
    val onNewPasswordChange: (String) -> Unit = {},
    val onConfirmPasswordChange: (String) -> Unit = {},
    val onPasswordConfirm: () -> Unit = {},
    val onPasswordDialogDismiss: () -> Unit = {},
    val onPasswordSuccessAccept: () -> Unit = {},
    val onDeleteAccountClick: () -> Unit = {},
    val onDeletePasswordChange: (String) -> Unit = {},
    val onDeleteConfirm: () -> Unit = {},
    val onDeleteDialogDismiss: () -> Unit = {},
    val onLogoutClick: () -> Unit = {},
    val onLogoutConfirm: () -> Unit = {},
    val onLogoutDismiss: () -> Unit = {},
)

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    actions: SettingsActions,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .systemBarsPadding(),
    ) {
        AppTopBar(
            title = stringResource(R.string.profile_settings),
            onBack = actions.onBack,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionCard(
                title = stringResource(R.string.settings_security),
                icon = Icons.Outlined.Lock,
            ) {
                SettingActionRow(
                    icon = Icons.Outlined.Key,
                    title = stringResource(R.string.settings_change_password),
                    onClick = actions.onChangePasswordClick,
                )
                SettingSwitchRow(
                    icon = Icons.Outlined.Shield,
                    title = stringResource(R.string.settings_two_factor),
                    checked = uiState.twoFactorEnabled,
                    onCheckedChange = actions.onTwoFactorChange,
                )
            }

            // Los moderadores no tienen perfil público ni privado.
            if (!uiState.isModerator) {
                SectionCard(
                    title = stringResource(R.string.settings_privacy),
                    icon = Icons.Outlined.Visibility,
                ) {
                    SettingSwitchRow(
                        icon = Icons.Outlined.Person,
                        title = stringResource(R.string.settings_public_profile),
                        checked = uiState.isProfilePublic,
                        onCheckedChange = actions.onProfilePublicChange,
                    )
                }
            }

            if (uiState.settingsError) {
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
                modifier = Modifier.padding(top = 8.dp),
            )

            // Un moderador no elimina su cuenta desde la app (se gestiona aparte, como su alta).
            if (!uiState.isModerator) {
                DangerButton(
                    text = stringResource(R.string.settings_delete_account),
                    onClick = actions.onDeleteAccountClick,
                )

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

    uiState.passwordDialog?.let { dialog ->
        ChangePasswordDialog(dialog = dialog, actions = actions)
    }

    uiState.deleteDialog?.let { dialog ->
        DeleteAccountDialog(dialog = dialog, actions = actions)
    }

    if (uiState.showLogoutDialog) {
        AppConfirmDialog(
            title = stringResource(R.string.settings_logout_title),
            message = stringResource(R.string.settings_logout_message),
            confirmText = stringResource(R.string.settings_logout),
            cancelText = stringResource(R.string.common_cancel),
            onConfirm = actions.onLogoutConfirm,
            onDismiss = actions.onLogoutDismiss,
            isDestructive = true,
        )
    }

    if (uiState.showPasswordSuccess) {
        AppInfoDialog(
            title = stringResource(R.string.settings_password_success_title),
            message = stringResource(R.string.settings_password_success_message),
            confirmText = stringResource(R.string.settings_password_success_button),
            onConfirm = actions.onPasswordSuccessAccept,
            icon = {
                IconCircle(
                    icon = Icons.Outlined.Shield,
                    size = 56.dp,
                    containerColor = AppTheme.colors.surfaceTag,
                )
            },
        )
    }
}

@Composable
private fun ChangePasswordDialog(dialog: PasswordDialogState, actions: SettingsActions) {
    AppFormDialog(
        title = stringResource(R.string.settings_change_password),
        confirmText = stringResource(R.string.settings_password_save),
        onConfirm = actions.onPasswordConfirm,
        onDismiss = actions.onPasswordDialogDismiss,
        errorMessage = passwordErrorMessage(dialog.error),
        confirmEnabled = dialog.isFilled,
        isLoading = dialog.isLoading,
    ) {
        AppTextField(
            value = dialog.currentPassword,
            onValueChange = actions.onCurrentPasswordChange,
            label = stringResource(R.string.settings_current_password),
            leadingIcon = Icons.Outlined.Lock,
            errorMessage = fieldErrorMessage(dialog.currentError, PasswordField.CURRENT),
            isPassword = true,
            textStyle = DialogFieldTextStyle,
        )
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
        AppTextField(
            value = dialog.confirmPassword,
            onValueChange = actions.onConfirmPasswordChange,
            label = stringResource(R.string.settings_confirm_password),
            leadingIcon = Icons.Outlined.Key,
            errorMessage = fieldErrorMessage(dialog.confirmError, PasswordField.CONFIRM),
            isPassword = true,
            textStyle = DialogFieldTextStyle,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { actions.onPasswordConfirm() }),
        )
    }
}

@Composable
private fun DeleteAccountDialog(dialog: DeleteDialogState, actions: SettingsActions) {
    AppFormDialog(
        title = stringResource(R.string.settings_delete_title),
        message = stringResource(R.string.settings_delete_message),
        confirmText = stringResource(R.string.settings_delete_confirm),
        onConfirm = actions.onDeleteConfirm,
        onDismiss = actions.onDeleteDialogDismiss,
        errorMessage = deleteErrorMessage(dialog.error),
        confirmEnabled = dialog.password.isNotBlank(),
        isLoading = dialog.isLoading,
        isDestructive = true,
    ) {
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

// Textos de los errores por campo: los mismos de Registro.
@Composable
private fun fieldErrorMessage(error: PasswordFieldError?, field: PasswordField): String? = when (error) {
    null -> null
    PasswordFieldError.EMPTY -> stringResource(
        if (field == PasswordField.CONFIRM) R.string.register_error_confirm_empty else R.string.register_error_password_empty
    )
    PasswordFieldError.WEAK -> stringResource(R.string.register_error_password_weak)
    PasswordFieldError.SAME -> stringResource(R.string.settings_error_same_password)
    PasswordFieldError.MISMATCH -> stringResource(R.string.register_error_password_mismatch)
}

// Traducen el tipo de error (lógica) al texto del recurso (presentación).
@Composable
private fun passwordErrorMessage(error: PasswordDialogError?): String? = when (error) {
    null -> null
    PasswordDialogError.WRONG_PASSWORD -> stringResource(R.string.settings_error_wrong_password)
    PasswordDialogError.WEAK_PASSWORD -> stringResource(R.string.register_error_password_weak)
    PasswordDialogError.SAME_PASSWORD -> stringResource(R.string.settings_error_same_password)
    PasswordDialogError.GENERIC -> stringResource(R.string.settings_error_generic)
}

@Composable
private fun deleteErrorMessage(error: DeleteDialogError?): String? = when (error) {
    null -> null
    DeleteDialogError.WRONG_PASSWORD -> stringResource(R.string.settings_error_wrong_password)
    DeleteDialogError.GENERIC -> stringResource(R.string.settings_error_generic)
}

@Preview(name = "Configuración", showBackground = true, heightDp = 800)
@Composable
private fun SettingsContentPreview() {
    PetAdoptionAppTheme {
        SettingsContent(
            uiState = SettingsUiState(twoFactorEnabled = false, isProfilePublic = true),
            actions = SettingsActions(),
        )
    }
}

@Preview(name = "Cambiar contraseña", showBackground = true, heightDp = 800)
@Composable
private fun SettingsPasswordDialogPreview() {
    PetAdoptionAppTheme {
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

@Preview(name = "Eliminar cuenta", showBackground = true, heightDp = 800)
@Composable
private fun SettingsDeleteDialogPreview() {
    PetAdoptionAppTheme {
        SettingsContent(
            uiState = SettingsUiState(deleteDialog = DeleteDialogState()),
            actions = SettingsActions(),
        )
    }
}
