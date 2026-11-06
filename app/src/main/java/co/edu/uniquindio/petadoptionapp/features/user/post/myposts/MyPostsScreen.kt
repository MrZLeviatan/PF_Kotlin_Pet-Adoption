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
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.util.agoText
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

/**
Pantalla "Mis publicaciones": lista las publicaciones del usuario agrupadas por estado
(Activas, Pendientes, Rechazadas, Finalizadas) con pestañas para filtrar y su contador.
Cada tarjeta muestra categoría, título, fecha, interesados y un pie con acciones según el
estado (finalizar, eliminar o motivo del rechazo). Finalizar y eliminar piden confirmación.
Observa MyPostsUiState del MyPostsViewModel y le envía los eventos; se recarga cada vez que
la pantalla vuelve a primer plano.
Navegación: `onBack` regresa y `onPostClick` abre el detalle; AppNavigation resuelve el destino.
**/

// Margen horizontal de la lista.
private val ScreenHorizontalPadding = 24.dp

/**
Pantalla "Mis publicaciones" con pestañas por estado.
Conecta el [MyPostsViewModel] con la UI; [MyPostsContent] solo dibuja.
No decide a dónde navegar: avisa y AppNavigation resuelve el destino.
**/
@Composable
fun MyPostsScreen(
    // Acción para volver atrás.
    onBack: () -> Unit,
    // Acción para abrir el detalle de una publicación.
    onPostClick: (postId: String) -> Unit,
    // ViewModel inyectado por Hilt.
    viewModel: MyPostsViewModel = hiltViewModel(),
) {
    // Observa el estado respetando el ciclo de vida.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Al volver (por ejemplo, desde el detalle) se recarga por si algo cambió.
    LifecycleResumeEffect(Unit) {
        // Recarga la lista al reanudarse la pantalla.
        viewModel.refresh()
        // No hay nada que limpiar al pausar o salir.
        onPauseOrDispose { }
    }

    // Dibuja el contenido con el estado y las acciones.
    MyPostsContent(
        // Estado actual.
        uiState = uiState,
        // Acciones conectadas al ViewModel y a la navegación.
        actions = MyPostsActions(
            // Volver atrás.
            onBack = onBack,
            // Cambiar de pestaña.
            onFilterSelected = viewModel::onFilterSelected,
            // Abrir una publicación.
            onPostClick = onPostClick,
            // Pedir finalizar.
            onFinishPost = viewModel::onFinishPost,
            // Pedir eliminar.
            onDeletePost = viewModel::onDeletePost,
            // Confirmar la acción del diálogo.
            onConfirmAction = viewModel::onConfirmAction,
            // Cerrar el diálogo.
            onDismissAction = viewModel::onDismissAction,
        ),
    )
}

