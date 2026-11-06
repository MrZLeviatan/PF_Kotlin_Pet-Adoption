package co.edu.uniquindio.petadoptionapp.core.component.text

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme

/**
Componente de texto para mostrar el error general de un formulario (por ejemplo, en Login o Registro),
debajo de los campos y encima del botón principal. Usa el color de error del tema, va centrado y se declara como
región viva de accesibilidad para que los lectores de pantalla lo anuncien en cuanto aparece.
**/

/**
Error general de un formulario ("Correo o contraseña incorrectos").
Es una región viva: TalkBack lo anuncia apenas aparece, sin mover el foco.
**/
@Composable
fun ErrorText(
    // Mensaje de error a mostrar.
    text: String,
    // Modificador para posicionar el texto desde la pantalla.
    modifier: Modifier = Modifier,
) {
    // Texto del error.
    Text(
        // Mensaje recibido.
        text = text,
        // Tipografía pequeña del cuerpo.
        style = MaterialTheme.typography.bodySmall,
        // Color de error del tema (rojo).
        color = AppTheme.colors.stateError,
        // Centra el texto.
        textAlign = TextAlign.Center,
        // Región viva "educada": TalkBack la anuncia al terminar lo que esté leyendo.
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
    )
}

// Vista previa en Android Studio con un mensaje de error de ejemplo.
@Preview(showBackground = true)
@Composable
private fun ErrorTextPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Texto de error de ejemplo.
        ErrorText(text = "Correo o contraseña incorrectos.")
    }
}
