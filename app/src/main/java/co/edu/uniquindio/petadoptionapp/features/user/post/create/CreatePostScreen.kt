package co.edu.uniquindio.petadoptionapp.features.user.post.create

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.petadoptionapp.core.component.button.PrimaryButton
import co.edu.uniquindio.petadoptionapp.core.component.header.AppTopBar
import co.edu.uniquindio.petadoptionapp.core.component.indicator.StepIndicator
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.FondoCrema
import co.edu.uniquindio.petadoptionapp.core.theme.TextoMarron
import co.edu.uniquindio.petadoptionapp.features.user.post.create.component.PublicationSentDialog
import co.edu.uniquindio.petadoptionapp.features.user.post.create.step.StepInfoContent
import co.edu.uniquindio.petadoptionapp.features.user.post.create.step.StepLocationContent
import co.edu.uniquindio.petadoptionapp.features.user.post.create.step.StepPetContent
import co.edu.uniquindio.petadoptionapp.features.user.post.create.step.StepPhotosContent

// Componente principal de la pantalla para crear una nueva publicación de adopción o reporte.
@Composable
fun CreatePostScreen(
    // Función que se ejecuta al salir de la pantalla (flecha atrás en el paso 1).
    onBack: () -> Unit = {},
    // Función que se ejecuta al tocar "Ver mi Publicación".
    onViewPost: () -> Unit = {},
    // Función que se ejecuta al tocar "Volver al Inicio".
    onGoHome: () -> Unit = {},
    // Instancia del ViewModel asociada que gestiona el estado y la lógica de los pasos.
    viewModel: CreatePostViewModel = viewModel()
) {
    // Observa el estado del ViewModel; cada vez que cambia, la pantalla se redibuja automáticamente.
    val state by viewModel.uiState.collectAsState()

    // Intercepta el botón "atrás" físico del celular: en los pasos 2, 3 y 4 retrocede de paso en vez de salir.
    BackHandler(enabled = state.step > 1) { viewModel.previousStep() }

    // Columna principal que organiza la barra superior, el contenido deslizable y el botón inferior.
    Column(
        modifier = Modifier
            // Ocupa todo el tamaño disponible en la pantalla.
            .fillMaxSize()
            // Aplica el color de fondo crema característico.
            .background(FondoCrema)
            // Respeta los márgenes de las barras del sistema (estado y navegación).
            .systemBarsPadding()
            // Desplaza el contenido hacia arriba cuando aparece el teclado virtual en pantalla.
            .imePadding()
    ) {
        // --- Barra superior: flecha atrás a la izquierda + título centrado ---
        // El padding vertical de 4 dp más los 56 dp de la barra dan el mismo alto de 64 dp.
        AppTopBar(
            title = "Nueva publicación",
            onBack = {
                // Si está más allá del paso 1, retrocede al paso anterior.
                if (state.step > 1) viewModel.previousStep()
                // Si está en el primer paso, limpia el formulario y sale de la pantalla.
                else { viewModel.resetForm(); onBack() }
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            titleStyle = LocalTextStyle.current.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold)
        )

        // --- Contenido con soporte para desplazamiento vertical ---
        Column(
            modifier = Modifier
                // Ocupa todo el espacio restante disponible dejando sitio para el botón inferior.
                .weight(1f)
                // Habilita el desplazamiento vertical en caso de que el contenido exceda la pantalla.
                .verticalScroll(rememberScrollState())
                // Agrega un margen interno horizontal simétrico de 24dp.
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Muestra el indicador visual con las barras de progreso del paso actual.
            StepIndicator(
                currentStep = state.step,
                totalSteps = 4,
                completedColor = AppTheme.colors.brand,
                segmentWidth = 32.dp,
                segmentHeight = 5.dp,
                segmentSpacing = 6.dp,
                labelSpacing = 4.dp,
                labelStyle = LocalTextStyle.current.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
            )

            // Inserta un espacio vertical de 8dp.
            Spacer(modifier = Modifier.height(8.dp))
            // Muestra el subtítulo descriptivo correspondiente al paso en curso.
            Text(
                text = subtitleFor(state.step),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextoMarron
            )

            // Inserta un espacio vertical de 24dp antes del contenido específico.
            Spacer(modifier = Modifier.height(24.dp))

            // Renderiza un contenido de formulario distinto dependiendo del número de paso actual.
            when (state.step) {
                1 -> StepInfoContent(state, viewModel)
                2 -> StepPetContent(state, viewModel)
                3 -> StepPhotosContent(state, viewModel)
                else -> StepLocationContent(state, viewModel)
            }

            // Inserta un espacio vertical de 16dp al final del contenido deslizable.
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- Botón fijo inferior: cambia entre "Continuar" y "Publicar", habilitado solo si el paso es válido ---
        PrimaryButton(
            text = if (state.step == 4) "Publicar" else "Continuar",
            enabled = state.isStepValid,
            onClick = viewModel::onNextClick,
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .height(52.dp),
            textStyle = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold)
        )
    }

    // Diálogo emergente final: se muestra únicamente cuando el ViewModel indica éxito al finalizar el paso 4.
    if (state.showSuccessDialog) {
        PublicationSentDialog(
            onViewPost = { viewModel.resetForm(); onViewPost() },
            onGoHome = { viewModel.resetForm(); onGoHome() }
        )
    }
}

// Función auxiliar privada para retornar el texto del subtítulo según el número de paso.
private fun subtitleFor(step: Int) = when (step) {
    1 -> "Ingresa la información sobre la publicación"
    2, 3 -> "Ingresa la información sobre la mascota"
    else -> "Ingresa la ubicación de la mascota"
}

// Vista previa (Preview) para renderizar y comprobar la interfaz en Android Studio.
@Preview(showBackground = true)
@Composable
fun CreatePostScreenPreview() {
    CreatePostScreen()
}