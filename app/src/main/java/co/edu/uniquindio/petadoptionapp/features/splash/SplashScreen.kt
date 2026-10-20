package co.edu.uniquindio.petadoptionapp.features.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import co.edu.uniquindio.petadoptionapp.core.theme.FondoChip
import co.edu.uniquindio.petadoptionapp.core.theme.FondoCrema
import co.edu.uniquindio.petadoptionapp.core.theme.TextoMarron
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    // Se ejecuta cuando termina la espera. Lo resuelve AppNavigation (a dónde ir después)
    onFinished: () -> Unit = {}
) {
    // Guarda siempre la versión más reciente de onFinished, sin reiniciar la espera si cambia
    val currentOnFinished by rememberUpdatedState(onFinished)

    // Se ejecuta una sola vez al abrir la pantalla
    LaunchedEffect(Unit) {
        delay(2500)              // Espera 2.5 segundos mostrando la pantalla
        currentOnFinished()      // Avisa que terminó para pasar a la siguiente pantalla
    }

    // Columna que apila todo de arriba hacia abajo, centrado en la pantalla
    Column(
        modifier = Modifier
            .fillMaxSize()                       // Ocupa toda la pantalla
            .background(FondoCrema)              // Fondo crema de la app
            .systemBarsPadding()                 // No se pega a la hora ni a los botones del sistema
            .padding(horizontal = 24.dp),        // Margen a los lados
        horizontalAlignment = Alignment.CenterHorizontally,   // Todo centrado horizontalmente
        verticalArrangement = Arrangement.Center              // Todo centrado verticalmente
    ) {

        // --- Logo: círculo con huellita + nombre de la app ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)    // 10dp entre el círculo y el texto
        ) {
            PawCircle(size = 40)
            Text(
                text = "Huellas",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextoMarron
            )
        }

        Spacer(modifier = Modifier.height(12.dp))   // Espacio vacío de 12dp

        // Lema de la app
        Text(
            text = "Adopta con amor, cambia una vida",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextoMarron
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- Foto principal desde Cloudinary ---
        AsyncImage(
            model = "https://res.cloudinary.com/dehltwwbu/image/upload/v1791692767/hero-image_utvtsx.png",
            contentDescription = "Perro y gato descansando juntos",
            contentScale = ContentScale.Crop,                // Llena el espacio sin deformar
            placeholder = ColorPainter(Color.LightGray),     // Gris mientras carga
            modifier = Modifier
                .fillMaxWidth()                              // Ocupa todo el ancho disponible
                .height(200.dp)
                .clip(RoundedCornerShape(24.dp))             // Esquinas redondeadas
        )

        Spacer(modifier = Modifier.height(40.dp))

        // --- Tarjeta "Adopción responsable" ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(FondoChip, RoundedCornerShape(24.dp))   // Fondo tostado con esquinas redondeadas
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título pequeño con huellita
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PawCircle(size = 28)
                Text(
                    text = "ADOPCIÓN RESPONSABLE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextoMarron
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mensaje de la tarjeta, centrado
            Text(
                text = "Adoptar responsablemente significa brindar amor, compromiso y cuidado durante toda la vida de tu compañero.",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = TextoMarron
            )
        }
    }
}

// Círculo claro con una huellita adentro. Se usa en el logo y en la tarjeta
@Composable
private fun PawCircle(size: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)                               // Tamaño del círculo
            .background(FondoChip, CircleShape),         // Fondo tostado y forma redonda
        contentAlignment = Alignment.Center              // Huellita al centro
    ) {
        Icon(
            imageVector = Icons.Outlined.Pets,           // Ícono de huella
            contentDescription = null,                   // Decorativo
            tint = TextoMarron,
            modifier = Modifier.size((size * 0.55f).dp)  // La huella ocupa un poco más de la mitad
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen()
}