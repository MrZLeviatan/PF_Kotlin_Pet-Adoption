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

/*
 Fotos elegidas en el teléfono (cámara o galería), guardadas en la carpeta privada de la app.
 Se copian (no se guarda el enlace a la galería) porque ese permiso de lectura se pierde al cerrar la app.
 Se reducen a un lado máximo de 1600 px en JPEG: ocupan mucho menos y se ven igual en pantalla.
 TODO (Cloudinary): subir el archivo y guardar su URL en lugar de la ruta local.
 */

private const val PHOTOS_DIR = "photos"
private const val MAX_SIDE_PX = 1600
private const val JPEG_QUALITY = 85

// Archivo nuevo (vacío) donde la cámara deja la foto, y su Uri para dársela a la app de cámara.
fun newCameraPhoto(context: Context): Pair<File, Uri> {
    val file = File(File(context.filesDir, PHOTOS_DIR).apply { mkdirs() }, "camera_${UUID.randomUUID()}.jpg")
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    return file to uri
}

/**
 Copia la imagen de [source] (galería o archivo de la cámara) a la carpeta de la app, enderezada y reducida.
 Devuelve su dirección ("file://…"), que Coil sabe mostrar; null si no se pudo leer.
 */
suspend fun saveImageLocally(context: Context, source: Uri): String? = withContext(Dispatchers.IO) {
    runCatching {
        val resolver = context.contentResolver
        // 1. Tamaño original sin cargar la imagen, para leerla ya reducida (cuida la memoria).
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE_PX) sample *= 2
        val decoded = resolver.openInputStream(source)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return@runCatching null

        // 2. Las fotos de cámara traen la rotación aparte (EXIF): se aplica para que no salgan de lado.
        val rotation = resolver.openInputStream(source)?.use {
            when (ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f

        // 3. Ajuste final al tamaño máximo.
        val scale = minOf(1f, MAX_SIDE_PX.toFloat() / maxOf(decoded.width, decoded.height))
        val matrix = Matrix().apply {
            if (scale < 1f) postScale(scale, scale)
            if (rotation != 0f) postRotate(rotation)
        }
        val bitmap = if (matrix.isIdentity) decoded else Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)

        val target = File(File(context.filesDir, PHOTOS_DIR).apply { mkdirs() }, "${UUID.randomUUID()}.jpg")
        target.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
        Uri.fromFile(target).toString()
    }.getOrNull()
}

// Borra el archivo temporal de la cámara cuando ya se copió (o si se canceló la foto).
fun deleteQuietly(file: File?) {
    runCatching { file?.delete() }
}
