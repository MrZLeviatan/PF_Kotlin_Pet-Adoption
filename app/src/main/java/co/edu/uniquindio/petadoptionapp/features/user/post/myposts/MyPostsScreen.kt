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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.button.TextActionButton
import co.edu.uniquindio.petadoptionapp.core.component.card.PostMetaLine
import co.edu.uniquindio.petadoptionapp.core.component.card.PostSummaryCard
import co.edu.uniquindio.petadoptionapp.core.component.dialog.AppConfirmDialog
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
Pantalla "Mis publicaciones" con pestañas por estado.
Conecta el [MyPostsViewModel] con la UI; [MyPostsContent] solo dibuja.
No decide a dónde navegar: avisa y AppNavigation resuelve el destino.
 */
@Composable
fun MyPostsScreen(
    onBack: () -> Unit,
    onPostClick: (postId: String) -> Unit,
    viewModel: MyPostsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Al volver (por ejemplo, desde el detalle) se recarga por si algo cambió.
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    MyPostsContent(
        uiState = uiState,
        actions = MyPostsActions(
            onBack = onBack,
            onFilterSelected = viewModel::onFilterSelected,
            onPostClick = onPostClick,
            onFinishPost = viewModel::onFinishPost,
            onDeletePost = viewModel::onDeletePost,
            onConfirmAction = viewModel::onConfirmAction,
            onDismissAction = viewModel::onDismissAction,
        ),
    )
}

