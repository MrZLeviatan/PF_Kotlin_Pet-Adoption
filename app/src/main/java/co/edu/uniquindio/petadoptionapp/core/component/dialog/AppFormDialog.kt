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
Diálogo con formulario usado cuando una acción necesita datos extra del usuario antes de
ejecutarse (cambiar contraseña, confirmar la contraseña para eliminar la cuenta, etc.).
Comparte el aspecto de los demás diálogos del sistema de diseño y reutiliza sus botones y
campos de texto, para que solo haya que pasarle los campos concretos de cada caso.
**/

/**
Diálogo con campos (contraseña, confirmaciones): título, mensaje opcional, los campos que
se pasen en [content], un error opcional y dos acciones (confirmar y cancelar).
Con [isDestructive] el botón de confirmar usa el estilo de peligro ("Eliminar").
Mientras [isLoading] no se puede cerrar ni confirmar dos veces.
El contenido es desplazable para que el teclado no tape los botones en pantallas pequeñas.
**/
@Composable
fun AppFormDialog(
    // Título del diálogo.
    title: String,
    // Texto del botón de confirmar.
    confirmText: String,
    // Acción que se ejecuta al confirmar.
    onConfirm: () -> Unit,
    // Acción que cierra el diálogo (cancelar, "atrás" o tocar fuera).
    onDismiss: () -> Unit,
    // Modificador opcional para personalizar el contenedor del diálogo.
    modifier: Modifier = Modifier,
    // Mensaje opcional bajo el título.
    message: String? = null,
    // Mensaje de error opcional (p. ej. contraseña incorrecta).
    errorMessage: String? = null,
    // Permite deshabilitar confirmar mientras los campos no sean válidos.
    confirmEnabled: Boolean = true,
    // Indica si la acción está en curso; bloquea botones y cierre.
    isLoading: Boolean = false,
    // Indica si la acción es destructiva, para usar el botón de peligro.
    isDestructive: Boolean = false,
    // Campos del formulario que se dibujan en el cuerpo del diálogo.
    content: @Composable ColumnScope.() -> Unit,
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
                // Permite desplazar el contenido si no cabe (por ejemplo con el teclado abierto).
                .verticalScroll(rememberScrollState())
                // Relleno interno de 24 dp.
                .padding(24.dp),
            // Centra horizontalmente los elementos.
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Título del diálogo, centrado.
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = AppTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            // Solo muestra el mensaje si se proporcionó.
            if (message != null) {
                // Separación entre el título y el mensaje.
                Spacer(Modifier.height(8.dp))
                // Mensaje explicativo, centrado.
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                )
            }

            // Separación entre la cabecera y los campos.
            Spacer(Modifier.height(20.dp))

            // Columna que contiene los campos recibidos, separados 12 dp entre sí.
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content,
            )

            // Solo muestra el error si existe.
            if (errorMessage != null) {
                // Separación entre los campos y el error.
                Spacer(Modifier.height(12.dp))
                // Texto de error del sistema de diseño a todo el ancho.
                ErrorText(text = errorMessage, modifier = Modifier.fillMaxWidth())
            }

            // Separación entre el contenido y los botones.
            Spacer(Modifier.height(24.dp))

            // Confirmar solo está activo si el formulario lo permite y no hay una operación en curso.
            val canConfirm = confirmEnabled && !isLoading
            // Elige el estilo del botón de confirmar según si la acción es destructiva.
            if (isDestructive) {
                // Confirmar con estilo de peligro (p. ej. "Eliminar").
                DangerButton(text = confirmText, onClick = onConfirm, enabled = canConfirm)
            } else {
                // Confirmar con el estilo principal.
                PrimaryButton(text = confirmText, onClick = onConfirm, enabled = canConfirm)
            }

            // Separación entre confirmar y cancelar.
            Spacer(Modifier.height(12.dp))

            // Cancelar como botón delineado (igual que "Cerrar sesión"), del mismo tamaño que confirmar.
            SecondaryButton(
                // Texto "Cancelar" desde recursos.
                text = stringResource(R.string.common_cancel),
                // Cierra el diálogo.
                onClick = onDismiss,
                // Deshabilitado mientras hay una operación en curso.
                enabled = !isLoading,
            )
        }
    }
}

// Vista previa en Android Studio de un diálogo de cambio de contraseña con error.
@Preview
@Composable
private fun AppFormDialogPreview() {
    // Envuelve el diálogo en el tema para que use los colores y tipografías reales.
    PetAdoptionAppTheme {
        // Diálogo de ejemplo con un error visible.
        AppFormDialog(
            title = "Cambiar contraseña",
            confirmText = "Guardar",
            onConfirm = {},
            onDismiss = {},
            errorMessage = "La contraseña actual no es correcta.",
        ) {
            // Campo de contraseña de ejemplo.
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
