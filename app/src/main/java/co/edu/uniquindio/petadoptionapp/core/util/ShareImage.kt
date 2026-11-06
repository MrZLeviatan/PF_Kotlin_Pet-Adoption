package co.edu.uniquindio.petadoptionapp.core.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
Utilidad para compartir una imagen generada por la app (por ejemplo, la tarjeta de una publicación) con otras apps
mediante el selector del sistema. Guarda la imagen en la caché, la expone de forma segura con el FileProvider y lanza
un Intent ACTION_SEND con permiso temporal de lectura.
**/

// Carpeta de la caché que el FileProvider expone (ver res/xml/file_paths.xml).
private const val SHARED_DIR = "shared"

/**
Comparte una imagen con el selector del sistema (WhatsApp, correo, Drive...).

La imagen se guarda como PNG en la caché de la app, que Android puede limpiar sola,
y se entrega con un `content://` del FileProvider: la app que la recibe solo puede
leer ese archivo, no el resto de la caché. [text] se adjunta como mensaje opcional.
**/
suspend fun Context.shareImage(bitmap: Bitmap, fileName: String, text: String? = null) {
    // Escribir el archivo es trabajo de disco: fuera del hilo principal.
    val file = withContext(Dispatchers.IO) {
        // Crea (si no existe) la carpeta compartida dentro de la caché.
        val dir = File(cacheDir, SHARED_DIR).apply { mkdirs() }
        // Crea el archivo PNG y escribe en él la imagen sin pérdida (calidad 100).
        File(dir, "$fileName.png").also { target ->
            target.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }

    // Obtiene la Uri content:// segura del archivo a través del FileProvider.
    val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
    // Construye el Intent de envío.
    val intent = Intent(Intent.ACTION_SEND).apply {
        // Tipo de contenido que se comparte.
        type = "image/png"
        // Adjunta la imagen.
        putExtra(Intent.EXTRA_STREAM, uri)
        // Adjunta el texto solo si se proporcionó.
        if (text != null) putExtra(Intent.EXTRA_TEXT, text)
        // ClipData + permiso de lectura: así la app elegida puede abrir la imagen.
        clipData = ClipData.newRawUri(null, uri)
        // Concede permiso temporal de lectura sobre la Uri a la app receptora.
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    // Abre el selector del sistema para que la persona elija con qué app compartir.
    startActivity(Intent.createChooser(intent, null))
}
