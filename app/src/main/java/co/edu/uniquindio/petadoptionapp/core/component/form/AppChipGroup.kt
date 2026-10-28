package co.edu.uniquindio.petadoptionapp.core.component.form

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Grupo de chips que bajan de línea cuando no caben (a diferencia de AppChipRow, que hace scroll).
Selección única: `selectedIndex = null` significa que no hay ninguna elegida.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppChipGroup(
    labels: List<String>,
    selectedIndex: Int?,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        labels.forEachIndexed { index, label ->
            val selected = index == selectedIndex

            // El Box exterior da el área táctil de 48 dp; el chip visual es más bajo.
            Box(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = { onSelected(index) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    // El peso distingue el chip activo sin depender solo del color.
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier
                        .background(
                            if (selected) AppTheme.colors.chipSelected else AppTheme.colors.surface,
                            CircleShape,
                        )
                        .border(1.dp, AppTheme.colors.border, CircleShape)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppChipGroupPreview() {
    PetAdoptionAppTheme {
        AppChipGroup(
            labels = listOf("Información insuficiente", "Imagen no válida", "Contenido sospechoso"),
            selectedIndex = 2,
            onSelected = {},
        )
    }
}