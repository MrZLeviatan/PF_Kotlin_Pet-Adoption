package co.edu.uniquindio.petadoptionapp.features.user.post.comments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.icon.UserAvatar
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.domain.model.comment.Comment

/**
 Un comentario: avatar, autor y tiempo, texto y acciones ("me gusta" y Responder).
 [footer] es opcional para lo que va debajo (ej. "Ver 3 respuestas").
 */
@Composable
fun CommentItem(
    comment: CommentUi,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
    modifier: Modifier = Modifier,
    // false donde ya no se puede comentar (publicación finalizada o sin verificar).
    canReply: Boolean = true,
    footer: (@Composable () -> Unit)? = null,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        UserAvatar(name = comment.authorName, photoUrl = comment.authorPhotoUrl, size = 36.dp)
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = comment.authorName,
                    style = MaterialTheme.typography.labelLarge,
                    color = AppTheme.colors.textPrimary,
                )
                Text(
                    text = "· ${commentTimeText(comment.ageMinutes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.stateDisabled,
                )
            }
            Spacer(Modifier.height(2.dp))
            CommentBody(comment)

            // Un comentario eliminado no se puede responder ni marcar con "me gusta".
            if (!comment.isDeleted) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LikeButton(comment = comment, onClick = onLikeClick)
                    if (canReply) {
                        TextButton(onClick = onReplyClick) {
                            Text(
                                text = stringResource(R.string.comment_reply),
                                style = MaterialTheme.typography.labelMedium,
                                color = AppTheme.colors.textPrimary,
                            )
                        }
                    }
                }
            }
            footer?.invoke()
        }
    }
}

// Texto del comentario: "@alias" delante en las respuestas aplanadas, o el aviso si fue eliminado.
@Composable
private fun CommentBody(comment: CommentUi) {
    if (comment.isDeleted) {
        Text(
            text = stringResource(R.string.comment_deleted),
            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
            color = AppTheme.colors.stateDisabled,
        )
        return
    }
    val prefix = comment.replyToUserName?.takeIf { comment.depth >= Comment.MAX_DEPTH }?.let { "@$it " }.orEmpty()
    Text(
        text = prefix + comment.text,
        style = MaterialTheme.typography.bodyMedium,
        color = AppTheme.colors.textPrimary,
    )
    if (comment.isEdited) {
        Text(
            text = stringResource(R.string.comment_edited),
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.colors.stateDisabled,
        )
    }
}

// Corazón con el contador: tocar da o quita el "me gusta" (uno por persona, también en el propio).
@Composable
private fun LikeButton(comment: CommentUi, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Icon(
            imageVector = if (comment.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = stringResource(
                if (comment.isLiked) R.string.comment_unlike else R.string.comment_like
            ),
            tint = if (comment.isLiked) AppTheme.colors.stateError else AppTheme.colors.textPrimary,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.size(4.dp))
        Text(
            text = comment.likeCount.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = AppTheme.colors.textPrimary,
        )
    }
}

// Sangría de cada nivel de respuesta (avatar + espacio), para que el hilo se lea de un vistazo.
private val ReplyIndent = 48.dp

/**
 Un comentario con su hilo: "Ver N respuestas" va justo debajo de ESE comentario y, al abrirlo,
 sus respuestas aparecen con sangría (y cada una puede tener las suyas).
 [replies] y [expanded] vienen del ViewModel; lo usan el detalle y la pantalla de comentarios.
 */
@Composable
fun CommentThread(
    comment: CommentUi,
    replies: Map<String, List<CommentUi>>,
    expanded: Set<String>,
    onLike: (commentId: String) -> Unit,
    onReply: (CommentUi) -> Unit,
    onToggleReplies: (commentId: String) -> Unit,
    modifier: Modifier = Modifier,
    canReply: Boolean = true,
) {
    val isExpanded = comment.id in expanded
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CommentItem(
            comment = comment,
            onLikeClick = { onLike(comment.id) },
            onReplyClick = { onReply(comment) },
            canReply = canReply,
            footer = if (comment.replyCount > 0) {
                {
                    RepliesToggle(
                        replyCount = comment.replyCount,
                        expanded = isExpanded,
                        onClick = { onToggleReplies(comment.id) },
                    )
                }
            } else null,
        )
        if (isExpanded) {
            Column(
                modifier = Modifier.padding(start = ReplyIndent),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                replies[comment.id].orEmpty().forEach { reply ->
                    CommentThread(
                        comment = reply,
                        replies = replies,
                        expanded = expanded,
                        onLike = onLike,
                        onReply = onReply,
                        onToggleReplies = onToggleReplies,
                        canReply = canReply,
                    )
                }
            }
        }
    }
}

