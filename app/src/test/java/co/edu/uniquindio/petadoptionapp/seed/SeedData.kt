package co.edu.uniquindio.petadoptionapp.seed

import co.edu.uniquindio.petadoptionapp.domain.model.adoption.AdoptionRequest
import co.edu.uniquindio.petadoptionapp.domain.model.adoption.AdoptionRequestStatus
import co.edu.uniquindio.petadoptionapp.domain.model.comment.Comment
import co.edu.uniquindio.petadoptionapp.domain.model.comment.CommentLike
import co.edu.uniquindio.petadoptionapp.domain.model.location.Location
import co.edu.uniquindio.petadoptionapp.domain.model.messaging.Chat
import co.edu.uniquindio.petadoptionapp.domain.model.messaging.Message
import co.edu.uniquindio.petadoptionapp.domain.model.messaging.MessageType
import co.edu.uniquindio.petadoptionapp.domain.model.notification.Notification
import co.edu.uniquindio.petadoptionapp.domain.model.notification.NotificationType
import co.edu.uniquindio.petadoptionapp.domain.model.post.AnimalSize
import co.edu.uniquindio.petadoptionapp.domain.model.post.AnimalType
import co.edu.uniquindio.petadoptionapp.domain.model.post.Category
import co.edu.uniquindio.petadoptionapp.domain.model.post.ImportantVote
import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.PriorityLevel
import co.edu.uniquindio.petadoptionapp.domain.model.post.Verification
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.Badge
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.BadgeRequirementType
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.PointReason
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.PointTransaction
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.Reputation
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.UnlockedBadge
import co.edu.uniquindio.petadoptionapp.domain.model.users.Account
import co.edu.uniquindio.petadoptionapp.domain.model.users.Moderator
import co.edu.uniquindio.petadoptionapp.domain.model.users.User
import co.edu.uniquindio.petadoptionapp.domain.model.users.UserRole

/**
Datos de arranque de la app. Ya no los usa la app: SeedExportTest los convierte en
firebase/seed/seed.json y el script firebase/seed/seed.mjs los carga una vez en Firebase.

Contenido: 4 usuarios, 5 moderadores, 5 categorías (con ícono y color para el mapa), 5 tipos de animal,
4 insignias, 14 publicaciones (feed, cola de moderación, en revisión, rechazada y finalizada),
comentarios, votos, chats, solicitudes de adopción, notificaciones e historial de puntos.
Los contadores (votos, comentarios, puntos, revisiones de cada moderador) cuadran con los datos.
 */
object SeedData {

    // Fechas relativas a "ahora" (el script de carga las corre al día en que se cargan).
    // Va primero, porque un object se inicializa en orden.
    private val now = System.currentTimeMillis()
    private fun minutesAgo(minutes: Long) = now - minutes * 60_000L
    private fun hoursAgo(hours: Long) = minutesAgo(hours * 60)
    private fun daysAgo(days: Long) = hoursAgo(days * 24)

    // Fotos de las publicaciones. Para cambiarlas, reemplaza las URL (por ejemplo, por fotos subidas a Cloudinary).
    private const val PHOTO = "https://res.cloudinary.com/dehltwwbu/image/upload/v1791690961/hero-photo_i8ti3d.png"
    private const val AVATAR = "https://res.cloudinary.com/dehltwwbu/image/upload/v1793019358/IconDefault_b3nesx.jpg"

    // Barrios de Armenia: las publicaciones quedan repartidas en el mapa de Explorar.
    private fun armenia(lat: Double, lng: Double, address: String) =
        Location(lat, lng, "Armenia", "Colombia", address)

    private val CENTRO = armenia(4.5339, -75.6811, "Centro")
    private val NORTE = armenia(4.5520, -75.6620, "Av. Bolívar, norte")
    private val CASTELLANA = armenia(4.5430, -75.6700, "La Castellana")
    private val SUR = armenia(4.5180, -75.6890, "Barrio La Patria, sur")
    private val ESTADIO = armenia(4.5280, -75.6640, "Estadio Centenario")
    private val UNIVERSIDAD = armenia(4.5560, -75.6590, "Universidad del Quindío")
    private val PARQUE_VIDA = armenia(4.5470, -75.6560, "Parque de la Vida")




