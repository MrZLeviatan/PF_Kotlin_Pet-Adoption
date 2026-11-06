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
Componente que gestiona todo el flujo para obtener fotos desde la cámara o la galería del dispositivo.
Se usa en Editar perfil, Crear/Editar publicación y el chat. Muestra la hoja inferior [PhotoSourceSheet] y, según la
opción elegida, lanza el selector de fotos del sistema o la cámara mediante la API de Activity Result.
Las fotos obtenidas se copian (y reducen) en la carpeta privada de la app con [saveImageLocally] y se entregan como
rutas locales; los archivos temporales de la cámara se borran después.
**/

/**
Elegir fotos con la hoja "Cámara / Galería" (Editar perfil, Crear/Editar publicación y chat).
- Galería: el selector de fotos del sistema. No pide permiso de almacenamiento: la persona elige
  qué fotos compartir y la app solo recibe esas (en Android viejos se usa el selector de documentos).
- Cámara: pide el permiso de cámara la primera vez, explica para qué si ya lo negó una vez, y si lo
  negó del todo ofrece abrir los ajustes de la app.
Las fotos se guardan en la carpeta de la app y [onPicked] recibe sus direcciones (máximo [maxSelection]).
Mientras [visible] sea true se muestra la hoja; [onDismiss] la cierra.
**/
@Composable
fun ImageSourcePicker(
    // Indica si la hoja "Cámara / Galería" debe mostrarse.
    visible: Boolean,
    // Acción para cerrar la hoja.
    onDismiss: () -> Unit,
    // Acción que recibe las rutas locales de las fotos guardadas.
    onPicked: (List<String>) -> Unit,
    // Número máximo de fotos que se pueden elegir de una vez.
    maxSelection: Int = 1,
) {
    // Contexto de Android para permisos, archivos y abrir ajustes.
    val context = LocalContext.current
    // Alcance de corrutinas para copiar las fotos sin bloquear la interfaz.
    val scope = rememberCoroutineScope()
    // Mantiene la versión más reciente del callback, ya que los launchers se registran una sola vez.
    val currentOnPicked by rememberUpdatedState(onPicked)

    // Controla el diálogo que explica para qué se necesita la cámara.
    var showRationale by remember { mutableStateOf(false) }
    // Controla el diálogo que ofrece abrir los ajustes cuando el permiso se negó de forma permanente.
    var showSettings by remember { mutableStateOf(false) }
    // Archivo donde la cámara deja la foto (se recuerda si la app se recrea mientras la cámara está abierta).
    var cameraFilePath by rememberSaveable { mutableStateOf<String?>(null) }

    // Copia (y reduce) las fotos elegidas y avisa con sus direcciones locales.
    fun store(uris: List<Uri>, temp: File? = null) {
        // Si no se eligió nada, solo borra el archivo temporal (si lo hay) y termina.
        if (uris.isEmpty()) return deleteQuietly(temp)
        // Copia las fotos en segundo plano.
        scope.launch {
            // Respeta el máximo permitido y guarda cada foto localmente, descartando las que fallen.
            val saved = uris.take(maxSelection).mapNotNull { saveImageLocally(context, it) }
            // Borra el archivo temporal de la cámara, ya copiado.
            deleteQuietly(temp)
            // Solo avisa si se guardó al menos una foto.
            if (saved.isNotEmpty()) currentOnPicked(saved)
        }
    }

    // Lanzador del selector de fotos del sistema para elegir una sola imagen.
    val galleryOne = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Guarda la foto elegida (si se eligió alguna).
        store(listOfNotNull(uri))
    }
    // Lanzador del selector de fotos para elegir varias imágenes (el contrato exige un máximo de al menos 2).
    val galleryMany = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = maxSelection.coerceAtLeast(2))
    ) { uris -> store(uris) }

    // Lanzador de la cámara: toma la foto y la escribe en la URI indicada; devuelve si se tomó.
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { taken ->
        // Recupera el archivo donde la cámara debía dejar la foto.
        val file = cameraFilePath?.let(::File)
        // Limpia la ruta guardada, ya no se necesita.
        cameraFilePath = null
        // Si se tomó la foto, la guarda y luego borra el temporal; si se canceló, solo borra el temporal.
        if (taken && file != null) store(listOf(Uri.fromFile(file)), temp = file) else deleteQuietly(file)
    }

    // Crea el archivo temporal de la foto y abre la cámara apuntando a él.
    fun openCamera() {
        // Obtiene el archivo y su URI compartible (FileProvider) para la cámara.
        val (file, uri) = newCameraPhoto(context)
        // Recuerda la ruta para leerla cuando la cámara regrese.
        cameraFilePath = file.absolutePath
        // Abre la app de cámara.
        camera.launch(uri)
    }

    // Lanzador del diálogo del sistema que pide el permiso de cámara.
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        when {
            // Si se concedió, abre la cámara.
            granted -> openCamera()
            // Negado con "no volver a preguntar": solo queda ir a los ajustes.
            context.findActivity()?.let { !ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA) } == true ->
                showSettings = true
        }
    }

    // Acción de la opción "Cámara" de la hoja.
    fun onCameraClick() {
        // Cierra la hoja.
        onDismiss()
        // Activity actual, necesaria para consultar si se debe explicar el permiso.
        val activity = context.findActivity()
        when {
            // Si ya hay permiso, abre la cámara directamente.
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> openCamera()
            // Ya lo negó una vez: se explica para qué antes de volver a pedirlo.
            activity != null && ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA) -> showRationale = true
            // Primera vez: pide el permiso al sistema.
            else -> cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    // Acción de la opción "Galería" de la hoja.
    fun onGalleryClick() {
        // Cierra la hoja.
        onDismiss()
        // Solicitud del selector limitada a imágenes (sin videos).
        val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        // Usa el selector múltiple o el simple según el máximo permitido.
        if (maxSelection > 1) galleryMany.launch(request) else galleryOne.launch(request)
    }

    // Muestra la hoja de elección de origen mientras sea visible.
    if (visible) {
        // Hoja inferior con las opciones cámara y galería.
        PhotoSourceSheet(onDismiss = onDismiss, onCameraClick = ::onCameraClick, onGalleryClick = ::onGalleryClick)
    }

    // Diálogo que explica para qué se usa la cámara antes de volver a pedir el permiso.
    if (showRationale) {
        AppConfirmDialog(
            // Título del diálogo de permiso de cámara.
            title = stringResource(R.string.photo_permission_title),
            // Mensaje con la razón del permiso.
            message = stringResource(R.string.photo_permission_message),
            // Texto del botón para permitir.
            confirmText = stringResource(R.string.photo_permission_allow),
            // Texto del botón para cancelar.
            cancelText = stringResource(R.string.common_cancel),
            // Al confirmar, cierra el diálogo y vuelve a pedir el permiso.
            onConfirm = {
                showRationale = false
                cameraPermission.launch(Manifest.permission.CAMERA)
            },
            // Al cancelar, solo cierra el diálogo.
            onDismiss = { showRationale = false },
        )
    }

    // Diálogo que lleva a los ajustes cuando el permiso se negó de forma permanente.
    if (showSettings) {
        AppConfirmDialog(
            // Título del diálogo de permiso de cámara.
            title = stringResource(R.string.photo_permission_title),
            // Mensaje que explica cómo activar el permiso desde los ajustes.
            message = stringResource(R.string.photo_permission_denied_message),
            // Texto del botón que abre los ajustes.
            confirmText = stringResource(R.string.photo_permission_settings),
            // Texto del botón para cancelar.
            cancelText = stringResource(R.string.common_cancel),
            // Al confirmar, cierra el diálogo y abre la pantalla de detalles de la app en los ajustes.
            onConfirm = {
                showSettings = false
                // Abre los ajustes del sistema en la ficha de esta app.
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
}

// La Activity detrás del contexto de Compose (hace falta para saber si conviene explicar el permiso).
private tailrec fun Context.findActivity(): Activity? = when (this) {
    // El contexto ya es una Activity.
    is Activity -> this
    // Es un envoltorio: se busca en su contexto base.
    is ContextWrapper -> baseContext.findActivity()
    // No hay Activity asociada.
    else -> null
}
