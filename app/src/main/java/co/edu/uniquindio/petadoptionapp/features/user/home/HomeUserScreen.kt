package co.edu.uniquindio.petadoptionapp.features.user.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.form.SearchField
import co.edu.uniquindio.petadoptionapp.core.component.header.HomeHeader
import co.edu.uniquindio.petadoptionapp.core.component.list.UserSearchResults
import co.edu.uniquindio.petadoptionapp.core.component.text.IconTag
import co.edu.uniquindio.petadoptionapp.core.component.text.IconTextRow
import co.edu.uniquindio.petadoptionapp.core.component.text.StatusMessage
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import co.edu.uniquindio.petadoptionapp.domain.model.post.AnimalSize
import co.edu.uniquindio.petadoptionapp.domain.model.users.profile.UserSummary
import coil3.compose.AsyncImage

private val ScreenHorizontalPadding = 16.dp

/**
Pantalla de inicio del usuario.

Conecta el [HomeUserViewModel] con la UI: observa el estado, reenvía las acciones
y reacciona a los eventos de un solo uso. [HomeUserContent] solo dibuja.
 */
@Composable
fun HomeUserScreen(
    padding: PaddingValues = PaddingValues(),
    onPostClick: (String) -> Unit = {},
    onUserClick: (String) -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    viewModel: HomeUserViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val importantErrorMessage = stringResource(R.string.home_error_important)

    // Cada vez que la pantalla vuelve a primer plano, recarga el contador de no leídas.
    // Al volver (por ejemplo, del detalle) se actualizan votos, contadores y la campana.
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    // Escucha los eventos de un solo uso.
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                HomeUserEvent.ImportantError -> snackbarHostState.showSnackbar(importantErrorMessage)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        HomeUserContent(
            state = state,
            padding = padding,
            onQueryChange = viewModel::onQueryChange,
            onPostClick = onPostClick,
            onUserClick = onUserClick,
            onNotificationsClick = onNotificationsClick,
            onImportantClick = viewModel::onImportantClick
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(padding)
        )
    }
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun HomeUserContent(
    state: HomeUserUiState,
    padding: PaddingValues,
    onQueryChange: (String) -> Unit,
    onPostClick: (String) -> Unit,
    onUserClick: (String) -> Unit,
    onNotificationsClick: () -> Unit,
    onImportantClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(padding)
    ) {
        HomeHeader(
            userName = state.userName,
            city = state.city,
            notifications = state.notifications,
            onNotificationsClick = onNotificationsClick,
            horizontalPadding = ScreenHorizontalPadding
        )

        // Buscador de publicaciones (y de personas con @): el estado vive en el ViewModel.
        SearchField(
            query = state.query,
            onQueryChange = onQueryChange,
            placeholder = stringResource(R.string.home_search_placeholder),
            iconDescription = stringResource(R.string.home_search_description),
            modifier = Modifier.padding(horizontal = ScreenHorizontalPadding),
        )

        when {
            // Búsqueda de personas (el texto empieza con @).
            state.isSearchingUsers -> UserSearchResults(
                users = state.users,
                onUserClick = onUserClick,
                modifier = Modifier.weight(1f),
                horizontalPadding = ScreenHorizontalPadding
            )
            // Error al cargar el feed.
            state.hasError -> CenteredMessage(stringResource(R.string.home_error_generic), isError = true)
            // Buscó algo y no hay coincidencias.
            state.posts.isEmpty() && state.query.isNotBlank() ->
                CenteredMessage(stringResource(R.string.home_search_empty))
            // Feed vacío (sin búsqueda).
            state.posts.isEmpty() && !state.isLoading ->
                CenteredMessage(stringResource(R.string.home_empty_feed))
            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = ScreenHorizontalPadding, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.posts, key = { it.id }) { post ->
                    FeedPostCard(
                        post = post,
                        onClick = { onPostClick(post.id) },
                        onImportantClick = { onImportantClick(post.id) }
                    )
                }
            }
        }
    }
}

// Mensaje centrado que ocupa el espacio sobrante.
@Composable
private fun ColumnScope.CenteredMessage(text: String, isError: Boolean = false) {
    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
        StatusMessage(text = text, isError = isError)
    }
}

