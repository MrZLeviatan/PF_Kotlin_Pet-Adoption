package co.edu.uniquindio.petadoptionapp.core.component.indicator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Indicador de progreso por pasos: una barra por paso y el texto "PASO X DE N".
Los pasos anteriores se pintan con el color de completado, el actual con el
color de selección y los que faltan con el color deshabilitado.
No conoce el flujo: recibe el paso actual y el total, así sirve para 2, 4 o más pasos.
Se usa en los asistentes de registro y de creación de publicación.
**/
@Composable
fun StepIndicator(
    // Paso actual, empezando en 1
    currentStep: Int,
    // Cantidad total de pasos del flujo
    totalSteps: Int,
    // Modificador externo para posicionar el indicador.
    modifier: Modifier = Modifier,
    // Pasos ya terminados (anteriores al actual)
    completedColor: Color = AppTheme.colors.stateComplete,
    // Paso en el que está el usuario
    currentColor: Color = AppTheme.colors.stateSelected,
    // Pasos que faltan
    pendingColor: Color = AppTheme.colors.stateDisabled,
    // Tamaño de cada barra y separación entre ellas
    segmentWidth: Dp = 28.dp,
    segmentHeight: Dp = 4.dp,
    segmentSpacing: Dp = 4.dp,
    // Espacio entre las barras y el texto, y estilo del texto
    labelSpacing: Dp = 6.dp,
    labelStyle: TextStyle = MaterialTheme.typography.labelSmall,
) {
    // Columna que apila las barras y el texto, centrados horizontalmente.
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        // Fila de barras separadas por el espaciado indicado.
        Row(horizontalArrangement = Arrangement.spacedBy(segmentSpacing)) {
            // Genera una barra por cada paso del flujo.
            repeat(totalSteps) { index ->
                // Convierte el índice (desde 0) al número de paso (desde 1) para compararlo con currentStep.
                val step = index + 1
                // Barra individual del paso.
                Box(
                    modifier = Modifier
                        // Ancho de la barra.
                        .width(segmentWidth)
                        // Alto (grosor) de la barra.
                        .height(segmentHeight)
                        // Pinta la barra con extremos redondeados según su estado.
                        .background(
                            color = when {
                                // Paso anterior al actual: completado.
                                step < currentStep -> completedColor
                                // Paso actual: resaltado.
                                step == currentStep -> currentColor
                                // Paso futuro: pendiente.
                                else -> pendingColor
                            },
                            shape = CircleShape,
                        ),
                )
            }
        }
        // Separación vertical entre las barras y el texto.
        Spacer(Modifier.height(labelSpacing))
        // Texto "PASO X DE N" debajo de las barras.
        Text(
            // Se guarda normal y se pasa a mayúsculas aquí, para que TalkBack lo lea como frase.
            text = stringResource(R.string.common_step_indicator, currentStep, totalSteps)
                .uppercase(),
            // Estilo recibido (por defecto, etiqueta pequeña).
            style = labelStyle,
            // Color de texto principal del tema.
            color = AppTheme.colors.textPrimary,
        )
    }
}

// Vista previa con dos configuraciones de pasos distintas.
@Preview(showBackground = true)
@Composable
private fun StepIndicatorPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Apila los ejemplos con separación vertical.
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Flujo de 2 pasos en el primero.
            StepIndicator(currentStep = 1, totalSteps = 2)
            // Flujo de 4 pasos en el tercero.
            StepIndicator(currentStep = 3, totalSteps = 4)
        }
    }
}
