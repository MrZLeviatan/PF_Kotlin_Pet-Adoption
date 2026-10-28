package co.edu.uniquindio.petadoptionapp.core.component.text

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
 Enlace de texto reutilizable
 Centraliza la regla visual de los links: Rubik SemiBold, subrayado y color
 del tema. Las pantallas no deciden cómo se ve un link, solo qué dice y qué hace.
 */
@Composable
fun LinkText(
    // Parámetro obligatorio: el texto que mostrará el enlace
    text: String,
    // Parámetro obligatorio: la acción que se ejecutará al hacer clic en el enlace
    onClick: () -> Unit,
    // Parámetro opcional: modificadores externos para personalizar el diseño desde fuera
    modifier: Modifier = Modifier,
) {

    Box(
        modifier = modifier
            // 48 dp: mínimo recomendado de accesibilidad para elementos tocables.
            .heightIn(min = 48.dp)
            .widthIn(min = 48.dp)
            // Role.Button: TalkBack lo anuncia como acción tocable.
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            // Asigna el texto que se recibió por parámetro
            text = text,
            // Solo se sube el peso aquí, para no afectar otros componentes que usan labelMedium.
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                // El color solo no basta para identificar un link (daltonismo).
                textDecoration = TextDecoration.Underline,
            ),
            color = AppTheme.colors.textLink,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LinkTextPreview() {
    PetAdoptionAppTheme {
        LinkText(text = "¿Olvidaste tu contraseña?", onClick = {})
    }
}