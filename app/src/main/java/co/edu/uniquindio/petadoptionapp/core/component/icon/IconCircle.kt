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
El icono es decorativo: el texto que lo acompaña ya describe la pantalla.
 */
@Composable
fun IconCircle(
    icon: ImageVector,
    size: Dp,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(containerColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppTheme.colors.textPrimary,
            modifier = Modifier.size(size * 0.5f),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun IconCirclePreview() {
    PetAdoptionAppTheme {
        IconCircle(
            icon = Icons.Outlined.Security,
            size = 72.dp,
            containerColor = AppTheme.colors.surfaceTag,
        )
    }
}