package co.edu.uniquindio.petadoptionapp.features.user.post.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.petadoptionapp.R
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
- El dueño ve "Editar" y "Eliminar".
- Los demás ven "Me interesa adoptar" (si la categoría lo permite) y "Es importante".
Los comentarios se ven de a tres; "Ver todos" abre la pantalla completa para comentar y responder.
Conecta el [PostDetailViewModel] con la UI; [PostDetailContent] solo dibuja.
 */
@Composable
fun PostDetailScreen(
    onBack: () -> Unit,
    onShare: (title: String) -> Unit,
    // replyToCommentId: al tocar "Responder", la pantalla de comentarios abre lista para responderle.
    onOpenComments: (postId: String, replyToCommentId: String?) -> Unit,
    onEditPost: (postId: String) -> Unit,
    onAuthorClick: (userId: String) -> Unit,
    viewModel: PostDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnBack by rememberUpdatedState(onBack)
    val snackbarHostState = remember { SnackbarHostState() }

    // Al volver (por ejemplo, de la pantalla de comentarios) se recarga.
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                PostDetailEvent.Deleted -> currentOnBack()
            }
        }
    }

    val messageText = uiState.message?.let { detailMessageText(it) }
    LaunchedEffect(uiState.message) {
        if (messageText != null) {
            snackbarHostState.showSnackbar(messageText)
            viewModel.onMessageShown()
        }
    }

    Box(Modifier.fillMaxSize()) {
        PostDetailContent(
            uiState = uiState,
            actions = PostDetailActions(
                onBack = onBack,
                onShare = { uiState.post?.let { onShare(it.title) } },
                onEdit = { uiState.post?.let { onEditPost(it.id) } },
                onDelete = viewModel::onDeleteClick,
                onDeleteConfirm = viewModel::onDeleteConfirm,
                onDeleteDismiss = viewModel::onDeleteDismiss,
                onImportant = viewModel::onImportantClick,
                onAdoptionInterest = viewModel::onAdoptionInterestClick,
                onOpenComments = { uiState.post?.let { onOpenComments(it.id, null) } },
                onReply = { comment -> uiState.post?.let { onOpenComments(it.id, comment.id) } },
                onAuthorClick = { uiState.author?.let { onAuthorClick(it.id) } },
                onCommentLike = viewModel::onCommentLike,
                onCommentTextChange = viewModel::onCommentTextChange,
                onSendComment = viewModel::onSendComment,
            ),
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp),
        )
    }
}

