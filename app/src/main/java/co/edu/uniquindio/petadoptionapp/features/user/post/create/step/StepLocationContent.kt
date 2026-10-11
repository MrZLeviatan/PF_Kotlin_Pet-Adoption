package co.edu.uniquindio.petadoptionapp.features.user.post.create.step

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.uniquindio.petadoptionapp.core.component.FormCard
import co.edu.uniquindio.petadoptionapp.core.component.InfoTip
import co.edu.uniquindio.petadoptionapp.core.theme.*
import co.edu.uniquindio.petadoptionapp.features.user.post.create.CreatePostUiState
import co.edu.uniquindio.petadoptionapp.features.user.post.create.CreatePostViewModel

// Componente visual correspondiente al cuarto paso del formulario (Selección de ubicación).
@Composable
fun StepLocationContent(state: CreatePostUiState, viewModel: CreatePostViewModel) {
    // Tarjeta contenedora blanca con título centrado para la sección de ubicación.
    FormCard(title = "Ubicación de la Mascota") {
        // Texto indicativo con la instrucción de selección para el usuario.
        Text("Seleccione dónde se encuentra o fue vista la mascota", fontSize = 13.sp, color = TextoMarron)
        // Inserta un espacio vertical de 12dp.
        Spacer(modifier = Modifier.height(12.dp))

        // Fila horizontal para organizar los dos botones de selección de tipo de ubicación.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Opción para seleccionar la ubicación actual mediante GPS.
            LocationOption(
                text = "Ubicación Actual",
                icon = Icons.Outlined.MyLocation,
                selected = state.useCurrentLocation,
                onClick = { viewModel.onUseCurrentLocationChange(true) },
                modifier = Modifier.weight(1f)
            )
            // Opción para seleccionar la ubicación manualmente mediante un mapa interactivo.
            LocationOption(
                text = "Seleccionar Mapa",
                icon = Icons.Outlined.Map,
                selected = !state.useCurrentLocation,
                onClick = { viewModel.onUseCurrentLocationChange(false) },
                modifier = Modifier.weight(1f)
            )
        }

        // Inserta un espacio vertical de 12dp.
        Spacer(modifier = Modifier.height(12.dp))

        // Caja contenedora que actúa como visor temporal para el mapa.
        Box(
            modifier = Modifier
                // Ocupa todo el ancho disponible.
                .fillMaxWidth()
                // Define una altura fija de 220dp para el área del mapa.
                .height(220.dp)
                // Recorta las esquinas con un radio de 16dp.
                .clip(RoundedCornerShape(16.dp))
                // Aplica el color de fondo para avisos/visores.
                .background(FondoAviso)
                // Dibuja un borde exterior marrón de 1dp.
                .border(1.dp, BordeMarron, RoundedCornerShape(16.dp)),
            // Centra horizontal y verticalmente el contenido dentro de la caja.
            contentAlignment = Alignment.Center
        ) {
            // Columna interna que agrupa el icono de localización y el texto de reemplazo.
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = ChipSeleccionado, modifier = Modifier.size(40.dp))
                Text("Aquí irá el mapa", fontSize = 13.sp, color = TextoMarron)
            }
        }
    }

    // Inserta un espacio vertical de 16dp fuera de la tarjeta.
    Spacer(modifier = Modifier.height(16.dp))
    // Muestra la caja de aviso de privacidad informando sobre la protección de la dirección exacta.
    InfoTip(
        icon = Icons.Outlined.VerifiedUser,
        text = "Por tu seguridad, solo se mostrará la ubicación aproximada (barrio/zona) en la publicación. La dirección exacta no será visible públicamente."
    )
}

// Componente privado reutilizable para los botones de opción de ubicación.
@Composable
private fun LocationOption(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        // Redondea las esquinas con un radio de 10dp.
        shape = RoundedCornerShape(10.dp),
        // Cambia el color de fondo: marrón de chip si está seleccionado, claro en reposo.
        color = if (selected) ChipSeleccionado else FondoClaro,
        // Dibuja un borde marrón de 1dp alrededor del botón.
        border = BorderStroke(1.dp, BordeMarron)
    ) {
        // Fila para organizar horizontalmente el icono y el texto de la opción.
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = TextoMarron, modifier = Modifier.size(16.dp))
            // Muestra el texto en una sola línea.
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextoMarron, maxLines = 1)
        }
    }
}