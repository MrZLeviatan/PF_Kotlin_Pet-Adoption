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
 Es más discreto que [NoticeBanner]: orienta, no advierte. El icono es decorativo.
 */
@Composable
fun InfoTip(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = AppTheme.colors.surfaceNotice,
        border = BorderStroke(1.dp, AppTheme.colors.textPrimary.copy(alpha = 0.6f)),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(icon, contentDescription = null, tint = AppTheme.colors.chipSelected, modifier = Modifier.size(24.dp))
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppTheme.colors.textPrimary)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InfoTipPreview() {
    PetAdoptionAppTheme {
        InfoTip(icon = Icons.Outlined.Image, text = "Usa una foto nítida y bien iluminada.")
    }
}
