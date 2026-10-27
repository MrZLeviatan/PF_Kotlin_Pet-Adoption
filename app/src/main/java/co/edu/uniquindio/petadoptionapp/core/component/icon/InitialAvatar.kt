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
@Composable
fun InitialAvatar(
    name: String,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val initial = name.trim().firstOrNull()?.uppercase() ?: "?"

    Box(
        modifier = modifier
            .size(size)
            .background(AppTheme.colors.brand, CircleShape)
            .border(3.dp, AppTheme.colors.surface, CircleShape)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.headlineMedium,
            color = AppTheme.colors.textOnDark,
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