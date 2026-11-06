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
Componente reutilizable de pestañas, usado en pantallas que dividen su contenido en secciones
(por ejemplo, "Todas", "Activas", "Pendientes" y "Finalizadas" en las publicaciones de la persona).
Dibuja los textos en una fila desplazable horizontalmente; la pestaña activa se marca con texto en negrita y una
línea inferior de color, y toda la fila termina con un divisor. El estado (qué pestaña está activa) lo maneja la pantalla.
**/

/**
Fila de pestañas con scroll horizontal e indicador inferior en la seleccionada.
No conoce el contenido: recibe los textos y avisa qué índice se tocó.
**/
@Composable
fun AppTabRow(
    // Textos de cada pestaña, en orden.
    tabs: List<String>,
    // Índice de la pestaña seleccionada.
    selectedIndex: Int,
    // Acción que recibe el índice de la pestaña tocada.
    onTabSelected: (Int) -> Unit,
    // Modificador para el contenedor.
    modifier: Modifier = Modifier,
) {
    // Columna que apila la fila de pestañas y el divisor inferior, ocupando todo el ancho.
    Column(modifier = modifier.fillMaxWidth()) {
        // Fila desplazable horizontalmente con las pestañas.
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            // Dibuja una pestaña por cada texto.
            tabs.forEachIndexed { index, label ->
                // Indica si esta pestaña es la seleccionada.
                val selected = index == selectedIndex

                // Contenedor de la pestaña: área táctil, texto e indicador inferior.
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
                    // Centra el texto dentro de la pestaña.
                    contentAlignment = Alignment.Center,
                ) {
                    // Texto de la pestaña.
                    Text(
                        // Texto a mostrar.
                        text = label,
                        // Tipografía de etiqueta grande.
                        style = MaterialTheme.typography.labelLarge,
                        // El peso distingue la pestaña activa sin depender solo del color.
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        // Color de texto principal del tema.
                        color = AppTheme.colors.textPrimary,
                        // Espaciado lateral que define el ancho de la pestaña.
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    // Indicador inferior: línea de 2 dp visible solo en la pestaña seleccionada.
                    Box(
                        modifier = Modifier
                            // Se ubica en la parte inferior central de la pestaña.
                            .align(Alignment.BottomCenter)
                            // Ocupa todo el ancho de la pestaña.
                            .fillMaxWidth()
                            // Grosor de la línea.
                            .height(2.dp)
                            // Color de selección si está activa; transparente si no.
                            .background(
                                if (selected) AppTheme.colors.stateSelected else Color.Transparent
                            ),
                    )
                }
            }
        }
        // Línea divisoria bajo toda la fila de pestañas.
        HorizontalDivider(color = AppTheme.colors.border)
    }
}

// Vista previa en Android Studio con cuatro pestañas y la primera seleccionada.
@Preview(showBackground = true)
@Composable
private fun AppTabRowPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Fila de pestañas de ejemplo.
        AppTabRow(
            tabs = listOf("Todas (3)", "Activas (2)", "Pendientes (1)", "Finalizadas (0)"),
            selectedIndex = 0,
            onTabSelected = {},
        )
    }
}
