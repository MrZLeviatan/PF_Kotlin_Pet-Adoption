package co.edu.uniquindio.petadoptionapp.features.moderator.stats

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.card.AppCard
import co.edu.uniquindio.petadoptionapp.core.component.card.SectionCard
import co.edu.uniquindio.petadoptionapp.core.component.card.StatCard
import co.edu.uniquindio.petadoptionapp.core.component.text.StatusMessage
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import co.edu.uniquindio.petadoptionapp.domain.model.post.ModeratorPerformance
import co.edu.uniquindio.petadoptionapp.domain.model.post.PriorityLevel
import co.edu.uniquindio.petadoptionapp.features.moderator.priorityColor
import co.edu.uniquindio.petadoptionapp.features.moderator.priorityText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val ScreenHorizontalPadding = 16.dp

/**
 Dashboard del moderador (pestaña Estadísticas): cuántas resolvió y con qué tasa de aprobación,
 cuántas tiene pendientes, su actividad de los últimos 7 días, pendientes por prioridad y resueltas por categoría.
 Los gráficos se dibujan con Compose (sin librerías).
 */
@Composable
fun ModeratorStatsScreen(
    padding: PaddingValues = PaddingValues(),
    viewModel: ModeratorStatsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Al volver (después de aprobar o rechazar) los números se actualizan.
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    ModeratorStatsContent(uiState = uiState, padding = padding)
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun ModeratorStatsContent(uiState: ModeratorStatsUiState, padding: PaddingValues) {
    val p = uiState.performance
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenHorizontalPadding, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column {
            Text(
                text = stringResource(R.string.mod_stats_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(R.string.mod_stats_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.textPrimary,
            )
        }

        if (uiState.hasError) {
            StatusMessage(text = stringResource(R.string.mod_stats_error), isError = true, modifier = Modifier.fillMaxWidth())
            return@Column
        }

        PerformanceSummaryCard(performance = p)

        // Indicadores clave en dos filas de tarjetas del mismo alto.
        KpiRow(
            first = Kpi(R.string.mod_stats_pending, p.inReview.toString(), R.string.mod_stats_pending_desc, AppTheme.colors.stateWarning),
            second = Kpi(R.string.mod_stats_today, p.reviewedToday.toString(), R.string.mod_stats_today_desc, AppTheme.colors.stateSuccess),
        )
        KpiRow(
            first = Kpi(R.string.mod_stats_week, p.reviewedLast7Days.toString(), R.string.mod_stats_week_desc, AppTheme.colors.textLink),
            second = Kpi(
                R.string.mod_stats_daily_avg,
                String.format(Locale.getDefault(), "%.1f", p.reviewedLast7Days / 7f),
                R.string.mod_stats_daily_avg_desc,
                AppTheme.colors.brand,
            ),
        )

        SectionCard(title = stringResource(R.string.mod_stats_activity), icon = Icons.Outlined.BarChart) {
            WeeklyBarChart(values = p.dailyReviews)
        }

        SectionCard(title = stringResource(R.string.mod_stats_by_priority), icon = Icons.Outlined.PendingActions) {
            if (p.inReview == 0) {
                StatusMessage(text = stringResource(R.string.mod_stats_no_pending))
            } else {
                val order = listOf(PriorityLevel.URGENT, PriorityLevel.HIGH, PriorityLevel.MEDIUM, PriorityLevel.LOW, null)
                val max = p.inReviewByPriority.values.maxOrNull() ?: 1
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    order.filter { (p.inReviewByPriority[it] ?: 0) > 0 }.forEach { priority ->
                        HorizontalBar(
                            label = priorityText(priority),
                            value = p.inReviewByPriority.getValue(priority),
                            max = max,
                            color = priorityColor(priority),
                        )
                    }
                }
            }
        }

        SectionCard(title = stringResource(R.string.mod_stats_by_category), icon = Icons.Outlined.Category) {
            if (uiState.categories.isEmpty()) {
                StatusMessage(text = stringResource(R.string.mod_stats_no_resolved))
            } else {
                val max = uiState.categories.maxOf { it.second }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    uiState.categories.forEach { (name, count) ->
                        HorizontalBar(label = name, value = count, max = max, color = AppTheme.colors.textLink)
                    }
                }
            }
        }
    }
}

/**
 Resumen principal: anillo con la tasa de aprobación, total resuelto y la leyenda aprobadas / rechazadas.
 También se usa en el perfil del moderador ([onClick] lleva a este dashboard).
 */
