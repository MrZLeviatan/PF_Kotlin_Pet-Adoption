# Modelos del Dominio

## Propósito

Los modelos representan la información que maneja la aplicación: publicaciones, cuentas, comentarios, conversaciones, notificaciones y reputación. Son `data class` inmutables con valores por defecto en todas sus propiedades. Algunos incluyen funciones con reglas propias de la entidad (por ejemplo, decidir si una publicación es visible en el feed o si un código de verificación sigue vigente).

Los modelos que se guardan en Firestore llevan la anotación `@Serializable`. La capa de datos los convierte en documentos mediante `FirestoreMapper`, de modo que el modelo nunca depende de Firebase.

## Estructura

```text
model/
├── post/            Publicación, catálogos, verificación, análisis de IA y estadísticas
│   └── filter/      Filtros y orden del feed
├── users/           Cuenta, usuario, moderador y rol
│   └── profile/     Ajustes, privacidad y vistas públicas del perfil
├── comment/         Comentarios y "me gusta"
├── messaging/       Chats y mensajes
├── adoption/        Solicitudes de adopción
├── notification/    Notificaciones internas
├── reputation/      Puntos, niveles e insignias
├── location/        Ubicación y catálogo de ciudades
└── auth/            Credenciales y reto de verificación en dos pasos
```

## Contenido por archivo

### `post/`

| Archivo | Descripción | Funciones principales |
|---|---|---|
| `Post.kt` | Publicación de una mascota (adopción, pérdida, hallazgo, hogar temporal o jornada veterinaria). Referencia al autor, la categoría y el tipo de animal por id. Incluye la verificación, el análisis de IA y los contadores de votos y comentarios. | `showsInPublicFeed()`, `canBePublished()` |
| `PostStatus.kt` | Ciclo de vida de la publicación: `IN_PROGRESS`, `FINISHED`, `DELETED`. Es independiente de la verificación. | — |
| `Verification.kt` | Revisión de la publicación: estado, prioridad sugerida y su motivo, motivo de rechazo, moderador y fecha. Toda publicación nace sin verificar. | `approve()`, `reject()`, `startReview()` |
| `VerificationStatus.kt` | Estados de revisión: `UNVERIFIED`, `IN_PROCESS`, `VERIFIED`, `REJECTED`. | — |
| `PriorityLevel.kt` | Prioridad sugerida por la IA: `LOW`, `MEDIUM`, `HIGH`, `URGENT`. Ordena la cola de moderación. | — |
| `AiReview.kt` | Resultado del análisis de IA: resumen, puntos clave, relevancia, inconsistencias, revisión de fotos, contenido inapropiado, posible duplicado y acciones recomendadas. Define también `RelevanceLevel` y `AiPostAnalysis`. | `relevanceLevel`, `hasWarnings` |
| `Category.kt` | Categoría del catálogo. Además del nombre, guarda `icon`, `color` e `isPlace`, que determinan el marcador en el mapa. | — |
| `AnimalType.kt` | Tipo de animal del catálogo (perro, gato, ave, etc.). | — |
| `AnimalSize.kt` | Tamaño aproximado del animal: `SMALL`, `MEDIUM`, `LARGE`. | — |
| `ImportantVote.kt` | Voto "Es importante" de una persona sobre una publicación. El id `postId_userId` impide votar dos veces. | `buildId()` |
| `PostStats.kt` | Métricas personales: publicaciones activas, finalizadas y pendientes. | — |
| `ModerationStats.kt` | Publicaciones sin moderador asignado, agrupadas por prioridad. | — |
| `ModeratorPerformance.kt` | Desempeño de un moderador: aprobadas, rechazadas, en revisión, actividad diaria y distribución por categoría y prioridad. | `resolved`, `approvalRate` |
| `filter/PostFilter.kt` | Filtros opcionales del feed: categoría, tipo de animal, ciudad, cercanía, tamaño y vacunación. | `hasNearbyArea()` |
| `filter/PostSort.kt` | Orden del feed: `RECENT` o `MOST_IMPORTANT`. | — |

