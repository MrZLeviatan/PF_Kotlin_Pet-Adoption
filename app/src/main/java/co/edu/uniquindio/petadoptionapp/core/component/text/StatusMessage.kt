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
 Mensaje centrado para listas vacías o con error ("Aún no tienes notificaciones").
 Con [isError] usa el color de error. La posición la decide quien lo usa con [modifier].
 */
@Composable
fun StatusMessage(
    text: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = if (isError) AppTheme.colors.stateError else AppTheme.colors.textPrimary,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun StatusMessagePreview() {
    PetAdoptionAppTheme {
        StatusMessage(
            text = "Aún no tienes publicaciones.",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp),
        )
    }
}
