package co.edu.uniquindio.petadoptionapp.core.component.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Consejo breve dentro de un formulario ("Usa una foto nítida..."), con borde suave.
Es más discreto que [NoticeBanner]: orienta, no advierte. Se coloca junto a los campos
del formulario de publicación para guiar al usuario sin interrumpirlo.
El icono es decorativo: el texto ya transmite el consejo.
**/
@Composable
fun InfoTip(
    // Icono decorativo que ilustra el consejo.
    icon: ImageVector,
    // Texto del consejo.
    text: String,
    // Modificador opcional para personalizar el componente desde afuera.
    modifier: Modifier = Modifier,
) {
    // Superficie de Material 3 que aporta fondo, forma y borde al consejo.
    Surface(
        // Ocupa todo el ancho disponible.
        modifier = modifier.fillMaxWidth(),
        // Esquinas redondeadas de 12 dp, más pequeñas que las tarjetas para restarle peso.
        shape = RoundedCornerShape(12.dp),
        // Fondo con el color de avisos del tema.
        color = AppTheme.colors.surfaceNotice,
        // Borde suave: color de texto con 60 % de opacidad.
        border = BorderStroke(1.dp, AppTheme.colors.textPrimary.copy(alpha = 0.6f)),
    ) {
        // Fila con el icono y el texto del consejo.
        Row(
            // Relleno interno de 12 dp.
            modifier = Modifier.padding(12.dp),
            // Centra verticalmente icono y texto.
            verticalAlignment = Alignment.CenterVertically,
            // Separación de 12 dp entre icono y texto.
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Icono decorativo de 24 dp con el color de acento de los chips seleccionados.
            Icon(icon, contentDescription = null, tint = AppTheme.colors.chipSelected, modifier = Modifier.size(24.dp))
            // Texto del consejo en tamaño pequeño y semi-negrita.
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppTheme.colors.textPrimary)
        }
    }
}

// Vista previa en Android Studio de un consejo de ejemplo.
@Preview(showBackground = true)
@Composable
private fun InfoTipPreview() {
    // Envuelve el componente en el tema para que use los colores y tipografías reales.
    PetAdoptionAppTheme {
        // Consejo de ejemplo sobre la foto de la mascota.
        InfoTip(icon = Icons.Outlined.Image, text = "Usa una foto nítida y bien iluminada.")
    }
}
