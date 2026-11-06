package co.edu.uniquindio.petadoptionapp.core.component.button

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme

/**
Botón de la acción principal de una pantalla (por ejemplo "Iniciar sesión" o "Publicar").
Es relleno con el color de acción principal del tema, ocupa todo el ancho y comparte forma y
altura con los demás botones del sistema de diseño (AppButtonDefaults).

El texto hereda `labelLarge` (Rubik Medium) del `Button` de Material.
Un solo botón principal por pantalla: si hay dos, ninguno es principal.
**/
@Composable
fun PrimaryButton(
    // Texto que mostrará el botón.
    text: String,
    // Acción que se ejecuta al presionar el botón.
    onClick: () -> Unit,
    // Modificador opcional para personalizar desde afuera.
    modifier: Modifier = Modifier,
    // Estado de habilitación del botón (por defecto activo).
    enabled: Boolean = true,
    // Estilo del texto; por defecto el mismo `labelLarge` que usa Material.
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
) {
    // Dibuja el botón oficial de Material 3 con los parámetros provistos.
    Button(
        // Conecta la acción recibida con el clic del botón.
        onClick = onClick,
        // Controla si el botón responde a toques (p. ej. deshabilitado mientras un formulario es inválido).
        enabled = enabled,
        // Ocupa todo el ancho disponible y respeta la altura mínima compartida.
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = AppButtonMinHeight),
        // Aplica la forma de píldora común a los botones de la app.
        shape = AppButtonShape,
        // Define los colores del botón en sus estados habilitado y deshabilitado.
        colors = ButtonDefaults.buttonColors(
            // Fondo con el color de acción principal del tema.
            containerColor = AppTheme.colors.actionPrimary,
            // Texto con el color de contraste sobre la acción principal.
            contentColor = AppTheme.colors.onActionPrimary,
            // Fondo gris de "deshabilitado" cuando el botón no está activo.
            disabledContainerColor = AppTheme.colors.stateDisabled,
            // Texto legible sobre el fondo deshabilitado.
            disabledContentColor = AppTheme.colors.textPrimary,
        ),
    ) {
        // Muestra el texto recibido dentro del botón.
        Text(text = text, style = textStyle)
    }
}