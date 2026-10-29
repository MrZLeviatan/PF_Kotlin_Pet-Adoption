package co.edu.uniquindio.petadoptionapp.data.repository.send

import co.edu.uniquindio.petadoptionapp.domain.model.adoption.AdoptionRequest
import co.edu.uniquindio.petadoptionapp.domain.model.location.Location
import co.edu.uniquindio.petadoptionapp.domain.model.messaging.Chat
import co.edu.uniquindio.petadoptionapp.domain.model.messaging.Message
import co.edu.uniquindio.petadoptionapp.domain.model.messaging.MessageType
import co.edu.uniquindio.petadoptionapp.domain.model.notification.Notification
import co.edu.uniquindio.petadoptionapp.domain.model.notification.NotificationType
import co.edu.uniquindio.petadoptionapp.domain.model.post.AnimalSize
import co.edu.uniquindio.petadoptionapp.domain.model.post.AnimalType
import co.edu.uniquindio.petadoptionapp.domain.model.post.Category
import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.PriorityLevel
import co.edu.uniquindio.petadoptionapp.domain.model.post.Verification
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.Badge
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.BadgeRequirementType
import co.edu.uniquindio.petadoptionapp.domain.model.users.Account
import co.edu.uniquindio.petadoptionapp.domain.model.users.Moderator
import co.edu.uniquindio.petadoptionapp.domain.model.users.User
import co.edu.uniquindio.petadoptionapp.domain.model.users.UserRole

/**
Documenta el objeto que contiene los datos quemados de arranque (solo para la fase sin base de datos).
Cuando se conecte Firebase, esta información se carga una vez en Firestore y este archivo desaparece.
 */
object SeedData {



    // Define los usuarios de prueba.
    val users = listOf(
        User(
            account = Account(id = "u1", email = "n@gmail.com", userName = "ana_77", role = UserRole.USER),
            name = "Ana Gómez",
            phone = "3001234567",
            location = Location(4.5339, -75.6811, "Armenia", "Colombia", "Centro")
        ),
        User(
            account = Account(id = "u2", email = "luis@correo.com", userName = "luis_9", role = UserRole.USER),
            name = "Luis Pérez",
            phone = "3007654321",
            location = Location(4.5402, -75.6658, "Armenia", "Colombia", "Norte")
        )
    )

    // Define el moderador precargado (los moderadores no se registran desde la app).
    val moderators = listOf(
        Moderator(
            account = Account(id = "m1", email = "mod@petadoption.com", userName = "mod_01", role = UserRole.MODERATOR),
            name = "Moderador Uno",
            phone = "3109876543"
        )
    )

    // Define las contraseñas de prueba por correo (solo existen en esta fase; en Firebase las guarda Auth).
    val passwords = mapOf(
        "n@gmail.com" to "12345",
        "luis@correo.com" to "Secreta123",
        "mod@petadoption.com" to "Moderador123"
    )


    // Define las categorías iniciales del catálogo.
    val categories = listOf(
        Category(id = "adoption", name = "Adopción", active = true, allowsAdoptionRequests = true),
        Category(id = "lost", name = "Perdidos", active = true),
        Category(id = "found", name = "Encontrados", active = true),
        Category(id = "temporary", name = "Temporal", active = true, allowsAdoptionRequests = true),
        Category(id = "veterinary", name = "Veterinaria", active = true)
    )

    // Define los tipos de animal iniciales.
    val animalTypes = listOf(
        AnimalType(id = "dog", name = "Perro"),
        AnimalType(id = "cat", name = "Gato"),
        AnimalType(id = "bird", name = "Ave"),
        AnimalType(id = "rodent", name = "Roedor"),
        AnimalType(id = "other", name = "Otro")
    )


    // Fechas relativas a "ahora": con valores fijos (1970) la app mostraría "hace 20 000 días".
    // Va antes de las listas que lo usan, porque un object se inicializa en orden.
    private val now = System.currentTimeMillis()
    private fun minutesAgo(minutes: Long) = now - minutes * 60_000L
    private fun hoursAgo(hours: Long) = minutesAgo(hours * 60)

    // Ids de los chats de ejemplo entre ana_77 (u1) y luis_9 (u2); los usan notificaciones, chats y solicitudes.
    private val CHAT_P1 = Chat.buildId("u1", "u2", "p1")
    private val CHAT_P2 = Chat.buildId("u2", "u1", "p2")

