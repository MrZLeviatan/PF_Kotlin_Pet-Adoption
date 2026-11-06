package co.edu.uniquindio.petadoptionapp.features.moderator.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.clickable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.size
import co.edu.uniquindio.petadoptionapp.core.component.button.SecondaryButton
import co.edu.uniquindio.petadoptionapp.features.moderator.AiReviewSummary
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.card.AppCard
import co.edu.uniquindio.petadoptionapp.core.component.card.StatCard
import co.edu.uniquindio.petadoptionapp.core.component.form.SearchField
import co.edu.uniquindio.petadoptionapp.core.component.header.HomeHeader
import co.edu.uniquindio.petadoptionapp.core.component.indicator.StatusIndicator
import co.edu.uniquindio.petadoptionapp.core.component.list.UserSearchResults
import co.edu.uniquindio.petadoptionapp.core.component.text.IconTextRow
import co.edu.uniquindio.petadoptionapp.core.component.text.StatusMessage
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import co.edu.uniquindio.petadoptionapp.core.util.agoText
import co.edu.uniquindio.petadoptionapp.domain.model.post.ModerationStats
import co.edu.uniquindio.petadoptionapp.features.moderator.priorityColor
import co.edu.uniquindio.petadoptionapp.features.moderator.priorityText
import co.edu.uniquindio.petadoptionapp.domain.model.post.PriorityLevel
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import coil3.compose.AsyncImage

/**
Pantalla de inicio del moderador (pestaña Dashboard del grafo de navegación del moderador).
Observa ModeratorHomeUiState desde ModeratorHomeViewModel: saludo, campana de notificaciones, buscador
(publicaciones por título o usuarios con "@"), dashboard de métricas que sirve de filtro por prioridad,
barra de análisis por lotes con IA y la cola de publicaciones sin moderador asignado.
Eventos que envía al ViewModel: onQueryChange, onFilterClick, onAnalyzePendingClick y onAiMessageShown;
además llama a refresh() cada vez que la pantalla vuelve a primer plano.
Casos de uso (a través del ViewModel): GetCurrentModerator, GetModerationStats, GetModerationQueue, GetFeed,
LoadCatalog, ObserveCategories, CountUnreadNotifications, SearchUsers y AnalyzePost.
Navegación: no navega por sí misma; expone onPostClick (detalle de publicación), onUserClick (perfil de usuario)
y onNotificationsClick (notificaciones) para que el NavHost decida el destino.
**/

// Margen horizontal común de toda la pantalla.
private val ScreenHorizontalPadding = 16.dp

/**
 Inicio del moderador (pestaña Dashboard).
 - Campana: las mismas notificaciones que ve un usuario.
 - Buscador: publicaciones por título, o personas con "@" (solo usuarios; los moderadores no se buscan).
 - Dashboard: cuatro tarjetas (prioridad alta, media, baja y "Pendientes") con las cifras de toda la app.
 - Por revisar: publicaciones que ningún moderador ha tomado, de mayor a menor prioridad.
   Las tarjetas cuentan las que no tienen moderador por prioridad (alta, media, baja) y en total ("Pendientes");
   tocar una filtra la lista por esa prioridad, y "Pendientes" (o tocar otra vez la misma) muestra todas.
 No decide a dónde navegar: avisa y la navegación resuelve el destino.
 */
