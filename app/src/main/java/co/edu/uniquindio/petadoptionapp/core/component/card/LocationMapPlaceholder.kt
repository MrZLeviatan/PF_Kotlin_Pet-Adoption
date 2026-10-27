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

private val MapCornerRadius = 16.dp

/**
Cuadro provisional donde irá el mapa.
TODO (Mapbox): reemplazar el contenido del Box por el MapView; el botón
"Establecer mi ubicación" se queda como overlay y la firma no cambia.
Con [onSetLocationClick] en `null` es solo de lectura: el recuadro vacío, sin botón
(ej. la ubicación aproximada de una publicación).
 */
@Composable
fun LocationMapPlaceholder(
    onSetLocationClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    height: Dp = 110.dp,
) {
    val shape = RoundedCornerShape(MapCornerRadius)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(AppTheme.colors.surfaceTag)
            .border(1.dp, AppTheme.colors.border, shape),
        contentAlignment = Alignment.Center,
    ) {
        if (onSetLocationClick != null) Button(
            onClick = onSetLocationClick,
            shape = CircleShape,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.actionPrimary,
                contentColor = AppTheme.colors.onActionPrimary,
            ),
        ) {
            Icon(
                imageVector = Icons.Outlined.MyLocation,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.register_set_location),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationMapPlaceholderPreview() {
    PetAdoptionAppTheme {
        LocationMapPlaceholder(onSetLocationClick = {})
    }
}