### `users/` y `users/profile/`

| Archivo | Descripción | Funciones principales |
|---|---|---|
| `Account.kt` | Identidad compartida por usuarios y moderadores: id, correo, alias, rol y fecha de creación. No contiene la contraseña. | — |
| `User.kt` | Perfil de una persona registrada: datos personales, foto, ubicación, reputación y ajustes. Se construye por composición con `Account`. | `toPublicProfile()` |
| `Moderator.kt` | Perfil de moderador: datos de contacto, estado activo, verificación en dos pasos y contadores de revisión. No se registra desde la aplicación. | `reviewedCount()`, `canModerate()` |
| `UserRole.kt` | Roles de la aplicación: `USER`, `MODERATOR`. | — |
| `profile/UserSettings.kt` | Preferencias de privacidad y seguridad. Es la única fuente de la política de privacidad. | `hasProfilePublic()`, `canViewName()`, `canViewPhone()`, `canViewReputation()`, `canViewPosts()` |
| `profile/ProfileVisibility.kt` | Nivel de privacidad: `PUBLIC`, `PRIVATE`. | — |
| `profile/PublicProfile.kt` | Vista del perfil tal como la ve otra persona; los datos ocultos llegan en `null`. | — |
| `profile/UserSummary.kt` | Resultado resumido de una búsqueda de usuarios (id, alias y foto). | — |
| `profile/AccountDeletionFeedback.kt` | Motivo anónimo opcional al eliminar la cuenta. | — |
| `profile/DeletionReason.kt` | Opciones de motivo de eliminación. | — |

### `comment/`

| Archivo | Descripción | Funciones principales |
|---|---|---|
| `Comment.kt` | Comentario o respuesta de una publicación. Admite un solo nivel de respuestas; las respuestas a respuestas se mantienen en el mismo hilo e indican a quién responden. | `createTopLevel()`, `createReply()`, `edit()`, `markAsDeleted()`, `visibleText()`, `canBeRepliedTo()` |
| `CommentLike.kt` | "Me gusta" de una persona sobre un comentario. El id `commentId_userId` impide duplicados. | `buildId()` |
| `CommentStatus.kt` | Estado del comentario: `ACTIVE`, `DELETED`. El borrado es lógico para conservar las respuestas. | — |

### `messaging/`

| Archivo | Descripción | Funciones principales |
|---|---|---|
| `Chat.kt` | Conversación entre dos personas, normalmente originada por una publicación. Guarda el resumen del último mensaje, los no leídos y las fechas de lectura de cada participante. | `buildId()`, `otherParticipantId()`, `unreadFor()`, `isHiddenFor()`, `isMessageSeenBy()` |
| `Message.kt` | Mensaje individual dentro de un chat (texto, foto o aviso de adopción). | — |
| `MessageType.kt` | Tipos de mensaje: `TEXT`, `ADOPTION_INTEREST`, `IMAGE`. | — |

### `adoption/`

| Archivo | Descripción | Funciones principales |
|---|---|---|
| `AdoptionRequest.kt` | Solicitud de adopción de una persona sobre una publicación. El id `postId_requesterId` limita a una solicitud por persona y publicación. | `accept()`, `decline()`, `cancel()`, `awaitsResponse()`, `buildId()` |
| `AdoptionRequestStatus.kt` | Estados: `PENDING`, `ACCEPTED`, `DECLINED`, `CANCELLED`. | — |

### `notification/`

| Archivo | Descripción | Funciones principales |
|---|---|---|
| `Notification.kt` | Notificación interna dirigida a una persona. Guarda el tipo y los datos necesarios; el texto lo compone la interfaz a partir de `strings.xml`. | `hasDestination()`, `markAsRead()`, `markAsUnread()` |
| `NotificationType.kt` | Eventos que generan notificaciones: comentarios, respuestas, verificación, rechazo, votos, solicitudes y respuestas de adopción, mensajes, insignias y niveles. | — |

