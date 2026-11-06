package co.edu.uniquindio.petadoptionapp.features.user.post.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.AssignmentInd
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.clickable
import androidx.compose.ui.semantics.Role
import co.edu.uniquindio.petadoptionapp.core.component.photo.PhotoViewerDialog
import co.edu.uniquindio.petadoptionapp.core.component.map.AppMap
import co.edu.uniquindio.petadoptionapp.core.component.map.MapArea
import co.edu.uniquindio.petadoptionapp.core.util.APPROXIMATE_RADIUS_METERS
import co.edu.uniquindio.petadoptionapp.core.util.approximatePoint
import androidx.compose.ui.draw.clip
import co.edu.uniquindio.petadoptionapp.features.moderator.AiReviewCard
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.util.agoText
import co.edu.uniquindio.petadoptionapp.core.component.card.AppCard
import co.edu.uniquindio.petadoptionapp.core.component.card.LocationMapPlaceholder
import co.edu.uniquindio.petadoptionapp.core.component.card.NoticeBanner
import co.edu.uniquindio.petadoptionapp.core.component.dialog.AppConfirmDialog
import co.edu.uniquindio.petadoptionapp.core.component.icon.UserAvatar
import co.edu.uniquindio.petadoptionapp.core.component.text.IconTag
import co.edu.uniquindio.petadoptionapp.core.component.text.IconTextRow
import co.edu.uniquindio.petadoptionapp.core.component.text.StatusMessage
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import co.edu.uniquindio.petadoptionapp.domain.model.post.AnimalSize
import co.edu.uniquindio.petadoptionapp.features.user.post.comments.CommentInputBar
import co.edu.uniquindio.petadoptionapp.features.user.post.comments.CommentItem
import co.edu.uniquindio.petadoptionapp.features.user.post.comments.CommentUi
import coil3.compose.AsyncImage
import java.util.Locale

/**
Detalle de una publicación. Cambia según quién la mira:
- El dueño ve "Editar" (solo mientras está en revisión) y "Eliminar".
- Un moderador: "Seleccionar para revisión" si nadie la revisa; "Aprobar" y "Rechazar" si es suya;
  un aviso si la revisa otro moderador; "Finalizar" si ya está verificada; y el análisis de la IA.
- Los demás ven "Me interesa adoptar" (si la categoría lo permite) o, si no, "Empezar a chatear", y "Es importante".
Los comentarios se ven de a tres; "Ver todos" abre la pantalla completa para comentar y responder.
Conecta el [PostDetailViewModel] con la UI: observa PostDetailUiState, recolecta los eventos de una
sola vez (eliminada → volver, abrir chat) y muestra los mensajes cortos en un Snackbar.
[PostDetailContent] solo dibuja.
Navegación: no navega por sí misma; avisa con onBack, onShare, onOpenComments, onEditPost,
onAuthorClick, onOpenChat, onRejectPost y onOpenPost, y AppNavigation resuelve el destino.
**/
@Composable
fun PostDetailScreen(
    // Acción para volver atrás.
    onBack: () -> Unit,
    // Acción para compartir (recibe el título).
    onShare: (title: String) -> Unit,
    // Abre la pantalla de comentarios. replyToCommentId: al tocar "Responder", la pantalla de comentarios abre lista para responderle.
    onOpenComments: (postId: String, replyToCommentId: String?) -> Unit,
    // Abre el formulario de edición.
    onEditPost: (postId: String) -> Unit,
    // Abre el perfil de un usuario.
    onAuthorClick: (userId: String) -> Unit,
    // Abre un chat.
    onOpenChat: (chatId: String) -> Unit,
    // Moderador: abre la pantalla para escribir el motivo del rechazo.
    onRejectPost: (postId: String) -> Unit = {},
    // Abre otra publicación (ej. la posible duplicada que señala la IA).
    onOpenPost: (postId: String) -> Unit = {},
    // ViewModel inyectado por Hilt.
    viewModel: PostDetailViewModel = hiltViewModel(),
) {
    // Observa el estado respetando el ciclo de vida.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Referencia siempre actualizada a onBack, para usarla dentro del LaunchedEffect.
    val currentOnBack by rememberUpdatedState(onBack)
    // Referencia siempre actualizada a onOpenChat.
    val currentOnOpenChat by rememberUpdatedState(onOpenChat)
    // Estado del Snackbar para los mensajes cortos.
    val snackbarHostState = remember { SnackbarHostState() }

    // Al volver (por ejemplo, de la pantalla de comentarios) se recarga.
    LifecycleResumeEffect(Unit) {
        // Recarga el detalle al reanudarse.
        viewModel.refresh()
        // No hay nada que limpiar al pausar.
        onPauseOrDispose { }
    }

    // Recolecta los eventos de una sola vez del ViewModel.
    LaunchedEffect(viewModel) {
        // Escucha cada evento.
        viewModel.events.collect { event ->
            // Reacciona según el tipo de evento.
            when (event) {
                // Publicación eliminada: vuelve atrás.
                PostDetailEvent.Deleted -> currentOnBack()
                // Abrir chat: navega al chat indicado.
                is PostDetailEvent.OpenChat -> currentOnOpenChat(event.chatId)
            }
        }
    }

    // Texto traducido del mensaje pendiente, si lo hay.
    val messageText = uiState.message?.let { detailMessageText(it) }
    // Muestra el Snackbar cada vez que cambia el mensaje.
    LaunchedEffect(uiState.message) {
        // Solo si hay mensaje.
        if (messageText != null) {
            // Muestra el Snackbar y espera a que se cierre.
            snackbarHostState.showSnackbar(messageText)
            // Avisa al ViewModel que ya se mostró.
            viewModel.onMessageShown()
        }
    }

    // Caja que superpone el contenido y el Snackbar.
    Box(Modifier.fillMaxSize()) {
        // Contenido de la pantalla.
        PostDetailContent(
            // Estado actual.
            uiState = uiState,
            // Acciones conectadas al ViewModel y a la navegación.
            actions = PostDetailActions(
                // Volver.
                onBack = onBack,
                // Compartir con el título de la publicación.
                onShare = { uiState.post?.let { onShare(it.title) } },
                // Editar la publicación.
                onEdit = { uiState.post?.let { onEditPost(it.id) } },
                // Abrir diálogo de eliminar.
                onDelete = viewModel::onDeleteClick,
                // Confirmar eliminación.
                onDeleteConfirm = viewModel::onDeleteConfirm,
                // Cerrar diálogo de eliminar.
                onDeleteDismiss = viewModel::onDeleteDismiss,
                // Marcar como importante.
                onImportant = viewModel::onImportantClick,
                // Interés de adopción.
                onAdoptionInterest = viewModel::onAdoptionInterestClick,
                // Empezar a chatear.
                onStartChat = viewModel::onStartChatClick,
                // Tomar para revisión (moderador).
                onTakeReview = viewModel::onTakeReviewClick,
                // Abrir diálogo de aprobar.
                onApprove = viewModel::onApproveClick,
                // Confirmar aprobación.
                onApproveConfirm = viewModel::onApproveConfirm,
                // Cerrar diálogo de aprobar.
                onApproveDismiss = viewModel::onApproveDismiss,
                // Rechazar: navega a la pantalla del motivo.
                onReject = { uiState.post?.let { onRejectPost(it.id) } },
                // Abrir diálogo de finalizar.
                onFinish = viewModel::onFinishClick,
                // Confirmar finalización.
                onFinishConfirm = viewModel::onFinishConfirm,
                // Cerrar diálogo de finalizar.
                onFinishDismiss = viewModel::onFinishDismiss,
                // Pedir análisis de IA.
                onAnalyze = viewModel::onAnalyzeClick,
                // Abrir otra publicación.
                onOpenPost = onOpenPost,
                // Ver todos los comentarios.
                onOpenComments = { uiState.post?.let { onOpenComments(it.id, null) } },
                // Responder un comentario: abre comentarios con ese comentario.
                onReply = { comment -> uiState.post?.let { onOpenComments(it.id, comment.id) } },
                // Abrir el perfil del autor de la publicación.
                onAuthorClick = { uiState.author?.let { onAuthorClick(it.id) } },
                // Abrir el perfil del autor de un comentario.
                onCommentAuthorClick = onAuthorClick,
                // "Me gusta" en un comentario.
                onCommentLike = viewModel::onCommentLike,
                // Cambio del texto del comentario.
                onCommentTextChange = viewModel::onCommentTextChange,
                // Enviar comentario.
                onSendComment = viewModel::onSendComment,
            ),
        )
        // Contenedor del Snackbar, encima de la barra de comentario.
        SnackbarHost(
            // Estado del Snackbar.
            hostState = snackbarHostState,
            // Abajo al centro, dejando espacio para la barra de comentario.
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp),
        )
    }
}