    // ---- Catálogos ----

    // Cada categoría trae su ícono, color y forma para el mapa de Explorar y los filtros.
    val categories = listOf(
        Category(id = "adoption", name = "Adopción", allowsAdoptionRequests = true,
            icon = "pets", color = "#A06931"),
        Category(id = "lost", name = "Perdidos",
            icon = "search", color = "#D32F2F"),
        Category(id = "found", name = "Encontrados",
            icon = "flag", color = "#2E7D32"),
        Category(id = "temporary", name = "Hogar temporal", allowsAdoptionRequests = true,
            icon = "home", color = "#E9C46A", isPlace = true),
        Category(id = "veterinary", name = "Veterinaria",
            icon = "hospital", color = "#2F7EA6", isPlace = true),
    )

    val animalTypes = listOf(
        AnimalType(id = "dog", name = "Perro"),
        AnimalType(id = "cat", name = "Gato"),
        AnimalType(id = "bird", name = "Ave"),
        AnimalType(id = "rodent", name = "Roedor"),
        AnimalType(id = "other", name = "Otro"),
    )

    val badges = listOf(
        Badge("first_post", "Primera publicación", "Crea tu primera publicación", "",
            BadgeRequirementType.POSTS_CREATED, 1),
        Badge("happy_ending", "Final feliz", "Finaliza una publicación porque la mascota encontró hogar o volvió a casa", "",
            BadgeRequirementType.POSTS_RESOLVED, 1),
        Badge("ten_verified", "10 verificadas", "Consigue 10 publicaciones verificadas", "",
            BadgeRequirementType.POSTS_VERIFIED, 10),
        Badge("commenter", "Conversador", "Escribe 20 comentarios", "",
            BadgeRequirementType.COMMENTS_WRITTEN, 20),
    )


    // ---- Publicaciones ----

    private fun verifiedBy(moderatorId: String, hours: Long) =
        Verification(status = VerificationStatus.VERIFIED, reviewedBy = moderatorId, reviewedAt = hoursAgo(hours))

    private fun pending(priority: PriorityLevel?, reason: String) =
        Verification(status = VerificationStatus.UNVERIFIED, priorityLevel = priority, priorityReason = reason)

