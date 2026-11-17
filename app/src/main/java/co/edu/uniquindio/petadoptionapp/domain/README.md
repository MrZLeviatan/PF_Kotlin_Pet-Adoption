<h1 align="center">

_Capa de Dominio_

</h1>

**En pocas palabras:** el dominio es el **corazón de la app**. Aquí se define qué es una publicación, un usuario o un chat, y cuáles son las **reglas**: quién puede aprobar una publicación, cuándo se ganan puntos, qué datos son obligatorios. Es Kotlin puro: no dibuja pantallas ni sabe que existe Firebase.

<br>

## Qué hay aquí

| Carpeta | Qué contiene | Guía |
|---|---|---|
| `model/` | Las "cosas" de la app: `Post`, `User`, `Comment`, `Chat`, `Notification`… | [model/README.md](./model/README.md) |
| `usecase/` | Las acciones: crear una publicación, aprobarla, comentar, enviar un mensaje… | [usecase/README.md](./usecase/README.md) |
| `repository/` | Los **contratos** de datos: qué operaciones se necesitan, sin decir cómo se hacen | [repository/README.md](./repository/README.md) |
| `error/` | Los errores con nombre propio: `AuthException.WrongPassword`, `PostException.AlreadyReviewed`… | — |

<br>

## Cómo funciona

```text
Pantalla (ViewModel)
   │  "quiero aprobar esta publicación"
   ▼
Caso de uso  ── revisa las reglas: ¿es moderador? ¿sigue pendiente?
   │
   ▼
Contrato de repositorio  ── "guarda esta verificación"
   │
   ▼
Implementación en data/  ── la escribe en Firestore
```

- Los casos de uso devuelven `Result`: éxito con el dato, o fallo con un error del dominio. La pantalla decide qué mensaje mostrar.
- Los modelos son inmutables (`data class` con `val`). Para cambiarlos se crea una copia con `copy(...)`.
- La única anotación que llevan los modelos es `@Serializable` (Kotlin puro), que la capa de datos usa para guardarlos en Firestore.

<br>

## Relación con las demás capas

| Capa | Relación |
|---|---|
| `features/` | Sus ViewModels llaman a los casos de uso |
| `data/` | Implementa los contratos de `repository/` |
| `di/` | Une cada contrato con su implementación y fabrica los casos de uso |
| `core/` | No se relaciona: es solo interfaz visual |

<br>

## Para desarrolladores

- **¿Una regla nueva?** Va en un caso de uso, no en la pantalla ni en el repositorio.
- **¿Validar un formato** (correo, teléfono, título)? Usa o amplía los validadores de `usecase/validator/`.
- **¿Un error nuevo?** Agrégalo a la `sealed class` de su área en `error/` y tradúcelo a un mensaje en la pantalla.
