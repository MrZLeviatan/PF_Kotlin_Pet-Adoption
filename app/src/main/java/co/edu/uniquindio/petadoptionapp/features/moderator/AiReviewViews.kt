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

/*
 Vistas del análisis con IA para moderadores:
 - AiReviewSummary: versión corta para las tarjetas de la cola (resumen, relevancia y alertas).
 - AiReviewCard: versión completa para el detalle (puntos clave, inconsistencias, fotos, contenido,
   duplicado y acciones recomendadas), con "Analizar" / "Volver a analizar".
 */

/** Resumen corto: relevancia, alertas y 2 líneas del resumen; sin análisis, lo dice. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiReviewSummary(review: AiReview?, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (review == null) {
            AiChip(stringResource(R.string.ai_not_analyzed), Icons.Outlined.AutoAwesome, AppTheme.colors.stateDisabled)
            return@Column
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            AiChip(relevanceText(review), Icons.Outlined.AutoAwesome, relevanceColor(review.relevanceLevel))
            AiWarningChips(review)
        }
        if (review.summary.isNotBlank()) {
            Text(
                text = review.summary,
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        review.recommendedActions.firstOrNull()?.let { action ->
            Text(
                text = stringResource(R.string.ai_next_action, action),
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Tarjeta completa del detalle (solo moderadores). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiReviewCard(
    review: AiReview?,
    isAnalyzing: Boolean,
    onAnalyze: () -> Unit,
    onOpenPost: (postId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionCard(
        title = stringResource(R.string.ai_review_title),
        subtitle = stringResource(R.string.ai_review_subtitle),
        icon = Icons.Outlined.AutoAwesome,
        modifier = modifier,
    ) {
        if (review == null) {
            Text(stringResource(R.string.ai_not_analyzed_long), style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.textPrimary)
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AiChip(relevanceText(review), Icons.Outlined.AutoAwesome, relevanceColor(review.relevanceLevel))
                AiWarningChips(review)
            }
            if (review.summary.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(review.summary, style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.textPrimary)
            }
            AiList(stringResource(R.string.ai_key_points), review.keyPoints)
            AiList(stringResource(R.string.ai_inconsistencies), review.inconsistencies, AppTheme.colors.stateWarning)

            // Fotos: si corresponden al animal descrito.
            review.imagesRelevant?.let { relevant ->
                AiSection(
                    title = stringResource(if (relevant) R.string.ai_images_ok else R.string.ai_images_not_relevant),
                    text = review.imageNotes,
                    color = if (relevant) AppTheme.colors.stateSuccess else AppTheme.colors.stateError,
                )
            }
            if (review.inappropriate) {
                AiSection(
                    title = stringResource(R.string.ai_inappropriate),
                    text = review.inappropriateReasons.joinToString(" · "),
                    color = AppTheme.colors.stateError,
                )
            }
            review.duplicateOfPostId?.let { duplicateId ->
                AiSection(title = stringResource(R.string.ai_duplicate), text = review.duplicateReason, color = AppTheme.colors.stateWarning)
                TextButton(onClick = { onOpenPost(duplicateId) }) {
                    Text(stringResource(R.string.ai_open_duplicate), color = AppTheme.colors.textLink)
                }
            }
            AiList(stringResource(R.string.ai_recommended_actions), review.recommendedActions, AppTheme.colors.stateSuccess, Icons.Outlined.TaskAlt)

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = AppTheme.colors.surfaceTag)
            Text(
                text = stringResource(R.string.ai_analyzed_with, review.model),
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.colors.textPrimary.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        Spacer(Modifier.height(10.dp))
        if (isAnalyzing) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = AppTheme.colors.textPrimary)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.ai_analyzing), style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.textPrimary)
            }
        } else {
            SecondaryButton(
                text = stringResource(if (review == null) R.string.ai_analyze else R.string.ai_reanalyze),
                icon = Icons.Outlined.AutoAwesome,
                onClick = onAnalyze,
            )
        }
    }
}

// Alertas: contenido inapropiado, duplicado, fotos que no corresponden, inconsistencias.
@Composable
private fun AiWarningChips(review: AiReview) {
    if (review.inappropriate) AiChip(stringResource(R.string.ai_chip_inappropriate), Icons.Outlined.Report, AppTheme.colors.stateError)
    if (review.duplicateOfPostId != null) AiChip(stringResource(R.string.ai_chip_duplicate), Icons.Outlined.ContentCopy, AppTheme.colors.stateWarning)
    if (review.imagesRelevant == false) AiChip(stringResource(R.string.ai_chip_images), Icons.Outlined.HideImage, AppTheme.colors.stateError)
    if (review.inconsistencies.isNotEmpty()) {
        AiChip(
            pluralStringResource(R.plurals.ai_chip_inconsistencies, review.inconsistencies.size, review.inconsistencies.size),
            Icons.Outlined.ErrorOutline,
            AppTheme.colors.stateWarning,
        )
    }
}

@Composable
private fun AiChip(text: String, icon: ImageVector, color: Color) {
    Row(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary, maxLines = 1)
    }
}

@Composable
private fun AiList(title: String, items: List<String>, color: Color = AppTheme.colors.textPrimary, icon: ImageVector? = null) {
    if (items.isEmpty()) return
    Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
        items.forEach { item ->
            Row(verticalAlignment = Alignment.Top) {
                if (icon != null) Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp).padding(top = 2.dp))
                else Text("•", color = color, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(6.dp))
                Text(item, style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.textPrimary)
            }
        }
    }
}

@Composable
private fun AiSection(title: String, text: String, color: Color) {
    Column(
        modifier = Modifier
            .padding(top = 10.dp)
            .fillMaxWidth()
            .background(color.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
        if (text.isNotBlank()) Text(text, style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.textPrimary)
    }
}

@Composable
private fun relevanceText(review: AiReview): String = stringResource(
    when (review.relevanceLevel) {
        RelevanceLevel.HIGH -> R.string.ai_relevance_high
        RelevanceLevel.MEDIUM -> R.string.ai_relevance_medium
        RelevanceLevel.LOW -> R.string.ai_relevance_low
    },
    review.relevance,
)

@Composable
private fun relevanceColor(level: RelevanceLevel): Color = when (level) {
    RelevanceLevel.HIGH -> AppTheme.colors.stateSuccess
    RelevanceLevel.MEDIUM -> AppTheme.colors.stateWarning
    RelevanceLevel.LOW -> AppTheme.colors.stateDisabled
}