// Agrupa las acciones de la pantalla (mismo patrón que Registro y Configuración).
private data class PostDetailActions(
    // Volver.
    val onBack: () -> Unit = {},
    // Compartir.
    val onShare: () -> Unit = {},
    // Editar.
    val onEdit: () -> Unit = {},
    // Pedir eliminar.
    val onDelete: () -> Unit = {},
    // Confirmar eliminar.
    val onDeleteConfirm: () -> Unit = {},
    // Cancelar eliminar.
    val onDeleteDismiss: () -> Unit = {},
    // Marcar importante.
    val onImportant: () -> Unit = {},
    // Interés de adopción.
    val onAdoptionInterest: () -> Unit = {},
    // Empezar chat.
    val onStartChat: () -> Unit = {},
    // Tomar revisión.
    val onTakeReview: () -> Unit = {},
    // Pedir aprobar.
    val onApprove: () -> Unit = {},
    // Confirmar aprobar.
    val onApproveConfirm: () -> Unit = {},
    // Cancelar aprobar.
    val onApproveDismiss: () -> Unit = {},
    // Rechazar.
    val onReject: () -> Unit = {},
    // Pedir finalizar.
    val onFinish: () -> Unit = {},
    // Confirmar finalizar.
    val onFinishConfirm: () -> Unit = {},
    // Cancelar finalizar.
    val onFinishDismiss: () -> Unit = {},
    // Analizar con IA.
    val onAnalyze: () -> Unit = {},
    // Abrir otra publicación (recibe su id).
    val onOpenPost: (String) -> Unit = {},
    // Ver todos los comentarios.
    val onOpenComments: () -> Unit = {},
    // "Me gusta" en comentario (recibe su id).
    val onCommentLike: (String) -> Unit = {},
    // Responder comentario.
    val onReply: (CommentUi) -> Unit = {},
    // Abrir perfil del autor de la publicación.
    val onAuthorClick: () -> Unit = {},
    // Abrir perfil del autor de un comentario (recibe su id).
    val onCommentAuthorClick: (String) -> Unit = {},
    // Cambio del texto del comentario.
    val onCommentTextChange: (String) -> Unit = {},
    // Enviar comentario.
    val onSendComment: () -> Unit = {},
)

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun PostDetailContent(
    // Estado a dibujar.
    uiState: PostDetailUiState,
    // Acciones de la interfaz.
    actions: PostDetailActions,
) {
    // Publicación actual (null mientras carga o si no se encontró).
    val post = uiState.post

    // Columna principal de la pantalla.
    Column(
        // Ocupa toda la pantalla con fondo.
        // statusBarsPadding: la foto empieza debajo de la barra de estado.
        // imePadding: sube la barra de comentario cuando aparece el teclado.
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .statusBarsPadding()
            .imePadding(),
    ) {
        // Si no hay publicación todavía.
        if (post == null) {
            // Cargando o no encontrada: solo la flecha para volver y, si aplica, el aviso.
            RoundIconButton(
                // Ícono de flecha atrás.
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                // Descripción de accesibilidad.
                description = stringResource(R.string.common_back),
                // Acción de volver.
                onClick = actions.onBack,
                // Margen alrededor.
                modifier = Modifier.padding(12.dp),
            )
            // Si no se encontró, muestra el aviso.
            if (uiState.notFound) {
                // Mensaje "Publicación no encontrada".
                StatusMessage(
                    // Texto del aviso.
                    text = stringResource(R.string.post_detail_not_found),
                    // Ancho completo con margen.
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                )
            }
            // No dibuja nada más.
            return@Column
        }

        // Columna desplazable con todo el contenido.
        Column(
            // Ocupa el espacio restante y se desplaza verticalmente.
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            // Tocar una foto la abre a pantalla completa (con su carrusel); al cerrar se vuelve aquí.
            var viewerPage by rememberSaveable { mutableStateOf<Int?>(null) }
            // Carrusel de fotos con botones de volver y compartir.
            PostImageHeader(
                // Fotos.
                imageUrls = post.photoUrls,
                // Acción de volver.
                onBack = actions.onBack,
                // Solo se comparte lo que está verificado y visible al público.
                onShare = actions.onShare.takeIf { post.isPublic },
                // Al tocar una foto, guarda su página para abrir el visor.
                onPhotoClick = { page -> viewerPage = page },
            )
            // Si hay una foto elegida, muestra el visor a pantalla completa.
            viewerPage?.let { page ->
                // Visor de fotos que se cierra limpiando la página.
                PhotoViewerDialog(imageUrls = post.photoUrls, initialPage = page, onDismiss = { viewerPage = null })
            }

            // Columna con las tarjetas de información.
            Column(
                // Margen interno.
                modifier = Modifier.padding(16.dp),
                // Separación entre tarjetas.
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Finalizada: aviso en lugar de acciones (no se edita ni se elimina).
                if (post.isFinished) {
                    // Banner de publicación finalizada.
                    NoticeBanner(
                        // Texto del aviso.
                        message = stringResource(R.string.post_finished_notice),
                        // Ícono de check.
                        icon = Icons.Outlined.CheckCircle,
                    )
                } else if (post.isRejected) {
                    // Rechazada: el motivo del moderador y solo "Eliminar" (no se edita).
                    RejectedNotice(reason = post.rejectionReason)
                    // El dueño solo puede eliminarla.
                    if (uiState.isOwner) OwnerActions(onEdit = null, onDelete = actions.onDelete)
                } else if (uiState.moderatorReview != null) {
                    // Moderador: arriba, como las acciones del dueño (diseño "Aprobar / Rechazar").
                    ModeratorActions(review = uiState.moderatorReview, isTaking = uiState.isTakingReview, actions = actions)
                } else if (uiState.isOwner) {
                    // El dueño tiene sus acciones arriba; los demás, debajo de los datos (como en el diseño).
                    // Ya verificada no se edita (solo se finaliza o elimina), igual que en UpdatePostUseCase.
                    OwnerActions(onEdit = actions.onEdit.takeUnless { post.isVerified }, onDelete = actions.onDelete)
                }

                // Tarjeta con etiquetas, título, ubicación y fecha.
                PostTitleCard(post)
                // Tarjeta del autor, cuando ya se cargó.
                uiState.author?.let { AuthorCard(author = it, isOwner = uiState.isOwner, onViewProfile = actions.onAuthorClick) }
                // Tarjeta con los datos de la mascota.
                PostInfoCard(post)
                // Moderador: el análisis de la IA (resumen, alertas, duplicado y acciones recomendadas).
                if (uiState.isModerator) {
                    // Tarjeta del análisis de IA.
                    AiReviewCard(
                        // Análisis guardado.
                        review = uiState.aiReview,
                        // Indica si se está analizando.
                        isAnalyzing = uiState.isAnalyzing,
                        // Pedir el análisis.
                        onAnalyze = actions.onAnalyze,
                        // Abrir la publicación duplicada sugerida.
                        onOpenPost = actions.onOpenPost,
                    )
                }

                // Los moderadores solo revisan: sin "Me interesa adoptar", chat ni "Es importante".
                if (!uiState.isOwner && !uiState.isModerator && post.isPublic) {
                    // Botones del visitante.
                    VisitorActions(uiState = uiState, post = post, actions = actions)
                }

                // Sección "Sobre la mascota".
                PostDescription(post)
                // Sección de ubicación con mapa.
                PostLocation(post, isOwner = uiState.isOwner)
                // Línea divisoria antes de los comentarios.
                HorizontalDivider(color = AppTheme.colors.border.copy(alpha = 0.5f))
                // Vista previa de comentarios.
                PostCommentsPreview(
                    // Total de comentarios.
                    totalComments = post.commentCount,
                    // Estado de la pantalla.
                    uiState = uiState,
                    // Solo se comenta si es pública.
                    canComment = post.isPublic,
                    // Responder: pública y no moderador.
                    canReply = post.isPublic && !uiState.isModerator,
                    // Acciones.
                    actions = actions,
                )
            }
        }

        // Solo se comenta en publicaciones visibles al público (regla del caso de uso), y no como moderador.
        if (post.isPublic && !uiState.isModerator) {
            // Barra inferior para escribir un comentario.
            CommentInputBar(
                // Alias de quien escribe.
                viewerName = uiState.viewerName,
                // Foto de quien escribe.
                viewerPhotoUrl = uiState.viewerPhotoUrl,
                // Texto actual.
                text = uiState.commentText,
                // Cambio del texto.
                onTextChange = actions.onCommentTextChange,
                // Enviar.
                onSend = actions.onSendComment,
                // Envío en curso.
                isSending = uiState.isSendingComment,
            )
        }
    }

    // Diálogo de confirmación para eliminar la publicación.
    if (uiState.showDeleteDialog) {
        // Diálogo de confirmar.
        AppConfirmDialog(
            // Título.
            title = stringResource(R.string.post_detail_delete_title),
            // Mensaje.
            message = stringResource(R.string.post_detail_delete_message),
            // Texto de aceptar.
            confirmText = stringResource(R.string.my_posts_dialog_accept),
            // Texto de cancelar.
            cancelText = stringResource(R.string.common_cancel),
            // Confirmar.
            onConfirm = actions.onDeleteConfirm,
            // Cerrar.
            onDismiss = actions.onDeleteDismiss,
            // Carga mientras se elimina.
            isLoading = uiState.isDeleting,
            // Acción destructiva: botón en rojo.
            isDestructive = true,
        )
    }

    // Moderador: confirmar antes de finalizar (se avisa al autor por notificación).
    if (uiState.showFinishDialog) {
        // Diálogo de confirmar.
        AppConfirmDialog(
            // Título.
            title = stringResource(R.string.my_posts_finish_title),
            // Mensaje.
            message = stringResource(R.string.mod_finish_message),
            // Texto de aceptar.
            confirmText = stringResource(R.string.my_posts_dialog_accept),
            // Texto de cancelar.
            cancelText = stringResource(R.string.common_cancel),
            // Confirmar.
            onConfirm = actions.onFinishConfirm,
            // Cerrar.
            onDismiss = actions.onFinishDismiss,
            // Carga mientras se finaliza.
            isLoading = uiState.isFinishing,
        )
    }

    // Moderador: confirmar antes de aprobar (la publicación pasa al feed público).
    if (uiState.showApproveDialog) {
        // Diálogo de confirmar.
        AppConfirmDialog(
            // Título.
            title = stringResource(R.string.mod_approve_title),
            // Mensaje.
            message = stringResource(R.string.mod_approve_message),
            // Texto de aprobar.
            confirmText = stringResource(R.string.mod_approve),
            // Texto de cancelar.
            cancelText = stringResource(R.string.common_cancel),
            // Confirmar.
            onConfirm = actions.onApproveConfirm,
            // Cerrar.
            onDismiss = actions.onApproveDismiss,
            // Carga mientras se aprueba.
            isLoading = uiState.isApproving,
        )
    }
}

