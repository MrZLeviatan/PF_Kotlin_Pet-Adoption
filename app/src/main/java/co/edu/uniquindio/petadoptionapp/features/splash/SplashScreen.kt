package co.edu.uniquindio.petadoptionapp.features.splash

// Importa las configuraciones y componentes de Jetpack compose
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

// Importación de interfaz, diseñó y componentes UI y tools
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// Importación de componentes implementados propios
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import kotlinx.coroutines.delay
import co.edu.uniquindio.petadoptionapp.core.component.icon.PawCircle


// Tiempo que se muestra la marca antes de navegar (Posteriormente se configura con la demora de conexión a los sistemas)
private const val SPLASH_DURATION_MS = 2_500L

// La altura de la imagen se deriva del ancho, para que se vea igual en cualquier pantalla.
private const val HERO_ASPECT_RATIO = 13f / 9f

// Margen horizontal estándar para los lados de la pantalla (24 dp).
private val ScreenHorizontalPadding = 24.dp

// Radio de redondeo estándar para las tarjetas y la imagen principal (24 dp).
private val CardCornerRadius = 24.dp

/**
Pantalla de carga inicial.
Solo contiene la lógica de tiempo; el dibujo vive en [SplashContent].
No decide a dónde navegar: avisa que terminó y AppNavigation resuelve el destino.
 */
@Composable
fun SplashScreen(
    // Sin valor por defecto: si se olvida pasarlo, el compilador lo señala.
    onFinished: () -> Unit,
) {
    // Siempre apunta al callback más reciente sin reiniciar la espera.
    val currentOnFinished by rememberUpdatedState(onFinished)

    // Key `Unit`: se ejecuta una sola vez al entrar a la pantalla.
    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MS)   // Espera 2.5L
        // Ejecuta la acción para cambiar de pantalla
        currentOnFinished()
    }
    // Dibuja la interfaz de la pantalla
    SplashContent()
}


// Solo dibuja. Separarlo permite que el preview no dispare el temporizador.
@Composable
private fun SplashContent() {
    // Coloca todos los elementos de la pantalla ordenados uno debajo del otro en columna
    Column(
        modifier = Modifier
            .fillMaxSize()              // Ocupa toda la pantalla del teléfono
            .background(AppTheme.colors.background)     // Pinta el fondo del color principal del tema (oscuro/claro)
            .systemBarsPadding()        // Evita que el contenido se tape por la barra superior del teléfono
            .verticalScroll(rememberScrollState())      // Permite hacer scroll vertical si la pantalla es muy pequeña
            .padding(horizontal = ScreenHorizontalPadding),    // Añade espacio a ambos lados
        // Centra los elementos horizontal y verticalmente
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Muestra el logo y lemas de la app
        BrandHeader()
        // Espacio de separación vertical de 32 dp
        Spacer(Modifier.height(32.dp))

        // Imagen local: aparece al instante y funciona sin conexión.
        Image(
            // Carga la imagen principal desde los recursos locales
            painter = painterResource(R.drawable.hero_image),
            // Descripción para la imagen (buenas prácticas)
            contentDescription = stringResource(R.string.splash_hero_description),
            // Ajusta la imagen recortándola para que llene su espacio sin deformarse
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()     // Ocupa todo el ancho disponible
                .aspectRatio(HERO_ASPECT_RATIO)     // Mantiene la proporción 16:9 fija
                .clip(RoundedCornerShape(CardCornerRadius)),    // Redondea la imagen
        )

        // Espacio de separación vertical
        Spacer(Modifier.height(40.dp))
        // Muestra la información mientras la carga se lleva
        ResponsibleAdoptionCard()
    }
}

// Logo (huella + nombre) y lema de la app.
@Composable
private fun BrandHeader() {
    // Columna interna para agrupar el logo arriba y el lema abajo
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Fila para poner el icono de la huella al lado del nombre de la app
        Row(
            verticalAlignment = Alignment.CenterVertically,     // Alinea verticalmente el icono y el texto al centro
            horizontalArrangement = Arrangement.spacedBy(10.dp), // Deja un espacio de 10 dp entre el icono y el texto
        ) {
            PawCircle(size = 40.dp, containerColor = AppTheme.colors.surfaceTag) // Dibuja el círculo con la huella (tamaño 40 dp)
            Text(
                text = stringResource(R.string.app_name_pantallas),       // Muestra el nombre de la app obtenido de los recursos
                // Título general
                style = MaterialTheme.typography.headlineLarge, // Aplica el estilo tipográfico de título grande
                color = AppTheme.colors.textPrimary,            // Pinta el texto con el color principal del tema
            )
        }
        // Espacio vertical de 12 dp entre el título y el lema
        Spacer(Modifier.height(12.dp))

        Text(
            // Muestra el lema o frase de la app
            text = stringResource(R.string.splash_tagline),
            // Rubik Medium, 14 sp.
            style = MaterialTheme.typography.bodyMedium,        // Aplica el estilo tipográfico de cuerpo mediano
            color = AppTheme.colors.textPrimary,                // Pinta el texto con el color principal del tema
        )
    }
}

// Tarjeta informativa sobre adopción responsable.
@Composable
private fun ResponsibleAdoptionCard() {
    // Columna que actúa como tarjeta con fondo y esquinas redondeadas
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surfaceTag, RoundedCornerShape(CardCornerRadius))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Fila para el título de la tarjeta (icono de huella pequeño + texto)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // El círculo usa `surface` (blanco)
            PawCircle(size = 28.dp, containerColor = AppTheme.colors.surface)
            Text(
                // El string se guarda normal y se pasa a mayúsculas aquí, para que TalkBack lo lea como una frase.
                text = stringResource(R.string.splash_notice_title).uppercase(),
                style = MaterialTheme.typography.titleSmall,
                color = AppTheme.colors.textPrimary,
            )
        }

        Spacer(Modifier.height(10.dp))

        // Muestra el mensaje de adopción responsable
        Text(
            text = stringResource(R.string.splash_notice_message),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = AppTheme.colors.textPrimary,
        )
    }
}



// Configura una vista previa (Preview) para el modo claro en Android Studio
@Preview(name = "Claro", showBackground = true)
@Composable
private fun SplashContentPreview() {
    PetAdoptionAppTheme {
        SplashContent()
    }
}