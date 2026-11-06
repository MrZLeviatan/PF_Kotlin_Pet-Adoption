package co.edu.uniquindio.petadoptionapp.features.user.likes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.util.agoText
import co.edu.uniquindio.petadoptionapp.core.component.button.TextActionButton
import co.edu.uniquindio.petadoptionapp.core.component.card.PostMetaLine
import co.edu.uniquindio.petadoptionapp.core.component.card.PostSummaryCard
import co.edu.uniquindio.petadoptionapp.core.component.header.AppTopBar
import co.edu.uniquindio.petadoptionapp.core.component.navigation.AppTabRow
import co.edu.uniquindio.petadoptionapp.core.component.text.ErrorText
import co.edu.uniquindio.petadoptionapp.core.component.text.IconTextRow
import co.edu.uniquindio.petadoptionapp.core.component.text.StatusMessage
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import coil3.compose.rememberAsyncImagePainter

/**
Pantalla "Me gusta" (Mis likes), accesible desde el menú de Mi Perfil.
Lista las publicaciones que la persona marcó como "Es importante", con una fila de pestañas
("Todas" y una por categoría) y la lista agrupada por categoría con encabezados.
Cada tarjeta abre el detalle de la publicación y trae un botón para quitar el "me gusta".
El estado (MyLikesUiState) lo produce MyLikesViewModel con GetMyImportantPostsUseCase,
ToggleImportantUseCase y el catálogo de categorías. La navegación la resuelve AppNavigation
mediante onBack y onPostClick; al volver a primer plano la pantalla pide refresh().
**/

// Margen horizontal común del contenido.
private val ScreenHorizontalPadding = 24.dp

