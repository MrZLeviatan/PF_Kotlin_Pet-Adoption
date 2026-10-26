package co.edu.uniquindio.petadoptionapp.domain.error

/**
Errores del dominio de notificaciones.
La UI traduce cada uno a un texto amigable usando strings.xml.
 */
sealed class NotificationException : Exception() {

    // Ocurre cuando la notificación no existe o no pertenece a la persona que la consulta.
    object NotificationNotFound : NotificationException()
}