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

/**
Tarjeta de gamificación que muestra el nivel del usuario: nombre del nivel, insignia "LVL",
puntos acumulados y una barra de progreso hacia el siguiente nivel. Se usa en el Perfil y en
la pantalla de métricas. Está construida sobre [AppCard], por lo que hereda su forma, borde y
comportamiento de clic del sistema de diseño.
**/

/**
Tarjeta de nivel: nombre, insignia "LVL", puntos y barra de progreso hacia el siguiente nivel.
Con [nextLevelPoints] en `null` (nivel máximo) muestra "Nivel máximo alcanzado".
[progress] va de 0 a 1; si no se pasa, se calcula como puntos / meta.
Con [onClick] la tarjeta es tocable (en Perfil abre las métricas); sin él es solo informativa.
**/
@Composable
fun LevelProgressCard(
    // Nombre del nivel actual (p. ej. "Guardián de Huellas").
    levelName: String,
    // Número del nivel actual, mostrado en la insignia "LVL".
    levelNumber: Int,
    // Puntos acumulados por el usuario.
    points: Int,
    // Puntos necesarios para el siguiente nivel; `null` si ya está en el nivel máximo.
    nextLevelPoints: Int?,
    // Modificador opcional para personalizar la tarjeta desde afuera.
    modifier: Modifier = Modifier,
    // Avance entre 0 y 1; por defecto se calcula a partir de los puntos y la meta.
    progress: Float = defaultProgress(points, nextLevelPoints),
    // Acción al tocar la tarjeta; `null` si es solo informativa.
    onClick: (() -> Unit)? = null,
    // Etiqueta de la acción que anuncia TalkBack.
    onClickLabel: String? = null,
) {
    // Formato según el idioma del teléfono (3.240 en Colombia, 3,240 en inglés); se recuerda para no recrearlo en cada recomposición.
    val formatter = remember { NumberFormat.getIntegerInstance() }

    // Tarjeta base que aporta forma, borde y comportamiento de clic.
    AppCard(modifier = modifier, onClick = onClick, onClickLabel = onClickLabel) {
        // Fila superior: textos del nivel a la izquierda e insignia a la derecha.
        Row(
            // Ocupa todo el ancho de la tarjeta.
            modifier = Modifier.fillMaxWidth(),
            // Centra verticalmente textos e insignia.
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Columna de textos que ocupa el espacio restante y empuja la insignia a la derecha.
            Column(modifier = Modifier.weight(1f)) {
                // Rótulo "NIVEL" en mayúsculas.
                Text(
                    // Se guarda normal y se pasa a mayúsculas aquí, para que TalkBack lo lea como frase.
                    text = stringResource(R.string.profile_level_label).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppTheme.colors.textPrimary,
                )
                // Nombre del nivel actual en un estilo de título.
                Text(
                    text = levelName,
                    style = MaterialTheme.typography.titleMedium,
                    color = AppTheme.colors.textPrimary,
                )
            }
            // Insignia "LVL n" con el número del nivel.
            LevelBadge(level = levelNumber)
        }

        // Separación vertical entre la cabecera y los puntos.
        Spacer(Modifier.height(12.dp))

        // Total de puntos formateados según el idioma del dispositivo.
        Text(
            text = stringResource(R.string.profile_points, formatter.format(points)),
            style = MaterialTheme.typography.titleLarge,
            color = AppTheme.colors.textPrimary,
        )

        // Separación vertical entre los puntos y la barra.
        Spacer(Modifier.height(8.dp))

        // Barra de progreso hacia el siguiente nivel.
        LinearProgressIndicator(
            // Se pasa como lambda para que solo se relea el valor al dibujar, evitando recomposiciones.
            progress = { progress },
            // Barra a todo el ancho, de 8 dp de alto y con extremos redondeados.
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            // Color de la parte completada.
            color = AppTheme.colors.actionPrimary,
            // Color del fondo (parte pendiente) de la barra.
            trackColor = AppTheme.colors.surfaceTag,
        )

        // Separación vertical entre la barra y los textos inferiores.
        Spacer(Modifier.height(6.dp))

        // Fila inferior: porcentaje a la izquierda y meta a la derecha.
        Row(
            modifier = Modifier.fillMaxWidth(),
            // Empuja cada texto a un extremo de la fila.
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Porcentaje de avance redondeado al entero más cercano.
            Text(
                text = stringResource(R.string.profile_progress, (progress * 100).roundToInt()),
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.colors.textPrimary,
            )
            // Meta del siguiente nivel o, si no existe, el texto de nivel máximo.
            Text(
                // Si hay meta muestra sus puntos formateados; si es null muestra "Nivel máximo alcanzado".
                text = nextLevelPoints
                    ?.let { stringResource(R.string.profile_points_short, formatter.format(it)) }
                    ?: stringResource(R.string.profile_progress_max),
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.colors.textPrimary,
            )
        }
    }
}

// Avance hacia la meta; coerceIn evita valores fuera de rango si los puntos la superan.
private fun defaultProgress(points: Int, nextLevelPoints: Int?): Float = when (nextLevelPoints) {
    // Sin meta (nivel máximo): la barra se muestra completa.
    null -> 1f
    // Meta en cero: se evita la división por cero dejando la barra vacía.
    0 -> 0f
    // Caso normal: proporción de puntos sobre la meta, limitada entre 0 y 1.
    else -> (points.toFloat() / nextLevelPoints).coerceIn(0f, 1f)
}

// Insignia "LVL 5": pequeña píldora con un trofeo y el número de nivel.
@Composable
private fun LevelBadge(level: Int) {
    // Fila con fondo redondeado que agrupa el icono y el texto.
    Row(
        modifier = Modifier
            // Fondo de etiqueta con esquinas de 12 dp.
            .background(AppTheme.colors.surfaceTag, RoundedCornerShape(12.dp))
            // Relleno interno de la insignia.
            .padding(horizontal = 10.dp, vertical = 6.dp),
        // Centra verticalmente icono y texto.
        verticalAlignment = Alignment.CenterVertically,
        // Separación de 4 dp entre icono y texto.
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Trofeo decorativo; el texto "LVL n" ya comunica el nivel.
        Icon(
            imageVector = Icons.Outlined.EmojiEvents,
            contentDescription = null,
            tint = AppTheme.colors.textPrimary,
            modifier = Modifier.size(16.dp),
        )
        // Texto "LVL n" construido desde recursos para permitir traducción.
        Text(
            text = stringResource(R.string.profile_level_badge, level),
            style = MaterialTheme.typography.labelMedium,
            color = AppTheme.colors.textPrimary,
        )
    }
}

// Vista previa en Android Studio de la tarjeta de nivel con datos de ejemplo.
@Preview(showBackground = true)
@Composable
private fun LevelProgressCardPreview() {
    // Envuelve el componente en el tema para que use los colores y tipografías reales.
    PetAdoptionAppTheme {
        // Usuario de ejemplo en nivel 5 con 3.240 de 4.500 puntos.
        LevelProgressCard(
            levelName = "Guardián de Huellas",
            levelNumber = 5,
            points = 3_240,
            nextLevelPoints = 4_500,
        )
    }
}