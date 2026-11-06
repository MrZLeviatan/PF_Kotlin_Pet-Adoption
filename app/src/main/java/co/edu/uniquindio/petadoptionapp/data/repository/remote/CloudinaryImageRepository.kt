package co.edu.uniquindio.petadoptionapp.data.repository.remote

import android.content.Context
import android.net.Uri
import co.edu.uniquindio.petadoptionapp.BuildConfig
import co.edu.uniquindio.petadoptionapp.domain.error.ImageException
import co.edu.uniquindio.petadoptionapp.domain.repository.ImageFolder
import co.edu.uniquindio.petadoptionapp.domain.repository.ImageStorageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
Sube imágenes a Cloudinary con su API REST y un "upload preset" sin firma (configurado en la cuenta).
El API secret nunca va en la app: solo el nombre de la nube y el preset, desde local.properties.
Todas quedan en el folder "KogMaw-Adoption App", con una subcarpeta por uso (posts, profiles, chats).

Implementa ImageStorageRepository del dominio (capa de datos, fuente remota) y se enlaza en
RepositoryModule. Recibe la URI local de la imagen elegida en la galería o cámara, la sube y
devuelve la URL https pública que luego se guarda en Firestore (foto de perfil, publicación o chat).
**/
// Una sola instancia: el cliente HTTP se reutiliza para todas las subidas.
@Singleton
// Hilt inyecta el Context de la aplicación para poder leer las URIs locales.
class CloudinaryImageRepository @Inject constructor(
    // Context de la aplicación, usado para abrir el contenido de la URI con el ContentResolver.
    @ApplicationContext private val context: Context,
) : ImageStorageRepository {

    // Cliente HTTP con tiempos de espera amplios porque las imágenes pueden pesar varios MB.
    private val client = OkHttpClient.Builder()
        // Máximo 20 s para establecer la conexión.
        .connectTimeout(20, TimeUnit.SECONDS)
        // Máximo 60 s esperando la respuesta del servidor.
        .readTimeout(60, TimeUnit.SECONDS)
        // Máximo 60 s enviando los bytes de la imagen.
        .writeTimeout(60, TimeUnit.SECONDS)
        // Construye el cliente con la configuración anterior.
        .build()

    // Lector JSON que ignora los muchos campos de la respuesta de Cloudinary que no se usan.
    private val json = Json { ignoreUnknownKeys = true }

    // Lo único que se lee de la respuesta: la URL https de la imagen.
    @Serializable
    private data class UploadResponse(val secure_url: String)

    // Sube la imagen a la carpeta indicada y devuelve su URL pública; todo el trabajo va en el hilo IO.
    override suspend fun upload(localUri: String, folder: ImageFolder): Result<String> = withContext(Dispatchers.IO) {
        // Lee el nombre de la nube de Cloudinary desde BuildConfig.
        val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
        // Lee el preset de subida sin firma desde BuildConfig.
        val preset = BuildConfig.CLOUDINARY_UPLOAD_PRESET
        // Si faltan las llaves en local.properties, avisa que Cloudinary no está configurado sin intentar la subida.
        if (cloudName.isBlank() || preset.isBlank()) return@withContext Result.failure(ImageException.NotConfigured)

        // Ejecuta la subida atrapando cualquier error en un Result.
        runCatching {
            // Lee todos los bytes de la imagen desde su URI local; si no se puede abrir, falla la subida.
            val bytes = context.contentResolver.openInputStream(Uri.parse(localUri))?.use { it.readBytes() }
                ?: throw ImageException.UploadFailed
            // Arma el cuerpo multipart que espera la API de Cloudinary.
            val body = MultipartBody.Builder()
                // Tipo formulario (multipart/form-data).
                .setType(MultipartBody.FORM)
                // Adjunta el archivo de la imagen como JPEG.
                .addFormDataPart("file", "image.jpg", bytes.toRequestBody("image/jpeg".toMediaType()))
                // Indica el preset sin firma que autoriza la subida.
                .addFormDataPart("upload_preset", preset)
                // Carpeta destino: la carpeta raíz de la app más la subcarpeta según el uso.
                .addFormDataPart("folder", "$ROOT_FOLDER/${folder.path}")
                // Construye el cuerpo.
                .build()
            // Arma la petición POST al endpoint de subida de imágenes de la nube configurada.
            val request = Request.Builder()
                // URL del endpoint de subida de Cloudinary.
                .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
                // Envía el cuerpo multipart por POST.
                .post(body)
                // Construye la petición.
                .build()
            // Ejecuta la petición de forma síncrona (ya se está en IO) y cierra la respuesta al terminar.
            client.newCall(request).execute().use { response ->
                // Lee el cuerpo de la respuesta como texto.
                val text = response.body?.string().orEmpty()
                // Si Cloudinary respondió con error, la subida falla.
                if (!response.isSuccessful) throw ImageException.UploadFailed
                // Extrae la URL https de la imagen subida; es el valor que devuelve la función.
                json.decodeFromString<UploadResponse>(text).secure_url
            }
        // Cualquier error que no sea del dominio (red, JSON) se traduce a UploadFailed para que la interfaz muestre un mensaje claro.
        }.recoverCatching { throw if (it is ImageException) it else ImageException.UploadFailed }
    }

    // Constantes privadas de la clase.
    private companion object {
        // Carpeta raíz en Cloudinary donde se guardan todas las imágenes de la app.
        const val ROOT_FOLDER = "KogMaw-Adoption App"
    }
}
