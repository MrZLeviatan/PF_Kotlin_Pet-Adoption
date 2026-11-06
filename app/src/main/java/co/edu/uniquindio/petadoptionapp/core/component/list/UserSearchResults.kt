package co.edu.uniquindio.petadoptionapp.core.component.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.text.StatusMessage
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import co.edu.uniquindio.petadoptionapp.domain.model.users.profile.UserSummary
import coil3.compose.AsyncImage

/**
 Resultados de buscar personas con "@" (inicio de usuario y de moderador): foto y alias; al tocar, abre su perfil.
 Sin resultados muestra el aviso centrado en el espacio que le den ([modifier], normalmente un weight).
 Se usa en HomeUserScreen y ModeratorHomeScreen en lugar del feed cuando la
 búsqueda empieza por "@".
**/
@Composable
fun UserSearchResults(
    // Lista de usuarios encontrados por la búsqueda.
    users: List<UserSummary>,
    // Acción al tocar un usuario; recibe su id para navegar a su perfil.
    onUserClick: (String) -> Unit,
    // Modificador externo; suele traer un weight para ocupar el espacio restante.
    modifier: Modifier = Modifier,
    // Margen lateral de la lista.
    horizontalPadding: Dp = 16.dp,
) {
    // Si no hay coincidencias se muestra un mensaje en lugar de una lista vacía.
    if (users.isEmpty()) {
        // Caja que centra el mensaje en el espacio asignado.
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            // Mensaje de "sin resultados" tomado de los recursos de texto.
            StatusMessage(text = stringResource(R.string.home_users_empty))
        }
        // Termina aquí para no componer la lista.
        return
    }
    // Lista perezosa: solo compone las filas visibles, eficiente con muchos resultados.
    LazyColumn(
        modifier = modifier,
        // Margen interno alrededor del contenido de la lista.
        contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 12.dp),
        // Separación vertical entre filas.
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Una fila por usuario; la clave estable (id) evita recomponer filas al cambiar la lista.
        items(users, key = { it.id }) { user ->
            // Fila tocable con la foto y el alias.
            Row(
                modifier = Modifier
                    // La fila ocupa todo el ancho para ampliar el área táctil.
                    .fillMaxWidth()
                    // Al tocar, notifica el id del usuario seleccionado.
                    .clickable { onUserClick(user.id) }
                    // Margen vertical interno de la fila.
                    .padding(vertical = 8.dp),
                // Centra foto y texto verticalmente.
                verticalAlignment = Alignment.CenterVertically,
                // Separación entre la foto y el alias.
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Foto de perfil cargada de forma asíncrona con Coil.
                AsyncImage(
                    // URL de la foto del usuario.
                    model = user.photoUrl,
                    // Descripción accesible que incluye el alias del usuario.
                    contentDescription = stringResource(R.string.home_user_photo_description, user.userName),
                    // Recorta la imagen para llenar el círculo sin deformarla.
                    contentScale = ContentScale.Crop,
                    // Color gris de relleno mientras se descarga la imagen (o si no hay foto).
                    placeholder = ColorPainter(Color.LightGray),
                    // Tamaño fijo y recorte circular.
                    modifier = Modifier.size(44.dp).clip(CircleShape)
                )
                // Alias del usuario precedido de "@".
                Text(
                    text = "@${user.userName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = AppTheme.colors.textPrimary
                )
            }
        }
    }
}

// Vista previa con un único resultado sin foto.
@Preview(showBackground = true)
@Composable
private fun UserSearchResultsPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Lista de ejemplo con un usuario.
        UserSearchResults(users = listOf(UserSummary("u2", "luis_9", null)), onUserClick = {})
    }
}