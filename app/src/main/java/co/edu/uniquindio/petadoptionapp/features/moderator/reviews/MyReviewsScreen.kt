package co.edu.uniquindio.petadoptionapp.features.moderator.reviews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.card.PostMetaLine
import co.edu.uniquindio.petadoptionapp.core.component.card.PostSummaryCard
import co.edu.uniquindio.petadoptionapp.core.component.indicator.StatusIndicator
import co.edu.uniquindio.petadoptionapp.core.component.navigation.AppTabRow
import co.edu.uniquindio.petadoptionapp.core.component.text.StatusMessage
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import co.edu.uniquindio.petadoptionapp.core.util.agoText
import co.edu.uniquindio.petadoptionapp.domain.model.post.PriorityLevel
import co.edu.uniquindio.petadoptionapp.features.moderator.priorityColor
import co.edu.uniquindio.petadoptionapp.features.moderator.priorityText
import coil3.compose.rememberAsyncImagePainter

private val ScreenHorizontalPadding = 24.dp

/**
 Pestaña "Publicaciones" del moderador: las que tomó para revisar y aún no decide
 (ni verificadas ni rechazadas), divididas por prioridad, con una pestaña por prioridad.
 Mismo formato que "Me gusta" del usuario. Al tocar una se abre su detalle para aprobarla o rechazarla.
 */
@Composable
fun MyReviewsScreen(
    padding: PaddingValues = PaddingValues(),
    onPostClick: (postId: String) -> Unit = {},
    viewModel: MyReviewsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Al volver del detalle se recarga: las aprobadas o rechazadas salen de la lista.
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    MyReviewsContent(
        uiState = uiState,
        padding = padding,
        onTabSelected = viewModel::onTabSelected,
        onPostClick = onPostClick,
    )
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun MyReviewsContent(
    uiState: MyReviewsUiState,
    padding: PaddingValues,
    onTabSelected: (String?) -> Unit,
    onPostClick: (String) -> Unit,
) {
    val tabs = uiState.tabs
    // "Todas" va primero; luego una pestaña por prioridad.
    val tabLabels = listOf(stringResource(R.string.my_likes_tab_all, uiState.posts.size)) +
        tabs.map { stringResource(R.string.my_likes_tab_category, priorityText(it.priority), it.count) }
    val selectedIndex = uiState.selectedKey?.let { key -> tabs.indexOfFirst { it.key == key } + 1 } ?: 0
    val sections = uiState.visibleSections

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(padding),
    ) {
        // Es una pestaña de la barra inferior: título sin flecha de volver.
        Text(
            text = stringResource(R.string.mod_reviews_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.textPrimary,
            modifier = Modifier
                .padding(horizontal = ScreenHorizontalPadding, vertical = 20.dp)
                .semantics { heading() },
        )

        if (uiState.posts.isNotEmpty()) {
            AppTabRow(
                tabs = tabLabels,
                selectedIndex = selectedIndex,
                onTabSelected = { index -> onTabSelected(if (index == 0) null else tabs[index - 1].key) },
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when {
                uiState.isLoading -> Unit
                uiState.hasError -> item(key = "error") { CenteredStatus(stringResource(R.string.mod_reviews_error), isError = true) }
                sections.isEmpty() -> item(key = "empty") { CenteredStatus(stringResource(R.string.mod_reviews_empty)) }
            }

            sections.forEach { (tab, posts) ->
                item(key = "header-${tab.key}") {
                    // Encabezado de prioridad: su punto de color y "Urgente (1)".
                    Row(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .semantics { heading() },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        StatusIndicator(color = priorityColor(tab.priority))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.my_likes_tab_category, priorityText(tab.priority), tab.count),
                            style = MaterialTheme.typography.titleSmall,
                            color = AppTheme.colors.textPrimary,
                        )
                    }
                }
                items(posts, key = { it.id }) { post ->
                    ReviewPostItem(post = post, onClick = { onPostClick(post.id) })
                }
            }
        }
    }
}

@Composable
private fun ReviewPostItem(post: ReviewPostUi, onClick: () -> Unit) {
    // Mientras carga (o si falla) se ve la imagen local, igual que en "Me gusta".
    val fallback = painterResource(R.drawable.hero_image)
    val photo = rememberAsyncImagePainter(model = post.photoUrl, placeholder = fallback, error = fallback)

    PostSummaryCard(
        image = if (post.photoUrl == null) fallback else photo,
        typeLabel = post.categoryName,
        statusColor = priorityColor(post.priority),
        title = post.title,
        metaLines = listOfNotNull(
            PostMetaLine(Icons.Outlined.CalendarToday, stringResource(R.string.my_posts_published, agoText(post.ageHours))),
            post.priorityReason.takeIf { it.isNotBlank() }?.let { PostMetaLine(Icons.Outlined.AutoAwesome, it) },
        ),
        onClick = onClick,
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

private val previewReviews = listOf(
    ReviewPostUi("p6", "Perrita herida necesita hogar urgente", "Adopción", null, 5, PriorityLevel.URGENT, "Animal herido que necesita atención"),
    ReviewPostUi("p7", "Gatitos abandonados en una caja", "Encontrados", null, 26, PriorityLevel.HIGH, "Animales muy pequeños sin cuidado"),
    ReviewPostUi("p8", "Jornada de vacunación gratuita", "Veterinaria", null, 3, PriorityLevel.LOW, "Evento informativo"),
)

@Preview(name = "Todas", showBackground = true, heightDp = 1000)
@Composable
private fun MyReviewsPreview() {
    PetAdoptionAppTheme {
        MyReviewsContent(uiState = MyReviewsUiState(posts = previewReviews, isLoading = false), padding = PaddingValues(), onTabSelected = {}, onPostClick = {})
    }
}

@Preview(name = "Vacía", showBackground = true, heightDp = 500)
@Composable
private fun MyReviewsEmptyPreview() {
    PetAdoptionAppTheme {
        MyReviewsContent(uiState = MyReviewsUiState(isLoading = false), padding = PaddingValues(), onTabSelected = {}, onPostClick = {})
    }
}