    private val basePosts = listOf(
        // Verificadas y en proceso: salen en el feed y en el mapa (una o más por categoría).
        Post(
            id = "p1", authorId = "u1", title = "Toby, cachorro criollo en adopción", categoryId = "adoption",
            description = "Cachorro de ocho meses, juguetón y sociable. Tiene sus vacunas y se lleva bien con niños.",
            animalTypeId = "dog", breed = "Criollo", size = AnimalSize.MEDIUM, ageInMonths = 8,
            vaccinated = true, neutered = false, photoUrls = listOf(PHOTO), location = CENTRO,
            verification = verifiedBy("m1", 5 * 24), createdAt = daysAgo(6)
        ),
        Post(
            id = "p2", authorId = "u2", title = "Gato gris perdido en el norte", categoryId = "lost",
            description = "Se llama Humo, tiene collar azul con cascabel. Se perdió cerca de la Av. Bolívar.",
            animalTypeId = "cat", breed = "Mestizo", size = AnimalSize.SMALL, ageInMonths = 24,
            vaccinated = true, neutered = true, photoUrls = listOf(PHOTO), location = NORTE,
            verification = verifiedBy("m2", 3 * 24), createdAt = daysAgo(4)
        ),
        Post(
            id = "p3", authorId = "u3", title = "Perrita encontrada en La Castellana", categoryId = "found",
            description = "Perrita café, mediana, muy dócil. La tengo en casa mientras aparece su familia.",
            animalTypeId = "dog", size = AnimalSize.MEDIUM, photoUrls = listOf(PHOTO), location = CASTELLANA,
            verification = verifiedBy("m3", 2 * 24), createdAt = daysAgo(3)
        ),
        Post(
            id = "p4", authorId = "u4", title = "Hogar de paso para gatitos", categoryId = "temporary",
            description = "Ofrezco hogar temporal para hasta dos gatitos mientras consiguen adopción. Tengo experiencia.",
            animalTypeId = "cat", size = AnimalSize.SMALL, photoUrls = listOf(PHOTO), location = SUR,
            verification = verifiedBy("m1", 4 * 24), createdAt = daysAgo(5)
        ),
        Post(
            id = "p5", authorId = "u3", title = "Jornada de vacunación gratuita", categoryId = "veterinary",
            description = "Este sábado de 8 a. m. a 12 m. en el Parque de la Vida. Vacuna antirrábica para perros y gatos.",
            photoUrls = listOf(PHOTO), location = PARQUE_VIDA,
            verification = verifiedBy("m4", 30), createdAt = daysAgo(2)
        ),
        Post(
            id = "p6", authorId = "u1", title = "Conejo busca familia", categoryId = "adoption",
            description = "Conejo blanco de un año, muy tranquilo. Lo entrego con su jaula y comedero.",
            animalTypeId = "other", size = AnimalSize.SMALL, ageInMonths = 12, vaccinated = false, neutered = false,
            photoUrls = listOf(PHOTO), location = UNIVERSIDAD,
            verification = verifiedBy("m5", 20), createdAt = daysAgo(2)
        ),
        Post(
            id = "p7", authorId = "u2", title = "Esterilización a bajo costo", categoryId = "veterinary",
            description = "Campaña de esterilización para gatos y perros pequeños. Cupos limitados, inscripción previa.",
            photoUrls = listOf(PHOTO), location = ESTADIO,
            verification = verifiedBy("m2", 10), createdAt = hoursAgo(30)
        ),

        // Pendientes de moderación, con prioridades distintas (una sin analizar por la IA).
        Post(
            id = "p8", authorId = "u2", title = "Perrita herida necesita hogar urgente", categoryId = "adoption",
            description = "La encontré atropellada; ya la vio un veterinario, pero no puedo quedármela.",
            animalTypeId = "dog", breed = "Criolla", size = AnimalSize.MEDIUM, photoUrls = listOf(PHOTO), location = NORTE,
            verification = pending(PriorityLevel.URGENT, "Animal herido que necesita atención"), createdAt = hoursAgo(5)
        ),
        Post(
            id = "p9", authorId = "u4", title = "Labrador perdido cerca del estadio", categoryId = "lost",
            description = "Labrador café, responde al nombre de Max. Tiene una mancha blanca en el pecho.",
            animalTypeId = "dog", breed = "Labrador", size = AnimalSize.LARGE, photoUrls = listOf(PHOTO), location = ESTADIO,
            verification = pending(PriorityLevel.MEDIUM, "Mascota perdida recientemente"), createdAt = hoursAgo(8)
        ),
        Post(
            id = "p10", authorId = "u3", title = "Hogar temporal para tres cachorros", categoryId = "temporary",
            description = "Busco hogar temporal para tres cachorros rescatados de la calle.",
            animalTypeId = "dog", size = AnimalSize.SMALL, photoUrls = listOf(PHOTO), location = CASTELLANA,
            verification = pending(PriorityLevel.HIGH, "Animales en riesgo"), createdAt = hoursAgo(12)
        ),
        Post(
            id = "p11", authorId = "u1", title = "Loro encontrado en el centro", categoryId = "found",
            description = "Loro verde, dice algunas palabras. Estaba en el parque Sucre.",
            animalTypeId = "bird", photoUrls = listOf(PHOTO), location = CENTRO,
            verification = pending(null, ""), createdAt = hoursAgo(2)
        ),

        // En revisión: ya la tomó un moderador (no sale en la cola del inicio).
        Post(
            id = "p12", authorId = "u4", title = "Gatitos abandonados en una caja", categoryId = "found",
            description = "Cuatro gatitos de pocas semanas, los dejaron frente a mi casa.",
            animalTypeId = "cat", size = AnimalSize.SMALL, photoUrls = listOf(PHOTO), location = SUR,
            verification = Verification(status = VerificationStatus.IN_PROCESS, reviewedBy = "m1",
                priorityLevel = PriorityLevel.HIGH, priorityReason = "Animales muy pequeños sin cuidado"),
            createdAt = hoursAgo(26)
        ),

        // Rechazada: no sale en el feed.
        Post(
            id = "p13", authorId = "u2", title = "Cachorros de raza a la venta", categoryId = "adoption",
            description = "Vendo cachorros de raza, precio negociable.",
            animalTypeId = "dog", photoUrls = listOf(PHOTO), location = NORTE,
            verification = Verification(status = VerificationStatus.REJECTED, reviewedBy = "m3",
                rejectionReason = "No se permite la venta de animales", reviewedAt = hoursAgo(20)),
            createdAt = daysAgo(1)
        ),

        // Finalizada: la mascota volvió a casa (ya no sale en el feed).
        Post(
            id = "p14", authorId = "u1", title = "Perro perdido en la universidad", categoryId = "lost",
            description = "Ya apareció, ¡gracias a todos por compartir!",
            animalTypeId = "dog", size = AnimalSize.MEDIUM, photoUrls = listOf(PHOTO), location = UNIVERSIDAD,
            status = PostStatus.FINISHED, finishedAt = hoursAgo(6),
            verification = verifiedBy("m4", 4 * 24), createdAt = daysAgo(5)
        ),
    )


