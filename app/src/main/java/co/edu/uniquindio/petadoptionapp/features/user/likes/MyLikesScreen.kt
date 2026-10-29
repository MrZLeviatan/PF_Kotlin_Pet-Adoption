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

private val ScreenHorizontalPadding = 24.dp

/**
Pantalla "Me gusta": las publicaciones que la persona marcó con "Es importante",
de la más reciente a la más antigua y agrupadas por categoría (con una pestaña por categoría).
Conecta el [MyLikesViewModel] con la UI; [MyLikesContent] solo dibuja.
 */
@Composable
fun MyLikesScreen(
    onBack: () -> Unit,
    onPostClick: (postId: String) -> Unit,
    viewModel: MyLikesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Al volver (por ejemplo, del detalle) se recarga por si se votó o se quitó un voto.
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    MyLikesContent(
        uiState = uiState,
        onBack = onBack,
        onTabSelected = viewModel::onTabSelected,
        onPostClick = onPostClick,
        onRemoveLike = viewModel::onRemoveLike,
    )
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun MyLikesContent(
    uiState: MyLikesUiState,
    onBack: () -> Unit,
    onTabSelected: (String?) -> Unit,
    onPostClick: (String) -> Unit,
    onRemoveLike: (String) -> Unit,
) {
    val tabs = uiState.tabs
    // "Todas" va primero; luego una pestaña por categoría.
    val tabLabels = listOf(stringResource(R.string.my_likes_tab_all, uiState.posts.size)) +
        tabs.map { stringResource(R.string.my_likes_tab_category, it.name, it.count) }
    val selectedIndex = uiState.selectedCategoryId
        ?.let { id -> tabs.indexOfFirst { it.categoryId == id } + 1 } ?: 0
    val sections = uiState.visibleSections

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .systemBarsPadding(),
    ) {
        AppTopBar(
            title = stringResource(R.string.my_likes_title),
            onBack = onBack,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        if (uiState.posts.isNotEmpty()) {
            AppTabRow(
                tabs = tabLabels,
                selectedIndex = selectedIndex,
                onTabSelected = { index -> onTabSelected(if (index == 0) null else tabs[index - 1].categoryId) },
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (uiState.actionError) {
                item(key = "action-error") {
                    ErrorText(text = stringResource(R.string.my_posts_action_error), modifier = Modifier.fillMaxWidth())
                }
            }

            when {
                uiState.isLoading -> Unit
                uiState.hasError -> item(key = "error") {
                    CenteredStatus(stringResource(R.string.my_likes_error), isError = true)
                }
                sections.isEmpty() -> item(key = "empty") {
                    CenteredStatus(stringResource(R.string.my_likes_empty))
                }
            }

            sections.forEach { (category, posts) ->
                item(key = "header-${category.categoryId}") {
                    // Encabezado de categoría: huella y "Adopción (2)".
                    IconTextRow(
                        icon = Icons.Outlined.Pets,
                        text = stringResource(R.string.my_likes_tab_category, category.name, category.count),
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .semantics { heading() },
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                items(posts, key = { it.id }) { post ->
                    LikedPostItem(
                        post = post,
                        onClick = { onPostClick(post.id) },
                        onRemove = { onRemoveLike(post.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LikedPostItem(post: LikedPostUi, onClick: () -> Unit, onRemove: () -> Unit) {
    // Mientras carga (o si falla) se ve la imagen local, igual que en "Mis publicaciones".
    val fallback = painterResource(R.drawable.hero_image)
    val photo = rememberAsyncImagePainter(model = post.photoUrl, placeholder = fallback, error = fallback)

    PostSummaryCard(
        image = if (post.photoUrl == null) fallback else photo,
        typeLabel = post.categoryName,
        // Solo aparecen publicaciones visibles al público (verificadas y en proceso).
        statusColor = AppTheme.colors.stateSuccess,
        title = post.title,
        metaLines = listOf(
            PostMetaLine(
                Icons.Outlined.CalendarToday,
                stringResource(R.string.my_posts_published, agoText(post.ageHours)),
            ),
            PostMetaLine(
                Icons.Outlined.FavoriteBorder,
                pluralStringResource(R.plurals.my_posts_interested, post.importantCount, post.importantCount),
            ),
        ),
        onClick = onClick,
        footer = {
            TextActionButton(
                text = stringResource(R.string.my_likes_remove),
                icon = Icons.Filled.Star,
                onClick = onRemove,
                contentColor = AppTheme.colors.actionSecondary,
            )
        },
    )
}

@Composable
private fun CenteredStatus(text: String, isError: Boolean = false) {
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

@Preview(name = "Todas", showBackground = true, heightDp = 1100)
@Composable
private fun MyLikesAllPreview() {
    PetAdoptionAppTheme {
        MyLikesContent(
            uiState = MyLikesUiState(posts = previewLikes, isLoading = false),
            onBack = {}, onTabSelected = {}, onPostClick = {}, onRemoveLike = {},
        )
    }
}

@Preview(name = "Vacía", showBackground = true, heightDp = 600)
@Composable
private fun MyLikesEmptyPreview() {
    PetAdoptionAppTheme {
        MyLikesContent(
            uiState = MyLikesUiState(isLoading = false),
            onBack = {}, onTabSelected = {}, onPostClick = {}, onRemoveLike = {},
        )
    }
}
