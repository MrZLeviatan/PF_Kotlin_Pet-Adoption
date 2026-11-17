# Casos de Uso

## Propósito

Cada caso de uso representa **una acción de la aplicación** con sus reglas: iniciar sesión, publicar, aprobar una publicación, comentar, enviar un mensaje. Un caso de uso valida los datos, verifica los permisos de quien actúa, coordina los repositorios necesarios y devuelve un `Result`.

Los casos de uso son clases Kotlin sin anotaciones de Hilt. Reciben sus dependencias por constructor y se construyen en `di/UseCaseModule.kt`. Se invocan como funciones gracias al operador `invoke`:

```kotlin
loginUseCase(Credentials(email, password))
    .onSuccess { result -> /* continuar */ }
    .onFailure { error -> /* mostrar el mensaje correspondiente */ }
```

## Tipos de casos de uso

| Tipo | Descripción | Ejemplos |
|---|---|---|
| Acción principal | Lo invoca un ViewModel en respuesta a la persona usuaria | `CreatePostUseCase`, `LoginUseCase` |
| Auxiliar de permisos | Confirma la identidad o el rol antes de una acción | `RequireUserUseCase`, `RequireModeratorUseCase`, `RequireChatMemberUseCase` |
| Interno de mejor esfuerzo | Lo llaman otros casos de uso después de la acción principal; si falla, no la revierte | `NotifyUseCase`, `AwardPointsUseCase` |
| Validador | Objeto con reglas de formato, sin acceso a datos | `AuthValidator`, `PostValidator` |

## Contenido por carpeta

### `auth/` — Autenticación y sesión

| Archivo | Responsabilidad |
|---|---|
| `LoginUseCase` | Valida el correo y autentica. Si la cuenta tiene verificación en dos pasos, solicita el código; si no, verifica el perfil. Ante cualquier fallo, cierra la sesión incompleta. |
| `VerifyTwoFactorCodeUseCase` | Valida el formato del código de 6 dígitos, lo verifica contra el reto guardado y confirma el perfil. |
| `ResendTwoFactorCodeUseCase` | Genera y envía un nuevo código cuando el anterior venció o no llegó. |
| `RegisterUseCase` | Valida los datos, comprueba duplicados de alias y teléfono, crea la cuenta en Firebase Auth y guarda el perfil. Si el perfil no se guarda, elimina la cuenta creada. |
| `CheckRegistrationAvailabilityUseCase` | Antes del segundo paso del registro, indica si el correo o el teléfono ya están en uso. Define `RegistrationAvailability`. |
| `RestoreSessionUseCase` | Al abrir la aplicación, recupera la sesión guardada y confirma que el perfil siga siendo válido. |
| `ObserveSessionUseCase` | Expone la cuenta de la sesión actual como flujo. |
| `SendPasswordResetUseCase` | Valida el correo y solicita el enlace de restablecimiento. |
| `ChangePasswordUseCase` | Valida que la nueva contraseña sea segura y distinta de la actual, y la actualiza. |
| `LogoutUseCase` | Cierra la sesión. |

### `user/` — Perfiles y permisos

| Archivo | Responsabilidad |
|---|---|
| `CheckProfileUseCase` | Confirma que una cuenta autenticada tenga un perfil válido, no eliminado, y que un moderador esté activo. |
| `RequireUserUseCase` | Devuelve la cuenta de la sesión si es un usuario. |
| `RequireModeratorUseCase` | Devuelve la cuenta de la sesión si es un moderador activo. |
| `RequireOwnUserUseCase` | Confirma que la persona en sesión es la titular del perfil a modificar. |
| `ObserveCurrentUserUseCase` | Expone el perfil del usuario en sesión como flujo. |
| `GetCurrentModeratorUseCase` | Devuelve el perfil del moderador en sesión. |
| `GetUserProfileUseCase` | Construye el `PublicProfile` de otra persona según su privacidad. Los moderadores lo ven completo. |
| `SearchUsersUseCase` | Busca usuarios por prefijo del alias, ignorando textos demasiado cortos. |
| `UpdateProfileUseCase` | Valida y guarda nombre, teléfono, foto y ubicación. Sube la foto nueva a Cloudinary. |
| `UpdateSettingsUseCase` | Guarda privacidad del perfil y verificación en dos pasos. |
| `UpdateModeratorTwoFactorUseCase` | Activa o desactiva la verificación en dos pasos de un moderador. |
| `DeleteAccountUseCase` | Reautentica, cierra las publicaciones, anonimiza el perfil y elimina la cuenta. Cada paso es repetible sin efectos duplicados. |

### `post/` — Publicaciones y moderación

