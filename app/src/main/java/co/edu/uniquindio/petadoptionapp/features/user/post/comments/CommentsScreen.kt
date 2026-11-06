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

/**
Pantalla de comentarios de una publicación.
Muestra la barra superior con el total de comentarios, la lista paginada de comentarios
principales (cada uno con su hilo de respuestas desplegable), un botón para cargar más y la
barra inferior para escribir un comentario o responder a uno existente.
Observa el estado CommentsUiState del CommentsViewModel y le envía los eventos del usuario
(me gusta, responder, abrir hilo, cargar más, escribir, enviar, cancelar respuesta).
Navegación: se abre desde el detalle de la publicación con la ruta MainRoutes.PostComments;
`onBack` regresa a la pantalla anterior y `onAuthorClick` abre el perfil del autor.
**/

// Margen horizontal de la lista de comentarios.
private val ScreenHorizontalPadding = 16.dp

/**
Todos los comentarios de una publicación, al estilo de Facebook: los principales, cada uno
con "Ver N respuestas" para abrir su hilo, y la barra inferior para comentar o responder.
Conecta el [CommentsViewModel] con la UI; [CommentsContent] solo dibuja.
**/
@Composable
fun CommentsScreen(
    // Acción para volver a la pantalla anterior.
    onBack: () -> Unit,
    // Acción para abrir el perfil del autor de un comentario.
    onAuthorClick: (userId: String) -> Unit,
    // ViewModel inyectado por Hilt.
    viewModel: CommentsViewModel = hiltViewModel(),
) {
    // Observa el estado del ViewModel respetando el ciclo de vida.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Dibuja el contenido con el estado y las acciones conectadas al ViewModel.
    CommentsContent(
        // Estado actual de la pantalla.
        uiState = uiState,
        // Agrupa las acciones de la pantalla.
        actions = CommentsActions(
            // Volver atrás.
            onBack = onBack,
            // Abrir el perfil del autor.
            onAuthorClick = onAuthorClick,
            // Dar o quitar "me gusta" a un comentario.
            onLike = viewModel::onLikeClick,
            // Elegir un comentario para responder.
            onReply = viewModel::onReplyClick,
            // Abrir o cerrar el hilo de respuestas.
            onToggleReplies = viewModel::onToggleReplies,
            // Cargar la siguiente página de comentarios.
            onLoadMore = viewModel::onLoadMore,
            // Actualizar el texto escrito.
            onTextChange = viewModel::onTextChange,
            // Enviar el comentario.
            onSend = viewModel::onSend,
            // Cancelar la respuesta en curso.
            onCancelReply = viewModel::onCancelReply,
        ),
    )
}

