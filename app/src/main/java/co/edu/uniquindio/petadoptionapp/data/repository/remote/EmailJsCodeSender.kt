package co.edu.uniquindio.petadoptionapp.data.repository.remote

import android.util.Log
import co.edu.uniquindio.petadoptionapp.BuildConfig
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
Envía el código de la verificación en dos pasos (A2F) por correo con EmailJS (plan gratuito, sin servidor).

La plantilla de EmailJS recibe `to_email`, `user_name`, `code` y `minutes`. Las llaves salen de
local.properties. EmailJS debe tener activado "Allow EmailJS API for non-browser applications".
 */
@Singleton
class EmailJsCodeSender @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    val isConfigured: Boolean
        get() = BuildConfig.EMAILJS_SERVICE_ID.isNotBlank() &&
                BuildConfig.EMAILJS_TEMPLATE_ID.isNotBlank() &&
                BuildConfig.EMAILJS_PUBLIC_KEY.isNotBlank()

    suspend fun sendTwoFactorCode(
        toEmail: String,
        userName: String,
        code: String,
        minutes: Long
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext Result.failure(IOException("EmailJS no está configurado"))
        runCatching {
            val body = buildJsonObject {
                put("service_id", BuildConfig.EMAILJS_SERVICE_ID)
                put("template_id", BuildConfig.EMAILJS_TEMPLATE_ID)
                put("user_id", BuildConfig.EMAILJS_PUBLIC_KEY)
                // Solo si en EmailJS se activó "Use Private Key".
                if (BuildConfig.EMAILJS_PRIVATE_KEY.isNotBlank()) put("accessToken", BuildConfig.EMAILJS_PRIVATE_KEY)
                put("template_params", buildJsonObject {
                    put("to_email", toEmail)
                    put("user_name", userName)
                    put("code", code)
                    put("minutes", minutes.toString())
                })
            }
            val request = Request.Builder()
                .url("https://api.emailjs.com/api/v1.0/email/send")
                .post(body.toString().toRequestBody("application/json".toMediaType()))
                .build()
            client.newCall(request).execute().use { response ->
                // EmailJS responde 200 "OK"; cualquier otra cosa explica el error en el cuerpo.
                if (!response.isSuccessful) throw IOException("EmailJS ${response.code}: ${response.body?.string().orEmpty()}")
            }
        }.onFailure { Log.w(TAG, "No se pudo enviar el código A2F", it) }
    }

    private companion object {
        // Filtro para Logcat: el motivo exacto del fallo (llave, plantilla, red).
        const val TAG = "EmailJsCodeSender"
    }
}
