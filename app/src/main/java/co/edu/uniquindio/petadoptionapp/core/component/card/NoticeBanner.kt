package co.edu.uniquindio.petadoptionapp.core.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Aviso destacado con icono y mensaje (por ejemplo "La publicación rechazada no aparecerá en el feed").
Se usa para advertir al usuario sobre el estado de algo importante; es más llamativo que [InfoTip]
gracias al fondo de color de acento. El icono es decorativo: el texto ya dice todo.
**/
@Composable
fun NoticeBanner(
    // Mensaje del aviso.
    message: String,
    // Modificador opcional para personalizar el aviso desde afuera.
    modifier: Modifier = Modifier,
    // Icono del aviso; por defecto un triángulo de advertencia.
    icon: ImageVector = Icons.Outlined.WarningAmber,
) {
    // Fila con fondo de color que contiene el icono y el mensaje.
    Row(
        modifier = modifier
            // Ocupa todo el ancho disponible.
            .fillMaxWidth()
            // Fondo de acento con esquinas redondeadas de 16 dp.
            .background(AppTheme.colors.chipSelected, RoundedCornerShape(16.dp))
            // Relleno interno de 16 dp.
            .padding(16.dp),
        // Centra verticalmente icono y texto.
        verticalAlignment = Alignment.CenterVertically,
        // Separación de 12 dp entre icono y texto.
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Icono decorativo del aviso.
        Icon(
            imageVector = icon,
            // Sin descripción: el mensaje ya transmite la información.
            contentDescription = null,
            tint = AppTheme.colors.textPrimary,
            modifier = Modifier.size(24.dp),
        )
        // Mensaje del aviso.
        Text(
            text = message,
            style = MaterialTheme.typography.labelMedium,
            color = AppTheme.colors.textPrimary,
            // Ocupa el espacio restante para que los textos largos se ajusten en varias líneas.
            modifier = Modifier.weight(1f),
        )
    }
}

// Vista previa en Android Studio de un aviso de ejemplo.
@Preview(showBackground = true)
@Composable
private fun NoticeBannerPreview() {
    // Envuelve el componente en el tema para que use los colores y tipografías reales.
    PetAdoptionAppTheme {
        // Aviso de ejemplo sobre una publicación rechazada.
        NoticeBanner(message = "La publicación rechazada no aparecerá en el feed público.")
    }
}