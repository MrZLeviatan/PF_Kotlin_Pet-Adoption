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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/** Línea de dato con icono pequeño y texto ("Armenia, Colombia"). El icono es decorativo: el texto ya lo dice. */
@Composable
fun IconTextRow(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 16.dp,
    spacing: Dp = 8.dp,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppTheme.colors.textPrimary,
            modifier = Modifier.size(iconSize),
        )
        Text(
            text = text,
            style = style,
            color = AppTheme.colors.textPrimary,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun IconTextRowPreview() {
    PetAdoptionAppTheme {
        IconTextRow(icon = Icons.Outlined.LocationOn, text = "Armenia, Colombia")
    }
}
