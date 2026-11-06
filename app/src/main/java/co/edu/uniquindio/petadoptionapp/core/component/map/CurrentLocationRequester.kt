package co.edu.uniquindio.petadoptionapp.core.component.map

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.dialog.AppConfirmDialog
import co.edu.uniquindio.petadoptionapp.core.util.currentDeviceLocation
import kotlinx.coroutines.launch

/**
Utilidad de Compose para obtener la ubicación actual del dispositivo gestionando los permisos de ubicación.
Se usa en las pantallas con mapa (por ejemplo, el botón "mi ubicación" del mapa de publicaciones) para centrar
la cámara en la persona o dibujar su punto azul en [AppMap].

Flujo de permisos (igual que con la cámara):
- Si ya hay permiso (preciso o aproximado), lee la ubicación directamente con [currentDeviceLocation].
- Si la persona ya lo negó una vez, primero muestra un diálogo explicando para qué se necesita.
- Si lo negó con "no volver a preguntar", ofrece abrir los ajustes de la app para concederlo manualmente.
**/

/**
Pide el permiso de ubicación (si hace falta) y lee la ubicación actual.
Devuelve la función que inicia el pedido; el resultado llega en [onResult] (null si se negó o no se pudo leer).
Igual que con la cámara: explica para qué si ya se negó una vez, y ofrece los ajustes si se negó del todo.
**/
@Composable
fun rememberCurrentLocationRequester(onResult: (GeoPoint?) -> Unit): () -> Unit {
    // Contexto de Android para revisar permisos, leer la ubicación y abrir ajustes.
    val context = LocalContext.current
    // Alcance de corrutinas ligado a la composición, para leer la ubicación sin bloquear la interfaz.
    val scope = rememberCoroutineScope()
    // Mantiene siempre la versión más reciente del callback, aunque el launcher se haya creado antes.
    val currentOnResult by rememberUpdatedState(onResult)
    // Controla si se muestra el diálogo que explica por qué se necesita el permiso.
    var showRationale by remember { mutableStateOf(false) }
    // Controla si se muestra el diálogo que invita a ir a los ajustes del sistema.
    var showSettings by remember { mutableStateOf(false) }

    // Lanza una corrutina que lee la ubicación del dispositivo y entrega el resultado al callback.
    fun read() = scope.launch { currentOnResult(currentDeviceLocation(context)) }

    // Permisos solicitados: ubicación precisa y aproximada (la persona puede conceder solo la aproximada).
    val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    // Registra el lanzador del diálogo de permisos del sistema y define qué hacer con la respuesta.
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        // Si se concedió al menos uno de los permisos, se puede leer la ubicación.
        if (result.values.any { it }) {
            // Lee la ubicación actual.
            read()
        } else {
            // Negado con "no volver a preguntar": solo queda ir a los ajustes.
            val activity = context.findActivity()
            // Si el sistema ya no mostraría la explicación, significa que la negación es permanente.
            if (activity != null && !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Muestra el diálogo que lleva a los ajustes.
                showSettings = true
            }
            // Informa que no se obtuvo ubicación.
            currentOnResult(null)
        }
    }

    // Diálogo explicativo: se muestra si la persona ya negó el permiso una vez.
    if (showRationale) {
        // Diálogo de confirmación con la razón del permiso.
        AppConfirmDialog(
            // Título del diálogo de permiso de ubicación.
            title = stringResource(R.string.location_permission_title),
            // Mensaje que explica para qué se usa la ubicación.
            message = stringResource(R.string.location_permission_message),
            // Texto del botón para permitir.
            confirmText = stringResource(R.string.photo_permission_allow),
            // Texto del botón para cancelar.
            cancelText = stringResource(R.string.common_cancel),
            // Al confirmar, cierra el diálogo y vuelve a pedir el permiso al sistema.
            onConfirm = {
                showRationale = false
                launcher.launch(permissions)
            },
            // Al cancelar, cierra el diálogo e informa que no hay ubicación.
            onDismiss = {
                showRationale = false
                currentOnResult(null)
            },
        )
    }
    // Diálogo de ajustes: se muestra si el permiso fue negado de forma permanente.
    if (showSettings) {
        // Diálogo de confirmación que ofrece abrir los ajustes de la app.
        AppConfirmDialog(
            // Título del diálogo de permiso de ubicación.
            title = stringResource(R.string.location_permission_title),
            // Mensaje que explica que el permiso fue negado y cómo activarlo.
            message = stringResource(R.string.location_permission_denied_message),
            // Texto del botón que abre los ajustes.
            confirmText = stringResource(R.string.photo_permission_settings),
            // Texto del botón para cancelar.
            cancelText = stringResource(R.string.common_cancel),
            // Al confirmar, cierra el diálogo y abre la pantalla de detalles de la app en los ajustes del sistema.
            onConfirm = {
                showSettings = false
                // Inicia la actividad de ajustes apuntando al paquete de esta app.
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                        // Necesario por si el contexto no es una Activity.
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            },
            // Al cancelar, solo cierra el diálogo.
            onDismiss = { showSettings = false },
        )
    }

    // Devuelve la función que la pantalla llama para iniciar el pedido de ubicación.
    return {
        // Revisa si ya se tiene alguno de los permisos de ubicación.
        val granted = permissions.any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
        // Busca la Activity que aloja la pantalla (necesaria para consultar si se debe explicar el permiso).
        val activity = context.findActivity()
        // Decide el siguiente paso según el estado del permiso.
        when {
            // Con permiso concedido, lee la ubicación de inmediato.
            granted -> read()
            // Si ya se negó una vez, primero muestra la explicación.
            activity != null && ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION) ->
                showRationale = true
            // En cualquier otro caso (primera vez), pide el permiso al sistema.
            else -> launcher.launch(permissions)
        }
    }
}

// Recorre la cadena de ContextWrapper hasta encontrar la Activity que contiene el contexto (o null si no hay).
private tailrec fun Context.findActivity(): Activity? = when (this) {
    // El contexto ya es una Activity: se devuelve.
    is Activity -> this
    // Es un envoltorio: se busca en su contexto base.
    is ContextWrapper -> baseContext.findActivity()
    // No hay Activity asociada.
    else -> null
}