@Composable
fun ModeratorHomeScreen(
    // Márgenes que entrega el Scaffold contenedor (barra inferior, etc.).
    padding: PaddingValues = PaddingValues(),
    // Se invoca con el id de la publicación tocada para abrir su detalle.
    onPostClick: (String) -> Unit = {},
    // Se invoca con el id del usuario tocado en la búsqueda para abrir su perfil.
    onUserClick: (String) -> Unit = {},
    // Se invoca al tocar la campana para abrir las notificaciones.
    onNotificationsClick: () -> Unit = {},
    // ViewModel inyectado por Hilt.
    viewModel: ModeratorHomeViewModel = hiltViewModel(),
) {
    // Observa el estado del ViewModel respetando el ciclo de vida.
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Al volver (de una publicación o de notificaciones) se recargan números, cola y campana.
    LifecycleResumeEffect(Unit) {
        // Recarga los datos cada vez que la pantalla pasa a primer plano.
        viewModel.refresh()
        // No hay nada que limpiar al pausar o salir.
        onPauseOrDispose { }
    }

    // Dibuja el contenido conectando el estado y los eventos del ViewModel.
    ModeratorHomeContent(
        // Estado actual de la pantalla.
        state = state,
        // Márgenes del contenedor.
        padding = padding,
        // Cambios del buscador hacia el ViewModel.
        onQueryChange = viewModel::onQueryChange,
        // Clic en publicación hacia la navegación.
        onPostClick = onPostClick,
        // Clic en usuario hacia la navegación.
        onUserClick = onUserClick,
        // Clic en la campana hacia la navegación.
        onNotificationsClick = onNotificationsClick,
        // Clic en una tarjeta del dashboard para filtrar la cola.
        onFilterClick = viewModel::onFilterClick,
        // Clic en "Analizar con IA".
        onAnalyzePending = viewModel::onAnalyzePendingClick,
        // Aviso de que el mensaje de la IA ya se mostró.
        onAiMessageShown = viewModel::onAiMessageShown,
    )
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun ModeratorHomeContent(
    // Estado que se dibuja.
    state: ModeratorHomeUiState,
    // Márgenes del contenedor.
    padding: PaddingValues,
    // Evento de cambio de texto del buscador.
    onQueryChange: (String) -> Unit,
    // Evento de clic en una publicación.
    onPostClick: (String) -> Unit,
    // Evento de clic en un usuario.
    onUserClick: (String) -> Unit,
    // Evento de clic en la campana.
    onNotificationsClick: () -> Unit,
    // Evento de clic en una tarjeta de filtro.
    onFilterClick: (QueueFilter) -> Unit = {},
    // Evento para lanzar el análisis por lotes con IA.
    onAnalyzePending: () -> Unit = {},
    // Evento para descartar el mensaje del análisis.
    onAiMessageShown: () -> Unit = {},
) {
    // Columna principal que ocupa toda la pantalla.
    Column(
        // Ocupa todo el espacio, pinta el fondo y aplica los márgenes del contenedor.
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(padding)
    ) {
        // Encabezado con saludo y campana de notificaciones.
        HomeHeader(
            // Nombre del moderador para el saludo.
            userName = state.name,
            // El moderador no muestra ciudad.
            city = "",
            // Número de notificaciones sin leer.
            notifications = state.notifications,
            // Acción al tocar la campana.
            onNotificationsClick = onNotificationsClick,
            // Margen horizontal del encabezado.
            horizontalPadding = ScreenHorizontalPadding,
        )
        // Campo de búsqueda de publicaciones o usuarios.
        SearchField(
            // Texto actual.
            query = state.query,
            // Notifica cada cambio de texto.
            onQueryChange = onQueryChange,
            // Texto de ayuda del campo.
            placeholder = stringResource(R.string.mod_home_search_placeholder),
            // Descripción accesible del ícono de búsqueda.
            iconDescription = stringResource(R.string.home_search_description),
            // Margen horizontal del campo.
            modifier = Modifier.padding(horizontal = ScreenHorizontalPadding),
        )

        // Decide qué mostrar debajo del buscador según el modo actual.
        when {
            // Búsqueda con "@": lista de usuarios encontrados.
            state.isSearchingUsers -> UserSearchResults(
                // Usuarios a mostrar.
                users = state.users,
                // Acción al tocar un usuario.
                onUserClick = onUserClick,
                // Ocupa el espacio restante.
                modifier = Modifier.weight(1f),
                // Margen horizontal de la lista.
                horizontalPadding = ScreenHorizontalPadding,
            )
            // Búsqueda por título: lista de publicaciones encontradas.
            state.isSearching -> PostList(
                // Ocupa el espacio restante.
                modifier = Modifier.weight(1f),
                // Texto de "sin resultados" solo cuando la lista está vacía.
                emptyText = stringResource(R.string.home_search_empty).takeIf { state.postResults.isEmpty() },
            ) {
                // Una tarjeta por cada publicación encontrada, identificada por su id.
                items(state.postResults, key = { it.id }) { post ->
                    // Tarjeta que abre el detalle al tocarla.
                    ModerationPostCard(post = post, onClick = { onPostClick(post.id) })
                }
            }
            // Sin búsqueda: dashboard, barra de IA y cola de moderación.
            else -> PostList(modifier = Modifier.weight(1f)) {
                // Primer elemento: el dashboard de métricas.
                item(key = "dashboard") {
                    // Tarjetas de estadísticas que también filtran la cola.
                    ModerationDashboard(state.stats, filter = state.filter, onFilterClick = onFilterClick)
                }
                // Cola ya filtrada según la tarjeta elegida.
                val queue = state.visibleQueue
                // Encabezado de la cola con su título y conteo.
                item(key = "queue-header") { QueueHeader(count = queue.size, filter = state.filter) }
                // IA: analizar las que aún no tienen análisis, con su progreso y resultado.
                if (state.notAnalyzedCount > 0 || state.aiProgress != null || state.aiMessage != null) {
                    // Elemento de la lista con la barra de IA.
                    item(key = "ai-batch") {
                        // Barra con botón, progreso o resultado del análisis por lotes.
                        AiBatchBar(
                            // Cantidad de publicaciones sin análisis.
                            notAnalyzed = state.notAnalyzedCount,
                            // Progreso actual del lote (null si no corre).
                            progress = state.aiProgress,
                            // Resultado del último lote.
                            message = state.aiMessage,
                            // Acción para iniciar el análisis.
                            onAnalyze = onAnalyzePending,
                            // Acción para descartar el mensaje.
                            onMessageShown = onAiMessageShown,
                        )
                    }
                }
                // Contenido de la cola según el estado de carga.
                when {
                    // Si hubo error, se muestra un mensaje de error.
                    state.hasError -> item(key = "error") { ListMessage(stringResource(R.string.mod_home_error), isError = true) }
                    // Si la cola está vacía y ya cargó, se muestra el mensaje de vacío.
                    queue.isEmpty() && !state.isLoading -> item(key = "empty") {
                        // Mensaje distinto si hay un filtro activo o no.
                        ListMessage(stringResource(if (state.filter == QueueFilter.ALL) R.string.mod_queue_empty else R.string.mod_queue_empty_filtered))
                    }
                    // En otro caso se dibuja una tarjeta por cada publicación de la cola.
                    else -> items(queue, key = { it.id }) { post ->
                        // Tarjeta que abre el detalle al tocarla.
                        ModerationPostCard(post = post, onClick = { onPostClick(post.id) })
                    }
                }
            }
        }
    }
}

// Lista desplazable con el mismo margen para el dashboard, la cola y los resultados.
@Composable
private fun PostList(modifier: Modifier, emptyText: String? = null, content: LazyListScope.() -> Unit) {
    // Lista perezosa vertical.
    LazyColumn(
        // Modificador recibido del llamador.
        modifier = modifier,
        // Márgenes internos horizontales y verticales.
        contentPadding = PaddingValues(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
        // Separación entre elementos.
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Si hay texto de vacío se muestra solo ese mensaje; si no, el contenido recibido.
        if (emptyText != null) item(key = "empty") { ListMessage(emptyText) } else content()
    }
}

// Mensaje centrado dentro de la lista (vacío o error).
@Composable
private fun ListMessage(text: String, isError: Boolean = false) {
    // Componente común para mensajes de estado.
    StatusMessage(
        // Texto a mostrar.
        text = text,
        // Indica si se pinta como error.
        isError = isError,
        // Ocupa todo el ancho con margen vertical.
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
    )
}

// Título y las cuatro métricas en dos filas; cada fila con tarjetas del mismo alto.
@Composable
private fun ModerationDashboard(stats: ModerationStats, filter: QueueFilter, onFilterClick: (QueueFilter) -> Unit) {
    // Columna con separación entre título y filas.
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Título del dashboard.
        Text(
            // Texto del título.
            text = stringResource(R.string.mod_home_dashboard_title),
            // Estilo de titular pequeño.
            style = MaterialTheme.typography.headlineSmall,
            // Texto en negrita.
            fontWeight = FontWeight.Bold,
            // Color principal del texto.
            color = AppTheme.colors.textPrimary,
        )
        // Mismos colores que la prioridad en las tarjetas de la lista.
        StatRow(
            // Tarjeta de prioridad alta: filtra por HIGH y queda marcada si ese es el filtro.
            first = StatItem(
                R.string.mod_stat_high_priority, stats.highPriority, R.string.mod_stat_high_priority_desc, AppTheme.colors.stateError,
                onClick = { onFilterClick(QueueFilter.HIGH) }, selected = filter == QueueFilter.HIGH,
            ),
            // Tarjeta de prioridad media: filtra por MEDIUM.
            second = StatItem(
                R.string.mod_stat_medium_priority, stats.mediumPriority, R.string.mod_stat_medium_priority_desc, AppTheme.colors.stateWarning,
                onClick = { onFilterClick(QueueFilter.MEDIUM) }, selected = filter == QueueFilter.MEDIUM,
            ),
        )
        // Segunda fila de tarjetas.
        StatRow(
            // Tarjeta de prioridad baja: filtra por LOW.
            first = StatItem(
                R.string.mod_stat_low_priority, stats.lowPriority, R.string.mod_stat_low_priority_desc, AppTheme.colors.stateSuccess,
                onClick = { onFilterClick(QueueFilter.LOW) }, selected = filter == QueueFilter.LOW,
            ),
            // "Pendientes" es la vista general: queda marcada cuando no hay filtro.
            second = StatItem(
                R.string.mod_stat_pending, stats.pending, R.string.mod_stat_pending_desc, AppTheme.colors.textLink,
                onClick = { onFilterClick(QueueFilter.ALL) }, selected = filter == QueueFilter.ALL,
            ),
        )
    }
}

// Datos de una tarjeta de métrica del dashboard.
private data class StatItem(
    // Recurso de texto de la etiqueta.
    val label: Int,
    // Valor numérico a mostrar.
    val value: Int,
    // Recurso de texto de la descripción.
    val description: Int,
    // Color del indicador de la tarjeta.
    val color: Color,
    // Acción al tocar la tarjeta (null si no es tocable).
    val onClick: (() -> Unit)? = null,
    // Indica si la tarjeta está marcada como filtro activo.
    val selected: Boolean = false,
)

// Fila con dos tarjetas de métrica de igual ancho y alto.
@Composable
private fun StatRow(first: StatItem, second: StatItem) {
    // Fila horizontal contenedora.
    Row(
        // Ocupa todo el ancho y toma el alto de la tarjeta más alta.
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        // Separación entre las dos tarjetas.
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Dibuja ambas tarjetas con el mismo código.
        listOf(first, second).forEach { item ->
            // Tarjeta de estadística reutilizable.
            StatCard(
                // Etiqueta localizada.
                label = stringResource(item.label),
                // Valor como texto.
                value = item.value.toString(),
                // Descripción localizada.
                description = stringResource(item.description),
                // Color del indicador.
                indicatorColor = item.color,
                // Acción al tocarla.
                onClick = item.onClick,
                // Marca de selección.
                selected = item.selected,
                // Reparte el ancho a partes iguales y llena el alto de la fila.
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }
}

// Barra de la IA: "Analizar con IA (N)" o el progreso; al terminar, el resultado.
@Composable
private fun AiBatchBar(
    // Cantidad de publicaciones sin análisis.
    notAnalyzed: Int,
    // Progreso (índice actual, total) o null si no hay lote en curso.
    progress: Pair<Int, Int>?,
    // Resultado del último lote o null.
    message: AiBatchMessage?,
    // Acción para iniciar el análisis.
    onAnalyze: () -> Unit,
    // Acción para descartar el mensaje.
    onMessageShown: () -> Unit,
) {
    // Columna con el botón/progreso y el mensaje.
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Elige entre progreso o botón.
        when {
            // Si hay un lote en curso, muestra el indicador de progreso.
            progress != null -> Row(verticalAlignment = Alignment.CenterVertically) {
                // Indicador circular pequeño.
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = AppTheme.colors.textPrimary)
                // Espacio entre indicador y texto.
                Spacer(Modifier.width(8.dp))
                // Texto "Analizando X de Y".
                Text(
                    // Muestra la posición actual (base 1) y el total.
                    stringResource(R.string.ai_batch_progress, progress.first + 1, progress.second),
                    // Estilo de texto pequeño.
                    style = MaterialTheme.typography.bodySmall,
                    // Color principal.
                    color = AppTheme.colors.textPrimary,
                )
            }
            // Si hay publicaciones sin analizar, muestra el botón para analizarlas.
            notAnalyzed > 0 -> SecondaryButton(
                // Texto en plural con la cantidad pendiente.
                text = pluralStringResource(R.plurals.ai_batch_button, notAnalyzed, notAnalyzed),
                // Ícono de IA.
                icon = Icons.Outlined.AutoAwesome,
                // Acción al pulsarlo.
                onClick = onAnalyze,
            )
        }
        // Si hay un resultado pendiente, lo muestra.
        message?.let {
            // Elige el texto según el resultado.
            val text = stringResource(
                when (it) {
                    // Análisis completado.
                    AiBatchMessage.DONE -> R.string.ai_batch_done
                    // La IA no tiene llave configurada.
                    AiBatchMessage.NOT_CONFIGURED -> R.string.ai_not_configured
                    // Alguna publicación no se pudo analizar.
                    AiBatchMessage.FAILED -> R.string.ai_failed
                }
            )
            // Texto del resultado; al tocarlo se descarta.
            Text(
                // Mensaje elegido.
                text = text,
                // Estilo pequeño.
                style = MaterialTheme.typography.bodySmall,
                // Color normal si salió bien; color de error en otro caso.
                color = if (it == AiBatchMessage.DONE) AppTheme.colors.textPrimary else AppTheme.colors.stateError,
                // Al tocarlo se avisa que ya se mostró.
                modifier = Modifier.clickable(onClick = onMessageShown),
            )
        }
    }
}

// "Por revisar" (o "Prioridad alta" con el filtro) y cuántas hay.
@Composable
private fun QueueHeader(count: Int, filter: QueueFilter) {
    // Fila con título a la izquierda y conteo a la derecha.
    Row(
        // Ocupa todo el ancho con un pequeño margen superior.
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        // Centra verticalmente los textos.
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Título de la cola.
        Text(
            // Elige el título según el filtro activo.
            text = stringResource(
                when (filter) {
                    // Sin filtro: "Por revisar".
                    QueueFilter.ALL -> R.string.mod_queue_title
                    // Filtro de prioridad alta.
                    QueueFilter.HIGH -> R.string.mod_stat_high_priority
                    // Filtro de prioridad media.
                    QueueFilter.MEDIUM -> R.string.mod_stat_medium_priority
                    // Filtro de prioridad baja.
                    QueueFilter.LOW -> R.string.mod_stat_low_priority
                }
            ),
            // Estilo de título grande.
            style = MaterialTheme.typography.titleLarge,
            // Texto en negrita.
            fontWeight = FontWeight.Bold,
            // Color principal.
            color = AppTheme.colors.textPrimary,
            // Ocupa el espacio sobrante para empujar el conteo a la derecha.
            modifier = Modifier.weight(1f),
        )
        // Conteo de publicaciones.
        Text(
            // Texto en plural con la cantidad.
            text = pluralStringResource(R.plurals.mod_queue_count, count, count),
            // Estilo de etiqueta.
            style = MaterialTheme.typography.labelMedium,
            // Color principal.
            color = AppTheme.colors.textPrimary,
        )
    }
}

// Tarjeta grande: foto, categoría y prioridad, título, motivo de la IA y antigüedad.
@Composable
private fun ModerationPostCard(post: ModerationPostUi, onClick: () -> Unit) {
    // Imagen de respaldo para carga, error o ausencia de foto.
    val fallback = painterResource(R.drawable.hero_image)
    // Tarjeta tocable sin relleno interno (la foto llega a los bordes).
    AppCard(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
        // Foto de la publicación cargada desde la red con Coil.
        AsyncImage(
            // URL de la imagen.
            model = post.imageUrl,
            // Decorativa: el título ya describe la publicación.
            contentDescription = null,
            // Recorta la imagen para llenar el área.
            contentScale = ContentScale.Crop,
            // Imagen mientras carga.
            placeholder = fallback,
            // Imagen si falla la carga.
            error = fallback,
            // Imagen si no hay URL.
            fallback = fallback,
            // Ancho completo y alto fijo.
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
        )
        // Columna con los textos de la tarjeta.
        Column(
            // Relleno interno.
            modifier = Modifier.padding(14.dp),
            // Separación entre filas.
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Fila de categoría, prioridad y estado.
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Solo muestra la categoría si tiene nombre.
                if (post.categoryName.isNotBlank()) {
                    // Etiqueta de la categoría.
                    Text(
                        // Nombre de la categoría.
                        text = post.categoryName,
                        // Estilo de etiqueta pequeña.
                        style = MaterialTheme.typography.labelSmall,
                        // Color principal.
                        color = AppTheme.colors.textPrimary,
                        // Fondo redondeado tipo chip con relleno.
                        modifier = Modifier
                            .background(AppTheme.colors.surfaceTag, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                    // Espacio tras la categoría.
                    Spacer(Modifier.width(8.dp))
                }
                // Punto de color según la prioridad.
                StatusIndicator(color = priorityColor(post.priority))
                // Espacio entre punto y texto.
                Spacer(Modifier.width(6.dp))
                // Texto de la prioridad.
                Text(
                    // Nombre localizado de la prioridad.
                    text = priorityText(post.priority),
                    // Estilo de etiqueta mediana.
                    style = MaterialTheme.typography.labelMedium,
                    // Texto en negrita.
                    fontWeight = FontWeight.Bold,
                    // Color principal.
                    color = AppTheme.colors.textPrimary,
                    // Ocupa el espacio sobrante.
                    modifier = Modifier.weight(1f),
                )
                // En los resultados de búsqueda también salen las tomadas o resueltas: se dice su estado.
                statusText(post.verificationStatus)?.let {
                    // Texto del estado de verificación.
                    Text(text = it, style = MaterialTheme.typography.labelSmall, color = AppTheme.colors.textPrimary)
                }
            }
            // Título de la publicación.
            Text(
                // Texto del título.
                text = post.title,
                // Estilo de título mediano.
                style = MaterialTheme.typography.titleMedium,
                // Texto en negrita.
                fontWeight = FontWeight.Bold,
                // Color principal.
                color = AppTheme.colors.textPrimary,
                // Máximo dos líneas.
                maxLines = 2,
                // Puntos suspensivos si se corta.
                overflow = TextOverflow.Ellipsis,
            )
            // Si la IA dio un motivo de prioridad, se muestra.
            if (post.priorityReason.isNotBlank()) {
                // Fila con ícono de IA y el motivo.
                IconTextRow(icon = Icons.Outlined.AutoAwesome, text = post.priorityReason, iconSize = 14.dp, spacing = 6.dp, style = MaterialTheme.typography.bodySmall)
            }
            // Resumen de la IA para revisar rápido: relevancia, alertas, resumen y siguiente acción.
            AiReviewSummary(review = post.aiReview)
            // Fila con la antigüedad de la publicación.
            IconTextRow(
                // Ícono de reloj.
                icon = Icons.Outlined.Schedule,
                // Texto "Publicado hace ...".
                text = stringResource(R.string.my_posts_published, agoText(post.ageHours)),
                // Tamaño del ícono.
                iconSize = 14.dp,
                // Espacio entre ícono y texto.
                spacing = 6.dp,
                // Estilo de texto pequeño.
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

// En la cola todas son "sin moderador", así que ahí no se repite; en la búsqueda sí se indica.
@Composable
private fun statusText(status: VerificationStatus): String? = when (status) {
    // Sin verificar: no se muestra texto.
    VerificationStatus.UNVERIFIED -> null
    // En revisión por un moderador.
    VerificationStatus.IN_PROCESS -> stringResource(R.string.mod_status_in_review)
    // Ya verificada.
    VerificationStatus.VERIFIED -> stringResource(R.string.mod_status_verified)
    // Rechazada.
    VerificationStatus.REJECTED -> stringResource(R.string.mod_status_rejected)
}

// Vista previa del inicio del moderador con datos de ejemplo.
@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun ModeratorHomePreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Contenedor simple.
        Box {
            // Contenido con un estado de ejemplo.
            ModeratorHomeContent(
                // Estado simulado.
                state = ModeratorHomeUiState(
                    // Nombre del saludo.
                    name = "Nicolas",
                    // Notificaciones sin leer.
                    notifications = 3,
                    // Métricas de ejemplo.
                    stats = ModerationStats(highPriority = 3, mediumPriority = 7, lowPriority = 2, pending = 12),
                    // Cola con una publicación urgente de ejemplo.
                    queue = listOf(
                        ModerationPostUi("p6", null, "Perrita herida necesita hogar urgente", "Adopción", PriorityLevel.URGENT,
                            "Animal herido que necesita atención", 5, VerificationStatus.UNVERIFIED),
                    ),
                    // Ya terminó de cargar.
                    isLoading = false,
                ),
                // Sin márgenes extra.
                padding = PaddingValues(),
                // Eventos vacíos para la vista previa.
                onQueryChange = {}, onPostClick = {}, onUserClick = {}, onNotificationsClick = {},
            )
        }
    }
}
