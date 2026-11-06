package co.edu.uniquindio.petadoptionapp.features.user.post.comments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.form.MessageInputBar
import co.edu.uniquindio.petadoptionapp.core.component.icon.UserAvatar
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.domain.model.comment.Comment

/**
Componentes visuales reutilizables de los comentarios.
Los usan la pantalla de comentarios (CommentsScreen) y la vista previa de comentarios del
detalle de la publicación (PostDetailScreen). No tienen ViewModel: reciben un CommentUi y
lambdas para avisar de las acciones (me gusta, responder, abrir hilo, abrir perfil del autor).
Incluye: CommentItem (un comentario), CommentThread (comentario con su hilo de respuestas),
RepliesToggle (enlace "Ver N respuestas"), CommentInputBar (barra para escribir) y
commentTimeText (texto relativo de antigüedad).
**/

/**
Un comentario: avatar, autor y tiempo, texto y acciones ("me gusta" y Responder).
[footer] es opcional para lo que va debajo (ej. "Ver 3 respuestas").
**/
@Composable
fun CommentItem(
    // Comentario que se va a dibujar.
    comment: CommentUi,
    // Acción al tocar el botón de "me gusta".
    onLikeClick: () -> Unit,
    // Acción al tocar "Responder".
    onReplyClick: () -> Unit,
    // Modificador externo para personalizar el contenedor.
    modifier: Modifier = Modifier,
    // false donde ya no se puede comentar (publicación finalizada o sin verificar).
    canReply: Boolean = true,
    // Resalta el comentario al que se está respondiendo.
    highlighted: Boolean = false,
    // Al tocar la foto o el nombre, abre el perfil de quien comentó.
    onAuthorClick: (() -> Unit)? = null,
    // Contenido opcional que se dibuja debajo del comentario.
    footer: (@Composable () -> Unit)? = null,
) {
    // Construye el modificador clicable del autor solo si hay acción para abrir su perfil.
    val authorModifier = if (onAuthorClick != null) {
        // Hace clicable el avatar/nombre con etiqueta de accesibilidad y rol de botón.
        Modifier.clickable(onClickLabel = stringResource(R.string.post_detail_view_profile), role = Role.Button, onClick = onAuthorClick)
    } else {
        // Sin acción, usa un modificador vacío.
        Modifier
    }
    // Fila principal: avatar a la izquierda y contenido a la derecha.
    Row(
        // Ocupa el ancho, pinta fondo si está resaltado y agrega margen interno en ese caso.
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (highlighted) AppTheme.colors.surfaceTag else Color.Transparent,
                RoundedCornerShape(16.dp),
            )
            .padding(if (highlighted) 8.dp else 0.dp),
        // Separación horizontal entre avatar y contenido.
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Avatar del autor del comentario.
        UserAvatar(
            // Nombre del autor (para las iniciales si no hay foto).
            name = comment.authorName,
            // Foto del autor.
            photoUrl = comment.authorPhotoUrl,
            // Tamaño del avatar.
            size = 36.dp,
            // Recorta en círculo y aplica el clic hacia el perfil.
            modifier = Modifier
                .clip(CircleShape)
                .then(authorModifier),
        )
        // Columna con nombre, texto y acciones; ocupa el espacio restante.
        Column(modifier = Modifier.weight(1f)) {
            // Fila con el nombre del autor y el tiempo transcurrido.
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Nombre del autor.
                Text(
                    // Texto con el nombre.
                    text = comment.authorName,
                    // Estilo de etiqueta grande.
                    style = MaterialTheme.typography.labelLarge,
                    // Color de texto principal.
                    color = AppTheme.colors.textPrimary,
                    // Clicable para abrir el perfil.
                    modifier = authorModifier,
                )
                // Tiempo transcurrido desde la publicación del comentario.
                Text(
                    // Texto relativo precedido por un punto separador.
                    text = "· ${commentTimeText(comment.ageMinutes)}",
                    // Estilo de cuerpo pequeño.
                    style = MaterialTheme.typography.bodySmall,
                    // Color atenuado.
                    color = AppTheme.colors.stateDisabled,
                )
            }
            // Pequeño espacio vertical.
            Spacer(Modifier.height(2.dp))
            // Cuerpo del comentario (texto, mención y marca de editado).
            CommentBody(comment)

            // Un comentario eliminado no se puede responder ni marcar con "me gusta".
            if (!comment.isDeleted) {
                // Fila de acciones.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Botón de "me gusta" con contador.
                    LikeButton(comment = comment, onClick = onLikeClick)
                    // Muestra "Responder" solo si se permite.
                    if (canReply) {
                        // Botón de texto para responder.
                        TextButton(onClick = onReplyClick) {
                            // Texto del botón.
                            Text(
                                // Texto "Responder".
                                text = stringResource(R.string.comment_reply),
                                // Estilo de etiqueta mediana.
                                style = MaterialTheme.typography.labelMedium,
                                // Color de texto principal.
                                color = AppTheme.colors.textPrimary,
                            )
                        }
                    }
                }
            }
            // Dibuja el pie opcional (por ejemplo "Ver respuestas").
            footer?.invoke()
        }
    }
}

