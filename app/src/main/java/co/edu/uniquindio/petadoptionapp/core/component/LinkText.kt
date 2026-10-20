package co.edu.uniquindio.petadoptionapp.core.component

import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.heightIn
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme

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
    Text(
        // Asigna el texto que se recibió por parámetro
        text = text,
        // Parte de labelMedium (Rubik) y solo sube el peso aquí. Así no se altera NavigationBar ni otros componentes que usan ese rol.
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.SemiBold,
            // Subrayado: el color solo no basta para identificar un link.
            textDecoration = TextDecoration.Underline,
        ),
        // Asigna el color específico para los hipervínculos
        color = AppTheme.colors.textLink,
        modifier = modifier
            // Área táctil mínima recomendada de 48dp: un texto de una línea mide ~20dp y sería difícil de tocar.
            .heightIn(min = 48.dp)
            // `Role.Button` hace que TalkBack lo anuncie como botón/acción.
            .clickable(role = Role.Button, onClick = onClick),
    )
}