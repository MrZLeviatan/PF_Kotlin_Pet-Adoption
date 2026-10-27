package co.edu.uniquindio.petadoptionapp.features.user.myposts

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.button.TextActionButton
import co.edu.uniquindio.petadoptionapp.core.component.card.PostMetaLine
import co.edu.uniquindio.petadoptionapp.core.component.card.PostSummaryCard
import co.edu.uniquindio.petadoptionapp.core.component.header.AppTopBar
import co.edu.uniquindio.petadoptionapp.core.component.navigation.AppTabRow
import co.edu.uniquindio.petadoptionapp.core.component.text.StatusMessage
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

private val ScreenHorizontalPadding = 24.dp

/**
Pantalla "Mis publicaciones" con pestañas por estado.
Conecta el [MyPostsViewModel] con la UI; [MyPostsContent] solo dibuja.
No decide a dónde navegar: avisa y AppNavigation resuelve el destino.
 */
@Composable
fun MyPostsScreen(
    onBack: () -> Unit,
    onPostClick: (postId: String) -> Unit,
    viewModel: MyPostsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MyPostsContent(
        uiState = uiState,
        onBack = onBack,
        onFilterSelected = viewModel::onFilterSelected,
        onPostClick = onPostClick,
        onFinishPost = viewModel::onFinishPost,
        onDeletePost = viewModel::onDeletePost,
    )
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun MyPostsContent(
    uiState: MyPostsUiState,
    onBack: () -> Unit,
    onFilterSelected: (PostFilter) -> Unit,
    onPostClick: (String) -> Unit,
    onFinishPost: (String) -> Unit,
    onDeletePost: (String) -> Unit,
) {
    val filters = PostFilter.entries
    val tabLabels = filters.map { filter ->
        stringResource(filter.tabLabelRes(), uiState.countOf(filter))
    }
    val sections = uiState.visibleSections

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .systemBarsPadding(),
    ) {
        AppTopBar(
            title = stringResource(R.string.my_posts_title),
            onBack = onBack,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        AppTabRow(
            tabs = tabLabels,
            selectedIndex = filters.indexOf(uiState.filter),
            onTabSelected = { index -> onFilterSelected(filters[index]) },
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (sections.isEmpty()) {
                item(key = "empty") {
                    StatusMessage(
                        text = stringResource(R.string.my_posts_empty),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                    )
                }
            }

            sections.forEach { (status, posts) ->
                item(key = "header-$status") {
                    SectionHeader(status = status, count = posts.size)
                }
                items(posts, key = { it.id }) { post ->
                    MyPostItem(
                        post = post,
                        onClick = { onPostClick(post.id) },
                        onFinish = { onFinishPost(post.id) },
                        onDelete = { onDeletePost(post.id) },
                    )
                }
            }
        }
    }
}

// Encabezado de grupo: punto de color y "Activas (2)".
@Composable
private fun SectionHeader(status: PostStatusUi, count: Int) {
    Row(
        modifier = Modifier.padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(status.color(), CircleShape),
        )
        Text(
            text = stringResource(status.sectionTitleRes(), count),
            style = MaterialTheme.typography.titleSmall,
            color = AppTheme.colors.textPrimary,
        )
    }
}

// Convierte el modelo de la pantalla en la tarjeta: textos, iconos y pie según el estado.
@Composable
private fun MyPostItem(
    post: MyPostUi,
    onClick: () -> Unit,
    onFinish: () -> Unit,
    onDelete: () -> Unit,
) {
    val ago = agoText(post.ageHours)
    val isPending = post.status == PostStatusUi.PENDING

    val dateText = stringResource(
        if (isPending) R.string.my_posts_sent else R.string.my_posts_published,
        ago,
    )
    val interestedText = pluralStringResource(
        R.plurals.my_posts_interested,
        post.interestedCount,
        post.interestedCount,
    )

    // Una publicación en revisión aún no recibe interesados: no se muestra esa línea.
    val metaLines = buildList {
        add(PostMetaLine(Icons.Outlined.CalendarToday, dateText))
        if (!isPending) add(PostMetaLine(Icons.Outlined.FavoriteBorder, interestedText))
    }

    val footer: (@Composable () -> Unit)? = when (post.status) {
        PostStatusUi.ACTIVE -> ({ ActiveFooter(onFinish = onFinish) })
        PostStatusUi.PENDING -> ({ PendingFooter(onDelete = onDelete) })
        PostStatusUi.FINISHED -> null
    }

    PostSummaryCard(
        // TODO: cargar la foto real desde una URL (Coil). Por ahora, imagen local de prueba.
        image = painterResource(R.drawable.hero_image),
        typeLabel = stringResource(post.type.labelRes()),
        statusColor = post.status.color(),
        title = post.title,
        metaLines = metaLines,
        onClick = onClick,
        footer = footer,
    )
}

