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
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.AuthValidator

/**
 Indicador visual de la seguridad de una contraseña. Incluye el modelo de niveles
 (PasswordStrength), la función que lo calcula con las mismas reglas del dominio
 (AuthValidator) y la barra que lo dibuja. Se usa en el registro y en el cambio
 de contraseña de ajustes; los colores salen de stateError, stateWarning y
 stateSuccess del tema.
**/

/** Niveles de seguridad, de menor a mayor; su orden (ordinal) define cuántos segmentos se llenan. */
enum class PasswordStrength { WEAK, MEDIUM, STRONG }

// Largo desde el que una contraseña con símbolo cuenta como "Alta".
private const val STRONG_PASSWORD_LENGTH = 12

/**
 Fuerza de una contraseña, con la misma regla que los casos de uso (AuthValidator).
 Baja = la rechazarían. Media = cumple la regla. Alta = además es larga y con símbolo. Vacía = null (sin barra).
**/
fun passwordStrengthOf(password: String): PasswordStrength? = when {
    // Sin texto no se muestra la barra.
    password.isEmpty() -> null
    // Si no cumple la regla del dominio, es débil.
    !AuthValidator.isPasswordValid(password) -> PasswordStrength.WEAK
    // Si además es larga y contiene al menos un símbolo, es fuerte.
    password.length >= STRONG_PASSWORD_LENGTH && password.any { !it.isLetterOrDigit() } -> PasswordStrength.STRONG
    // En otro caso cumple la regla mínima: media.
    else -> PasswordStrength.MEDIUM
}

/**
Barra de tres segmentos (rojo, amarillo, verde) con el texto "Seguridad: Media".
Solo dibuja: se llena hasta el nivel recibido y el resto queda en gris.
**/
@Composable
fun PasswordStrengthBar(
    // Nivel de seguridad a representar.
    strength: PasswordStrength,
    // Modificador externo para posicionar la barra.
    modifier: Modifier = Modifier,
) {
    // Color de cada segmento en orden: débil, media, fuerte.
    val segmentColors = listOf(
        AppTheme.colors.stateError,
        AppTheme.colors.stateWarning,
        AppTheme.colors.stateSuccess,
    )
    // Cantidad de segmentos a colorear: 1 para débil, 2 para media y 3 para fuerte.
    val filledSegments = strength.ordinal + 1
    // Texto del nivel ("Baja", "Media", "Alta") según el recurso correspondiente.
    val levelText = stringResource(
        when (strength) {
            PasswordStrength.WEAK -> R.string.common_strength_weak
            PasswordStrength.MEDIUM -> R.string.common_strength_medium
            PasswordStrength.STRONG -> R.string.common_strength_strong
        }
    )

    // Columna que apila los segmentos y el texto.
    Column(modifier = modifier.fillMaxWidth()) {
        // Fila de segmentos que ocupa todo el ancho.
        Row(
            modifier = Modifier.fillMaxWidth(),
            // Separación entre segmentos.
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // Dibuja un segmento por cada color definido.
            segmentColors.forEachIndexed { index, color ->
                // Segmento individual.
                Box(
                    modifier = Modifier
                        // Cada segmento ocupa una parte igual del ancho.
                        .weight(1f)
                        // Grosor de la barra.
                        .height(4.dp)
                        // Pinta con su color si está dentro del nivel alcanzado o en gris si no.
                        .background(
                            color = if (index < filledSegments) color
                            else AppTheme.colors.stateDisabled,
                            shape = CircleShape,
                        ),
                )
            }
        }
        // Separación entre la barra y el texto.
        Spacer(Modifier.height(4.dp))
        // Texto "Seguridad: <nivel>".
        Text(
            text = stringResource(R.string.common_password_strength, levelText),
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.colors.textPrimary,
        )
    }
}

// Vista previa de los tres niveles de seguridad.
@Preview(showBackground = true)
@Composable
private fun PasswordStrengthBarPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Apila una barra por nivel.
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Recorre todos los niveles del enum y dibuja su barra.
            PasswordStrength.entries.forEach { PasswordStrengthBar(strength = it) }
        }
    }
}