    // ---- Comentarios, "me gusta" y votos "Es importante" ----

    // Comentarios por publicación (subcolección posts/{id}/comments).
    val comments: List<Comment> = listOf(
        Comment(id = "c1", postId = "p1", authorId = "u2", authorName = "luis_9", authorPhotoUrl = AVATAR,
            text = "¿Todavía está disponible? Me interesa mucho.", replyCount = 1, likeCount = 1, createdAt = daysAgo(3)),
        Comment(id = "c2", postId = "p1", parentId = "c1", depth = 1, replyToUserName = "luis_9",
            authorId = "u1", authorName = "ana_77", authorPhotoUrl = AVATAR,
            text = "¡Sí! Escríbeme por el chat y coordinamos.", createdAt = daysAgo(3) + 3_600_000),
        Comment(id = "c3", postId = "p2", authorId = "u4", authorName = "andres_m", authorPhotoUrl = AVATAR,
            text = "Creo que lo vi ayer cerca del parque del norte.", likeCount = 1, createdAt = daysAgo(2)),
        Comment(id = "c4", postId = "p3", authorId = "u1", authorName = "ana_77", authorPhotoUrl = AVATAR,
            text = "¡Qué bonita! Ojalá aparezca su familia pronto.", createdAt = daysAgo(1)),
    )

    val commentLikes: List<CommentLike> = listOf(
        CommentLike(commentId = "c1", postId = "p1", userId = "u1", createdAt = daysAgo(3)),
        CommentLike(commentId = "c3", postId = "p2", userId = "u2", createdAt = daysAgo(2)),
    )

    val importantVotes: List<ImportantVote> = listOf(
        ImportantVote(postId = "p1", userId = "u2", createdAt = daysAgo(4)),
        ImportantVote(postId = "p1", userId = "u3", createdAt = daysAgo(3)),
        ImportantVote(postId = "p2", userId = "u1", createdAt = daysAgo(3)),
        ImportantVote(postId = "p2", userId = "u4", createdAt = daysAgo(2)),
    )

    // Publicaciones con sus contadores calculados de los comentarios y votos de arriba.
    val posts: List<Post> = basePosts.map { post ->
        post.copy(
            commentCount = comments.count { it.postId == post.id },
            importantCount = importantVotes.count { it.postId == post.id },
        )
    }


    // ---- Moderadores (no se registran desde la app; van después de las publicaciones porque cuentan sus revisiones) ----

    val moderators = listOf(
        moderator("m1", "mod@petadoption.com", "mod_laura", "Laura Restrepo", "3109876543"),
        moderator("m2", "mod2@petadoption.com", "mod_carlos", "Carlos Mejía", "3109876544"),
        moderator("m3", "mod3@petadoption.com", "mod_diana", "Diana Ospina", "3109876545"),
        moderator("m4", "mod4@petadoption.com", "mod_jorge", "Jorge Salazar", "3109876546"),
        moderator("m5", "mod5@petadoption.com", "mod_sofia", "Sofía Cardona", "3109876547"),
    ).map { mod ->
        // Aprobadas y rechazadas de cada moderador, contadas de las publicaciones.
        val reviewed = posts.filter { it.verification.reviewedBy == mod.account.id }
        mod.copy(
            approvedCount = reviewed.count { it.verification.status == VerificationStatus.VERIFIED },
            rejectedCount = reviewed.count { it.verification.status == VerificationStatus.REJECTED },
        )
    }