    // Define las publicaciones de ejemplo, con estados mezclados para probar cada pantalla.
    // Se conserva el orden de antes: p1 es la más antigua y p5 la más reciente.
    val posts = listOf(
        // Verificada y en proceso: aparece en el feed.
        Post(
            id = "p1", authorId = "u1", title = "Perrito en adopción", categoryId = "adoption",
            description = "Cachorro criollo de ocho meses, juguetón y sociable.",
            animalTypeId = "dog", breed = "Criollo", size = AnimalSize.MEDIUM, ageInMonths = 8,
            vaccinated = true, neutered = false,
            photoUrls = listOf("https://res.cloudinary.com/dehltwwbu/image/upload/v1791690961/hero-photo_i8ti3d.png"),
            location = Location(4.5339, -75.6811, "Armenia", "Colombia", "Centro"),
            verification = Verification(status = VerificationStatus.VERIFIED, reviewedBy = "m1", reviewedAt = hoursAgo(5 * 24)),
            createdAt = hoursAgo(6 * 24)
        ),
        // Verificada: gato perdido.
        Post(
            id = "p2", authorId = "u2", title = "Gato perdido en el norte", categoryId = "lost",
            description = "Gato gris con collar azul, se perdió cerca del parque.",
            animalTypeId = "cat", breed = "Mestizo", size = AnimalSize.SMALL, ageInMonths = 24,
            photoUrls = listOf("https://res.cloudinary.com/dehltwwbu/image/upload/v1791690961/hero-photo_i8ti3d.png"),
            location = Location(4.5402, -75.6658, "Armenia", "Colombia", "Norte"),
            verification = Verification(status = VerificationStatus.VERIFIED, reviewedBy = "m1", reviewedAt = hoursAgo(1)),
            importantCount = 3, createdAt = hoursAgo(4 * 24)
        ),
        // Sin verificar, con prioridad sugerida: aparece en la cola del moderador.
        Post(
            id = "p3", authorId = "u1", title = "Hogar de paso para cachorros", categoryId = "temporary",
            description = "Busco hogar temporal para tres cachorros rescatados.",
            animalTypeId = "dog", size = AnimalSize.SMALL,
            photoUrls = listOf("https://res.cloudinary.com/dehltwwbu/image/upload/v1791690961/hero-photo_i8ti3d.png"),
            location = Location(4.5339, -75.6811, "Armenia", "Colombia", "Centro"),
            verification = Verification(status = VerificationStatus.UNVERIFIED,
                priorityLevel = PriorityLevel.HIGH, priorityReason = "Animales en riesgo"),
            createdAt = hoursAgo(2 * 24)
        ),
        // Rechazada: no sale en el feed.
        Post(
            id = "p4", authorId = "u2", title = "Cachorros a la venta", categoryId = "adoption",
            description = "Vendo cachorros de raza, precio negociable.",
            animalTypeId = "dog", photoUrls = listOf("https://res.cloudinary.com/dehltwwbu/image/upload/v1791690961/hero-photo_i8ti3d.png"),
            location = Location(4.5402, -75.6658, "Armenia", "Colombia", "Norte"),
            verification = Verification(status = VerificationStatus.REJECTED, reviewedBy = "m1",
                rejectionReason = "No se permite la venta de animales", reviewedAt = hoursAgo(2)),
            createdAt = hoursAgo(24)
        ),
        // Finalizada: ya no sale en el feed.
        Post(
            id = "p5", authorId = "u1", title = "Loro encontrado", categoryId = "found",
            description = "Loro verde encontrado en el centro, ya volvió con su dueño.",
            animalTypeId = "bird", photoUrls = listOf("https://picsum.photos/400?random=15"),
            location = Location(4.5339, -75.6811, "Armenia", "Colombia", "Centro"),
            status = PostStatus.FINISHED, finishedAt = hoursAgo(3),
            verification = Verification(status = VerificationStatus.VERIFIED, reviewedBy = "m1", reviewedAt = hoursAgo(26)),
            createdAt = hoursAgo(10)
        ),
        // Pendientes de moderación (de luis_9), con prioridades distintas para ordenar la cola.
        Post(
            id = "p6", authorId = "u2", title = "Perrita herida necesita hogar urgente", categoryId = "adoption",
            description = "La encontré atropellada; ya la vio un veterinario, pero no puedo quedármela.",
            animalTypeId = "dog", breed = "Criolla", size = AnimalSize.MEDIUM,
            photoUrls = listOf("https://res.cloudinary.com/dehltwwbu/image/upload/v1791690961/hero-photo_i8ti3d.png"),
            location = Location(4.5402, -75.6658, "Armenia", "Colombia", "Norte"),
            verification = Verification(status = VerificationStatus.UNVERIFIED,
                priorityLevel = PriorityLevel.URGENT, priorityReason = "Animal herido que necesita atención"),
            createdAt = hoursAgo(5)
        ),
        // En revisión: ya la tomó un moderador, por eso no sale en la cola del inicio.
        Post(
            id = "p7", authorId = "u2", title = "Gatitos abandonados en una caja", categoryId = "found",
            description = "Cuatro gatitos de pocas semanas, los dejaron frente a mi casa.",
            animalTypeId = "cat", size = AnimalSize.SMALL,
            photoUrls = listOf("https://res.cloudinary.com/dehltwwbu/image/upload/v1791690961/hero-photo_i8ti3d.png"),
            location = Location(4.5402, -75.6658, "Armenia", "Colombia", "Norte"),
            verification = Verification(status = VerificationStatus.IN_PROCESS, reviewedBy = "m1",
                priorityLevel = PriorityLevel.HIGH, priorityReason = "Animales muy pequeños sin cuidado"),
            createdAt = hoursAgo(26)
        ),
        Post(
            id = "p8", authorId = "u2", title = "Jornada de vacunación gratuita", categoryId = "veterinary",
            description = "Este sábado en el parque principal, de 8 a 12.",
            photoUrls = listOf("https://res.cloudinary.com/dehltwwbu/image/upload/v1791690961/hero-photo_i8ti3d.png"),
            location = Location(4.5339, -75.6811, "Armenia", "Colombia", "Centro"),
            verification = Verification(status = VerificationStatus.UNVERIFIED,
                priorityLevel = PriorityLevel.LOW, priorityReason = "Evento informativo"),
            createdAt = hoursAgo(3)
        ),
        Post(
            id = "p9", authorId = "u2", title = "Perro perdido cerca del estadio", categoryId = "lost",
            description = "Labrador café, responde al nombre de Toby.",
            animalTypeId = "dog", breed = "Labrador", size = AnimalSize.LARGE,
            photoUrls = listOf("https://res.cloudinary.com/dehltwwbu/image/upload/v1791690961/hero-photo_i8ti3d.png"),
            location = Location(4.5402, -75.6658, "Armenia", "Colombia", "Norte"),
            verification = Verification(status = VerificationStatus.UNVERIFIED,
                priorityLevel = PriorityLevel.MEDIUM, priorityReason = "Mascota perdida recientemente"),
            createdAt = hoursAgo(8)
        )
    )

