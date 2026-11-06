<h1 align="center">

_Capa de Datos_

</h1>

La capa `data` es la **única parte de la aplicación que habla con el mundo exterior**: Firebase (Authentication y Firestore), Cloudinary, OpenRouter, EmailJS y los servicios del propio teléfono (geocodificador). Implementa los contratos que define `domain/repository` y traduce entre los modelos del dominio y el formato de cada servicio.

Ninguna pantalla ni caso de uso conoce estas clases: reciben las interfaces del dominio y Hilt (`di/RepositoryModule.kt`) decide qué implementación entregar. Si mañana se cambia Firestore por otra base de datos, solo cambia esta carpeta.

<br>

## Estructura

```text
data/repository/
├── firebase/   → Firebase Authentication + Cloud Firestore (datos y sesión)
├── remote/     → Servicios HTTP externos: Cloudinary, OpenRouter, EmailJS
└── device/     → Servicios del teléfono: geocodificador de Android
```

<br>

### `firebase/`

| Archivo | Implementa | Qué hace |
|---|---|---|
| `FirebaseAuthRepository` | `AuthRepository` | Inicio de sesión, registro, sesión guardada, recuperación y cambio de contraseña, reautenticación, borrado de cuenta y la verificación en dos pasos (A2F). |
| `FirestoreUserRepository` | `UserRepository` | Perfiles (`users`, `moderators`), unicidad de correo, alias y teléfono, búsqueda por alias, ajustes y anonimización. Escucha en vivo el perfil de la sesión. |
| `FirestorePostRepository` | `PostRepository` | Publicaciones: crear, feed con filtros y paginación, cola de moderación, verificación en transacción, análisis de IA. |
| `FirestoreCatalogRepository` | `CatalogRepository` | Categorías y tipos de animal, escuchados en vivo desde que abre la app. |
| `FirestoreCommentRepository` | `CommentRepository` | Comentarios y respuestas en `posts/{id}/comments`, "me gusta" en `commentLikes`. |
| `FirestoreImportantVoteRepository` | `ImportantVoteRepository` | Votos "Es importante" (`importantVotes/{postId_userId}`) y su contador. |
| `FirestoreNotificationRepository` | `NotificationRepository` | Bandeja de notificaciones, no leídas (conteo agregado) y marcado. |
| `FirestoreChatRepository` | `ChatRepository` | Chats, mensajes en vivo y solicitudes de adopción. |
| `FirestoreReputationRepository` | `ReputationRepository` | Historial de puntos, suma de puntos e insignias, catálogo de insignias. |
| `FirestoreMapper` | — | Convierte modelos `@Serializable` ↔ documentos de Firestore. |
| `FirestoreCollections` | — | Nombres de las colecciones (deben coincidir con `firebase/firestore.rules`). |

### `remote/`

| Archivo | Implementa | Servicio |
|---|---|---|
| `CloudinaryImageRepository` | `ImageStorageRepository` | Sube fotos a Cloudinary con un *upload preset* sin firma. |
| `OpenRouterAiRepository` | `AiAnalysisRepository` | Analiza publicaciones con un modelo de IA vía OpenRouter. |
| `BackgroundPostAnalysisScheduler` | `PostAnalysisScheduler` | Lanza el análisis de IA en segundo plano al crear o editar. |
| `EmailJsCodeSender` | — (lo usa `FirebaseAuthRepository`) | Envía el correo con el código A2F mediante la API REST de EmailJS. |

### `device/`

| Archivo | Implementa | Servicio |
|---|---|---|
| `AndroidGeocodingRepository` | `GeocodingRepository` | `android.location.Geocoder`: dirección → coordenadas y coordenadas → dirección, sin llaves. |

<br>

## Cómo funciona por dentro

### Modelos ↔ documentos (`FirestoreMapper`)

Los modelos del dominio son `data class` inmutables (`val`) con valores por defecto y la anotación `@Serializable`. El SDK de Firestore, por su cuenta, solo sabe leer clases con `var` y *setters*; por eso el mapeo pasa por JSON con **kotlinx.serialization**:

```text
Post (Kotlin) ──encodeToJsonElement──▶ JsonObject ──fromJson──▶ Map<String, Any?> ──set()──▶ Firestore
Firestore ──get()──▶ Map<String, Any?> ──toJson──▶ JsonObject ──decodeFromJsonElement──▶ Post
```

- Los enums se guardan como texto (`"VERIFIED"`), así las consultas `whereEqualTo("verification.status", "VERIFIED")` funcionan.
- `encodeDefaults = true` escribe también los valores por defecto (las consultas filtran por ellos).
- `ignoreUnknownKeys` permite guardar campos extra que el modelo no tiene (por ejemplo `userNameLower` para buscar por alias).
- Un documento mal formado devuelve `null` en vez de tumbar la pantalla.

Extensiones disponibles: `modelo.toFirestore()`, `snapshot.toModel<T>()`, `querySnapshot.toModels<T>()`.

### Colecciones de Firestore

