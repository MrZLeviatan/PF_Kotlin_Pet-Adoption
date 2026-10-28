<h1 align="center">

_Casos de uso del dominio_

</h1>

Esta carpeta contiene las acciones que puede realizar la aplicación. Cada caso de uso coordina una tarea concreta, aplica las reglas del negocio y utiliza los contratos de `domain/repository` para leer o cambiar datos.

Los casos de uso no dibujan pantallas ni implementan el almacenamiento. La interfaz los invoca desde sus ViewModels; los repositorios concretos realizan las operaciones sobre los datos.

<br>

## Estructura

### `auth/`

Acciones de autenticación y sesión:

- `RegisterUseCase`: valida datos y coordina la creación de la cuenta y el perfil.
- `LoginUseCase`: inicia sesión, gestiona el paso de doble factor cuando aplica y valida el perfil.
- `VerifyTwoFactorCodeUseCase` y `ResendTwoFactorCodeUseCase`: verifican o vuelven a solicitar el código de doble factor.
- `SendPasswordResetUseCase`: solicita el restablecimiento de contraseña.
- `ChangePasswordUseCase`: valida la contraseña nueva y solicita el cambio.
- `ObserveSessionUseCase`: expone el estado observable de la cuenta actual.

Usan principalmente `AuthRepository` y, para los datos de perfil, `UserRepository` y `CheckProfileUseCase`. La interfaz y el repositorio determinan si el correo, el segundo factor y las contraseñas son simulados o reales.

<Br>

### `user/`

Acciones sobre cuentas y perfiles:

- `CheckProfileUseCase`: confirma que exista un perfil válido para la cuenta y su rol.
- `RequireUserUseCase`, `RequireModeratorUseCase` y `RequireOwnUserUseCase`: comprueban sesión, rol o titularidad.
- `GetUserProfileUseCase` y `SearchUsersUseCase`: consultan perfiles públicos o buscan usuarios.
- `ObserveCurrentUserUseCase`: expone el perfil de la sesión actual.
- `UpdateProfileUseCase` y `UpdateSettingsUseCase`: validan y actualizan perfil y preferencias.
- `DeleteAccountUseCase`: coordina la reautenticación, el cierre de publicaciones, la anonimización del perfil y la eliminación de la cuenta.

Se relacionan con `Account`, `User`, `Moderator` y los modelos de `users/profile/`. Usan `AuthRepository`, `UserRepository` y, al eliminar la cuenta, `PostRepository`.

<br>

### `post/`

Acciones sobre publicaciones y moderación:

- `CreatePostUseCase`, `UpdatePostUseCase` y `DeletePostUseCase`: crean, editan o eliminan publicaciones propias.
- `GetFeedUseCase`, `GetMyPostsUseCase` y `GetPostDetailUseCase`: consultan el feed, las publicaciones del autor y el detalle.
- `FinishPostUseCase`: marca una publicación como finalizada.
- `GetPostStatsUseCase`: calcula las métricas del dashboard.
- `GetModerationQueueUseCase`, `StartReviewUseCase`, `ApprovePostUseCase` y `RejectPostUseCase`: permiten a moderadores consultar, tomar, aprobar o rechazar publicaciones.

Usan `PostRepository`, los modelos de `post/` y, según el proceso, `CatalogRepository`, `RequireModeratorUseCase`, `NotifyUseCase` o `AwardPointsUseCase`.

<br>

### `catalog/`

Acciones para cargar, observar y administrar categorías y tipos de animal:

- `LoadCatalogUseCase`, `ObserveCategoriesUseCase` y `ObserveAnimalTypesUseCase`: cargan o exponen los catálogos.
- `CheckPostCatalogUseCase`: comprueba que la categoría y el tipo de animal existan y estén activos.
- `CreateCategoryUseCase`, `CreateAnimalTypeUseCase`, `UpdateCategoryUseCase` y `UpdateAnimalTypeUseCase`: permiten a un moderador crear o modificar elementos del catálogo.

Usan `CatalogRepository`, `Category`, `AnimalType` y `RequireModeratorUseCase`.

<br>

### `comment/`

Acciones para crear, consultar y administrar comentarios:

- `CreateCommentUseCase`: crea comentarios o respuestas y coordina notificaciones y puntos.
- `GetCommentsUseCase`: consulta comentarios principales o respuestas.
- `EditCommentUseCase` y `DeleteCommentUseCase`: editan o eliminan lógicamente comentarios.
- `ToggleCommentLikeUseCase` y `GetLikedCommentsUseCase`: agregan/quitan “me gusta” y consultan los comentarios marcados.