/**
Pantalla "Me gusta": las publicaciones que la persona marcó con "Es importante",
de la más reciente a la más antigua y agrupadas por categoría (con una pestaña por categoría).
Conecta el [MyLikesViewModel] con la UI; [MyLikesContent] solo dibuja.
**/
@Composable
fun MyLikesScreen(
    // Callback para volver atrás.
    onBack: () -> Unit,
    // Callback para abrir el detalle de una publicación.
    onPostClick: (postId: String) -> Unit,
    // ViewModel inyectado por Hilt.
    viewModel: MyLikesViewModel = hiltViewModel(),
) {
    // Recolecta el estado respetando el ciclo de vida.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Al volver (por ejemplo, del detalle) se recarga por si se votó o se quitó un voto.
    LifecycleResumeEffect(Unit) {
        // Recarga la lista en cada ON_RESUME.
        viewModel.refresh()
        // No hay nada que limpiar al pausar o salir.
        onPauseOrDispose { }
    }

    // Dibuja el contenido sin estado.
    MyLikesContent(
        // Estado actual.
        uiState = uiState,
        // Volver atrás.
        onBack = onBack,
        // Cambio de pestaña.
        onTabSelected = viewModel::onTabSelected,
        // Abrir una publicación.
        onPostClick = onPostClick,
        // Quitar un "me gusta".
        onRemoveLike = viewModel::onRemoveLike,
    )
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun MyLikesContent(
    // Estado de la pantalla.
    uiState: MyLikesUiState,
    // Volver atrás.
    onBack: () -> Unit,
    // Selección de pestaña (null es "Todas").
    onTabSelected: (String?) -> Unit,
    // Abrir una publicación por id.
    onPostClick: (String) -> Unit,
    // Quitar el "me gusta" de una publicación por id.
    onRemoveLike: (String) -> Unit,
) {
    // Pestañas de categoría calculadas por el estado.
    val tabs = uiState.tabs
    // "Todas" va primero; luego una pestaña por categoría.
    val tabLabels = listOf(stringResource(R.string.my_likes_tab_all, uiState.posts.size)) +
        tabs.map { stringResource(R.string.my_likes_tab_category, it.name, it.count) }
    // Índice de la pestaña elegida: 0 para "Todas", o la posición de la categoría más uno.
    val selectedIndex = uiState.selectedCategoryId
        ?.let { id -> tabs.indexOfFirst { it.categoryId == id } + 1 } ?: 0
    // Secciones a mostrar según la pestaña.
    val sections = uiState.visibleSections

    // Columna raíz: barra superior, pestañas y lista.
    Column(
        // Ocupa toda la pantalla con el fondo del tema y respeta las barras del sistema.
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .systemBarsPadding(),
    ) {
        // Barra superior con título y flecha atrás.
        AppTopBar(
            title = stringResource(R.string.my_likes_title),
            onBack = onBack,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        // Las pestañas solo aparecen si hay publicaciones.
        if (uiState.posts.isNotEmpty()) {
            // Fila de pestañas.
            AppTabRow(
                tabs = tabLabels,
                selectedIndex = selectedIndex,
                // Convierte el índice tocado en id de categoría (0 es "Todas" = null).
                onTabSelected = { index -> onTabSelected(if (index == 0) null else tabs[index - 1].categoryId) },
            )
        }

        // Lista perezosa con encabezados de categoría y tarjetas.
        LazyColumn(
            // Ancho completo y el resto del alto disponible.
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            // Márgenes internos de la lista.
            contentPadding = PaddingValues(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
            // Separación de 12 dp entre elementos.
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Si falló quitar un "me gusta", muestra el error arriba de la lista.
            if (uiState.actionError) {
                item(key = "action-error") {
                    ErrorText(text = stringResource(R.string.my_posts_action_error), modifier = Modifier.fillMaxWidth())
                }
            }

            // Estados especiales de la lista.
            when {
                // Cargando: no se muestra nada.
                uiState.isLoading -> Unit
                // Error de carga.
                uiState.hasError -> item(key = "error") {
                    CenteredStatus(stringResource(R.string.my_likes_error), isError = true)
                }
                // Sin "me gusta".
                sections.isEmpty() -> item(key = "empty") {
                    CenteredStatus(stringResource(R.string.my_likes_empty))
                }
            }

            // Recorre cada sección (categoría con sus publicaciones).
            sections.forEach { (category, posts) ->
                // Encabezado de la sección con clave estable.
                item(key = "header-${category.categoryId}") {
                    // Encabezado de categoría: huella y "Adopción (2)".
                    IconTextRow(
                        icon = Icons.Outlined.Pets,
                        text = stringResource(R.string.my_likes_tab_category, category.name, category.count),
                        // Pequeño margen superior y semántica de encabezado para TalkBack.
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .semantics { heading() },
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                // Tarjetas de la sección, con el id de la publicación como clave.
                items(posts, key = { it.id }) { post ->
                    LikedPostItem(
                        post = post,
                        // Abre el detalle.
                        onClick = { onPostClick(post.id) },
                        // Quita el "me gusta".
                        onRemove = { onRemoveLike(post.id) },
                    )
                }
            }
        }
    }
}

// Tarjeta de una publicación con "me gusta" y su botón para quitarlo.
@Composable
private fun LikedPostItem(post: LikedPostUi, onClick: () -> Unit, onRemove: () -> Unit) {
    // Mientras carga (o si falla) se ve la imagen local, igual que en "Mis publicaciones".
    val fallback = painterResource(R.drawable.hero_image)
    // Painter asíncrono (Coil) para la foto remota.
    val photo = rememberAsyncImagePainter(model = post.photoUrl, placeholder = fallback, error = fallback)

    // Tarjeta resumen reutilizable.
    PostSummaryCard(
        // Sin foto se usa directamente la imagen local.
        image = if (post.photoUrl == null) fallback else photo,
        // Etiqueta con la categoría.
        typeLabel = post.categoryName,
        // Solo aparecen publicaciones visibles al público (verificadas y en proceso).
        statusColor = AppTheme.colors.stateSuccess,
        title = post.title,
        // Metadatos: antigüedad e interesados.
        metaLines = listOf(
            // Hace cuánto se publicó.
            PostMetaLine(
                Icons.Outlined.CalendarToday,
                stringResource(R.string.my_posts_published, agoText(post.ageHours)),
            ),
            // Número de interesados (en plural según la cantidad).
            PostMetaLine(
                Icons.Outlined.FavoriteBorder,
                pluralStringResource(R.plurals.my_posts_interested, post.importantCount, post.importantCount),
            ),
        ),
        // Al tocar la tarjeta se abre el detalle.
        onClick = onClick,
        // Pie de la tarjeta con la acción de quitar.
        footer = {
            // Botón de texto con estrella para quitar el "me gusta".
            TextActionButton(
                text = stringResource(R.string.my_likes_remove),
                icon = Icons.Filled.Star,
                onClick = onRemove,
                contentColor = AppTheme.colors.actionSecondary,
            )
        },
    )
}

// Mensaje de estado centrado (vacío o error) dentro de la lista.
@Composable
private fun CenteredStatus(text: String, isError: Boolean = false) {
    // Reutiliza StatusMessage con margen vertical amplio.
    StatusMessage(
        text = text,
        isError = isError,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
    )
}

// Datos de ejemplo solo para las previews (el primero es el "me gusta" más reciente).
private val previewLikes = listOf(
    LikedPostUi("p2", "Gato perdido en el norte", "lost", "Perdidos", null, 5, 4),
    LikedPostUi("p1", "Perrito en adopción", "adoption", "Adopción", null, 30, 12),
    LikedPostUi("p6", "Max busca familia", "adoption", "Adopción", null, 72, 7),
)

// Vista previa con varias publicaciones en dos categorías.
@Preview(name = "Todas", showBackground = true, heightDp = 1100)
@Composable
private fun MyLikesAllPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Pinta el contenido con los datos de ejemplo.
        MyLikesContent(
            uiState = MyLikesUiState(posts = previewLikes, isLoading = false),
            onBack = {}, onTabSelected = {}, onPostClick = {}, onRemoveLike = {},
        )
    }
}

// Vista previa del estado vacío.
@Preview(name = "Vacía", showBackground = true, heightDp = 600)
@Composable
private fun MyLikesEmptyPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Pinta el contenido sin publicaciones.
        MyLikesContent(
            uiState = MyLikesUiState(isLoading = false),
            onBack = {}, onTabSelected = {}, onPostClick = {}, onRemoveLike = {},
        )
    }
}
