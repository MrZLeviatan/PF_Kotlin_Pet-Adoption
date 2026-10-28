package co.edu.uniquindio.petadoptionapp.core.component.icon

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil3.compose.SubcomposeAsyncImage

// Foto de perfil circular; si no hay foto (o falla la carga) muestra la inicial.
@Composable
fun UserAvatar(
    name: String,
    photoUrl: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    containerColor: Color = AppTheme.colors.brand,
    contentColor: Color = AppTheme.colors.textOnDark,
) {
    if (photoUrl.isNullOrBlank()) {
        InitialAvatar(name = name, size = size, modifier = modifier, containerColor = containerColor, contentColor = contentColor)
    } else {
        SubcomposeAsyncImage(
            model = photoUrl,
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = modifier.size(size).clip(CircleShape),
            loading = { InitialAvatar(name = name, size = size, containerColor = containerColor, contentColor = contentColor) },
            error = { InitialAvatar(name = name, size = size, containerColor = containerColor, contentColor = contentColor) },
        )
    }
}