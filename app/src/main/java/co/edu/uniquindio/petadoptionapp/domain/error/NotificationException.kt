package co.edu.uniquindio.petadoptionapp.domain.error

/**
Errores del dominio de notificaciones.
Los lanzan los casos de uso de usecase/notification al marcar como leída o borrar
una notificación. La UI traduce cada uno a un texto amigable usando strings.xml.
**/
// Jerarquía sellada con los errores posibles de las notificaciones.
sealed class NotificationException : Exception() {

    // Ocurre cuando la notificación no existe o no pertenece a la persona que la consulta.
    object NotificationNotFound : NotificationException()
}
