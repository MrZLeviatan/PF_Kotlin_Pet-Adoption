package co.edu.uniquindio.petadoptionapp.features.user.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.dialog.AppConfirmDialog

/**
Diálogo de confirmación para eliminar un chat. Ofrece tres opciones: "Eliminar para ambos"
(botón principal en rojo, porque es destructivo), "Eliminar solo para mí" (opción alternativa)
y "Cancelar". Es un componente sin estado propio: recibe las acciones como lambdas y el estado
de carga desde el ViewModel que lo muestra.
Se usa en la conversación (menú ⋮ de ChatScreen) y en la lista de chats (al mantener presionado
un chat en ChatListScreen); en ambos casos el ViewModel termina llamando a DeleteChatUseCase.
**/
@Composable
fun DeleteChatDialog(
    // Acción que se ejecuta al elegir eliminar el chat para ambos participantes.
    onDeleteForEveryone: () -> Unit,
    // Acción que se ejecuta al elegir eliminar el chat solo para el usuario actual.
    onDeleteForMe: () -> Unit,
    // Acción que se ejecuta al cancelar o cerrar el diálogo.
    onDismiss: () -> Unit,
    // Indica si la eliminación está en curso para mostrar el indicador de carga.
    isLoading: Boolean = false,
) {
    // Reutiliza el diálogo de confirmación común de la aplicación.
    AppConfirmDialog(
        // Título del diálogo.
        title = stringResource(R.string.chat_delete_title),
        // Mensaje explicativo de lo que ocurrirá al eliminar.
        message = stringResource(R.string.chat_delete_message),
        // Texto del botón principal: eliminar para ambos.
        confirmText = stringResource(R.string.chat_delete_for_everyone),
        // Texto del botón de cancelar.
        cancelText = stringResource(R.string.common_cancel),
        // El botón principal elimina el chat para ambos.
        onConfirm = onDeleteForEveryone,
        // Cerrar o cancelar descarta el diálogo.
        onDismiss = onDismiss,
        // Muestra el estado de carga mientras se elimina.
        isLoading = isLoading,
        // Pinta el botón principal en rojo por ser una acción destructiva.
        isDestructive = true,
        // Texto de la opción alternativa: eliminar solo para mí.
        alternativeText = stringResource(R.string.chat_delete_for_me),
        // La opción alternativa elimina el chat solo para el usuario actual.
        onAlternative = onDeleteForMe,
    )
}
