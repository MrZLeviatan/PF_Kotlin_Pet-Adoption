# Diagrama de datos del dominio

Diagrama lógico basado en los modelos de `domain/model`. Representa las entidades y sus relaciones; no implica que la aplicación ya use una base de datos relacional. Actualmente, los repositorios enlazados por Hilt son implementaciones en memoria.

```mermaid
erDiagram
    ACCOUNT {
        string id PK
        string email
        string userName
        enum role
        long createdAt
    }

    USER_PROFILE {
        string accountId PK, FK
        string name
        string phone
        string photoUrl
        int reputationPoints
        boolean twoFactorEnabled
        enum profileVisibility
        boolean deleted
        long deletedAt
    }

    MODERATOR_PROFILE {
        string accountId PK, FK
        string name
        boolean active
        int approvedCount
        int rejectedCount
    }

    CATEGORY {
        string id PK
        string name
        boolean active
        boolean allowsAdoptionRequests
    }

    ANIMAL_TYPE {
        string id PK
        string name
        boolean active
    }

    POST {
        string id PK
        string authorId FK
        string categoryId FK
        string animalTypeId FK
        string title
        string description
        string breed
        enum size
        int ageInMonths
        boolean vaccinated
        boolean neutered
        string photoUrls
        double latitude
        double longitude
        string city
        string address
        enum status
        enum verificationStatus
        enum priorityLevel
        string rejectionReason
        string reviewedBy FK
        int importantCount
        int commentCount
        long createdAt
        long finishedAt
    }

    COMMENT {
        string id PK
        string postId FK
        string parentId FK
        string authorId FK
        string text
        enum status
        int likeCount
        int replyCount
        long createdAt
    }

    COMMENT_LIKE {
        string commentId PK, FK
        string userId PK, FK
        long createdAt
    }

    IMPORTANT_VOTE {
        string postId PK, FK
        string userId PK, FK
        long createdAt
    }

    ADOPTION_REQUEST {
        string id PK
        string postId FK
        string postAuthorId FK
        string requesterId FK
        string requesterName
        string chatId FK
        string message
        enum status
        long createdAt
        long respondedAt
    }

    CHAT {
        string id PK
        string postId FK
        string lastMessage
        enum lastMessageType
        long lastMessageAt
        string lastSenderId FK
        long createdAt
    }

    CHAT_PARTICIPANT {
        string chatId PK, FK
        string accountId PK, FK
        int unreadCount
        long lastReadAt
    }

    MESSAGE {
        string id PK
        string chatId FK
        string senderId FK
        enum type
        string text
        long sentAt
    }

    NOTIFICATION {
        string id PK
        string recipientId FK
        string actorId FK
        string postId FK
        string commentId FK
        enum type
        string extra
        boolean read
        long createdAt
    }

    BADGE {
        string id PK
        string name
        string description
        string iconUrl
        enum requirementType
        int requirementValue
    }

    UNLOCKED_BADGE {
        string userId PK, FK
        string badgeId PK, FK
        long unlockedAt
    }

    POINT_TRANSACTION {
        string id PK
        string userId FK
        enum reason
        int points
        string postId FK
        string actorId FK
        long createdAt
    }

    TWO_FACTOR_CHALLENGE {
        string userId PK, FK
        string codeHash
        long expiresAt
        int attempts
        int maxAttempts
        boolean consumed
        long createdAt
    }

    ACCOUNT ||--o| USER_PROFILE : "tiene perfil"
    ACCOUNT ||--o| MODERATOR_PROFILE : "tiene perfil"

    USER_PROFILE ||--o{ POST : "publica"
    CATEGORY ||--o{ POST : "clasifica"
    ANIMAL_TYPE o|--o{ POST : "describe animal"
    MODERATOR_PROFILE o|--o{ POST : "revisa"

    POST ||--o{ COMMENT : "recibe"
    ACCOUNT ||--o{ COMMENT : "escribe"
    COMMENT o|--o{ COMMENT : "responde a"
    COMMENT ||--o{ COMMENT_LIKE : "recibe likes"
    ACCOUNT ||--o{ COMMENT_LIKE : "marca like"

    POST ||--o{ IMPORTANT_VOTE : "recibe votos"
    ACCOUNT ||--o{ IMPORTANT_VOTE : "vota"

    POST ||--o{ ADOPTION_REQUEST : "recibe solicitudes"
    ACCOUNT ||--o{ ADOPTION_REQUEST : "solicita"
    ACCOUNT ||--o{ ADOPTION_REQUEST : "recibe como autor"
    CHAT o|--o| ADOPTION_REQUEST : "inicia conversación"

    POST o|--o{ CHAT : "origina"
    CHAT ||--|{ CHAT_PARTICIPANT : "tiene participantes"
    ACCOUNT ||--o{ CHAT_PARTICIPANT : "participa"
    CHAT ||--o{ MESSAGE : "contiene"
    ACCOUNT ||--o{ MESSAGE : "envía"

    ACCOUNT ||--o{ NOTIFICATION : "recibe"
    ACCOUNT o|--o{ NOTIFICATION : "genera"
    POST o|--o{ NOTIFICATION : "relacionada con"
    COMMENT o|--o{ NOTIFICATION : "relacionada con"

    USER_PROFILE ||--o{ UNLOCKED_BADGE : "desbloquea"
    BADGE ||--o{ UNLOCKED_BADGE : "define"
    USER_PROFILE ||--o{ POINT_TRANSACTION : "acumula"
    POST o|--o{ POINT_TRANSACTION : "origina puntos"
    ACCOUNT o|--o{ POINT_TRANSACTION : "acción de"

    ACCOUNT ||--o| TWO_FACTOR_CHALLENGE : "tiene desafío"
```

## Correspondencia con los modelos

- `Location` y `Verification` se representan como campos embebidos en `USER_PROFILE` o `POST`, no como entidades independientes.
- `Reputation` se representa parcialmente en `USER_PROFILE` (`points`) y parcialmente en `UNLOCKED_BADGE` y `POINT_TRANSACTION`.
- `Chat.participantIds`, `unreadCounts` y `lastReadAt` son mapas/listas dentro del modelo actual. `CHAT_PARTICIPANT` es su representación relacional lógica; no es un modelo Kotlin independiente.
- `Account` está compuesta dentro de `User` y `Moderator`. Se muestra separada para aclarar su relación conceptual con los perfiles y con otras entidades.
- `Credentials` no debe persistirse. `TwoFactorChallenge` describe datos temporales y sensibles; su código debe guardarse como hash.
- `PublicProfile`, `UserSummary`, `PostFilter`, `PostStats` y `AwardResult` son modelos de presentación/consulta o resultados, no entidades persistentes.
- `AccountDeletionFeedback` existe como modelo de entrada, pero la implementación en memoria actual lo descarta; no se muestra como tabla persistida.
- Los estados y valores enumerados (`UserRole`, `PostStatus`, `VerificationStatus`, `NotificationType`, etc.) se representan como `enum`; no son tablas.
- `photoUrls` se muestra como campo de lista simplificado. En una base relacional normalizada podría separarse en una tabla de imágenes.
