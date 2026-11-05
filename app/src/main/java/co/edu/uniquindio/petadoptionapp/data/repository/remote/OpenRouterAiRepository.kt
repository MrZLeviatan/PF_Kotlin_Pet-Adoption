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
 */
@Singleton
class OpenRouterAiRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : AiAnalysisRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    override suspend fun analyze(input: AiPostInput): Result<AiPostAnalysis> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.OPENROUTER_API_KEY
        if (apiKey.isBlank()) return@withContext Result.failure(AiException.NotConfigured)
        val model = BuildConfig.OPENROUTER_MODEL.ifBlank { DEFAULT_MODEL }

        runCatching {
            val body = buildJsonObject {
                put("model", model)
                put("temperature", 0.2)
                put("response_format", buildJsonObject { put("type", "json_object") })
                put("messages", buildJsonArray {
                    add(buildJsonObject {
                        put("role", "system")
                        put("content", SYSTEM_PROMPT)
                    })
                    add(buildJsonObject {
                        put("role", "user")
                        put("content", userContent(input))
                    })
                })
            }
            val request = Request.Builder()
                .url("https://openrouter.ai/api/v1/chat/completions")
                .header("Authorization", "Bearer $apiKey")
                // Identifican la app en OpenRouter (opcionales).
                .header("HTTP-Referer", "https://github.com/MrZLeviatan")
                .header("X-Title", "KogMaw Adoption App")
                .post(body.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val content = client.newCall(request).execute().use { response ->
                val text = response.body?.string().orEmpty()
                if (!response.isSuccessful) throw AiException.AnalysisFailed
                json.decodeFromString<ChatResponse>(text).choices.firstOrNull()?.message?.content
                    ?: throw AiException.AnalysisFailed
            }
            parseAiAnswer(content, model)
        }.recoverCatching { throw if (it is AiException) it else AiException.AnalysisFailed }
    }

    // Texto con los datos de la publicación y las candidatas, y las fotos como imágenes.
    private fun userContent(input: AiPostInput): JsonArray = buildJsonArray {
        val post = input.post
        val text = buildString {
            appendLine("PUBLICACIÓN A REVISAR")
            appendLine("id: ${post.id}")
            appendLine("título: ${post.title}")
            appendLine("categoría: ${input.categoryName}")
            appendLine("descripción: ${post.description}")
            appendLine("tipo de animal: ${input.animalTypeName ?: "sin dato"}")
            appendLine("raza: ${post.breed.ifBlank { "sin dato" }}")
            appendLine("tamaño: ${post.size?.name ?: "sin dato"}")
            appendLine("edad en meses: ${post.ageInMonths ?: "sin dato"}")
            appendLine("vacunas al día: ${post.vaccinated ?: "sin dato"}; esterilizado: ${post.neutered ?: "sin dato"}")
            appendLine("ubicación: ${listOf(post.location.address, post.location.city).filter { it.isNotBlank() }.joinToString(", ")}")
            appendLine("número de fotos: ${post.photoUrls.size}")
            appendLine()
            appendLine("PUBLICACIONES PARECIDAS (posibles duplicados):")
            if (input.candidates.isEmpty()) appendLine("ninguna")
            input.candidates.forEach { appendLine("- id=${it.id} | autor=${if (it.authorId == post.authorId) "mismo autor" else "otro"} | ${it.title} | ${it.description.take(160)}") }
        }
        add(buildJsonObject { put("type", "text"); put("text", text) })
        post.photoUrls.take(MAX_IMAGES).mapNotNull { imageUrlFor(it) }.forEach { url ->
            add(buildJsonObject {
                put("type", "image_url")
                put("image_url", buildJsonObject { put("url", url) })
            })
        }
    }

    // Las fotos en Cloudinary se envían por URL; las que siguen en el teléfono, como data URL (base64).
    private fun imageUrlFor(url: String): String? = when {
        url.startsWith("http") -> url
        url.startsWith("file://") || url.startsWith("content://") -> runCatching {
            val bytes = context.contentResolver.openInputStream(Uri.parse(url))?.use { it.readBytes() } ?: return null
            "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
        }.getOrNull()
        else -> null
    }

    // --- Respuesta de OpenRouter ---

    @Serializable private data class ChatResponse(val choices: List<Choice> = emptyList())
    @Serializable private data class Choice(val message: ChatMessage? = null)
    @Serializable private data class ChatMessage(val content: String? = null)

    private companion object {
        // Modelo con visión y bajo costo; se cambia con OPENROUTER_MODEL en local.properties.
        const val DEFAULT_MODEL = "openai/gpt-4o-mini"
        const val MAX_IMAGES = 3

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
@Serializable
internal data class AiAnswer(
    val priority: String = "MEDIUM",
    @SerialName("priority_reason") val priorityReason: String = "",
    val summary: String = "",
    @SerialName("key_points") val keyPoints: List<String> = emptyList(),
    val relevance: Int = 50,
    val inconsistencies: List<String> = emptyList(),
    @SerialName("images_relevant") val imagesRelevant: Boolean? = null,
    @SerialName("image_notes") val imageNotes: String = "",
    val inappropriate: Boolean = false,
    @SerialName("inappropriate_reasons") val inappropriateReasons: List<String> = emptyList(),
    @SerialName("duplicate_of") val duplicateOf: String? = null,
    @SerialName("duplicate_reason") val duplicateReason: String = "",
    @SerialName("recommended_actions") val recommendedActions: List<String> = emptyList(),
) {
    fun toAnalysis(model: String) = AiPostAnalysis(
        review = AiReview(
            summary = summary.trim(),
            keyPoints = keyPoints.map(String::trim).filter(String::isNotBlank).take(6),
            relevance = relevance.coerceIn(0, 100),
            inconsistencies = inconsistencies.map(String::trim).filter(String::isNotBlank),
            imagesRelevant = imagesRelevant,
            imageNotes = imageNotes.trim(),
            inappropriate = inappropriate,
            inappropriateReasons = inappropriateReasons.map(String::trim).filter(String::isNotBlank),
            duplicateOfPostId = duplicateOf?.trim()?.takeIf { it.isNotBlank() && it != "null" },
            duplicateReason = duplicateReason.trim(),
            recommendedActions = recommendedActions.map(String::trim).filter(String::isNotBlank).take(5),
            model = model,
            analyzedAt = System.currentTimeMillis(),
        ),
        priority = runCatching { PriorityLevel.valueOf(priority.trim().uppercase()) }.getOrDefault(PriorityLevel.MEDIUM),
        priorityReason = priorityReason.trim(),
    )
}

private val answerJson = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

/** Convierte la respuesta de la IA (JSON, a veces dentro de ```json ... ```) en el análisis del dominio. */
internal fun parseAiAnswer(content: String, model: String): AiPostAnalysis {
    val clean = content.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
    return answerJson.decodeFromString<AiAnswer>(clean).toAnalysis(model)
}