```text
users/{uid}                     Perfil de usuario (+ userNameLower para buscar)
moderators/{uid}                Perfil de moderador (solo se crean desde la consola o el script de datos)
emails/{correo}                 { uid }  ─┐
userNames/{alias}               { uid }   ├─ Índices de valores únicos
phones/{teléfono}               { uid }  ─┘
twoFactorChallenges/{uid}       Código A2F (solo el hash), vencimiento e intentos
categories/{id}, animalTypes/{id}, badges/{id}   Catálogos
posts/{id}                      Publicación (verificación, IA y contadores dentro)
posts/{id}/comments/{id}        Comentarios y respuestas
commentLikes/{commentId_userId} "Me gusta" (id fijo: imposible duplicarlo)
importantVotes/{postId_userId}  Votos "Es importante" (id fijo)
notifications/{id}              Notificaciones (recipientId)
chats/{userA_userB_postId}      Resumen del chat (id determinista)
chats/{id}/messages/{id}        Mensajes
adoptionRequests/{postId_requesterId}  Solicitudes de adopción
pointTransactions/{userId_REASON_sourceId}  Historial de puntos (id fijo: un hecho da puntos una sola vez)
deletionFeedback/{id}           Opinión anónima al eliminar la cuenta
```

### Decisiones importantes

- **Transacciones** en todo lo que toca varios documentos o depende del estado actual: verificar una publicación (dos moderadores no deciden la misma), comentar (+1 a `commentCount` y `replyCount`), votar, dar "me gusta", enviar un mensaje (mensaje + resumen + no leídos), solicitar adopción (solicitud + chat + mensaje), otorgar puntos e insignias, validar el código A2F.
- **Ids deterministas** (`postId_userId`, `userA_userB_postId`…): un doble toque nunca crea dos votos, dos chats o dos premios.
- **Pocos índices**: las consultas filtran en Firestore por uno o dos campos y el resto (ciudad, tamaño, vacunas, distancia) se refina en la app. El feed lee por tandas hasta llenar la página. Los índices compuestos necesarios están en `firebase/firestore.indexes.json`.
- **Datos en vivo** con *snapshot listeners*: perfil de la sesión (puntos e insignias se actualizan solos), catálogos, insignias y mensajes del chat.
- **Caché local de Firestore**: la app abre rápido y muestra datos sin conexión; las escrituras se sincronizan al volver la red.
- **Errores**: las excepciones de Firebase se traducen a las del dominio (`AuthException.WrongPassword`, `PostException.AlreadyReviewed`…). `unwrapTransactionError()` recupera la excepción original lanzada dentro de una transacción.

### Autenticación y A2F (`FirebaseAuthRepository`)

```text
login(email, clave)
  └─ Firebase Auth valida ──▶ se lee el perfil (users/ o moderators/)
        ├─ A2F apagada ──▶ currentAccount = cuenta  ──▶ LoginResult.Success
        └─ A2F activa  ──▶ se recuerda "pendiente" en el teléfono ──▶ LoginResult.TwoFactorRequired
                             └─ requestTwoFactorCode: código de 6 dígitos
                                  ├─ Firestore guarda SHA-256(uid:código), vence en 5 min, máx. 5 intentos
                                  └─ EmailJS envía el correo
verifyTwoFactorCode ──▶ transacción: vencido / agotado / incorrecto (+1 intento) / correcto (un solo uso)
                    ──▶ currentAccount = cuenta
```

- La sesión de **la app** (`currentAccount`) solo se abre con el código correcto. Firebase sí queda autenticado mientras tanto (las reglas lo exigen para guardar el código), por eso, si la app se cierra a mitad de la A2F, `restoreSession()` la cierra al volver a abrir.
- Recuperar contraseña usa `sendPasswordResetEmail`: Firebase envía un enlace a su propia página para escribir la nueva clave.
- Con la protección contra enumeración de Firebase Auth no se puede preguntar si un correo existe; por eso el registro consulta el índice `emails`.

### Servicios remotos

- **Cloudinary**: `POST https://api.cloudinary.com/v1_1/{cloud}/image/upload` multipart con el archivo, `upload_preset` (sin firma) y `folder = "KogMaw-Adoption App/{posts|profiles|chats}"`. Devuelve `secure_url`. El *API secret* nunca está en la app.
- **OpenRouter**: `POST https://openrouter.ai/api/v1/chat/completions` con `Authorization: Bearer <llave>`, `response_format: json_object`, temperatura 0.2, el texto de la publicación, las candidatas a duplicado y hasta 3 fotos (URL o base64). `parseAiAnswer` valida y normaliza la respuesta.
- **EmailJS**: `POST https://api.emailjs.com/api/v1.0/email/send` con `service_id`, `template_id`, `user_id` (llave pública), `accessToken` (llave privada, si la cuenta está en modo estricto) y `template_params` (`to_email`, `user_name`, `code`, `minutes`).

Todas las llamadas HTTP usan **OkHttp** en `Dispatchers.IO` y devuelven `Result`, nunca lanzan a la interfaz.

<br>

## Conexión con las demás capas

```text
features/ (ViewModel) ──▶ domain/usecase ──▶ domain/repository (interfaz)
                                                    ▲
                                  di/RepositoryModule│ @Binds
                                                    │
                                   data/repository/* (implementación) ──▶ Firebase / Cloudinary / OpenRouter / EmailJS / Geocoder
```

- **`domain`** define los contratos y los modelos; `data` depende de `domain`, nunca al revés.
- **`di`** enlaza cada interfaz con su implementación (`RepositoryModule`) y entrega `FirebaseAuth` y `FirebaseFirestore` (`FirebaseModule`).
- **Configuración**: las llaves se leen de `local.properties` y llegan como constantes de `BuildConfig` (ver el README principal).
