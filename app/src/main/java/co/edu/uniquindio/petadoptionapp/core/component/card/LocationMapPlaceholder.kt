package co.edu.uniquindio.petadoptionapp.core.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Recuadro provisional que ocupa el lugar de un mapa dentro de formularios y tarjetas (registro,
publicación). Mantiene el mismo tamaño, forma y borde que tendrá el mapa real, para que el diseño
de las pantallas no cambie cuando se integre el mapa definitivo.
**/

// Radio de las esquinas del recuadro del mapa.
private val MapCornerRadius = 16.dp

/**
Cuadro provisional donde irá el mapa.
TODO (MapLibre): donde aún se use, reemplazar el contenido del Box por el MapView; el botón
"Establecer mi ubicación" se queda como overlay y la firma no cambia.
Con [onSetLocationClick] en `null` es solo de lectura: el recuadro vacío, sin botón
(ej. la ubicación aproximada de una publicación).
**/
@Composable
fun LocationMapPlaceholder(
    // Acción del botón "Establecer mi ubicación"; `null` para la versión de solo lectura.
    onSetLocationClick: (() -> Unit)?,
    // Modificador opcional para personalizar el recuadro desde afuera.
    modifier: Modifier = Modifier,
    // Alto del recuadro (por defecto 110 dp).
    height: Dp = 110.dp,
) {
    // Forma redondeada reutilizada por el recorte y el borde.
    val shape = RoundedCornerShape(MapCornerRadius)

    // Caja que simula el área del mapa y centra su contenido.
    Box(
        modifier = modifier
            // Ocupa todo el ancho disponible.
            .fillMaxWidth()
            // Aplica el alto configurado.
            .height(height)
            // Recorta el contenido con esquinas redondeadas.
            .clip(shape)
            // Fondo neutro que representa el mapa vacío.
            .background(AppTheme.colors.surfaceTag)
            // Borde de 1 dp con la misma forma.
            .border(1.dp, AppTheme.colors.border, shape),
        // Centra el botón dentro del recuadro.
        contentAlignment = Alignment.Center,
    ) {
        // Solo muestra el botón cuando hay una acción para establecer la ubicación.
        if (onSetLocationClick != null) Button(
            // Ejecuta la acción de establecer ubicación.
            onClick = onSetLocationClick,
            // Botón en forma de píldora.
            shape = CircleShape,
            // Relleno interno compacto para que quepa en el recuadro.
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            // Colores de la acción principal del tema.
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.actionPrimary,
                contentColor = AppTheme.colors.onActionPrimary,
            ),
        ) {
            // Icono decorativo de ubicación; el texto ya describe la acción.
            Icon(
                imageVector = Icons.Outlined.MyLocation,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            // Separación entre icono y texto.
            Spacer(Modifier.width(8.dp))
            // Texto "Establecer mi ubicación" desde recursos.
            Text(
                text = stringResource(R.string.register_set_location),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

// Vista previa en Android Studio del recuadro con el botón visible.
@Preview(showBackground = true)
@Composable
private fun LocationMapPlaceholderPreview() {
    // Envuelve el componente en el tema para que use los colores y tipografías reales.
    PetAdoptionAppTheme {
        // Ejemplo con acción, por lo que se muestra el botón.
        LocationMapPlaceholder(onSetLocationClick = {})
    }
}