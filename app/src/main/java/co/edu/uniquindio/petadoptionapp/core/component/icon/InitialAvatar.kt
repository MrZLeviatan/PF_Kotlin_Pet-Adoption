package co.edu.uniquindio.petadoptionapp.core.component.icon
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Avatar circular con la inicial del nombre.
Se usa cuando el usuario no tiene foto de perfil (directamente o a través de
[UserAvatar]) en perfiles, comentarios, chats y resultados de búsqueda.
Es decorativo: el nombre completo aparece justo al lado, así que TalkBack
no necesita leer la letra suelta.
**/
// Desde este tamaño se usa la letra grande del perfil.
private val LargeAvatarSize = 56.dp

/**
Dibuja el círculo con la inicial en mayúscula. El estilo de la letra se elige
según el tamaño del avatar para que siempre quede proporcionada.
**/
@Composable
fun InitialAvatar(
    // Nombre del usuario del que se toma la primera letra.
    name: String,
    // Diámetro del avatar.
    size: Dp,
    // Modificador externo para posicionar el avatar.
    modifier: Modifier = Modifier,
    // Fondo del círculo; sobre una barra marrón conviene otro color para que se distinga.
    containerColor: Color = AppTheme.colors.brand,
    // Color de la letra; por defecto claro para contrastar con el fondo de marca.
    contentColor: Color = AppTheme.colors.textOnDark,
) {
    // Obtiene la primera letra del nombre (sin espacios iniciales) en mayúscula, o "?" si el nombre está vacío.
    val initial = name.trim().firstOrNull()?.uppercase() ?: "?"

    // Caja circular que contiene la inicial.
    Box(
        modifier = modifier
            // Fija el tamaño del avatar.
            .size(size)
            // Pinta el fondo circular.
            .background(containerColor, CircleShape)
            // Agrega un aro del color de superficie que separa el avatar del fondo.
            .border(3.dp, AppTheme.colors.surface, CircleShape)
            // Elimina la semántica para que TalkBack no lea la letra suelta.
            .clearAndSetSemantics {},
        // Centra la letra en el círculo.
        contentAlignment = Alignment.Center,
    ) {
        // Muestra la inicial calculada.
        Text(
            // Letra a mostrar.
            text = initial,
            // La letra crece con el avatar: grande en el perfil, pequeña en los comentarios.
            style = if (size < LargeAvatarSize) MaterialTheme.typography.titleSmall
            else MaterialTheme.typography.headlineMedium,
            // Color de la letra recibido por parámetro.
            color = contentColor,
        )
    }
}

// Vista previa del avatar en tamaño de perfil.
@Preview(showBackground = true)
@Composable
private fun InitialAvatarPreview() {
    // Aplica el tema de la app al ejemplo.
    PetAdoptionAppTheme {
        // Ejemplo con un nombre real y 88 dp, que usa la tipografía grande.
        InitialAvatar(name = "Nicolás C.S", size = 88.dp)
    }
}