@Composable
private fun ActiveFooter(onFinish: () -> Unit) {
    TextActionButton(
        text = stringResource(R.string.my_posts_finish),
        icon = Icons.Outlined.CheckCircle,
        onClick = onFinish,
        contentColor = AppTheme.colors.actionSecondary,
    )
}

@Composable
private fun PendingFooter(onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextActionButton(
            text = stringResource(R.string.my_posts_delete),
            icon = Icons.Outlined.Delete,
            onClick = onDelete,
        )
        Text(
            text = stringResource(R.string.my_posts_not_editable),
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.colors.textPrimary,
        )
    }
}

// Menos de 24 horas se muestra en horas; desde ahí, en días.
@Composable
private fun agoText(ageHours: Int): String =
    if (ageHours < 24) {
        pluralStringResource(R.plurals.common_time_hours_ago, ageHours, ageHours)
    } else {
        val days = ageHours / 24
        pluralStringResource(R.plurals.common_time_days_ago, days, days)
    }

// Traducen el tipo (lógica) al texto, color o recurso (presentación).
@Composable
private fun PostStatusUi.color() = when (this) {
    PostStatusUi.ACTIVE -> AppTheme.colors.stateSuccess
    PostStatusUi.PENDING -> AppTheme.colors.stateWarning
    PostStatusUi.FINISHED -> AppTheme.colors.stateDisabled
}

@StringRes
private fun PostStatusUi.sectionTitleRes() = when (this) {
    PostStatusUi.ACTIVE -> R.string.my_posts_section_active
    PostStatusUi.PENDING -> R.string.my_posts_section_pending
    PostStatusUi.FINISHED -> R.string.my_posts_section_finished
}

@StringRes
private fun PostTypeUi.labelRes() = when (this) {
    PostTypeUi.ADOPTION -> R.string.my_posts_type_adoption
    PostTypeUi.LOST -> R.string.my_posts_type_lost
}

@StringRes
private fun PostFilter.tabLabelRes() = when (this) {
    PostFilter.ALL -> R.string.my_posts_tab_all
    PostFilter.ACTIVE -> R.string.my_posts_tab_active
    PostFilter.PENDING -> R.string.my_posts_tab_pending
    PostFilter.FINISHED -> R.string.my_posts_tab_finished
}

@Preview(name = "Todas", showBackground = true, heightDp = 800)
@Composable
private fun MyPostsAllPreview() {
    PetAdoptionAppTheme {
        MyPostsContent(
            uiState = MyPostsUiState(),
            onBack = {}, onFilterSelected = {}, onPostClick = {},
            onFinishPost = {}, onDeletePost = {},
        )
    }
}

@Preview(name = "Solo activas", showBackground = true, heightDp = 800)
@Composable
private fun MyPostsActivePreview() {
    PetAdoptionAppTheme {
        MyPostsContent(
            uiState = MyPostsUiState(filter = PostFilter.ACTIVE),
            onBack = {}, onFilterSelected = {}, onPostClick = {},
            onFinishPost = {}, onDeletePost = {},
        )
    }
}

@Preview(name = "Finalizadas (vacía)", showBackground = true, heightDp = 800)
@Composable
private fun MyPostsFinishedEmptyPreview() {
    PetAdoptionAppTheme {
        MyPostsContent(
            uiState = MyPostsUiState(filter = PostFilter.FINISHED),
            onBack = {}, onFilterSelected = {}, onPostClick = {},
            onFinishPost = {}, onDeletePost = {},
        )
    }
}