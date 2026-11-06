package co.edu.uniquindio.petadoptionapp.features.moderator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HideImage
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.button.SecondaryButton
import co.edu.uniquindio.petadoptionapp.core.component.card.SectionCard
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.domain.model.post.AiReview
import co.edu.uniquindio.petadoptionapp.domain.model.post.RelevanceLevel

/**
Vistas del análisis con IA para moderadores:
- AiReviewSummary: versión corta para las tarjetas de la cola (resumen, relevancia y alertas).
- AiReviewCard: versión completa para el detalle (puntos clave, inconsistencias, fotos, contenido,
  duplicado y acciones recomendadas), con "Analizar" / "Volver a analizar".
Son composables sin estado propio ni ViewModel: reciben el AiReview (o null si aún no existe) y
callbacks. La acción de analizar (que usa AnalyzePostUseCase) y la apertura de la publicación duplicada
las resuelve la pantalla que las contiene (inicio del moderador o detalle de la publicación).
**/

/** Resumen corto: relevancia, alertas y 2 líneas del resumen; sin análisis, lo dice. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiReviewSummary(review: AiReview?, modifier: Modifier = Modifier) {
    // Columna con los elementos del resumen separados entre sí.
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Si no hay análisis, se muestra solo un chip que lo indica.
        if (review == null) {
            // Chip gris de "sin analizar".
            AiChip(stringResource(R.string.ai_not_analyzed), Icons.Outlined.AutoAwesome, AppTheme.colors.stateDisabled)
            // Termina la columna sin dibujar nada más.
            return@Column
        }
        // Fila que se ajusta en varias líneas con los chips de relevancia y alertas.
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Chip de relevancia con el color de su nivel.
            AiChip(relevanceText(review), Icons.Outlined.AutoAwesome, relevanceColor(review.relevanceLevel))
            // Chips de alertas detectadas por la IA.
            AiWarningChips(review)
        }
        // Solo muestra el resumen si tiene texto.
        if (review.summary.isNotBlank()) {
            // Resumen de la IA recortado a dos líneas.
            Text(
                // Texto del resumen.
                text = review.summary,
                // Estilo pequeño.
                style = MaterialTheme.typography.bodySmall,
                // Color principal.
                color = AppTheme.colors.textPrimary,
                // Máximo dos líneas.
                maxLines = 2,
                // Puntos suspensivos si se corta.
                overflow = TextOverflow.Ellipsis,
            )
        }
        // Si hay acciones recomendadas, muestra la primera como "siguiente acción".
        review.recommendedActions.firstOrNull()?.let { action ->
            // Texto de la siguiente acción sugerida.
            Text(
                // Texto formateado con la acción.
                text = stringResource(R.string.ai_next_action, action),
                // Estilo de etiqueta pequeña.
                style = MaterialTheme.typography.labelSmall,
                // Color principal.
                color = AppTheme.colors.textPrimary,
                // Una sola línea.
                maxLines = 1,
                // Puntos suspensivos si se corta.
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Tarjeta completa del detalle (solo moderadores). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiReviewCard(
    // Análisis de la IA o null si la publicación aún no se analizó.
    review: AiReview?,
    // Indica si hay un análisis en curso (muestra el indicador en vez del botón).
    isAnalyzing: Boolean,
    // Acción para pedir (o repetir) el análisis.
    onAnalyze: () -> Unit,
    // Acción para abrir otra publicación (la posible duplicada).
    onOpenPost: (postId: String) -> Unit,
    // Modificador opcional del llamador.
    modifier: Modifier = Modifier,
) {
    // Tarjeta de sección con título, subtítulo e ícono de IA.
    SectionCard(
        // Título de la tarjeta.
        title = stringResource(R.string.ai_review_title),
        // Subtítulo explicativo.
        subtitle = stringResource(R.string.ai_review_subtitle),
        // Ícono de IA.
        icon = Icons.Outlined.AutoAwesome,
        // Modificador recibido.
        modifier = modifier,
    ) {
        // Sin análisis: solo un texto explicativo.
        if (review == null) {
            // Texto que indica que la publicación no se ha analizado.
            Text(stringResource(R.string.ai_not_analyzed_long), style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.textPrimary)
        } else {
            // Chips de relevancia y alertas.
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Chip de relevancia.
                AiChip(relevanceText(review), Icons.Outlined.AutoAwesome, relevanceColor(review.relevanceLevel))
                // Chips de alertas.
                AiWarningChips(review)
            }
            // Resumen completo si existe.
            if (review.summary.isNotBlank()) {
                // Espacio antes del resumen.
                Spacer(Modifier.height(10.dp))
                // Texto del resumen sin recortar.
                Text(review.summary, style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.textPrimary)
            }
            // Lista de puntos clave.
            AiList(stringResource(R.string.ai_key_points), review.keyPoints)
            // Lista de inconsistencias en color de advertencia.
            AiList(stringResource(R.string.ai_inconsistencies), review.inconsistencies, AppTheme.colors.stateWarning)

            // Fotos: si corresponden al animal descrito.
            review.imagesRelevant?.let { relevant ->
                // Sección resaltada con el veredicto sobre las fotos.
                AiSection(
                    // Título según si las fotos son relevantes o no.
                    title = stringResource(if (relevant) R.string.ai_images_ok else R.string.ai_images_not_relevant),
                    // Notas de la IA sobre las fotos.
                    text = review.imageNotes,
                    // Verde si son relevantes; rojo si no.
                    color = if (relevant) AppTheme.colors.stateSuccess else AppTheme.colors.stateError,
                )
            }
            // Si la IA detectó contenido inapropiado, lo muestra.
            if (review.inappropriate) {
                // Sección de contenido inapropiado.
                AiSection(
                    // Título de la sección.
                    title = stringResource(R.string.ai_inappropriate),
                    // Motivos unidos con un separador.
                    text = review.inappropriateReasons.joinToString(" · "),
                    // Color de error.
                    color = AppTheme.colors.stateError,
                )
            }
            // Si la IA sospecha que es un duplicado, lo muestra con un enlace.
            review.duplicateOfPostId?.let { duplicateId ->
                // Sección del posible duplicado con su motivo.
                AiSection(title = stringResource(R.string.ai_duplicate), text = review.duplicateReason, color = AppTheme.colors.stateWarning)
                // Botón de texto para abrir la publicación original.
                TextButton(onClick = { onOpenPost(duplicateId) }) {
                    // Texto del enlace.
                    Text(stringResource(R.string.ai_open_duplicate), color = AppTheme.colors.textLink)
                }
            }
            // Lista de acciones recomendadas con ícono de tarea.
            AiList(stringResource(R.string.ai_recommended_actions), review.recommendedActions, AppTheme.colors.stateSuccess, Icons.Outlined.TaskAlt)

            // Espacio antes del pie.
            Spacer(Modifier.height(8.dp))
            // Línea divisoria.
            HorizontalDivider(color = AppTheme.colors.surfaceTag)
            // Pie con el modelo de IA usado.
            Text(
                // Texto "Analizado con <modelo>".
                text = stringResource(R.string.ai_analyzed_with, review.model),
                // Estilo de etiqueta pequeña.
                style = MaterialTheme.typography.labelSmall,
                // Color principal atenuado.
                color = AppTheme.colors.textPrimary.copy(alpha = 0.7f),
                // Margen superior.
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        // Espacio antes del botón o indicador.
        Spacer(Modifier.height(10.dp))
        // Mientras analiza, muestra progreso; si no, el botón.
        if (isAnalyzing) {
            // Fila con indicador y texto.
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Indicador circular pequeño.
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = AppTheme.colors.textPrimary)
                // Espacio entre indicador y texto.
                Spacer(Modifier.width(8.dp))
                // Texto "Analizando...".
                Text(stringResource(R.string.ai_analyzing), style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.textPrimary)
            }
        } else {
            // Botón para analizar o volver a analizar.
            SecondaryButton(
                // Texto según si ya existe análisis.
                text = stringResource(if (review == null) R.string.ai_analyze else R.string.ai_reanalyze),
                // Ícono de IA.
                icon = Icons.Outlined.AutoAwesome,
                // Acción al pulsarlo.
                onClick = onAnalyze,
            )
        }
    }
}

// Alertas: contenido inapropiado, duplicado, fotos que no corresponden, inconsistencias.
@Composable
private fun AiWarningChips(review: AiReview) {
    // Chip rojo si el contenido es inapropiado.
    if (review.inappropriate) AiChip(stringResource(R.string.ai_chip_inappropriate), Icons.Outlined.Report, AppTheme.colors.stateError)
    // Chip de advertencia si parece duplicada.
    if (review.duplicateOfPostId != null) AiChip(stringResource(R.string.ai_chip_duplicate), Icons.Outlined.ContentCopy, AppTheme.colors.stateWarning)
    // Chip rojo si las fotos no corresponden.
    if (review.imagesRelevant == false) AiChip(stringResource(R.string.ai_chip_images), Icons.Outlined.HideImage, AppTheme.colors.stateError)
    // Chip con el número de inconsistencias, si las hay.
    if (review.inconsistencies.isNotEmpty()) {
        // Chip de advertencia con texto en plural.
        AiChip(
            // Texto con la cantidad de inconsistencias.
            pluralStringResource(R.plurals.ai_chip_inconsistencies, review.inconsistencies.size, review.inconsistencies.size),
            // Ícono de error.
            Icons.Outlined.ErrorOutline,
            // Color de advertencia.
            AppTheme.colors.stateWarning,
        )
    }
}

// Chip pequeño con ícono y texto sobre un fondo translúcido del color dado.
@Composable
private fun AiChip(text: String, icon: ImageVector, color: Color) {
    // Fila contenedora del chip.
    Row(
        // Fondo redondeado translúcido y relleno interno.
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        // Centra verticalmente ícono y texto.
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Ícono decorativo teñido con el color del chip.
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
        // Espacio entre ícono y texto.
        Spacer(Modifier.width(4.dp))
        // Texto del chip en negrita y una sola línea.
        Text(text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary, maxLines = 1)
    }
}

// Lista con título y viñetas (o ícono); no dibuja nada si está vacía.
@Composable
private fun AiList(title: String, items: List<String>, color: Color = AppTheme.colors.textPrimary, icon: ImageVector? = null) {
    // Si no hay elementos, no se muestra la sección.
    if (items.isEmpty()) return
    // Columna con margen superior y separación entre elementos.
    Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Título de la lista.
        Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
        // Recorre cada elemento de la lista.
        items.forEach { item ->
            // Fila con la viñeta y el texto alineados arriba.
            Row(verticalAlignment = Alignment.Top) {
                // Si hay ícono se usa como viñeta; si no (rama else), se usa un punto de texto.
                if (icon != null) Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp).padding(top = 2.dp))
                else Text("•", color = color, style = MaterialTheme.typography.bodySmall)
                // Espacio entre viñeta y texto.
                Spacer(Modifier.width(6.dp))
                // Texto del elemento.
                Text(item, style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.textPrimary)
            }
        }
    }
}

// Bloque resaltado con título y texto opcional sobre un fondo del color dado.
@Composable
private fun AiSection(title: String, text: String, color: Color) {
    // Columna contenedora del bloque.
    Column(
        // Margen superior, ancho completo, fondo translúcido redondeado y relleno.
        modifier = Modifier
            .padding(top = 10.dp)
            .fillMaxWidth()
            .background(color.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
            .padding(10.dp),
        // Separación entre título y texto.
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        // Título del bloque.
        Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
        // Texto del bloque, solo si tiene contenido.
        if (text.isNotBlank()) Text(text, style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.textPrimary)
    }
}

// Texto localizado de la relevancia con su porcentaje.
@Composable
private fun relevanceText(review: AiReview): String = stringResource(
    // Elige el recurso según el nivel de relevancia.
    when (review.relevanceLevel) {
        // Relevancia alta.
        RelevanceLevel.HIGH -> R.string.ai_relevance_high
        // Relevancia media.
        RelevanceLevel.MEDIUM -> R.string.ai_relevance_medium
        // Relevancia baja.
        RelevanceLevel.LOW -> R.string.ai_relevance_low
    },
    // Valor numérico de relevancia que se inserta en el texto.
    review.relevance,
)

// Color asociado a cada nivel de relevancia.
@Composable
private fun relevanceColor(level: RelevanceLevel): Color = when (level) {
    // Alta: color de éxito.
    RelevanceLevel.HIGH -> AppTheme.colors.stateSuccess
    // Media: color de advertencia.
    RelevanceLevel.MEDIUM -> AppTheme.colors.stateWarning
    // Baja: color deshabilitado.
    RelevanceLevel.LOW -> AppTheme.colors.stateDisabled
}
