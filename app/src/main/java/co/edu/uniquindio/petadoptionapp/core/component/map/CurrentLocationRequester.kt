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
 Pide el permiso de ubicación (si hace falta) y lee la ubicación actual.
 Devuelve la función que inicia el pedido; el resultado llega en [onResult] (null si se negó o no se pudo leer).
 Igual que con la cámara: explica para qué si ya se negó una vez, y ofrece los ajustes si se negó del todo.
 */
@Composable
fun rememberCurrentLocationRequester(onResult: (GeoPoint?) -> Unit): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentOnResult by rememberUpdatedState(onResult)
    var showRationale by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    fun read() = scope.launch { currentOnResult(currentDeviceLocation(context)) }

    val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.any { it }) {
            read()
        } else {
            // Negado con "no volver a preguntar": solo queda ir a los ajustes.
            val activity = context.findActivity()
            if (activity != null && !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                showSettings = true
            }
            currentOnResult(null)
        }
    }

    if (showRationale) {
        AppConfirmDialog(
            title = stringResource(R.string.location_permission_title),
            message = stringResource(R.string.location_permission_message),
            confirmText = stringResource(R.string.photo_permission_allow),
            cancelText = stringResource(R.string.common_cancel),
            onConfirm = {
                showRationale = false
                launcher.launch(permissions)
            },
            onDismiss = {
                showRationale = false
                currentOnResult(null)
            },
        )
    }
    if (showSettings) {
        AppConfirmDialog(
            title = stringResource(R.string.location_permission_title),
            message = stringResource(R.string.location_permission_denied_message),
            confirmText = stringResource(R.string.photo_permission_settings),
            cancelText = stringResource(R.string.common_cancel),
            onConfirm = {
                showSettings = false
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            },
            onDismiss = { showSettings = false },
        )
    }

    return {
        val granted = permissions.any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
        val activity = context.findActivity()
        when {
            granted -> read()
            activity != null && ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION) ->
                showRationale = true
            else -> launcher.launch(permissions)
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
