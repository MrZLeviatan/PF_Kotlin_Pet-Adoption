# Contratos de Repositorio

## Propósito

Esta carpeta define las **interfaces de acceso a datos** que necesita el dominio. Cada interfaz describe qué operaciones existen (crear una publicación, consultar el feed, enviar un mensaje), pero no cómo ni dónde se realizan. Este patrón, conocido como *puerto*, separa las reglas de negocio de la tecnología de almacenamiento.

Las implementaciones concretas viven en `data/repository/` y se enlazan con cada contrato en `di/RepositoryModule.kt`.

## Responsabilidades

- **Ejecutan, no deciden.** Las validaciones de formato y la comprobación de permisos corresponden a los casos de uso; el repositorio solo lee y escribe.
- **Informan el resultado.** Las operaciones que pueden fallar devuelven `Result<T>`; las búsquedas simples devuelven el objeto o `null`.
- **Garantizan atomicidad cuando el contrato lo exige.** Votar, comentar, enviar un mensaje, verificar una publicación u otorgar puntos modifican varios datos a la vez y deben ejecutarse en una sola transacción.
- **Exponen el estado de la sesión como flujo.** `AuthRepository.currentAccount` y `UserRepository.currentUser` son `StateFlow`, lo que permite a la interfaz reaccionar a los cambios sin consultar repetidamente.

## Contenido por archivo

| Contrato | Responsabilidad | Operaciones principales | Implementación |
|---|---|---|---|
| `AuthRepository.kt` | Identidad y sesión. Define también `LoginResult` (`Success`, `TwoFactorRequired`). | `currentAccount`, `restoreSession`, `login`, `logout`, `register`, `isEmailRegistered`, `requestTwoFactorCode`, `verifyTwoFactorCode`, `sendPasswordReset`, `changePassword`, `reauthenticate`, `deleteAccount`, `updateSessionAccount` | `firebase/FirebaseAuthRepository` |
| `UserRepository.kt` | Perfiles de usuarios y moderadores. | `currentUser`, `save`, `findById`, `findModeratorById`, `searchByUserName`, `existsByUserName`, `existsByPhone`, `updateProfile`, `updateSettings`, `updateModeratorTwoFactor`, `anonymize` | `firebase/FirestoreUserRepository` |
| `PostRepository.kt` | Publicaciones, feed, moderación y análisis de IA. | `create`, `findById`, `findByIds`, `getFeed`, `getByAuthor`, `countByAuthor`, `updateContent`, `updateStatus`, `deleteAllByAuthor`, `getPendingModeration`, `getInReviewBy`, `getReviewedBy`, `countForModeration`, `updateVerification`, `updateAiAnalysis` | `firebase/FirestorePostRepository` |
| `CatalogRepository.kt` | Categorías y tipos de animal, con caché reactiva. | `categories`, `animalTypes`, `refresh`, `findCategoryById`, `findAnimalTypeById`, `createCategory`, `updateCategory`, `createAnimalType`, `updateAnimalType` | `firebase/FirestoreCatalogRepository` |
| `CommentRepository.kt` | Comentarios, respuestas y "me gusta". | `create`, `findById`, `getTopLevel`, `getReplies`, `updateText`, `markDeleted`, `toggleLike`, `getLikedCommentIds` | `firebase/FirestoreCommentRepository` |
| `ImportantVoteRepository.kt` | Votos "Es importante". | `toggle`, `hasVoted`, `getVotedAmong`, `getVotedPostIds` | `firebase/FirestoreImportantVoteRepository` |
| `NotificationRepository.kt` | Notificaciones internas. | `create`, `getByRecipient`, `findById`, `countUnread`, `setRead`, `markAllAsRead`, `delete` | `firebase/FirestoreNotificationRepository` |
| `ChatRepository.kt` | Chats, mensajes y solicitudes de adopción. | `openChat`, `findChatById`, `getChatsByParticipant`, `countChatsWithUnread`, `sendMessage`, `observeMessages`, `getMessagesBefore`, `markAsRead`, `hideChatFor`, `deleteChat`, `sendAdoptionInterest`, `findAdoptionRequest`, `getRequestsByPost`, `updateRequestStatus` | `firebase/FirestoreChatRepository` |
| `ReputationRepository.kt` | Puntos, historial e insignias. | `badges`, `refreshBadges`, `award`, `getTransactions`, `countByReason`, `unlockBadge` | `firebase/FirestoreReputationRepository` |
| `ImageStorageRepository.kt` | Almacenamiento de imágenes. Define `ImageFolder` (publicaciones, perfiles, chats). | `upload` | `remote/CloudinaryImageRepository` |
| `AiAnalysisRepository.kt` | Análisis de publicaciones con IA. Define `AiPostInput`. | `analyze` | `remote/OpenRouterAiRepository` |
| `PostAnalysisScheduler.kt` | Ejecución del análisis de IA en segundo plano. | `schedule` | `remote/BackgroundPostAnalysisScheduler` |
| `GeocodingRepository.kt` | Conversión entre direcciones y coordenadas. | `search`, `reverse` | `device/AndroidGeocodingRepository` |

## Interconexión

```text
Caso de uso                         Contrato                   Implementación
───────────────────────────────     ────────────────────────   ──────────────────────────────
LoginUseCase ─────────────────────► AuthRepository ──────────► FirebaseAuthRepository
CreatePostUseCase ──┬─────────────► PostRepository ──────────► FirestorePostRepository
                    ├─────────────► CatalogRepository (vía CheckPostCatalogUseCase)
                    ├─────────────► ImageStorageRepository ──► CloudinaryImageRepository
                    └─────────────► PostAnalysisScheduler ───► BackgroundPostAnalysisScheduler
                                                                   └─► AnalyzePostUseCase
                                                                         └─► AiAnalysisRepository ─► OpenRouter
```

Un mismo caso de uso puede utilizar varios contratos, y un contrato puede ser consumido por muchos casos de uso. Ningún ViewModel accede directamente a un repositorio.

## Guía de extensión

1. Declarar la operación en la interfaz, documentando qué recibe y qué devuelve.
2. Implementarla en la clase correspondiente de `data/repository/`.
3. Consumirla desde un caso de uso. Si es una tecnología nueva, enlazarla en `di/RepositoryModule.kt`.
