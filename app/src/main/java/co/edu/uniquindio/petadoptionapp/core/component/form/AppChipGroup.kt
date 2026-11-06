package co.edu.uniquindio.petadoptionapp.core.component.form

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
[leading] dibuja algo antes del texto de cada chip (por ejemplo, el ícono de la categoría).
Se usa en el diálogo de filtros de exploración y en la pantalla de rechazo de
publicaciones; los colores salen de los roles chipSelected, surface y border del tema.
**/
// FlowRow todavía es una API experimental de Compose, por eso se requiere el opt-in.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppChipGroup(
    // Textos de cada chip, en el orden en que se muestran.
    labels: List<String>,
    // Posición del chip seleccionado, o null si no hay ninguno.
    selectedIndex: Int?,
    // Avisa la posición del chip que el usuario tocó.
    onSelected: (Int) -> Unit,
    // Modificador externo para posicionar el grupo.
    modifier: Modifier = Modifier,
    // Contenido opcional antes del texto de cada chip; recibe el índice para personalizarlo.
    leading: (@Composable (index: Int) -> Unit)? = null,
) {
    // Fila que salta a la siguiente línea cuando los chips no caben a lo ancho.
    FlowRow(
        modifier = modifier,
        // Separación horizontal entre chips.
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Recorre las etiquetas con su posición para construir cada chip.
        labels.forEachIndexed { index, label ->
            // Determina si este chip es el seleccionado actualmente.
            val selected = index == selectedIndex

            // El Box exterior da el área táctil de 48 dp; el chip visual es más bajo.
            Box(
                modifier = Modifier
                    // Garantiza la altura táctil mínima recomendada por accesibilidad.
                    .heightIn(min = 48.dp)
                    // Hace el chip seleccionable y lo anuncia a TalkBack como botón de opción.
                    .selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = { onSelected(index) },
                    ),
                // Centra el chip visual dentro del área táctil.
                contentAlignment = Alignment.Center,
            ) {
                // Chip visual: píldora con el contenido opcional y el texto.
                Row(
                    modifier = Modifier
                        // Fondo de chip seleccionado o de superficie normal según el estado.
                        .background(
                            if (selected) AppTheme.colors.chipSelected else AppTheme.colors.surface,
                            CircleShape,
                        )
                        // Contorno con el color de borde del tema.
                        .border(1.dp, AppTheme.colors.border, CircleShape)
                        // Con contenido inicial se reduce el margen izquierdo para que el ícono quede equilibrado.
                        .padding(start = if (leading != null) 6.dp else 14.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
                    // Centra verticalmente el ícono y el texto.
                    verticalAlignment = Alignment.CenterVertically,
                    // Separación entre el contenido inicial y el texto.
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    // Dibuja el contenido inicial solo si se proporcionó.
                    leading?.invoke(index)
                    // Texto del chip.
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        // El peso distingue el chip activo sin depender solo del color.
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = AppTheme.colors.textPrimary,
                        // Pequeño margen vertical para que el texto respire dentro de la píldora.
                        modifier = Modifier.padding(vertical = 2.dp),
                    )
                }
            }
        }
    }
}

// Vista previa con motivos de rechazo y el tercero seleccionado.
@Preview(showBackground = true)
@Composable
private fun AppChipGroupPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Grupo de ejemplo con tres opciones.
        AppChipGroup(
            labels = listOf("Información insuficiente", "Imagen no válida", "Contenido sospechoso"),
            selectedIndex = 2,
            onSelected = {},
        )
    }
}