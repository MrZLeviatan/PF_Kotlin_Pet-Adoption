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

/** Frase seguida de un enlace en la misma línea ("¿No tienes cuenta? Crear cuenta"). */
@Composable
fun TextWithLink(
    text: String,
    linkText: String,
    onLinkClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.textPrimary,
        )
        LinkText(text = linkText, onClick = onLinkClick)
    }
}

@Preview(showBackground = true)
@Composable
private fun TextWithLinkPreview() {
    PetAdoptionAppTheme {
        TextWithLink(text = "¿No tienes cuenta?", linkText = "Crear cuenta", onLinkClick = {})
    }
}
