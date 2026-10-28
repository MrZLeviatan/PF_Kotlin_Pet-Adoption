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

/** Etiqueta pequeña con icono y texto ("Adopción", "1.2 km"). El icono es decorativo: el texto ya lo dice. */
@Composable
fun IconTag(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconColor: Color = AppTheme.colors.textPrimary,
    containerColor: Color = AppTheme.colors.surfaceTag,
    textStyle: TextStyle = MaterialTheme.typography.labelMedium,
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(8.dp), color = containerColor) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(14.dp))
            Text(text, style = textStyle, color = AppTheme.colors.textPrimary)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun IconTagPreview() {
    PetAdoptionAppTheme {
        IconTag(text = "Adopción", icon = Icons.Outlined.Pets)
    }
}
