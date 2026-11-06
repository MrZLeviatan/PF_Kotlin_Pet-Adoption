package co.edu.uniquindio.petadoptionapp.core.component.text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Componente que muestra una frase normal seguida de un enlace en la misma línea, centrados.
Se usa al pie de pantallas como Login y Registro para navegar entre ellas ("¿No tienes cuenta? Crear cuenta").
Reutiliza [LinkText] para que el enlace tenga el estilo y el área táctil estándar de la app.
**/

/**
Frase seguida de un enlace en la misma línea ("¿No tienes cuenta? Crear cuenta").
**/
@Composable
fun TextWithLink(
    // Frase que precede al enlace.
    text: String,
    // Texto del enlace.
    linkText: String,
    // Acción al tocar el enlace.
    onLinkClick: () -> Unit,
    // Modificador para posicionar la fila.
    modifier: Modifier = Modifier,
) {
    // Fila con la frase y el enlace.
    Row(
        // Aplica el modificador recibido.
        modifier = modifier,
        // Centra verticalmente frase y enlace.
        verticalAlignment = Alignment.CenterVertically,
        // Separa los elementos 8 dp y los centra horizontalmente.
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        // Frase normal.
        Text(
            // Texto recibido.
            text = text,
            // Tipografía mediana del cuerpo.
            style = MaterialTheme.typography.bodyMedium,
            // Color de texto principal.
            color = AppTheme.colors.textPrimary,
        )
        // Enlace tocable con el estilo estándar.
        LinkText(text = linkText, onClick = onLinkClick)
    }
}

// Vista previa en Android Studio con la frase de la pantalla de Login.
@Preview(showBackground = true)
@Composable
private fun TextWithLinkPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Frase y enlace de ejemplo.
        TextWithLink(text = "¿No tienes cuenta?", linkText = "Crear cuenta", onLinkClick = {})
    }
}