    // Define el catálogo de insignias.
    val badges = listOf(
        Badge(
            "first_post", "Primera publicación", "Crea tu primera publicación", "",
            BadgeRequirementType.POSTS_CREATED, 1
        ),
        Badge("ten_verified", "10 verificadas", "Consigue 10 publicaciones verificadas", "",
            BadgeRequirementType.POSTS_VERIFIED, 10),
        Badge("commenter", "Conversador", "Escribe 20 comentarios", "",
            BadgeRequirementType.COMMENTS_WRITTEN, 20)
    )

    // Define las notificaciones de ejemplo para ana_77 (u1).
    val notifications = listOf(
        Notification(id = "n1", recipientId = "u1", type = NotificationType.NEW_COMMENT,
            actorId = "u2", actorName = "luis_9", postId = "p1",
            read = false, createdAt = minutesAgo(2)),
        Notification(id = "n2", recipientId = "u1", type = NotificationType.POST_VERIFIED,
            actorId = "m1", actorName = "mod_01", postId = "p1",
            read = false, createdAt = minutesAgo(18)),
        Notification(id = "n3", recipientId = "u1", type = NotificationType.POST_IMPORTANT,
            actorId = "u2", actorName = "luis_9", postId = "p1",
            read = false, createdAt = minutesAgo(120)),
        Notification(id = "n4", recipientId = "u1", type = NotificationType.BADGE_UNLOCKED,
            extra = "first_post", read = true, createdAt = minutesAgo(24 * 60)),
        Notification(id = "n5", recipientId = "u1", type = NotificationType.LEVEL_UP,
            extra = "PROTECTOR", read = true, createdAt = minutesAgo(3 * 24 * 60)),
        // Mensaje sin leer: al tocarla abre el chat de "Perrito en adopción".
        Notification(id = "n6", recipientId = "u1", type = NotificationType.NEW_MESSAGE,
            actorId = "u2", actorName = "luis_9", postId = "p1", chatId = CHAT_P1,
            read = false, createdAt = minutesAgo(20))
    )

