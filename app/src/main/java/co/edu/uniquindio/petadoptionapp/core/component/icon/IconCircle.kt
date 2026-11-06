package co.edu.uniquindio.petadoptionapp.core.component.icon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Círculo con un icono cualquiera. Es la versión genérica de [PawCircle]:
recibe el icono, así sirve para escudo, candado, correo, etc.
Se usa como ilustración en cabeceras (IconHeader en recuperar y cambiar
contraseña), en ajustes, filas de información e insignias del perfil, y toma
sus colores del tema (AppTheme).
El icono es decorativo: el texto que lo acompaña ya describe la pantalla.
**/
@Composable
fun IconCircle(
    // Icono vectorial que se dibuja en el centro del círculo.
    icon: ImageVector,
    // Diámetro del círculo; el icono se escala en proporción a este valor.
    size: Dp,
    // Color de fondo del círculo, para adaptarlo a la superficie sobre la que se ubica.
    containerColor: Color,
    // Modificador externo para posicionar el componente desde quien lo usa.
    modifier: Modifier = Modifier,
) {
    // Caja contenedora que actúa como el círculo de fondo.
    Box(
        modifier = modifier
            // Fija el ancho y el alto al diámetro recibido.
            .size(size)
            // Pinta el fondo con forma circular.
            .background(containerColor, CircleShape),
        // Centra el icono dentro del círculo.
        contentAlignment = Alignment.Center,
    ) {
        // Dibuja el icono recibido.
        Icon(
            // Vector del icono a mostrar.
            imageVector = icon,
            // Sin descripción porque es decorativo y TalkBack debe ignorarlo.
            contentDescription = null,
            // Usa el color de texto principal del tema para mantener contraste.
            tint = AppTheme.colors.textPrimary,
            // El icono ocupa la mitad del círculo para dejar margen visual.
            modifier = Modifier.size(size * 0.5f),
        )
    }
}

// Vista previa en Android Studio del círculo con el icono de escudo.
@Preview(showBackground = true)
@Composable
private fun IconCirclePreview() {
    // Envuelve el componente en el tema de la app para que use sus colores.
    PetAdoptionAppTheme {
        // Ejemplo con icono de seguridad, 72 dp y fondo de etiqueta.
        IconCircle(
            icon = Icons.Outlined.Security,
            size = 72.dp,
            containerColor = AppTheme.colors.surfaceTag,
        )
    }
}
