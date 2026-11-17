# Capa de Datos

## Propósito

La capa de datos implementa los contratos definidos en `domain/repository` y es la única parte de la aplicación que se comunica con servicios externos: **Firebase Authentication**, **Cloud Firestore**, **Cloudinary**, **OpenRouter**, **EmailJS** y el **geocodificador de Android**. Traduce entre los modelos del dominio y el formato que exige cada servicio, y convierte los errores técnicos en errores del dominio.

Ningún caso de uso ni pantalla conoce estas clases. Hilt entrega la implementación adecuada a quien solicita una interfaz (`di/RepositoryModule.kt`).

## Estructura

```text
data/repository/
├── firebase/    Firebase Authentication y Cloud Firestore
├── remote/      Servicios HTTP: Cloudinary, OpenRouter y EmailJS
└── device/      Servicios del sistema operativo: geocodificador
```

## Contenido por archivo

### `firebase/`

| Archivo | Implementa | Responsabilidad | Funciones principales |
|---|---|---|---|
| `FirebaseAuthRepository.kt` | `AuthRepository` | Inicio de sesión con correo y contraseña, registro, restauración de la sesión, recuperación y cambio de contraseña, reautenticación, eliminación de la cuenta y verificación en dos pasos propia. El rol y el alias se leen del perfil en Firestore. | `login`, `restoreSession`, `register`, `requestTwoFactorCode`, `verifyTwoFactorCode`, `sendPasswordReset`, `changePassword`, `deleteAccount` |
| `FirestoreUserRepository.kt` | `UserRepository` | Perfiles en `users` y `moderators`. Mantiene los índices de valores únicos (`emails`, `userNames`, `phones`) y escucha en tiempo real el perfil de la sesión. | `save`, `findById`, `searchByUserName`, `existsByPhone`, `updateProfile`, `updateSettings`, `anonymize` |
| `FirestorePostRepository.kt` | `PostRepository` | Publicaciones en `posts`. Filtra en Firestore por estado y refina en la aplicación (ciudad, tamaño, vacunas, distancia). Verifica publicaciones mediante transacción. | `create`, `getFeed`, `getPendingModeration`, `updateVerification`, `updateAiAnalysis`, `countForModeration` |
| `FirestoreCatalogRepository.kt` | `CatalogRepository` | Categorías y tipos de animal escuchados en tiempo real desde el arranque; expuestos como `StateFlow`. | `categories`, `animalTypes`, `refresh`, `createCategory`, `updateCategory` |
| `FirestoreCommentRepository.kt` | `CommentRepository` | Comentarios en `posts/{id}/comments` y "me gusta" en `commentLikes`. Ajusta los contadores en la misma transacción. | `create`, `getTopLevel`, `getReplies`, `toggleLike`, `markDeleted` |
| `FirestoreImportantVoteRepository.kt` | `ImportantVoteRepository` | Votos en `importantVotes` y su contador en la publicación. | `toggle`, `hasVoted`, `getVotedAmong`, `getVotedPostIds` |
| `FirestoreNotificationRepository.kt` | `NotificationRepository` | Notificaciones en `notifications`; usa consultas de agregación para contar las no leídas. | `create`, `getByRecipient`, `countUnread`, `markAllAsRead` |
| `FirestoreChatRepository.kt` | `ChatRepository` | Chats, mensajes en tiempo real y solicitudes de adopción. Mensaje y resumen del chat se guardan en una transacción. | `openChat`, `sendMessage`, `observeMessages`, `sendAdoptionInterest`, `updateRequestStatus` |
| `FirestoreReputationRepository.kt` | `ReputationRepository` | Historial en `pointTransactions`, puntos e insignias dentro del perfil y catálogo de insignias. | `award`, `unlockBadge`, `getTransactions`, `countByReason` |
| `FirestoreMapper.kt` | — | Convierte modelos `@Serializable` en mapas de Firestore y viceversa. | `toFirestore()`, `toModel()`, `toModels()` |
| `FirestoreCollections.kt` | — | Nombres de todas las colecciones, compartidos con las reglas y el script de datos. | Constantes |

### `remote/`

| Archivo | Implementa | Responsabilidad | Funciones principales |
|---|---|---|---|
| `CloudinaryImageRepository.kt` | `ImageStorageRepository` | Sube imágenes a Cloudinary mediante su API REST con un *upload preset* sin firma. | `upload` |
| `OpenRouterAiRepository.kt` | `AiAnalysisRepository` | Envía la publicación a un modelo de lenguaje y convierte su respuesta JSON en un `AiPostAnalysis`. | `analyze`, `parseAiAnswer` |
| `BackgroundPostAnalysisScheduler.kt` | `PostAnalysisScheduler` | Ejecuta `AnalyzePostUseCase` en un alcance de corrutinas propio de la aplicación, independiente de la pantalla. | `schedule` |
| `EmailJsCodeSender.kt` | — (lo usa `FirebaseAuthRepository`) | Envía el correo con el código de verificación en dos pasos. | `sendTwoFactorCode` |

### `device/`

| Archivo | Implementa | Responsabilidad | Funciones principales |
|---|---|---|---|
| `AndroidGeocodingRepository.kt` | `GeocodingRepository` | Usa `android.location.Geocoder` para convertir direcciones en coordenadas y coordenadas en direcciones. No requiere llaves. | `search`, `reverse` |

## Modelo de datos en Firestore

