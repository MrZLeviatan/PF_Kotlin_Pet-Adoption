<h1 align="center">

_Contratos de repositorio_

</h1>

**En pocas palabras:** un repositorio es un **contrato**: una lista de operaciones de datos que el dominio necesita ("guarda esta publicación", "dame el feed", "envía este mensaje"). Aquí solo están las interfaces; quién las cumple y con qué tecnología se decide en `data/`.

Así, si mañana se cambia Firestore por otra base de datos, las reglas y las pantallas no cambian.

<br>

## Qué hay aquí

| Contrato | Responsable de | Lo cumple (en `data/repository/`) |
|---|---|---|
| `AuthRepository` | Sesión, login, registro, contraseña, código A2F | `firebase/FirebaseAuthRepository` |
| `UserRepository` | Perfiles, búsqueda por alias, ajustes, eliminar datos personales | `firebase/FirestoreUserRepository` |
| `PostRepository` | Publicaciones, feed, cola de moderación, verificación, análisis de IA | `firebase/FirestorePostRepository` |
| `CatalogRepository` | Categorías y tipos de animal | `firebase/FirestoreCatalogRepository` |
| `CommentRepository` | Comentarios, respuestas y "me gusta" | `firebase/FirestoreCommentRepository` |
| `ImportantVoteRepository` | Votos "Es importante" | `firebase/FirestoreImportantVoteRepository` |
| `NotificationRepository` | Notificaciones | `firebase/FirestoreNotificationRepository` |
| `ChatRepository` | Chats, mensajes y solicitudes de adopción | `firebase/FirestoreChatRepository` |
| `ReputationRepository` | Puntos e insignias | `firebase/FirestoreReputationRepository` |
| `ImageStorageRepository` | Subir fotos y obtener su enlace | `remote/CloudinaryImageRepository` |
| `AiAnalysisRepository` | Analizar una publicación con IA | `remote/OpenRouterAiRepository` |
| `PostAnalysisScheduler` | Lanzar ese análisis en segundo plano | `remote/BackgroundPostAnalysisScheduler` |
| `GeocodingRepository` | Dirección ↔ coordenadas | `device/AndroidGeocodingRepository` |

La unión entre cada contrato y su implementación está en `di/RepositoryModule.kt`.

<br>

## Reglas de los contratos

- **Solo ejecutan:** no deciden permisos ni validan formatos; eso lo hacen los casos de uso.
- **Devuelven `Result`** cuando la operación puede fallar (sin internet, documento inexistente). Las búsquedas simples devuelven el objeto o `null`.
- **Algunas operaciones son atómicas por contrato:** votar, comentar, enviar un mensaje o verificar una publicación cambian varios datos a la vez, y la implementación debe hacerlo en una sola transacción.
- **Los datos de la sesión se exponen como `StateFlow`** (`AuthRepository.currentAccount`, `UserRepository.currentUser`), así las pantallas se actualizan solas.

<br>

## Para desarrolladores

1. Declara la operación nueva en la interfaz que corresponda (con un comentario de qué recibe y qué devuelve).
2. Impleméntala en la clase de `data/repository/`.
3. Úsala desde un caso de uso, nunca directo desde una pantalla.
