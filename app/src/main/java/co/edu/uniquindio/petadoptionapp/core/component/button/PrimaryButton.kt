package co.edu.uniquindio.petadoptionapp.core.component.button

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme

/**
 Botón de la acción principal de una pantalla (ej. "Iniciar sesión").

 El texto hereda `labelLarge` (Rubik Medium) del `Button` de Material.
 Un solo botón principal por pantalla: si hay dos, ninguno es principal.
 */
@Composable
fun PrimaryButton(
    // Texto que mostrará el botón
    text: String,
    // Acción que se ejecuta al presionar el botón
    onClick: () -> Unit,
    // Modificador opcional para personalizar desde afuera
    modifier: Modifier = Modifier,
    // Estado de habilitación del botón (por defecto activo)
    enabled: Boolean = true,
    // Estilo del texto; por defecto el mismo `labelLarge` que usa Material
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
) {
    // Dibuja el botón oficial de Material 3 con los parámetros provistos
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),   // área táctil cómoda (mínimo recomendado: 48 dp)
        shape = CircleShape,          // forma de píldora, como en el diseño
        colors = ButtonDefaults.buttonColors(
            containerColor = AppTheme.colors.actionPrimary,
            contentColor = AppTheme.colors.onActionPrimary,
            disabledContainerColor = AppTheme.colors.stateDisabled,
            disabledContentColor = AppTheme.colors.textPrimary,
        ),
    ) {
        // Muestra el texto recibido dentro del botón
        Text(text = text, style = textStyle)
    }
}