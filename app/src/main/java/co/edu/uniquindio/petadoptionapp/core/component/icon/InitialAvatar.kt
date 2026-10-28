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
Es decorativo: el nombre completo aparece justo al lado, así que TalkBack
no necesita leer la letra suelta.
 */
// Desde este tamaño se usa la letra grande del perfil.
private val LargeAvatarSize = 56.dp

@Composable
fun InitialAvatar(
    name: String,
    size: Dp,
    modifier: Modifier = Modifier,
    // Fondo del círculo; sobre una barra marrón conviene otro color para que se distinga.
    containerColor: Color = AppTheme.colors.brand,
    contentColor: Color = AppTheme.colors.textOnDark,
) {
    val initial = name.trim().firstOrNull()?.uppercase() ?: "?"

    Box(
        modifier = modifier
            .size(size)
            .background(containerColor, CircleShape)
            .border(3.dp, AppTheme.colors.surface, CircleShape)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            // La letra crece con el avatar: grande en el perfil, pequeña en los comentarios.
            style = if (size < LargeAvatarSize) MaterialTheme.typography.titleSmall
            else MaterialTheme.typography.headlineMedium,
            color = contentColor,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InitialAvatarPreview() {
    PetAdoptionAppTheme {
        InitialAvatar(name = "Nicolás C.S", size = 88.dp)
    }
}