/** Enlace "Ver 3 respuestas" / "Ocultar respuestas" debajo de un comentario. */
@Composable
fun RepliesToggle(replyCount: Int, expanded: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(
            text = if (expanded) stringResource(R.string.comment_hide_replies)
            else pluralStringResource(R.plurals.comment_view_replies, replyCount, replyCount),
            style = MaterialTheme.typography.labelMedium,
            color = AppTheme.colors.textLink,
        )
    }
}

/**
 Barra inferior para escribir: avatar de quien comenta, campo en forma de píldora y botón enviar.
 Con [replyingTo] muestra "Respondiendo a @alias" con una X para cancelar.
 */
@Composable
fun CommentInputBar(
    viewerName: String,
    viewerPhotoUrl: String?,
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
    replyingTo: String? = null,
    onCancelReply: () -> Unit = {},
    isSending: Boolean = false,
) {
    val canSend = text.isNotBlank() && !isSending
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.brand)
            // El color llega hasta abajo sin tapar los botones del sistema.
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        if (replyingTo != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.comment_replying_to, replyingTo),
                    style = MaterialTheme.typography.labelMedium,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onCancelReply) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.comment_cancel_reply),
                        tint = AppTheme.colors.textPrimary,
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            UserAvatar(
                name = viewerName,
                photoUrl = viewerPhotoUrl,
                size = 36.dp,
                // Amarillo sobre la barra marrón, como en el diseño.
                containerColor = AppTheme.colors.actionPrimary,
                contentColor = AppTheme.colors.onActionPrimary,
            )
            OutlinedTextField(
                value = text,
                // El máximo lo define el dominio: no se deja escribir más allá.
                onValueChange = { if (it.length <= Comment.MAX_LENGTH) onTextChange(it) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp),
                placeholder = {
                    Text(stringResource(R.string.comment_placeholder), style = MaterialTheme.typography.bodyMedium)
                },
                maxLines = 4,
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (canSend) onSend() }),
                trailingIcon = {
                    FilledIconButton(
                        onClick = onSend,
                        enabled = canSend,
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = AppTheme.colors.actionPrimary,
                            contentColor = AppTheme.colors.onActionPrimary,
                            disabledContainerColor = AppTheme.colors.surfaceTag,
                            disabledContentColor = AppTheme.colors.stateDisabled,
                        ),
                    ) {
                        Icon(
                            Icons.Filled.ArrowUpward,
                            contentDescription = stringResource(R.string.comment_send),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AppTheme.colors.background,
                    unfocusedContainerColor = AppTheme.colors.background,
                    focusedBorderColor = Color.Transparent,     // Sin borde, como en el diseño
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = AppTheme.colors.textPrimary,
                    unfocusedTextColor = AppTheme.colors.textPrimary,
                    cursorColor = AppTheme.colors.textPrimary,
                    focusedPlaceholderColor = AppTheme.colors.stateDisabled,
                    unfocusedPlaceholderColor = AppTheme.colors.stateDisabled,
                ),
            )
        }
    }
}

// Minutos → "Ahora", "Hace 5 min", "Hace 3 h", "Ayer", "Hace 4 días" (mismos textos que Notificaciones).
@Composable
fun commentTimeText(ageMinutes: Int): String = when {
    ageMinutes < 1 -> stringResource(R.string.comment_time_now)
    ageMinutes < 60 -> stringResource(R.string.notifications_time_minutes, ageMinutes)
    ageMinutes < 24 * 60 -> stringResource(R.string.notifications_time_hours, ageMinutes / 60)
    ageMinutes < 48 * 60 -> stringResource(R.string.notifications_time_yesterday)
    else -> {
        val days = ageMinutes / (24 * 60)
        pluralStringResource(R.plurals.notifications_time_days, days, days)
    }
}
