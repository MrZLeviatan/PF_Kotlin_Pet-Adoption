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

Es un servicio auxiliar de la capa de datos (no implementa una interfaz del dominio): lo usa
FirebaseAuthRepository, que genera el código, lo guarda en Firestore y delega aquí su envío.
**/
// Una sola instancia: el cliente HTTP se reutiliza para cada envío.
@Singleton
// Hilt lo construye sin dependencias externas.
class EmailJsCodeSender @Inject constructor() {

    // Cliente HTTP con tiempos de espera cortos: el cuerpo es pequeño y el usuario está esperando el código.
    private val client = OkHttpClient.Builder()
        // Máximo 15 s para conectar con EmailJS.
        .connectTimeout(15, TimeUnit.SECONDS)
        // Máximo 20 s esperando la respuesta.
        .readTimeout(20, TimeUnit.SECONDS)
        // Construye el cliente.
        .build()

    // Indica si las llaves obligatorias de EmailJS están configuradas; se calcula cada vez que se consulta.
    val isConfigured: Boolean
        // Es verdadero solo si el servicio, la plantilla y la llave pública tienen valor.
        get() = BuildConfig.EMAILJS_SERVICE_ID.isNotBlank() &&
                BuildConfig.EMAILJS_TEMPLATE_ID.isNotBlank() &&
                BuildConfig.EMAILJS_PUBLIC_KEY.isNotBlank()

    // Envía el código A2F al correo del usuario; devuelve éxito o el error ocurrido.
    suspend fun sendTwoFactorCode(
        // Correo destino.
        toEmail: String,
        // Nombre con el que se saluda en el correo.
        userName: String,
        // Código de verificación generado.
        code: String,
        // Minutos de vigencia del código, para mostrarlos en el correo.
        minutes: Long
    // Ejecuta todo en el hilo IO porque hace una llamada de red.
    ): Result<Unit> = withContext(Dispatchers.IO) {
        // Si faltan llaves, falla de inmediato con un mensaje claro en vez de intentar el envío.
        if (!isConfigured) return@withContext Result.failure(IOException("EmailJS no está configurado"))
        // Ejecuta el envío atrapando cualquier error en un Result.
        runCatching {
            // Construye el JSON que espera la API de EmailJS.
            val body = buildJsonObject {
                // Servicio de correo configurado en EmailJS.
                put("service_id", BuildConfig.EMAILJS_SERVICE_ID)
                // Plantilla del correo.
                put("template_id", BuildConfig.EMAILJS_TEMPLATE_ID)
                // Llave pública de la cuenta (EmailJS la llama user_id).
                put("user_id", BuildConfig.EMAILJS_PUBLIC_KEY)
                // Solo si en EmailJS se activó "Use Private Key".
                if (BuildConfig.EMAILJS_PRIVATE_KEY.isNotBlank()) put("accessToken", BuildConfig.EMAILJS_PRIVATE_KEY)
                // Variables que la plantilla reemplaza en el texto del correo.
                put("template_params", buildJsonObject {
                    // Destinatario.
                    put("to_email", toEmail)
                    // Nombre del usuario.
                    put("user_name", userName)
                    // Código A2F.
                    put("code", code)
                    // Vigencia en minutos, como texto.
                    put("minutes", minutes.toString())
                })
            }
            // Arma la petición POST al endpoint de envío de EmailJS.
            val request = Request.Builder()
                // URL de la API REST de EmailJS para enviar correos.
                .url("https://api.emailjs.com/api/v1.0/email/send")
                // Envía el JSON como cuerpo con tipo application/json.
                .post(body.toString().toRequestBody("application/json".toMediaType()))
                // Construye la petición.
                .build()
            // Ejecuta la petición y cierra la respuesta al terminar.
            client.newCall(request).execute().use { response ->
                // EmailJS responde 200 "OK"; cualquier otra cosa explica el error en el cuerpo.
                if (!response.isSuccessful) throw IOException("EmailJS ${response.code}: ${response.body?.string().orEmpty()}")
            }
        // Si algo falla, lo registra en Logcat para poder diagnosticarlo, sin ocultar el error al llamador.
        }.onFailure { Log.w(TAG, "No se pudo enviar el código A2F", it) }
    }

    // Constantes privadas de la clase.
    private companion object {
        // Filtro para Logcat: el motivo exacto del fallo (llave, plantilla, red).
        const val TAG = "EmailJsCodeSender"
    }
}
