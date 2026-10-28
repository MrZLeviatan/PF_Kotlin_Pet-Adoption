package co.edu.uniquindio.petadoptionapp.core.component.icon

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme

// Separador con texto central ("o") para alternar entre dos acciones.
@Composable
fun OrDivider(
    // Texto que se mostrará en el centro del divisor (ej. "o")
    text: String,
    modifier: Modifier = Modifier,
) {
    // Organiza los elementos en una fila horizontal que abarca todo el ancho
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // `weight(1f)`: cada línea ocupa la mitad del espacio restante.
        HorizontalDivider(modifier = Modifier.weight(1f), color = AppTheme.colors.border)
        // Muestra el texto central (ej. "o") con espaciado horizontal y estilo de cuerpo
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.textPrimary,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        // Dibuja la línea divisoria derecha con el color de borde del tema
        HorizontalDivider(modifier = Modifier.weight(1f), color = AppTheme.colors.border)
    }
}