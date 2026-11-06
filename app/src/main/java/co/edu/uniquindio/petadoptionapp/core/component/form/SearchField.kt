package co.edu.uniquindio.petadoptionapp.core.component.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
 Buscador en forma de píldora con la lupa a la izquierda (Inicio, Mensajes).
 El texto lo guarda quien lo usa (normalmente su ViewModel).
 [borderColor] en transparente lo deja sin borde, como en la lista de chats.
 Se usa en HomeUserScreen, ModeratorHomeScreen y ChatListScreen, con fondo y
 borde tomados del tema.
**/
@Composable
fun SearchField(
    // Texto de búsqueda actual.
    query: String,
    // Se invoca con el nuevo texto cada vez que el usuario escribe.
    onQueryChange: (String) -> Unit,
    // Texto guía del buscador vacío.
    placeholder: String,
    // Modificador externo para posicionar el buscador.
    modifier: Modifier = Modifier,
    // Descripción de la lupa para lectores de pantalla.
    iconDescription: String? = null,
    // Color del contorno; por defecto el del texto principal.
    borderColor: Color = AppTheme.colors.textPrimary,
) {
    // Campo con borde de Material 3 con forma de píldora.
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        // Ocupa todo el ancho disponible.
        modifier = modifier.fillMaxWidth(),
        // Texto guía mientras no hay búsqueda.
        placeholder = { Text(placeholder) },
        // Lupa a la izquierda.
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = iconDescription) },
        // Búsqueda en una sola línea.
        singleLine = true,
        // Bordes totalmente redondeados.
        shape = RoundedCornerShape(24.dp),
        // Colores: fondo de pantalla y el mismo borde con y sin foco.
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = AppTheme.colors.background,
            unfocusedContainerColor = AppTheme.colors.background,
            focusedBorderColor = borderColor,
            unfocusedBorderColor = borderColor,
        ),
    )
}

// Vista previa del buscador vacío.
@Preview(showBackground = true)
@Composable
private fun SearchFieldPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Buscador de ejemplo para la lista de chats.
        SearchField(query = "", onQueryChange = {}, placeholder = "Buscar chat")
    }
}
