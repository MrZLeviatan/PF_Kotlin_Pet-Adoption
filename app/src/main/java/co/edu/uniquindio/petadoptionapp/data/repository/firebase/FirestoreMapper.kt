package co.edu.uniquindio.petadoptionapp.data.repository.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

/**
Convierte los modelos del dominio en documentos de Firestore y viceversa.

Los modelos son `@Serializable` con `val` y valores por defecto. Firestore, por su cuenta, pide
`var` y setters; por eso aquí se pasa por JSON (kotlinx.serialization), que sí respeta los `val`,
los enums (se guardan como texto) y los campos opcionales.
 */
object FirestoreMapper {

    // Escribe también los valores por defecto (las consultas filtran por ellos) e ignora campos que el modelo no tiene.
    val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    // JSON → tipos de Firestore (String, Long, Double, Boolean, List, Map).
    fun fromJson(element: JsonElement): Any? = when (element) {
        is JsonNull -> null
        is JsonObject -> element.mapValues { fromJson(it.value) }
        is JsonArray -> element.map { fromJson(it) }
        is JsonPrimitive -> when {
            element.isString -> element.content
            element.booleanOrNull != null -> element.booleanOrNull
            // Sin punto ni exponente es entero; Firestore lo guarda como Long.
            element.content.any { it == '.' || it == 'e' || it == 'E' } -> element.content.toDouble()
            else -> element.content.toLong()
        }
    }

    // Tipos de Firestore → JSON.
    fun toJson(value: Any?): JsonElement = when (value) {
        null -> JsonNull
        is String -> JsonPrimitive(value)
        is Boolean -> JsonPrimitive(value)
        is Number -> JsonPrimitive(value)
        is Timestamp -> JsonPrimitive(value.toDate().time)
        is Map<*, *> -> JsonObject(value.entries.associate { (k, v) -> k.toString() to toJson(v) })
        is List<*> -> JsonArray(value.map { toJson(it) })
        else -> JsonPrimitive(value.toString())
    }
}

// Modelo → mapa listo para `set()`.
@Suppress("UNCHECKED_CAST")
inline fun <reified T> T.toFirestore(): Map<String, Any?> =
    FirestoreMapper.fromJson(FirestoreMapper.json.encodeToJsonElement(this)) as Map<String, Any?>

// Mapa leído de Firestore → modelo; null si no se puede leer (un documento mal formado no tumba la pantalla).
inline fun <reified T> Map<String, Any?>.toModel(): T? =
    runCatching { FirestoreMapper.json.decodeFromJsonElement<T>(FirestoreMapper.toJson(this)) }.getOrNull()

// Documento → modelo; null si no existe.
inline fun <reified T> DocumentSnapshot.toModel(): T? = data?.toModel<T>()

// Todos los documentos de una consulta que se pudieron leer.
inline fun <reified T> QuerySnapshot.toModels(): List<T> = documents.mapNotNull { it.toModel<T>() }