| Archivo | Responsabilidad |
|---|---|
| `CreatePostUseCase` | Valida el borrador, comprueba el catálogo, sube las fotos, guarda la publicación como no verificada y programa el análisis de IA. |
| `UpdatePostUseCase` | Edita una publicación propia mientras no esté verificada ni rechazada; reinicia el análisis de IA. |
| `DeletePostUseCase` | Borrado lógico de una publicación propia no finalizada. |
| `FinishPostUseCase` | Marca una publicación como finalizada (autor o moderador) y otorga puntos al autor. |
| `GetFeedUseCase` | Entrega una página del feed público con los filtros elegidos. |
| `GetPostDetailUseCase` | Abre el detalle aplicando las reglas de visibilidad según el rol. |
| `GetMyPostsUseCase` | Lista las publicaciones propias, excepto las eliminadas. |
| `GetUserPostsUseCase` | Lista las publicaciones visibles en el perfil de otra persona, según su privacidad. |
| `GetPostStatsUseCase` | Calcula las métricas personales mediante conteos. |
| `GetModerationQueueUseCase` | Entrega la cola de revisión ordenada por prioridad y antigüedad. |
| `StartReviewUseCase` | El moderador toma una publicación para revisarla. |
| `ApprovePostUseCase` | Aprueba la publicación, notifica al autor y le otorga puntos. |
| `RejectPostUseCase` | Rechaza la publicación con motivo obligatorio y notifica al autor. |
| `GetMyReviewsUseCase` | Lista las publicaciones que el moderador tiene en revisión. |
| `GetModerationStatsUseCase` | Cuenta las publicaciones sin moderador por prioridad. |
| `GetModeratorPerformanceUseCase` | Calcula el panel de desempeño del moderador. |

### `ai/` — Inteligencia artificial

| Archivo | Responsabilidad |
|---|---|
| `AnalyzePostUseCase` | Reúne publicaciones similares como candidatas a duplicado (mismo autor, o misma categoría a menos de 10 km), solicita el análisis y guarda la revisión y la prioridad sugerida. No cambia el estado de verificación. |

### `catalog/` — Catálogos

| Archivo | Responsabilidad |
|---|---|
| `LoadCatalogUseCase` | Carga categorías y tipos de animal. |
| `ObserveCategoriesUseCase` / `ObserveAnimalTypesUseCase` | Exponen los catálogos como flujos para filtros y formularios. |
| `CheckPostCatalogUseCase` | Confirma que la categoría y el tipo de animal de una publicación existan y estén activos. |
| `CreateCategoryUseCase` / `CreateAnimalTypeUseCase` | Un moderador agrega un elemento; el id se deriva del nombre para evitar duplicados. |
| `UpdateCategoryUseCase` / `UpdateAnimalTypeUseCase` | Un moderador renombra, activa o desactiva un elemento. No existe el borrado. |

### `comment/` — Comentarios

| Archivo | Responsabilidad |
|---|---|
| `CreateCommentUseCase` | Crea un comentario o una respuesta en publicaciones visibles; notifica a los involucrados y otorga puntos. |
| `EditCommentUseCase` | Edita un comentario propio activo. |
| `DeleteCommentUseCase` | Borrado lógico por el autor o por un moderador. |
| `GetCommentsUseCase` | Entrega una página de comentarios o respuestas, respetando la visibilidad de la publicación. |
| `ToggleCommentLikeUseCase` | Da o quita el "me gusta". |
| `GetLikedCommentsUseCase` | Indica qué comentarios de una publicación tienen el "me gusta" de la persona. |

### `importantVote/` — Votos "Es importante"

| Archivo | Responsabilidad |
|---|---|
| `ToggleImportantUseCase` | Da o quita el voto en publicaciones visibles, nunca en la propia; notifica y otorga puntos al autor. |
| `GetMyVotedPostIdsUseCase` | Indica a cuáles publicaciones de una página ya votó la persona. |
| `GetMyImportantPostsUseCase` | Lista las publicaciones marcadas como importantes que siguen visibles. |

### `chat/` y `chat/adoption/` — Mensajería y adopción

