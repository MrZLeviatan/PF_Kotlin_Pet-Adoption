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
import co.edu.uniquindio.petadoptionapp.core.component.button.PrimaryButton
import co.edu.uniquindio.petadoptionapp.core.component.icon.PawCircle
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Diálogo informativo del sistema de diseño, usado para comunicar un resultado que el usuario debe
leer antes de continuar (por ejemplo "Cuenta creada"). Es el más simple de la familia de diálogos:
no pide decisiones, solo un reconocimiento con un único botón.
**/

/**
Diálogo informativo con icono, título, mensaje y un solo botón.
No se cierra al tocar fuera ni con "atrás": la única salida es el botón.
**/
@Composable
fun AppInfoDialog(
    // Título del diálogo.
    title: String,
    // Mensaje informativo.
    message: String,
    // Texto del único botón.
    confirmText: String,
    // Acción del botón, que también debe cerrar el diálogo.
    onConfirm: () -> Unit,
    // Modificador opcional para personalizar el contenedor del diálogo.
    modifier: Modifier = Modifier,
    // Por defecto la huella; cada pantalla puede poner el suyo.
    icon: @Composable () -> Unit = {
        // Huella de 56 dp sobre el fondo de etiquetas.
        PawCircle(size = 56.dp, containerColor = AppTheme.colors.surfaceTag)
    },
) {
    // Ventana de diálogo que se muestra sobre la pantalla actual.
    Dialog(
        // Lambda vacía: ignora las solicitudes de cierre externas.
        onDismissRequest = {},
        // Desactiva el cierre con "atrás" y al tocar fuera.
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
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
            // Mensaje informativo, centrado.
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            // Separación entre el mensaje y el botón.
            Spacer(Modifier.height(24.dp))
            // Único botón del diálogo, con el estilo principal.
            PrimaryButton(text = confirmText, onClick = onConfirm)
        }
    }
}

// Vista previa en Android Studio del diálogo de cuenta creada.
@Preview
@Composable
private fun AppInfoDialogPreview() {
    // Envuelve el diálogo en el tema para que use los colores y tipografías reales.
    PetAdoptionAppTheme {
        // Diálogo de ejemplo tras crear una cuenta.
        AppInfoDialog(
            title = "Cuenta creada",
            message = "¡Bienvenido! Tu cuenta ya está lista.",
            confirmText = "Aceptar",
            onConfirm = {},
        )
    }
}