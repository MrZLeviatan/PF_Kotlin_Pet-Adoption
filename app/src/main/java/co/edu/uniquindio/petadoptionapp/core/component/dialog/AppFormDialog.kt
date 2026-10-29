package co.edu.uniquindio.petadoptionapp.core.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.button.DangerButton
import co.edu.uniquindio.petadoptionapp.core.component.button.PrimaryButton
import co.edu.uniquindio.petadoptionapp.core.component.button.SecondaryButton
import co.edu.uniquindio.petadoptionapp.core.component.form.AppTextField
import co.edu.uniquindio.petadoptionapp.core.component.text.ErrorText
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
 Diálogo con campos (contraseña, confirmaciones): título, mensaje opcional, los campos que
 se pasen en [content], un error opcional y dos acciones (confirmar y cancelar).
 Con [isDestructive] el botón de confirmar usa el estilo de peligro ("Eliminar").
 Mientras [isLoading] no se puede cerrar ni confirmar dos veces.
 */
@Composable
fun AppFormDialog(
    title: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    errorMessage: String? = null,
    confirmEnabled: Boolean = true,
    isLoading: Boolean = false,
    isDestructive: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(dismissOnBackPress = !isLoading, dismissOnClickOutside = !isLoading),
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(AppTheme.colors.surface, RoundedCornerShape(24.dp))
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = AppTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            if (message != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content,
            )

            if (errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                ErrorText(text = errorMessage, modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(24.dp))

            val canConfirm = confirmEnabled && !isLoading
            if (isDestructive) {
                DangerButton(text = confirmText, onClick = onConfirm, enabled = canConfirm)
            } else {
                PrimaryButton(text = confirmText, onClick = onConfirm, enabled = canConfirm)
            }

            Spacer(Modifier.height(12.dp))

            // Cancelar como botón delineado (igual que "Cerrar sesión"), del mismo tamaño que confirmar.
            SecondaryButton(
                text = stringResource(R.string.common_cancel),
                onClick = onDismiss,
                enabled = !isLoading,
            )
        }
    }
}

@Preview
@Composable
private fun AppFormDialogPreview() {
    PetAdoptionAppTheme {
        AppFormDialog(
            title = "Cambiar contraseña",
            confirmText = "Guardar",
            onConfirm = {},
            onDismiss = {},
            errorMessage = "La contraseña actual no es correcta.",
        ) {
            AppTextField(
                value = "",
                onValueChange = {},
                label = "Contraseña actual",
                leadingIcon = Icons.Outlined.Lock,
                isPassword = true,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions.Default,
            )
        }
    }
}
