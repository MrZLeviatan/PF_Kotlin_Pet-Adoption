package co.edu.uniquindio.petadoptionapp.core.component.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
Componente reutilizable de navegación secundaria: una fila de chips (pastillas) para filtrar contenido,
por ejemplo en notificaciones ("Todas", "Sin leer", "Mensajes") o en listados con categorías.
Funciona con selección única: el chip activo se resalta con otro color de fondo y texto en negrita, y la fila
se desplaza horizontalmente si los chips no caben en el ancho de la pantalla.
**/

/**
Fila de chips de filtro con selección única y scroll horizontal.
No conoce el contenido: recibe los textos y avisa qué índice se tocó.
**/
@Composable
fun AppChipRow(
    // Textos de cada chip, en orden.
    labels: List<String>,
    // Índice del chip seleccionado actualmente.
    selectedIndex: Int,
    // Acción que recibe el índice del chip tocado.
    onSelected: (Int) -> Unit,
    // Modificador para el contenedor de la fila.
    modifier: Modifier = Modifier,
) {
    // Fila de chips.
    Row(
        // Permite desplazar la fila horizontalmente, recordando la posición del scroll.
        modifier = modifier.horizontalScroll(rememberScrollState()),
        // Deja 4 dp de separación entre chips.
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Dibuja un chip por cada texto.
        labels.forEachIndexed { index, label ->
            // Indica si este chip es el seleccionado.
            val selected = index == selectedIndex

            // El Box exterior da el área táctil de 48 dp; el chip visual es más bajo.
            Box(
                modifier = Modifier
                    // Altura mínima de 48 dp recomendada por accesibilidad.
                    .heightIn(min = 48.dp)
                    // Lo marca como elemento seleccionable con rol de pestaña para lectores de pantalla.
                    .selectable(
                        selected = selected,
                        role = Role.Tab,
                        onClick = { onSelected(index) },
                    ),
                // Centra el chip dentro del área táctil.
                contentAlignment = Alignment.Center,
            ) {
                // Texto del chip, que a la vez dibuja el fondo y el borde de la pastilla.
                Text(
                    // Texto a mostrar.
                    text = label,
                    // Tipografía de etiqueta mediana.
                    style = MaterialTheme.typography.labelMedium,
                    // El peso distingue el chip activo sin depender solo del color.
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    // Color de texto principal del tema.
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier
                        // Fondo en forma de pastilla, con color distinto si está seleccionado.
                        .background(
                            if (selected) AppTheme.colors.chipSelected
                            else AppTheme.colors.chipUnselected,
                            CircleShape,
                        )
                        // Borde fino con la misma forma.
                        .border(1.dp, AppTheme.colors.border, CircleShape)
                        // Espaciado interno del chip.
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                )
            }
        }
    }
}

// Vista previa en Android Studio con tres chips y el primero seleccionado.
@Preview(showBackground = true)
@Composable
private fun AppChipRowPreview() {
    // Aplica el tema de la app para ver colores y tipografías reales.
    PetAdoptionAppTheme {
        // Fila de ejemplo.
        AppChipRow(
            labels = listOf("Todas", "Sin leer", "Mensajes"),
            selectedIndex = 0,
            onSelected = {},
        )
    }
}