// Carrusel de fotos de la publicación con los botones de volver/compartir, puntos y contador.
@Composable
private fun PostImageHeader(imageUrls: List<String>, onBack: () -> Unit, onShare: (() -> Unit)?, onPhotoClick: (page: Int) -> Unit) {
    // Sin fotos se muestra la imagen local para no dejar el espacio vacío (al menos una página).
    val pageCount = imageUrls.size.coerceAtLeast(1)
    // Estado del carrusel con la cantidad de páginas.
    val pagerState = rememberPagerState(pageCount = { pageCount })

    // Caja de alto fijo que contiene el carrusel y los controles superpuestos.
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(230.dp)) {
        // Carrusel horizontal de fotos.
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            // Imagen cargada de forma asíncrona.
            AsyncImage(
                // URL de la foto de la página (null si no hay fotos).
                model = imageUrls.getOrNull(page),
                // Descripción "Foto X de N".
                contentDescription = stringResource(R.string.post_detail_photo, page + 1, pageCount),
                // Recorta para llenar el espacio.
                contentScale = ContentScale.Crop,
                // Color de fondo mientras carga.
                placeholder = ColorPainter(AppTheme.colors.surfaceTag),
                // Imagen local si falla la carga.
                error = painterResource(R.drawable.hero_image),
                // Imagen local si no hay URL.
                fallback = painterResource(R.drawable.hero_image),
                // Ocupa todo y al tocarla abre el visor.
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClickLabel = stringResource(R.string.photo_viewer_open), role = Role.Image) { onPhotoClick(page) },
            )
        }

        // Botón de volver arriba a la izquierda.
        RoundIconButton(
            // Ícono de flecha.
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            // Descripción de accesibilidad.
            description = stringResource(R.string.common_back),
            // Acción de volver.
            onClick = onBack,
            // Alineado arriba a la izquierda con margen.
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
        )
        // Botón de compartir solo si se permite.
        if (onShare != null) {
            // Botón de compartir arriba a la derecha.
            RoundIconButton(
                // Ícono de compartir.
                icon = Icons.Outlined.Share,
                // Descripción de accesibilidad.
                description = stringResource(R.string.common_share),
                // Acción de compartir.
                onClick = onShare,
                // Alineado arriba a la derecha con margen.
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
            )
        }

        // Solo muestra los puntos si hay más de una foto.
        if (pageCount > 1) {
            // Puntos: indican cuántas fotos hay y cuál se ve.
            Row(
                // Abajo a la izquierda con margen.
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                // Centra verticalmente.
                verticalAlignment = Alignment.CenterVertically,
                // Separación entre puntos.
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Un punto por cada foto.
                repeat(pageCount) { index ->
                    // Indica si es la foto visible.
                    val selected = index == pagerState.currentPage
                    // Punto circular.
                    Box(
                        // Más grande y opaco si está seleccionado.
                        modifier = Modifier
                            .size(if (selected) 8.dp else 6.dp)
                            .background(if (selected) Color.White else Color.White.copy(alpha = 0.5f), CircleShape),
                    )
                }
            }
        }

        // Contador "2/5" sobre la foto: blanco sobre fondo oscuro para que se lea en cualquier imagen.
        Surface(
            // Abajo a la derecha con margen.
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            // Forma de píldora.
            shape = RoundedCornerShape(50),
            // Fondo negro semitransparente.
            color = Color.Black.copy(alpha = 0.55f),
        ) {
            // Texto del contador.
            Text(
                // Página actual / total.
                text = "${pagerState.currentPage + 1}/$pageCount",
                // Margen interno.
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                // Texto blanco.
                color = Color.White,
                // Estilo de etiqueta pequeña.
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

// Botón redondo claro sobre la foto (volver, compartir). Sí se anuncia: es interactivo.
@Composable
private fun RoundIconButton(
    // Ícono a mostrar.
    icon: ImageVector,
    // Descripción de accesibilidad.
    description: String,
    // Acción al tocar.
    onClick: () -> Unit,
    // Modificador externo.
    modifier: Modifier = Modifier,
) {
    // Superficie clicable circular.
    Surface(
        // Acción al tocar.
        onClick = onClick,
        // Tamaño del botón.
        modifier = modifier.size(40.dp),
        // Forma circular.
        shape = CircleShape,
        // Fondo claro.
        color = AppTheme.colors.background,
    ) {
        // Centra el ícono.
        Box(contentAlignment = Alignment.Center) {
            // Ícono con su descripción.
            Icon(icon, contentDescription = description, tint = AppTheme.colors.textPrimary, modifier = Modifier.size(20.dp))
        }
    }
}

// Moderador: tomarla para revisión, decidirla (si es suya) o el aviso de que la revisa otro.
@Composable
private fun ModeratorActions(review: ModeratorReview, isTaking: Boolean, actions: PostDetailActions) {
    // Elige qué mostrar según el estado de revisión.
    when (review) {
        // Disponible: botón para tomarla.
        ModeratorReview.AVAILABLE -> PillButton(
            // Texto "Seleccionar para revisión".
            text = stringResource(R.string.mod_take_review),
            // Ícono de asignación.
            icon = Icons.Outlined.AssignmentInd,
            // Tomar revisión.
            onClick = actions.onTakeReview,
            // Deshabilitado mientras se toma.
            enabled = !isTaking,
            // Color principal.
            containerColor = AppTheme.colors.actionPrimary,
            // Color del contenido.
            contentColor = AppTheme.colors.onActionPrimary,
            // Ancho completo.
            modifier = Modifier.fillMaxWidth(),
        )
        // Suya: aprobar o rechazar lado a lado.
        ModeratorReview.MINE -> Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Botón de aprobar.
            PillButton(
                // Texto "Aprobar".
                text = stringResource(R.string.mod_approve),
                // Ícono de check.
                icon = Icons.Outlined.CheckCircle,
                // Abrir diálogo de aprobar.
                onClick = actions.onApprove,
                // Color principal.
                containerColor = AppTheme.colors.actionPrimary,
                // Color del contenido.
                contentColor = AppTheme.colors.onActionPrimary,
                // Mitad del ancho.
                modifier = Modifier.weight(1f),
            )
            // Botón de rechazar.
            PillButton(
                // Texto "Rechazar".
                text = stringResource(R.string.mod_reject),
                // Ícono de bloqueo.
                icon = Icons.Outlined.Block,
                // Ir a la pantalla del motivo.
                onClick = actions.onReject,
                // Fondo neutro.
                containerColor = AppTheme.colors.surface,
                // Texto rojo.
                contentColor = AppTheme.colors.stateError,
                // Borde rojo.
                border = BorderStroke(1.dp, AppTheme.colors.stateError),
                // Mitad del ancho.
                modifier = Modifier.weight(1f),
            )
        }
        // La revisa otro moderador: solo un aviso.
        ModeratorReview.OTHER -> NoticeBanner(
            // Texto del aviso.
            message = stringResource(R.string.mod_in_review_by_other),
            // Ícono de reloj de arena.
            icon = Icons.Outlined.HourglassTop,
        )
        // Verificada y en proceso: el moderador puede cerrar el caso.
        ModeratorReview.VERIFIED -> PillButton(
            // Texto "Finalizar publicación".
            text = stringResource(R.string.mod_finish_post),
            // Ícono de check.
            icon = Icons.Outlined.CheckCircle,
            // Abrir diálogo de finalizar.
            onClick = actions.onFinish,
            // Color principal.
            containerColor = AppTheme.colors.actionPrimary,
            // Color del contenido.
            contentColor = AppTheme.colors.onActionPrimary,
            // Ancho completo.
            modifier = Modifier.fillMaxWidth(),
        )
        // Cerrada: sin acciones.
        ModeratorReview.CLOSED -> Unit
    }
}

// Dueño: editar (principal) y eliminar (delineado en rojo).
// Sin [onEdit] (verificada o rechazada) solo queda "Eliminar", a todo el ancho.
@Composable
private fun OwnerActions(onEdit: (() -> Unit)?, onDelete: () -> Unit) {
    // Fila con los botones.
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        // Botón de editar solo si se permite.
        if (onEdit != null) {
            // Botón de editar.
            PillButton(
                // Texto "Editar".
                text = stringResource(R.string.post_detail_edit),
                // Ícono de lápiz.
                icon = Icons.Outlined.Edit,
                // Acción de editar.
                onClick = onEdit,
                // Color principal.
                containerColor = AppTheme.colors.actionPrimary,
                // Color del contenido.
                contentColor = AppTheme.colors.onActionPrimary,
                // Comparte el ancho.
                modifier = Modifier.weight(1f),
            )
        }
        // Botón de eliminar.
        PillButton(
            // Texto "Eliminar".
            text = stringResource(R.string.post_detail_delete),
            // Ícono de papelera.
            icon = Icons.Outlined.Delete,
            // Acción de eliminar.
            onClick = onDelete,
            // Fondo neutro.
            containerColor = AppTheme.colors.surface,
            // Texto rojo.
            contentColor = AppTheme.colors.stateError,
            // Borde rojo.
            border = BorderStroke(1.dp, AppTheme.colors.stateError),
            // Comparte el ancho (o lo ocupa todo si no hay editar).
            modifier = Modifier.weight(1f),
        )
    }
}

