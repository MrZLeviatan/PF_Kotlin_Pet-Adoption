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

/** Una opción del selector: texto y, si se quiere, un icono a la izquierda. */
data class SelectorOption(val label: String, val icon: ImageVector? = null)

/**
 Fila de opciones de selección única con forma de botón ("Pequeño / Mediano / Grande").
 [label] opcional va arriba con asterisco. Con [fillWidth] las opciones se reparten
 todo el ancho (útil para dos opciones con icono, como "Ubicación actual / Seleccionar mapa").
 TalkBack las anuncia como botones de opción.
 */
@Composable
fun OptionSelector(
    options: List<SelectorOption>,
    selectedIndex: Int?,
    onSelect: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    fillWidth: Boolean = false,
) {
    Column(modifier = modifier) {
        if (label != null) {
            FormLabel(label)
            Spacer(modifier = Modifier.height(6.dp))
        }
        Row(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEachIndexed { index, option ->
                val selected = index == selectedIndex
                Surface(
                    onClick = { onSelect(index) },
                    modifier = Modifier
                        .then(if (fillWidth) Modifier.weight(1f) else Modifier)
                        .semantics {
                            role = Role.RadioButton
                            this.selected = selected
                        },
                    shape = RoundedCornerShape(10.dp),
                    color = if (selected) AppTheme.colors.chipSelected else AppTheme.colors.background,
                    border = BorderStroke(1.dp, AppTheme.colors.border),
                ) {
                    if (option.icon != null) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(option.icon, contentDescription = null, tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                            Text(option.label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AppTheme.colors.textPrimary, maxLines = 1)
                        }
                    } else {
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

@Preview(showBackground = true)
@Composable
private fun OptionSelectorPreview() {
    PetAdoptionAppTheme {
        Column {
            OptionSelector(
                label = "Tamaño aproximado",
                options = listOf("Pequeño", "Mediano", "Grande").map { SelectorOption(it) },
                selectedIndex = 1,
                onSelect = {},
            )
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
