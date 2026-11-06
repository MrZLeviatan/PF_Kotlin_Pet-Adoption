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

/**
Pantalla de estadísticas del moderador (pestaña Estadísticas de su barra inferior).
Observa ModeratorStatsUiState desde ModeratorStatsViewModel: desempeño (aprobadas, rechazadas, en revisión,
revisadas hoy y en los últimos 7 días, actividad diaria y pendientes por prioridad) y resueltas por categoría.
Evento: refresh() cada vez que la pantalla vuelve a primer plano (por ejemplo, tras aprobar o rechazar).
Casos de uso (vía ViewModel): GetModeratorPerformanceUseCase, ObserveCategoriesUseCase y LoadCatalogUseCase.
Navegación: no navega; se abre desde su pestaña o desde la tarjeta PerformanceSummaryCard del perfil
del moderador, que se define en este archivo y se reutiliza allá.
Los gráficos (anillo de aprobación y barras) se dibujan con Compose, sin librerías externas.
**/

// Margen horizontal de la pantalla.
private val ScreenHorizontalPadding = 16.dp

/**
 Dashboard del moderador (pestaña Estadísticas): cuántas resolvió y con qué tasa de aprobación,
 cuántas tiene pendientes, su actividad de los últimos 7 días, pendientes por prioridad y resueltas por categoría.
 Los gráficos se dibujan con Compose (sin librerías).
 */
