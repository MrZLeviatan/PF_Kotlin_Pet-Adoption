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
 Es decorativa: el texto de al lado ya dice lo mismo, así que TalkBack la ignora.
 */
@Composable
fun StatusIndicator(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 24.dp, height = 14.dp)
            .border(1.dp, AppTheme.colors.border, CircleShape)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatusIndicatorPreview() {
    PetAdoptionAppTheme { StatusIndicator(color = AppTheme.colors.stateError) }
}
