package co.edu.uniquindio.petadoptionapp.seed

import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreMapper
import co.edu.uniquindio.petadoptionapp.domain.model.comment.CommentLike
import co.edu.uniquindio.petadoptionapp.domain.model.post.ImportantVote
import java.io.File
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import org.junit.Test

/**
Genera firebase/seed/seed.json a partir de [SeedData], con el mismo formato que usa la app en Firestore.
Se corre solo cuando cambian los datos de arranque:
  ./gradlew :app:testDebugUnitTest --tests "*SeedExportTest*"

No es una prueba de verdad sino una herramienta: se aprovecha JUnit para ejecutar código Kotlin
que reutiliza los modelos del dominio y el mismo FirestoreMapper de la capa de datos, así los
documentos de semilla quedan idénticos a los que escribiría la app. El archivo resultante lo
carga en Firebase el script firebase/seed/seed.mjs (cuentas de Auth y colecciones de Firestore).
**/
class SeedExportTest {

    // Usa la misma configuración JSON que la app (valores por defecto incluidos, enums como texto).
    private val json = FirestoreMapper.json

    // Convierte un modelo en documento JSON y le agrega campos extra de texto (por ejemplo, el alias en minúsculas).
    private inline fun <reified T> doc(value: T, extra: Map<String, String> = emptyMap()): JsonObject {
        // Serializa el modelo y lo toma como objeto JSON.
        val base = json.encodeToJsonElement(value).jsonObject
        // Une los campos del modelo con los extra (convertidos a primitivas JSON).
        return JsonObject(base + extra.mapValues { JsonPrimitive(it.value) })
    }

    // Marca la función como prueba para que Gradle pueda ejecutarla.
    @Test
    fun exportSeed() {
        // Función auxiliar que crea el contenido de un documento índice: {"uid": ...}.
        val owner = { uid: String -> buildJsonObject { put("uid", uid) } }

        // Colecciones de primer nivel: nombre de colección -> {id del documento -> contenido}.
        val collections = buildJsonObject {
            // Perfiles de usuario indexados por uid, con el campo userNameLower que la app usa para buscar.
            put("users", JsonObject(SeedData.users.associate {
                it.account.id to doc(it, mapOf("userNameLower" to it.account.userName.lowercase()))
            }))
            // Perfiles de moderador indexados por uid.
            put("moderators", JsonObject(SeedData.moderators.associate { it.account.id to doc(it) }))

            // Índices de valores únicos (correo, alias y teléfono).
            // Lista de (cuenta, teléfono, esUsuario) para usuarios y moderadores.
            val accounts = SeedData.users.map { Triple(it.account, it.phone, true) } +
                    SeedData.moderators.map { Triple(it.account, it.phone, false) }
            // Índice de correos: todas las cuentas.
            put("emails", JsonObject(accounts.associate { (a, _, _) -> a.email.lowercase() to owner(a.id) }))
            // Índice de alias: solo usuarios comunes.
            put("userNames", JsonObject(accounts.filter { it.third }.associate { (a, _, _) -> a.userName.lowercase() to owner(a.id) }))
            // Índice de teléfonos: solo usuarios comunes.
            put("phones", JsonObject(accounts.filter { it.third }.associate { (a, phone, _) -> phone to owner(a.id) }))

            // Catálogo de categorías.
            put("categories", JsonObject(SeedData.categories.associate { it.id to doc(it) }))
            // Catálogo de tipos de animal.
            put("animalTypes", JsonObject(SeedData.animalTypes.associate { it.id to doc(it) }))
            // Catálogo de insignias.
            put("badges", JsonObject(SeedData.badges.associate { it.id to doc(it) }))
            // Publicaciones de ejemplo.
            put("posts", JsonObject(SeedData.posts.associate { it.id to doc(it) }))
            // Notificaciones de ejemplo.
            put("notifications", JsonObject(SeedData.notifications.associate { it.id to doc(it) }))
            // Chats de ejemplo.
            put("chats", JsonObject(SeedData.chats.associate { it.id to doc(it) }))
            // Solicitudes de adopción de ejemplo.
            put("adoptionRequests", JsonObject(SeedData.adoptionRequests.associate { it.id to doc(it) }))
            // "Me gusta" de comentarios con su id determinista commentId_userId.
            put("commentLikes", JsonObject(SeedData.commentLikes.associate { CommentLike.buildId(it.commentId, it.userId) to doc(it) }))
            // Votos "Es importante" con su id determinista postId_userId.
            put("importantVotes", JsonObject(SeedData.importantVotes.associate { ImportantVote.buildId(it.postId, it.userId) to doc(it) }))
            // Historial de puntos de reputación.
            put("pointTransactions", JsonObject(SeedData.pointTransactions.associate { it.id to doc(it) }))
        }

        // Subcolecciones: lista de {ruta, documentos}.
        val subcollections = buildJsonArray {
            // Agrupa los comentarios por publicación y crea una entrada por cada subcolección posts/{id}/comments.
            SeedData.comments.groupBy { it.postId }.forEach { (postId, comments) ->
                add(buildJsonObject {
                    // Ruta de la subcolección.
                    put("path", "posts/$postId/comments")
                    // Documentos de la subcolección indexados por id.
                    put("docs", JsonObject(comments.associate { it.id to doc(it) }))
                })
            }
            // Crea una entrada por cada subcolección chats/{id}/messages.
            SeedData.chatMessages.forEach { (chatId, messages) ->
                add(buildJsonObject {
                    // Ruta de la subcolección.
                    put("path", "chats/$chatId/messages")
                    // Mensajes indexados por id.
                    put("docs", JsonObject(messages.associate { it.id to doc(it) }))
                })
            }
        }

        // Las cuentas de Firebase Auth se crean con el mismo uid que usan los datos (u1, u2, m1).
        val auth = buildJsonArray {
            // Recorre las cuentas de usuarios y moderadores.
            (SeedData.users.map { it.account } + SeedData.moderators.map { it.account }).forEach { account ->
                add(buildJsonObject {
                    // Uid fijo de la cuenta.
                    put("uid", account.id)
                    // Correo de inicio de sesión.
                    put("email", account.email)
                    // La contraseña NO va aquí (el repositorio es público): el script la lee de seed/.env.
                    // Rol, usado por el script para elegir la contraseña de prueba.
                    put("role", account.role.name)
                    // Nombre visible en la consola de Firebase.
                    put("displayName", account.userName)
                })
            }
        }

        // Documento raíz del archivo de semilla.
        val seed = buildJsonObject {
            // El script corre las fechas para que los datos se vean recientes el día que se carguen.
            put("generatedAt", System.currentTimeMillis())
            // Cuentas de Firebase Auth.
            put("auth", auth)
            // Colecciones de primer nivel.
            put("collections", collections)
            // Subcolecciones.
            put("subcollections", subcollections)
        }

        // El test corre con la carpeta del módulo (app/) como directorio de trabajo.
        val out = File("../firebase/seed/seed.json")
        // Crea la carpeta destino si no existe.
        out.parentFile?.mkdirs()
        // Escribe el JSON con formato legible.
        out.writeText(kotlinx.serialization.json.Json { prettyPrint = true }.encodeToString(JsonObject.serializer(), seed))
        // Muestra en consola la ruta absoluta del archivo generado.
        println("Semilla escrita en ${out.canonicalPath}")
    }
}