// Aviso de rechazo: lo escribe el moderador al rechazar y explica qué corregir o por qué no se publica.
@Composable
private fun RejectedNotice(reason: String?) {
    // Tarjeta con borde rojo.
    AppCard(
        // Fondo neutro.
        containerColor = AppTheme.colors.surface,
        // Borde rojo.
        borderColor = AppTheme.colors.stateError,
        // Margen interno.
        contentPadding = PaddingValues(16.dp),
    ) {
        // Fila con ícono y título.
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Ícono de bloqueo decorativo.
            Icon(Icons.Outlined.Block, contentDescription = null, tint = AppTheme.colors.stateError, modifier = Modifier.size(20.dp))
            // Título "Publicación rechazada".
            Text(
                // Texto del título.
                text = stringResource(R.string.post_rejected_title),
                // Estilo de título pequeño.
                style = MaterialTheme.typography.titleSmall,
                // Color rojo.
                color = AppTheme.colors.stateError,
            )
        }
        // Espacio vertical.
        Spacer(Modifier.height(8.dp))
        // Texto con el motivo.
        Text(
            // Motivo del moderador o texto genérico si no lo dio.
            text = reason?.let { stringResource(R.string.my_posts_rejected_reason, it) }
                ?: stringResource(R.string.post_rejected_no_reason),
            // Estilo de cuerpo mediano.
            style = MaterialTheme.typography.bodyMedium,
            // Color de texto principal.
            color = AppTheme.colors.textPrimary,
        )
    }
}

