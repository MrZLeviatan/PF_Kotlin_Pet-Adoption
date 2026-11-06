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
Contenedor que envuelve una tarjeta de una lista (notificaciones, chats, publicaciones) para
permitir borrarla con un gesto de deslizamiento. No dibuja la tarjeta: solo añade el gesto,
el fondo rojo de borrado y la acción accesible equivalente.
**/

/**
Deslizar para borrar (como en Instagram): al arrastrar hacia la izquierda aparece un fondo rojo con la papelera,
y al completar el deslizamiento se llama [onDelete]. Hacia la derecha no hace nada.
Quien lo usa quita el elemento de su lista; si el borrado falla, lo vuelve a poner.
Como deslizar no sirve con TalkBack, la misma acción queda como acción personalizada ([deleteDescription]).
**/
@Composable
fun SwipeToDeleteBox(
    // Acción que se ejecuta al completar el deslizamiento (o desde la acción accesible).
    onDelete: () -> Unit,
    // Texto de la acción de borrado ("Eliminar") para TalkBack y para el icono de la papelera.
    deleteDescription: String,
    // Modificador opcional para personalizar el contenedor desde afuera.
    modifier: Modifier = Modifier,
    // Misma forma que la tarjeta que envuelve, para que el rojo no se salga de sus esquinas.
    shape: Shape = RoundedCornerShape(20.dp),
    // Tarjeta o contenido que se podrá deslizar.
    content: @Composable RowScope.() -> Unit,
) {
    // Estado del deslizamiento: guarda la posición y si el elemento fue descartado.
    val state = rememberSwipeToDismissBoxState()
    // Mantiene siempre la versión más reciente de onDelete sin reiniciar el LaunchedEffect.
    val currentOnDelete by rememberUpdatedState(onDelete)

    // Deslizamiento completo hacia la izquierda: se borra. Se relanza cada vez que cambia el valor del estado.
    LaunchedEffect(state.currentValue) {
        // Solo borra cuando el elemento quedó descartado de derecha a izquierda.
        if (state.currentValue == SwipeToDismissBoxValue.EndToStart) currentOnDelete()
    }

    // Componente de Material 3 que implementa el gesto de deslizar para descartar.
    SwipeToDismissBox(
        // Conecta el estado del deslizamiento.
        state = state,
        // Agrega la acción personalizada "Eliminar" para usuarios de TalkBack.
        modifier = modifier.semantics {
            // La acción ejecuta el borrado y devuelve true para indicar que se manejó.
            customActions = listOf(CustomAccessibilityAction(deleteDescription) { currentOnDelete(); true })
        },
        // Desactiva el deslizamiento hacia la derecha.
        enableDismissFromStartToEnd = false,
        // Fondo que se revela detrás de la tarjeta al deslizar.
        backgroundContent = {
            // Solo se pinta mientras se arrastra hacia la izquierda.
            if (state.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                // Caja roja que ocupa todo el espacio detrás de la tarjeta.
                Box(
                    modifier = Modifier
                        // Llena todo el área del contenedor.
                        .fillMaxSize()
                        // Recorta con la forma de la tarjeta para no sobresalir de sus esquinas.
                        .clip(shape)
                        // Fondo con el color de error del tema.
                        .background(AppTheme.colors.stateError)
                        // Separa la papelera del borde derecho.
                        .padding(end = 24.dp),
                    // Alinea la papelera al centro del extremo derecho.
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    // Icono de papelera con color de contraste sobre el rojo.
                    Icon(Icons.Outlined.Delete, contentDescription = deleteDescription, tint = AppTheme.colors.onStateError)
                }
            }
        },
        // Contenido deslizable proporcionado por quien usa el componente.
        content = content,
    )
}

// Vista previa en Android Studio del contenedor con una tarjeta de ejemplo.
@Preview(showBackground = true)
@Composable
private fun SwipeToDeleteBoxPreview() {
    // Envuelve el componente en el tema para que use los colores y tipografías reales.
    PetAdoptionAppTheme {
        // Contenedor de ejemplo sin acción real de borrado.
        SwipeToDeleteBox(onDelete = {}, deleteDescription = "Eliminar") {
            // Tarjeta de ejemplo con una instrucción para el usuario.
            AppCard { Text("Desliza hacia la izquierda") }
        }
    }
}