// Agrupa las acciones de la pantalla (mismo patrón que Registro y Configuración).
private data class MyPostsActions(
    // Volver atrás.
    val onBack: () -> Unit = {},
    // Cambiar de pestaña.
    val onFilterSelected: (PostFilter) -> Unit = {},
    // Abrir una publicación (recibe su id).
    val onPostClick: (String) -> Unit = {},
    // Pedir finalizar (recibe el id).
    val onFinishPost: (String) -> Unit = {},
    // Pedir eliminar (recibe el id).
    val onDeletePost: (String) -> Unit = {},
    // Confirmar la acción del diálogo.
    val onConfirmAction: () -> Unit = {},
    // Cerrar el diálogo.
    val onDismissAction: () -> Unit = {},
)

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun MyPostsContent(
    // Estado a dibujar.
    uiState: MyPostsUiState,
    // Acciones de la interfaz.
    actions: MyPostsActions,
) {
    // Lista de filtros en el orden del enum.
    val filters = PostFilter.entries
    // Etiquetas de las pestañas con su contador, por ejemplo "Activas (2)".
    val tabLabels = filters.map { filter ->
        // Texto traducido con la cantidad de la pestaña.
        stringResource(filter.tabLabelRes(), uiState.countOf(filter))
    }
    // Secciones visibles de la pestaña actual.
    val sections = uiState.visibleSections

    // Columna principal de la pantalla.
    Column(
        // Ocupa toda la pantalla con fondo y respeta las barras del sistema.
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .systemBarsPadding(),
    ) {
        // Barra superior con título y botón de volver.
        AppTopBar(
            // Título "Mis publicaciones".
            title = stringResource(R.string.my_posts_title),
            // Acción de volver.
            onBack = actions.onBack,
            // Margen horizontal.
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        // Fila de pestañas de filtro.
        AppTabRow(
            // Etiquetas de las pestañas.
            tabs = tabLabels,
            // Pestaña seleccionada.
            selectedIndex = filters.indexOf(uiState.filter),
            // Al tocar una pestaña, avisa el filtro correspondiente.
            onTabSelected = { index -> actions.onFilterSelected(filters[index]) },
        )

        // Lista perezosa de secciones y tarjetas.
        LazyColumn(
            // Ocupa el ancho y el alto restante.
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            // Márgenes internos de la lista.
            contentPadding = PaddingValues(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
            // Separación vertical entre elementos.
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Si falló la última acción, muestra el error al inicio.
            if (uiState.actionError) {
                // Elemento con clave fija.
                item(key = "action-error") {
                    // Texto de error de la acción.
                    ErrorText(text = stringResource(R.string.my_posts_action_error), modifier = Modifier.fillMaxWidth())
                }
            }

            // Mensajes de estado de la lista.
            when {
                // Mientras carga no muestra nada.
                uiState.isLoading -> Unit
                // Si hubo error, muestra el mensaje de error.
                uiState.hasError -> item(key = "error") {
                    // Mensaje de error centrado.
                    StatusMessage(
                        // Texto del error.
                        text = stringResource(R.string.my_posts_error),
                        // Estilo de error.
                        isError = true,
                        // Ancho completo con margen vertical.
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                    )
                }
                // Si no hay publicaciones en la pestaña, muestra el mensaje vacío.
                sections.isEmpty() -> item(key = "empty") {
                    // Mensaje de lista vacía.
                    StatusMessage(
                        // Texto de vacío.
                        text = stringResource(R.string.my_posts_empty),
                        // Ancho completo con margen vertical.
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                    )
                }
            }

            // Recorre cada sección (estado) con sus publicaciones.
            sections.forEach { (status, posts) ->
                // Encabezado de la sección.
                item(key = "header-$status") {
                    // Punto de color y título con el contador.
                    SectionHeader(status = status, count = posts.size)
                }
                // Tarjetas de la sección, con el id como clave.
                items(posts, key = { it.id }) { post ->
                    // Tarjeta de la publicación.
                    MyPostItem(
                        // Publicación a dibujar.
                        post = post,
                        // Abrir el detalle.
                        onClick = { actions.onPostClick(post.id) },
                        // Pedir finalizar.
                        onFinish = { actions.onFinishPost(post.id) },
                        // Pedir eliminar.
                        onDelete = { actions.onDeletePost(post.id) },
                    )
                }
            }
        }
    }

    // Confirmación antes de finalizar o eliminar (el caso de uso se llama al aceptar).
    uiState.pendingAction?.let { action ->
        // Indica si la acción pendiente es finalizar.
        val isFinish = action is PendingPostAction.Finish
        // Diálogo de confirmación.
        AppConfirmDialog(
            // Título según la acción.
            title = stringResource(if (isFinish) R.string.my_posts_finish_title else R.string.my_posts_delete_title),
            // Mensaje según la acción.
            message = stringResource(if (isFinish) R.string.my_posts_finish_message else R.string.my_posts_delete_message),
            // Texto del botón de aceptar.
            confirmText = stringResource(R.string.my_posts_dialog_accept),
            // Texto del botón de cancelar.
            cancelText = stringResource(R.string.common_cancel),
            // Acción al confirmar.
            onConfirm = actions.onConfirmAction,
            // Acción al cerrar.
            onDismiss = actions.onDismissAction,
            // Muestra carga mientras se procesa.
            isLoading = uiState.isProcessing,
            // Eliminar no tiene vuelta atrás: confirmar en rojo. Finalizar mantiene el estilo normal.
            isDestructive = !isFinish,
        )
    }
}

// Encabezado de grupo: punto de color y "Activas (2)".
@Composable
private fun SectionHeader(status: PostStatusUi, count: Int) {
    // Fila del encabezado.
    Row(
        // Pequeño margen superior.
        modifier = Modifier.padding(top = 4.dp),
        // Centra verticalmente.
        verticalAlignment = Alignment.CenterVertically,
        // Separación entre punto y texto.
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Punto de color del estado.
        Box(
            // Círculo pequeño con el color del estado.
            modifier = Modifier
                .size(8.dp)
                .background(status.color(), CircleShape),
        )
        // Título de la sección.
        Text(
            // Texto con el nombre del estado y el contador.
            text = stringResource(status.sectionTitleRes(), count),
            // Estilo de título pequeño.
            style = MaterialTheme.typography.titleSmall,
            // Color de texto principal.
            color = AppTheme.colors.textPrimary,
        )
    }
}

// Convierte el modelo de la pantalla en la tarjeta: textos, iconos y pie según el estado.
@Composable
private fun MyPostItem(
    // Publicación a dibujar.
    post: MyPostUi,
    // Acción al tocar la tarjeta.
    onClick: () -> Unit,
    // Acción de finalizar.
    onFinish: () -> Unit,
    // Acción de eliminar.
    onDelete: () -> Unit,
) {
    // Texto relativo de antigüedad ("hace 3 días").
    val ago = agoText(post.ageHours)
    // Sin verificar (pendiente o rechazada) se dice "Enviado"; ya publicada, "Publicado".
    val notPublished = post.status == PostStatusUi.PENDING || post.status == PostStatusUi.REJECTED

    // Texto de la fecha: "Enviado hace..." o "Publicado hace...".
    val dateText = stringResource(
        if (notPublished) R.string.my_posts_sent else R.string.my_posts_published,
        ago,
    )
    // Texto de interesados con plural correcto.
    val interestedText = pluralStringResource(
        R.plurals.my_posts_interested,
        post.interestedCount,
        post.interestedCount,
    )

    // Una publicación sin publicar aún no recibe interesados: no se muestra esa línea.
    val metaLines = buildList {
        // Línea con la fecha.
        add(PostMetaLine(Icons.Outlined.CalendarToday, dateText))
        // Línea con interesados solo si ya está publicada.
        if (!notPublished) add(PostMetaLine(Icons.Outlined.FavoriteBorder, interestedText))
    }

    // Pie de la tarjeta según el estado.
    val footer: (@Composable () -> Unit)? = when (post.status) {
        // Activa: botón de finalizar.
        PostStatusUi.ACTIVE -> ({ ActiveFooter(onFinish = onFinish) })
        // Pendiente: eliminar y nota de "no editable en revisión".
        PostStatusUi.PENDING -> ({ DeleteFooter(onDelete = onDelete, note = stringResource(R.string.my_posts_not_editable)) })
        // Rechazada: motivo y eliminar.
        PostStatusUi.REJECTED -> ({ RejectedFooter(reason = post.rejectionReason, onDelete = onDelete) })
        // Finalizada: sin acciones, solo el aviso (no se edita ni se elimina).
        PostStatusUi.FINISHED -> ({ FinishedFooter() })
    }

    // Mientras carga (o si falla) se ve la imagen local.
    val fallback = painterResource(R.drawable.hero_image)
    // Carga asíncrona de la foto con la imagen local como respaldo.
    val photo = rememberAsyncImagePainter(model = post.photoUrl, placeholder = fallback, error = fallback)

    // Tarjeta resumen de la publicación.
    PostSummaryCard(
        // Imagen: la local si no hay foto, si no la remota.
        image = if (post.photoUrl == null) fallback else photo,
        // Etiqueta con la categoría.
        typeLabel = post.categoryName,
        // Color del estado.
        statusColor = post.status.color(),
        // Título.
        title = post.title,
        // Líneas de metadatos.
        metaLines = metaLines,
        // Acción al tocar.
        onClick = onClick,
        // Pie con acciones.
        footer = footer,
    )
}

// Pie de las activas: botón para finalizar la publicación.
@Composable
private fun ActiveFooter(onFinish: () -> Unit) {
    // Botón de texto con ícono.
    TextActionButton(
        // Texto "Finalizar".
        text = stringResource(R.string.my_posts_finish),
        // Ícono de check.
        icon = Icons.Outlined.CheckCircle,
        // Acción de finalizar.
        onClick = onFinish,
        // Color secundario de acción.
        contentColor = AppTheme.colors.actionSecondary,
    )
}

// Aviso fijo de las finalizadas: el caso ya se cerró y la publicación queda como historial.
@Composable
private fun FinishedFooter() {
    // Misma letra que "No editable en revisión" (labelSmall), un punto más grande; labelMedium es el estilo de los enlaces.
    IconTextRow(
        // Ícono de check.
        icon = Icons.Outlined.CheckCircle,
        // Texto del aviso.
        text = stringResource(R.string.post_finished_notice),
        // Margen vertical.
        modifier = Modifier.padding(vertical = 12.dp),
        // Tamaño del ícono.
        iconSize = 14.dp,
        // Separación entre ícono y texto.
        spacing = 6.dp,
        // Estilo del texto.
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
    )
}

// Rechazada: el motivo del moderador bien visible y solo "Eliminar" (no se edita).
@Composable
private fun RejectedFooter(reason: String?, onDelete: () -> Unit) {
    // Columna con el motivo y el pie de eliminar.
    Column(modifier = Modifier.padding(top = 8.dp)) {
        // Fila con ícono y motivo.
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            // Ícono de bloqueo.
            Icon(
                // Ícono "Block".
                Icons.Outlined.Block,
                // Decorativo, sin descripción.
                contentDescription = null,
                // Color de error.
                tint = AppTheme.colors.stateError,
                // Tamaño del ícono.
                modifier = Modifier.size(16.dp),
            )
            // Texto del motivo.
            Text(
                // Motivo del moderador o un texto genérico si no lo dio.
                text = reason?.let { stringResource(R.string.my_posts_rejected_reason, it) }
                    ?: stringResource(R.string.post_rejected_no_reason),
                // Estilo de cuerpo pequeño.
                style = MaterialTheme.typography.bodySmall,
                // Color de error.
                color = AppTheme.colors.stateError,
            )
        }
        // Botón de eliminar con nota de no editable.
        DeleteFooter(onDelete = onDelete, note = stringResource(R.string.my_posts_not_editable_rejected))
    }
}

// Eliminar a la izquierda y una nota a la derecha ("No editable en revisión" o el motivo del rechazo).
@Composable
private fun DeleteFooter(onDelete: () -> Unit, note: String) {
    // Fila del pie.
    Row(
        // Ancho completo.
        modifier = Modifier.fillMaxWidth(),
        // Separación horizontal.
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        // Centra verticalmente.
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Botón de eliminar.
        TextActionButton(
            // Texto "Eliminar".
            text = stringResource(R.string.my_posts_delete),
            // Ícono de papelera.
            icon = Icons.Outlined.Delete,
            // Acción de eliminar.
            onClick = onDelete,
        )
        // Nota a la derecha.
        Text(
            // Texto de la nota.
            text = note,
            // Estilo de etiqueta pequeña.
            style = MaterialTheme.typography.labelSmall,
            // Color de texto principal.
            color = AppTheme.colors.textPrimary,
            // Máximo dos líneas.
            maxLines = 2,
            // Recorta con puntos suspensivos.
            overflow = TextOverflow.Ellipsis,
            // Ocupa el espacio restante.
            modifier = Modifier.weight(1f),
            // Alineado a la derecha.
            textAlign = TextAlign.End,
        )
    }
}

// Traducen el estado (lógica) al texto, color o recurso (presentación).
// Color de cada estado.
@Composable
private fun PostStatusUi.color() = when (this) {
    // Activa: verde.
    PostStatusUi.ACTIVE -> AppTheme.colors.stateSuccess
    // Pendiente: amarillo.
    PostStatusUi.PENDING -> AppTheme.colors.stateWarning
    // Rechazada: rojo.
    PostStatusUi.REJECTED -> AppTheme.colors.stateError
    // Finalizada: gris.
    PostStatusUi.FINISHED -> AppTheme.colors.stateDisabled
}

// Recurso de texto del título de cada sección.
@StringRes
private fun PostStatusUi.sectionTitleRes() = when (this) {
    // Activas.
    PostStatusUi.ACTIVE -> R.string.my_posts_section_active
    // Pendientes.
    PostStatusUi.PENDING -> R.string.my_posts_section_pending
    // Rechazadas.
    PostStatusUi.REJECTED -> R.string.my_posts_section_rejected
    // Finalizadas.
    PostStatusUi.FINISHED -> R.string.my_posts_section_finished
}

// Recurso de texto de cada pestaña.
@StringRes
private fun PostFilter.tabLabelRes() = when (this) {
    // Todas.
    PostFilter.ALL -> R.string.my_posts_tab_all
    // Activas.
    PostFilter.ACTIVE -> R.string.my_posts_tab_active
    // Pendientes.
    PostFilter.PENDING -> R.string.my_posts_tab_pending
    // Finalizadas.
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

// Vista previa de la pestaña "Todas".
@Preview(name = "Todas", showBackground = true, heightDp = 1100)
@Composable
private fun MyPostsAllPreview() {
    // Aplica el tema.
    PetAdoptionAppTheme {
        // Contenido con publicaciones de ejemplo.
        MyPostsContent(
            // Estado de ejemplo ya cargado.
            uiState = MyPostsUiState(posts = previewPosts, isLoading = false),
            // Acciones vacías.
            actions = MyPostsActions(),
        )
    }
}

// Vista previa con el diálogo de finalizar abierto.
@Preview(name = "Confirmar finalizar", showBackground = true, heightDp = 800)
@Composable
private fun MyPostsFinishDialogPreview() {
    // Aplica el tema.
    PetAdoptionAppTheme {
        // Contenido con la acción pendiente.
        MyPostsContent(
            // Estado de ejemplo con el diálogo abierto.
            uiState = MyPostsUiState(
                // Publicaciones de ejemplo.
                posts = previewPosts,
                // Ya cargado.
                isLoading = false,
                // Finalizar la publicación "1".
                pendingAction = PendingPostAction.Finish("1"),
            ),
            // Acciones vacías.
            actions = MyPostsActions(),
        )
    }
}

// Vista previa de la pestaña "Finalizadas" vacía.
@Preview(name = "Finalizadas (vacía)", showBackground = true, heightDp = 800)
@Composable
private fun MyPostsFinishedEmptyPreview() {
    // Aplica el tema.
    PetAdoptionAppTheme {
        // Contenido filtrado por finalizadas.
        MyPostsContent(
            // Estado de ejemplo sin finalizadas.
            uiState = MyPostsUiState(filter = PostFilter.FINISHED, posts = previewPosts, isLoading = false),
            // Acciones vacías.
            actions = MyPostsActions(),
        )
    }
}
