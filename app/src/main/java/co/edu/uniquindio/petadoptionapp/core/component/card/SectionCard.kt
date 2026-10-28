package co.edu.uniquindio.petadoptionapp.core.component.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
 Tarjeta con encabezado y un contenido libre debajo.
 [subtitle] e [icon] son opcionales: con icono, el título va a su derecha ("Seguridad").
 El icono es decorativo: el título ya nombra la sección.
 */
@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    AppCard(modifier = modifier) {
        Row(
            // heading: TalkBack permite saltar de sección en sección.
            modifier = Modifier.semantics { heading() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppTheme.colors.brand,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = AppTheme.colors.textPrimary,
            )
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.textPrimary,
            )
        }
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionCardPreview() {
    PetAdoptionAppTheme {
        SectionCard(title = "Seguridad", icon = Icons.Outlined.Lock) {
            Text("Contenido", color = AppTheme.colors.textPrimary)
        }
    }
}
