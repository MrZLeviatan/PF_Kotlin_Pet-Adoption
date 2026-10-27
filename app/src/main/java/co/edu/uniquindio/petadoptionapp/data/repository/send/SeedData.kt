package co.edu.uniquindio.petadoptionapp.data.repository.send

import co.edu.uniquindio.petadoptionapp.domain.model.location.Location
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
            name = "Moderador Uno"
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


    // Define las publicaciones de ejemplo, con estados mezclados para probar cada pantalla.
    val posts = listOf(
        // Verificada y en proceso: aparece en el feed.
        Post(
            id = "p1", authorId = "u1", title = "Perrito en adopción", categoryId = "adoption",
            description = "Cachorro criollo de ocho meses, juguetón y sociable.",
            animalTypeId = "dog", breed = "Criollo", size = AnimalSize.MEDIUM, ageInMonths = 8,
            vaccinated = true, neutered = false,
            photoUrls = listOf("https://res.cloudinary.com/dehltwwbu/image/upload/v1791690961/hero-photo_i8ti3d.png"),
            location = Location(4.5339, -75.6811, "Armenia", "Colombia", "Centro"),
            verification = Verification(status = VerificationStatus.VERIFIED, reviewedBy = "m1"),
            createdAt = 1_000L
        ),
        // Verificada: gato perdido.
        Post(
            id = "p2", authorId = "u2", title = "Gato perdido en el norte", categoryId = "lost",
            description = "Gato gris con collar azul, se perdió cerca del parque.",
            animalTypeId = "cat", breed = "Mestizo", size = AnimalSize.SMALL, ageInMonths = 24,
            photoUrls = listOf("https://res.cloudinary.com/dehltwwbu/image/upload/v1791690961/hero-photo_i8ti3d.png"),
            location = Location(4.5402, -75.6658, "Armenia", "Colombia", "Norte"),
            verification = Verification(status = VerificationStatus.VERIFIED, reviewedBy = "m1"),
            importantCount = 3, createdAt = 2_000L
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
            createdAt = 3_000L
        ),
        // Rechazada: no sale en el feed.
        Post(
            id = "p4", authorId = "u2", title = "Cachorros a la venta", categoryId = "adoption",
            description = "Vendo cachorros de raza, precio negociable.",
            animalTypeId = "dog", photoUrls = listOf("https://res.cloudinary.com/dehltwwbu/image/upload/v1791690961/hero-photo_i8ti3d.png"),
            location = Location(4.5402, -75.6658, "Armenia", "Colombia", "Norte"),
            verification = Verification(status = VerificationStatus.REJECTED, reviewedBy = "m1",
                rejectionReason = "No se permite la venta de animales"),
            createdAt = 4_000L
        ),
        // Finalizada: ya no sale en el feed.
        Post(
            id = "p5", authorId = "u1", title = "Loro encontrado", categoryId = "found",
            description = "Loro verde encontrado en el centro, ya volvió con su dueño.",
            animalTypeId = "bird", photoUrls = listOf("https://picsum.photos/400?random=15"),
            location = Location(4.5339, -75.6811, "Armenia", "Colombia", "Centro"),
            status = PostStatus.FINISHED, finishedAt = 6_000L,
            verification = Verification(status = VerificationStatus.VERIFIED, reviewedBy = "m1"),
            createdAt = 5_000L
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

    // Define las notificaciones de ejemplo para ana_77 (u1): fechas relativas a "ahora".
    private val now = System.currentTimeMillis()
    private fun minutesAgo(minutes: Long) = now - minutes * 60_000L

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
            extra = "PROTECTOR", read = true, createdAt = minutesAgo(3 * 24 * 60))
    )


}