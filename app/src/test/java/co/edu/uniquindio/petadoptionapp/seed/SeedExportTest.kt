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
 */
class SeedExportTest {

    private val json = FirestoreMapper.json

    private inline fun <reified T> doc(value: T, extra: Map<String, String> = emptyMap()): JsonObject {
        val base = json.encodeToJsonElement(value).jsonObject
        return JsonObject(base + extra.mapValues { JsonPrimitive(it.value) })
    }

    @Test
    fun exportSeed() {
        val owner = { uid: String -> buildJsonObject { put("uid", uid) } }

        val collections = buildJsonObject {
            put("users", JsonObject(SeedData.users.associate {
                it.account.id to doc(it, mapOf("userNameLower" to it.account.userName.lowercase()))
            }))
            put("moderators", JsonObject(SeedData.moderators.associate { it.account.id to doc(it) }))

            // Índices de valores únicos (correo, alias y teléfono).
            val accounts = SeedData.users.map { Triple(it.account, it.phone, true) } +
                    SeedData.moderators.map { Triple(it.account, it.phone, false) }
            put("emails", JsonObject(accounts.associate { (a, _, _) -> a.email.lowercase() to owner(a.id) }))
            put("userNames", JsonObject(accounts.filter { it.third }.associate { (a, _, _) -> a.userName.lowercase() to owner(a.id) }))
            put("phones", JsonObject(accounts.filter { it.third }.associate { (a, phone, _) -> phone to owner(a.id) }))

            put("categories", JsonObject(SeedData.categories.associate { it.id to doc(it) }))
            put("animalTypes", JsonObject(SeedData.animalTypes.associate { it.id to doc(it) }))
            put("badges", JsonObject(SeedData.badges.associate { it.id to doc(it) }))
            put("posts", JsonObject(SeedData.posts.associate { it.id to doc(it) }))
            put("notifications", JsonObject(SeedData.notifications.associate { it.id to doc(it) }))
            put("chats", JsonObject(SeedData.chats.associate { it.id to doc(it) }))
            put("adoptionRequests", JsonObject(SeedData.adoptionRequests.associate { it.id to doc(it) }))
            put("commentLikes", JsonObject(SeedData.commentLikes.associate { CommentLike.buildId(it.commentId, it.userId) to doc(it) }))
            put("importantVotes", JsonObject(SeedData.importantVotes.associate { ImportantVote.buildId(it.postId, it.userId) to doc(it) }))
            put("pointTransactions", JsonObject(SeedData.pointTransactions.associate { it.id to doc(it) }))
        }

        val subcollections = buildJsonArray {
            SeedData.comments.groupBy { it.postId }.forEach { (postId, comments) ->
                add(buildJsonObject {
                    put("path", "posts/$postId/comments")
                    put("docs", JsonObject(comments.associate { it.id to doc(it) }))
                })
            }
            SeedData.chatMessages.forEach { (chatId, messages) ->
                add(buildJsonObject {
                    put("path", "chats/$chatId/messages")
                    put("docs", JsonObject(messages.associate { it.id to doc(it) }))
                })
            }
        }

        // Las cuentas de Firebase Auth se crean con el mismo uid que usan los datos (u1, u2, m1).
        val auth = buildJsonArray {
            (SeedData.users.map { it.account } + SeedData.moderators.map { it.account }).forEach { account ->
                add(buildJsonObject {
                    put("uid", account.id)
                    put("email", account.email)
                    // La contraseña NO va aquí (el repositorio es público): el script la lee de seed/.env.
                    put("role", account.role.name)
                    put("displayName", account.userName)
                })
            }
        }

        val seed = buildJsonObject {
            // El script corre las fechas para que los datos se vean recientes el día que se carguen.
            put("generatedAt", System.currentTimeMillis())
            put("auth", auth)
            put("collections", collections)
            put("subcollections", subcollections)
        }

        // El test corre con la carpeta del módulo (app/) como directorio de trabajo.
        val out = File("../firebase/seed/seed.json")
        out.parentFile?.mkdirs()
        out.writeText(kotlinx.serialization.json.Json { prettyPrint = true }.encodeToString(JsonObject.serializer(), seed))
        println("Semilla escrita en ${out.canonicalPath}")
    }
}
