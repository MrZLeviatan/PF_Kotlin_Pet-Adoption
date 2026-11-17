# Capa de Dominio

## Propósito

La capa de dominio contiene las **reglas de negocio** de la aplicación. Define qué información existe (publicaciones, usuarios, comentarios, chats, reputación), qué acciones se pueden realizar y bajo qué condiciones. Es código Kotlin puro: no depende de Android, de Jetpack Compose ni de Firebase, lo que permite probarla de forma aislada y cambiar la tecnología de almacenamiento sin modificar las reglas.

<br>

## Ubicación en la arquitectura

```text
features (presentación)  ──llama a──►  domain/usecase
                                            │ usa
                                            ▼
                                     domain/model  +  domain/repository (interfaces)
                                                              ▲
data (implementaciones)  ──────────implementa─────────────────┘
```

El dominio es el centro de la arquitectura: las demás capas dependen de él, pero él no depende de ninguna.

<br>

## Estructura

| Carpeta | Contenido | Documentación |
|---|---|---|
| `model/` | Entidades y objetos de valor del negocio | [model/README.md](./model/README.md) |
| `usecase/` | Acciones de la aplicación y sus reglas | [usecase/README.md](./usecase/README.md) |
| `repository/` | Contratos (interfaces) de acceso a datos | [repository/README.md](./repository/README.md) |
| `error/` | Errores tipados del negocio | Descrita en esta página |

<br>

## Errores del dominio (`error/`)

Cada área funcional define una `sealed class` que extiende `Exception`. Los casos de uso devuelven estos errores dentro de `Result.failure(...)`; los ViewModels los traducen a mensajes localizados de `strings.xml`. De esta forma, ningún mensaje técnico llega a la persona usuaria.

| Archivo | Errores que define | Quién los produce |
|---|---|---|
| `AuthException.kt` | `InvalidEmail`, `WeakPassword`, `EmailAlreadyInUse`, `WrongPassword`, `SamePassword`, `InactiveModerator`, `NotModerator`, `InvalidCode`, `WrongCode`, `CodeExpired`, `TooManyAttempts`, `CodeNotSent`, `NotAuthenticated` | Casos de uso de `usecase/auth` y `AuthRepository` |
| `UserException.kt` | `InvalidName`, `InvalidUserName`, `InvalidPhone`, `InvalidLocation`, `UserNameTaken`, `PhoneTaken`, `ProfileNotFound`, `UserNotFound`, `UserDeleted`, `NotAllowed`, `ProfileMismatch`, `AccountDeleted`, `DeletionIncomplete` | `UserValidator`, `usecase/user`, `usecase/auth` |
| `PostException.kt` | `InvalidTitle`, `InvalidDescription`, `InvalidCategory`, `NoPhotos`, `TooManyPhotos`, `InvalidAge`, `InvalidLocation`, `PostNotFound`, `NotAllowed`, `NotModerator`, `PostNotActive`, `AlreadyReviewed`, `RejectionReasonRequired`, `ReviewedByAnotherModerator` | `PostValidator`, `usecase/post` |
| `CommentException.kt` | `InvalidText`, `CommentNotFound`, `CannotReplyDeleted`, `NotAllowed` | `CommentValidator`, `usecase/comment` |
| `ChatException.kt` | `InvalidMessage`, `ChatNotFound`, `CannotChatWithSelf` | `ChatValidator`, `usecase/chat` |
| `AdoptionException.kt` | `CannotRequestOwnPost`, `NotAdoptable`, `AlreadyRequested`, `RequestNotFound`, `AlreadyResponded`, `InvalidMessage` | `usecase/chat/adoption` |
| `CatalogException.kt` | `InvalidName`, `CategoryAlreadyExists`, `AnimalTypeAlreadyExists`, `CategoryNotFound`, `AnimalTypeNotFound`, `CategoryInactive`, `AnimalTypeInactive` | `CatalogValidator`, `usecase/catalog` |
| `NotificationException.kt` | `NotificationNotFound` | `usecase/notification` |
| `VoteException.kt` | `CannotVoteOwnPost` | `ToggleImportantUseCase` |
| `ImageException.kt` | `NotConfigured`, `UploadFailed` | `ImageStorageRepository` (Cloudinary) |
| `AiException.kt` | `NotConfigured`, `AnalysisFailed` | `AiAnalysisRepository` (OpenRouter) |

<br>

## Principios de diseño

- **Inmutabilidad.** Los modelos son `data class` con propiedades `val`. Un cambio produce una copia (`post.copy(...)`), lo que evita estados inconsistentes.
- **Resultados explícitos.** Toda operación que puede fallar devuelve `Result<T>`; nunca se lanza una excepción hacia la interfaz.
- **Identidad desde la sesión.** Los casos de uso que requieren permisos obtienen la cuenta de la sesión actual (`RequireUserUseCase`, `RequireModeratorUseCase`) y nunca confían en un identificador recibido por parámetro.
- **Independencia tecnológica.** La única anotación externa en los modelos es `@Serializable` (kotlinx.serialization, Kotlin puro), que la capa de datos utiliza para convertirlos en documentos de Firestore.

<br>

## Relación con otras capas

| Capa | Relación con el dominio |
|---|---|
| `features/` | Sus ViewModels invocan casos de uso y muestran sus resultados |
| `data/` | Implementa los contratos de `repository/` con Firebase y servicios externos |
| `di/` | Enlaza cada contrato con su implementación y construye los casos de uso |
| `core/` | Sin relación directa; contiene únicamente elementos visuales y utilidades |

<br>

## Guía de extensión

1. Una regla nueva se implementa en un caso de uso, nunca en un ViewModel ni en un repositorio.
2. Una validación de formato se agrega al validador correspondiente de `usecase/validator/`.
3. Un error nuevo se declara en la `sealed class` de su área y se traduce a un mensaje en la pantalla que lo muestra.
4. Un dato nuevo que deba persistirse se agrega al modelo con valor por defecto, para mantener la compatibilidad con los documentos ya guardados.