// Texto del comentario: "@alias" delante en las respuestas aplanadas, o el aviso si fue eliminado.
@Composable
private fun CommentBody(comment: CommentUi) {
    // Si el comentario fue eliminado, muestra solo el aviso.
    if (comment.isDeleted) {
        // Texto del aviso de eliminado.
        Text(
            // Mensaje "Comentario eliminado".
            text = stringResource(R.string.comment_deleted),
            // Estilo en cursiva.
            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
            // Color atenuado.
            color = AppTheme.colors.stateDisabled,
        )
        // No dibuja nada más.
        return
    }
    // Respuestas: "@alias: respuesta", con el alias resaltado para ver a quién se contesta.
    val replyTo = comment.replyToUserName?.takeIf { it.isNotBlank() }
    // Texto del comentario con posible mención.
    Text(
        // Construye un texto con estilos mixtos.
        text = buildAnnotatedString {
            // Si es una respuesta, agrega la mención.
            if (replyTo != null) {
                // Agrega "@alias" en negrita y color de enlace.
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = AppTheme.colors.textLink)) { append("@$replyTo") }
                // Agrega el separador.
                append(": ")
            }
            // Agrega el texto del comentario.
            append(comment.text)
        },
        // Estilo de cuerpo mediano.
        style = MaterialTheme.typography.bodyMedium,
        // Color de texto principal.
        color = AppTheme.colors.textPrimary,
    )
    // Si fue editado, muestra la marca "Editado".
    if (comment.isEdited) {
        // Texto de la marca.
        Text(
            // Mensaje "Editado".
            text = stringResource(R.string.comment_edited),
            // Estilo de etiqueta pequeña.
            style = MaterialTheme.typography.labelSmall,
            // Color atenuado.
            color = AppTheme.colors.stateDisabled,
        )
    }
}