// Otra persona: interés de adopción (si la categoría lo admite) y voto "Es importante".
@Composable
private fun VisitorActions(uiState: PostDetailUiState, post: PostDetailUi, actions: PostDetailActions) {
    // IntrinsicSize.Min + fillMaxHeight: si un texto ocupa dos líneas, los dos botones quedan del mismo alto.
    Row(
        // Ancho completo y alto según el contenido más alto.
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        // Separación entre botones.
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Si la categoría admite adopción, muestra el botón de interés.
        if (post.allowsAdoption) {
            // Botón de interés de adopción.
            PillButton(
                // Ya enviada: "Ir al chat" abre la conversación con el autor (si se conoce su chat).
                text = stringResource(
                    when {
                        // Hay chat: "Ir al chat".
                        uiState.adoptionChatId != null -> R.string.post_detail_open_chat
                        // Ya enviada sin chat: "Solicitud enviada".
                        uiState.adoptionRequested -> R.string.post_detail_interest_sent
                        // Aún no: "Me interesa adoptar".
                        else -> R.string.post_detail_interest
                    }
                ),
                // Ícono según el mismo estado.
                icon = when {
                    // Chat.
                    uiState.adoptionChatId != null -> Icons.AutoMirrored.Outlined.Chat
                    // Corazón lleno.
                    uiState.adoptionRequested -> Icons.Filled.Favorite
                    // Corazón vacío.
                    else -> Icons.Outlined.FavoriteBorder
                },
                // Acción de interés / abrir chat.
                onClick = actions.onAdoptionInterest,
                // Habilitado si hay chat o aún no se envió, y no se está enviando.
                enabled = (uiState.adoptionChatId != null || !uiState.adoptionRequested) && !uiState.isSendingInterest,
                // Color principal.
                containerColor = AppTheme.colors.actionPrimary,
                // Color del contenido.
                contentColor = AppTheme.colors.onActionPrimary,
                // Hasta dos líneas de texto.
                maxLines = 2,
                // Mitad del ancho y alto completo de la fila.
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        } else {
            // Perdidos, encontrados, veterinaria...: no hay solicitud, se escribe directo al autor.
            PillButton(
                // Texto "Empezar a chatear".
                text = stringResource(R.string.post_detail_start_chat),
                // Ícono de chat.
                icon = Icons.AutoMirrored.Outlined.Chat,
                // Acción de iniciar chat.
                onClick = actions.onStartChat,
                // Deshabilitado mientras se abre.
                enabled = !uiState.isStartingChat,
                // Color principal.
                containerColor = AppTheme.colors.actionPrimary,
                // Color del contenido.
                contentColor = AppTheme.colors.onActionPrimary,
                // Hasta dos líneas.
                maxLines = 2,
                // Mitad del ancho y alto completo.
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
        // Mismo texto y colores invertidos que en el feed cuando ya votó ("Marcada como importante").
        PillButton(
            // Texto según si ya votó.
            text = stringResource(if (uiState.isVoted) R.string.home_important_active else R.string.home_important_button),
            // Estrella llena o vacía.
            icon = if (uiState.isVoted) Icons.Filled.Star else Icons.Outlined.StarBorder,
            // Alternar voto.
            onClick = actions.onImportant,
            // Deshabilitado mientras se procesa.
            enabled = !uiState.isTogglingImportant,
            // Fondo invertido si ya votó.
            containerColor = if (uiState.isVoted) AppTheme.colors.textPrimary else AppTheme.colors.brand,
            // Contenido invertido si ya votó.
            contentColor = if (uiState.isVoted) AppTheme.colors.background else AppTheme.colors.textPrimary,
            // Hasta dos líneas.
            maxLines = 2,
            // Mitad del ancho y alto completo.
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
    }
}

// Botón en forma de píldora con icono, para las acciones de la publicación.
@Composable
private fun PillButton(
    // Texto del botón.
    text: String,
    // Ícono del botón.
    icon: ImageVector,
    // Acción al tocar.
    onClick: () -> Unit,
    // Color de fondo.
    containerColor: Color,
    // Color del contenido.
    contentColor: Color,
    // Modificador externo.
    modifier: Modifier = Modifier,
    // Habilitado.
    enabled: Boolean = true,
    // Borde opcional.
    border: BorderStroke? = null,
    // Máximo de líneas: 2 en los botones del visitante, para que "Marcada como importante" no se corte.
    maxLines: Int = 1,
    // Más aire a los lados en un botón suelto (ej. "Ver perfil"), para que no se vea apretado.
    horizontalPadding: Dp = 8.dp,
) {
    // Botón de Material 3.
    Button(
        // Acción al tocar.
        onClick = onClick,
        // Habilitado.
        enabled = enabled,
        // Alto mínimo accesible de 48 dp.
        modifier = modifier.heightIn(min = 48.dp),
        // Forma de píldora.
        shape = RoundedCornerShape(50),
        // Borde opcional.
        border = border,
        // Márgenes internos.
        contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 8.dp),
        // Colores normales y deshabilitados.
        colors = ButtonDefaults.buttonColors(
            // Fondo.
            containerColor = containerColor,
            // Contenido.
            contentColor = contentColor,
            // Fondo deshabilitado (más transparente).
            disabledContainerColor = containerColor.copy(alpha = 0.6f),
            // Contenido deshabilitado.
            disabledContentColor = contentColor,
        ),
    ) {
        // Ícono decorativo.
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        // Espacio entre ícono y texto.
        Spacer(modifier = Modifier.width(6.dp))
        // 12 sp para todos: los dos botones de una fila se ven del mismo tamaño.
        Text(
            // Texto.
            text,
            // Estilo con tamaño fijo.
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, lineHeight = 14.sp),
            // Máximo de líneas.
            maxLines = maxLines,
            // Recorta con puntos suspensivos.
            overflow = TextOverflow.Ellipsis,
            // Centrado.
            textAlign = TextAlign.Center,
        )
    }
}

// Tarjeta con etiquetas, título, ubicación y fecha.
@Composable
private fun PostTitleCard(post: PostDetailUi) {
    // Tarjeta con el color de marca.
    AppCard(
        // Fondo de marca.
        containerColor = AppTheme.colors.brand,
        // Sin borde.
        borderColor = null,
        // Margen interno.
        contentPadding = PaddingValues(16.dp),
    ) {
        // Estilo de las etiquetas.
        val tagStyle = LocalTextStyle.current.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium)
        // Fila de etiquetas.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Etiqueta de categoría si tiene nombre.
            if (post.categoryName.isNotBlank()) {
                // Etiqueta con ícono de huella.
                IconTag(text = post.categoryName, icon = Icons.Outlined.Pets, textStyle = tagStyle)
            }
            // Etiqueta "Verificada" si aplica.
            if (post.isVerified) {
                // Etiqueta con check verde.
                IconTag(
                    // Texto "Verificada".
                    text = stringResource(R.string.post_detail_verified),
                    // Ícono de check.
                    icon = Icons.Filled.CheckCircle,
                    // Color verde.
                    iconColor = AppTheme.colors.stateSuccess,
                    // Estilo de texto.
                    textStyle = tagStyle,
                )
            }
        }

        // Espacio vertical.
        Spacer(modifier = Modifier.height(12.dp))

        // Título de la publicación.
        Text(post.title, style = MaterialTheme.typography.headlineSmall, color = AppTheme.colors.textPrimary)

        // Espacio vertical.
        Spacer(modifier = Modifier.height(12.dp))

        // Línea de ubicación y distancia.
        DetailIconText(icon = Icons.Outlined.LocationOn, text = locationText(post))
        // Espacio vertical.
        Spacer(modifier = Modifier.height(6.dp))
        // Línea con la fecha de publicación.
        DetailIconText(
            // Ícono de calendario.
            icon = Icons.Outlined.CalendarMonth,
            // "Publicado hace...".
            text = stringResource(R.string.my_posts_published, agoText(post.ageHours)),
        )
    }
}