@Composable
fun PerformanceSummaryCard(performance: ModeratorPerformance, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    AppCard(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        onClick = onClick,
        onClickLabel = onClick?.let { stringResource(R.string.mod_stats_open) },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ApprovalRing(
                approved = performance.approved,
                rejected = performance.rejected,
                rate = performance.approvalRate,
                size = 112.dp,
            )
            Spacer(Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.mod_stats_resolved).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppTheme.colors.textPrimary,
                )
                Text(
                    text = performance.resolved.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                )
                LegendRow(color = AppTheme.colors.stateSuccess, label = stringResource(R.string.mod_stats_approved), value = performance.approved)
                LegendRow(color = AppTheme.colors.stateError, label = stringResource(R.string.mod_stats_rejected), value = performance.rejected)
                LegendRow(color = AppTheme.colors.stateWarning, label = stringResource(R.string.mod_stats_pending), value = performance.inReview)
            }
        }
    }
}

// Anillo: verde = aprobadas, rojo = rechazadas; en el centro, el porcentaje de aprobación.
@Composable
private fun ApprovalRing(approved: Int, rejected: Int, rate: Int, size: Dp) {
    val track = AppTheme.colors.surfaceTag
    val approvedColor = AppTheme.colors.stateSuccess
    val rejectedColor = AppTheme.colors.stateError
    val total = approved + rejected
    val description = stringResource(R.string.mod_stats_ring_description, rate)
    Box(
        modifier = Modifier
            .size(size)
            .clearAndSetSemantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 12.dp.toPx()
            val inset = stroke / 2
            val arcSize = androidx.compose.ui.geometry.Size(this.size.width - stroke, this.size.height - stroke)
            val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
            drawArc(track, 0f, 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(stroke))
            if (total > 0) {
                val approvedSweep = 360f * approved / total
                // Un pequeño espacio entre los dos tramos para que se distingan.
                val gap = if (approved > 0 && rejected > 0) 4f else 0f
                if (approved > 0) {
                    drawArc(approvedColor, -90f, approvedSweep - gap, useCenter = false, topLeft = topLeft, size = arcSize,
                        style = Stroke(stroke, cap = StrokeCap.Round))
                }
                if (rejected > 0) {
                    drawArc(rejectedColor, -90f + approvedSweep, 360f - approvedSweep - gap, useCenter = false, topLeft = topLeft,
                        size = arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$rate%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
            )
            Text(
                text = stringResource(R.string.mod_stats_approval),
                fontSize = 11.sp,
                color = AppTheme.colors.textPrimary,
            )
        }
    }
}

@Composable
private fun LegendRow(color: Color, label: String, value: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.textPrimary, modifier = Modifier.weight(1f))
        Text(text = value.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
    }
}

private data class Kpi(val label: Int, val value: String, val description: Int, val color: Color)

@Composable
private fun KpiRow(first: Kpi, second: Kpi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        listOf(first, second).forEach { kpi ->
            StatCard(
                label = stringResource(kpi.label),
                value = kpi.value,
                description = stringResource(kpi.description),
                indicatorColor = kpi.color,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }
}

// Barras verticales de los últimos 7 días: el valor arriba y la inicial del día abajo; hoy resaltado.
@Composable
private fun WeeklyBarChart(values: List<Int>) {
    val max = (values.maxOrNull() ?: 0).coerceAtLeast(1)
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val labels = (6 downTo 0).map { daysAgo ->
        dayFormat.format(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }.time)
            .trimEnd('.').replaceFirstChar { it.uppercase() }
    }
    val description = stringResource(R.string.mod_stats_activity_description, values.sum())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        values.forEachIndexed { index, value ->
            val isToday = index == values.lastIndex
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Text(text = value.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                Spacer(Modifier.height(4.dp))
                // Un día sin decisiones deja una base mínima, para que se vea el día.
                val fraction = (value.toFloat() / max).coerceAtLeast(0.04f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((104 * fraction).dp)
                        .background(
                            if (isToday) AppTheme.colors.actionPrimary else AppTheme.colors.brand,
                            RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                        ),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = labels[index],
                    fontSize = 11.sp,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    color = AppTheme.colors.textPrimary,
                    maxLines = 1,
                )
            }
        }
    }
}

// Barra horizontal con etiqueta a la izquierda y valor a la derecha (prioridades y categorías).
@Composable
private fun HorizontalBar(label: String, value: Int, max: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(text = value.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(AppTheme.colors.surfaceTag, RoundedCornerShape(50)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((value.toFloat() / max.coerceAtLeast(1)).coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(50)),
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun ModeratorStatsPreview() {
    PetAdoptionAppTheme {
        ModeratorStatsContent(
            uiState = ModeratorStatsUiState(
                performance = ModeratorPerformance(
                    approved = 18, rejected = 4, inReview = 3, reviewedToday = 2, reviewedLast7Days = 11,
                    dailyReviews = listOf(1, 3, 0, 2, 1, 2, 2),
                    inReviewByPriority = mapOf(PriorityLevel.URGENT to 1, PriorityLevel.HIGH to 1, PriorityLevel.LOW to 1),
                ),
                categories = listOf("Adopción" to 10, "Perdidos" to 7, "Encontrados" to 5),
                isLoading = false,
            ),
            padding = PaddingValues(),
        )
    }
}
