package co.edu.uniquindio.petadoptionapp.core.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
 Deslizar para borrar (como en Instagram): al arrastrar hacia la izquierda aparece un fondo rojo con la papelera,
 y al completar el deslizamiento se llama [onDelete]. Hacia la derecha no hace nada.
 Quien lo usa quita el elemento de su lista; si el borrado falla, lo vuelve a poner.
 Como deslizar no sirve con TalkBack, la misma acción queda como acción personalizada ([deleteDescription]).
 */
@Composable
fun SwipeToDeleteBox(
    onDelete: () -> Unit,
    deleteDescription: String,
    modifier: Modifier = Modifier,
    // Misma forma que la tarjeta que envuelve, para que el rojo no se salga de sus esquinas.
    shape: Shape = RoundedCornerShape(20.dp),
    content: @Composable RowScope.() -> Unit,
) {
    val state = rememberSwipeToDismissBoxState()
    val currentOnDelete by rememberUpdatedState(onDelete)

    // Deslizamiento completo hacia la izquierda: se borra.
    LaunchedEffect(state.currentValue) {
        if (state.currentValue == SwipeToDismissBoxValue.EndToStart) currentOnDelete()
    }

    SwipeToDismissBox(
        state = state,
        modifier = modifier.semantics {
            customActions = listOf(CustomAccessibilityAction(deleteDescription) { currentOnDelete(); true })
        },
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            // Solo se pinta mientras se arrastra hacia la izquierda.
            if (state.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(shape)
                        .background(AppTheme.colors.stateError)
                        .padding(end = 24.dp),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = deleteDescription, tint = AppTheme.colors.onStateError)
                }
            }
        },
        content = content,
    )
}

@Preview(showBackground = true)
@Composable
private fun SwipeToDeleteBoxPreview() {
    PetAdoptionAppTheme {
        SwipeToDeleteBox(onDelete = {}, deleteDescription = "Eliminar") {
            AppCard { Text("Desliza hacia la izquierda") }
        }
    }
}
