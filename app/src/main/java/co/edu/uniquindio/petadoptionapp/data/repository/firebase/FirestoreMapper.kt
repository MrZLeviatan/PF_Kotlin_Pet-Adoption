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

Es la pieza de traducción de la capa de datos: los repositorios Firestore* llaman a
`toFirestore()` antes de escribir y a `toModel()`/`toModels()` al leer, de modo que el dominio
nunca depende de clases de Firebase. El camino es: modelo -> JsonElement -> Map de Firestore,
y al revés: Map de Firestore -> JsonElement -> modelo.
**/
// Se usa un object porque el mapeador no tiene estado y se comparte en toda la capa de datos.
object FirestoreMapper {

    // Configuración JSON común para todas las conversiones.
    val json = Json {
        // Escribe también los valores por defecto (las consultas filtran por ellos).
        encodeDefaults = true
        // Ignora campos del documento que el modelo no tiene (por ejemplo, campos antiguos).
        ignoreUnknownKeys = true
        // Si un valor no es válido (como un enum desconocido o un null en campo no nulo), usa el valor por defecto.
        coerceInputValues = true
    }

    // JSON → tipos de Firestore (String, Long, Double, Boolean, List, Map).
    fun fromJson(element: JsonElement): Any? = when (element) {
        // El null de JSON se guarda como null en Firestore.
        is JsonNull -> null
        // Un objeto JSON se convierte en mapa, convirtiendo cada valor de forma recursiva.
        is JsonObject -> element.mapValues { fromJson(it.value) }
        // Un arreglo JSON se convierte en lista, convirtiendo cada elemento de forma recursiva.
        is JsonArray -> element.map { fromJson(it) }
        // Un valor primitivo se convierte según su tipo real.
        is JsonPrimitive -> when {
            // Si es texto, se guarda como String.
            element.isString -> element.content
            // Si es true o false, se guarda como Boolean.
            element.booleanOrNull != null -> element.booleanOrNull
            // Sin punto ni exponente es entero; Firestore lo guarda como Long. Con ellos, es decimal (Double).
            element.content.any { it == '.' || it == 'e' || it == 'E' } -> element.content.toDouble()
            // En cualquier otro caso es un número entero.
            else -> element.content.toLong()
        }
    }

    // Tipos de Firestore → JSON.
    fun toJson(value: Any?): JsonElement = when (value) {
        // Un campo nulo se representa como null de JSON.
        null -> JsonNull
        // El texto se mantiene como texto.
        is String -> JsonPrimitive(value)
        // Los booleanos se mantienen como booleanos.
        is Boolean -> JsonPrimitive(value)
        // Long y Double se mantienen como números.
        is Number -> JsonPrimitive(value)
        // Las fechas de Firestore se pasan a milisegundos, que es como las guardan los modelos.
        is Timestamp -> JsonPrimitive(value.toDate().time)
        // Un mapa se convierte en objeto JSON, convirtiendo cada valor de forma recursiva.
        is Map<*, *> -> JsonObject(value.entries.associate { (k, v) -> k.toString() to toJson(v) })
        // Una lista se convierte en arreglo JSON, convirtiendo cada elemento de forma recursiva.
        is List<*> -> JsonArray(value.map { toJson(it) })
        // Cualquier otro tipo (GeoPoint, referencias...) se guarda como su texto para no fallar.
        else -> JsonPrimitive(value.toString())
    }
}

// Modelo → mapa listo para `set()`. El cast es seguro porque un modelo siempre se serializa como objeto JSON.
@Suppress("UNCHECKED_CAST")
inline fun <reified T> T.toFirestore(): Map<String, Any?> =
    // Serializa el modelo a JSON y luego lo convierte a los tipos que entiende Firestore.
    FirestoreMapper.fromJson(FirestoreMapper.json.encodeToJsonElement(this)) as Map<String, Any?>

// Mapa leído de Firestore → modelo; null si no se puede leer (un documento mal formado no tumba la pantalla).
inline fun <reified T> Map<String, Any?>.toModel(): T? =
    // Convierte el mapa a JSON, lo decodifica al modelo y atrapa cualquier error devolviendo null.
    runCatching { FirestoreMapper.json.decodeFromJsonElement<T>(FirestoreMapper.toJson(this)) }.getOrNull()

// Documento → modelo; null si no existe (su data es null) o si no se puede leer.
inline fun <reified T> DocumentSnapshot.toModel(): T? = data?.toModel<T>()

// Todos los documentos de una consulta que se pudieron leer; los mal formados se descartan.
inline fun <reified T> QuerySnapshot.toModels(): List<T> = documents.mapNotNull { it.toModel<T>() }
