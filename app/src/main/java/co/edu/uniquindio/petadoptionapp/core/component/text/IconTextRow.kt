package co.edu.uniquindio.petadoptionapp.core.component.text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Componente de una línea de información con un ícono pequeño a la izquierda y un texto a la derecha.
Se usa en muchas partes de la app (ubicación en tarjetas, sugerencias del selector de ubicación, pistas bajo el mapa,
lista de chats, etc.). El tamaño del ícono, la separación, el estilo y el número máximo de líneas son configurables.
**/

/**
Línea de dato con icono pequeño y texto ("Armenia, Colombia"). El icono es decorativo: el texto ya lo dice.
**/
@Composable
fun IconTextRow(
    // Ícono a mostrar.
    icon: ImageVector,
    // Texto del dato.
    text: String,
    // Modificador para posicionar la fila.
    modifier: Modifier = Modifier,
    // Tamaño del ícono.
    iconSize: Dp = 16.dp,
    // Separación entre ícono y texto.
    spacing: Dp = 8.dp,
    // Estilo tipográfico del texto.
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    // Con un límite, lo que sobra termina en "…" (ej. el título de la publicación en la lista de chats).
    maxLines: Int = Int.MAX_VALUE,
) {
    // Fila con ícono y texto.
    Row(
        // Aplica el modificador recibido.
        modifier = modifier,
        // Centra verticalmente ícono y texto.
        verticalAlignment = Alignment.CenterVertically,
        // Separación configurable entre ícono y texto.
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        // Ícono decorativo.
        Icon(
            // Vector del ícono.
            imageVector = icon,
            // Sin descripción: el texto ya transmite la información.
            contentDescription = null,
            // Color del texto principal del tema.
            tint = AppTheme.colors.textPrimary,
            // Tamaño configurable.
            modifier = Modifier.size(iconSize),
        )
        // Texto del dato.
        Text(
            // Texto recibido.
            text = text,
            // Estilo configurable.
            style = style,
            // Color del texto principal del tema.
            color = AppTheme.colors.textPrimary,
            // Número máximo de líneas.
            maxLines = maxLines,
            // Si no cabe, termina con puntos suspensivos.
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// Vista previa en Android Studio con una ubicación de ejemplo.
@Preview(showBackground = true)
@Composable
private fun IconTextRowPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Fila de ejemplo con ícono de ubicación.
        IconTextRow(icon = Icons.Outlined.LocationOn, text = "Armenia, Colombia")
    }
}