### `reputation/`

| Archivo | Descripción | Funciones principales |
|---|---|---|
| `Reputation.kt` | Puntos acumulados e insignias desbloqueadas de un usuario. El nivel se calcula a partir de los puntos. | `level()`, `levelNumber()`, `addPoints()`, `unlockBadge()`, `hasBadge()` |
| `UserLevel.kt` | Niveles: Amigo Animal (0), Protector (100), Guardián (300), Héroe de las Mascotas (700). | `fromPoints()` |
| `PointRules.kt` | Valores de puntos por acción y tope diario de comentarios premiados. Es el único lugar donde se configuran. | — |
| `PointReason.kt` | Motivos que otorgan puntos: publicación verificada, publicación finalizada, comentario creado y voto recibido. | — |
| `PointTransaction.kt` | Registro de puntos otorgados. Su id determinista garantiza que un mismo hecho solo puntúa una vez. | `buildId()` |
| `Badge.kt` | Insignia del catálogo con su requisito (tipo y cantidad). | — |
| `BadgeRequirementType.kt` | Tipos de requisito: publicaciones creadas, verificadas o resueltas, comentarios, votos recibidos y puntos totales. | — |
| `UnlockedBadge.kt` | Referencia a una insignia ya obtenida y su fecha. | — |
| `AwardResult.kt` | Respuesta de otorgar puntos: si se aplicó y los puntos antes y después. | `leveledUp()` |

### `location/` y `auth/`

| Archivo | Descripción | Funciones principales |
|---|---|---|
| `location/Location.kt` | Ubicación reutilizable: coordenadas, ciudad, país y dirección. Las coordenadas son nulas hasta elegir un punto. | `hasCoordinates()`, `distanceKmTo()` |
| `location/City.kt` | Ciudad con coordenadas centrales y `CityCatalog`, la lista fija de ciudades del selector. | `toLocation()`, `CityCatalog.find()` |
| `auth/Credentials.kt` | Correo y contraseña escritos en el formulario. Solo existe en memoria y su `toString()` oculta la contraseña. | — |
| `auth/TwoFactorChallenge.kt` | Reto de verificación en dos pasos: huella del código, vencimiento (5 minutos), intentos (máximo 5) y uso único. | `hasExpired()`, `hasReachedMaxAttempts()`, `canBeUsed()` |

## Relaciones entre modelos

```text
Account ◄── User ──► Reputation ──► UnlockedBadge ──► Badge (catálogo)
   ▲          └────► UserSettings, Location
   └──── Moderator

Post ──► Verification, AiReview, Location
 │  └──► categoryId → Category · animalTypeId → AnimalType · authorId → Account
 ├──► Comment (subcolección) ──► CommentLike
 ├──► ImportantVote
 └──► AdoptionRequest ──► Chat ──► Message
```

Las relaciones se expresan mediante identificadores (`authorId`, `categoryId`, `postId`) y no mediante objetos anidados, lo que facilita su almacenamiento como documentos independientes.

## Reglas relevantes

- **Visibilidad de una publicación.** Solo aparece en el feed si `verification.status == VERIFIED` y `status == IN_PROGRESS` (`Post.showsInPublicFeed()`).
- **Privacidad.** Ninguna pantalla decide qué datos mostrar de otra persona: se consulta `UserSettings` y se construye un `PublicProfile`.
- **Identificadores deterministas.** `ImportantVote`, `CommentLike`, `AdoptionRequest`, `Chat` y `PointTransaction` construyen su id a partir de sus participantes. Esto evita duplicados aun ante toques repetidos o fallos de red.
- **Datos derivados.** El nivel de reputación y la relevancia del análisis de IA se calculan; no se almacenan.

## Guía de extensión

- Todo modelo persistido debe llevar `@Serializable` y valores por defecto en todas sus propiedades.
- No se deben agregar dependencias de Android, Compose ni Firebase en esta carpeta.
- Las modificaciones se realizan creando copias con `copy(...)`.
