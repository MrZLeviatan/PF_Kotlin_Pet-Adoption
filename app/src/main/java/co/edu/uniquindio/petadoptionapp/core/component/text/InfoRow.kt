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
Componentes para mostrar datos destacados (teléfono, correo, dirección, etc.), por ejemplo en el perfil o en el
detalle de una publicación. [InfoRow] dibuja un ícono dentro de un círculo, una etiqueta en mayúsculas y el valor,
con un espacio opcional al final para una acción; [InfoRowAction] es el botón redondo pensado para ese espacio.
**/

/**
Fila de dato destacado: icono en círculo, etiqueta pequeña arriba ("TELÉFONO") y el valor
debajo. [trailing] es opcional para una acción (llamar, copiar...).
El icono es decorativo: la etiqueta ya dice qué dato es.
**/
@Composable
fun InfoRow(
    // Ícono del dato.
    icon: ImageVector,
    // Nombre del dato (se muestra en mayúsculas).
    label: String,
    // Valor del dato.
    value: String,
    // Modificador para posicionar la fila.
    modifier: Modifier = Modifier,
    // Contenido opcional al final de la fila (normalmente un botón de acción).
    trailing: (@Composable () -> Unit)? = null,
) {
    // Fila principal.
    Row(
        modifier = modifier
            // Ocupa todo el ancho.
            .fillMaxWidth()
            // Altura mínima para que sea cómoda de leer y tocar.
            .heightIn(min = 56.dp),
        // Centra verticalmente el contenido.
        verticalAlignment = Alignment.CenterVertically,
        // Separación entre elementos.
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Ícono dentro de un círculo de color.
        IconCircle(icon = icon, size = 40.dp, containerColor = AppTheme.colors.surfaceTag)
        // Columna con etiqueta y valor, que ocupa el espacio restante.
        Column(modifier = Modifier.weight(1f)) {
            // Etiqueta del dato.
            Text(
                // Se guarda normal y se pasa a mayúsculas aquí, para que TalkBack lo lea como palabra.
                text = label.uppercase(),
                // Tipografía de etiqueta pequeña.
                style = MaterialTheme.typography.labelSmall,
                // Color de texto principal.
                color = AppTheme.colors.textPrimary,
            )
            // Valor del dato.
            Text(
                // Valor recibido.
                text = value,
                // Tipografía de título pequeño para destacarlo.
                style = MaterialTheme.typography.titleSmall,
                // Color de texto principal.
                color = AppTheme.colors.textPrimary,
            )
        }
        // Dibuja la acción final si se proporcionó.
        trailing?.invoke()
    }
}

/**
Botón redondo de acción para [InfoRow] (ej. llamar). Sí se anuncia: es interactivo.
**/
@Composable
fun InfoRowAction(
    // Ícono de la acción.
    icon: ImageVector,
    // Descripción accesible de la acción (obligatoria por ser interactiva).
    contentDescription: String,
    // Acción al tocar el botón.
    onClick: () -> Unit,
    // Modificador para posicionar el botón.
    modifier: Modifier = Modifier,
) {
    // Botón de ícono relleno.
    FilledIconButton(
        // Acción al tocarlo.
        onClick = onClick,
        // Modificador recibido.
        modifier = modifier,
        // Colores de acción principal del tema.
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = AppTheme.colors.actionPrimary,
            contentColor = AppTheme.colors.onActionPrimary,
        ),
    ) {
        // Ícono con su descripción accesible.
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}

// Vista previa en Android Studio de un teléfono con botón para llamar.
@Preview(showBackground = true)
@Composable
private fun InfoRowPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Fila de ejemplo.
        InfoRow(
            icon = Icons.Outlined.Phone,
            label = "Teléfono",
            value = "300 123 4567",
            trailing = { InfoRowAction(icon = Icons.Outlined.Call, contentDescription = "Llamar", onClick = {}) },
        )
    }
}
