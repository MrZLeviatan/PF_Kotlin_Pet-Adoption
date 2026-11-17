<h1 align="center">

_Modelos del dominio_

</h1>

**En pocas palabras:** los modelos son las **cosas** que maneja la app: publicaciones, personas, comentarios, chats, notificaciones, puntos. Cada uno es una `data class` con sus datos y, a veces, pequeñas reglas propias (por ejemplo, `Post.showsInPublicFeed()` dice si una publicación puede verse en el feed).

<br>

## Qué hay aquí

| Carpeta | Modelos | Para qué |
|---|---|---|
| `post/` | `Post`, `Category`, `AnimalType`, `AnimalSize`, `PostStatus`, `Verification`, `VerificationStatus`, `PriorityLevel`, `ImportantVote`, `AiReview`, `PostStats`, `ModerationStats`, `ModeratorPerformance` | La publicación, su revisión, su análisis de IA y sus cifras |
| `post/filter/` | `PostFilter`, `PostSort` | Los filtros y el orden del feed |
| `users/` | `Account`, `User`, `Moderator`, `UserRole` | La cuenta y los perfiles de usuario y moderador |
| `users/profile/` | `UserSettings`, `ProfileVisibility`, `PublicProfile`, `UserSummary`, `AccountDeletionFeedback`, `DeletionReason` | Ajustes, privacidad y lo que otros ven de un perfil |
| `comment/` | `Comment`, `CommentLike`, `CommentStatus` | Comentarios, respuestas y "me gusta" |
| `messaging/` | `Chat`, `Message`, `MessageType` | Conversaciones y mensajes |
| `adoption/` | `AdoptionRequest`, `AdoptionRequestStatus` | Solicitudes de adopción |
| `notification/` | `Notification`, `NotificationType` | Avisos para cada persona |
| `reputation/` | `Reputation`, `UserLevel`, `Badge`, `UnlockedBadge`, `PointTransaction`, `PointReason`, `PointRules`, `BadgeRequirementType`, `AwardResult` | Puntos, niveles e insignias |
| `location/` | `Location`, `City` (`CityCatalog`) | Coordenadas, ciudad y dirección |
| `auth/` | `Credentials`, `TwoFactorChallenge` | Datos de inicio de sesión y del código A2F |

<br>

## Ideas clave

- **Una publicación tiene dos estados distintos:**
  - `status`: su vida (`IN_PROGRESS`, `FINISHED`, `DELETED`).
  - `verification.status`: la revisión (`UNVERIFIED`, `IN_PROCESS`, `VERIFIED`, `REJECTED`).
  - Solo sale en el feed si está **verificada y en proceso**.
- **Las relaciones son ids:** `Post.authorId`, `Post.categoryId`, `Comment.postId`. No se guardan objetos dentro de otros.
- **Ids fijos para evitar duplicados:** `ImportantVote.buildId(postId, userId)`, `Chat.buildId(a, b, postId)`, `PointTransaction.buildId(...)`.
- **`Category`** guarda también el `icon`, el `color` y si es un lugar (`isPlace`), con los que el mapa dibuja su marcador.
- **Niveles:** `UserLevel` se calcula siempre a partir de los puntos (Amigo Animal → Protector → Guardián → Héroe de las Mascotas).

<br>

## Para desarrolladores

- Todo modelo que se guarda en Firestore lleva `@Serializable` y **valores por defecto** en todos sus campos.
- No agregues dependencias de Android, Compose o Firebase aquí.
- Para "cambiar" un modelo, crea una copia: `post.copy(title = "Nuevo")`.