// Tarjeta de una publicación del feed.
@Composable
private fun FeedPostCard(post: FeedPost, onClick: () -> Unit, onImportantClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.background),   // CAMBIAR: color de tarjeta
        border = BorderStroke(1.dp, AppTheme.colors.textPrimary)                          // CAMBIAR: borde
    ) {
        AsyncImage(
            model = post.imageUrl,
            contentDescription = post.title,
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(Color.LightGray),
            modifier = Modifier.fillMaxWidth().height(200.dp)
        )

        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconTag(
                    text = post.categoryName,
                    icon = Icons.Outlined.Pets,
                    containerColor = AppTheme.colors.background
                )

                // Sin ubicación del usuario no hay distancia que mostrar.
                post.distanceKm?.let {
                    IconTag(
                        text = stringResource(R.string.home_distance_km, it.toString()),
                        icon = Icons.Filled.LocationOn,
                        iconColor = AppTheme.colors.stateError,
                        containerColor = AppTheme.colors.background
                    )
                }

                // Etiqueta que indica que la publicación es del usuario actual.
                if (post.isOwn) {
                    IconTag(
                        text = stringResource(R.string.home_own_post),
                        icon = Icons.Outlined.Person,
                        containerColor = AppTheme.colors.background
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                color = AppTheme.colors.textPrimary
            )

            Spacer(Modifier.height(6.dp))

            // Los textos se arman aquí porque stringResource necesita el contexto de Compose.
            IconTextRow(
                icon = Icons.Outlined.Pets,
                text = stringResource(
                    R.string.home_info_animal,
                    post.animalName.orEmpty(),
                    post.breed.ifBlank { stringResource(R.string.home_breed_unknown) },
                    sizeText(post.size)
                ),
                modifier = Modifier.padding(vertical = 2.dp)
            )
            IconTextRow(
                icon = Icons.Outlined.VerifiedUser,
                text = vaccinesText(post.vaccinated),
                modifier = Modifier.padding(vertical = 2.dp)
            )
            IconTextRow(
                icon = Icons.Outlined.FavoriteBorder,
                text = stringResource(R.string.home_interested, post.importantCount),
                modifier = Modifier.padding(vertical = 2.dp),
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(Modifier.height(12.dp))

            // Botón "Es importante": estrella llena y colores invertidos cuando ya votaste.
            Button(
                onClick = onImportantClick,
                enabled = !post.isOwn,   // no se vota la propia publicación
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (post.voted) AppTheme.colors.textPrimary else AppTheme.colors.stateSelected,  // CAMBIAR: acento
                    contentColor = if (post.voted) AppTheme.colors.background else AppTheme.colors.textPrimary
                ),
                border = BorderStroke(1.dp, AppTheme.colors.textPrimary)    // CAMBIAR: borde
            ) {
                Icon(
                    imageVector = if (post.voted) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(
                        if (post.voted) R.string.home_important_active else R.string.home_important_button
                    ),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

// Traducen valores del dominio a texto del recurso (viven aquí por stringResource).
@Composable
private fun sizeText(size: AnimalSize?): String = when (size) {
    null -> stringResource(R.string.home_size_unknown)
    AnimalSize.SMALL -> stringResource(R.string.home_size_small)
    AnimalSize.MEDIUM -> stringResource(R.string.home_size_medium)
    AnimalSize.LARGE -> stringResource(R.string.home_size_large)
}

@Composable
private fun vaccinesText(vaccinated: Boolean?): String = when (vaccinated) {
    true -> stringResource(R.string.home_vaccines_yes)
    false -> stringResource(R.string.home_vaccines_no)
    null -> stringResource(R.string.home_vaccines_unknown)
}

@Preview(name = "Con publicaciones", showBackground = true, heightDp = 900)
@Composable
private fun HomeUserContentPreview() {
    PetAdoptionAppTheme {
        HomeUserContent(
            state = HomeUserUiState(
                userName = "ana_77", city = "Armenia, Colombia", notifications = 2,
                posts = listOf(
                    FeedPost("1", null, "Adopción", 1.2, "Perrito en adopción", "Perro", "Criollo",
                        AnimalSize.MEDIUM, true, 3),
                    FeedPost("2", null, "Perdidos", 2.5, "Gato perdido", "Gato", "Mestizo",
                        AnimalSize.SMALL, null, 4, voted = true)
                )
            ),
            padding = PaddingValues(),
            onQueryChange = {}, onPostClick = {}, onUserClick = {},
            onNotificationsClick = {}, onImportantClick = {}
        )
    }
}

@Preview(name = "Búsqueda de personas", showBackground = true, heightDp = 600)
@Composable
private fun HomeUserSearchUsersPreview() {
    PetAdoptionAppTheme {
        HomeUserContent(
            state = HomeUserUiState(
                userName = "ana_77", query = "@lu", isSearchingUsers = true,
                users = listOf(UserSummary("u2", "luis_9", null))
            ),
            padding = PaddingValues(),
            onQueryChange = {}, onPostClick = {}, onUserClick = {},
            onNotificationsClick = {}, onImportantClick = {}
        )
    }
}

@Preview(name = "Vacío", showBackground = true)
@Composable
private fun HomeUserEmptyPreview() {
    PetAdoptionAppTheme {
        HomeUserContent(
            state = HomeUserUiState(userName = "ana_77"),
            padding = PaddingValues(),
            onQueryChange = {}, onPostClick = {}, onUserClick = {},
            onNotificationsClick = {}, onImportantClick = {}
        )
    }
}