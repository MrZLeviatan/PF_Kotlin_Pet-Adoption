package co.edu.uniquindio.petadoptionapp.features.auth.newpassword

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Security
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.button.BackButton
import co.edu.uniquindio.petadoptionapp.core.component.button.PrimaryButton
import co.edu.uniquindio.petadoptionapp.core.component.dialog.AppInfoDialog
import co.edu.uniquindio.petadoptionapp.core.component.form.AppTextField
import co.edu.uniquindio.petadoptionapp.core.component.header.IconHeader
import co.edu.uniquindio.petadoptionapp.core.component.icon.IconCircle
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

private val ScreenHorizontalPadding = 24.dp

/**
Pantalla para definir la nueva contraseña (se llega desde el enlace del correo).
Conecta el [NewPasswordViewModel] con la UI; [NewPasswordContent] solo dibuja.
 */
@Composable
fun NewPasswordScreen(
    onBack: () -> Unit,
    onPasswordUpdated: () -> Unit,
    viewModel: NewPasswordViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnPasswordUpdated by rememberUpdatedState(onPasswordUpdated)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                NewPasswordEvent.PasswordUpdated -> currentOnPasswordUpdated()
            }
        }
    }

    NewPasswordContent(
        uiState = uiState,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onConfirmClick = viewModel::onConfirmClick,
        onDialogAccept = viewModel::onDialogAccept,
        onBack = onBack,
    )
}

@Composable
private fun NewPasswordContent(
    uiState: NewPasswordUiState,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onConfirmClick: () -> Unit,
    onDialogAccept: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BackButton(onClick = onBack, modifier = Modifier.align(Alignment.Start))

        Spacer(Modifier.height(16.dp))

        IconHeader(
            icon = Icons.Outlined.Security,
            title = stringResource(R.string.new_password_title),
            subtitle = stringResource(R.string.new_password_subtitle),
        )

        Spacer(Modifier.height(32.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = stringResource(R.string.new_password_label),
                leadingIcon = Icons.Outlined.Lock,
                isPassword = true,
            )
            AppTextField(
                value = uiState.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = stringResource(R.string.new_password_confirm_label),
                leadingIcon = Icons.Outlined.Lock,
                isPassword = true,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(
                    onDone = { if (uiState.isFormFilled) onConfirmClick() }
                ),
            )
        }

        Spacer(Modifier.height(32.dp))

        PrimaryButton(
            text = stringResource(R.string.new_password_button),
            onClick = onConfirmClick,
            enabled = uiState.isFormFilled,
        )
    }

    if (uiState.showSuccessDialog) {
        AppInfoDialog(
            title = stringResource(R.string.new_password_success_title),
            message = stringResource(R.string.new_password_success_message),
            confirmText = stringResource(R.string.new_password_success_button),
            onConfirm = onDialogAccept,
            icon = {
                IconCircle(
                    icon = Icons.Outlined.Security,
                    size = 56.dp,
                    containerColor = AppTheme.colors.surfaceTag,
                )
            },
        )
    }
}

@Preview(name = "Vacía", showBackground = true, heightDp = 800)
@Composable
private fun NewPasswordEmptyPreview() {
    PetAdoptionAppTheme {
        NewPasswordContent(
            uiState = NewPasswordUiState(),
            onPasswordChange = {}, onConfirmPasswordChange = {},
            onConfirmClick = {}, onDialogAccept = {}, onBack = {},
        )
    }
}

@Preview(name = "Llena", showBackground = true, heightDp = 800)
@Composable
private fun NewPasswordFilledPreview() {
    PetAdoptionAppTheme {
        NewPasswordContent(
            uiState = NewPasswordUiState(password = "12345678", confirmPassword = "12345678"),
            onPasswordChange = {}, onConfirmPasswordChange = {},
            onConfirmClick = {}, onDialogAccept = {}, onBack = {},
        )
    }
}

@Preview(name = "Contraseña actualizada", showBackground = true, heightDp = 800)
@Composable
private fun NewPasswordSuccessPreview() {
    PetAdoptionAppTheme {
        NewPasswordContent(
            uiState = NewPasswordUiState(
                password = "12345678",
                confirmPassword = "12345678",
                showSuccessDialog = true,
            ),
            onPasswordChange = {}, onConfirmPasswordChange = {},
            onConfirmClick = {}, onDialogAccept = {}, onBack = {},
        )
    }
}