// Corazón con el contador: tocar da o quita el "me gusta" (uno por persona, también en el propio).
@Composable
private fun LikeButton(comment: CommentUi, onClick: () -> Unit) {
    // Botón de texto que contiene el ícono y el contador.
    TextButton(onClick = onClick) {
        // Ícono de corazón.
        Icon(
            // Corazón lleno si ya tiene "me gusta", vacío si no.
            imageVector = if (comment.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            // Descripción de accesibilidad según la acción que realizará.
            contentDescription = stringResource(
                if (comment.isLiked) R.string.comment_unlike else R.string.comment_like
            ),
            // Rojo si tiene "me gusta", color normal si no.
            tint = if (comment.isLiked) AppTheme.colors.stateError else AppTheme.colors.textPrimary,
            // Tamaño pequeño del ícono.
            modifier = Modifier.size(16.dp),
        )
        // Espacio entre ícono y contador.
        Spacer(Modifier.size(4.dp))
        // Contador de "me gusta".
        Text(
            // Número de "me gusta" como texto.
            text = comment.likeCount.toString(),
            // Estilo de etiqueta mediana.
            style = MaterialTheme.typography.labelMedium,
            // Color de texto principal.
            color = AppTheme.colors.textPrimary,
        )
    }
}

// Sangría de cada nivel de respuesta (avatar + espacio), para que el hilo se lea de un vistazo.
private val ReplyIndent = 48.dp

/**
Un comentario principal con su hilo, como en Facebook: solo el principal tiene
"Ver N respuestas"; al abrirlo aparecen TODAS las respuestas debajo, con sangría,
y al cerrarlo se ocultan. [highlightedId] resalta el comentario al que se responde.
**/
@Composable
fun CommentThread(
    // Comentario principal del hilo.
    comment: CommentUi,
    // Respuestas cargadas, indexadas por el id del comentario padre.
    replies: Map<String, List<CommentUi>>,
    // Ids de los hilos abiertos.
    expanded: Set<String>,
    // Acción de "me gusta" (recibe el id del comentario).
    onLike: (commentId: String) -> Unit,
    // Acción de responder (recibe el comentario).
    onReply: (CommentUi) -> Unit,
    // Acción de abrir/cerrar el hilo (recibe el id del comentario).
    onToggleReplies: (commentId: String) -> Unit,
    // Modificador externo.
    modifier: Modifier = Modifier,
    // Indica si se permite responder.
    canReply: Boolean = true,
    // Id del comentario a resaltar.
    highlightedId: String? = null,
    // Acción al tocar el autor (recibe el id del usuario).
    onAuthorClick: (userId: String) -> Unit = {},
) {
    // Calcula si el hilo de este comentario está abierto.
    val isExpanded = comment.id in expanded
    // Columna que contiene el comentario principal y sus respuestas.
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Dibuja el comentario principal.
        CommentItem(
            // Comentario principal.
            comment = comment,
            // Da "me gusta" al comentario principal.
            onLikeClick = { onLike(comment.id) },
            // Responde al comentario principal.
            onReplyClick = { onReply(comment) },
            // Permite responder según la bandera.
            canReply = canReply,
            // Resalta si es el comentario al que se responde.
            highlighted = comment.id == highlightedId,
            // Abre el perfil del autor.
            onAuthorClick = { onAuthorClick(comment.authorId) },
            // Solo agrega el enlace de respuestas si tiene alguna.
            footer = if (comment.replyCount > 0) {
                {
                    // Enlace "Ver N respuestas" / "Ocultar respuestas".
                    RepliesToggle(
                        // Cantidad de respuestas.
                        replyCount = comment.replyCount,
                        // Estado del hilo.
                        expanded = isExpanded,
                        // Abre o cierra el hilo.
                        onClick = { onToggleReplies(comment.id) },
                    )
                }
            } else null,
        )
        // Si el hilo está abierto, dibuja las respuestas.
        if (isExpanded) {
            // Columna con sangría para las respuestas.
            Column(
                // Sangría a la izquierda para distinguir el hilo.
                modifier = Modifier.padding(start = ReplyIndent),
                // Separación vertical entre respuestas.
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Las respuestas no tienen su propio "Ver respuestas": todo el hilo vive aquí.
                // Las guardadas antes de tener "a quién responde" contestan al comentario principal.
                replies[comment.id].orEmpty().forEach { stored ->
                    // Si la respuesta no tiene destinatario, se asume el autor del comentario principal.
                    val reply = if (stored.replyToUserName.isNullOrBlank()) stored.copy(replyToUserName = comment.authorName) else stored
                    // Dibuja la respuesta.
                    CommentItem(
                        // Respuesta a dibujar.
                        comment = reply,
                        // Da "me gusta" a la respuesta.
                        onLikeClick = { onLike(reply.id) },
                        // Responde a la respuesta.
                        onReplyClick = { onReply(reply) },
                        // Permite responder según la bandera.
                        canReply = canReply,
                        // Resalta si es la respuesta a la que se responde.
                        highlighted = reply.id == highlightedId,
                        // Abre el perfil del autor de la respuesta.
                        onAuthorClick = { onAuthorClick(reply.authorId) },
                    )
                }
            }
        }
    }
}

/**
Enlace "Ver 3 respuestas" / "Ocultar respuestas" debajo de un comentario.
**/
@Composable
fun RepliesToggle(replyCount: Int, expanded: Boolean, onClick: () -> Unit) {
    // Botón de texto que abre o cierra el hilo.
    TextButton(onClick = onClick) {
        // Texto del enlace.
        Text(
            // "Ocultar respuestas" si está abierto; si no, "Ver N respuestas" en plural correcto.
            text = if (expanded) stringResource(R.string.comment_hide_replies)
            else pluralStringResource(R.plurals.comment_view_replies, replyCount, replyCount),
            // Estilo de etiqueta mediana.
            style = MaterialTheme.typography.labelMedium,
            // Color de enlace.
            color = AppTheme.colors.textLink,
        )
    }
}

