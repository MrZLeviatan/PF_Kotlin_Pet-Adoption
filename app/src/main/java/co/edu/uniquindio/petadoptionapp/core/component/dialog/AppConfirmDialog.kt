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
Diálogo de confirmación reutilizable en toda la app: se muestra antes de cualquier acción que el
usuario debe aprobar (finalizar una publicación, cerrar sesión, eliminar un mensaje). Combina los
botones del sistema de diseño (PrimaryButton, DangerButton, SecondaryButton) para que todas las
confirmaciones se vean y se comporten igual.
**/

/**
Diálogo de confirmación ("¿Finalizar publicación?"): icono, título, mensaje y dos botones.
Normalmente confirmar es el botón principal (amarillo) y Cancelar va delineado en rojo.
Con [isDestructive] (cerrar sesión, eliminar) se invierten: confirmar va en rojo y Cancelar en amarillo,
para que la acción que no tiene vuelta atrás se distinga.
Con [alternativeText] aparece una tercera opción neutral entre las dos (ej. "Eliminar para mí").
Mientras [isLoading] no se puede cerrar ni confirmar dos veces.
**/
@Composable
fun AppConfirmDialog(
    // Título o pregunta del diálogo.
    title: String,
    // Mensaje que explica las consecuencias de la acción.
    message: String,
    // Texto del botón de confirmar.
    confirmText: String,
    // Texto del botón de cancelar.
    cancelText: String,
    // Acción que se ejecuta al confirmar.
    onConfirm: () -> Unit,
    // Acción que cierra el diálogo (cancelar, "atrás" o tocar fuera).
    onDismiss: () -> Unit,
    // Modificador opcional para personalizar el contenedor del diálogo.
    modifier: Modifier = Modifier,
    // Indica si la acción está en curso; bloquea botones y cierre.
    isLoading: Boolean = false,
    // Indica si la acción es destructiva, lo que invierte los estilos de los botones.
    isDestructive: Boolean = false,
    // Texto de una tercera opción neutral; `null` para no mostrarla.
    alternativeText: String? = null,
    // Acción de la tercera opción.
    onAlternative: () -> Unit = {},
    // Por defecto la huella; cada pantalla puede poner el suyo.
    icon: @Composable () -> Unit = {
        // Huella de 64 dp sobre el color de marca.
        PawCircle(size = 64.dp, containerColor = AppTheme.colors.brand)
    },
) {
    // Ventana de diálogo que se muestra sobre la pantalla actual.
    Dialog(
        // Solo permite cerrar el diálogo cuando no hay una operación en curso.
        onDismissRequest = { if (!isLoading) onDismiss() },
        // Bloquea el cierre con "atrás" y al tocar fuera mientras carga.
        properties = DialogProperties(dismissOnBackPress = !isLoading, dismissOnClickOutside = !isLoading),
    ) {
        // Columna centrada que forma el cuerpo del diálogo.
        Column(
            modifier = modifier
                // Ocupa el ancho disponible del diálogo.
                .fillMaxWidth()
                // Fondo de superficie con esquinas de 24 dp.
                .background(AppTheme.colors.surface, RoundedCornerShape(24.dp))
                // Relleno interno de 24 dp.
                .padding(24.dp),
            // Centra horizontalmente todos los elementos.
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Dibuja el icono recibido (por defecto la huella).
            icon()
            // Separación entre el icono y el título.
            Spacer(Modifier.height(16.dp))
            // Título del diálogo, centrado.
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = AppTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            // Separación entre el título y el mensaje.
            Spacer(Modifier.height(8.dp))
            // Mensaje explicativo, centrado.
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            // Separación entre el mensaje y los botones.
            Spacer(Modifier.height(24.dp))
            // Acción destructiva: confirmar en rojo y cancelar como botón principal.
            if (isDestructive) {
                // Botón de confirmar con estilo de peligro, en texto normal (sin mayúsculas).
                DangerButton(text = confirmText, onClick = onConfirm, enabled = !isLoading, uppercase = false)
                // Separación entre botones.
                Spacer(Modifier.height(12.dp))
                // Solo muestra la opción alternativa si se proporcionó.
                if (alternativeText != null) {
                    // Opción neutral intermedia con el botón secundario.
                    SecondaryButton(text = alternativeText, onClick = onAlternative, enabled = !isLoading)
                    // Separación tras la opción alternativa.
                    Spacer(Modifier.height(12.dp))
                }
                // Cancelar como botón principal, para que sea la opción "segura" más visible.
                PrimaryButton(text = cancelText, onClick = onDismiss, enabled = !isLoading)
            // Acción normal: confirmar como botón principal y cancelar en rojo.
            } else {
                // Botón de confirmar con el estilo principal.
                PrimaryButton(text = confirmText, onClick = onConfirm, enabled = !isLoading)
                // Separación entre botones.
                Spacer(Modifier.height(12.dp))
                // Solo muestra la opción alternativa si se proporcionó.
                if (alternativeText != null) {
                    // Opción neutral intermedia con el botón secundario.
                    SecondaryButton(text = alternativeText, onClick = onAlternative, enabled = !isLoading)
                    // Separación tras la opción alternativa.
                    Spacer(Modifier.height(12.dp))
                }
                // Botón de cancelar delineado en rojo, en texto normal.
                DangerButton(text = cancelText, onClick = onDismiss, enabled = !isLoading, uppercase = false)
            }
        }
    }
}

// Vista previa en Android Studio de una confirmación no destructiva.
@Preview
@Composable
private fun AppConfirmDialogPreview() {
    // Envuelve el diálogo en el tema para que use los colores y tipografías reales.
    PetAdoptionAppTheme {
        // Confirmación de ejemplo para finalizar una publicación.
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
