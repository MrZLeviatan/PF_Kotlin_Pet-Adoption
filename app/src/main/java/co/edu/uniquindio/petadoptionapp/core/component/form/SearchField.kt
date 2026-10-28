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
 */
@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    // Descripción de la lupa para lectores de pantalla.
    iconDescription: String? = null,
    borderColor: Color = AppTheme.colors.textPrimary,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = iconDescription) },
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = AppTheme.colors.background,
            unfocusedContainerColor = AppTheme.colors.background,
            focusedBorderColor = borderColor,
            unfocusedBorderColor = borderColor,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun SearchFieldPreview() {
    PetAdoptionAppTheme {
        SearchField(query = "", onQueryChange = {}, placeholder = "Buscar chat")
    }
}
