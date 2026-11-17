<h1 align="center">

_Casos de uso_

</h1>

**En pocas palabras:** cada caso de uso es **una acción** de la app (iniciar sesión, publicar, aprobar, comentar…) con sus **reglas**. Valida los datos, revisa los permisos y pide a los repositorios lo que necesita. Las pantallas los llaman como si fueran funciones:

```kotlin
loginUseCase(Credentials(email, password))
    .onSuccess { /* entrar */ }
    .onFailure { error -> /* mostrar mensaje */ }
```

<br>

## Qué hay aquí

| Carpeta | Acciones |
|---|---|
| `auth/` | `Login`, `Register`, `CheckRegistrationAvailability`, `VerifyTwoFactorCode`, `ResendTwoFactorCode`, `SendPasswordReset`, `ChangePassword`, `RestoreSession`, `ObserveSession`, `Logout` |
| `user/` | `CheckProfile`, `RequireUser`, `RequireModerator`, `RequireOwnUser`, `GetUserProfile`, `SearchUsers`, `ObserveCurrentUser`, `GetCurrentModerator`, `UpdateProfile`, `UpdateSettings`, `UpdateModeratorTwoFactor`, `DeleteAccount` |
| `post/` | `CreatePost`, `UpdatePost`, `DeletePost`, `FinishPost`, `GetFeed`, `GetPostDetail`, `GetMyPosts`, `GetUserPosts`, `GetPostStats`, y para moderar: `GetModerationQueue`, `StartReview`, `ApprovePost`, `RejectPost`, `GetMyReviews`, `GetModerationStats`, `GetModeratorPerformance` |
| `ai/` | `AnalyzePost` |
| `catalog/` | `LoadCatalog`, `ObserveCategories`, `ObserveAnimalTypes`, `CheckPostCatalog`, `CreateCategory`, `UpdateCategory`, `CreateAnimalType`, `UpdateAnimalType` |
| `comment/` | `GetComments`, `CreateComment`, `EditComment`, `DeleteComment`, `ToggleCommentLike`, `GetLikedComments` |
| `importantVote/` | `ToggleImportant`, `GetMyVotedPostIds`, `GetMyImportantPosts` |
| `chat/` | `StartChat`, `GetMyChats`, `ObserveMessages`, `SendMessage`, `GetOlderMessages`, `MarkChatRead`, `CountUnreadChats`, `DeleteChat`, `RequireChatMember` |
| `chat/adoption/` | `SendAdoptionInterest`, `GetMyAdoptionRequest`, `GetPostAdoptionRequests`, `RespondAdoptionRequest`, `CancelAdoptionRequest` |
| `notification/` | `Notify`, `GetNotifications`, `CountUnreadNotifications`, `MarkNotificationRead`, `MarkAllNotificationsRead`, `DeleteNotification` |
| `reputation/` | `AwardPoints`, `EvaluateMyBadges`, `GetMyPointHistory`, `LoadBadges`, `ObserveBadges` |
| `image/` | `UploadImages` |
| `location/` | `SearchAddress`, `ReverseGeocode` |
| `validator/` | `AuthValidator`, `UserValidator`, `PostValidator`, `CommentValidator`, `ChatValidator`, `CatalogValidator` |

*(Todos los nombres terminan en `UseCase`, por ejemplo `LoginUseCase`; los validadores son objetos con reglas de formato.)*

<br>

## Ejemplos de cómo trabajan juntos

**Aprobar una publicación** (`ApprovePostUseCase`):
1. `RequireModerator` comprueba que quien aprueba sea un moderador activo.
2. Se guarda la verificación solo si la publicación sigue pendiente (otro moderador pudo decidirla antes).
3. `AwardPoints` suma puntos al autor y `Notify` le envía un aviso.

**Publicar** (`CreatePostUseCase`):
1. `PostValidator` revisa título, descripción, fotos y ubicación.
2. `CheckPostCatalog` confirma que la categoría y el tipo de animal existan y estén activos.
3. `UploadImages` sube las fotos a Cloudinary.
4. Se guarda la publicación como **sin verificar** y se programa el análisis de IA en segundo plano.

**Iniciar sesión** (`LoginUseCase`):
1. `AuthValidator` revisa el correo.
2. Si la cuenta usa verificación en dos pasos, se envía el código y la pantalla lo pide.
3. `CheckProfile` confirma que el perfil exista y no esté eliminado ni inactivo.

<br>

## Para desarrolladores

- **Un caso de uso = una acción.** Si crece demasiado, divide la lógica en otros casos de uso y combínalos.
- **No dependen de Hilt ni de Android:** reciben sus repositorios por constructor y `di/UseCaseModule.kt` los fabrica.
- **Siempre devuelven `Result`** con errores del dominio (`error/`), nunca mensajes de texto.
- **Los permisos se revisan aquí**, aunque la pantalla ya oculte el botón: es la segunda barrera (la tercera son las reglas de Firestore).