| Colección | Documento | Contenido |
|---|---|---|
| `users` | `{uid}` | Perfil de usuario y campo auxiliar `userNameLower` para la búsqueda |
| `moderators` | `{uid}` | Perfil de moderador; su existencia define el rol |
| `emails`, `userNames`, `phones` | `{valor}` | `{ uid }` del propietario; garantizan unicidad sin exponer los perfiles |
| `twoFactorChallenges` | `{uid}` | Huella SHA-256 del código, vencimiento e intentos |
| `categories`, `animalTypes`, `badges` | `{id}` | Catálogos |
| `posts` | `{id}` | Publicación con su verificación y análisis de IA |
| `posts/{id}/comments` | `{id}` | Comentarios y respuestas |
| `commentLikes` | `{commentId_userId}` | "Me gusta" |
| `importantVotes` | `{postId_userId}` | Votos "Es importante" |
| `notifications` | `{id}` | Notificaciones con su destinatario |
| `chats` | `{userA_userB_postId}` | Resumen de la conversación |
| `chats/{id}/messages` | `{id}` | Mensajes |
| `adoptionRequests` | `{postId_requesterId}` | Solicitudes de adopción |
| `pointTransactions` | `{userId_MOTIVO_origen}` | Historial de puntos |
| `deletionFeedback` | `{id}` | Motivo anónimo de eliminación de cuenta |

## Funcionamiento interno

### Conversión de modelos (`FirestoreMapper`)

El SDK de Firestore solo puede deserializar clases con propiedades mutables y constructor vacío. Los modelos del dominio son inmutables, por lo que la conversión se realiza a través de JSON con kotlinx.serialization:

```text
Modelo ──encodeToJsonElement──► JsonObject ──fromJson──► Map<String, Any?> ──set()──► Firestore
Firestore ──get()──► Map<String, Any?> ──toJson──► JsonObject ──decodeFromJsonElement──► Modelo
```

La configuración escribe los valores por defecto (necesarios para las consultas), ignora campos desconocidos y guarda los enumerados como texto. Un documento mal formado se descarta en lugar de interrumpir la pantalla.

### Consistencia

- **Transacciones** en toda operación que modifica varios documentos o depende del estado actual: verificación de publicaciones, comentarios y respuestas, votos, "me gusta", mensajes, solicitudes de adopción, puntos, insignias y validación del código A2F.
- **Identificadores deterministas** para votos, chats, solicitudes y transacciones de puntos, de modo que las operaciones repetidas no generan duplicados.
- **Traducción de errores.** Las excepciones de Firebase se convierten en errores del dominio; `unwrapTransactionError()` recupera el error original lanzado dentro de una transacción.

### Tiempo real y caché

Los *snapshot listeners* mantienen actualizados el perfil de la sesión, los catálogos, las insignias y los mensajes del chat. Firestore conserva una copia local, lo que permite abrir la aplicación con rapidez y consultar datos sin conexión.

## Servicios externos

La configuración de cada servicio se lee de `local.properties` y llega al código como constantes de `BuildConfig` (ver el [README principal](../../../../../../../../../README.md#configuración-de-llaves)).

### Firebase Authentication

1. `login` autentica con correo y contraseña y lee el perfil para conocer el rol y la configuración de A2F.
2. Si la cuenta no tiene A2F, publica la cuenta en `currentAccount` y la sesión queda abierta.
3. `restoreSession` recupera la sesión al abrir la aplicación; si había una verificación en dos pasos pendiente, la cierra.
4. `sendPasswordResetEmail` envía el enlace de restablecimiento. Por seguridad, la respuesta es la misma exista o no la cuenta.

### Verificación en dos pasos con EmailJS

```text
login correcto ─► perfil con A2F activa
   └─ requestTwoFactorCode
        ├─ genera un código de 6 dígitos con SecureRandom
        ├─ guarda SHA-256(uid:código) en twoFactorChallenges/{uid} (5 minutos, máximo 5 intentos)
        └─ EmailJsCodeSender ─► POST https://api.emailjs.com/api/v1.0/email/send
verifyTwoFactorCode ─► transacción: vencido / agotado / incorrecto (+1 intento) / correcto (un solo uso)
                    ─► se abre la sesión de la aplicación
```

La petición a EmailJS incluye `service_id`, `template_id`, `user_id` (llave pública), `accessToken` (llave privada, cuando la cuenta está en modo estricto) y los parámetros de la plantilla: `to_email`, `user_name`, `code` y `minutes`.

### Cloudinary

`POST https://api.cloudinary.com/v1_1/{cloud_name}/image/upload` en formato *multipart*, con el archivo, el `upload_preset` sin firma y la carpeta `KogMaw-Adoption App/{posts|profiles|chats}`. La respuesta contiene la `secure_url` que se guarda en el modelo. Antes de subirla, la imagen se reduce a 1600 píxeles en `core/util/LocalImageStore`.

### OpenRouter

`POST https://openrouter.ai/api/v1/chat/completions` (API compatible con OpenAI) con `Authorization: Bearer <llave>`, temperatura 0.2 y respuesta en formato JSON. El mensaje incluye el texto de la publicación, hasta tres imágenes (como URL o en base64) y hasta diez publicaciones similares. `parseAiAnswer` valida la respuesta, acota los valores y descarta duplicados que no estén entre las candidatas enviadas.

### Geocodificador de Android

`Geocoder.getFromLocationName` y `Geocoder.getFromLocation`, con la API asíncrona en Android 13 o superior. No requiere llaves; si el dispositivo no ofrece el servicio, la persona puede elegir el punto directamente en el mapa.

## Guía de extensión

1. Declarar la operación en la interfaz del dominio e implementarla en la clase correspondiente.
2. Para un modelo nuevo, marcarlo con `@Serializable`, asignar valores por defecto y usar `toFirestore()` y `toModel()`.
3. Si una consulta combina filtros con orden, agregar el índice compuesto en `firebase/firestore.indexes.json` y publicarlo.
4. Si se agrega una colección, declararla en `FirestoreCollections` y protegerla en `firebase/firestore.rules`.
