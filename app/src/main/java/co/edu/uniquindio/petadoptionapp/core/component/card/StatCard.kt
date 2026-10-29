package co.edu.uniquindio.petadoptionapp.core.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.component.indicator.StatusIndicator
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
 Métrica individual: etiqueta pequeña en mayúsculas y el valor grande debajo.
 Opcionales: [description] bajo el valor y [indicatorColor], un punto de color junto a la etiqueta
 (dashboard de moderación). Sin ellos se ve igual que en Métricas.
 Con [onClick] es tocable (ej. filtrar por esa métrica) y [selected] la marca con un borde más grueso.
 */
@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    indicatorColor: Color? = null,
    onClick: (() -> Unit)? = null,
    selected: Boolean = false,
) {
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppTheme.colors.surface, shape)
            .border(if (selected) 2.dp else 1.dp, if (selected) AppTheme.colors.textPrimary else AppTheme.colors.border, shape)
            .then(
                if (onClick != null) {
                    Modifier.selectable(selected = selected, role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                // Se guarda normal y se pasa a mayúsculas aquí, para que TalkBack lo lea como palabra.
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            if (indicatorColor != null) StatusIndicator(color = indicatorColor)
        }
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = AppTheme.colors.textPrimary,
        )
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.textPrimary,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatCardPreview() {
    PetAdoptionAppTheme {
        StatCard(label = "Activas", value = "12")
    }
}