    private fun moderator(id: String, email: String, userName: String, name: String, phone: String) = Moderator(
        account = Account(id = id, email = email, userName = userName, role = UserRole.MODERATOR, createdAt = daysAgo(60)),
        name = name,
        phone = phone,
    )


    // ---- Reputación ----

    // Historial de puntos. Los puntos de cada perfil son la suma de su historial.
    val pointTransactions: List<PointTransaction> = listOf(
        points("u1", PointReason.POST_VERIFIED, "p1", "m1", daysAgo(5)),
        points("u1", PointReason.POST_VERIFIED, "p6", "m5", hoursAgo(20)),
        points("u1", PointReason.POST_VERIFIED, "p14", "m4", daysAgo(4)),
        points("u1", PointReason.POST_FINISHED, "p14", null, hoursAgo(6)),
        points("u1", PointReason.IMPORTANT_RECEIVED, "p1_u2", "u2", daysAgo(4), postId = "p1"),
        points("u1", PointReason.IMPORTANT_RECEIVED, "p1_u3", "u3", daysAgo(3), postId = "p1"),
        points("u1", PointReason.COMMENT_CREATED, "c2", null, daysAgo(3), postId = "p1"),
        points("u1", PointReason.COMMENT_CREATED, "c4", null, daysAgo(1), postId = "p3"),
        points("u2", PointReason.POST_VERIFIED, "p2", "m2", daysAgo(3)),
        points("u2", PointReason.POST_VERIFIED, "p7", "m2", hoursAgo(10)),
        points("u2", PointReason.IMPORTANT_RECEIVED, "p2_u1", "u1", daysAgo(3), postId = "p2"),
        points("u2", PointReason.IMPORTANT_RECEIVED, "p2_u4", "u4", daysAgo(2), postId = "p2"),
        points("u2", PointReason.COMMENT_CREATED, "c1", null, daysAgo(3), postId = "p1"),
        points("u3", PointReason.POST_VERIFIED, "p3", "m3", daysAgo(2)),
        points("u3", PointReason.POST_VERIFIED, "p5", "m4", hoursAgo(30)),
        points("u4", PointReason.POST_VERIFIED, "p4", "m1", daysAgo(4)),
        points("u4", PointReason.COMMENT_CREATED, "c3", null, daysAgo(2), postId = "p2"),
    )

    private fun points(
        userId: String, reason: PointReason, sourceId: String, actorId: String?, at: Long,
        postId: String? = sourceId.takeIf { it.startsWith("p") && !it.contains('_') }
    ) = PointTransaction(
        id = PointTransaction.buildId(userId, reason, sourceId),
        userId = userId, reason = reason, points = reason.points,
        postId = postId, actorId = actorId, createdAt = at
    )

    private fun reputationOf(userId: String, vararg badgeIds: String) = Reputation(
        points = pointTransactions.filter { it.userId == userId }.sumOf { it.points },
        unlockedBadges = badgeIds.map { UnlockedBadge(it, daysAgo(4)) }
    )


    // ---- Usuarios ----

    val users = listOf(
        User(
            account = Account(id = "u1", email = "n@gmail.com", userName = "ana_77", role = UserRole.USER, createdAt = daysAgo(30)),
            name = "Ana Gómez", phone = "3001234567", location = CENTRO,
            reputation = reputationOf("u1", "first_post", "happy_ending"),
        ),
        User(
            account = Account(id = "u2", email = "luis@correo.com", userName = "luis_9", role = UserRole.USER, createdAt = daysAgo(25)),
            name = "Luis Pérez", phone = "3007654321", location = NORTE,
            reputation = reputationOf("u2", "first_post"),
        ),
        User(
            account = Account(id = "u3", email = "camila@correo.com", userName = "camila_r", role = UserRole.USER, createdAt = daysAgo(20)),
            name = "Camila Rojas", phone = "3015551234", location = CASTELLANA,
            reputation = reputationOf("u3", "first_post"),
        ),
        User(
            account = Account(id = "u4", email = "andres@correo.com", userName = "andres_m", role = UserRole.USER, createdAt = daysAgo(15)),
            name = "Andrés Muñoz", phone = "3024449876", location = SUR,
            reputation = reputationOf("u4", "first_post"),
        ),
    )