// Quién publicó: avatar, alias y nombre (si es público) con el acceso a su perfil.
// En una publicación propia dice "Tú" en lugar del botón.
@Composable
private fun AuthorCard(author: PostAuthorUi, isOwner: Boolean, onViewProfile: () -> Unit) {
    // Tarjeta tocable (salvo si es propia).
    AppCard(
        // Margen interno.
        contentPadding = PaddingValues(16.dp),
        // Al tocar abre el perfil, salvo que sea propia.
        onClick = if (isOwner) null else onViewProfile,
        // Etiqueta de accesibilidad de la acción.
        onClickLabel = stringResource(R.string.post_detail_view_profile),
    ) {
        // Fila con avatar, textos y botón.
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Avatar del autor.
            UserAvatar(name = author.userName, photoUrl = author.photoUrl, size = 48.dp)
            // Columna de textos que ocupa el espacio restante.
            Column(modifier = Modifier.weight(1f)) {
                // Rótulo "PUBLICADO POR".
                Text(
                    // Se guarda normal y se pasa a mayúsculas aquí, para que TalkBack lo lea como frase.
                    text = stringResource(R.string.post_detail_published_by).uppercase(),
                    // Estilo de etiqueta pequeña.
                    style = MaterialTheme.typography.labelSmall,
                    // Color de texto principal.
                    color = AppTheme.colors.textPrimary,
                )
                // Alias del autor.
                Text(
                    // "@alias".
                    text = stringResource(R.string.user_profile_username, author.userName),
                    // Estilo de título pequeño.
                    style = MaterialTheme.typography.titleSmall,
                    // Color de texto principal.
                    color = AppTheme.colors.textPrimary,
                    // Una línea.
                    maxLines = 1,
                    // Recorta con puntos suspensivos.
                    overflow = TextOverflow.Ellipsis,
                )
                // Nombre real solo si el perfil es público.
                author.name?.let {
                    // Texto del nombre.
                    Text(
                        // Nombre real.
                        text = it,
                        // Estilo de cuerpo pequeño.
                        style = MaterialTheme.typography.bodySmall,
                        // Color de texto principal.
                        color = AppTheme.colors.textPrimary,
                        // Una línea.
                        maxLines = 1,
                        // Recorta con puntos suspensivos.
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            // Si es propia, muestra la etiqueta "Tú".
            if (isOwner) {
                // Etiqueta "Tú".
                Text(
                    // Texto "Tú".
                    text = stringResource(R.string.post_detail_you),
                    // Estilo de etiqueta mediana.
                    style = MaterialTheme.typography.labelMedium,
                    // Color de texto principal.
                    color = AppTheme.colors.textPrimary,
                    // Fondo redondeado con margen interno.
                    modifier = Modifier
                        .background(AppTheme.colors.surfaceTag, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            } else {
                // Toda la tarjeta es tocable; el botón deja claro qué pasa al tocar.
                PillButton(
                    // Texto "Ver perfil".
                    text = stringResource(R.string.post_detail_view_profile),
                    // Ícono de persona.
                    icon = Icons.Outlined.Person,
                    // Abrir el perfil.
                    onClick = onViewProfile,
                    // Color principal.
                    containerColor = AppTheme.colors.actionPrimary,
                    // Color del contenido.
                    contentColor = AppTheme.colors.onActionPrimary,
                    // Más margen lateral.
                    horizontalPadding = 20.dp,
                    // Ancho mínimo.
                    modifier = Modifier.widthIn(min = 128.dp),
                )
            }
        }
    }
}

// Tarjeta con los datos de la mascota en 2 columnas.
@Composable
private fun PostInfoCard(post: PostDetailUi) {
    // Texto para los datos desconocidos.
    val unknown = stringResource(R.string.post_detail_unknown)
    // Tarjeta con el color de marca.
    AppCard(
        // Fondo de marca.
        containerColor = AppTheme.colors.brand,
        // Sin borde.
        borderColor = null,
        // Margen interno.
        contentPadding = PaddingValues(16.dp),
    ) {
        // Primera fila: tipo y raza.
        Row(modifier = Modifier.fillMaxWidth()) {
            // Celda del tipo de animal.
            InfoCell(stringResource(R.string.post_detail_type), post.animalTypeName ?: unknown, Icons.Outlined.Pets, Modifier.weight(1f))
            // Celda de la raza.
            InfoCell(stringResource(R.string.post_detail_breed), post.breed.ifBlank { unknown }, Icons.Outlined.Sell, Modifier.weight(1f))
        }

        // Divisor entre filas.
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = AppTheme.colors.border)

        // Segunda fila: tamaño y vacunas.
        Row(modifier = Modifier.fillMaxWidth()) {
            // Celda del tamaño.
            InfoCell(stringResource(R.string.post_detail_size), sizeText(post.size), Icons.Outlined.Straighten, Modifier.weight(1f))
            // Celda de las vacunas.
            InfoCell(stringResource(R.string.post_detail_vaccines), vaccinesText(post.vaccinated), Icons.Outlined.TaskAlt, Modifier.weight(1f))
        }
    }
}

// Una celda de datos: cuadro con ícono + etiqueta pequeña + valor.
@Composable
private fun InfoCell(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    // Fila de la celda.
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        // Cuadro del ícono.
        Box(
            // Tamaño y fondo redondeado.
            modifier = Modifier
                .size(36.dp)
                .background(AppTheme.colors.background, RoundedCornerShape(10.dp)),
            // Centra el ícono.
            contentAlignment = Alignment.Center,
        ) {
            // Ícono decorativo.
            Icon(icon, contentDescription = null, tint = AppTheme.colors.textPrimary, modifier = Modifier.size(20.dp))
        }
        // Espacio horizontal.
        Spacer(modifier = Modifier.width(8.dp))
        // Columna con etiqueta y valor.
        Column {
            // Etiqueta.
            Text(label, style = MaterialTheme.typography.labelSmall, color = AppTheme.colors.textPrimary)
            // Valor.
            Text(value, style = MaterialTheme.typography.titleSmall, color = AppTheme.colors.textPrimary)
        }
    }
}

// Sección "Sobre la mascota" con la descripción.
@Composable
private fun PostDescription(post: PostDetailUi) {
    // Columna de la sección.
    Column {
        // Título de la sección.
        SectionTitle(stringResource(R.string.post_detail_about))
        // Espacio vertical.
        Spacer(modifier = Modifier.height(8.dp))
        // Texto de la descripción.
        Text(post.description, style = MaterialTheme.typography.bodyLarge, color = AppTheme.colors.textPrimary)
    }
}

// Mapa (MapLibre) con la zona aproximada; sin coordenadas, el recuadro vacío.
@Composable
private fun PostLocation(post: PostDetailUi, isOwner: Boolean) {
    // Columna de la sección.
    Column {
        // Título de la sección.
        SectionTitle(stringResource(R.string.post_detail_location))
        // Espacio vertical.
        Spacer(modifier = Modifier.height(8.dp))
        // Zona aproximada: el punto se corre unos cientos de metros y se dibuja un círculo, nunca la dirección exacta.
        val lat = post.latitude
        // Longitud guardada.
        val lng = post.longitude
        // Si hay coordenadas, dibuja el mapa.
        if (lat != null && lng != null) {
            // Punto desplazado de forma estable según el id de la publicación.
            val approx = approximatePoint(lat, lng, post.id)
            // Mapa con la zona aproximada.
            AppMap(
                // Centro en el punto aproximado.
                center = approx,
                // Nivel de acercamiento.
                zoom = 14.0,
                // Círculo de la zona aproximada.
                areas = listOf(MapArea(approx, APPROXIMATE_RADIUS_METERS, AppTheme.colors.brand)),
                // Mapa sin interacción.
                interactive = false,
                // Ancho completo, alto fijo y esquinas redondeadas.
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp)),
            )
        } else {
            // Sin coordenadas: recuadro vacío.
            LocationMapPlaceholder(onSetLocationClick = null, height = 140.dp)
        }
        // Espacio vertical.
        Spacer(modifier = Modifier.height(8.dp))
        // El dueño ve su dirección; los demás, solo la zona y la ciudad.
        DetailIconText(
            // Ícono de ubicación.
            icon = Icons.Outlined.LocationOn,
            // Dirección completa para el dueño; "Zona aproximada: ciudad, país" para los demás.
            text = if (isOwner) listOf(post.address, post.city, post.country).filter { it.isNotBlank() }.joinToString(", ")
            else stringResource(R.string.post_detail_location_approx, listOf(post.city, post.country).filter { it.isNotBlank() }.joinToString(", ")),
            // En negrita.
            bold = true,
        )
        // A los demás les explica por qué es aproximada.
        if (!isOwner) {
            // Texto de ayuda.
            Text(
                // Explicación de la zona aproximada.
                text = stringResource(R.string.post_detail_location_approx_hint),
                // Estilo de cuerpo pequeño.
                style = MaterialTheme.typography.bodySmall,
                // Color de texto principal.
                color = AppTheme.colors.textPrimary,
            )
        }
    }
}