| Archivo | Responsabilidad |
|---|---|
| `StartChatUseCase` | Abre o recupera el chat con el autor de una publicación (id determinista). |
| `GetMyChatsUseCase` | Lista los chats de la persona, del más reciente al más antiguo. |
| `ObserveMessagesUseCase` | Entrega los mensajes en tiempo real tras confirmar la pertenencia. |
| `GetOlderMessagesUseCase` | Carga mensajes anteriores al desplazarse hacia arriba. |
| `SendMessageUseCase` | Envía texto o foto y notifica solo con el primer mensaje no leído. |
| `MarkChatReadUseCase` | Marca el chat como leído. |
| `CountUnreadChatsUseCase` | Cuenta los chats con mensajes no leídos. |
| `DeleteChatUseCase` | Elimina el chat para quien lo pide o para ambos; cancela la solicitud de adopción pendiente cuando corresponde. |
| `RequireChatMemberUseCase` | Confirma que la persona participa en el chat. Define `ChatMembership`. |
| `adoption/SendAdoptionInterestUseCase` | Crea la solicitud, abre el chat y deja el mensaje automático en una sola transacción; notifica al autor. |
| `adoption/RespondAdoptionRequestUseCase` | El autor acepta o rechaza una solicitud pendiente y notifica al solicitante. |
| `adoption/CancelAdoptionRequestUseCase` | El solicitante cancela su solicitud pendiente. |
| `adoption/GetMyAdoptionRequestUseCase` | Devuelve la solicitud propia sobre una publicación. |
| `adoption/GetPostAdoptionRequestsUseCase` | Lista las solicitudes recibidas por una publicación (solo para su autor). |

### `notification/`, `reputation/`, `image/` y `location/`

| Archivo | Responsabilidad |
|---|---|
| `notification/NotifyUseCase` | Crea una notificación para otra persona. Lo usan los demás casos de uso. |
| `notification/GetNotificationsUseCase` | Entrega una página de notificaciones. |
| `notification/CountUnreadNotificationsUseCase` | Cuenta las no leídas. |
| `notification/MarkNotificationReadUseCase` / `MarkAllNotificationsReadUseCase` | Marcan una o todas como leídas. |
| `notification/DeleteNotificationUseCase` | Elimina una notificación propia. |
| `reputation/AwardPointsUseCase` | Otorga puntos una sola vez por hecho, aplica el tope diario de comentarios y notifica la subida de nivel. |
| `reputation/EvaluateMyBadgesUseCase` | Revisa si la persona cumple requisitos de insignias pendientes y las desbloquea. |
| `reputation/GetMyPointHistoryUseCase` | Entrega el historial de puntos por páginas. |
| `reputation/LoadBadgesUseCase` / `ObserveBadgesUseCase` | Cargan y exponen el catálogo de insignias. |
| `image/UploadImagesUseCase` | Sube a Cloudinary las fotos locales y conserva las URL ya remotas. |
| `location/SearchAddressUseCase` | Busca direcciones dentro de la ciudad elegida. |
| `location/ReverseGeocodeUseCase` | Convierte un punto del mapa en una ubicación con dirección. |

### `validator/` — Reglas de formato

| Archivo | Funciones |
|---|---|
| `AuthValidator` | `isEmailValid`, `isPasswordValid` (mínimo 8 caracteres, un número y una mayúscula), `isCodeValid` |
| `UserValidator` | `isNameValid`, `isUserNameValid`, `isPhoneValid` |
| `PostValidator` | `isTitleValid`, `isDescriptionValid`, `isRejectionReasonValid`, `validate` |
| `CommentValidator` | `isTextValid` |
| `ChatValidator` | `isMessageValid`, `isCaptionValid`, `isAdoptionMessageValid` |
| `CatalogValidator` | `isNameValid`, `buildId` |

## Flujos representativos

**Publicar una mascota**

```text
CreatePostViewModel
  └─► CreatePostUseCase (recibe el id del autor desde la sesión del ViewModel)
        ├─ PostValidator ................. título, descripción, fotos, ubicación
        ├─ CheckPostCatalogUseCase ....... categoría y tipo de animal activos
        ├─ UploadImagesUseCase ........... fotos a Cloudinary
        ├─ PostRepository.create ......... publicación sin verificar
        └─ PostAnalysisScheduler.schedule  análisis de IA en segundo plano
```

**Aprobar una publicación**

```text
PostDetailViewModel
  └─► ApprovePostUseCase
        ├─ RequireModeratorUseCase ....... moderador activo
        ├─ PostRepository.updateVerification (transacción: solo si sigue pendiente)
        ├─ AwardPointsUseCase ............ puntos para el autor
        └─ NotifyUseCase ................. aviso al autor
```

**Iniciar sesión con verificación en dos pasos**

```text
LoginViewModel ─► LoginUseCase ─► AuthRepository.login ─► TwoFactorRequired
                                └► AuthRepository.requestTwoFactorCode (correo con el código)
TwoFactorDialog ─► VerifyTwoFactorCodeUseCase ─► AuthRepository.verifyTwoFactorCode
                                              └► CheckProfileUseCase ─► sesión abierta
```

## Guía de extensión

- Un caso de uso debe representar una sola acción. Si su lógica crece, se divide en casos de uso más pequeños que se combinan.
- Los permisos se comprueban en el caso de uso aunque la interfaz oculte el botón. Las reglas de Firestore constituyen la última barrera.
- Un caso de uso nuevo se registra en `di/UseCaseModule.kt` mediante una función `@Provides`.
