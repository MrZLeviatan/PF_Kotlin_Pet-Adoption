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

/**
Pantalla de inicio del usuario (feed). Muestra el encabezado con el saludo, la ciudad y la campana
de notificaciones; un buscador que filtra publicaciones por título o busca personas si el texto
empieza con "@"; y la lista de tarjetas de publicaciones con foto, categoría, distancia, datos de la
mascota, interesados y el botón "Es importante".
Estado: observa HomeUserUiState desde HomeUserViewModel. Eventos: escribir en el buscador, tocar una
publicación, una persona o la campana, y votar "Es importante". Evento de una sola vez:
HomeUserEvent.ImportantError, que se muestra en un Snackbar.
Casos de uso: no los llama directamente; el ViewModel usa GetFeedUseCase, SearchUsersUseCase,
ToggleImportantUseCase, CountUnreadNotificationsUseCase, entre otros.
Navegación: no decide a dónde navegar; avisa con onPostClick, onUserClick y onNotificationsClick
y AppNavigation abre el detalle, el perfil o las notificaciones. Al volver a la pantalla se recarga.
**/

// Margen horizontal común de la pantalla.
private val ScreenHorizontalPadding = 16.dp

/**
Pantalla de inicio del usuario.

Conecta el [HomeUserViewModel] con la UI: observa el estado, reenvía las acciones
y reacciona a los eventos de un solo uso. [HomeUserContent] solo dibuja.
**/
@Composable
fun HomeUserScreen(
    // Márgenes que impone el Scaffold contenedor.
    padding: PaddingValues = PaddingValues(),
    // Acción para abrir el detalle de una publicación.
    onPostClick: (String) -> Unit = {},
    // Acción para abrir el perfil de una persona.
    onUserClick: (String) -> Unit = {},
    // Acción para abrir las notificaciones.
    onNotificationsClick: () -> Unit = {},
    // ViewModel del inicio, inyectado por Hilt.
    viewModel: HomeUserViewModel = hiltViewModel()
) {
    // Observa el estado del ViewModel respetando el ciclo de vida.
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // Estado del Snackbar que muestra los avisos, recordado entre recomposiciones.
    val snackbarHostState = remember { SnackbarHostState() }
    // Texto del error al votar, leído aquí porque stringResource solo funciona en Compose.
    val importantErrorMessage = stringResource(R.string.home_error_important)

    // Cada vez que la pantalla vuelve a primer plano, recarga el contador de no leídas.
    // Al volver (por ejemplo, del detalle) se actualizan votos, contadores y la campana.
    LifecycleResumeEffect(Unit) {
        // Recarga feed y notificaciones.
        viewModel.refresh()
        // No hay nada que limpiar al pausar o salir.
        onPauseOrDispose { }
    }

    // Escucha los eventos de un solo uso.
    LaunchedEffect(viewModel) {
        // Recoge cada evento emitido.
        viewModel.events.collect { event ->
            // Reacciona según el tipo de evento.
            when (event) {
                // Si falló el voto, muestra el aviso en el Snackbar.
                HomeUserEvent.ImportantError -> snackbarHostState.showSnackbar(importantErrorMessage)
            }
        }
    }

    // Contenedor que superpone el contenido y el Snackbar.
    Box(Modifier.fillMaxSize()) {
        // Contenido de la pantalla con el estado y las acciones.
        HomeUserContent(
            // Estado actual.
            state = state,
            // Márgenes del contenedor.
            padding = padding,
            // Cambios del buscador.
            onQueryChange = viewModel::onQueryChange,
            // Abrir publicación.
            onPostClick = onPostClick,
            // Abrir perfil.
            onUserClick = onUserClick,
            // Abrir notificaciones.
            onNotificationsClick = onNotificationsClick,
            // Votar "Es importante".
            onImportantClick = viewModel::onImportantClick
        )
        // Zona donde aparecen los Snackbars, abajo y respetando los márgenes.
        SnackbarHost(
            // Estado del Snackbar.
            hostState = snackbarHostState,
            // Alineado abajo al centro y con los márgenes del Scaffold.
            modifier = Modifier.align(Alignment.BottomCenter).padding(padding)
        )
    }
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun HomeUserContent(
    // Estado a dibujar.
    state: HomeUserUiState,
    // Márgenes del contenedor.
    padding: PaddingValues,
    // Acción al escribir en el buscador.
    onQueryChange: (String) -> Unit,
    // Acción al tocar una publicación.
    onPostClick: (String) -> Unit,
    // Acción al tocar una persona.
    onUserClick: (String) -> Unit,
    // Acción al tocar la campana.
    onNotificationsClick: () -> Unit,
    // Acción al votar "Es importante".
    onImportantClick: (String) -> Unit
) {
    // Columna principal: encabezado, buscador y contenido.
    Column(
        modifier = Modifier
            // Ocupa toda la pantalla.
            .fillMaxSize()
            // Fondo del tema.
            .background(AppTheme.colors.background)
            // Respeta los márgenes del Scaffold.
            .padding(padding)
    ) {
        // Encabezado con saludo, ciudad y campana.
        HomeHeader(
            // Alias del usuario.
            userName = state.userName,
            // Ciudad del usuario.
            city = state.city,
            // Notificaciones sin leer.
            notifications = state.notifications,
            // Abrir notificaciones.
            onNotificationsClick = onNotificationsClick,
            // Margen horizontal común.
            horizontalPadding = ScreenHorizontalPadding
        )

        // Buscador de publicaciones (y de personas con @): el estado vive en el ViewModel.
        SearchField(
            // Texto actual.
            query = state.query,
            // Cambios del texto.
            onQueryChange = onQueryChange,
            // Texto de ayuda.
            placeholder = stringResource(R.string.home_search_placeholder),
            // Descripción accesible del ícono.
            iconDescription = stringResource(R.string.home_search_description),
            // Margen horizontal común.
            modifier = Modifier.padding(horizontal = ScreenHorizontalPadding),
        )

        // Decide qué mostrar bajo el buscador.
        when {
            // Búsqueda de personas (el texto empieza con @).
            state.isSearchingUsers -> UserSearchResults(
                // Personas encontradas.
                users = state.users,
                // Abrir perfil.
                onUserClick = onUserClick,
                // Ocupa el espacio restante.
                modifier = Modifier.weight(1f),
                // Margen horizontal común.
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
            // En otro caso, la lista perezosa de publicaciones.
            else -> LazyColumn(
                // Ocupa el espacio restante.
                modifier = Modifier.weight(1f),
                // Márgenes internos de la lista.
                contentPadding = PaddingValues(horizontal = ScreenHorizontalPadding, vertical = 12.dp),
                // Separación entre tarjetas.
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Una tarjeta por publicación, con su id como clave estable.
                items(state.posts, key = { it.id }) { post ->
                    // Tarjeta de la publicación.
                    FeedPostCard(
                        // Datos de la publicación.
                        post = post,
                        // Tocarla abre el detalle.
                        onClick = { onPostClick(post.id) },
                        // Votar "Es importante".
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
    // Contenedor que ocupa el espacio restante y centra el mensaje.
    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
        // Mensaje informativo o de error.
        StatusMessage(text = text, isError = isError)
    }
}

// Tarjeta de una publicación del feed.
@Composable
private fun FeedPostCard(post: FeedPost, onClick: () -> Unit, onImportantClick: () -> Unit) {
    // Tarjeta táctil de Material 3.
    Card(
        // Tocarla abre el detalle.
        onClick = onClick,
        // Ocupa todo el ancho.
        modifier = Modifier.fillMaxWidth(),
        // Esquinas redondeadas.
        shape = RoundedCornerShape(20.dp),
        // Color de fondo de la tarjeta. CAMBIAR: color de tarjeta.
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.background),
        // Borde de la tarjeta. CAMBIAR: borde.
        border = BorderStroke(1.dp, AppTheme.colors.textPrimary)
    ) {
        // Foto principal de la publicación cargada con Coil.
        AsyncImage(
            // URL de la foto.
            model = post.imageUrl,
            // Descripción accesible: el título.
            contentDescription = post.title,
            // Recorta para llenar el espacio.
            contentScale = ContentScale.Crop,
            // Color gris mientras carga.
            placeholder = ColorPainter(Color.LightGray),
            // Ancho completo y alto fijo.
            modifier = Modifier.fillMaxWidth().height(200.dp)
        )

        // Columna con la información de la publicación.
        Column(modifier = Modifier.padding(12.dp)) {
            // Fila de etiquetas: categoría, distancia y "Mía".
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Etiqueta de la categoría.
                IconTag(
                    // Nombre de la categoría.
                    text = post.categoryName,
                    // Ícono de huella.
                    icon = Icons.Outlined.Pets,
                    // Fondo de la etiqueta.
                    containerColor = AppTheme.colors.background
                )

                // Sin ubicación del usuario no hay distancia que mostrar.
                post.distanceKm?.let {
                    // Etiqueta de distancia.
                    IconTag(
                        // Texto "N km".
                        text = stringResource(R.string.home_distance_km, it.toString()),
                        // Ícono de ubicación.
                        icon = Icons.Filled.LocationOn,
                        // Ícono en rojo.
                        iconColor = AppTheme.colors.stateError,
                        // Fondo de la etiqueta.
                        containerColor = AppTheme.colors.background
                    )
                }

                // Etiqueta que indica que la publicación es del usuario actual.
                if (post.isOwn) {
                    // Etiqueta "Tu publicación".
                    IconTag(
                        // Texto desde strings.xml.
                        text = stringResource(R.string.home_own_post),
                        // Ícono de persona.
                        icon = Icons.Outlined.Person,
                        // Fondo de la etiqueta.
                        containerColor = AppTheme.colors.background
                    )
                }
            }

            // Espacio bajo las etiquetas.
            Spacer(Modifier.height(8.dp))

            // Título de la publicación.
            Text(
                // Texto del título.
                text = post.title,
                // Estilo de título mediano.
                style = MaterialTheme.typography.titleMedium,
                // Color principal.
                color = AppTheme.colors.textPrimary
            )

            // Espacio bajo el título.
            Spacer(Modifier.height(6.dp))

            // Los textos se arman aquí porque stringResource necesita el contexto de Compose.
            IconTextRow(
                // Ícono de huella.
                icon = Icons.Outlined.Pets,
                // Texto "Animal • raza • tamaño".
                text = stringResource(
                    // Plantilla del texto.
                    R.string.home_info_animal,
                    // Tipo de animal (vacío si no se conoce).
                    post.animalName.orEmpty(),
                    // Raza, o "Raza desconocida" si está vacía.
                    post.breed.ifBlank { stringResource(R.string.home_breed_unknown) },
                    // Tamaño traducido.
                    sizeText(post.size)
                ),
                // Margen vertical.
                modifier = Modifier.padding(vertical = 2.dp)
            )
            // Fila con el estado de vacunas.
            IconTextRow(
                // Ícono de verificado.
                icon = Icons.Outlined.VerifiedUser,
                // Texto de vacunas.
                text = vaccinesText(post.vaccinated),
                // Margen vertical.
                modifier = Modifier.padding(vertical = 2.dp)
            )
            // Fila con la cantidad de interesados.
            IconTextRow(
                // Ícono de corazón.
                icon = Icons.Outlined.FavoriteBorder,
                // Texto "N interesados".
                text = stringResource(R.string.home_interested, post.importantCount),
                // Margen vertical.
                modifier = Modifier.padding(vertical = 2.dp),
                // Estilo de título pequeño.
                style = MaterialTheme.typography.titleSmall
            )

            // Espacio antes del botón.
            Spacer(Modifier.height(12.dp))

            // Botón "Es importante": estrella llena y colores invertidos cuando ya votaste.
            Button(
                // Acción al tocarlo.
                onClick = onImportantClick,
                // No se vota la propia publicación.
                enabled = !post.isOwn,
                // Ocupa todo el ancho.
                modifier = Modifier.fillMaxWidth(),
                // Esquinas redondeadas.
                shape = RoundedCornerShape(12.dp),
                // Colores según si ya se votó.
                colors = ButtonDefaults.buttonColors(
                    // Fondo oscuro si ya votó; color de acento si no. CAMBIAR: acento.
                    containerColor = if (post.voted) AppTheme.colors.textPrimary else AppTheme.colors.stateSelected,
                    // Texto claro si ya votó; oscuro si no.
                    contentColor = if (post.voted) AppTheme.colors.background else AppTheme.colors.textPrimary
                ),
                // Borde del botón. CAMBIAR: borde.
                border = BorderStroke(1.dp, AppTheme.colors.textPrimary)
            ) {
                // Ícono de estrella.
                Icon(
                    // Estrella llena si ya votó; vacía si no.
                    imageVector = if (post.voted) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    // Decorativo: el texto ya describe la acción.
                    contentDescription = null,
                    // Tamaño del ícono.
                    modifier = Modifier.size(20.dp)
                )
                // Espacio entre ícono y texto.
                Spacer(Modifier.width(8.dp))
                // Texto del botón.
                Text(
                    // Texto según si ya votó o no.
                    text = stringResource(
                        // Elige "Marcado como importante" o "Es importante".
                        if (post.voted) R.string.home_important_active else R.string.home_important_button
                    ),
                    // Estilo de etiqueta grande.
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

// Traducen valores del dominio a texto del recurso (viven aquí por stringResource).
// Devuelve el texto del tamaño del animal.
@Composable
private fun sizeText(size: AnimalSize?): String = when (size) {
    // Tamaño no indicado.
    null -> stringResource(R.string.home_size_unknown)
    // Pequeño.
    AnimalSize.SMALL -> stringResource(R.string.home_size_small)
    // Mediano.
    AnimalSize.MEDIUM -> stringResource(R.string.home_size_medium)
    // Grande.
    AnimalSize.LARGE -> stringResource(R.string.home_size_large)
}

// Devuelve el texto del estado de vacunas.
@Composable
private fun vaccinesText(vaccinated: Boolean?): String = when (vaccinated) {
    // Vacunado.
    true -> stringResource(R.string.home_vaccines_yes)
    // No vacunado.
    false -> stringResource(R.string.home_vaccines_no)
    // No se indicó.
    null -> stringResource(R.string.home_vaccines_unknown)
}

// Previsualización del feed con dos publicaciones de ejemplo.
@Preview(name = "Con publicaciones", showBackground = true, heightDp = 900)
@Composable
private fun HomeUserContentPreview() {
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Dibuja el contenido sin ViewModel.
        HomeUserContent(
            // Estado de ejemplo.
            state = HomeUserUiState(
                // Alias, ciudad y notificaciones de ejemplo.
                userName = "ana_77", city = "Armenia, Colombia", notifications = 2,
                // Publicaciones de ejemplo (una ya votada).
                posts = listOf(
                    FeedPost("1", null, "Adopción", 1.2, "Perrito en adopción", "Perro", "Criollo",
                        AnimalSize.MEDIUM, true, 3),
                    FeedPost("2", null, "Perdidos", 2.5, "Gato perdido", "Gato", "Mestizo",
                        AnimalSize.SMALL, null, 4, voted = true)
                )
            ),
            // Sin márgenes externos.
            padding = PaddingValues(),
            // Acciones vacías en la previsualización.
            onQueryChange = {}, onPostClick = {}, onUserClick = {},
            onNotificationsClick = {}, onImportantClick = {}
        )
    }
}

// Previsualización de la búsqueda de personas con "@".
@Preview(name = "Búsqueda de personas", showBackground = true, heightDp = 600)
@Composable
private fun HomeUserSearchUsersPreview() {
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Dibuja el contenido sin ViewModel.
        HomeUserContent(
            // Estado de ejemplo buscando personas.
            state = HomeUserUiState(
                // Alias, texto buscado y modo de búsqueda de personas.
                userName = "ana_77", query = "@lu", isSearchingUsers = true,
                // Una persona encontrada.
                users = listOf(UserSummary("u2", "luis_9", null))
            ),
            // Sin márgenes externos.
            padding = PaddingValues(),
            // Acciones vacías en la previsualización.
            onQueryChange = {}, onPostClick = {}, onUserClick = {},
            onNotificationsClick = {}, onImportantClick = {}
        )
    }
}

// Previsualización del feed vacío.
@Preview(name = "Vacío", showBackground = true)
@Composable
private fun HomeUserEmptyPreview() {
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Dibuja el contenido sin ViewModel.
        HomeUserContent(
            // Estado sin publicaciones.
            state = HomeUserUiState(userName = "ana_77"),
            // Sin márgenes externos.
            padding = PaddingValues(),
            // Acciones vacías en la previsualización.
            onQueryChange = {}, onPostClick = {}, onUserClick = {},
            onNotificationsClick = {}, onImportantClick = {}
        )
    }
}
