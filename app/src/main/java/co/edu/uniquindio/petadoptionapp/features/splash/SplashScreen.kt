package co.edu.uniquindio.petadoptionapp.features.splash

// Importa las configuraciones y componentes de Jetpack compose
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import co.edu.uniquindio.petadoptionapp.core.component.card.AppCard
import co.edu.uniquindio.petadoptionapp.core.component.text.PawTitle

/**
Pantalla de carga inicial (Splash) de la aplicación.
Muestra el logo, el lema, una imagen principal y una tarjeta sobre adopción responsable
mientras, en paralelo, se recupera la sesión guardada en el teléfono.
No tiene estado de UI propio: usa SplashViewModel.hasSession(), que internamente llama al caso
de uso RestoreSessionUseCase.
Cuando terminan la espera mínima y la recuperación de la sesión, emite el evento onFinished(hasSession)
y AppNavigation decide el destino: el dashboard si hay sesión o el login si no la hay.
**/

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
Mientras se muestra la marca, recupera la sesión guardada; el dibujo vive en [SplashContent].
No decide a dónde navegar: avisa si hay sesión y AppNavigation resuelve el destino.
**/
// Marca la función como un composable que dibuja interfaz.
@Composable
fun SplashScreen(
    // Sin valor por defecto: si se olvida pasarlo, el compilador lo señala.
    onFinished: (hasSession: Boolean) -> Unit,
    // ViewModel inyectado por Hilt que sabe consultar la sesión guardada.
    viewModel: SplashViewModel = hiltViewModel(),
) {
    // Siempre apunta al callback más reciente sin reiniciar la espera.
    val currentOnFinished by rememberUpdatedState(onFinished)

    // Key `Unit`: se ejecuta una sola vez al entrar a la pantalla.
    LaunchedEffect(Unit) {
        // La sesión se recupera mientras corre la espera; se avanza cuando terminan las dos.
        val session = async { viewModel.hasSession() }
        // Espera 2.5 s para que la marca sea visible antes de navegar.
        delay(SPLASH_DURATION_MS)
        // Espera el resultado de la sesión y ejecuta la acción para cambiar de pantalla.
        currentOnFinished(session.await())
    }
    // Dibuja la interfaz de la pantalla
    SplashContent()
}


// Solo dibuja. Separarlo permite que el preview no dispare el temporizador.
@Composable
private fun SplashContent() {
    // Coloca todos los elementos de la pantalla ordenados uno debajo del otro en columna
    Column(
        // Modificadores de la columna principal, aplicados en orden.
        modifier = Modifier
            // Ocupa toda la pantalla del teléfono
            .fillMaxSize()
            // Pinta el fondo del color principal del tema (oscuro/claro)
            .background(AppTheme.colors.background)
            // Evita que el contenido se tape por la barra superior del teléfono
            .systemBarsPadding()
            // Permite hacer scroll vertical si la pantalla es muy pequeña
            .verticalScroll(rememberScrollState())
            // Añade espacio a ambos lados
            .padding(horizontal = ScreenHorizontalPadding),
        // Centra los elementos horizontalmente.
        horizontalAlignment = Alignment.CenterHorizontally,
        // Centra los elementos verticalmente.
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
            // Modificadores de tamaño y forma de la imagen.
            modifier = Modifier
                // Ocupa todo el ancho disponible
                .fillMaxWidth()
                // Mantiene fija la proporción 13:9 (la altura se calcula a partir del ancho)
                .aspectRatio(HERO_ASPECT_RATIO)
                // Redondea las esquinas de la imagen
                .clip(RoundedCornerShape(CardCornerRadius)),
        )

        // Espacio de separación vertical
        Spacer(Modifier.height(40.dp))
        // Muestra la tarjeta informativa mientras se completa la carga
        ResponsibleAdoptionCard()
    }
}

// Logo (huella + nombre) y lema de la app.
@Composable
private fun BrandHeader() {
    // Columna interna para agrupar el logo arriba y el lema abajo
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Huella al lado del nombre de la app
        PawTitle(
            // Nombre de la aplicación tomado de strings.xml.
            text = stringResource(R.string.app_name_pantallas),
            // Tipografía grande para el título de la marca.
            style = MaterialTheme.typography.headlineLarge,
        )
        // Espacio vertical de 12 dp entre el título y el lema
        Spacer(Modifier.height(12.dp))

        // Texto con el lema de la aplicación.
        Text(
            // Muestra el lema o frase de la app
            text = stringResource(R.string.splash_tagline),
            // Aplica el estilo tipográfico de cuerpo mediano (Rubik Medium, 14 sp).
            style = MaterialTheme.typography.bodyMedium,
            // Pinta el texto con el color principal del tema
            color = AppTheme.colors.textPrimary,
        )
    }
}

// Tarjeta informativa sobre adopción responsable.
@Composable
private fun ResponsibleAdoptionCard() {
    // Tarjeta sin borde, con el fondo de las etiquetas
    AppCard(
        // Centra horizontalmente el contenido de la tarjeta.
        horizontalAlignment = Alignment.CenterHorizontally,
        // Usa el color de fondo de las etiquetas del tema.
        containerColor = AppTheme.colors.surfaceTag,
        // null: la tarjeta se dibuja sin borde.
        borderColor = null,
    ) {
        // Título de la tarjeta: huella pequeña sobre `surface` (blanco) + texto.
        // El string se guarda normal y se pasa a mayúsculas aquí, para que TalkBack lo lea como una frase.
        PawTitle(
            // Título del aviso convertido a mayúsculas.
            text = stringResource(R.string.splash_notice_title).uppercase(),
            // Tipografía pequeña de título.
            style = MaterialTheme.typography.titleSmall,
            // Tamaño del círculo de la huella.
            pawSize = 28.dp,
            // Color de fondo del círculo de la huella.
            pawContainerColor = AppTheme.colors.surface,
            // Separación entre la huella y el texto.
            spacing = 8.dp,
        )

        // Espacio vertical de 10 dp entre el título y el mensaje.
        Spacer(Modifier.height(10.dp))

        // Muestra el mensaje de adopción responsable
        Text(
            // Texto del aviso tomado de strings.xml.
            text = stringResource(R.string.splash_notice_message),
            // Estilo de cuerpo mediano.
            style = MaterialTheme.typography.bodyMedium,
            // Centra el texto dentro de la tarjeta.
            textAlign = TextAlign.Center,
            // Color principal del texto según el tema.
            color = AppTheme.colors.textPrimary,
        )
    }
}



// Configura una vista previa (Preview) para el modo claro en Android Studio
@Preview(name = "Claro", showBackground = true)
@Composable
private fun SplashContentPreview() {
    // Aplica el tema de la app para que la vista previa use sus colores y tipografías.
    PetAdoptionAppTheme {
        // Dibuja solo el contenido, sin ViewModel ni temporizador.
        SplashContent()
    }
}
