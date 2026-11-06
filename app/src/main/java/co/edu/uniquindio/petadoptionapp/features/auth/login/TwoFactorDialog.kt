package co.edu.uniquindio.petadoptionapp.features.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.button.PrimaryButton
import co.edu.uniquindio.petadoptionapp.core.component.text.ErrorText
import co.edu.uniquindio.petadoptionapp.core.component.text.LinkText
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import co.edu.uniquindio.petadoptionapp.domain.model.auth.TwoFactorChallenge

/**
Diálogo de la verificación en dos pasos (A2F): una casilla por dígito, "Verificar código" y
"Reenviar código" con cuenta regresiva. Solo dibuja; la lógica vive en [LoginViewModel].
 */
@Composable
fun TwoFactorDialog(
    state: TwoFactorUiState,
    onCodeChange: (String) -> Unit,
    onVerifyClick: () -> Unit,
    onResendClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        // Atrás equivale a la X: cancela el inicio de sesión.
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.background, RoundedCornerShape(24.dp))
                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(24.dp))
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CloseButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End))

            Text(
                text = stringResource(R.string.two_factor_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = AppTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.two_factor_subtitle, TwoFactorChallenge.CODE_LENGTH),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = AppTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(24.dp))

            CodeField(
                code = state.code,
                onCodeChange = onCodeChange,
                onDone = onVerifyClick,
                isError = state.error != null,
                enabled = !state.isVerifying,
            )

            state.error?.let { error ->
                Spacer(Modifier.height(12.dp))
                ErrorText(text = twoFactorErrorMessage(error))
            }

            Spacer(Modifier.height(24.dp))

            PrimaryButton(
                text = stringResource(if (state.isVerifying) R.string.two_factor_verifying else R.string.two_factor_verify),
                onClick = onVerifyClick,
                modifier = Modifier.height(52.dp),
                enabled = state.isCodeComplete && !state.isVerifying,
                textStyle = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
            )

            Spacer(Modifier.height(4.dp))

            ResendRow(state = state, onResendClick = onResendClick)
        }
    }
}

// X redonda arriba a la derecha, como en el diseño.
@Composable
private fun CloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(AppTheme.colors.surface, CircleShape)
                .border(1.dp, AppTheme.colors.border, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.two_factor_close),
                tint = AppTheme.colors.textPrimary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/**
Un solo campo de texto invisible que dibuja una casilla por dígito.
Así funcionan pegar el código, borrar y el autocompletado del teclado sin manejar el foco casilla por casilla.
 */
@Composable
private fun CodeField(
    code: String,
    onCodeChange: (String) -> Unit,
    onDone: () -> Unit,
    isError: Boolean,
    enabled: Boolean,
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    val description = stringResource(R.string.two_factor_code_description)

    // Abre el teclado numérico al mostrar el diálogo.
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    BasicTextField(
        value = code,
        onValueChange = onCodeChange,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        modifier = Modifier
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused }
            .semantics { contentDescription = description },
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                repeat(TwoFactorChallenge.CODE_LENGTH) { index ->
                    // La casilla activa es la siguiente por llenar.
                    val isCurrent = isFocused && index == code.length.coerceAtMost(TwoFactorChallenge.CODE_LENGTH - 1)
                    val borderColor = when {
                        isError -> AppTheme.colors.stateError
                        isCurrent -> AppTheme.colors.textLink
                        else -> AppTheme.colors.border
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .widthIn(max = 48.dp)
                            .height(52.dp)
                            .border(if (isCurrent) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = code.getOrNull(index)?.toString().orEmpty(),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = AppTheme.colors.textPrimary,
                        )
                    }
                }
            }
        },
    )
}

// "Reenviar código en 00:30" mientras corre la espera; después, el enlace para pedir otro.
@Composable
private fun ResendRow(state: TwoFactorUiState, onResendClick: () -> Unit) {
    when {
        state.isResending -> ResendLabel(stringResource(R.string.two_factor_resending))
        state.resendSeconds > 0 -> ResendLabel(
            stringResource(R.string.two_factor_resend_in, "00:%02d".format(state.resendSeconds))
        )
        else -> LinkText(text = stringResource(R.string.two_factor_resend), onClick = onResendClick)
    }
}

@Composable
private fun ResendLabel(text: String) {
    Box(modifier = Modifier.height(48.dp), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.textLink,
        )
    }
}

@Composable
private fun twoFactorErrorMessage(error: TwoFactorError): String = when (error) {
    TwoFactorError.WRONG_CODE -> stringResource(R.string.two_factor_error_wrong)
    TwoFactorError.EXPIRED -> stringResource(R.string.two_factor_error_expired)
    TwoFactorError.TOO_MANY_ATTEMPTS -> stringResource(R.string.two_factor_error_too_many)
    TwoFactorError.NOT_SENT -> stringResource(R.string.two_factor_error_not_sent)
    TwoFactorError.GENERIC -> stringResource(R.string.two_factor_error_generic)
}

@Preview(name = "Esperando el código")
@Composable
private fun TwoFactorDialogPreview() {
    PetAdoptionAppTheme {
        TwoFactorDialog(
            state = TwoFactorUiState(accountId = "u1", code = "123", resendSeconds = 30),
            onCodeChange = {}, onVerifyClick = {}, onResendClick = {}, onDismiss = {},
        )
    }
}

@Preview(name = "Código incorrecto")
@Composable
private fun TwoFactorDialogErrorPreview() {
    PetAdoptionAppTheme {
        TwoFactorDialog(
            state = TwoFactorUiState(accountId = "u1", error = TwoFactorError.WRONG_CODE),
            onCodeChange = {}, onVerifyClick = {}, onResendClick = {}, onDismiss = {},
        )
    }
}
