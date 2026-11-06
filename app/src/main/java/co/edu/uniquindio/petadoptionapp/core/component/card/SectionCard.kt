package co.edu.uniquindio.petadoptionapp.core.component.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Tarjeta con encabezado y un contenido libre debajo, usada para agrupar bloques de una pantalla
(por ejemplo las secciones de Configuración como "Seguridad"). Se construye sobre [AppCard].
[subtitle] e [icon] son opcionales: con icono, el título va a su derecha ("Seguridad").
El icono es decorativo: el título ya nombra la sección.
**/
@Composable
fun SectionCard(
    // Título de la sección.
    title: String,
    // Modificador opcional para personalizar la tarjeta desde afuera.
    modifier: Modifier = Modifier,
    // Texto secundario opcional bajo el título.
    subtitle: String? = null,
    // Icono opcional a la izquierda del título.
    icon: ImageVector? = null,
    // Contenido libre de la sección.
    content: @Composable ColumnScope.() -> Unit,
) {
    // Tarjeta base que aporta forma, fondo y borde.
    AppCard(modifier = modifier) {
        // Fila del encabezado con icono opcional y título.
        Row(
            // heading: TalkBack permite saltar de sección en sección.
            modifier = Modifier.semantics { heading() },
            // Centra verticalmente icono y título.
            verticalAlignment = Alignment.CenterVertically,
            // Separación de 8 dp entre icono y título.
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Solo dibuja el icono si se proporcionó.
            if (icon != null) {
                // Icono decorativo con el color de marca.
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppTheme.colors.brand,
                    modifier = Modifier.size(18.dp),
                )
            }
            // Título de la sección.
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = AppTheme.colors.textPrimary,
            )
        }
        // Solo dibuja el subtítulo si se proporcionó.
        if (subtitle != null) {
            // Subtítulo en texto pequeño bajo el encabezado.
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.textPrimary,
            )
        }
        // Separación vertical entre el encabezado y el contenido.
        Spacer(Modifier.height(12.dp))
        // Dibuja el contenido proporcionado por la pantalla.
        content()
    }
}

// Vista previa en Android Studio de una sección con icono.
@Preview(showBackground = true)
@Composable
private fun SectionCardPreview() {
    // Envuelve el componente en el tema para que use los colores y tipografías reales.
    PetAdoptionAppTheme {
        // Sección de ejemplo "Seguridad" con un candado.
        SectionCard(title = "Seguridad", icon = Icons.Outlined.Lock) {
            // Texto de relleno que simula el contenido.
            Text("Contenido", color = AppTheme.colors.textPrimary)
        }
    }
}
