package co.edu.uniquindio.petadoptionapp.data.repository.firebase

/**
Nombres de las colecciones de Firestore. Deben coincidir con firebase/firestore.rules.
 */
object FirestoreCollections {
    const val USERS = "users"
    const val MODERATORS = "moderators"

    // Índices de valores únicos: el id del documento es el valor y guarda el uid de su dueño.
    // Permiten revisar si un correo, alias o teléfono ya existe sin abrir los perfiles a todo el mundo.
    const val EMAILS = "emails"
    const val USER_NAMES = "userNames"
    const val PHONES = "phones"

    const val TWO_FACTOR = "twoFactorChallenges"
    const val DELETION_FEEDBACK = "deletionFeedback"

    const val CATEGORIES = "categories"
    const val ANIMAL_TYPES = "animalTypes"
    const val BADGES = "badges"

    const val POSTS = "posts"
    const val COMMENTS = "comments"          // Subcolección de posts/{postId}
    const val COMMENT_LIKES = "commentLikes"
    const val IMPORTANT_VOTES = "importantVotes"
    const val NOTIFICATIONS = "notifications"
    const val CHATS = "chats"
    const val MESSAGES = "messages"          // Subcolección de chats/{chatId}
    const val ADOPTION_REQUESTS = "adoptionRequests"
    const val POINT_TRANSACTIONS = "pointTransactions"

    // Firestore acepta como máximo 30 valores en una consulta whereIn.
    const val MAX_IN_VALUES = 30
}
