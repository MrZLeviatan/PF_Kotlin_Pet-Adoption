package co.edu.uniquindio.petadoptionapp.core.component.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Tarjeta blanca que agrupa un paso de un formulario por pasos (por ejemplo al crear una publicación),
con el título centrado arriba ("Información de la Mascota") y los campos debajo.
El título se marca como encabezado para que TalkBack permita saltar entre secciones del formulario.
**/
@Composable
fun FormCard(
    // Título del paso o sección del formulario.
    title: String,
    // Modificador opcional para personalizar la tarjeta desde afuera.
    modifier: Modifier = Modifier,
    // Campos del formulario que se dibujan bajo el título.
    content: @Composable ColumnScope.() -> Unit,
) {
    // Tarjeta de Material 3 que sirve como contenedor del formulario.
    Card(
        // Ocupa todo el ancho disponible.
        modifier = modifier.fillMaxWidth(),
        // Esquinas redondeadas de 24 dp, iguales a las de AppCard.
        shape = RoundedCornerShape(24.dp),
        // Fondo con el color de superficie del tema.
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
    ) {
        // Columna interna con relleno de 20 dp para el título y los campos.
        Column(modifier = Modifier.padding(20.dp)) {
            // Título del formulario.
            Text(
                text = title,
                // Ocupa el ancho completo (para poder centrarse) y se marca como encabezado accesible.
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { heading() },
                // Centra el texto horizontalmente.
                textAlign = TextAlign.Center,
                // Tamaño de 18 sp para destacarlo sobre los campos.
                fontSize = 18.sp,
                // Peso extra negrita para reforzar la jerarquía visual.
                fontWeight = FontWeight.ExtraBold,
                // Color de texto principal del tema.
                color = AppTheme.colors.textPrimary,
            )
            // Separación vertical entre el título y el contenido.
            Spacer(modifier = Modifier.height(16.dp))
            // Dibuja los campos proporcionados por la pantalla.
            content()
        }
    }
}

// Vista previa en Android Studio de una tarjeta de formulario con contenido de ejemplo.
@Preview(showBackground = true)
@Composable
private fun FormCardPreview() {
    // Envuelve el componente en el tema para que use los colores y tipografías reales.
    PetAdoptionAppTheme {
        // Tarjeta de ejemplo con un título típico del formulario de publicación.
        FormCard(title = "Información de la Mascota") {
            // Texto de relleno que simula los campos.
            Text("Contenido", color = AppTheme.colors.textPrimary)
        }
    }
}
