package co.edu.uniquindio.petadoptionapp.core.component.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Fila de pestañas con scroll horizontal e indicador inferior en la seleccionada.
No conoce el contenido: recibe los textos y avisa qué índice se tocó.
 */
@Composable
fun AppTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            tabs.forEachIndexed { index, label ->
                val selected = index == selectedIndex

                Box(
                    modifier = Modifier
                        // 48 dp: mínimo recomendado de accesibilidad para elementos tocables.
                        .heightIn(min = 48.dp)
                        // Role.Tab: TalkBack anuncia "pestaña, seleccionada".
                        .selectable(
                            selected = selected,
                            role = Role.Tab,
                            onClick = { onTabSelected(index) },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        // El peso distingue la pestaña activa sin depender solo del color.
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = AppTheme.colors.textPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(
                                if (selected) AppTheme.colors.stateSelected else Color.Transparent
                            ),
                    )
                }
            }
        }
        HorizontalDivider(color = AppTheme.colors.border)
    }
}

@Preview(showBackground = true)
@Composable
private fun AppTabRowPreview() {
    PetAdoptionAppTheme {
        AppTabRow(
            tabs = listOf("Todas (3)", "Activas (2)", "Pendientes (1)", "Finalizadas (0)"),
            selectedIndex = 0,
            onTabSelected = {},
        )
    }
}