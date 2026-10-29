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
 */
@Composable
fun UserSearchResults(
    users: List<UserSummary>,
    onUserClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 16.dp,
) {
    if (users.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            StatusMessage(text = stringResource(R.string.home_users_empty))
        }
        return
    }
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(users, key = { it.id }) { user ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUserClick(user.id) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AsyncImage(
                    model = user.photoUrl,
                    contentDescription = stringResource(R.string.home_user_photo_description, user.userName),
                    contentScale = ContentScale.Crop,
                    placeholder = ColorPainter(Color.LightGray),
                    modifier = Modifier.size(44.dp).clip(CircleShape)
                )
                Text(
                    text = "@${user.userName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = AppTheme.colors.textPrimary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserSearchResultsPreview() {
    PetAdoptionAppTheme {
        UserSearchResults(users = listOf(UserSummary("u2", "luis_9", null)), onUserClick = {})
    }
}
