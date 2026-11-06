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

/**
Avatar de usuario reutilizable en perfiles, publicaciones, comentarios, chats y
listas de búsqueda. Carga la foto remota (por ejemplo, alojada en Cloudinary)
con Coil y, si no existe o no se puede descargar, recurre a [InitialAvatar]
para que siempre se muestre algo coherente con el tema.
**/
// Foto de perfil circular; si no hay foto (o falla la carga) muestra la inicial.
@Composable
fun UserAvatar(
    // Nombre del usuario: sirve para la inicial de respaldo y como descripción accesible de la foto.
    name: String,
    // URL de la foto de perfil; puede ser nula o vacía si el usuario no subió una.
    photoUrl: String?,
    // Diámetro del avatar.
    size: Dp,
    // Modificador externo para posicionar el avatar.
    modifier: Modifier = Modifier,
    // Fondo del avatar de iniciales cuando no hay foto.
    containerColor: Color = AppTheme.colors.brand,
    // Color de la letra del avatar de iniciales.
    contentColor: Color = AppTheme.colors.textOnDark,
) {
    // Si no hay URL de foto, se muestra directamente la inicial sin intentar ninguna descarga.
    if (photoUrl.isNullOrBlank()) {
        // Avatar de respaldo con la inicial del nombre.
        InitialAvatar(name = name, size = size, modifier = modifier, containerColor = containerColor, contentColor = contentColor)
    } else {
        // Carga la imagen de forma asíncrona con Coil, permitiendo composables propios para la carga y el error.
        SubcomposeAsyncImage(
            // URL de la imagen a descargar.
            model = photoUrl,
            // TalkBack anuncia el nombre del usuario al enfocar la foto.
            contentDescription = name,
            // Recorta la imagen para llenar el círculo sin deformarla.
            contentScale = ContentScale.Crop,
            // Aplica el tamaño y recorta en forma circular.
            modifier = modifier.size(size).clip(CircleShape),
            // Mientras se descarga, muestra la inicial para evitar un hueco vacío.
            loading = { InitialAvatar(name = name, size = size, containerColor = containerColor, contentColor = contentColor) },
            // Si la descarga falla, también recurre a la inicial.
            error = { InitialAvatar(name = name, size = size, containerColor = containerColor, contentColor = contentColor) },
        )
    }
}
