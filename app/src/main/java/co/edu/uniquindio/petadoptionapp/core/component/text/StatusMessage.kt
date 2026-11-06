package co.edu.uniquindio.petadoptionapp.core.component.text

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Componente de texto para estados de una lista o pantalla: lista vacía ("Aún no tienes publicaciones") o error
al cargar. Se usa en notificaciones, publicaciones, chats, etc. Solo define el aspecto (texto centrado y color
normal o de error); dónde y con qué márgenes aparece lo decide la pantalla mediante el modificador.
**/

/**
Mensaje centrado para listas vacías o con error ("Aún no tienes notificaciones").
Con [isError] usa el color de error. La posición la decide quien lo usa con [modifier].
**/
@Composable
fun StatusMessage(
    // Mensaje a mostrar.
    text: String,
    // Modificador para posicionar el mensaje.
    modifier: Modifier = Modifier,
    // Indica si el mensaje es un error (cambia el color).
    isError: Boolean = false,
) {
    // Texto del mensaje.
    Text(
        // Mensaje recibido.
        text = text,
        // Tipografía mediana del cuerpo.
        style = MaterialTheme.typography.bodyMedium,
        // Rojo de error si es un error; si no, el color de texto principal.
        color = if (isError) AppTheme.colors.stateError else AppTheme.colors.textPrimary,
        // Centra el texto.
        textAlign = TextAlign.Center,
        // Aplica el modificador recibido.
        modifier = modifier,
    )
}

// Vista previa en Android Studio de un mensaje de lista vacía.
@Preview(showBackground = true)
@Composable
private fun StatusMessagePreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Mensaje de ejemplo a todo el ancho con margen vertical.
        StatusMessage(
            text = "Aún no tienes publicaciones.",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp),
        )
    }
}