// Vista previa de comentarios: solo los primeros tres principales y "Ver todos" para la pantalla completa.
@Composable
private fun PostCommentsPreview(
    // Total de comentarios.
    totalComments: Int,
    // Estado de la pantalla.
    uiState: PostDetailUiState,
    // Indica si se puede comentar.
    canComment: Boolean,
    // Responder: no para moderadores, aunque los comentarios estén abiertos.
    canReply: Boolean = canComment,
    // Acciones.
    actions: PostDetailActions,
) {
    // Comentarios de la vista previa.
    val comments = uiState.comments
    // Acción de "Ver todos".
    val onSeeAll = actions.onOpenComments
    // Columna de la sección.
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Fila con el título y "Ver todos".
        Row(
            // Ancho completo.
            modifier = Modifier.fillMaxWidth(),
            // Título a la izquierda y enlace a la derecha.
            horizontalArrangement = Arrangement.SpaceBetween,
            // Centra verticalmente.
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Título "Comentarios (N)".
            SectionTitle(stringResource(R.string.post_detail_comments, totalComments))
            // "Ver todos" solo si hay comentarios.
            if (totalComments > 0) {
                // Botón de texto.
                TextButton(onClick = onSeeAll) {
                    // Texto del enlace.
                    Text(
                        // "Ver todos".
                        stringResource(R.string.post_detail_see_all),
                        // Estilo de etiqueta mediana.
                        style = MaterialTheme.typography.labelMedium,
                        // Color de enlace.
                        color = AppTheme.colors.textLink,
                    )
                }
            }
        }

        // Si no hay comentarios, muestra un aviso.
        if (comments.isEmpty()) {
            // Mensaje de estado.
            StatusMessage(
                // "Sin comentarios" o "Comentarios cerrados".
                text = stringResource(
                    if (canComment) R.string.post_detail_no_comments else R.string.post_detail_comments_closed
                ),
                // Ancho completo con margen vertical.
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
            )
        }

        // Sin hilos aquí: "Responder" lleva a la pantalla de comentarios con ese comentario resaltado.
        comments.forEach { comment ->
            // Dibuja un comentario.
            CommentItem(
                // Comentario.
                comment = comment,
                // "Me gusta".
                onLikeClick = { actions.onCommentLike(comment.id) },
                // Responder.
                onReplyClick = { actions.onReply(comment) },
                // Permite responder según la bandera.
                canReply = canReply,
                // Abrir el perfil del autor del comentario.
                onAuthorClick = { actions.onCommentAuthorClick(comment.authorId) },
            )
        }
    }
}

