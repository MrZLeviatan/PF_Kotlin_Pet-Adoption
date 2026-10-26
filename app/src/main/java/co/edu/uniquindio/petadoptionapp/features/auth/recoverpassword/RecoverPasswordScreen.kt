package co.edu.uniquindio.petadoptionapp.features.auth.recoverpassword

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Security
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
Pantalla para pedir el enlace de recuperación de contraseña.
Conecta el [RecoverPasswordViewModel] con la UI; [RecoverPasswordContent] solo dibuja.
 */
@Composable
fun RecoverPasswordScreen(
    onBack: () -> Unit,
    onEmailSent: () -> Unit,
    viewModel: RecoverPasswordViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnEmailSent by rememberUpdatedState(onEmailSent)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                RecoverPasswordEvent.EmailSent -> currentOnEmailSent()
            }
        }
    }

    RecoverPasswordContent(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onSendClick = viewModel::onSendClick,
        onDialogAccept = viewModel::onDialogAccept,
        onBack = onBack,
    )
}

@Composable
private fun RecoverPasswordContent(
    uiState: RecoverPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSendClick: () -> Unit,
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
            title = stringResource(R.string.recover_title),
            subtitle = stringResource(R.string.recover_subtitle),
        )

        Spacer(Modifier.height(32.dp))

        AppTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            label = stringResource(R.string.recover_email_label),
            leadingIcon = Icons.Outlined.Email,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(
                onDone = { if (uiState.isEmailFilled) onSendClick() }
            ),
        )

        Spacer(Modifier.height(32.dp))

        PrimaryButton(
            text = stringResource(R.string.recover_send_button),
            onClick = onSendClick,
            enabled = uiState.isEmailFilled,
        )
    }

    if (uiState.showSuccessDialog) {
        AppInfoDialog(
            title = stringResource(R.string.recover_success_title),
            message = stringResource(R.string.recover_success_message),
            confirmText = stringResource(R.string.recover_success_button),
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
private fun RecoverPasswordEmptyPreview() {
    PetAdoptionAppTheme {
        RecoverPasswordContent(
            uiState = RecoverPasswordUiState(),
            onEmailChange = {}, onSendClick = {}, onDialogAccept = {}, onBack = {},
        )
    }
}

@Preview(name = "Con correo", showBackground = true, heightDp = 800)
@Composable
private fun RecoverPasswordFilledPreview() {
    PetAdoptionAppTheme {
        RecoverPasswordContent(
            uiState = RecoverPasswordUiState(email = "nicolas.cabreras@uqvirtual.edu.co"),
            onEmailChange = {}, onSendClick = {}, onDialogAccept = {}, onBack = {},
        )
    }
}

@Preview(name = "Correo enviado", showBackground = true, heightDp = 800)
@Composable
private fun RecoverPasswordSuccessPreview() {
    PetAdoptionAppTheme {
        RecoverPasswordContent(
            uiState = RecoverPasswordUiState(
                email = "nicolas.cabreras@uqvirtual.edu.co",
                showSuccessDialog = true,
            ),
            onEmailChange = {}, onSendClick = {}, onDialogAccept = {}, onBack = {},
        )
    }
}