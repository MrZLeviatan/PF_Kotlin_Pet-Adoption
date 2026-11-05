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
 */
@Singleton
class CloudinaryImageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : ImageStorageRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    // Lo único que se lee de la respuesta: la URL https de la imagen.
    @Serializable
    private data class UploadResponse(val secure_url: String)

    override suspend fun upload(localUri: String, folder: ImageFolder): Result<String> = withContext(Dispatchers.IO) {
        val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
        val preset = BuildConfig.CLOUDINARY_UPLOAD_PRESET
        if (cloudName.isBlank() || preset.isBlank()) return@withContext Result.failure(ImageException.NotConfigured)

        runCatching {
            val bytes = context.contentResolver.openInputStream(Uri.parse(localUri))?.use { it.readBytes() }
                ?: throw ImageException.UploadFailed
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "image.jpg", bytes.toRequestBody("image/jpeg".toMediaType()))
                .addFormDataPart("upload_preset", preset)
                .addFormDataPart("folder", "$ROOT_FOLDER/${folder.path}")
                .build()
            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
                .post(body)
                .build()
            client.newCall(request).execute().use { response ->
                val text = response.body?.string().orEmpty()
                if (!response.isSuccessful) throw ImageException.UploadFailed
                json.decodeFromString<UploadResponse>(text).secure_url
            }
        }.recoverCatching { throw if (it is ImageException) it else ImageException.UploadFailed }
    }

    private companion object {
        const val ROOT_FOLDER = "KogMaw-Adoption App"
    }
}