@Composable
fun ModeratorStatsScreen(
    // Márgenes que entrega el Scaffold contenedor.
    padding: PaddingValues = PaddingValues(),
    // ViewModel inyectado por Hilt.
    viewModel: ModeratorStatsViewModel = hiltViewModel(),
) {
    // Observa el estado respetando el ciclo de vida.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Al volver (después de aprobar o rechazar) los números se actualizan.
    LifecycleResumeEffect(Unit) {
        // Pide al ViewModel recargar las estadísticas.
        viewModel.refresh()
        // No hay nada que limpiar al pausar o salir.
        onPauseOrDispose { }
    }

    // Dibuja el contenido con el estado actual.
    ModeratorStatsContent(uiState = uiState, padding = padding)
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun ModeratorStatsContent(uiState: ModeratorStatsUiState, padding: PaddingValues) {
    // Alias corto del desempeño para simplificar el código.
    val p = uiState.performance
    // Columna principal desplazable.
    Column(
        // Ocupa todo, pinta el fondo, aplica márgenes del contenedor, permite desplazamiento y añade márgenes propios.
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenHorizontalPadding, vertical = 20.dp),
        // Separación entre secciones.
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Bloque de título y subtítulo.
        Column {
            // Título de la pantalla.
            Text(
                // Texto del título.
                text = stringResource(R.string.mod_stats_title),
                // Estilo de titular pequeño.
                style = MaterialTheme.typography.headlineSmall,
                // Texto en negrita.
                fontWeight = FontWeight.Bold,
                // Color principal.
                color = AppTheme.colors.textPrimary,
                // Marca el texto como encabezado para accesibilidad.
                modifier = Modifier.semantics { heading() },
            )
            // Subtítulo explicativo.
            Text(
                // Texto del subtítulo.
                text = stringResource(R.string.mod_stats_subtitle),
                // Estilo de cuerpo mediano.
                style = MaterialTheme.typography.bodyMedium,
                // Color principal.
                color = AppTheme.colors.textPrimary,
            )
        }

        // Si hubo error, muestra el mensaje y no dibuja el resto.
        if (uiState.hasError) {
            // Mensaje de error.
            StatusMessage(text = stringResource(R.string.mod_stats_error), isError = true, modifier = Modifier.fillMaxWidth())
            // Termina la columna aquí.
            return@Column
        }

        // Tarjeta resumen con el anillo de aprobación.
        PerformanceSummaryCard(performance = p)

        // Indicadores clave en dos filas de tarjetas del mismo alto.
        KpiRow(
            // Pendientes en revisión.
            first = Kpi(R.string.mod_stats_pending, p.inReview.toString(), R.string.mod_stats_pending_desc, AppTheme.colors.stateWarning),
            // Revisadas hoy.
            second = Kpi(R.string.mod_stats_today, p.reviewedToday.toString(), R.string.mod_stats_today_desc, AppTheme.colors.stateSuccess),
        )
        // Segunda fila de indicadores.
        KpiRow(
            // Revisadas en los últimos 7 días.
            first = Kpi(R.string.mod_stats_week, p.reviewedLast7Days.toString(), R.string.mod_stats_week_desc, AppTheme.colors.textLink),
            // Promedio diario de la semana.
            second = Kpi(
                // Etiqueta del promedio.
                R.string.mod_stats_daily_avg,
                // Promedio con un decimal según la configuración regional.
                String.format(Locale.getDefault(), "%.1f", p.reviewedLast7Days / 7f),
                // Descripción del promedio.
                R.string.mod_stats_daily_avg_desc,
                // Color de marca.
                AppTheme.colors.brand,
            ),
        )

        // Sección de actividad semanal.
        SectionCard(title = stringResource(R.string.mod_stats_activity), icon = Icons.Outlined.BarChart) {
            // Gráfico de barras de los últimos 7 días.
            WeeklyBarChart(values = p.dailyReviews)
        }

        // Sección de pendientes por prioridad.
        SectionCard(title = stringResource(R.string.mod_stats_by_priority), icon = Icons.Outlined.PendingActions) {
            // Si no hay pendientes, muestra un mensaje.
            if (p.inReview == 0) {
                // Mensaje de "sin pendientes".
                StatusMessage(text = stringResource(R.string.mod_stats_no_pending))
            } else {
                // Orden fijo de las prioridades (null = sin prioridad al final).
                val order = listOf(PriorityLevel.URGENT, PriorityLevel.HIGH, PriorityLevel.MEDIUM, PriorityLevel.LOW, null)
                // Valor máximo para escalar las barras.
                val max = p.inReviewByPriority.values.maxOrNull() ?: 1
                // Columna con las barras.
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Solo dibuja las prioridades que tienen pendientes.
                    order.filter { (p.inReviewByPriority[it] ?: 0) > 0 }.forEach { priority ->
                        // Barra de la prioridad.
                        HorizontalBar(
                            // Nombre de la prioridad.
                            label = priorityText(priority),
                            // Cantidad pendiente.
                            value = p.inReviewByPriority.getValue(priority),
                            // Máximo para la escala.
                            max = max,
                            // Color de la prioridad.
                            color = priorityColor(priority),
                        )
                    }
                }
            }
        }

        // Sección de resueltas por categoría.
        SectionCard(title = stringResource(R.string.mod_stats_by_category), icon = Icons.Outlined.Category) {
            // Si no hay resueltas, muestra un mensaje.
            if (uiState.categories.isEmpty()) {
                // Mensaje de "sin resueltas".
                StatusMessage(text = stringResource(R.string.mod_stats_no_resolved))
            } else {
                // Valor máximo para escalar las barras.
                val max = uiState.categories.maxOf { it.second }
                // Columna con las barras.
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Una barra por categoría.
                    uiState.categories.forEach { (name, count) ->
                        // Barra con el nombre y la cantidad.
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
    // Tarjeta contenedora (tocable solo si hay onClick).
    AppCard(
        // Modificador recibido.
        modifier = modifier,
        // Relleno interno.
        contentPadding = PaddingValues(20.dp),
        // Acción al tocarla.
        onClick = onClick,
        // Etiqueta accesible de la acción, solo si es tocable.
        onClickLabel = onClick?.let { stringResource(R.string.mod_stats_open) },
    ) {
        // Fila con el anillo y los textos.
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Anillo de aprobación.
            ApprovalRing(
                // Aprobadas.
                approved = performance.approved,
                // Rechazadas.
                rejected = performance.rejected,
                // Porcentaje de aprobación.
                rate = performance.approvalRate,
                // Tamaño del anillo.
                size = 112.dp,
            )
            // Espacio entre anillo y textos.
            Spacer(Modifier.width(20.dp))
            // Columna con el total y la leyenda.
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Etiqueta "RESUELTAS".
                Text(
                    // Texto en mayúsculas.
                    text = stringResource(R.string.mod_stats_resolved).uppercase(),
                    // Estilo de etiqueta pequeña.
                    style = MaterialTheme.typography.labelSmall,
                    // Color principal.
                    color = AppTheme.colors.textPrimary,
                )
                // Total de resueltas.
                Text(
                    // Número como texto.
                    text = performance.resolved.toString(),
                    // Estilo de titular grande.
                    style = MaterialTheme.typography.headlineLarge,
                    // Texto en negrita.
                    fontWeight = FontWeight.Bold,
                    // Color principal.
                    color = AppTheme.colors.textPrimary,
                )
                // Leyenda de aprobadas.
                LegendRow(color = AppTheme.colors.stateSuccess, label = stringResource(R.string.mod_stats_approved), value = performance.approved)
                // Leyenda de rechazadas.
                LegendRow(color = AppTheme.colors.stateError, label = stringResource(R.string.mod_stats_rejected), value = performance.rejected)
                // Leyenda de pendientes.
                LegendRow(color = AppTheme.colors.stateWarning, label = stringResource(R.string.mod_stats_pending), value = performance.inReview)
            }
        }
    }
}

// Anillo: verde = aprobadas, rojo = rechazadas; en el centro, el porcentaje de aprobación.
@Composable
private fun ApprovalRing(approved: Int, rejected: Int, rate: Int, size: Dp) {
    // Color de la pista de fondo.
    val track = AppTheme.colors.surfaceTag
    // Color del tramo de aprobadas.
    val approvedColor = AppTheme.colors.stateSuccess
    // Color del tramo de rechazadas.
    val rejectedColor = AppTheme.colors.stateError
    // Total de decisiones.
    val total = approved + rejected
    // Descripción accesible del anillo.
    val description = stringResource(R.string.mod_stats_ring_description, rate)
    // Caja que superpone el lienzo y el texto central.
    Box(
        // Tamaño fijo y semántica única con la descripción.
        modifier = Modifier
            .size(size)
            .clearAndSetSemantics { contentDescription = description },
        // Centra el contenido.
        contentAlignment = Alignment.Center,
    ) {
        // Lienzo donde se dibujan los arcos.
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Grosor del trazo en píxeles.
            val stroke = 12.dp.toPx()
            // Desplazamiento para que el trazo no se salga del área.
            val inset = stroke / 2
            // Tamaño del arco descontando el grosor.
            val arcSize = androidx.compose.ui.geometry.Size(this.size.width - stroke, this.size.height - stroke)
            // Esquina superior izquierda del arco.
            val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
            // Dibuja la pista completa de fondo.
            drawArc(track, 0f, 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(stroke))
            // Solo dibuja los tramos si hay decisiones.
            if (total > 0) {
                // Ángulo proporcional a las aprobadas.
                val approvedSweep = 360f * approved / total
                // Un pequeño espacio entre los dos tramos para que se distingan.
                val gap = if (approved > 0 && rejected > 0) 4f else 0f
                // Dibuja el tramo de aprobadas desde arriba (-90°).
                if (approved > 0) {
                    // Arco verde con extremos redondeados.
                    drawArc(approvedColor, -90f, approvedSweep - gap, useCenter = false, topLeft = topLeft, size = arcSize,
                        style = Stroke(stroke, cap = StrokeCap.Round))
                }
                // Dibuja el tramo de rechazadas a continuación.
                if (rejected > 0) {
                    // Arco rojo con extremos redondeados.
                    drawArc(rejectedColor, -90f + approvedSweep, 360f - approvedSweep - gap, useCenter = false, topLeft = topLeft,
                        size = arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
                }
            }
        }
        // Textos centrales del anillo.
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Porcentaje de aprobación.
            Text(
                // Texto "N%".
                text = "$rate%",
                // Estilo de título grande.
                style = MaterialTheme.typography.titleLarge,
                // Texto en negrita.
                fontWeight = FontWeight.Bold,
                // Color principal.
                color = AppTheme.colors.textPrimary,
            )
            // Etiqueta "aprobación".
            Text(
                // Texto de la etiqueta.
                text = stringResource(R.string.mod_stats_approval),
                // Tamaño de fuente pequeño.
                fontSize = 11.sp,
                // Color principal.
                color = AppTheme.colors.textPrimary,
            )
        }
    }
}

