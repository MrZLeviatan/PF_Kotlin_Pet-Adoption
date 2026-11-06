package co.edu.uniquindio.petadoptionapp.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
Fotos elegidas en el teléfono (cámara o galería), guardadas en la carpeta privada de la app.
Se copian (no se guarda el enlace a la galería) porque ese permiso de lectura se pierde al cerrar la app.
Se reducen a un lado máximo de 1600 px en JPEG: ocupan mucho menos y se ven igual en pantalla.
Lo usa [ImageSourcePicker] (paquete photo): crea el archivo donde la cámara escribe la foto, copia la imagen elegida
y borra los temporales. Las rutas resultantes ("file://…") las puede mostrar Coil directamente.
TODO (Cloudinary): subir el archivo y guardar su URL en lugar de la ruta local.
**/

// Subcarpeta dentro de filesDir donde se guardan las fotos.
private const val PHOTOS_DIR = "photos"
// Lado más largo permitido, en píxeles, de las fotos guardadas.
private const val MAX_SIDE_PX = 1600
// Calidad de compresión JPEG (0-100): buen equilibrio entre peso y calidad.
private const val JPEG_QUALITY = 85

// Archivo nuevo (vacío) donde la cámara deja la foto, y su Uri para dársela a la app de cámara.
fun newCameraPhoto(context: Context): Pair<File, Uri> {
    // Crea la carpeta de fotos si no existe y define un archivo con nombre único.
    val file = File(File(context.filesDir, PHOTOS_DIR).apply { mkdirs() }, "camera_${UUID.randomUUID()}.jpg")
    // Genera una Uri content:// segura con el FileProvider, para que la app de cámara pueda escribir en el archivo.
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    // Devuelve el archivo y su Uri juntos.
    return file to uri
}

/**
Copia la imagen de [source] (galería o archivo de la cámara) a la carpeta de la app, enderezada y reducida.
Devuelve su dirección ("file://…"), que Coil sabe mostrar; null si no se pudo leer.
Se ejecuta en el despachador de entrada/salida porque lee y escribe en disco.
**/
suspend fun saveImageLocally(context: Context, source: Uri): String? = withContext(Dispatchers.IO) {
    // Cualquier error de lectura o escritura se convierte en null.
    runCatching {
        // Resolvedor de contenido para abrir la Uri (galería o archivo).
        val resolver = context.contentResolver
        // 1. Tamaño original sin cargar la imagen, para leerla ya reducida (cuida la memoria).
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        // Lee solo las dimensiones de la imagen.
        resolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        // Si no se pudieron leer las dimensiones, no es una imagen válida.
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null
        // Factor de submuestreo (potencia de 2) para decodificar la imagen ya reducida.
        var sample = 1
        // Duplica el factor mientras la imagen reducida siga siendo mayor o igual al máximo.
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE_PX) sample *= 2
        // Decodifica la imagen con el submuestreo calculado; si falla, termina con null.
        val decoded = resolver.openInputStream(source)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return@runCatching null

        // 2. Las fotos de cámara traen la rotación aparte (EXIF): se aplica para que no salgan de lado.
        val rotation = resolver.openInputStream(source)?.use {
            // Lee la orientación EXIF y la traduce a grados.
            when (ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f

        // 3. Ajuste final al tamaño máximo.
        val scale = minOf(1f, MAX_SIDE_PX.toFloat() / maxOf(decoded.width, decoded.height))
        // Matriz de transformación con la escala y la rotación necesarias.
        val matrix = Matrix().apply {
            // Solo reduce si la imagen sigue siendo más grande que el máximo.
            if (scale < 1f) postScale(scale, scale)
            // Solo rota si la foto lo necesita.
            if (rotation != 0f) postRotate(rotation)
        }
        // Si no hay transformación, usa la imagen tal cual; si no, crea una nueva imagen transformada.
        val bitmap = if (matrix.isIdentity) decoded else Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)

        // Archivo de destino con nombre único en la carpeta de fotos.
        val target = File(File(context.filesDir, PHOTOS_DIR).apply { mkdirs() }, "${UUID.randomUUID()}.jpg")
        // Comprime la imagen en JPEG y la escribe en el archivo.
        target.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
        // Devuelve la ruta del archivo como texto "file://…".
        Uri.fromFile(target).toString()
    }.getOrNull()
}

// Borra el archivo temporal de la cámara cuando ya se copió (o si se canceló la foto).
fun deleteQuietly(file: File?) {
    // Intenta borrarlo ignorando cualquier error (por eso "quietly").
    runCatching { file?.delete() }
}
