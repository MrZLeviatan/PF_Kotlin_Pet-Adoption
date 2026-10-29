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

private val ScreenHorizontalPadding = 16.dp

/**
 Inicio del moderador (pestaña Dashboard).
 - Campana: las mismas notificaciones que ve un usuario.
 - Buscador: publicaciones por título, o personas con "@" (solo usuarios; los moderadores no se buscan).
 - Dashboard: prioridad alta, en revisión y pendientes de toda la app; revisadas hoy, del moderador con sesión.
 - Por revisar: publicaciones que ningún moderador ha tomado, de mayor a menor prioridad.
   Tocar "Prioridad alta" deja solo las altas y urgentes (otra vez, quita el filtro).
 No decide a dónde navegar: avisa y la navegación resuelve el destino.
 */
@Composable
fun ModeratorHomeScreen(
    padding: PaddingValues = PaddingValues(),
    onPostClick: (String) -> Unit = {},
    onUserClick: (String) -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    viewModel: ModeratorHomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Al volver (de una publicación o de notificaciones) se recargan números, cola y campana.
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    ModeratorHomeContent(
        state = state,
        padding = padding,
        onQueryChange = viewModel::onQueryChange,
        onPostClick = onPostClick,
        onUserClick = onUserClick,
        onNotificationsClick = onNotificationsClick,
        onHighPriorityClick = viewModel::onHighPriorityClick,
    )
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun ModeratorHomeContent(
    state: ModeratorHomeUiState,
    padding: PaddingValues,
    onQueryChange: (String) -> Unit,
    onPostClick: (String) -> Unit,
    onUserClick: (String) -> Unit,
    onNotificationsClick: () -> Unit,
    onHighPriorityClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(padding)
    ) {
        HomeHeader(
            userName = state.name,
            city = "",
            notifications = state.notifications,
            onNotificationsClick = onNotificationsClick,
            horizontalPadding = ScreenHorizontalPadding,
        )
        SearchField(
            query = state.query,
            onQueryChange = onQueryChange,
            placeholder = stringResource(R.string.mod_home_search_placeholder),
            iconDescription = stringResource(R.string.home_search_description),
            modifier = Modifier.padding(horizontal = ScreenHorizontalPadding),
        )

        when {
            state.isSearchingUsers -> UserSearchResults(
                users = state.users,
                onUserClick = onUserClick,
                modifier = Modifier.weight(1f),
                horizontalPadding = ScreenHorizontalPadding,
            )
            state.isSearching -> PostList(
                modifier = Modifier.weight(1f),
                emptyText = stringResource(R.string.home_search_empty).takeIf { state.postResults.isEmpty() },
            ) {
                items(state.postResults, key = { it.id }) { post ->
                    ModerationPostCard(post = post, onClick = { onPostClick(post.id) })
                }
            }
            else -> PostList(modifier = Modifier.weight(1f)) {
                item(key = "dashboard") {
                    ModerationDashboard(state.stats, highPriorityOnly = state.highPriorityOnly, onHighPriorityClick = onHighPriorityClick)
                }
                val queue = state.visibleQueue
                item(key = "queue-header") { QueueHeader(count = queue.size, highPriorityOnly = state.highPriorityOnly) }
                when {
                    state.hasError -> item(key = "error") { ListMessage(stringResource(R.string.mod_home_error), isError = true) }
                    queue.isEmpty() && !state.isLoading -> item(key = "empty") {
                        ListMessage(stringResource(if (state.highPriorityOnly) R.string.mod_queue_empty_high else R.string.mod_queue_empty))
                    }
                    else -> items(queue, key = { it.id }) { post ->
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
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (emptyText != null) item(key = "empty") { ListMessage(emptyText) } else content()
    }
}

@Composable
private fun ListMessage(text: String, isError: Boolean = false) {
    StatusMessage(
        text = text,
        isError = isError,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
    )
}

// Título y las cuatro métricas en dos filas; cada fila con tarjetas del mismo alto.
@Composable
private fun ModerationDashboard(stats: ModerationStats, highPriorityOnly: Boolean, onHighPriorityClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.mod_home_dashboard_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.textPrimary,
        )
        StatRow(
            first = StatItem(
                R.string.mod_stat_high_priority, stats.highPriority, R.string.mod_stat_high_priority_desc, AppTheme.colors.stateError,
                onClick = onHighPriorityClick, selected = highPriorityOnly,
            ),
            second = StatItem(R.string.mod_stat_in_review, stats.inReview, R.string.mod_stat_in_review_desc, AppTheme.colors.stateWarning),
        )
        StatRow(
            first = StatItem(R.string.mod_stat_reviewed_today, stats.reviewedToday, R.string.mod_stat_reviewed_today_desc, AppTheme.colors.stateSuccess),
            second = StatItem(R.string.mod_stat_pending, stats.pending, R.string.mod_stat_pending_desc, AppTheme.colors.textLink),
        )
    }
}

private data class StatItem(
    val label: Int,
    val value: Int,
    val description: Int,
    val color: Color,
    val onClick: (() -> Unit)? = null,
    val selected: Boolean = false,
)

@Composable
private fun StatRow(first: StatItem, second: StatItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        listOf(first, second).forEach { item ->
            StatCard(
                label = stringResource(item.label),
                value = item.value.toString(),
                description = stringResource(item.description),
                indicatorColor = item.color,
                onClick = item.onClick,
                selected = item.selected,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }
}

// "Por revisar" (o "Prioridad alta" con el filtro) y cuántas hay.
@Composable
private fun QueueHeader(count: Int, highPriorityOnly: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(if (highPriorityOnly) R.string.mod_stat_high_priority else R.string.mod_queue_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = pluralStringResource(R.plurals.mod_queue_count, count, count),
            style = MaterialTheme.typography.labelMedium,
            color = AppTheme.colors.textPrimary,
        )
    }
}

// Tarjeta grande: foto, categoría y prioridad, título, motivo de la IA y antigüedad.
@Composable
private fun ModerationPostCard(post: ModerationPostUi, onClick: () -> Unit) {
    val fallback = painterResource(R.drawable.hero_image)
    AppCard(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
        AsyncImage(
            model = post.imageUrl,
            contentDescription = null,   // Decorativa: el título ya describe la publicación.
            contentScale = ContentScale.Crop,
            placeholder = fallback,
            error = fallback,
            fallback = fallback,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
        )
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (post.categoryName.isNotBlank()) {
                    Text(
                        text = post.categoryName,
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.colors.textPrimary,
                        modifier = Modifier
                            .background(AppTheme.colors.surfaceTag, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                }
                StatusIndicator(color = priorityColor(post.priority))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = priorityText(post.priority),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                // En los resultados de búsqueda también salen las tomadas o resueltas: se dice su estado.
                statusText(post.verificationStatus)?.let {
                    Text(text = it, style = MaterialTheme.typography.labelSmall, color = AppTheme.colors.textPrimary)
                }
            }
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (post.priorityReason.isNotBlank()) {
                IconTextRow(icon = Icons.Outlined.AutoAwesome, text = post.priorityReason, iconSize = 14.dp, spacing = 6.dp, style = MaterialTheme.typography.bodySmall)
            }
            IconTextRow(
                icon = Icons.Outlined.Schedule,
                text = stringResource(R.string.my_posts_published, agoText(post.ageHours)),
                iconSize = 14.dp,
                spacing = 6.dp,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

// En la cola todas son "sin moderador", así que ahí no se repite; en la búsqueda sí se indica.
@Composable
private fun statusText(status: VerificationStatus): String? = when (status) {
    VerificationStatus.UNVERIFIED -> null
    VerificationStatus.IN_PROCESS -> stringResource(R.string.mod_status_in_review)
    VerificationStatus.VERIFIED -> stringResource(R.string.mod_status_verified)
    VerificationStatus.REJECTED -> stringResource(R.string.mod_status_rejected)
}

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun ModeratorHomePreview() {
    PetAdoptionAppTheme {
        Box {
            ModeratorHomeContent(
                state = ModeratorHomeUiState(
                    name = "Nicolas",
                    notifications = 3,
                    stats = ModerationStats(highPriority = 3, inReview = 7, reviewedToday = 12, pending = 2),
                    queue = listOf(
                        ModerationPostUi("p6", null, "Perrita herida necesita hogar urgente", "Adopción", PriorityLevel.URGENT,
                            "Animal herido que necesita atención", 5, VerificationStatus.UNVERIFIED),
                    ),
                    isLoading = false,
                ),
                padding = PaddingValues(),
                onQueryChange = {}, onPostClick = {}, onUserClick = {}, onNotificationsClick = {},
            )
        }
    }
}
