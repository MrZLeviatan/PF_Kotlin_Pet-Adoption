package co.edu.uniquindio.petadoptionapp.data.repository.memory

import co.edu.uniquindio.petadoptionapp.domain.error.NotificationException
import co.edu.uniquindio.petadoptionapp.domain.model.notification.Notification
import co.edu.uniquindio.petadoptionapp.domain.repository.NotificationRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
Documenta la implementación en memoria del repositorio de notificaciones.
Simula la colección `notifications`. La lista guarda las más nuevas primero, así el orden no depende de milisegundos repetidos.
 */
@Singleton
class NotificationRepositoryImpl @Inject constructor() : NotificationRepository {

    // Guarda las notificaciones, de la más reciente a la más antigua.
    private val notifications = MutableStateFlow<List<Notification>>(emptyList())

    override suspend fun create(notification: Notification): Result<Unit> {
        val saved = notification.copy(id = UUID.randomUUID().toString())
        notifications.update { listOf(saved) + it }
        return Result.success(Unit)
    }

    override suspend fun getByRecipient(
        recipientId: String,
        limit: Int,
        afterNotificationId: String?
    ): Result<List<Notification>> {
        val list = notifications.value.filter { it.recipientId == recipientId }
        val start = if (afterNotificationId == null) 0 else {
            val index = list.indexOfFirst { it.id == afterNotificationId }
            if (index < 0) return Result.success(emptyList()) else index + 1
        }
        return Result.success(list.drop(start).take(limit))
    }

    override suspend fun findById(id: String): Notification? =
        notifications.value.firstOrNull { it.id == id }

    override suspend fun countUnread(recipientId: String): Result<Int> =
        Result.success(notifications.value.count { it.recipientId == recipientId && !it.read })

    override suspend fun setRead(id: String, read: Boolean): Result<Unit> {
        if (findById(id) == null) return Result.failure(NotificationException.NotificationNotFound)
        notifications.update { list -> list.map { if (it.id == id) it.copy(read = read) else it } }
        return Result.success(Unit)
    }

    override suspend fun markAllAsRead(recipientId: String): Result<Unit> {
        notifications.update { list -> list.map { if (it.recipientId == recipientId) it.copy(read = true) else it } }
        return Result.success(Unit)
    }

    override suspend fun delete(id: String): Result<Unit> {
        if (findById(id) == null) return Result.failure(NotificationException.NotificationNotFound)
        notifications.update { list -> list.filterNot { it.id == id } }
        return Result.success(Unit)
    }
}