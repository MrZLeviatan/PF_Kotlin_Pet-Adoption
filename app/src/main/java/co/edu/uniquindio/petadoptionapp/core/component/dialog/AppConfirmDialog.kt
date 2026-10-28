package co.edu.uniquindio.petadoptionapp.core.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import co.edu.uniquindio.petadoptionapp.core.component.button.DangerButton
import co.edu.uniquindio.petadoptionapp.core.component.button.PrimaryButton
import co.edu.uniquindio.petadoptionapp.core.component.button.SecondaryButton
import co.edu.uniquindio.petadoptionapp.core.component.icon.PawCircle
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
 Diálogo de confirmación ("¿Finalizar publicación?"): icono, título, mensaje y dos botones.
 Normalmente confirmar es el botón principal (amarillo) y Cancelar va delineado en rojo.
 Con [isDestructive] (cerrar sesión, eliminar) se invierten: confirmar va en rojo y Cancelar en amarillo,
 para que la acción que no tiene vuelta atrás se distinga.
 Con [alternativeText] aparece una tercera opción neutral entre las dos (ej. "Eliminar para mí").
 Mientras [isLoading] no se puede cerrar ni confirmar dos veces.
 */
@Composable
fun AppConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    cancelText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    isDestructive: Boolean = false,
    alternativeText: String? = null,
    onAlternative: () -> Unit = {},
    // Por defecto la huella; cada pantalla puede poner el suyo.
    icon: @Composable () -> Unit = {
        PawCircle(size = 64.dp, containerColor = AppTheme.colors.brand)
    },
) {
    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(dismissOnBackPress = !isLoading, dismissOnClickOutside = !isLoading),
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(AppTheme.colors.surface, RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            icon()
            Spacer(Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = AppTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            if (isDestructive) {
                DangerButton(text = confirmText, onClick = onConfirm, enabled = !isLoading, uppercase = false)
                Spacer(Modifier.height(12.dp))
                if (alternativeText != null) {
                    SecondaryButton(text = alternativeText, onClick = onAlternative, enabled = !isLoading)
                    Spacer(Modifier.height(12.dp))
                }
                PrimaryButton(text = cancelText, onClick = onDismiss, enabled = !isLoading)
            } else {
                PrimaryButton(text = confirmText, onClick = onConfirm, enabled = !isLoading)
                Spacer(Modifier.height(12.dp))
                if (alternativeText != null) {
                    SecondaryButton(text = alternativeText, onClick = onAlternative, enabled = !isLoading)
                    Spacer(Modifier.height(12.dp))
                }
                DangerButton(text = cancelText, onClick = onDismiss, enabled = !isLoading, uppercase = false)
            }
        }
    }
}

@Preview
@Composable
private fun AppConfirmDialogPreview() {
    PetAdoptionAppTheme {
        AppConfirmDialog(
            title = "¿Finalizar publicación?",
            message = "¿Deseas darle fin a esta publicación?",
            confirmText = "Aceptar",
            cancelText = "Cancelar",
            onConfirm = {},
            onDismiss = {},
        )
    }
}