// Agrupa las acciones de la pantalla (mismo patrón que Registro y Configuración).
private data class PostDetailActions(
    val onBack: () -> Unit = {},
    val onShare: () -> Unit = {},
    val onEdit: () -> Unit = {},
    val onDelete: () -> Unit = {},
    val onDeleteConfirm: () -> Unit = {},
    val onDeleteDismiss: () -> Unit = {},
    val onImportant: () -> Unit = {},
    val onAdoptionInterest: () -> Unit = {},
    val onOpenComments: () -> Unit = {},
    val onCommentLike: (String) -> Unit = {},
    val onReply: (CommentUi) -> Unit = {},
    val onAuthorClick: () -> Unit = {},
    val onCommentTextChange: (String) -> Unit = {},
    val onSendComment: () -> Unit = {},
)

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun PostDetailContent(
    uiState: PostDetailUiState,
    actions: PostDetailActions,
) {
    val post = uiState.post

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .statusBarsPadding()      // La foto empieza debajo de la barra de estado
            .imePadding(),            // Sube la barra de comentario cuando aparece el teclado
    ) {
        if (post == null) {
            // Cargando o no encontrada: solo la flecha para volver y, si aplica, el aviso.
            RoundIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                description = stringResource(R.string.common_back),
                onClick = actions.onBack,
                modifier = Modifier.padding(12.dp),
            )
            if (uiState.notFound) {
                StatusMessage(
                    text = stringResource(R.string.post_detail_not_found),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                )
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            PostImageHeader(imageUrls = post.photoUrls, onBack = actions.onBack, onShare = actions.onShare)

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Finalizada: aviso en lugar de acciones (no se edita ni se elimina).
                if (post.isFinished) {
                    NoticeBanner(
                        message = stringResource(R.string.post_finished_notice),
                        icon = Icons.Outlined.CheckCircle,
                    )
                } else if (uiState.isOwner) {
                    // El dueño tiene sus acciones arriba; los demás, debajo de los datos (como en el diseño).
                    OwnerActions(onEdit = actions.onEdit, onDelete = actions.onDelete)
                }

                PostTitleCard(post)
                uiState.author?.let { AuthorCard(author = it, isOwner = uiState.isOwner, onViewProfile = actions.onAuthorClick) }
                PostInfoCard(post)

                if (!uiState.isOwner && post.isPublic) {
                    VisitorActions(uiState = uiState, post = post, actions = actions)
                }

                PostDescription(post)
                PostLocation(post)
                HorizontalDivider(color = AppTheme.colors.border.copy(alpha = 0.5f))
                PostCommentsPreview(
                    totalComments = post.commentCount,
                    uiState = uiState,
                    canComment = post.isPublic,
                    actions = actions,
                )
            }
        }

        // Solo se comenta en publicaciones visibles al público (regla del caso de uso).
        if (post.isPublic) {
            CommentInputBar(
                viewerName = uiState.viewerName,
                viewerPhotoUrl = uiState.viewerPhotoUrl,
                text = uiState.commentText,
                onTextChange = actions.onCommentTextChange,
                onSend = actions.onSendComment,
                isSending = uiState.isSendingComment,
            )
        }
    }

    if (uiState.showDeleteDialog) {
        AppConfirmDialog(
            title = stringResource(R.string.post_detail_delete_title),
            message = stringResource(R.string.post_detail_delete_message),
            confirmText = stringResource(R.string.my_posts_dialog_accept),
            cancelText = stringResource(R.string.common_cancel),
            onConfirm = actions.onDeleteConfirm,
            onDismiss = actions.onDeleteDismiss,
            isLoading = uiState.isDeleting,
        )
    }
}

@Composable
private fun PostImageHeader(imageUrls: List<String>, onBack: () -> Unit, onShare: () -> Unit) {
    // Sin fotos se muestra la imagen local para no dejar el espacio vacío.
    val pageCount = imageUrls.size.coerceAtLeast(1)
    val pagerState = rememberPagerState(pageCount = { pageCount })

    Box(modifier = Modifier
        .fillMaxWidth()
        .height(230.dp)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            AsyncImage(
                model = imageUrls.getOrNull(page),
                contentDescription = stringResource(R.string.post_detail_photo, page + 1, pageCount),
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(AppTheme.colors.surfaceTag),
                error = painterResource(R.drawable.hero_image),
                fallback = painterResource(R.drawable.hero_image),
                modifier = Modifier.fillMaxSize(),
            )
        }

        RoundIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            description = stringResource(R.string.common_back),
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
        )
        RoundIconButton(
            icon = Icons.Outlined.Share,
            description = stringResource(R.string.common_share),
            onClick = onShare,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
        )

        if (pageCount > 1) {
            // Puntos: indican cuántas fotos hay y cuál se ve.
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                repeat(pageCount) { index ->
                    val selected = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .size(if (selected) 8.dp else 6.dp)
                            .background(if (selected) Color.White else Color.White.copy(alpha = 0.5f), CircleShape),
                    )
                }
            }
        }

        // Contador "2/5" sobre la foto: blanco sobre fondo oscuro para que se lea en cualquier imagen.
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            shape = RoundedCornerShape(50),
            color = Color.Black.copy(alpha = 0.55f),
        ) {
            Text(
                text = "${pagerState.currentPage + 1}/$pageCount",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

// Botón redondo claro sobre la foto (volver, compartir). Sí se anuncia: es interactivo.
@Composable
private fun RoundIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        shape = CircleShape,
        color = AppTheme.colors.background,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = description, tint = AppTheme.colors.textPrimary, modifier = Modifier.size(20.dp))
        }
    }
}

