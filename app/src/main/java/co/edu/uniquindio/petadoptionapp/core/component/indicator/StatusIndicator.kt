package co.edu.uniquindio.petadoptionapp.core.component.indicator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
 Píldora con un punto de color (estado de una publicación, tipo de métrica).
 Se usa en tarjetas de publicación, reseñas del moderador y tarjetas de
 estadísticas; quien la invoca elige el color según el estado (por ejemplo
 stateError, stateWarning o stateSuccess del tema).
 Es decorativa: el texto de al lado ya dice lo mismo, así que TalkBack la ignora.
**/
@Composable
fun StatusIndicator(color: Color, modifier: Modifier = Modifier) {
    // Contenedor exterior con forma de píldora.
    Box(
        modifier = modifier
            // Tamaño fijo y achatado para lograr la forma de píldora.
            .size(width = 24.dp, height = 14.dp)
            // Contorno fino con el color de borde del tema.
            .border(1.dp, AppTheme.colors.border, CircleShape)
            // Quita la semántica para que TalkBack no la anuncie.
            .clearAndSetSemantics {},
        // Centra el punto dentro de la píldora.
        contentAlignment = Alignment.Center,
    ) {
        // Punto interior que comunica el estado mediante su color.
        Box(
            modifier = Modifier
                // Diámetro del punto.
                .size(8.dp)
                // Rellena el punto con el color recibido.
                .background(color, CircleShape),
        )
    }
}

// Vista previa del indicador con el color de error.
@Preview(showBackground = true)
@Composable
private fun StatusIndicatorPreview() {
    // Aplica el tema y muestra el indicador en estado de error.
    PetAdoptionAppTheme { StatusIndicator(color = AppTheme.colors.stateError) }
}