// Título de sección marcado como encabezado para accesibilidad.
@Composable
private fun SectionTitle(text: String) {
    // Texto del título.
    Text(
        // Contenido.
        text = text,
        // Estilo de título mediano.
        style = MaterialTheme.typography.titleMedium,
        // Color de texto principal.
        color = AppTheme.colors.textPrimary,
        // TalkBack lo anuncia como encabezado.
        modifier = Modifier.semantics { heading() },
    )
}

// Línea con ícono a la izquierda y texto a la derecha, con el tamaño propio del detalle.
@Composable
private fun DetailIconText(icon: ImageVector, text: String, bold: Boolean = false) {
    // Fila reutilizable de ícono + texto.
    IconTextRow(
        // Ícono.
        icon = icon,
        // Texto.
        text = text,
        // Tamaño del ícono.
        iconSize = 18.dp,
        // Estilo con negrita opcional.
        style = LocalTextStyle.current.copy(
            fontSize = 14.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
        ),
    )
}

// "Armenia, Centro • 1.2 km" (la distancia solo si ambos tienen coordenadas).
@Composable
private fun locationText(post: PostDetailUi): String {
    // Une ciudad y dirección no vacías.
    val place = listOf(post.city, post.address).filter { it.isNotBlank() }.joinToString(", ")
    // Distancia con un decimal, si existe.
    val distance = post.distanceKm?.let { stringResource(R.string.home_distance_km, String.format(Locale.US, "%.1f", it)) }
    // Une lugar y distancia con un punto separador.
    return listOfNotNull(place.ifBlank { null }, distance).joinToString(" • ")
}

// Texto traducido del tamaño del animal.
@Composable
private fun sizeText(size: AnimalSize?): String = when (size) {
    // Desconocido.
    null -> stringResource(R.string.post_detail_unknown)
    // Pequeño.
    AnimalSize.SMALL -> stringResource(R.string.home_size_small)
    // Mediano.
    AnimalSize.MEDIUM -> stringResource(R.string.home_size_medium)
    // Grande.
    AnimalSize.LARGE -> stringResource(R.string.home_size_large)
}

// Texto traducido del estado de vacunas.
@Composable
private fun vaccinesText(vaccinated: Boolean?): String = when (vaccinated) {
    // Vacunado.
    true -> stringResource(R.string.post_detail_vaccines_yes)
    // No vacunado.
    false -> stringResource(R.string.post_detail_vaccines_no)
    // Desconocido.
    null -> stringResource(R.string.post_detail_unknown)
}

// Traduce cada mensaje corto del ViewModel a su texto de strings.xml.
@Composable
private fun detailMessageText(message: DetailMessage): String = stringResource(
    // Elige el recurso según el mensaje.
    when (message) {
        // Solicitud ya enviada.
        DetailMessage.ALREADY_REQUESTED -> R.string.post_detail_msg_already_requested
        // Error genérico.
        DetailMessage.ACTION_ERROR -> R.string.post_detail_msg_error
        // Revisión tomada.
        DetailMessage.REVIEW_TAKEN -> R.string.mod_msg_review_taken
        // La tomó otro moderador.
        DetailMessage.REVIEW_TAKEN_BY_OTHER -> R.string.mod_msg_review_taken_by_other
        // Aprobada.
        DetailMessage.APPROVED -> R.string.mod_msg_approved
        // Finalizada.
        DetailMessage.FINISHED -> R.string.mod_msg_finished
        // Análisis de IA listo.
        DetailMessage.AI_DONE -> R.string.ai_done
        // IA sin configurar.
        DetailMessage.AI_NOT_CONFIGURED -> R.string.ai_not_configured
        // Falló la IA.
        DetailMessage.AI_FAILED -> R.string.ai_failed
        // Error al comentar.
        DetailMessage.COMMENT_ERROR -> R.string.post_detail_msg_comment_error
    }
)

// Datos de ejemplo solo para las previews.
private val previewPost = PostDetailUi(
    id = "p1", photoUrls = emptyList(), categoryName = "Adopción", isVerified = true,
    title = "Luna busca un hogar lleno de amor", city = "Bogotá", address = "Chapinero", country = "Colombia",
    distanceKm = 1.2, ageHours = 72, animalTypeName = "Perro", breed = "Mestiza (Labrador)",
    size = AnimalSize.MEDIUM, vaccinated = true,
    description = "Luna es una perrita mestiza de aproximadamente 2 años. Es cariñosa, juguetona y se lleva muy bien con niños y otros perros.",
    commentCount = 8, importantCount = 12, isPublic = true, allowsAdoption = true, isFinished = false,
)

// Comentarios de ejemplo solo para las previews.
private val previewComments = listOf(
    CommentUi("c1", null, 0, "u3", "María García", null, "¡Qué hermosa Luna! ¿Tienen horarios de visita?",
        null, 120, 3, 1, isLiked = false, isOwn = false, isDeleted = false, isEdited = false),
    CommentUi("c2", null, 0, "u4", "Carlos Rodríguez", null, "Ya contacté a la fundación. Recomiendo visitarla.",
        null, 300, 7, 0, isLiked = true, isOwn = false, isDeleted = false, isEdited = false),
)

// Vista previa de una publicación ajena (visitante).
@Preview(name = "Publicación ajena", showBackground = true, heightDp = 1700)
@Composable
private fun PostDetailVisitorPreview() {
    // Aplica el tema.
    PetAdoptionAppTheme {
        // Contenido con datos de ejemplo.
        PostDetailContent(
            // Estado de ejemplo del visitante.
            uiState = PostDetailUiState(
                post = previewPost, comments = previewComments, isLoading = false, viewerName = "ana_77",
                author = PostAuthorUi("u1", "fundacion_huellas", null, "Fundación Huellas"),
            ),
            // Acciones vacías.
            actions = PostDetailActions(),
        )
    }
}

// Vista previa de una publicación propia (dueño).
@Preview(name = "Mi publicación", showBackground = true, heightDp = 1700)
@Composable
private fun PostDetailOwnerPreview() {
    // Aplica el tema.
    PetAdoptionAppTheme {
        // Contenido como dueño.
        PostDetailContent(
            // Estado de ejemplo del dueño.
            uiState = PostDetailUiState(post = previewPost, comments = previewComments, isOwner = true, isLoading = false),
            // Acciones vacías.
            actions = PostDetailActions(),
        )
    }
}
