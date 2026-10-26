package co.edu.uniquindio.petadoptionapp.core.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import java.text.NumberFormat
import kotlin.math.roundToInt

/** Tarjeta de nivel: nombre, insignia "LVL", puntos y barra de progreso hacia el siguiente nivel. */
@Composable
fun LevelProgressCard(
    levelName: String,
    levelNumber: Int,
    points: Int,
    nextLevelPoints: Int,
    modifier: Modifier = Modifier,
) {
    // coerceIn evita valores fuera de rango si los puntos superan la meta.
    val progress = if (nextLevelPoints == 0) 0f
    else (points.toFloat() / nextLevelPoints).coerceIn(0f, 1f)

    // Formato según el idioma del teléfono (3.240 en Colombia, 3,240 en inglés).
    val formatter = remember { NumberFormat.getIntegerInstance() }

    AppCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    // Se guarda normal y se pasa a mayúsculas aquí, para que TalkBack lo lea como frase.
                    text = stringResource(R.string.profile_level_label).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppTheme.colors.textPrimary,
                )
                Text(
                    text = levelName,
                    style = MaterialTheme.typography.titleMedium,
                    color = AppTheme.colors.textPrimary,
                )
            }
            LevelBadge(level = levelNumber)
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.profile_points, formatter.format(points)),
            style = MaterialTheme.typography.titleLarge,
            color = AppTheme.colors.textPrimary,
        )

        Spacer(Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = AppTheme.colors.actionPrimary,
            trackColor = AppTheme.colors.surfaceTag,
        )

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.profile_progress, (progress * 100).roundToInt()),
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.colors.textPrimary,
            )
            Text(
                text = stringResource(R.string.profile_points_short, formatter.format(nextLevelPoints)),
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.colors.textPrimary,
            )
        }
    }
}

// Insignia "LVL 5".
@Composable
private fun LevelBadge(level: Int) {
    Row(
        modifier = Modifier
            .background(AppTheme.colors.surfaceTag, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.EmojiEvents,
            contentDescription = null,
            tint = AppTheme.colors.textPrimary,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = stringResource(R.string.profile_level_badge, level),
            style = MaterialTheme.typography.labelMedium,
            color = AppTheme.colors.textPrimary,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LevelProgressCardPreview() {
    PetAdoptionAppTheme {
        LevelProgressCard(
            levelName = "Guardián de Huellas",
            levelNumber = 5,
            points = 3_240,
            nextLevelPoints = 4_500,
        )
    }
}