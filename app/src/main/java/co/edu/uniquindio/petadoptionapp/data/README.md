<h1 align="center">

_Capa de Datos_

</h1>

**En pocas palabras:** aquí vive todo lo que habla con el exterior. La app guarda y lee la información en **Firebase**, sube las fotos a **Cloudinary**, pide análisis a la **IA (OpenRouter)**, envía correos con **EmailJS** y convierte direcciones con el **GPS del teléfono**. El resto de la app no sabe nada de esto: solo usa los contratos que define `domain/repository`.

<br>

## Qué hay aquí

```text
data/repository/
├── firebase/   Cuentas (Firebase Auth) y base de datos (Cloud Firestore)
├── remote/     Servicios web: Cloudinary, OpenRouter y EmailJS
└── device/     Servicios del teléfono: geocodificador
```

| Archivo | Cumple el contrato | Qué hace |
|---|---|---|
| `firebase/FirebaseAuthRepository` | `AuthRepository` | Inicio de sesión, registro, sesión guardada, contraseña y verificación en dos pasos |
| `firebase/FirestoreUserRepository` | `UserRepository` | Perfiles de usuarios y moderadores, búsqueda por alias, ajustes |
| `firebase/FirestorePostRepository` | `PostRepository` | Publicaciones, feed con filtros y cola de moderación |
| `firebase/FirestoreCatalogRepository` | `CatalogRepository` | Categorías y tipos de animal (en vivo) |
| `firebase/FirestoreCommentRepository` | `CommentRepository` | Comentarios, respuestas y "me gusta" |
| `firebase/FirestoreImportantVoteRepository` | `ImportantVoteRepository` | Votos "Es importante" |
| `firebase/FirestoreNotificationRepository` | `NotificationRepository` | Notificaciones |
| `firebase/FirestoreChatRepository` | `ChatRepository` | Chats, mensajes en vivo y solicitudes de adopción |
| `firebase/FirestoreReputationRepository` | `ReputationRepository` | Puntos, niveles e insignias |
| `remote/CloudinaryImageRepository` | `ImageStorageRepository` | Sube fotos y devuelve su enlace |
| `remote/OpenRouterAiRepository` | `AiAnalysisRepository` | Analiza publicaciones con IA |
| `remote/BackgroundPostAnalysisScheduler` | `PostAnalysisScheduler` | Lanza ese análisis en segundo plano |
| `remote/EmailJsCodeSender` | — | Envía el correo con el código A2F |
| `device/AndroidGeocodingRepository` | `GeocodingRepository` | Dirección ↔ coordenadas, sin llaves |
| `firebase/FirestoreMapper` | — | Convierte los modelos en documentos de Firestore y al revés |
| `firebase/FirestoreCollections` | — | Nombres de las colecciones |

<br>

## Cómo se guardan los datos en Firestore

Firestore guarda **documentos** (parecidos a objetos JSON) dentro de **colecciones**:

| Colección | Qué guarda |
|---|---|
| `users/{uid}`, `moderators/{uid}` | Perfiles. El rol depende de en cuál está la cuenta |
| `emails`, `userNames`, `phones` | Índices de valores únicos: permiten saber si un correo, alias o teléfono ya existe |
| `posts/{id}` y `posts/{id}/comments` | Publicaciones (con su verificación y análisis de IA) y sus comentarios |
| `categories`, `animalTypes`, `badges` | Catálogos |
| `importantVotes`, `commentLikes` | Votos y "me gusta" (un documento por persona) |
| `chats/{id}` y `chats/{id}/messages` | Conversaciones y mensajes |
| `adoptionRequests` | Solicitudes de adopción |
| `notifications` | Notificaciones de cada persona |
| `pointTransactions` | Historial de puntos (cada hecho suma una sola vez) |
| `twoFactorChallenges/{uid}` | Código A2F pendiente (solo su huella SHA-256, nunca el código) |

**Tres ideas que se repiten en todo el código:**
- **Transacciones:** cuando un cambio toca varios documentos (votar y sumar al contador, enviar un mensaje y actualizar el chat, aprobar una publicación), todo se guarda junto o nada se guarda.
- **Identificadores fijos:** un voto se llama `publicación_persona`. Un doble toque nunca crea dos votos.
- **Datos en vivo:** el perfil de la sesión, los catálogos y los mensajes del chat se actualizan solos. Firestore guarda una copia local, así la app abre rápido y funciona sin conexión.

<br>

## Servicios externos

Las llaves se escriben en `local.properties`, Gradle las copia a `BuildConfig` al compilar y el código las lee de ahí. Ninguna llave está escrita en el código.

### Firebase Authentication
Correo y contraseña. Al iniciar sesión se lee el perfil en Firestore para saber el rol y si la cuenta usa verificación en dos pasos. Al abrir la app se recupera la sesión guardada. Recuperar la contraseña usa el correo de Firebase con un enlace.

### Verificación en dos pasos (EmailJS)
```text
Correo y contraseña correctos
   └─ ¿A2F activa? → se genera un código de 6 dígitos
         ├─ Firestore guarda su huella SHA-256, vence en 5 minutos y admite 5 intentos
         └─ EmailJS envía el correo
El código se valida en una transacción → se abre la sesión
```
Si la app se cierra a mitad del proceso, la próxima vez la sesión empieza cerrada.

### Cloudinary (fotos)
Cada foto se reduce a 1600 px, se sube con un *upload preset* sin firma a la carpeta `KogMaw-Adoption App/{posts|profiles|chats}` y se guarda su enlace `https`. El *API secret* nunca está en la app.

### OpenRouter (IA)
Envía el texto de la publicación, hasta 3 fotos y hasta 10 publicaciones parecidas. Recibe un JSON con prioridad, resumen, alertas y posible duplicado. Corre en segundo plano al publicar o editar. La IA **solo sugiere**: aprobar o rechazar siempre lo decide un moderador.

### Geocodificador del teléfono
`android.location.Geocoder` convierte una dirección escrita en coordenadas y un punto del mapa en dirección. No necesita llaves.

<br>

## Para desarrolladores

- **¿Agregar una función que guarda datos?** Primero declara la operación en la interfaz de `domain/repository` y luego impleméntala aquí.
- **¿Un modelo nuevo para Firestore?** Márcalo con `@Serializable`, dale valores por defecto y usa `modelo.toFirestore()` para guardar y `snapshot.toModel<T>()` para leer.
- **¿Una consulta nueva con filtro y orden?** Probablemente necesite un índice: agrégalo en `firebase/firestore.indexes.json` y publícalo.
- **Los errores** de Firebase se traducen a errores del dominio (`AuthException`, `PostException`…), así las pantallas muestran mensajes claros.