// Agrupa las acciones de la pantalla (mismo patrón que Registro y Configuración).
private data class MyPostsActions(
    val onBack: () -> Unit = {},
    val onFilterSelected: (PostFilter) -> Unit = {},
    val onPostClick: (String) -> Unit = {},
    val onFinishPost: (String) -> Unit = {},
    val onDeletePost: (String) -> Unit = {},
    val onConfirmAction: () -> Unit = {},
    val onDismissAction: () -> Unit = {},
)

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun MyPostsContent(
    uiState: MyPostsUiState,
    actions: MyPostsActions,
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
            onBack = actions.onBack,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        AppTabRow(
            tabs = tabLabels,
            selectedIndex = filters.indexOf(uiState.filter),
            onTabSelected = { index -> actions.onFilterSelected(filters[index]) },
        )

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
                    StatusMessage(
                        text = stringResource(R.string.my_posts_error),
                        isError = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                    )
                }
                sections.isEmpty() -> item(key = "empty") {
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
                        onClick = { actions.onPostClick(post.id) },
                        onFinish = { actions.onFinishPost(post.id) },
                        onDelete = { actions.onDeletePost(post.id) },
                    )
                }
            }
        }
    }

    // Confirmación antes de finalizar o eliminar (el caso de uso se llama al aceptar).
    uiState.pendingAction?.let { action ->
        val isFinish = action is PendingPostAction.Finish
        AppConfirmDialog(
            title = stringResource(if (isFinish) R.string.my_posts_finish_title else R.string.my_posts_delete_title),
            message = stringResource(if (isFinish) R.string.my_posts_finish_message else R.string.my_posts_delete_message),
            confirmText = stringResource(R.string.my_posts_dialog_accept),
            cancelText = stringResource(R.string.common_cancel),
            onConfirm = actions.onConfirmAction,
            onDismiss = actions.onDismissAction,
            isLoading = uiState.isProcessing,
        )
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
    // Sin verificar (pendiente o rechazada) se dice "Enviado"; ya publicada, "Publicado".
    val notPublished = post.status == PostStatusUi.PENDING || post.status == PostStatusUi.REJECTED

    val dateText = stringResource(
        if (notPublished) R.string.my_posts_sent else R.string.my_posts_published,
        ago,
    )
    val interestedText = pluralStringResource(
        R.plurals.my_posts_interested,
        post.interestedCount,
        post.interestedCount,
    )

    // Una publicación sin publicar aún no recibe interesados: no se muestra esa línea.
    val metaLines = buildList {
        add(PostMetaLine(Icons.Outlined.CalendarToday, dateText))
        if (!notPublished) add(PostMetaLine(Icons.Outlined.FavoriteBorder, interestedText))
    }

    val footer: (@Composable () -> Unit)? = when (post.status) {
        PostStatusUi.ACTIVE -> ({ ActiveFooter(onFinish = onFinish) })
        PostStatusUi.PENDING -> ({ DeleteFooter(onDelete = onDelete, note = stringResource(R.string.my_posts_not_editable)) })
        PostStatusUi.REJECTED -> ({
            DeleteFooter(
                onDelete = onDelete,
                note = post.rejectionReason?.let { stringResource(R.string.my_posts_rejected_reason, it) }.orEmpty(),
            )
        })
        // Finalizada: sin acciones, solo el aviso (no se edita ni se elimina).
        PostStatusUi.FINISHED -> ({ FinishedFooter() })
    }

    // Mientras carga (o si falla) se ve la imagen local.
    val fallback = painterResource(R.drawable.hero_image)
    val photo = rememberAsyncImagePainter(model = post.photoUrl, placeholder = fallback, error = fallback)

    PostSummaryCard(
        image = if (post.photoUrl == null) fallback else photo,
        typeLabel = post.categoryName,
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

// Aviso fijo de las finalizadas: el caso ya se cerró y la publicación queda como historial.
@Composable
private fun FinishedFooter() {
    IconTextRow(
        icon = Icons.Outlined.CheckCircle,
        text = stringResource(R.string.post_finished_notice),
        modifier = Modifier.padding(vertical = 12.dp),
        style = MaterialTheme.typography.labelMedium,
    )
}

// Eliminar a la izquierda y una nota a la derecha ("No editable en revisión" o el motivo del rechazo).
@Composable
private fun DeleteFooter(onDelete: () -> Unit, note: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextActionButton(
            text = stringResource(R.string.my_posts_delete),
            icon = Icons.Outlined.Delete,
            onClick = onDelete,
        )
        Text(
            text = note,
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.colors.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
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

// Traducen el estado (lógica) al texto, color o recurso (presentación).
@Composable
private fun PostStatusUi.color() = when (this) {
    PostStatusUi.ACTIVE -> AppTheme.colors.stateSuccess
    PostStatusUi.PENDING -> AppTheme.colors.stateWarning
    PostStatusUi.REJECTED -> AppTheme.colors.stateError
    PostStatusUi.FINISHED -> AppTheme.colors.stateDisabled
}

@StringRes
private fun PostStatusUi.sectionTitleRes() = when (this) {
    PostStatusUi.ACTIVE -> R.string.my_posts_section_active
    PostStatusUi.PENDING -> R.string.my_posts_section_pending
    PostStatusUi.REJECTED -> R.string.my_posts_section_rejected
    PostStatusUi.FINISHED -> R.string.my_posts_section_finished
}

@StringRes
private fun PostFilter.tabLabelRes() = when (this) {
    PostFilter.ALL -> R.string.my_posts_tab_all
    PostFilter.ACTIVE -> R.string.my_posts_tab_active
    PostFilter.PENDING -> R.string.my_posts_tab_pending
    PostFilter.FINISHED -> R.string.my_posts_tab_finished
}

// Datos de ejemplo solo para las previews.
private val previewPosts = listOf(
    MyPostUi("1", "Adopción", PostStatusUi.ACTIVE, "Luna busca un hogar lleno de amor", null, ageHours = 72, interestedCount = 12),
    MyPostUi("2", "Perdidos", PostStatusUi.ACTIVE, "Max, Chihuahua perdido en Usaquén", null, ageHours = 24, interestedCount = 5),
    MyPostUi("3", "Adopción", PostStatusUi.PENDING, "Simón, gatito juguetón de 8 meses", null, ageHours = 2),
    MyPostUi("4", "Adopción", PostStatusUi.REJECTED, "Cachorros a la venta", null, ageHours = 30,
        rejectionReason = "No se permite la venta de animales"),
)

@Preview(name = "Todas", showBackground = true, heightDp = 1100)
@Composable
private fun MyPostsAllPreview() {
    PetAdoptionAppTheme {
        MyPostsContent(
            uiState = MyPostsUiState(posts = previewPosts, isLoading = false),
            actions = MyPostsActions(),
        )
    }
}

@Preview(name = "Confirmar finalizar", showBackground = true, heightDp = 800)
@Composable
private fun MyPostsFinishDialogPreview() {
    PetAdoptionAppTheme {
        MyPostsContent(
            uiState = MyPostsUiState(
                posts = previewPosts,
                isLoading = false,
                pendingAction = PendingPostAction.Finish("1"),
            ),
            actions = MyPostsActions(),
        )
    }
}

@Preview(name = "Finalizadas (vacía)", showBackground = true, heightDp = 800)
@Composable
private fun MyPostsFinishedEmptyPreview() {
    PetAdoptionAppTheme {
        MyPostsContent(
            uiState = MyPostsUiState(filter = PostFilter.FINISHED, posts = previewPosts, isLoading = false),
            actions = MyPostsActions(),
        )
    }
}
