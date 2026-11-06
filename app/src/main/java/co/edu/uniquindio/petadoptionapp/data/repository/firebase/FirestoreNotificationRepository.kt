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
 */
@Singleton
class FirestoreNotificationRepository @Inject constructor(
    private val db: FirebaseFirestore,
) : NotificationRepository {

    private val notifications = db.collection(NOTIFICATIONS)

    override suspend fun create(notification: Notification): Result<Unit> = runCatching {
        val ref = notifications.document()
        ref.set(notification.copy(id = ref.id).toFirestore()).await()
        Unit
    }

    override suspend fun getByRecipient(
        recipientId: String,
        limit: Int,
        afterNotificationId: String?
    ): Result<List<Notification>> = runCatching {
        val query = notifications.whereEqualTo("recipientId", recipientId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
        val cursor = afterNotificationId?.let { id ->
            notifications.document(id).get().await().takeIf { it.exists() } ?: return@runCatching emptyList()
        }
        (cursor?.let { query.startAfter(it) } ?: query)
            .limit(limit.toLong())
            .get().await()
            .toModels()
    }

    override suspend fun findById(id: String): Notification? =
        runCatching { notifications.document(id).get().await().toModel<Notification>() }.getOrNull()

    // Un conteo cuesta mucho menos que descargar los documentos.
    override suspend fun countUnread(recipientId: String): Result<Int> = runCatching {
        notifications.whereEqualTo("recipientId", recipientId)
            .whereEqualTo("read", false)
            .count().get(AggregateSource.SERVER).await()
            .count.toInt()
    }

    override suspend fun setRead(id: String, read: Boolean): Result<Unit> = runCatching {
        notifications.document(id).update("read", read).await()
        Unit
    }.mapNotFound()

    override suspend fun markAllAsRead(recipientId: String): Result<Unit> = runCatching {
        val unread = notifications.whereEqualTo("recipientId", recipientId)
            .whereEqualTo("read", false)
            .get().await().documents
        // Un lote acepta hasta 500 escrituras.
        unread.chunked(450).forEach { chunk ->
            db.batch().apply { chunk.forEach { update(it.reference, "read", true) } }.commit().await()
        }
    }

    override suspend fun delete(id: String): Result<Unit> = runCatching {
        val ref = notifications.document(id)
        if (!ref.get().await().exists()) throw NotificationException.NotificationNotFound
        ref.delete().await()
        Unit
    }

    private fun <T> Result<T>.mapNotFound(): Result<T> = recoverCatching { error ->
        throw if (error is FirebaseFirestoreException && error.code == FirebaseFirestoreException.Code.NOT_FOUND)
            NotificationException.NotificationNotFound else error
    }
}
