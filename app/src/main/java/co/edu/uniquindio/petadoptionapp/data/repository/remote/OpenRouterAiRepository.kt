package co.edu.uniquindio.petadoptionapp.data.repository.remote

import android.content.Context
import android.net.Uri
import android.util.Base64
import co.edu.uniquindio.petadoptionapp.BuildConfig
import co.edu.uniquindio.petadoptionapp.domain.error.AiException
import co.edu.uniquindio.petadoptionapp.domain.model.post.AiPostAnalysis
import co.edu.uniquindio.petadoptionapp.domain.model.post.AiReview
import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.model.post.PriorityLevel
import co.edu.uniquindio.petadoptionapp.domain.repository.AiAnalysisRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.AiPostInput
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
Análisis de publicaciones con IA mediante OpenRouter (API compatible con OpenAI).
Envía el texto de la publicación, hasta 3 fotos y las publicaciones parecidas, y pide un JSON con
prioridad, resumen, puntos clave, relevancia, inconsistencias, revisión de fotos, contenido inapropiado,
posible duplicado y acciones recomendadas. La llave y el modelo salen de local.properties.

Implementa AiAnalysisRepository del dominio (capa de datos, fuente remota) y se enlaza en
RepositoryModule. La invoca AnalyzePostUseCase (normalmente a través de
BackgroundPostAnalysisScheduler); el resultado (AiPostAnalysis) se guarda en la publicación para
ayudar al moderador a priorizar y revisar. La IA no aprueba ni rechaza: solo asiste.
**/
// Una sola instancia: el cliente HTTP y el lector JSON se reutilizan.
@Singleton
// Hilt inyecta el Context de la aplicación para poder leer fotos que aún están en el teléfono.
class OpenRouterAiRepository @Inject constructor(
    // Context de la aplicación, usado para abrir URIs locales de las fotos.
    @ApplicationContext private val context: Context,
) : AiAnalysisRepository {

    // Cliente HTTP con lectura larga (90 s) porque los modelos con imágenes pueden tardar en responder.
    private val client = OkHttpClient.Builder()
        // Máximo 20 s para conectar con OpenRouter.
        .connectTimeout(20, TimeUnit.SECONDS)
        // Máximo 90 s esperando la respuesta del modelo.
        .readTimeout(90, TimeUnit.SECONDS)
        // Máximo 60 s enviando la petición (puede incluir imágenes en base64).
        .writeTimeout(60, TimeUnit.SECONDS)
        // Construye el cliente.
        .build()

    // Lector JSON tolerante: ignora campos extra, acepta formato relajado y usa valores por defecto ante valores inválidos.
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    // Envía la publicación a la IA y devuelve el análisis ya convertido al modelo del dominio.
    override suspend fun analyze(input: AiPostInput): Result<AiPostAnalysis> = withContext(Dispatchers.IO) {
        // Lee la llave de la API desde BuildConfig.
        val apiKey = BuildConfig.OPENROUTER_API_KEY
        // Sin llave no se puede llamar a la IA: devuelve un error de configuración.
        if (apiKey.isBlank()) return@withContext Result.failure(AiException.NotConfigured)
        // Usa el modelo configurado en local.properties o, si está vacío, el modelo por defecto.
        val model = BuildConfig.OPENROUTER_MODEL.ifBlank { DEFAULT_MODEL }

        // Ejecuta la llamada atrapando cualquier error en un Result.
        runCatching {
            // Construye el cuerpo de la petición en el formato de chat de OpenAI.
            val body = buildJsonObject {
                // Modelo de IA que procesará la petición.
                put("model", model)
                // Temperatura baja para respuestas consistentes y poco creativas.
                put("temperature", 0.2)
                // Pide que la respuesta sea un objeto JSON válido.
                put("response_format", buildJsonObject { put("type", "json_object") })
                // Lista de mensajes de la conversación con el modelo.
                put("messages", buildJsonArray {
                    // Mensaje de sistema: instrucciones y formato de respuesta esperado.
                    add(buildJsonObject {
                        // Rol de sistema.
                        put("role", "system")
                        // Texto con las instrucciones de moderación.
                        put("content", SYSTEM_PROMPT)
                    })
                    // Mensaje de usuario: los datos de la publicación y sus fotos.
                    add(buildJsonObject {
                        // Rol de usuario.
                        put("role", "user")
                        // Contenido mixto (texto + imágenes) armado por userContent.
                        put("content", userContent(input))
                    })
                })
            }
            // Arma la petición HTTP al endpoint de chat de OpenRouter.
            val request = Request.Builder()
                // URL del endpoint compatible con OpenAI.
                .url("https://openrouter.ai/api/v1/chat/completions")
                // Autenticación con la llave de la API.
                .header("Authorization", "Bearer $apiKey")
                // Identifican la app en OpenRouter (opcionales).
                .header("HTTP-Referer", "https://github.com/MrZLeviatan")
                // Nombre de la app que aparece en el panel de OpenRouter.
                .header("X-Title", "KogMaw Adoption App")
                // Envía el JSON por POST.
                .post(body.toString().toRequestBody("application/json".toMediaType()))
                // Construye la petición.
                .build()

            // Ejecuta la petición y extrae el texto de la respuesta del modelo.
            val content = client.newCall(request).execute().use { response ->
                // Lee el cuerpo completo de la respuesta.
                val text = response.body?.string().orEmpty()
                // Si OpenRouter respondió con error (llave inválida, sin saldo, etc.), el análisis falla.
                if (!response.isSuccessful) throw AiException.AnalysisFailed
                // Toma el contenido del primer mensaje devuelto; si no hay, el análisis falla.
                json.decodeFromString<ChatResponse>(text).choices.firstOrNull()?.message?.content
                    ?: throw AiException.AnalysisFailed
            }
            // Convierte el JSON devuelto por la IA en el análisis del dominio.
            parseAiAnswer(content, model)
        // Cualquier error que no sea del dominio (red, JSON mal formado) se traduce a AnalysisFailed.
        }.recoverCatching { throw if (it is AiException) it else AiException.AnalysisFailed }
    }

    // Texto con los datos de la publicación y las candidatas, y las fotos como imágenes.
    private fun userContent(input: AiPostInput): JsonArray = buildJsonArray {
        // Publicación que se va a revisar.
        val post = input.post
        // Arma un texto legible con todos los datos relevantes de la publicación.
        val text = buildString {
            // Encabezado de la sección principal.
            appendLine("PUBLICACIÓN A REVISAR")
            // Identificador de la publicación.
            appendLine("id: ${post.id}")
            // Título.
            appendLine("título: ${post.title}")
            // Nombre legible de la categoría (no su id).
            appendLine("categoría: ${input.categoryName}")
            // Descripción completa.
            appendLine("descripción: ${post.description}")
            // Tipo de animal o "sin dato" si no se indicó.
            appendLine("tipo de animal: ${input.animalTypeName ?: "sin dato"}")
            // Raza o "sin dato" si está vacía.
            appendLine("raza: ${post.breed.ifBlank { "sin dato" }}")
            // Tamaño o "sin dato".
            appendLine("tamaño: ${post.size?.name ?: "sin dato"}")
            // Edad en meses o "sin dato".
            appendLine("edad en meses: ${post.ageInMonths ?: "sin dato"}")
            // Estado de vacunación y esterilización.
            appendLine("vacunas al día: ${post.vaccinated ?: "sin dato"}; esterilizado: ${post.neutered ?: "sin dato"}")
            // Dirección y ciudad, omitiendo las partes vacías.
            appendLine("ubicación: ${listOf(post.location.address, post.location.city).filter { it.isNotBlank() }.joinToString(", ")}")
            // Cantidad de fotos, para que la IA sepa si faltan imágenes.
            appendLine("número de fotos: ${post.photoUrls.size}")
            // Línea en blanco para separar secciones.
            appendLine()
            // Encabezado de la sección de posibles duplicados.
            appendLine("PUBLICACIONES PARECIDAS (posibles duplicados):")
            // Si no hay candidatas, lo indica explícitamente.
            if (input.candidates.isEmpty()) appendLine("ninguna")
            // Escribe una línea resumida por candidata, indicando si es del mismo autor y recortando la descripción a 160 caracteres.
            input.candidates.forEach { appendLine("- id=${it.id} | autor=${if (it.authorId == post.authorId) "mismo autor" else "otro"} | ${it.title} | ${it.description.take(160)}") }
        }
        // Agrega el bloque de texto al contenido del mensaje.
        add(buildJsonObject { put("type", "text"); put("text", text) })
        // Toma como máximo MAX_IMAGES fotos, descarta las que no se puedan enviar y agrega cada una como imagen.
        post.photoUrls.take(MAX_IMAGES).mapNotNull { imageUrlFor(it) }.forEach { url ->
            // Bloque de imagen en el formato de OpenAI.
            add(buildJsonObject {
                // Tipo de contenido: imagen por URL.
                put("type", "image_url")
                // URL (https o data URL) de la imagen.
                put("image_url", buildJsonObject { put("url", url) })
            })
        }
    }

    // Las fotos en Cloudinary se envían por URL; las que siguen en el teléfono, como data URL (base64).
    private fun imageUrlFor(url: String): String? = when {
        // Si ya es una URL web, se envía tal cual.
        url.startsWith("http") -> url
        // Si es un archivo local, se lee y se codifica en base64.
        url.startsWith("file://") || url.startsWith("content://") -> runCatching {
            // Lee los bytes de la imagen; si no se puede abrir, la foto se omite.
            val bytes = context.contentResolver.openInputStream(Uri.parse(url))?.use { it.readBytes() } ?: return null
            // Construye la data URL JPEG en base64 sin saltos de línea.
            "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
        // Si la lectura falla, la foto se omite.
        }.getOrNull()
        // Cualquier otro formato no se puede enviar.
        else -> null
    }

    // --- Respuesta de OpenRouter ---

    // Respuesta del endpoint de chat: solo interesa la lista de opciones.
    @Serializable private data class ChatResponse(val choices: List<Choice> = emptyList())
    // Cada opción trae un mensaje del modelo.
    @Serializable private data class Choice(val message: ChatMessage? = null)
    // El mensaje trae el contenido de texto (el JSON del análisis).
    @Serializable private data class ChatMessage(val content: String? = null)

    // Constantes privadas de la clase.
    private companion object {
        // Modelo con visión y bajo costo; se cambia con OPENROUTER_MODEL en local.properties.
        const val DEFAULT_MODEL = "openai/gpt-4o-mini"
        // Máximo de fotos que se envían a la IA, para limitar el costo y el tiempo de respuesta.
        const val MAX_IMAGES = 3

        // Instrucciones de sistema para la IA: su rol, el formato JSON exacto de respuesta y los criterios de prioridad.
        val SYSTEM_PROMPT = """
            Eres un asistente de moderación de una app de adopción y rescate de mascotas en Colombia.
            Revisas una publicación ANTES de que un moderador humano la apruebe. No apruebas ni rechazas: ayudas a priorizar y revisar rápido.
            Responde SOLO con un objeto JSON válido, en español, con exactamente estas claves:
            {
              "priority": "URGENT" | "HIGH" | "MEDIUM" | "LOW",
              "priority_reason": "una frase corta con el motivo de la prioridad",
              "summary": "resumen de 1 a 2 frases para el moderador",
              "key_points": ["puntos clave, máximo 5"],
              "relevance": número de 0 a 100 (qué tanto ayuda a un animal en necesidad y qué tan completa es la información),
              "inconsistencies": ["datos que se contradicen entre título, descripción, categoría, edad, tamaño, tipo de animal o fotos; vacío si no hay"],
              "images_relevant": true | false | null (null si no hay fotos),
              "image_notes": "qué muestran las fotos y si corresponden al animal descrito",
              "inappropriate": true | false,
              "inappropriate_reasons": ["venta o cobro por animales, maltrato explícito, contenido sexual o violento, spam, insultos, datos personales expuestos...; vacío si no hay"],
              "duplicate_of": "id de una de las PUBLICACIONES PARECIDAS si es la misma mascota/caso, o null",
              "duplicate_reason": "por qué parece duplicada; vacío si no",
              "recommended_actions": ["acciones concretas para el moderador, máximo 4"]
            }
            Criterios de prioridad: URGENT = animal herido, enfermo o en peligro inmediato; HIGH = animales muy jóvenes,
            perdidos recientemente o en riesgo; MEDIUM = adopción u hogar temporal sin urgencia; LOW = eventos, información general.
            Si la publicación ofrece animales a cambio de dinero, márcala como inapropiada.
        """.trimIndent()
    }
}