// Agrupa las acciones de la pantalla (mismo patrón que Registro y Configuración).
private data class CommentsActions(
    // Acción de volver atrás.
    val onBack: () -> Unit = {},
    // Acción al tocar el autor de un comentario (recibe su id).
    val onAuthorClick: (String) -> Unit = {},
    // Acción de "me gusta" (recibe el id del comentario).
    val onLike: (String) -> Unit = {},
    // Acción de responder (recibe el comentario).
    val onReply: (CommentUi) -> Unit = {},
    // Acción de abrir/cerrar respuestas (recibe el id del comentario).
    val onToggleReplies: (String) -> Unit = {},
    // Acción de cargar más comentarios.
    val onLoadMore: () -> Unit = {},
    // Acción al cambiar el texto del campo.
    val onTextChange: (String) -> Unit = {},
    // Acción de enviar el comentario.
    val onSend: () -> Unit = {},
    // Acción de cancelar la respuesta.
    val onCancelReply: () -> Unit = {},
)

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun CommentsContent(
    // Estado que se va a dibujar.
    uiState: CommentsUiState,
    // Acciones que dispara la interfaz.
    actions: CommentsActions,
) {
    // Estado de desplazamiento de la lista, usado para bajar hasta un comentario.
    val listState = rememberLazyListState()

    // Al elegir a quién responder (o al llegar desde "Responder" del detalle), baja hasta ese comentario.
    // Si es una respuesta, baja hasta su hilo (el comentario principal).
    val target = uiState.replyingTo
    // Efecto que se relanza cada vez que cambia el comentario al que se responde.
    LaunchedEffect(target?.id) {
        // Si no se está respondiendo a nadie, no hace nada.
        if (target == null) return@LaunchedEffect
        // Busca la posición del comentario o de su comentario principal.
        val index = uiState.comments.indexOfFirst { it.id == target.id || it.id == target.parentId }
        // Si lo encontró, desplaza la lista con animación hasta él.
        if (index >= 0) listState.animateScrollToItem(index)
    }

    // Columna principal que organiza barra superior, lista y barra de escritura.
    Column(
        // Ocupa toda la pantalla, aplica el fondo y respeta la barra de estado y el teclado.
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .statusBarsPadding()
            .imePadding(),
    ) {
        // Barra superior con el título y el botón de volver.
        AppTopBar(
            // Título con la cantidad total de comentarios.
            title = stringResource(R.string.comments_title, uiState.totalComments),
            // Acción del botón de volver.
            onBack = actions.onBack,
            // Margen horizontal de la barra.
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        // Lista perezosa con los comentarios.
        LazyColumn(
            // Estado de desplazamiento compartido con el LaunchedEffect.
            state = listState,
            // Ocupa el ancho y todo el alto restante.
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            // Márgenes internos del contenido de la lista.
            contentPadding = PaddingValues(horizontal = ScreenHorizontalPadding, vertical = 8.dp),
            // Separación vertical entre elementos.
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Elige qué mensaje de estado mostrar, si corresponde.
            when {
                // Mientras carga no muestra nada.
                uiState.isLoading -> Unit
                // Si hubo error, muestra el mensaje de error.
                uiState.hasError -> item(key = "error") {
                    // Mensaje centrado de error.
                    CenteredStatus(stringResource(R.string.comments_error), isError = true)
                }
                // Si no hay comentarios, muestra un mensaje vacío.
                uiState.comments.isEmpty() -> item(key = "empty") {
                    // Mensaje centrado: "sin comentarios" o "comentarios cerrados" según se pueda comentar.
                    CenteredStatus(
                        stringResource(if (uiState.canComment) R.string.post_detail_no_comments else R.string.post_detail_comments_closed)
                    )
                }
            }

            // Dibuja cada comentario principal con su hilo, usando el id como clave estable.
            items(uiState.comments, key = { it.id }) { comment ->
                // Hilo del comentario con sus respuestas.
                CommentThread(
                    // Comentario principal.
                    comment = comment,
                    // Mapa de respuestas cargadas.
                    replies = uiState.replies,
                    // Hilos abiertos.
                    expanded = uiState.expanded,
                    // Acción de "me gusta".
                    onLike = actions.onLike,
                    // Acción de responder.
                    onReply = actions.onReply,
                    // Acción de abrir/cerrar respuestas.
                    onToggleReplies = actions.onToggleReplies,
                    // Solo permite responder si se puede comentar.
                    canReply = uiState.canComment,
                    // Resalta el comentario al que se está respondiendo.
                    highlightedId = uiState.replyingTo?.id,
                    // Acción al tocar el autor.
                    onAuthorClick = actions.onAuthorClick,
                )
            }

            // Si hay más páginas, muestra el botón de cargar más.
            if (uiState.canLoadMore) {
                // Elemento de la lista con clave fija.
                item(key = "more") {
                    // Botón de texto que carga la siguiente página.
                    TextButton(onClick = actions.onLoadMore, modifier = Modifier.fillMaxWidth()) {
                        // Texto del botón con color de enlace.
                        Text(stringResource(R.string.comments_load_more), color = AppTheme.colors.textLink)
                    }
                }
            }
        }

        // Si falló el envío, muestra el mensaje de error encima de la barra de escritura.
        if (uiState.sendError) {
            // Texto de error.
            ErrorText(
                // Mensaje de error al comentar.
                text = stringResource(R.string.post_detail_msg_comment_error),
                // Ocupa el ancho con margen.
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            )
        }

        // Solo muestra la barra de escritura si se permite comentar.
        if (uiState.canComment) {
            // Barra inferior para escribir el comentario o la respuesta.
            CommentInputBar(
                // Nombre de quien escribe (para el avatar).
                viewerName = uiState.viewerName,
                // Foto de quien escribe.
                viewerPhotoUrl = uiState.viewerPhotoUrl,
                // Texto actual.
                text = uiState.text,
                // Acción al cambiar el texto.
                onTextChange = actions.onTextChange,
                // Acción de enviar.
                onSend = actions.onSend,
                // Nombre del autor al que se responde, si hay respuesta en curso.
                replyingTo = uiState.replyingTo?.authorName,
                // Acción de cancelar la respuesta.
                onCancelReply = actions.onCancelReply,
                // Indica si se está enviando para deshabilitar el botón.
                isSending = uiState.isSending,
            )
        }
    }
}

// Mensaje de estado centrado (error o lista vacía) dentro de la lista.
@Composable
private fun CenteredStatus(text: String, isError: Boolean = false) {
    // Componente reutilizable de mensaje de estado.
    StatusMessage(
        // Texto a mostrar.
        text = text,
        // Indica si se pinta como error.
        isError = isError,
        // Ocupa el ancho con margen vertical amplio.
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
    )
}

// Datos de ejemplo solo para las previews.
private fun previewComment(id: String, name: String, text: String, depth: Int = 0, replies: Int = 0, parent: String? = null) =
    // Construye un CommentUi de ejemplo con valores fijos.
    CommentUi(id, parent, depth, "a$id", name, null, text, null, 90, 3, replies,
        isLiked = false, isOwn = false, isDeleted = false, isEdited = false)

// Vista previa de la pantalla con un hilo abierto y una respuesta en curso.
@Preview(name = "Comentarios con hilo abierto", showBackground = true, heightDp = 900)
@Composable
private fun CommentsContentPreview() {
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Dibuja el contenido con un estado de ejemplo.
        CommentsContent(
            // Estado de ejemplo.
            uiState = CommentsUiState(
                // Total de comentarios.
                totalComments = 4,
                // Comentarios principales de ejemplo.
                comments = listOf(
                    previewComment("c1", "María García", "¡Qué hermosa Luna! ¿Tienen horarios de visita?", replies = 2),
                    previewComment("c2", "Carlos Rodríguez", "Ya contacté a la fundación. Recomiendo visitarla."),
                ),
                // Respuestas de ejemplo del comentario c1.
                replies = mapOf(
                    "c1" to listOf(
                        previewComment("r1", "Ana Martínez", "Sí, los sábados de 9 a 12.", depth = 1, parent = "c1"),
                        previewComment("r2", "Luis Gómez", "¡Gracias por la info!", depth = 1, parent = "c1"),
                    )
                ),
                // Hilo c1 abierto.
                expanded = setOf("c1"),
                // Ya terminó de cargar.
                isLoading = false,
                // Se permite comentar.
                canComment = true,
                // Nombre del usuario que ve.
                viewerName = "ana_77",
                // Respondiendo al comentario c2.
                replyingTo = previewComment("c2", "Carlos Rodríguez", ""),
            ),
            // Acciones vacías para la vista previa.
            actions = CommentsActions(),
        )
    }
}