// Dueño: editar (principal) y eliminar (delineado en rojo).
@Composable
private fun OwnerActions(onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        PillButton(
            text = stringResource(R.string.post_detail_edit),
            icon = Icons.Outlined.Edit,
            onClick = onEdit,
            containerColor = AppTheme.colors.actionPrimary,
            contentColor = AppTheme.colors.onActionPrimary,
            modifier = Modifier.weight(1f),
        )
        PillButton(
            text = stringResource(R.string.post_detail_delete),
            icon = Icons.Outlined.Delete,
            onClick = onDelete,
            containerColor = AppTheme.colors.surface,
            contentColor = AppTheme.colors.stateError,
            border = BorderStroke(1.dp, AppTheme.colors.stateError),
            modifier = Modifier.weight(1f),
        )
    }
}

// Otra persona: interés de adopción (si la categoría lo admite) y voto "Es importante".
@Composable
private fun VisitorActions(uiState: PostDetailUiState, post: PostDetailUi, actions: PostDetailActions) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (post.allowsAdoption) {
            PillButton(
                text = stringResource(
                    if (uiState.adoptionRequested) R.string.post_detail_interest_sent else R.string.post_detail_interest
                ),
                icon = if (uiState.adoptionRequested) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                onClick = actions.onAdoptionInterest,
                enabled = !uiState.adoptionRequested && !uiState.isSendingInterest,
                containerColor = AppTheme.colors.actionPrimary,
                contentColor = AppTheme.colors.onActionPrimary,
                modifier = Modifier.weight(1f),
            )
        }
        // Colores invertidos cuando ya votó, igual que en el feed.
        PillButton(
            text = stringResource(R.string.post_detail_important),
            icon = if (uiState.isVoted) Icons.Filled.Star else Icons.Outlined.StarBorder,
            onClick = actions.onImportant,
            enabled = !uiState.isTogglingImportant,
            containerColor = if (uiState.isVoted) AppTheme.colors.textPrimary else AppTheme.colors.brand,
            contentColor = if (uiState.isVoted) AppTheme.colors.background else AppTheme.colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
    }
}