/**
Barra inferior para escribir: avatar de quien comenta, campo en forma de píldora y botón enviar.
Con [replyingTo] muestra "Respondiendo a @alias" con una X para cancelar.
**/
@Composable
fun CommentInputBar(
    // Nombre de quien escribe.
    viewerName: String,
    // Foto de quien escribe.
    viewerPhotoUrl: String?,
    // Texto actual del campo.
    text: String,
    // Acción al cambiar el texto.
    onTextChange: (String) -> Unit,
    // Acción de enviar.
    onSend: () -> Unit,
    // Modificador externo.
    modifier: Modifier = Modifier,
    // Nombre del autor al que se responde; null si es comentario nuevo.
    replyingTo: String? = null,
    // Acción de cancelar la respuesta.
    onCancelReply: () -> Unit = {},
    // Indica si se está enviando.
    isSending: Boolean = false,
) {
    // Barra de mensaje genérica reutilizada del sistema de componentes.
    MessageInputBar(
        // Texto del campo.
        text = text,
        // Acción al escribir.
        onTextChange = onTextChange,
        // Acción de enviar.
        onSend = onSend,
        // Texto de ayuda del campo.
        placeholder = stringResource(R.string.comment_placeholder),
        // Descripción de accesibilidad del botón enviar.
        sendDescription = stringResource(R.string.comment_send),
        // Longitud máxima permitida para un comentario.
        maxLength = Comment.MAX_LENGTH,
        // Modificador externo.
        modifier = modifier,
        // Estado de envío.
        isSending = isSending,
        // Contenido a la izquierda del campo: el avatar de quien escribe.
        leading = {
            // Avatar del usuario actual.
            UserAvatar(
                // Nombre del usuario.
                name = viewerName,
                // Foto del usuario.
                photoUrl = viewerPhotoUrl,
                // Tamaño del avatar.
                size = 36.dp,
                // Amarillo sobre la barra marrón, como en el diseño.
                containerColor = AppTheme.colors.actionPrimary,
                // Color del contenido (iniciales) sobre el fondo amarillo.
                contentColor = AppTheme.colors.onActionPrimary,
            )
        },
        // Encabezado opcional: solo aparece cuando se está respondiendo a alguien.
        header = replyingTo?.let { name ->
            {
                // Fila con el texto "Respondiendo a" y el botón para cancelar.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Texto que indica a quién se responde.
                    Text(
                        // "Respondiendo a @alias".
                        text = stringResource(R.string.comment_replying_to, name),
                        // Estilo de etiqueta mediana.
                        style = MaterialTheme.typography.labelMedium,
                        // Color de texto principal.
                        color = AppTheme.colors.textPrimary,
                        // Ocupa el espacio disponible.
                        modifier = Modifier.weight(1f),
                    )
                    // Botón para cancelar la respuesta.
                    IconButton(onClick = onCancelReply) {
                        // Ícono de cerrar (X).
                        Icon(
                            // Ícono de cerrar.
                            Icons.Outlined.Close,
                            // Descripción de accesibilidad.
                            contentDescription = stringResource(R.string.comment_cancel_reply),
                            // Color del ícono.
                            tint = AppTheme.colors.textPrimary,
                        )
                    }
                }
            }
        },
    )
}

// Minutos → "Ahora", "Hace 5 min", "Hace 3 h", "Ayer", "Hace 4 días" (mismos textos que Notificaciones).
@Composable
fun commentTimeText(ageMinutes: Int): String = when {
    // Menos de un minuto: "Ahora".
    ageMinutes < 1 -> stringResource(R.string.comment_time_now)
    // Menos de una hora: minutos.
    ageMinutes < 60 -> stringResource(R.string.notifications_time_minutes, ageMinutes)
    // Menos de un día: horas.
    ageMinutes < 24 * 60 -> stringResource(R.string.notifications_time_hours, ageMinutes / 60)
    // Menos de dos días: "Ayer".
    ageMinutes < 48 * 60 -> stringResource(R.string.notifications_time_yesterday)
    // En otro caso: días.
    else -> {
        // Calcula los días completos transcurridos.
        val days = ageMinutes / (24 * 60)
        // Devuelve el texto en plural según la cantidad de días.
        pluralStringResource(R.plurals.notifications_time_days, days, days)
    }
}
