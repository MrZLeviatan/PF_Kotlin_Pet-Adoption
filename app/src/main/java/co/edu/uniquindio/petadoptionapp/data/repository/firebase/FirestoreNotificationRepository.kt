package co.edu.uniquindio.petadoptionapp.data.repository.firebase

import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCollections.NOTIFICATIONS
import co.edu.uniquindio.petadoptionapp.domain.error.NotificationException
import co.edu.uniquindio.petadoptionapp.domain.model.notification.Notification
import co.edu.uniquindio.petadoptionapp.domain.repository.NotificationRepository
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

/**
Notificaciones en `notifications/{id}`, cada una con su destinataria (`recipientId`).

Implementa NotificationRepository del dominio y se enlaza en RepositoryModule. Los casos de uso
crean notificaciones cuando ocurre algo relevante (comentarios, mensajes, aprobaciones,
insignias) y la pantalla de notificaciones las lista, cuenta las no leídas y las marca como leídas.
**/
// Una sola instancia compartida en toda la app.
@Singleton
// Hilt inyecta Firestore desde FirebaseModule.
class FirestoreNotificationRepository @Inject constructor(
    // Base de datos Firestore.
    private val db: FirebaseFirestore,
) : NotificationRepository {

    // Referencia a la colección de notificaciones.
    private val notifications = db.collection(NOTIFICATIONS)

    // Guarda una nueva notificación.
    override suspend fun create(notification: Notification): Result<Unit> = runCatching {
        // Crea una referencia con id automático generado por Firestore.
        val ref = notifications.document()
        // Guarda la notificación con su propio id incluido en el documento.
        ref.set(notification.copy(id = ref.id).toFirestore()).await()
        // Resultado exitoso.
        Unit
    }

    // Lista paginada de notificaciones de un usuario, de la más reciente a la más antigua.
    override suspend fun getByRecipient(
        // Usuario que recibe las notificaciones.
        recipientId: String,
        // Tamaño de la página.
        limit: Int,
        // Id de la última notificación de la página anterior (null para la primera página).
        afterNotificationId: String?
    ): Result<List<Notification>> = runCatching {
        // Filtra por destinataria.
        val query = notifications.whereEqualTo("recipientId", recipientId)
            // Ordena de la más reciente a la más antigua.
            .orderBy("createdAt", Query.Direction.DESCENDING)
        // Obtiene el documento cursor para continuar la paginación; si ya no existe, devuelve una lista vacía.
        val cursor = afterNotificationId?.let { id ->
            notifications.document(id).get().await().takeIf { it.exists() } ?: return@runCatching emptyList()
        }
        // Empieza después del cursor si existe; si no, desde el principio.
        (cursor?.let { query.startAfter(it) } ?: query)
            // Limita el tamaño de la página.
            .limit(limit.toLong())
            // Ejecuta la consulta.
            .get().await()
            // Convierte los documentos en notificaciones.
            .toModels()
    }

    // Busca una notificación por id; devuelve null si no existe o si ocurre un error.
    override suspend fun findById(id: String): Notification? =
        runCatching { notifications.document(id).get().await().toModel<Notification>() }.getOrNull()

    // Un conteo cuesta mucho menos que descargar los documentos.
    override suspend fun countUnread(recipientId: String): Result<Int> = runCatching {
        // Filtra por destinataria.
        notifications.whereEqualTo("recipientId", recipientId)
            // Solo las que no se han leído.
            .whereEqualTo("read", false)
            // Pide al servidor solo el número de coincidencias (consulta de agregación).
            .count().get(AggregateSource.SERVER).await()
            // Convierte el conteo a entero.
            .count.toInt()
    }

    // Marca una notificación como leída o no leída.
    override suspend fun setRead(id: String, read: Boolean): Result<Unit> = runCatching {
        // Actualiza solo el campo read del documento.
        notifications.document(id).update("read", read).await()
        // Resultado exitoso.
        Unit
    // Traduce el error NOT_FOUND de Firestore al error del dominio.
    }.mapNotFound()

    // Marca todas las notificaciones no leídas del usuario como leídas.
    override suspend fun markAllAsRead(recipientId: String): Result<Unit> = runCatching {
        // Obtiene los documentos de las notificaciones no leídas del usuario.
        val unread = notifications.whereEqualTo("recipientId", recipientId)
            .whereEqualTo("read", false)
            .get().await().documents
        // Un lote acepta hasta 500 escrituras; se usan bloques de 450 por margen de seguridad.
        unread.chunked(450).forEach { chunk ->
            // Crea un lote, agrega una actualización por documento y lo confirma de una sola vez.
            db.batch().apply { chunk.forEach { update(it.reference, "read", true) } }.commit().await()
        }
    }

    // Elimina una notificación.
    override suspend fun delete(id: String): Result<Unit> = runCatching {
        // Referencia a la notificación.
        val ref = notifications.document(id)
        // Si no existe, informa el error del dominio (borrar algo inexistente en Firestore no fallaría).
        if (!ref.get().await().exists()) throw NotificationException.NotificationNotFound
        // Borra el documento.
        ref.delete().await()
        // Resultado exitoso.
        Unit
    }

    // Convierte el error NOT_FOUND de Firestore en NotificationNotFound; deja pasar los demás errores.
    private fun <T> Result<T>.mapNotFound(): Result<T> = recoverCatching { error ->
        // Lanza el error del dominio si el documento no existía; si no, relanza el error original.
        throw if (error is FirebaseFirestoreException && error.code == FirebaseFirestoreException.Code.NOT_FOUND)
            NotificationException.NotificationNotFound else error
    }
}
