package co.edu.uniquindio.petadoptionapp.core.component.icon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme

/**
 Círculo con una huella. Recibe el color de fondo para poder usarse sobre cualquier superficie.
 Al ser público, puede ser reutilizado en cualquier pantalla de la aplicación.
 */
@Composable
fun PawCircle(
    // Tamaño dinámico que recibe el círculo (ancho y alto)
    size: Dp,
    // Color de fondo que tendrá el círculo
    containerColor: Color,
    // Modificador opcional externo
    modifier: Modifier = Modifier,
) {
    // Contenedor en forma de caja para dibujar un círculo perfecto con un icono en su interior
    Box(
        modifier = modifier
            .size(size)
            .background(containerColor, CircleShape),
        // Centra el icono exactamente en el medio de la caja
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Pets,
            contentDescription = null,
            tint = AppTheme.colors.textPrimary,
            modifier = Modifier.size(size * 0.55f),
        )
    }
}