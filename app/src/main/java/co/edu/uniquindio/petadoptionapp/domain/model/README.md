<h1 align="center">

_Modelos del Dominio_

</h1>

Esta carpeta contiene los modelos que describen los datos y conceptos del negocio de adopción de mascotas. Se comparten entre los casos de uso y los contratos de repositorio para que las reglas del proyecto no dependan de la interfaz ni de una tecnología de almacenamiento específica.

<br>

## Estructura

### `adoption/`

- `AdoptionRequest`: solicitud de una persona interesada en adoptar una mascota. Incluye publicación, solicitante, autor, mensaje, chat y estado.
- `AdoptionRequestStatus`: estados posibles de la solicitud: pendiente, aceptada, rechazada o cancelada.

Se relaciona con las publicaciones y conversaciones. Los casos de uso de adopción crean, consultan, responden o cancelan solicitudes mediante `ChatRepository`.

### `auth/`

- `Credentials`: email y contraseña usados durante el acceso o el registro. Es información temporal; no debe persistirse ni registrarse en logs.
- `TwoFactorChallenge`: datos de un desafío de autenticación en dos pasos, como hash del código, expiración e intentos.

Los casos de uso de autenticación usan estos modelos con `AuthRepository`. La contraseña no forma parte de `Account`, `User` ni `Moderator`.

### `comment/`

- `Comment`: comentario o respuesta dentro de una publicación. Incluye autor, texto, jerarquía, estado y contadores.
- `CommentLike`: “me gusta” de una persona sobre un comentario.
- `CommentStatus`: estado activo o eliminado lógicamente.

Los comentarios se relacionan con una publicación y pueden responder a otro comentario. Los casos de uso de comentarios aplican las reglas de visibilidad, autoría y formato; `CommentRepository` los consulta y modifica.

### `location/`

- `Location`: coordenadas y datos legibles de ubicación, reutilizados en perfiles y publicaciones.

Incluye comprobación de coordenadas válidas y cálculo de distancia. 

### `messaging/`

- `Chat`: conversación entre participantes, con resumen del último mensaje y datos de lectura.
- `Message`: mensaje enviado dentro de un chat.
- `MessageType`: distingue mensajes escritos de mensajes automáticos de interés de adopción.

Los casos de uso de chat comprueban la sesión y la pertenencia de la persona a la conversación antes de leer o enviar. `ChatRepository` administra chats, mensajes y datos de lectura.

### `notification/`

- `Notification`: evento dirigido a una cuenta, con estado leído/no leído y referencias opcionales a publicación o comentario.
- `NotificationType`: tipos de eventos, como comentario, verificación, rechazo, voto, solicitud de adopción o logro.

Los casos de uso deciden cuándo generar una notificación y quién puede consultarla. `NotificationRepository` se encarga de crear, listar, contar, marcar y eliminar notificaciones.

### `post/`

Contiene la entidad principal y los tipos asociados:

- `Post`: publicación de adopción, pérdida, hallazgo, hogar temporal o jornada veterinaria. Reúne autor, categoría, animal, fotos, ubicación, estados y contadores.
- `Category` y `AnimalType`: elementos de catálogo referenciados por sus identificadores.
- `AnimalSize`: tamaños aproximados del animal.
- `PostStatus`: ciclo de vida (activa, finalizada o eliminada).
- `Verification` y `VerificationStatus`: información y estado de la revisión del moderador; incluye motivo de rechazo y datos de revisión.
- `PriorityLevel`: prioridad sugerida para ordenar la revisión.
- `ImportantVote`: voto “Es importante” de una cuenta sobre una publicación.
- `PostStats`: resumen de métricas para el dashboard.

`Post` contiene funciones propias para determinar si puede mostrarse en el feed público. Los casos de uso validan las acciones (crear, editar, finalizar y moderar) y `PostRepository` implementa las consultas y cambios.

#### `post/filter/`

- `PostFilter`: criterios opcionales para categoría, tipo de animal, ciudad, cercanía y orden.
- `PostSort`: orden reciente o por importancia.

Estos modelos describen una consulta del feed; no son publicaciones almacenadas.

### `reputation/`

- `Reputation`: puntos e insignias desbloqueadas; calcula el nivel a partir de los puntos.
- `UserLevel`: los cuatro niveles de la temática: Amigo Animal, Protector, Guardián y Héroe de las Mascotas.
- `Badge`: definición de una insignia y el requisito para obtenerla.
- `BadgeRequirementType`: tipo de progreso que se evalúa para desbloquearla.
- `UnlockedBadge`: insignia que ya obtuvo una persona.
- `PointReason` y `PointRules`: motivos y valores de los puntos otorgados.
- `PointTransaction`: registro de puntos asociado con una acción.
- `AwardResult`: resultado de otorgar puntos y señal de cambio de nivel.

Los casos de uso evalúan logros y solicitan premios; `ReputationRepository` administra el catálogo, historial y actualización de reputación.

### `users/`

- `Account`: identidad de la cuenta: identificador, email, alias y rol; no almacena contraseña.
- `User`: perfil de una persona que usa la aplicación, con datos personales, ubicación, reputación y configuración.
- `Moderator`: perfil de moderador, con estado activo e indicadores de revisión.
- `UserRole`: roles `USER` y `MODERATOR`.

`User` y `Moderator` contienen un `Account`. Los casos de uso de usuario y autenticación utilizan estos modelos para validar perfiles, permisos, sesión y edición.

#### `users/profile/`

- `UserSettings` y `ProfileVisibility`: preferencias de segundo factor y privacidad del perfil.
- `PublicProfile`: vista del perfil filtrada según las preferencias de privacidad.
- `UserSummary`: datos mínimos para mostrar resultados de búsqueda.
- `AccountDeletionFeedback` y `DeletionReason`: datos opcionales sobre la eliminación de una cuenta.

Los modelos `PublicProfile` y `UserSummary` son vistas reducidas para consultas; no sustituyen al perfil completo `User`.

<br>

## Cómo se conectan con el dominio

El flujo habitual es:

```text
Pantalla / ViewModel
        ↓
Caso de uso (reglas, validaciones y permisos)
        ↓
Contrato de repositorio en domain/repository
        ↓
Implementación del repositorio en data/
        ↓
Modelo del dominio compartido
```

Por ejemplo, al crear una publicación, `CreatePostUseCase` valida un `Post`, comprueba sus referencias al catálogo y lo entrega a `PostRepository`. El repositorio devuelve el modelo guardado para que el caso de uso y la presentación puedan utilizarlo.

Los modelos no deben depender de Jetpack Compose, Android, Firebase ni de las implementaciones de `data`. Sus referencias a otras entidades normalmente se expresan mediante identificadores, por ejemplo `Post.authorId`, `Post.categoryId` o `Comment.postId`.

