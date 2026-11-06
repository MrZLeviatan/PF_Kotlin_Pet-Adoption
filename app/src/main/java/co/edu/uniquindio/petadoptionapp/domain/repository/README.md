<h1 align="center">

_Repositorios del dominio_

</h1>

Esta carpeta define los contratos para consultar y modificar los datos de la aplicación. Cada interfaz describe **qué operaciones necesita el dominio**, pero no cómo ni dónde se almacenan los datos.

Las reglas del negocio (por ejemplo, quién puede editar una publicación o cuándo se otorgan puntos) corresponden principalmente a los casos de uso. El repositorio ejecuta las operaciones de lectura y escritura solicitadas.

<br>

## Estructura

### `AuthRepository`

Administra la identidad y la sesión:

- Observa la cuenta actual.
- Inicia y cierra sesión.
- Registra cuentas.
- Solicita y verifica códigos de autenticación en dos pasos.
- Envía solicitudes de recuperación y permite cambiar la contraseña.
- Reautentica y elimina la cuenta.

Trabaja con `Credentials` y `Account`. Los casos de uso de `usecase/auth` validan los datos y coordinan el flujo; el contrato no almacena contraseñas en los modelos de perfil.

<br>

### `UserRepository`

Administra perfiles de usuarios y moderadores:

- Expone el perfil del usuario activo.
- Guarda y busca perfiles.
- Busca moderadores precargados.
- Comprueba alias y teléfonos duplicados.
- Busca usuarios por alias.
- Actualiza datos personales y preferencias.
- Anonimiza un perfil al eliminar una cuenta.

Trabaja con modelos de `users/`, `users/profile/` y `location/`. Los casos de uso de `usecase/user` verifican permisos, privacidad, estado del perfil y validaciones antes de solicitar cambios.

<br>

### `PostRepository`

Administra publicaciones:

- Crea, busca y lista publicaciones.
- Consulta el feed con filtros y paginación.
- Lista publicaciones por autor y calcula conteos para estadísticas.
- Actualiza contenido y estado; permite borrado lógico y cierre de publicaciones de una cuenta.
- Obtiene la cola pendiente de moderación.
- Actualiza la verificación comparando el estado esperado, para evitar sobrescribir una revisión concurrente.

Trabaja con `Post`, `PostFilter`, `PostStatus`, `Verification` y `VerificationStatus`. Los casos de uso de `usecase/post` aplican las reglas de publicación, permisos y moderación.

<br>

### `CatalogRepository`

Administra categorías y tipos de animal:

- Expone listas observables.
- Refresca los catálogos y busca elementos por ID.
- Crea y actualiza categorías y tipos.

Trabaja con `Category` y `AnimalType`. Los casos de uso de `usecase/catalog` validan nombres, permisos de moderador y que los elementos usados por una publicación estén activos.

<br>

### `ChatRepository`

Administra conversaciones, mensajes y solicitudes de adopción:

- Abre y consulta chats, lista los de una persona y cuenta chats no leídos.
- Envía mensajes, observa mensajes recientes y consulta mensajes anteriores.
- Marca conversaciones como leídas.
- Guarda una solicitud de adopción junto con el chat y el mensaje inicial.
- Consulta solicitudes y actualiza su estado comparando el estado anterior.

Trabaja con `Chat`, `Message` y `AdoptionRequest`. Los casos de uso de `usecase/chat` comprueban sesión, membresía, autoría, contenido y transiciones permitidas.

<br>

### `CommentRepository`

Administra comentarios, respuestas y sus “me gusta”:

- Crea y consulta comentarios principales o respuestas paginadas.
- Actualiza el texto y marca comentarios como eliminados lógicamente.
- Alterna “me gusta” y devuelve los comentarios marcados por una persona.

Trabaja con `Comment` y `CommentLike`. Según el contrato, crear un comentario debe mantener los contadores de la publicación y de las respuestas.

<br>

### `ImportantVoteRepository`

Administra los votos “Es importante”:

- Agrega o quita el voto de una persona.
- Consulta si ya votó, qué publicaciones de una lista votó y los IDs votados por página.

Trabaja con `ImportantVote`. El caso de uso `ToggleImportantUseCase` comprueba sesión, visibilidad y que no se vote la propia publicación; el repositorio administra el voto y el contador asociado.

