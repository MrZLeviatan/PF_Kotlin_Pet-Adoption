package co.edu.uniquindio.petadoptionapp.core.component.form

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
 Selector de opción única con aspecto de botones, usado en los pasos de crear
 publicación (tamaño de la mascota, forma de elegir la ubicación). Comparte
 FormLabel y los roles chipSelected/background/border del tema con el resto del
 formulario por pasos.
**/

/** Una opción del selector: texto y, si se quiere, un icono a la izquierda. */
data class SelectorOption(val label: String, val icon: ImageVector? = null)

/**
 Fila de opciones de selección única con forma de botón ("Pequeño / Mediano / Grande").
 [label] opcional va arriba con asterisco. Con [fillWidth] las opciones se reparten
 todo el ancho (útil para dos opciones con icono, como "Ubicación actual / Seleccionar mapa").
 TalkBack las anuncia como botones de opción.
**/
@Composable
fun OptionSelector(
    // Opciones a mostrar.
    options: List<SelectorOption>,
    // Posición de la opción elegida, o null si no hay ninguna.
    selectedIndex: Int?,
    // Avisa la posición de la opción tocada.
    onSelect: (index: Int) -> Unit,
    // Modificador externo para posicionar el selector.
    modifier: Modifier = Modifier,
    // Etiqueta opcional encima de las opciones.
    label: String? = null,
    // Si es true las opciones se reparten el ancho por igual.
    fillWidth: Boolean = false,
) {
    // Columna que apila la etiqueta y la fila de opciones.
    Column(modifier = modifier) {
        // Solo dibuja la etiqueta si se proporcionó.
        if (label != null) {
            // Etiqueta obligatoria con asterisco.
            FormLabel(label)
            // Separación entre la etiqueta y las opciones.
            Spacer(modifier = Modifier.height(6.dp))
        }
        // Fila de opciones agrupadas para accesibilidad.
        Row(
            // Agrupa las opciones para que TalkBack las trate como un único grupo de selección.
            modifier = Modifier.selectableGroup(),
            // Separación entre opciones.
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Crea un botón por opción conservando su posición.
            options.forEachIndexed { index, option ->
                // Indica si esta opción es la elegida.
                val selected = index == selectedIndex
                // Superficie tocable que actúa como botón de la opción.
                Surface(
                    // Avisa la posición al tocar.
                    onClick = { onSelect(index) },
                    modifier = Modifier
                        // Reparte el ancho solo cuando se pide fillWidth.
                        .then(if (fillWidth) Modifier.weight(1f) else Modifier)
                        // Anuncia la opción como botón de opción con su estado de selección.
                        .semantics {
                            role = Role.RadioButton
                            this.selected = selected
                        },
                    // Esquinas ligeramente redondeadas.
                    shape = RoundedCornerShape(10.dp),
                    // Fondo de chip seleccionado o fondo normal según el estado.
                    color = if (selected) AppTheme.colors.chipSelected else AppTheme.colors.background,
                    // Contorno con el color de borde del tema.
                    border = BorderStroke(1.dp, AppTheme.colors.border),
                ) {
                    // Si la opción trae icono se dibuja icono + texto en fila.
                    if (option.icon != null) {
                        // Fila centrada con el icono y el texto.
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Icono decorativo de la opción.
                            Icon(option.icon, contentDescription = null, tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                            // Texto de la opción en una sola línea.
                            Text(option.label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AppTheme.colors.textPrimary, maxLines = 1)
                        }
                    } else {
                        // Opción sin icono: solo el texto con su margen interno.
                        Text(
                            text = option.label,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontSize = 13.sp,
                            color = AppTheme.colors.textPrimary,
                        )
                    }
                }
            }
        }
    }
}

// Vista previa con un selector de texto y otro con iconos a todo el ancho.
@Preview(showBackground = true)
@Composable
private fun OptionSelectorPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Apila los dos ejemplos.
        Column {
            // Selector de tamaño con etiqueta y "Mediano" elegido.
            OptionSelector(
                label = "Tamaño aproximado",
                options = listOf("Pequeño", "Mediano", "Grande").map { SelectorOption(it) },
                selectedIndex = 1,
                onSelect = {},
            )
            // Selector de ubicación con iconos que ocupa todo el ancho.
            OptionSelector(
                options = listOf(
                    SelectorOption("Ubicación actual", Icons.Outlined.MyLocation),
                    SelectorOption("Seleccionar mapa", Icons.Outlined.Map),
                ),
                selectedIndex = 0,
                onSelect = {},
                fillWidth = true,
            )
        }
    }
}
