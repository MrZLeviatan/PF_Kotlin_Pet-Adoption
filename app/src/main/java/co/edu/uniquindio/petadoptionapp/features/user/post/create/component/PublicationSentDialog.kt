package co.edu.uniquindio.petadoptionapp.features.user.post.create.component

// Importación general de materiales y componentes visuales (androidx)
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import co.edu.uniquindio.petadoptionapp.core.component.button.PrimaryButton
// Importación de los temas a usar
import co.edu.uniquindio.petadoptionapp.core.theme.*

// Componente de diálogo modal que se muestra cuando la publicación ha sido enviada con éxito.
@Composable
fun PublicationSentDialog(
    // Función que se ejecuta al tocar el botón "Ver mi Publicación".
    onViewPost: () -> Unit,
    // Función que se ejecuta al tocar el botón "Volver al Inicio".
    onGoHome: () -> Unit
) {
    Dialog(
        // Evita que el diálogo se cierre al tocar fuera de él.
        onDismissRequest = {},
        // Configura el diálogo para que permita usar casi todo el ancho de la pantalla.
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            // Ocupa todo el ancho disponible aplicando un margen horizontal simétrico de 24dp.
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            // Da esquinas muy redondeadas con un radio de 28dp a la tarjeta del diálogo.
            shape = RoundedCornerShape(28.dp),
            // Define el color de fondo claro para la tarjeta emergente.
            colors = CardDefaults.cardColors(containerColor = FondoClaro)
        ) {
            // Organiza verticalmente y centra todos los elementos internos con un margen de 24dp.
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // Caja circular con fondo de color chip seleccionado para contener el icono de la huella.
                Box(
                    modifier = Modifier.size(56.dp).background(ChipSeleccionado, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Muestra el icono de la huella de mascota con color marrón.
                    Icon(Icons.Filled.Pets, contentDescription = null, tint = TextoMarron)
                }

                // Inserta un espacio vertical de 16dp.
                Spacer(modifier = Modifier.height(16.dp))
                // Muestra el título principal de éxito del diálogo en negrita extra.
                Text("Publicación Enviada", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextoMarron)

                // Inserta un espacio vertical de 12dp.
                Spacer(modifier = Modifier.height(12.dp))
                // Muestra el texto explicativo sobre el proceso de moderación y verificación.
                Text(
                    text = "Tu publicación está pendiente de verificación por nuestro equipo de moderadores. Recibirás una notificación cuando sea aprobada o si necesita ajustes.",
                    // Centra el texto alineado horizontalmente.
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    color = TextoMarron
                )

                // Inserta un espacio vertical de 16dp.
                Spacer(modifier = Modifier.height(16.dp))
                // Superficie contenedora redondeada que resalta el tiempo estimado de espera.
                Surface(shape = RoundedCornerShape(50), color = FondoAviso) {
                    Text(
                        text = "Tiempo estimado: 24-48 horas",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        color = TextoMarron
                    )
                }

                // Inserta un espacio vertical de 24dp antes de los botones de acción.
                Spacer(modifier = Modifier.height(24.dp))
                // Botón principal interactivo para redirigir a ver la publicación enviada.
                PrimaryButton(
                    text = "Ver mi Publicación",
                    onClick = onViewPost,
                    modifier = Modifier.height(52.dp),
                    textStyle = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )

                // Inserta un espacio vertical de 12dp entre ambos botones.
                Spacer(modifier = Modifier.height(12.dp))
                // Botón secundario para regresar a la pantalla principal de la aplicación.
                Button(
                    onClick = onGoHome,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    // Aplica esquinas completamente redondeadas tipo píldora.
                    shape = RoundedCornerShape(50),
                    // Define los colores personalizados del botón con fondo naranja y texto blanco.
                    colors = ButtonDefaults.buttonColors(containerColor = NaranjaBoton, contentColor = Color.White)
                ) {
                    // Texto del botón secundario en negrita.
                    Text("Volver al Inicio", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}