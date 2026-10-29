package co.edu.uniquindio.petadoptionapp.core.component.photo

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.dialog.AppConfirmDialog
import co.edu.uniquindio.petadoptionapp.core.util.deleteQuietly
import co.edu.uniquindio.petadoptionapp.core.util.newCameraPhoto
import co.edu.uniquindio.petadoptionapp.core.util.saveImageLocally
import java.io.File
import kotlinx.coroutines.launch

/**
 Elegir fotos con la hoja "Cámara / Galería" (Editar perfil, Crear/Editar publicación y chat).
 - Galería: el selector de fotos del sistema. No pide permiso de almacenamiento: la persona elige
   qué fotos compartir y la app solo recibe esas (en Android viejos se usa el selector de documentos).
 - Cámara: pide el permiso de cámara la primera vez, explica para qué si ya lo negó una vez, y si lo
   negó del todo ofrece abrir los ajustes de la app.
 Las fotos se guardan en la carpeta de la app y [onPicked] recibe sus direcciones (máximo [maxSelection]).
 Mientras [visible] sea true se muestra la hoja; [onDismiss] la cierra.
 */
@Composable
fun ImageSourcePicker(
    visible: Boolean,
    onDismiss: () -> Unit,
    onPicked: (List<String>) -> Unit,
    maxSelection: Int = 1,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentOnPicked by rememberUpdatedState(onPicked)

    var showRationale by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    // Archivo donde la cámara deja la foto (se recuerda si la app se recrea mientras la cámara está abierta).
    var cameraFilePath by rememberSaveable { mutableStateOf<String?>(null) }

    // Copia (y reduce) las fotos elegidas y avisa con sus direcciones locales.
    fun store(uris: List<Uri>, temp: File? = null) {
        if (uris.isEmpty()) return deleteQuietly(temp)
        scope.launch {
            val saved = uris.take(maxSelection).mapNotNull { saveImageLocally(context, it) }
            deleteQuietly(temp)
            if (saved.isNotEmpty()) currentOnPicked(saved)
        }
    }

    val galleryOne = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        store(listOfNotNull(uri))
    }
    val galleryMany = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = maxSelection.coerceAtLeast(2))
    ) { uris -> store(uris) }

    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { taken ->
        val file = cameraFilePath?.let(::File)
        cameraFilePath = null
        if (taken && file != null) store(listOf(Uri.fromFile(file)), temp = file) else deleteQuietly(file)
    }

    fun openCamera() {
        val (file, uri) = newCameraPhoto(context)
        cameraFilePath = file.absolutePath
        camera.launch(uri)
    }

    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        when {
            granted -> openCamera()
            // Negado con "no volver a preguntar": solo queda ir a los ajustes.
            context.findActivity()?.let { !ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA) } == true ->
                showSettings = true
        }
    }

    fun onCameraClick() {
        onDismiss()
        val activity = context.findActivity()
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> openCamera()
            // Ya lo negó una vez: se explica para qué antes de volver a pedirlo.
            activity != null && ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA) -> showRationale = true
            else -> cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    fun onGalleryClick() {
        onDismiss()
        val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        if (maxSelection > 1) galleryMany.launch(request) else galleryOne.launch(request)
    }

    if (visible) {
        PhotoSourceSheet(onDismiss = onDismiss, onCameraClick = ::onCameraClick, onGalleryClick = ::onGalleryClick)
    }

    if (showRationale) {
        AppConfirmDialog(
            title = stringResource(R.string.photo_permission_title),
            message = stringResource(R.string.photo_permission_message),
            confirmText = stringResource(R.string.photo_permission_allow),
            cancelText = stringResource(R.string.common_cancel),
            onConfirm = {
                showRationale = false
                cameraPermission.launch(Manifest.permission.CAMERA)
            },
            onDismiss = { showRationale = false },
        )
    }

    if (showSettings) {
        AppConfirmDialog(
            title = stringResource(R.string.photo_permission_title),
            message = stringResource(R.string.photo_permission_denied_message),
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
}

// La Activity detrás del contexto de Compose (hace falta para saber si conviene explicar el permiso).
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
