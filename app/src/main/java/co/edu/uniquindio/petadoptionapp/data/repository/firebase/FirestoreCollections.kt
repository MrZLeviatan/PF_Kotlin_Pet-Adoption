package co.edu.uniquindio.petadoptionapp.data.repository.firebase

/**
Nombres de las colecciones de Firestore. Deben coincidir con firebase/firestore.rules.
Pertenece a la capa de datos: todos los repositorios Firestore* usan estas constantes en vez
de escribir los nombres a mano, así un error de escritura no crea colecciones fantasma y
renombrar una colección se hace en un solo lugar. El script firebase/seed/seed.mjs y las
reglas de seguridad usan los mismos nombres.
**/
// Se usa un object porque solo agrupa constantes globales, sin estado ni instancias.
object FirestoreCollections {
    // Perfiles de los usuarios comunes (documento por uid).
    const val USERS = "users"
    // Perfiles de los moderadores (documento por uid); su existencia define el rol de moderador.
    const val MODERATORS = "moderators"

    // Índices de valores únicos: el id del documento es el valor y guarda el uid de su dueño.
    // Permiten revisar si un correo, alias o teléfono ya existe sin abrir los perfiles a todo el mundo.
    // Índice de correos registrados.
    const val EMAILS = "emails"
    // Índice de alias (nombres de usuario) en uso.
    const val USER_NAMES = "userNames"
    // Índice de teléfonos registrados.
    const val PHONES = "phones"

    // Retos de verificación en dos pasos (A2F): guardan el código enviado por correo y su vencimiento.
    const val TWO_FACTOR = "twoFactorChallenges"
    // Motivos que dejan las personas al eliminar su cuenta.
    const val DELETION_FEEDBACK = "deletionFeedback"

    // Catálogo de categorías con las que se clasifican las publicaciones.
    const val CATEGORIES = "categories"
    // Catálogo de tipos de animal.
    const val ANIMAL_TYPES = "animalTypes"
    // Catálogo de insignias que se pueden ganar con la reputación.
    const val BADGES = "badges"

    // Publicaciones de mascotas.
    const val POSTS = "posts"
    // Subcolección de posts/{postId}: comentarios de cada publicación.
    const val COMMENTS = "comments"
    // "Me gusta" de los comentarios.
    const val COMMENT_LIKES = "commentLikes"
    // Votos "Es importante" sobre las publicaciones.
    const val IMPORTANT_VOTES = "importantVotes"
    // Notificaciones dirigidas a cada usuario.
    const val NOTIFICATIONS = "notifications"
    // Conversaciones entre usuarios.
    const val CHATS = "chats"
    // Subcolección de chats/{chatId}: mensajes de cada conversación.
    const val MESSAGES = "messages"
    // Solicitudes de adopción enviadas por los interesados.
    const val ADOPTION_REQUESTS = "adoptionRequests"
    // Historial de puntos de reputación ganados o perdidos.
    const val POINT_TRANSACTIONS = "pointTransactions"

    // Firestore acepta como máximo 30 valores en una consulta whereIn; se usa para partir las consultas en lotes.
    const val MAX_IN_VALUES = 30
}
