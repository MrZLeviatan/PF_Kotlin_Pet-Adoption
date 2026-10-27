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
Los pasos ya alcanzados (incluido el actual) se pintan con el color de selección.
No conoce el flujo: recibe el paso actual y el total, así sirve para 2, 4 o más pasos.
 */
@Composable
fun StepIndicator(
    // Paso actual, empezando en 1
    currentStep: Int,
    // Cantidad total de pasos del flujo
    totalSteps: Int,
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
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(segmentSpacing)) {
            repeat(totalSteps) { index ->
                val step = index + 1
                Box(
                    modifier = Modifier
                        .width(segmentWidth)
                        .height(segmentHeight)
                        .background(
                            color = when {
                                step < currentStep -> completedColor
                                step == currentStep -> currentColor
                                else -> pendingColor
                            },
                            shape = CircleShape,
                        ),
                )
            }
        }
        Spacer(Modifier.height(labelSpacing))
        Text(
            // Se guarda normal y se pasa a mayúsculas aquí, para que TalkBack lo lea como frase.
            text = stringResource(R.string.common_step_indicator, currentStep, totalSteps)
                .uppercase(),
            style = labelStyle,
            color = AppTheme.colors.textPrimary,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StepIndicatorPreview() {
    PetAdoptionAppTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            StepIndicator(currentStep = 1, totalSteps = 2)
            StepIndicator(currentStep = 3, totalSteps = 4)
        }
    }
}