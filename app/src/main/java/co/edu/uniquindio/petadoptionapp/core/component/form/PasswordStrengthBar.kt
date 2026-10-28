package co.edu.uniquindio.petadoptionapp.core.component.form

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/** Niveles de seguridad. Quién los calcula se decide después, en el ViewModel. */
enum class PasswordStrength { WEAK, MEDIUM, STRONG }

/**
Barra de tres segmentos (rojo, amarillo, verde) con el texto "Seguridad: Media".
Solo dibuja: se llena hasta el nivel recibido y el resto queda en gris.
 */
@Composable
fun PasswordStrengthBar(
    strength: PasswordStrength,
    modifier: Modifier = Modifier,
) {
    val segmentColors = listOf(
        AppTheme.colors.stateError,
        AppTheme.colors.stateWarning,
        AppTheme.colors.stateSuccess,
    )
    val filledSegments = strength.ordinal + 1
    val levelText = stringResource(
        when (strength) {
            PasswordStrength.WEAK -> R.string.common_strength_weak
            PasswordStrength.MEDIUM -> R.string.common_strength_medium
            PasswordStrength.STRONG -> R.string.common_strength_strong
        }
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            segmentColors.forEachIndexed { index, color ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            color = if (index < filledSegments) color
                            else AppTheme.colors.stateDisabled,
                            shape = CircleShape,
                        ),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.common_password_strength, levelText),
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.colors.textPrimary,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PasswordStrengthBarPreview() {
    PetAdoptionAppTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            PasswordStrength.entries.forEach { PasswordStrengthBar(strength = it) }
        }
    }
}