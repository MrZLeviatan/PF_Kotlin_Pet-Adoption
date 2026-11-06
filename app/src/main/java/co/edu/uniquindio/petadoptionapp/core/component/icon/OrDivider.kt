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

/**
Separador horizontal con un texto en el centro (por ejemplo "o"). Se usa en
pantallas de autenticación para separar dos alternativas, como iniciar sesión
con correo o registrarse. Las líneas usan el color de borde del tema y el texto
el color principal, por lo que se adapta a modo claro y oscuro.
**/
// Separador con texto central ("o") para alternar entre dos acciones.
@Composable
fun OrDivider(
    // Texto que se mostrará en el centro del divisor (ej. "o")
    text: String,
    // Modificador externo para ajustar márgenes desde la pantalla que lo usa.
    modifier: Modifier = Modifier,
) {
    // Organiza los elementos en una fila horizontal que abarca todo el ancho
    Row(
        modifier = modifier.fillMaxWidth(),
        // Alinea líneas y texto en su centro vertical.
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // `weight(1f)`: cada línea ocupa la mitad del espacio restante.
        HorizontalDivider(modifier = Modifier.weight(1f), color = AppTheme.colors.border)
        // Muestra el texto central (ej. "o") con espaciado horizontal y estilo de cuerpo
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.textPrimary,
            // Separa el texto de ambas líneas.
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        // Dibuja la línea divisoria derecha con el color de borde del tema
        HorizontalDivider(modifier = Modifier.weight(1f), color = AppTheme.colors.border)
    }
}
