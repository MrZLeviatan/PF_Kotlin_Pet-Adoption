package co.edu.uniquindio.petadoptionapp.features.moderator.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.button.MenuActionButton
import co.edu.uniquindio.petadoptionapp.core.component.card.SectionCard
import co.edu.uniquindio.petadoptionapp.core.component.card.UserSummaryCard
import co.edu.uniquindio.petadoptionapp.core.component.text.InfoRow
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import co.edu.uniquindio.petadoptionapp.domain.model.post.ModeratorPerformance
import co.edu.uniquindio.petadoptionapp.features.moderator.stats.PerformanceSummaryCard

/**
Pantalla de perfil del moderador (pestaña Perfil de su barra inferior).
Observa ModeratorProfileUiState desde ModeratorProfileViewModel: alias, nombre, teléfono, correo y
el resumen de desempeño. Llama a refresh() cada vez que vuelve a primer plano para mantener las cifras al día.
Casos de uso (vía ViewModel): GetCurrentModeratorUseCase y GetModeratorPerformanceUseCase.
Navegación: onStatsClick abre el dashboard completo de estadísticas (ModeratorStatsScreen) y
onSettingsClick abre Configuración; las rutas las define el grafo de navegación.
**/

// Margen horizontal de la pantalla.
private val ScreenHorizontalPadding = 24.dp

/**
 Perfil del moderador (pestaña Perfil): su alias, el resumen de su desempeño (al tocarlo abre el dashboard),
 su información (nombre, teléfono y correo) y Configuración. No tiene editar perfil, me gusta ni publicaciones.
 */
@Composable
fun ModeratorProfileScreen(
    // Acción para abrir el dashboard de estadísticas.
    onStatsClick: () -> Unit,
    // Acción para abrir Configuración.
    onSettingsClick: () -> Unit,
    // Modificador opcional del contenedor.
    modifier: Modifier = Modifier,
    // ViewModel inyectado por Hilt.
    viewModel: ModeratorProfileViewModel = hiltViewModel(),
) {
    // Observa el estado respetando el ciclo de vida.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Recarga los datos cada vez que la pantalla vuelve a primer plano.
    LifecycleResumeEffect(Unit) {
        // Pide al ViewModel refrescar perfil y desempeño.
        viewModel.refresh()
        // No hay nada que limpiar al pausar o salir.
        onPauseOrDispose { }
    }

    // Dibuja el contenido con el estado y los callbacks de navegación.
    ModeratorProfileContent(uiState = uiState, onStatsClick = onStatsClick, onSettingsClick = onSettingsClick, modifier = modifier)
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun ModeratorProfileContent(
    // Estado a dibujar.
    uiState: ModeratorProfileUiState,
    // Acción al tocar el resumen de desempeño.
    onStatsClick: () -> Unit,
    // Acción al tocar Configuración.
    onSettingsClick: () -> Unit,
    // Modificador opcional.
    modifier: Modifier = Modifier,
) {
    // Columna principal desplazable.
    Column(
        // Ocupa todo, pinta el fondo, permite desplazamiento vertical y aplica márgenes.
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenHorizontalPadding, vertical = 24.dp),
        // Centra horizontalmente el contenido.
        horizontalAlignment = Alignment.CenterHorizontally,
        // Separación entre secciones.
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Avatar y alias; sin ubicación (los moderadores no tienen).
        UserSummaryCard(
            // Nombre visible: el nombre completo o, si está vacío, el alias.
            name = uiState.name.ifBlank { uiState.userName },
            // Alias con formato "@usuario".
            displayName = stringResource(R.string.profile_username, uiState.userName),
            // Sin ubicación.
            location = null,
            // Relleno interno de la tarjeta.
            contentPadding = PaddingValues(vertical = 24.dp, horizontal = 20.dp),
        )

        // Mismo resumen que el dashboard; tocarlo lleva al dashboard completo.
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Título de la sección de desempeño.
            Text(
                // Texto del título.
                text = stringResource(R.string.mod_stats_title),
                // Estilo de título mediano.
                style = MaterialTheme.typography.titleMedium,
                // Color principal.
                color = AppTheme.colors.textPrimary,
            )
            // Tarjeta resumen de desempeño que abre las estadísticas al tocarla.
            PerformanceSummaryCard(performance = uiState.performance, onClick = onStatsClick)
        }

        // Tarjeta con la información personal del moderador.
        SectionCard(title = stringResource(R.string.mod_profile_info), icon = Icons.Outlined.Badge) {
            // Fila del nombre (guion si está vacío).
            InfoRow(icon = Icons.Outlined.Person, label = stringResource(R.string.user_profile_name), value = uiState.name.ifBlank { "—" })
            // Separador entre filas.
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = AppTheme.colors.surfaceTag)
            // Fila del teléfono (guion si está vacío).
            InfoRow(icon = Icons.Outlined.Phone, label = stringResource(R.string.mod_profile_phone), value = uiState.phone.ifBlank { "—" })
            // Separador entre filas.
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = AppTheme.colors.surfaceTag)
            // Fila del correo (guion si está vacío).
            InfoRow(icon = Icons.Outlined.Email, label = stringResource(R.string.mod_profile_email), value = uiState.email.ifBlank { "—" })
        }

        // Pequeño espacio antes del botón de Configuración.
        Spacer(Modifier.height(4.dp))

        // Botón de menú que abre Configuración.
        MenuActionButton(
            // Texto del botón.
            text = stringResource(R.string.profile_settings),
            // Ícono de engranaje.
            icon = Icons.Outlined.Settings,
            // Acción al pulsarlo.
            onClick = onSettingsClick,
        )
    }
}

// Vista previa del perfil del moderador con datos de ejemplo.
@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun ModeratorProfilePreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Contenido con un estado simulado.
        ModeratorProfileContent(
            // Estado de ejemplo con datos y desempeño.
            uiState = ModeratorProfileUiState(
                // Datos personales de ejemplo.
                userName = "mod_01", name = "Nicolás Rojas", email = "mod@petadoption.com", phone = "3109876543",
                // Cifras de desempeño de ejemplo.
                performance = ModeratorPerformance(approved = 12, rejected = 3, inReview = 2),
                // Ya terminó de cargar.
                isLoading = false,
            ),
            // Callbacks vacíos para la vista previa.
            onStatsClick = {}, onSettingsClick = {},
        )
    }
}
