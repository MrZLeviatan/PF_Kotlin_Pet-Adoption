package co.edu.uniquindio.petadoptionapp.features.user.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.dialog.AppConfirmDialog

/**
 Confirmación para eliminar un chat: "Eliminar para ambos" (rojo), "Eliminar solo para mí" y "Cancelar".
 Se usa en la conversación (menú ⋮) y en la lista (mantener presionado).
 */
@Composable
fun DeleteChatDialog(
    onDeleteForEveryone: () -> Unit,
    onDeleteForMe: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false,
) {
    AppConfirmDialog(
        title = stringResource(R.string.chat_delete_title),
        message = stringResource(R.string.chat_delete_message),
        confirmText = stringResource(R.string.chat_delete_for_everyone),
        cancelText = stringResource(R.string.common_cancel),
        onConfirm = onDeleteForEveryone,
        onDismiss = onDismiss,
        isLoading = isLoading,
        isDestructive = true,
        alternativeText = stringResource(R.string.chat_delete_for_me),
        onAlternative = onDeleteForMe,
    )
}
