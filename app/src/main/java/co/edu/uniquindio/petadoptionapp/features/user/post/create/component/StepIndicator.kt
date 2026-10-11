package co.edu.uniquindio.petadoptionapp.features.user.post.create.component

// Importación general de materiales y componentes visuales (androidx)
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Importación de los temas a usar
import co.edu.uniquindio.petadoptionapp.core.theme.BotonDeshabilitado
import co.edu.uniquindio.petadoptionapp.core.theme.FondoMarron
import co.edu.uniquindio.petadoptionapp.core.theme.ItemSeleccionado
import co.edu.uniquindio.petadoptionapp.core.theme.TextoMarron

// Componente visual que muestra las barras indicadoras de progreso y el texto "PASO X DE 4".
@Composable
fun StepIndicator(currentStep: Int, totalSteps: Int = 4) {
    // Organiza verticalmente y centra las barras indicadoras y el texto inferior.
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Fila horizontal que distribuye las líneas de progreso con una separación de 6dp.
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            // Repite la creación de una barra por cada paso total definido (por defecto 4).
            repeat(totalSteps) { index ->
                // Calcula el número de paso actual evaluado (índice base 1).
                val step = index + 1
                // Asigna dinámicamente un color según el estado en el que se encuentre el paso.
                val color = when {
                    // Si el paso ya fue superado, muestra el color marrón de completado.
                    step < currentStep -> FondoMarron
                    // Si es el paso actual en el que se encuentra el usuario, muestra el color de selección.
                    step == currentStep -> ItemSeleccionado
                    // Si aún no se ha alcanzado, muestra el color de inactivo/deshabilitado.
                    else -> BotonDeshabilitado
                }
                // Dibuja la barra rectangular con ancho de 32dp, alto de 5dp y esquinas redondeadas.
                Box(modifier = Modifier.width(32.dp).height(5.dp).background(color, RoundedCornerShape(50)))
            }
        }
        // Inserta un pequeño espacio vertical de 4dp entre las barras y el texto.
        Spacer(modifier = Modifier.height(4.dp))
        // Muestra el texto descriptivo indicando el paso actual y el total (ej: "PASO 1 DE 4").
        Text("PASO $currentStep DE $totalSteps", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextoMarron)
    }
}