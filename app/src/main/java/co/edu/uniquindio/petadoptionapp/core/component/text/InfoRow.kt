package co.edu.uniquindio.petadoptionapp.core.component.text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.component.icon.IconCircle
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
 Fila de dato destacado: icono en círculo, etiqueta pequeña arriba ("TELÉFONO") y el valor
 debajo. [trailing] es opcional para una acción (llamar, copiar...).
 El icono es decorativo: la etiqueta ya dice qué dato es.
 */
@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconCircle(icon = icon, size = 40.dp, containerColor = AppTheme.colors.surfaceTag)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                // Se guarda normal y se pasa a mayúsculas aquí, para que TalkBack lo lea como palabra.
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.colors.textPrimary,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = AppTheme.colors.textPrimary,
            )
        }
        trailing?.invoke()
    }
}

/** Botón redondo de acción para [InfoRow] (ej. llamar). Sí se anuncia: es interactivo. */
@Composable
fun InfoRowAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledIconButton(
        onClick = onClick,
        modifier = modifier,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = AppTheme.colors.actionPrimary,
            contentColor = AppTheme.colors.onActionPrimary,
        ),
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}

@Preview(showBackground = true)
@Composable
private fun InfoRowPreview() {
    PetAdoptionAppTheme {
        InfoRow(
            icon = Icons.Outlined.Phone,
            label = "Teléfono",
            value = "300 123 4567",
            trailing = { InfoRowAction(icon = Icons.Outlined.Call, contentDescription = "Llamar", onClick = {}) },
        )
    }
}
