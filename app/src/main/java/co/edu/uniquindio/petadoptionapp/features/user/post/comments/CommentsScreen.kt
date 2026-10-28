package co.edu.uniquindio.petadoptionapp.features.user.post.comments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.header.AppTopBar
import co.edu.uniquindio.petadoptionapp.core.component.text.ErrorText
import co.edu.uniquindio.petadoptionapp.core.component.text.StatusMessage
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

private val ScreenHorizontalPadding = 16.dp

/**
Todos los comentarios de una publicación, al estilo de Facebook: los principales, cada uno
con "Ver N respuestas" para abrir su hilo, y la barra inferior para comentar o responder.
Conecta el [CommentsViewModel] con la UI; [CommentsContent] solo dibuja.
 */
@Composable
fun CommentsScreen(
    onBack: () -> Unit,
    onAuthorClick: (userId: String) -> Unit,
    viewModel: CommentsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CommentsContent(
        uiState = uiState,
        actions = CommentsActions(
            onBack = onBack,
            onAuthorClick = onAuthorClick,
            onLike = viewModel::onLikeClick,
            onReply = viewModel::onReplyClick,
            onToggleReplies = viewModel::onToggleReplies,
            onLoadMore = viewModel::onLoadMore,
            onTextChange = viewModel::onTextChange,
            onSend = viewModel::onSend,
            onCancelReply = viewModel::onCancelReply,
        ),
    )
}

// Agrupa las acciones de la pantalla (mismo patrón que Registro y Configuración).
private data class CommentsActions(
    val onBack: () -> Unit = {},
    val onAuthorClick: (String) -> Unit = {},
    val onLike: (String) -> Unit = {},
    val onReply: (CommentUi) -> Unit = {},
    val onToggleReplies: (String) -> Unit = {},
    val onLoadMore: () -> Unit = {},
    val onTextChange: (String) -> Unit = {},
    val onSend: () -> Unit = {},
    val onCancelReply: () -> Unit = {},
)

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun CommentsContent(
    uiState: CommentsUiState,
    actions: CommentsActions,
) {
    val listState = rememberLazyListState()

    // Al elegir a quién responder (o al llegar desde "Responder" del detalle), baja hasta ese comentario.
    // Si es una respuesta, baja hasta su hilo (el comentario principal).
    val target = uiState.replyingTo
    LaunchedEffect(target?.id) {
        if (target == null) return@LaunchedEffect
        val index = uiState.comments.indexOfFirst { it.id == target.id || it.id == target.parentId }
        if (index >= 0) listState.animateScrollToItem(index)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .statusBarsPadding()
            .imePadding(),
    ) {
        AppTopBar(
            title = stringResource(R.string.comments_title, uiState.totalComments),
            onBack = actions.onBack,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = ScreenHorizontalPadding, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when {
                uiState.isLoading -> Unit
                uiState.hasError -> item(key = "error") {
                    CenteredStatus(stringResource(R.string.comments_error), isError = true)
                }
                uiState.comments.isEmpty() -> item(key = "empty") {
                    CenteredStatus(
                        stringResource(if (uiState.canComment) R.string.post_detail_no_comments else R.string.post_detail_comments_closed)
                    )
                }
            }

            items(uiState.comments, key = { it.id }) { comment ->
                CommentThread(
                    comment = comment,
                    replies = uiState.replies,
                    expanded = uiState.expanded,
                    onLike = actions.onLike,
                    onReply = actions.onReply,
                    onToggleReplies = actions.onToggleReplies,
                    canReply = uiState.canComment,
                    highlightedId = uiState.replyingTo?.id,
                    onAuthorClick = actions.onAuthorClick,
                )
            }

            if (uiState.canLoadMore) {
                item(key = "more") {
                    TextButton(onClick = actions.onLoadMore, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.comments_load_more), color = AppTheme.colors.textLink)
                    }
                }
            }
        }

        if (uiState.sendError) {
            ErrorText(
                text = stringResource(R.string.post_detail_msg_comment_error),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            )
        }

        if (uiState.canComment) {
            CommentInputBar(
                viewerName = uiState.viewerName,
                viewerPhotoUrl = uiState.viewerPhotoUrl,
                text = uiState.text,
                onTextChange = actions.onTextChange,
                onSend = actions.onSend,
                replyingTo = uiState.replyingTo?.authorName,
                onCancelReply = actions.onCancelReply,
                isSending = uiState.isSending,
            )
        }
    }
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

// Datos de ejemplo solo para las previews.
private fun previewComment(id: String, name: String, text: String, depth: Int = 0, replies: Int = 0, parent: String? = null) =
    CommentUi(id, parent, depth, "a$id", name, null, text, null, 90, 3, replies,
        isLiked = false, isOwn = false, isDeleted = false, isEdited = false)

@Preview(name = "Comentarios con hilo abierto", showBackground = true, heightDp = 900)
@Composable
private fun CommentsContentPreview() {
    PetAdoptionAppTheme {
        CommentsContent(
            uiState = CommentsUiState(
                totalComments = 4,
                comments = listOf(
                    previewComment("c1", "María García", "¡Qué hermosa Luna! ¿Tienen horarios de visita?", replies = 2),
                    previewComment("c2", "Carlos Rodríguez", "Ya contacté a la fundación. Recomiendo visitarla."),
                ),
                replies = mapOf(
                    "c1" to listOf(
                        previewComment("r1", "Ana Martínez", "Sí, los sábados de 9 a 12.", depth = 1, parent = "c1"),
                        previewComment("r2", "Luis Gómez", "¡Gracias por la info!", depth = 1, parent = "c1"),
                    )
                ),
                expanded = setOf("c1"),
                isLoading = false,
                canComment = true,
                viewerName = "ana_77",
                replyingTo = previewComment("c2", "Carlos Rodríguez", ""),
            ),
            actions = CommentsActions(),
        )
    }
}