Usan `CommentRepository`, `PostRepository` y, según la acción, `UserRepository`, `NotifyUseCase`, `AwardPointsUseCase` o `RequireUserUseCase`.

<br>

### `chat/`

Acciones de conversaciones y mensajes:

- `StartChatUseCase`: abre o recupera una conversación relacionada con una publicación.
- `GetMyChatsUseCase` y `CountUnreadChatsUseCase`: consultan chats propios y el conteo de no leídos.
- `RequireChatMemberUseCase`: verifica que la sesión pertenezca al chat.
- `SendMessageUseCase`: valida y envía un mensaje.
- `ObserveMessagesUseCase` y `GetOlderMessagesUseCase`: observan mensajes recientes o consultan mensajes anteriores.
- `MarkChatReadUseCase`: marca la conversación como leída.

Usan `ChatRepository`; los flujos asociados con publicaciones también usan `PostRepository`.



#### `chat/adoption/`

Acciones de solicitudes de adopción:

- `SendAdoptionInterestUseCase`: valida el interés, crea la solicitud y coordina chat y mensaje inicial.
- `RespondAdoptionRequestUseCase` y `CancelAdoptionRequestUseCase`: permiten responder o cancelar solicitudes pendientes.
- `GetMyAdoptionRequestUseCase` y `GetPostAdoptionRequestsUseCase`: consultan la solicitud propia o las solicitudes recibidas por el autor.

Usan `ChatRepository`, `PostRepository`, `CatalogRepository` y `RequireUserUseCase`; las acciones pueden generar notificaciones.

<br>

### `importantVote/`

Acciones del botón “Es importante”:

- `ToggleImportantUseCase`: alterna el voto y coordina la notificación y los puntos al autor cuando corresponde.
- `GetMyVotedPostIdsUseCase`: consulta qué publicaciones de una lista ya votó la persona.
- `GetMyImportantPostsUseCase`: lista las publicaciones visibles que la persona marcó.

Usan `ImportantVoteRepository`, `PostRepository` y `RequireUserUseCase`.

<br>

### `notification/`

Acciones de notificaciones:

- `NotifyUseCase`: crea una notificación para un destinatario.
- `GetNotificationsUseCase` y `CountUnreadNotificationsUseCase`: consultan notificaciones y cuentan las no leídas.
- `MarkNotificationReadUseCase` y `MarkAllNotificationsReadUseCase`: cambian el estado de lectura.
- `DeleteNotificationUseCase`: elimina una notificación propia.

Usan `NotificationRepository`; las consultas y modificaciones de la bandeja obtienen el destinatario de la sesión.

<br>

### `reputation/`

Acciones de puntos e insignias:

- `AwardPointsUseCase`: registra puntos por una acción y puede notificar un cambio de nivel.
- `EvaluateMyBadgesUseCase`: calcula el progreso y desbloquea insignias pendientes.
- `GetMyPointHistoryUseCase`: consulta el historial de puntos propio.
- `LoadBadgesUseCase` y `ObserveBadgesUseCase`: cargan u observan el catálogo de insignias.

Usan `ReputationRepository` y, para calcular progreso o notificar, `UserRepository`, `PostRepository`, `NotifyUseCase` y `RequireUserUseCase`.

<br>

### `validator/`

Validaciones de formato independientes de la interfaz:

- `AuthValidator`: correo, contraseña y código de doble factor.
- `UserValidator`: nombre, alias y teléfono.
- `PostValidator`: título, descripción, fotos, edad, ubicación y motivo de rechazo.
- `CatalogValidator`: nombres e identificadores de catálogo.
- `ChatValidator`: mensajes de chat e interés de adopción.
- `CommentValidator`: texto de comentarios.

Los casos de uso llaman estas reglas antes de solicitar cambios al repositorio. Las validaciones de formato no reemplazan la comprobación de permisos ni las reglas de estado.

<br>

## Conexión con las capas

```text
Pantalla / ViewModel
        ↓
Caso de uso en domain/usecase
  valida, autoriza y coordina
        ↓
Modelo de domain/model
  representa los datos y estados
        ↓
Contrato de domain/repository
  define las operaciones de datos
        ↓
Implementación en data/
  ejecuta las operaciones
```

Un caso de uso puede coordinar varios repositorios o casos de uso cuando una acción del negocio lo requiere. Por ejemplo, aprobar una publicación actualiza la verificación y puede generar una notificación y otorgar puntos.