    // Mensajes de cada chat, del más antiguo al más nuevo.
    val chatMessages: Map<String, List<Message>> = mapOf(
        // Empezó con "Me interesa adoptar" de luis_9 en la publicación de ana_77.
        CHAT_P1 to listOf(
            Message(id = "m1", senderId = "u2", type = MessageType.ADOPTION_INTEREST, sentAt = minutesAgo(2 * 24 * 60)),
            Message(id = "m2", senderId = "u1", text = "¡Hola Luis! Gracias por escribir. Es muy juguetón y sociable.", sentAt = minutesAgo(2 * 24 * 60 - 30)),
            Message(id = "m3", senderId = "u2", text = "¿Cómo es su temperamento con niños pequeños?", sentAt = minutesAgo(26 * 60)),
            Message(id = "m4", senderId = "u1", text = "Muy tranquilo, convive con mis sobrinos sin problema.", sentAt = minutesAgo(25 * 60)),
            Message(id = "m5", senderId = "u2", text = "¿Podemos vernos mañana para conocerlo?", sentAt = minutesAgo(20)),
        ),
        // ana_77 le escribió a luis_9 por su gato perdido.
        CHAT_P2 to listOf(
            Message(id = "m6", senderId = "u1", text = "Hola, creo que vi a tu gato cerca del parque del norte.", sentAt = minutesAgo(3 * 24 * 60)),
            Message(id = "m7", senderId = "u2", text = "¿En serio? ¿Recuerdas a qué hora fue?", sentAt = minutesAgo(3 * 24 * 60 - 15)),
            Message(id = "m8", senderId = "u1", text = "Como a las 6 de la tarde, tenía el collar azul.", sentAt = minutesAgo(3 * 24 * 60 - 20)),
        ),
    )

    // Resumen de cada chat, coherente con sus mensajes (último mensaje y no leídos).
    val chats: List<Chat> = listOf(
        Chat(
            id = CHAT_P1, participantIds = listOf("u1", "u2"), postId = "p1",
            lastMessage = "¿Podemos vernos mañana para conocerlo?", lastMessageAt = minutesAgo(20), lastSenderId = "u2",
            unreadCounts = mapOf("u1" to 1, "u2" to 0),
            lastReadAt = mapOf("u1" to minutesAgo(25 * 60), "u2" to minutesAgo(25 * 60)),
            createdAt = minutesAgo(2 * 24 * 60)
        ),
        Chat(
            id = CHAT_P2, participantIds = listOf("u2", "u1"), postId = "p2",
            lastMessage = "Como a las 6 de la tarde, tenía el collar azul.", lastMessageAt = minutesAgo(3 * 24 * 60 - 20), lastSenderId = "u1",
            unreadCounts = mapOf("u1" to 0, "u2" to 0),
            lastReadAt = mapOf("u1" to minutesAgo(3 * 24 * 60 - 20), "u2" to minutesAgo(3 * 24 * 60 - 25)),
            createdAt = minutesAgo(3 * 24 * 60)
        ),
    )

    // La solicitud que originó el chat de p1 (por eso luis_9 ve "Ir al chat" en esa publicación).
    val adoptionRequests: List<AdoptionRequest> = listOf(
        AdoptionRequest(
            id = AdoptionRequest.buildId("p1", "u2"), postId = "p1", postAuthorId = "u1",
            requesterId = "u2", requesterName = "luis_9", chatId = CHAT_P1, createdAt = minutesAgo(2 * 24 * 60)
        )
    )


}