<br>

### `NotificationRepository`

Administra notificaciones guardadas:

- Crea y consulta notificaciones por destinatario con paginación.
- Busca una notificación y cuenta las no leídas.
- Cambia el estado leído/no leído, marca todas como leídas y elimina.

Trabaja con `Notification`. Los casos de uso de `usecase/notification` determinan el destinatario desde la sesión y comprueban que una persona solo modifique sus notificaciones.

<br>

### `ReputationRepository`

Administra puntos e insignias:

- Expone y actualiza el catálogo de insignias.
- Registra premios de puntos evitando aplicar dos veces la misma transacción.
- Consulta el historial y cuenta transacciones por motivo.
- Desbloquea insignias sin duplicarlas.

Trabaja con `Badge`, `PointTransaction`, `PointReason` y `AwardResult`. Los casos de uso de `usecase/reputation` calculan cuándo corresponde otorgar puntos o desbloquear logros; el nivel se deriva de los puntos en el modelo `Reputation`.

<br>

### `AiAnalysisRepository` y `PostAnalysisScheduler`

- `AiAnalysisRepository.analyze(input)`: envía una publicación (con sus candidatas a duplicado) a un modelo de IA y devuelve un `AiPostAnalysis`: revisión (`AiReview`), prioridad sugerida y su motivo.
- `PostAnalysisScheduler.schedule(postId)`: pide ese análisis en segundo plano, sin hacer esperar a quien publica.

Errores: `AiException.NotConfigured` (falta la llave) y `AiException.AnalysisFailed`.

<br>

### `ImageStorageRepository`

Sube una imagen local y devuelve su URL pública. Es la frontera entre la app y el almacenamiento de imágenes (Cloudinary); los modelos solo guardan URL.

<br>

### `GeocodingRepository`

Convierte texto en direcciones con coordenadas y coordenadas en direcciones. No tiene estado y no requiere llaves.

<br>

## Implementaciones actuales

| Contrato | Implementación (`data/repository/…`) | Tecnología |
|---|---|---|
| `AuthRepository` | `firebase/FirebaseAuthRepository` | Firebase Authentication + Firestore (`twoFactorChallenges`) + EmailJS |
| `UserRepository` | `firebase/FirestoreUserRepository` | Firestore (`users`, `moderators`, índices `emails`/`userNames`/`phones`) |
| `PostRepository` | `firebase/FirestorePostRepository` | Firestore (`posts`) |
| `CatalogRepository` | `firebase/FirestoreCatalogRepository` | Firestore (`categories`, `animalTypes`) en vivo |
| `CommentRepository` | `firebase/FirestoreCommentRepository` | Firestore (`posts/{id}/comments`, `commentLikes`) |
| `ImportantVoteRepository` | `firebase/FirestoreImportantVoteRepository` | Firestore (`importantVotes`) |
| `NotificationRepository` | `firebase/FirestoreNotificationRepository` | Firestore (`notifications`) |
| `ChatRepository` | `firebase/FirestoreChatRepository` | Firestore (`chats`, `chats/{id}/messages`, `adoptionRequests`) |
| `ReputationRepository` | `firebase/FirestoreReputationRepository` | Firestore (`pointTransactions`, `badges`, `users.reputation`) |
| `ImageStorageRepository` | `remote/CloudinaryImageRepository` | Cloudinary (subida sin firma) |
| `AiAnalysisRepository` | `remote/OpenRouterAiRepository` | OpenRouter (API compatible con OpenAI) |
| `PostAnalysisScheduler` | `remote/BackgroundPostAnalysisScheduler` | Corrutinas en un alcance de la app |
| `GeocodingRepository` | `device/AndroidGeocodingRepository` | `android.location.Geocoder` |

<br>

## Conexión con las capas

```text
Pantalla / ViewModel
        ↓
Caso de uso en domain/usecase
  valida y aplica reglas del negocio
        ↓
Interfaz de repositorio en domain/repository
  define las operaciones disponibles
        ↓
Implementación en data/
  realiza las lecturas y escrituras
        ↓
Modelos en domain/model
```

Las dependencias se conectan en `di/RepositoryModule.kt`: allí Hilt enlaza cada interfaz del dominio con una implementación concreta.

