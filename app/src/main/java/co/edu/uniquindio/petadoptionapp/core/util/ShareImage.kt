package co.edu.uniquindio.petadoptionapp.core.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Carpeta de la caché que el FileProvider expone (ver res/xml/file_paths.xml).
private const val SHARED_DIR = "shared"

/**
 Comparte una imagen con el selector del sistema (WhatsApp, correo, Drive...).

 La imagen se guarda como PNG en la caché de la app, que Android puede limpiar sola,
 y se entrega con un `content://` del FileProvider: la app que la recibe solo puede
 leer ese archivo, no el resto de la caché. [text] se adjunta como mensaje opcional.
 */
suspend fun Context.shareImage(bitmap: Bitmap, fileName: String, text: String? = null) {
    // Escribir el archivo es trabajo de disco: fuera del hilo principal.
    val file = withContext(Dispatchers.IO) {
        val dir = File(cacheDir, SHARED_DIR).apply { mkdirs() }
        File(dir, "$fileName.png").also { target ->
            target.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }

    val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        if (text != null) putExtra(Intent.EXTRA_TEXT, text)
        // ClipData + permiso de lectura: así la app elegida puede abrir la imagen.
        clipData = ClipData.newRawUri(null, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startActivity(Intent.createChooser(intent, null))
}
