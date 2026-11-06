package co.edu.uniquindio.petadoptionapp.core.component.header

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.component.icon.IconCircle
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Encabezado de pantallas de un solo propósito: icono, título y una línea de explicación.
Se usa en las pantallas de recuperar contraseña y de nueva contraseña. Combina
[IconCircle] con la tipografía titleLarge y bodyMedium del tema, todo centrado.
**/
@Composable
fun IconHeader(
    // Icono que ilustra el propósito de la pantalla.
    icon: ImageVector,
    // Título principal del encabezado.
    title: String,
    // Texto breve que explica qué debe hacer el usuario.
    subtitle: String,
    // Modificador externo para posicionar el encabezado.
    modifier: Modifier = Modifier,
) {
    // Columna que apila icono, título y subtítulo centrados horizontalmente.
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Círculo de 72 dp con el icono sobre la superficie de etiqueta.
        IconCircle(icon = icon, size = 72.dp, containerColor = AppTheme.colors.surfaceTag)
        // Separación entre el icono y el título.
        Spacer(Modifier.height(24.dp))
        // Título centrado con la tipografía de título grande.
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = AppTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        // Separación entre el título y el subtítulo.
        Spacer(Modifier.height(8.dp))
        // Subtítulo explicativo centrado con estilo de cuerpo.
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
    }
}

// Vista previa con el contenido de la pantalla de recuperar contraseña.
@Preview(showBackground = true)
@Composable
private fun IconHeaderPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Encabezado de ejemplo con icono de seguridad.
        IconHeader(
            icon = Icons.Outlined.Security,
            title = "Recuperar contraseña",
            subtitle = "Ingresa tu correo electrónico y te enviaremos un enlace.",
        )
    }
}