// Botón en forma de píldora con icono, para las acciones de la publicación.
@Composable
private fun PillButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    border: BorderStroke? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(50),
        border = border,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.6f),
            disabledContentColor = contentColor,
        ),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        // 12 sp: los textos del diseño ("Me interesa adoptar") caben completos en media pantalla.
        Text(text, style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// Tarjeta con etiquetas, título, ubicación y fecha
@Composable
private fun PostTitleCard(post: PostDetailUi) {
    AppCard(
        containerColor = AppTheme.colors.brand,
        borderColor = null,
        contentPadding = PaddingValues(16.dp),
    ) {
        val tagStyle = LocalTextStyle.current.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (post.categoryName.isNotBlank()) {
                IconTag(text = post.categoryName, icon = Icons.Outlined.Pets, textStyle = tagStyle)
            }
            if (post.isVerified) {
                IconTag(
                    text = stringResource(R.string.post_detail_verified),
                    icon = Icons.Filled.CheckCircle,
                    iconColor = AppTheme.colors.stateSuccess,
                    textStyle = tagStyle,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(post.title, style = MaterialTheme.typography.headlineSmall, color = AppTheme.colors.textPrimary)

        Spacer(modifier = Modifier.height(12.dp))

        DetailIconText(icon = Icons.Outlined.LocationOn, text = locationText(post))
        Spacer(modifier = Modifier.height(6.dp))
        DetailIconText(
            icon = Icons.Outlined.CalendarMonth,
            text = stringResource(R.string.my_posts_published, agoText(post.ageHours)),
        )
    }
}

// Quién publicó: avatar, alias y nombre (si es público) con el acceso a su perfil.
// En una publicación propia dice "Tú" en lugar del botón.
@Composable
private fun AuthorCard(author: PostAuthorUi, isOwner: Boolean, onViewProfile: () -> Unit) {
    AppCard(
        contentPadding = PaddingValues(16.dp),
        onClick = if (isOwner) null else onViewProfile,
        onClickLabel = stringResource(R.string.post_detail_view_profile),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            UserAvatar(name = author.userName, photoUrl = author.photoUrl, size = 48.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    // Se guarda normal y se pasa a mayúsculas aquí, para que TalkBack lo lea como frase.
                    text = stringResource(R.string.post_detail_published_by).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppTheme.colors.textPrimary,
                )
                Text(
                    text = stringResource(R.string.user_profile_username, author.userName),
                    style = MaterialTheme.typography.titleSmall,
                    color = AppTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                author.name?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (isOwner) {
                Text(
                    text = stringResource(R.string.post_detail_you),
                    style = MaterialTheme.typography.labelMedium,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier
                        .background(AppTheme.colors.surfaceTag, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            } else {
                // Toda la tarjeta es tocable; el botón deja claro qué pasa al tocar.
                PillButton(
                    text = stringResource(R.string.post_detail_view_profile),
                    icon = Icons.Outlined.Person,
                    onClick = onViewProfile,
                    containerColor = AppTheme.colors.actionPrimary,
                    contentColor = AppTheme.colors.onActionPrimary,
                )
            }
        }
    }
}

// Tarjeta con los datos de la mascota en 2 columnas
@Composable
private fun PostInfoCard(post: PostDetailUi) {
    val unknown = stringResource(R.string.post_detail_unknown)
    AppCard(
        containerColor = AppTheme.colors.brand,
        borderColor = null,
        contentPadding = PaddingValues(16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            InfoCell(stringResource(R.string.post_detail_type), post.animalTypeName ?: unknown, Icons.Outlined.Pets, Modifier.weight(1f))
            InfoCell(stringResource(R.string.post_detail_breed), post.breed.ifBlank { unknown }, Icons.Outlined.Sell, Modifier.weight(1f))
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = AppTheme.colors.border)

        Row(modifier = Modifier.fillMaxWidth()) {
            InfoCell(stringResource(R.string.post_detail_size), sizeText(post.size), Icons.Outlined.Straighten, Modifier.weight(1f))
            InfoCell(stringResource(R.string.post_detail_vaccines), vaccinesText(post.vaccinated), Icons.Outlined.TaskAlt, Modifier.weight(1f))
        }
    }
}

// Una celda de datos: cuadro con ícono + etiqueta pequeña + valor
@Composable
private fun InfoCell(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(AppTheme.colors.background, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = AppTheme.colors.textPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = AppTheme.colors.textPrimary)
            Text(value, style = MaterialTheme.typography.titleSmall, color = AppTheme.colors.textPrimary)
        }
    }
}

@Composable
private fun PostDescription(post: PostDetailUi) {
    Column {
        SectionTitle(stringResource(R.string.post_detail_about))
        Spacer(modifier = Modifier.height(8.dp))
        Text(post.description, style = MaterialTheme.typography.bodyLarge, color = AppTheme.colors.textPrimary)
    }
}

// Por ahora el recuadro del mapa queda vacío (se agregará con Mapbox).
@Composable
private fun PostLocation(post: PostDetailUi) {
    Column {
        SectionTitle(stringResource(R.string.post_detail_location))
        Spacer(modifier = Modifier.height(8.dp))
        LocationMapPlaceholder(onSetLocationClick = null, height = 140.dp)
        Spacer(modifier = Modifier.height(8.dp))
        DetailIconText(
            icon = Icons.Outlined.LocationOn,
            text = listOf(post.address, post.city, post.country).filter { it.isNotBlank() }.joinToString(", "),
            bold = true,
        )
    }
}

// Vista previa de comentarios: solo los primeros tres principales y "Ver todos" para la pantalla completa.
@Composable
private fun PostCommentsPreview(
    totalComments: Int,
    uiState: PostDetailUiState,
    canComment: Boolean,
    actions: PostDetailActions,
) {
    val comments = uiState.comments
    val onSeeAll = actions.onOpenComments
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionTitle(stringResource(R.string.post_detail_comments, totalComments))
            if (totalComments > 0) {
                TextButton(onClick = onSeeAll) {
                    Text(
                        stringResource(R.string.post_detail_see_all),
                        style = MaterialTheme.typography.labelMedium,
                        color = AppTheme.colors.textLink,
                    )
                }
            }
        }

        if (comments.isEmpty()) {
            StatusMessage(
                text = stringResource(
                    if (canComment) R.string.post_detail_no_comments else R.string.post_detail_comments_closed
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
            )
        }

        // Sin hilos aquí: "Responder" lleva a la pantalla de comentarios con ese comentario resaltado.
        comments.forEach { comment ->
            CommentItem(
                comment = comment,
                onLikeClick = { actions.onCommentLike(comment.id) },
                onReplyClick = { actions.onReply(comment) },
                canReply = canComment,
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = AppTheme.colors.textPrimary,
        modifier = Modifier.semantics { heading() },
    )
}

// Línea con ícono a la izquierda y texto a la derecha, con el tamaño propio del detalle
@Composable
private fun DetailIconText(icon: ImageVector, text: String, bold: Boolean = false) {
    IconTextRow(
        icon = icon,
        text = text,
        iconSize = 18.dp,
        style = LocalTextStyle.current.copy(
            fontSize = 14.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
        ),
    )
}

// "Armenia, Centro • 1.2 km" (la distancia solo si ambos tienen coordenadas).
@Composable
private fun locationText(post: PostDetailUi): String {
    val place = listOf(post.city, post.address).filter { it.isNotBlank() }.joinToString(", ")
    val distance = post.distanceKm?.let { stringResource(R.string.home_distance_km, String.format(Locale.US, "%.1f", it)) }
    return listOfNotNull(place.ifBlank { null }, distance).joinToString(" • ")
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

@Composable
private fun sizeText(size: AnimalSize?): String = when (size) {
    null -> stringResource(R.string.post_detail_unknown)
    AnimalSize.SMALL -> stringResource(R.string.home_size_small)
    AnimalSize.MEDIUM -> stringResource(R.string.home_size_medium)
    AnimalSize.LARGE -> stringResource(R.string.home_size_large)
}

@Composable
private fun vaccinesText(vaccinated: Boolean?): String = when (vaccinated) {
    true -> stringResource(R.string.post_detail_vaccines_yes)
    false -> stringResource(R.string.post_detail_vaccines_no)
    null -> stringResource(R.string.post_detail_unknown)
}

@Composable
private fun detailMessageText(message: DetailMessage): String = stringResource(
    when (message) {
        DetailMessage.INTEREST_SENT -> R.string.post_detail_msg_interest_sent
        DetailMessage.ALREADY_REQUESTED -> R.string.post_detail_msg_already_requested
        DetailMessage.ACTION_ERROR -> R.string.post_detail_msg_error
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

private val previewComments = listOf(
    CommentUi("c1", null, 0, "u3", "María García", null, "¡Qué hermosa Luna! ¿Tienen horarios de visita?",
        null, 120, 3, 1, isLiked = false, isOwn = false, isDeleted = false, isEdited = false),
    CommentUi("c2", null, 0, "u4", "Carlos Rodríguez", null, "Ya contacté a la fundación. Recomiendo visitarla.",
        null, 300, 7, 0, isLiked = true, isOwn = false, isDeleted = false, isEdited = false),
)

@Preview(name = "Publicación ajena", showBackground = true, heightDp = 1700)
@Composable
private fun PostDetailVisitorPreview() {
    PetAdoptionAppTheme {
        PostDetailContent(
            uiState = PostDetailUiState(
                post = previewPost, comments = previewComments, isLoading = false, viewerName = "ana_77",
                author = PostAuthorUi("u1", "fundacion_huellas", null, "Fundación Huellas"),
            ),
            actions = PostDetailActions(),
        )
    }
}

@Preview(name = "Mi publicación", showBackground = true, heightDp = 1700)
@Composable
private fun PostDetailOwnerPreview() {
    PetAdoptionAppTheme {
        PostDetailContent(
            uiState = PostDetailUiState(post = previewPost, comments = previewComments, isOwner = true, isLoading = false),
            actions = PostDetailActions(),
        )
    }
}
