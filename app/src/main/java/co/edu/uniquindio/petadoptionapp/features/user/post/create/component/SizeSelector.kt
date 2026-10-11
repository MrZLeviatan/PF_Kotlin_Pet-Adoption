package co.edu.uniquindio.petadoptionapp.features.user.post.create.component

// Importación general de materiales y componentes visuales (androidx)
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Importa la función auxiliar para renderizar textos obligatorios con asterisco rojo.
import co.edu.uniquindio.petadoptionapp.core.component.requiredText
// Importación de los temas a usar
import co.edu.uniquindio.petadoptionapp.core.theme.BordeMarron
import co.edu.uniquindio.petadoptionapp.core.theme.ChipSeleccionado
import co.edu.uniquindio.petadoptionapp.core.theme.FondoClaro
import co.edu.uniquindio.petadoptionapp.core.theme.TextoMarron

// Componente selector de tamaño para la mascota: Pequeño / Mediano / Grande.
@Composable
fun SizeSelector(selected: String, onSelect: (String) -> Unit) {
    Column {
        // Muestra la etiqueta del selector indicando que es un campo obligatorio con asterisco rojo.
        Text(requiredText("Tamaño Aproximado"), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextoMarron)
        // Agrega un espacio vertical de separación de 6dp debajo de la etiqueta.
        Spacer(modifier = Modifier.height(6.dp))
        // Fila horizontal que organiza los botones de opción separados por 8dp entre sí.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Recorre cada una de las opciones posibles de tamaño.
            listOf("Pequeño", "Mediano", "Grande").forEach { option ->
                Surface(
                    // Al hacer clic en la opción, notifica al componente padre cuál se seleccionó.
                    onClick = { onSelect(option) },
                    // Da una forma de esquinas redondeadas con radio de 10dp a cada opción.
                    shape = RoundedCornerShape(10.dp),
                    // Cambia dinámicamente el color de fondo: si está seleccionada usa el color chip, si no el fondo claro.
                    color = if (option == selected) ChipSeleccionado else FondoClaro,
                    // Dibuja un borde marrón de 1dp alrededor de cada opción de tamaño.
                    border = BorderStroke(1.dp, BordeMarron)
                ) {
                    // Texto interior que muestra el nombre de la opción de tamaño.
                    Text(
                        text = option,
                        // Agrega márgenes internos a los lados y arriba/abajo para darle aspecto de botón táctil.
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        // Define el tamaño de letra en 13sp.
                        fontSize = 13.sp,
                        // Asigna el color de texto marrón característico.
                        color = TextoMarron
                    )
                }
            }
        }
    }
}