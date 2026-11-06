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
Métrica individual: etiqueta pequeña en mayúsculas y el valor grande debajo. Se usa en la pantalla
de Métricas y en el dashboard de moderación, normalmente en cuadrículas de varias tarjetas.
Opcionales: [description] bajo el valor y [indicatorColor], un punto de color junto a la etiqueta
(dashboard de moderación). Sin ellos se ve igual que en Métricas.
Con [onClick] es tocable (ej. filtrar por esa métrica) y [selected] la marca con un borde más grueso.
**/
@Composable
fun StatCard(
    // Nombre de la métrica ("Activas").
    label: String,
    // Valor de la métrica ya formateado ("12").
    value: String,
    // Modificador opcional para personalizar la tarjeta desde afuera.
    modifier: Modifier = Modifier,
    // Texto explicativo opcional bajo el valor.
    description: String? = null,
    // Color opcional del punto de estado junto a la etiqueta.
    indicatorColor: Color? = null,
    // Acción al tocar la tarjeta; `null` si es solo informativa.
    onClick: (() -> Unit)? = null,
    // Indica si la métrica está seleccionada (p. ej. como filtro activo).
    selected: Boolean = false,
) {
    // Forma redondeada de 16 dp reutilizada por el recorte, el fondo y el borde.
    val shape = RoundedCornerShape(16.dp)

    // Columna que forma la tarjeta de la métrica.
    Column(
        modifier = modifier
            // Ocupa todo el ancho disponible (en cuadrícula, el de su celda).
            .fillMaxWidth()
            // Recorta antes del clic para que el efecto de toque respete las esquinas.
            .clip(shape)
            // Fondo de superficie del tema.
            .background(AppTheme.colors.surface, shape)
            // Borde más grueso y oscuro cuando está seleccionada; borde neutro de 1 dp si no.
            .border(if (selected) 2.dp else 1.dp, if (selected) AppTheme.colors.textPrimary else AppTheme.colors.border, shape)
            // Agrega el comportamiento de selección solo cuando hay acción.
            .then(
                // selectable informa a TalkBack si la métrica está seleccionada.
                if (onClick != null) {
                    Modifier.selectable(selected = selected, role = Role.Button, onClick = onClick)
                // Sin acción la tarjeta no es interactiva.
                } else {
                    Modifier
                }
            )
            // Relleno interno de la tarjeta.
            .padding(12.dp),
    ) {
        // Fila con la etiqueta y el punto de estado opcional.
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Etiqueta de la métrica.
            Text(
                // Se guarda normal y se pasa a mayúsculas aquí, para que TalkBack lo lea como palabra.
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.colors.textPrimary,
                // Ocupa el espacio restante para empujar el punto al extremo derecho.
                modifier = Modifier.weight(1f),
            )
            // Muestra el punto de estado solo si se indicó un color.
            if (indicatorColor != null) StatusIndicator(color = indicatorColor)
        }
        // Valor de la métrica en tamaño grande.
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = AppTheme.colors.textPrimary,
        )
        // Solo dibuja la descripción si se proporcionó.
        if (description != null) {
            // Descripción en texto pequeño bajo el valor.
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.textPrimary,
            )
        }
    }
}

// Vista previa en Android Studio de una métrica simple.
@Preview(showBackground = true)
@Composable
private fun StatCardPreview() {
    // Envuelve el componente en el tema para que use los colores y tipografías reales.
    PetAdoptionAppTheme {
        // Métrica de ejemplo sin descripción ni indicador.
        StatCard(label = "Activas", value = "12")
    }
}