// Fila de leyenda: punto de color, etiqueta y valor.
@Composable
private fun LegendRow(color: Color, label: String, value: Int) {
    // Fila centrada verticalmente.
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Punto circular de color.
        Box(Modifier.size(10.dp).background(color, CircleShape))
        // Espacio tras el punto.
        Spacer(Modifier.width(8.dp))
        // Etiqueta que ocupa el espacio sobrante.
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.textPrimary, modifier = Modifier.weight(1f))
        // Valor en negrita.
        Text(text = value.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
    }
}

// Datos de un indicador clave: etiqueta, valor, descripción y color.
private data class Kpi(val label: Int, val value: String, val description: Int, val color: Color)

// Fila con dos indicadores clave del mismo ancho y alto.
@Composable
private fun KpiRow(first: Kpi, second: Kpi) {
    // Fila contenedora.
    Row(
        // Ancho completo y alto del indicador más alto.
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        // Separación entre tarjetas.
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Dibuja ambos indicadores con el mismo código.
        listOf(first, second).forEach { kpi ->
            // Tarjeta de estadística.
            StatCard(
                // Etiqueta localizada.
                label = stringResource(kpi.label),
                // Valor ya formateado.
                value = kpi.value,
                // Descripción localizada.
                description = stringResource(kpi.description),
                // Color del indicador.
                indicatorColor = kpi.color,
                // Reparte el ancho y llena el alto de la fila.
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
    // Valor máximo (al menos 1 para no dividir entre cero).
    val max = (values.maxOrNull() ?: 0).coerceAtLeast(1)
    // Formato abreviado del día de la semana.
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    // Etiquetas de los últimos 7 días, de hace 6 días hasta hoy.
    val labels = (6 downTo 0).map { daysAgo ->
        // Formatea la fecha restando los días, quita el punto final y pone mayúscula inicial.
        dayFormat.format(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }.time)
            .trimEnd('.').replaceFirstChar { it.uppercase() }
    }
    // Descripción accesible con el total de la semana.
    val description = stringResource(R.string.mod_stats_activity_description, values.sum())
    // Fila con una columna por día.
    Row(
        // Ancho completo, alto fijo y semántica única.
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clearAndSetSemantics { contentDescription = description },
        // Separación entre barras.
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        // Alinea las barras abajo.
        verticalAlignment = Alignment.Bottom,
    ) {
        // Recorre los valores de cada día.
        values.forEachIndexed { index, value ->
            // El último valor corresponde a hoy.
            val isToday = index == values.lastIndex
            // Columna del día.
            Column(
                // Mismo ancho para todos los días y alto completo.
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                // Centra horizontalmente.
                horizontalAlignment = Alignment.CenterHorizontally,
                // Apila los elementos desde abajo.
                verticalArrangement = Arrangement.Bottom,
            ) {
                // Valor del día sobre la barra.
                Text(text = value.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                // Espacio entre valor y barra.
                Spacer(Modifier.height(4.dp))
                // Un día sin decisiones deja una base mínima, para que se vea el día.
                val fraction = (value.toFloat() / max).coerceAtLeast(0.04f)
                // Barra del día.
                Box(
                    // Ancho completo, alto proporcional y fondo redondeado arriba.
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((104 * fraction).dp)
                        .background(
                            // Hoy usa el color de acción; los demás, el de marca.
                            if (isToday) AppTheme.colors.actionPrimary else AppTheme.colors.brand,
                            // Esquinas superiores redondeadas.
                            RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                        ),
                )
                // Espacio entre barra y etiqueta.
                Spacer(Modifier.height(6.dp))
                // Etiqueta del día.
                Text(
                    // Nombre abreviado del día.
                    text = labels[index],
                    // Tamaño de fuente pequeño.
                    fontSize = 11.sp,
                    // Negrita solo para hoy.
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    // Color principal.
                    color = AppTheme.colors.textPrimary,
                    // Una sola línea.
                    maxLines = 1,
                )
            }
        }
    }
}

// Barra horizontal con etiqueta a la izquierda y valor a la derecha (prioridades y categorías).
@Composable
private fun HorizontalBar(label: String, value: Int, max: Int, color: Color) {
    // Columna con la fila de textos y la barra.
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Fila de etiqueta y valor.
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Etiqueta.
            Text(
                // Texto de la etiqueta.
                text = label,
                // Estilo pequeño.
                style = MaterialTheme.typography.bodySmall,
                // Color principal.
                color = AppTheme.colors.textPrimary,
                // Una sola línea.
                maxLines = 1,
                // Puntos suspensivos si se corta.
                overflow = TextOverflow.Ellipsis,
                // Ocupa el espacio sobrante.
                modifier = Modifier.weight(1f),
            )
            // Valor en negrita.
            Text(text = value.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
        }
        // Pista de fondo de la barra.
        Box(
            // Ancho completo, alto fijo y fondo redondeado.
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(AppTheme.colors.surfaceTag, RoundedCornerShape(50)),
        ) {
            // Relleno de la barra proporcional al valor.
            Box(
                // Ancho proporcional (entre 0 y 1), alto completo y color dado.
                modifier = Modifier
                    .fillMaxWidth((value.toFloat() / max.coerceAtLeast(1)).coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(50)),
            )
        }
    }
}

// Vista previa del dashboard con datos de ejemplo.
@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun ModeratorStatsPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Contenido con un estado simulado.
        ModeratorStatsContent(
            // Estado de ejemplo.
            uiState = ModeratorStatsUiState(
                // Desempeño de ejemplo.
                performance = ModeratorPerformance(
                    // Cifras generales.
                    approved = 18, rejected = 4, inReview = 3, reviewedToday = 2, reviewedLast7Days = 11,
                    // Decisiones de cada uno de los últimos 7 días.
                    dailyReviews = listOf(1, 3, 0, 2, 1, 2, 2),
                    // Pendientes por prioridad.
                    inReviewByPriority = mapOf(PriorityLevel.URGENT to 1, PriorityLevel.HIGH to 1, PriorityLevel.LOW to 1),
                ),
                // Resueltas por categoría.
                categories = listOf("Adopción" to 10, "Perdidos" to 7, "Encontrados" to 5),
                // Ya terminó de cargar.
                isLoading = false,
            ),
            // Sin márgenes extra.
            padding = PaddingValues(),
        )
    }
}