    // ---- Chats y solicitudes de adopción ----

    private val CHAT_P1 = Chat.buildId("u1", "u2", "p1")   // Luis quiere adoptar a Toby (de Ana).
    private val CHAT_P6 = Chat.buildId("u1", "u3", "p6")   // Camila quiere adoptar el conejo (de Ana).
    private val CHAT_P4 = Chat.buildId("u1", "u4", "p4")   // Ana pidió el hogar de paso de Andrés.
    private val CHAT_P2 = Chat.buildId("u4", "u2", "p2")   // Andrés vio el gato perdido de Luis.

    val chatMessages: Map<String, List<Message>> = mapOf(
        CHAT_P1 to listOf(
            Message(id = "msg1", senderId = "u2", type = MessageType.ADOPTION_INTEREST, sentAt = minutesAgo(2 * 24 * 60)),
            Message(id = "msg2", senderId = "u1", text = "¡Hola Luis! Gracias por escribir. Toby es muy juguetón.", sentAt = minutesAgo(2 * 24 * 60 - 30)),
            Message(id = "msg3", senderId = "u2", text = "¿Cómo es con niños pequeños?", sentAt = minutesAgo(26 * 60)),
            Message(id = "msg4", senderId = "u1", text = "Muy tranquilo, convive con mis sobrinos sin problema.", sentAt = minutesAgo(25 * 60)),
            Message(id = "msg5", senderId = "u2", text = "¿Podemos vernos mañana para conocerlo?", sentAt = minutesAgo(20)),
        ),
        CHAT_P6 to listOf(
            Message(id = "msg6", senderId = "u3", type = MessageType.ADOPTION_INTEREST, sentAt = minutesAgo(18 * 60)),
            Message(id = "msg7", senderId = "u1", text = "¡Claro, Camila! Te lo entrego con su jaula.", sentAt = minutesAgo(17 * 60)),
            Message(id = "msg8", senderId = "u3", text = "¡Genial! Paso el domingo por él.", sentAt = minutesAgo(16 * 60)),
        ),
        CHAT_P4 to listOf(
            Message(id = "msg9", senderId = "u1", type = MessageType.ADOPTION_INTEREST, sentAt = minutesAgo(3 * 24 * 60)),
            Message(id = "msg10", senderId = "u4", text = "Hola Ana, por ahora ya tengo los dos cupos ocupados. ¡Lo siento!", sentAt = minutesAgo(3 * 24 * 60 - 60)),
        ),
        CHAT_P2 to listOf(
            Message(id = "msg11", senderId = "u4", text = "Hola, creo que vi a tu gato cerca del parque del norte.", sentAt = minutesAgo(2 * 24 * 60)),
            Message(id = "msg12", senderId = "u2", text = "¿En serio? ¿Recuerdas a qué hora fue?", sentAt = minutesAgo(2 * 24 * 60 - 15)),
            Message(id = "msg13", senderId = "u4", text = "Como a las 6 de la tarde, tenía el collar azul.", sentAt = minutesAgo(2 * 24 * 60 - 20)),
        ),
    )

    // Resumen de cada chat, coherente con sus mensajes (último mensaje y no leídos).
    val chats: List<Chat> = listOf(
        chat(CHAT_P1, "u2", "u1", "p1", unread = mapOf("u1" to 1)),
        chat(CHAT_P6, "u3", "u1", "p6"),
        chat(CHAT_P4, "u1", "u4", "p4"),
        chat(CHAT_P2, "u4", "u2", "p2"),
    )

