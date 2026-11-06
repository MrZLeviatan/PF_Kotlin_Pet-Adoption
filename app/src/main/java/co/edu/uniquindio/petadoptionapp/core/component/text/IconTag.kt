package co.edu.uniquindio.petadoptionapp.core.component.text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Componente de etiqueta compacta (tag) con ícono y texto sobre un fondo redondeado.
Se usa en las tarjetas y el detalle de las publicaciones para mostrar datos breves como la categoría
("Adopción") o la distancia ("1.2 km"). Los colores del ícono, del fondo y el estilo del texto son configurables.
**/

/**
Etiqueta pequeña con icono y texto ("Adopción", "1.2 km"). El icono es decorativo: el texto ya lo dice.
**/
@Composable
fun IconTag(
    // Texto de la etiqueta.
    text: String,
    // Ícono que acompaña al texto.
    icon: ImageVector,
    // Modificador para posicionar la etiqueta.
    modifier: Modifier = Modifier,
    // Color del ícono; por defecto, el del texto principal.
    iconColor: Color = AppTheme.colors.textPrimary,
    // Color de fondo de la etiqueta.
    containerColor: Color = AppTheme.colors.surfaceTag,
    // Estilo tipográfico del texto.
    textStyle: TextStyle = MaterialTheme.typography.labelMedium,
) {
    // Fondo redondeado de la etiqueta.
    Surface(modifier = modifier, shape = RoundedCornerShape(8.dp), color = containerColor) {
        // Fila con ícono y texto.
        Row(
            // Espaciado interno de la etiqueta.
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            // Centra verticalmente ícono y texto.
            verticalAlignment = Alignment.CenterVertically,
            // Separación entre ícono y texto.
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // Ícono decorativo pequeño (sin descripción, el texto ya informa).
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(14.dp))
            // Texto de la etiqueta.
            Text(text, style = textStyle, color = AppTheme.colors.textPrimary)
        }
    }
}

// Vista previa en Android Studio con la etiqueta "Adopción".
@Preview(showBackground = true)
@Composable
private fun IconTagPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Etiqueta de ejemplo con ícono de huella.
        IconTag(text = "Adopción", icon = Icons.Outlined.Pets)
    }
}