// JSON que se le pide a la IA (ver SYSTEM_PROMPT). Los nombres siguen el formato snake_case del prompt.
// Cada campo tiene un valor por defecto para que una respuesta incompleta no rompa el análisis.
@Serializable
internal data class AiAnswer(
    // Prioridad sugerida como texto (URGENT, HIGH, MEDIUM, LOW).
    val priority: String = "MEDIUM",
    // Motivo de la prioridad.
    @SerialName("priority_reason") val priorityReason: String = "",
    // Resumen para el moderador.
    val summary: String = "",
    // Puntos clave de la publicación.
    @SerialName("key_points") val keyPoints: List<String> = emptyList(),
    // Relevancia de 0 a 100.
    val relevance: Int = 50,
    // Datos contradictorios detectados.
    val inconsistencies: List<String> = emptyList(),
    // Si las fotos corresponden al animal (null si no hay fotos).
    @SerialName("images_relevant") val imagesRelevant: Boolean? = null,
    // Descripción de lo que muestran las fotos.
    @SerialName("image_notes") val imageNotes: String = "",
    // Si el contenido es inapropiado.
    val inappropriate: Boolean = false,
    // Motivos por los que es inapropiado.
    @SerialName("inappropriate_reasons") val inappropriateReasons: List<String> = emptyList(),
    // Id de la publicación de la que podría ser duplicado.
    @SerialName("duplicate_of") val duplicateOf: String? = null,
    // Motivo de la sospecha de duplicado.
    @SerialName("duplicate_reason") val duplicateReason: String = "",
    // Acciones recomendadas al moderador.
    @SerialName("recommended_actions") val recommendedActions: List<String> = emptyList(),
) {
    // Convierte la respuesta cruda de la IA en el modelo del dominio, limpiando y acotando los valores.
    fun toAnalysis(model: String) = AiPostAnalysis(
        // Revisión detallada para el moderador.
        review = AiReview(
            // Resumen sin espacios sobrantes.
            summary = summary.trim(),
            // Puntos clave sin vacíos, máximo 6.
            keyPoints = keyPoints.map(String::trim).filter(String::isNotBlank).take(6),
            // Relevancia forzada al rango 0-100 por si la IA devuelve un valor fuera de rango.
            relevance = relevance.coerceIn(0, 100),
            // Inconsistencias sin vacíos.
            inconsistencies = inconsistencies.map(String::trim).filter(String::isNotBlank),
            // Relevancia de las imágenes tal cual.
            imagesRelevant = imagesRelevant,
            // Notas de imagen sin espacios sobrantes.
            imageNotes = imageNotes.trim(),
            // Bandera de contenido inapropiado.
            inappropriate = inappropriate,
            // Motivos de inapropiado sin vacíos.
            inappropriateReasons = inappropriateReasons.map(String::trim).filter(String::isNotBlank),
            // Id del duplicado; se descarta si viene vacío o como el texto "null".
            duplicateOfPostId = duplicateOf?.trim()?.takeIf { it.isNotBlank() && it != "null" },
            // Motivo del duplicado sin espacios sobrantes.
            duplicateReason = duplicateReason.trim(),
            // Acciones recomendadas sin vacíos, máximo 5.
            recommendedActions = recommendedActions.map(String::trim).filter(String::isNotBlank).take(5),
            // Modelo que hizo el análisis, para trazabilidad.
            model = model,
            // Momento del análisis en milisegundos.
            analyzedAt = System.currentTimeMillis(),
        ),
        // Convierte el texto de prioridad al enum; si no es válido, usa MEDIUM.
        priority = runCatching { PriorityLevel.valueOf(priority.trim().uppercase()) }.getOrDefault(PriorityLevel.MEDIUM),
        // Motivo de la prioridad sin espacios sobrantes.
        priorityReason = priorityReason.trim(),
    )
}

// Lector JSON tolerante para la respuesta de la IA (independiente de la instancia del repositorio, para poder probarlo).
private val answerJson = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

/**
Convierte la respuesta de la IA (JSON, a veces dentro de ```json ... ```) en el análisis del dominio.
Es `internal` y de nivel superior para poder usarla y probarla sin crear el repositorio.
**/
internal fun parseAiAnswer(content: String, model: String): AiPostAnalysis {
    // Quita los delimitadores de bloque de código Markdown que algunos modelos agregan alrededor del JSON.
    val clean = content.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
    // Decodifica el JSON limpio y lo convierte al modelo del dominio.
    return answerJson.decodeFromString<AiAnswer>(clean).toAnalysis(model)
}
