package co.edu.uniquindio.petadoptionapp.core.component.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Acción secundaria de solo texto con un icono a la izquierda (por ejemplo "Eliminar" o "Editar"
dentro de una tarjeta). Es el botón de menor peso visual del sistema de diseño: no tiene fondo
ni borde, solo texto e icono en el color indicado. El icono es decorativo: el texto ya describe la acción.
**/
@Composable
fun TextActionButton(
    // Texto que describe la acción.
    text: String,
    // Icono decorativo que acompaña el texto.
    icon: ImageVector,
    // Acción que se ejecuta al presionar el botón.
    onClick: () -> Unit,
    // Modificador opcional para personalizar el botón desde afuera.
    modifier: Modifier = Modifier,
    // Color del texto e icono; permite, por ejemplo, usar el color de error en acciones destructivas.
    contentColor: Color = AppTheme.colors.textPrimary,
) {
    // Botón de texto de Material 3 sin fondo ni borde.
    TextButton(
        // Conecta la acción recibida con el clic del botón.
        onClick = onClick,
        // Asegura una altura mínima de 48 dp para mantener un área táctil accesible.
        modifier = modifier.heightIn(min = 48.dp),
        // Relleno horizontal reducido para que el botón ocupe poco espacio junto a otros elementos.
        contentPadding = PaddingValues(horizontal = 4.dp),
        // Aplica el color de contenido recibido al texto y al icono.
        colors = ButtonDefaults.textButtonColors(contentColor = contentColor),
    ) {
        // Icono decorativo de 18 dp, sin descripción porque el texto ya explica la acción.
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
        // Separación horizontal entre el icono y el texto.
        Spacer(Modifier.width(6.dp))
        // Texto de la acción con un estilo de etiqueta mediano, más discreto que los botones principales.
        Text(text = text, style = MaterialTheme.typography.labelMedium)
    }
}

// Vista previa en Android Studio del botón de texto con icono.
@Preview(showBackground = true)
@Composable
private fun TextActionButtonPreview() {
    // Envuelve el botón en el tema para que use los colores y tipografías reales.
    PetAdoptionAppTheme {
        // Ejemplo de acción "Eliminar" con su icono de papelera.
        TextActionButton(text = "Eliminar", icon = Icons.Outlined.Delete, onClick = {})
    }
}