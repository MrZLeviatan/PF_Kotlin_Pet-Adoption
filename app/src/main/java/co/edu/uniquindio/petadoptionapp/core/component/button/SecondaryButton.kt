package co.edu.uniquindio.petadoptionapp.core.component.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Botón secundario delineado, con icono opcional (por ejemplo "Cerrar sesión").
Misma forma que [DangerButton] pero en el color del texto: es una acción normal, no destructiva,
y de menor jerarquía que PrimaryButton. Comparte forma y altura mediante AppButtonDefaults.
El icono es decorativo: el texto ya describe la acción.
**/
@Composable
fun SecondaryButton(
    // Texto que muestra el botón.
    text: String,
    // Acción que se ejecuta al presionar el botón.
    onClick: () -> Unit,
    // Modificador opcional para personalizar el botón desde afuera.
    modifier: Modifier = Modifier,
    // Icono opcional que se muestra a la izquierda del texto; null si no se quiere icono.
    icon: ImageVector? = null,
    // Indica si el botón está habilitado (por defecto activo).
    enabled: Boolean = true,
) {
    // Dibuja un botón delineado de Material 3 con el estilo secundario.
    OutlinedButton(
        // Conecta la acción recibida con el clic del botón.
        onClick = onClick,
        // Controla si el botón responde a toques.
        enabled = enabled,
        // Ocupa todo el ancho disponible y respeta la altura mínima compartida.
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = AppButtonMinHeight),
        // Aplica la forma de píldora común a los botones de la app.
        shape = AppButtonShape,
        // Borde de 1 dp con el color neutro de bordes del tema.
        border = BorderStroke(1.dp, AppTheme.colors.border),
        // Fondo de superficie y contenido con el color de texto principal.
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = AppTheme.colors.surface,
            contentColor = AppTheme.colors.textPrimary,
        ),
    ) {
        // Solo dibuja el icono y su separación cuando se proporcionó uno.
        if (icon != null) {
            // Icono decorativo de 18 dp, sin descripción porque el texto ya explica la acción.
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            // Separación horizontal entre el icono y el texto.
            Spacer(Modifier.width(8.dp))
        }
        // Texto del botón con el estilo estándar de etiquetas.
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

// Vista previa en Android Studio del botón secundario con icono.
@Preview(showBackground = true)
@Composable
private fun SecondaryButtonPreview() {
    // Envuelve el botón en el tema para que use los colores y tipografías reales.
    PetAdoptionAppTheme {
        // Ejemplo de uso "Cerrar sesión" con su icono de salida.
        SecondaryButton(text = "Cerrar sesión", icon = Icons.AutoMirrored.Outlined.Logout, onClick = {})
    }
}