    private fun chat(id: String, starter: String, other: String, postId: String, unread: Map<String, Int> = emptyMap()): Chat {
        val messages = chatMessages.getValue(id)
        val last = messages.last()
        val participants = listOf(starter, other)
        return Chat(
            id = id, participantIds = participants, postId = postId,
            lastMessage = last.text, lastMessageType = last.type, lastMessageAt = last.sentAt, lastSenderId = last.senderId,
            unreadCounts = participants.associateWith { unread[it] ?: 0 },
            // Quien no tiene pendientes leyó hasta el último mensaje; quien sí, hasta el anterior.
            lastReadAt = participants.associateWith { if ((unread[it] ?: 0) > 0) messages[messages.size - 2].sentAt else last.sentAt },
            createdAt = messages.first().sentAt
        )
    }

    val adoptionRequests: List<AdoptionRequest> = listOf(
        AdoptionRequest(
            id = AdoptionRequest.buildId("p1", "u2"), postId = "p1", postAuthorId = "u1",
            requesterId = "u2", requesterName = "luis_9", chatId = CHAT_P1, createdAt = minutesAgo(2 * 24 * 60)
        ),
        AdoptionRequest(
            id = AdoptionRequest.buildId("p6", "u3"), postId = "p6", postAuthorId = "u1",
            requesterId = "u3", requesterName = "camila_r", chatId = CHAT_P6,
            status = AdoptionRequestStatus.ACCEPTED, createdAt = minutesAgo(18 * 60), respondedAt = minutesAgo(17 * 60)
        ),
        AdoptionRequest(
            id = AdoptionRequest.buildId("p4", "u1"), postId = "p4", postAuthorId = "u4",
            requesterId = "u1", requesterName = "ana_77", chatId = CHAT_P4,
            status = AdoptionRequestStatus.DECLINED, createdAt = minutesAgo(3 * 24 * 60), respondedAt = minutesAgo(3 * 24 * 60 - 60)
        ),
    )


    // ---- Notificaciones ----

    val notifications = listOf(
        // Para ana_77 (u1).
        Notification(id = "n1", recipientId = "u1", type = NotificationType.NEW_MESSAGE,
            actorId = "u2", actorName = "luis_9", postId = "p1", chatId = CHAT_P1, read = false, createdAt = minutesAgo(20)),
        Notification(id = "n2", recipientId = "u1", type = NotificationType.NEW_COMMENT,
            actorId = "u2", actorName = "luis_9", postId = "p1", commentId = "c1", read = false, createdAt = daysAgo(3)),
        Notification(id = "n3", recipientId = "u1", type = NotificationType.POST_IMPORTANT,
            actorId = "u3", actorName = "camila_r", postId = "p1", read = true, createdAt = daysAgo(3)),
        Notification(id = "n4", recipientId = "u1", type = NotificationType.POST_VERIFIED,
            actorId = "m5", actorName = "mod_sofia", postId = "p6", read = false, createdAt = hoursAgo(20)),
        Notification(id = "n5", recipientId = "u1", type = NotificationType.ADOPTION_REQUEST,
            actorId = "u3", actorName = "camila_r", postId = "p6", chatId = CHAT_P6, read = true, createdAt = minutesAgo(18 * 60)),
        Notification(id = "n6", recipientId = "u1", type = NotificationType.BADGE_UNLOCKED,
            extra = "happy_ending", read = true, createdAt = hoursAgo(6)),
        // Para luis_9 (u2).
        Notification(id = "n7", recipientId = "u2", type = NotificationType.POST_REJECTED,
            actorId = "m3", actorName = "mod_diana", postId = "p13", extra = "No se permite la venta de animales",
            read = false, createdAt = hoursAgo(20)),
        Notification(id = "n8", recipientId = "u2", type = NotificationType.NEW_COMMENT,
            actorId = "u4", actorName = "andres_m", postId = "p2", commentId = "c3", read = false, createdAt = daysAgo(2)),
        // Para camila_r (u3) y andres_m (u4).
        Notification(id = "n9", recipientId = "u3", type = NotificationType.ADOPTION_RESPONSE,
            actorId = "u1", actorName = "ana_77", postId = "p6", chatId = CHAT_P6, extra = "ACCEPTED",
            read = false, createdAt = minutesAgo(17 * 60)),
        Notification(id = "n10", recipientId = "u4", type = NotificationType.POST_VERIFIED,
            actorId = "m1", actorName = "mod_laura", postId = "p4", read = true, createdAt = daysAgo(4)),
    )
}
