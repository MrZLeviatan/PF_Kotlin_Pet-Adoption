<h1 align="center">

_Huellas — KogMaw Adoption App_

</h1>

<p align="center">
Aplicación móvil nativa para Android, escrita en Kotlin con Jetpack Compose, para la <b>adopción responsable de mascotas</b>, el reporte de animales <b>perdidos y encontrados</b>, los <b>hogares de paso</b> y la difusión de <b>jornadas veterinarias</b>.
</p>

<br>

> **Universidad del Quindío**  
> **Programa:** Ingeniería de Sistemas y Computación  
> **Curso:** Desarrollo de Aplicaciones para Dispositivos Móviles (2026-2)  
> **Autores:** Jhon Stivenson Mendez, Nicolas Cabrera Serrano, Robinson Daniel Gañan

<br>

## Contenido

1. [Funcionalidades](#funcionalidades)
2. [Arquitectura](#arquitectura)
3. [Tecnologías](#tecnologías)
4. [Servicios externos: cómo se implementaron](#servicios-externos-cómo-se-implementaron)
5. [Puesta en marcha](#puesta-en-marcha)
6. [Seguridad](#seguridad)
7. [Estructura del repositorio](#estructura-del-repositorio)

<br>

## Funcionalidades

**Personas usuarias**
- Registro en dos pasos con validación de correo y teléfono únicos, y ubicación elegida en el mapa.
- Inicio de sesión con **verificación en dos pasos (A2F)** por correo, sesión guardada y recuperación de contraseña.
- Publicaciones de cinco categorías (adopción, perdidos, encontrados, hogar temporal, veterinaria) con fotos de cámara o galería, datos del animal y ubicación.
- Feed con filtros y orden, y **mapa de Explorar** con un marcador por categoría, filtros por cercanía y ubicación aproximada para proteger la dirección.
- Comentarios con respuestas y "me gusta", votos "Es importante", solicitudes de adopción y **chat en vivo** con fotos.
- Notificaciones, reputación (puntos, niveles e insignias) y métricas para compartir.
- Perfil público o privado, edición de perfil y foto, cambio de idioma (español, inglés, francés) y eliminación de cuenta.

**Moderadores**
- Cola de revisión ordenada por prioridad, tomar, aprobar o rechazar con motivo, y finalizar publicaciones.
- **Análisis con IA** de cada publicación: prioridad sugerida, resumen, puntos clave, relevancia, inconsistencias, revisión de fotos, contenido inapropiado, posibles duplicados y acciones recomendadas.
- Estadísticas propias y acceso al perfil completo de quien publica.

<br>

## Arquitectura

El proyecto sigue **Clean Architecture** con **MVVM** en la presentación. Las dependencias apuntan siempre hacia el dominio:

```text
┌──────────────────────────────────────────────────────────────┐
│  features/  (Compose + ViewModel)        navigation/          │  Presentación
│  core/      (tema, componentes, mapa, utilidades)             │
└───────────────┬──────────────────────────────────────────────┘
                │ llama a casos de uso
┌───────────────▼──────────────────────────────────────────────┐
│  domain/    modelos · casos de uso · contratos de repositorio │  Dominio (Kotlin puro)
└───────────────▲──────────────────────────────────────────────┘
                │ implementa los contratos
┌───────────────┴──────────────────────────────────────────────┐
│  data/      Firebase · Cloudinary · OpenRouter · EmailJS ·    │  Datos
│             geocodificador del teléfono                       │
└──────────────────────────────────────────────────────────────┘
        di/  (Hilt) une interfaces con implementaciones
```

**Ejemplo de un flujo completo — aprobar una publicación:**

1. El moderador pulsa "Aprobar" en `PostDetailScreen`.
2. `PostDetailViewModel` llama a `ApprovePostUseCase`.
3. El caso de uso comprueba que sea un moderador activo (`RequireModeratorUseCase`), pide a `PostRepository.updateVerification` el cambio, otorga puntos al autor (`AwardPointsUseCase`) y le envía una notificación (`NotifyUseCase`).
4. `FirestorePostRepository` hace la escritura en una **transacción** de Firestore: si otro moderador ya la decidió, falla con `PostException.AlreadyReviewed`.
5. El ViewModel actualiza su `uiState` y la pantalla se redibuja.

Documentación de cada capa:

| Capa | README |
|---|---|
| Dominio | [domain/README.md](app/src/main/java/co/edu/uniquindio/petadoptionapp/domain/README.md) · [model](app/src/main/java/co/edu/uniquindio/petadoptionapp/domain/model/README.md) · [repository](app/src/main/java/co/edu/uniquindio/petadoptionapp/domain/repository/README.md) · [usecase](app/src/main/java/co/edu/uniquindio/petadoptionapp/domain/usecase/README.md) |
| Datos | [data/README.md](app/src/main/java/co/edu/uniquindio/petadoptionapp/data/README.md) |
| Presentación | [features/README.md](app/src/main/java/co/edu/uniquindio/petadoptionapp/features/README.md) |
| Núcleo compartido | [core/README.md](app/src/main/java/co/edu/uniquindio/petadoptionapp/core/README.md) |
| Inyección de dependencias | [di/README.md](app/src/main/java/co/edu/uniquindio/petadoptionapp/di/README.md) |
| Navegación | [navigation/README.md](app/src/main/java/co/edu/uniquindio/petadoptionapp/navigation/README.md) |
| Firebase (consola, reglas, datos) | [firebase/README.md](firebase/README.md) |
| Modelo de datos | [docs/modelo-datos.md](docs/modelo-datos.md) |

<br>

## Tecnologías

| Tecnología | Versión | Uso |
|---|---|---|
| Kotlin | 2.4.20 | Lenguaje |
| Android Gradle Plugin | 9.4.1 | Compilación (minSdk 24, targetSdk 37) |
| Jetpack Compose (BoM) + Material 3 | 2026.09.00 | Interfaz declarativa |
| Navigation Compose | 2.10.2 | Navegación con rutas con tipo |
| Hilt (Dagger) + KSP | 2.60.1 | Inyección de dependencias |
| Coroutines / Flow | 1.10.2 | Asincronía y estado reactivo |
| kotlinx.serialization | 1.11.0 | JSON (rutas, Firestore, OpenRouter, EmailJS) |
| Firebase BoM (Auth, Firestore) | 35.1.0 | Cuentas y base de datos |
| OkHttp | 4.12.0 | Llamadas REST (Cloudinary, OpenRouter, EmailJS) |
| Coil | 3.6.3 | Carga de imágenes desde URL |
| MapLibre Android | 11.13.5 | Mapas (estilo OpenFreeMap) |
| AppCompat | 1.7.1 | Idioma de la app por aplicación |

<br>

## Servicios externos: cómo se implementaron

Todas las llaves se escriben en **`local.properties`** (ignorado por git). `app/build.gradle.kts` las lee al compilar y las expone como constantes de **`BuildConfig`**; el código nunca contiene una llave escrita.

```properties
CLOUDINARY_CLOUD_NAME=...
CLOUDINARY_UPLOAD_PRESET=...
OPENROUTER_API_KEY=...
OPENROUTER_MODEL=openai/gpt-4o-mini   # opcional
EMAILJS_SERVICE_ID=...
EMAILJS_TEMPLATE_ID=...
EMAILJS_PUBLIC_KEY=...
EMAILJS_PRIVATE_KEY=...                # si la cuenta de EmailJS está en modo estricto
```

### Firebase Authentication y Cloud Firestore
- **Configuración**: plugin `com.google.gms.google-services` + `app/google-services.json`. `di/FirebaseModule` entrega `FirebaseAuth` y `FirebaseFirestore` como singletons.
- **Autenticación** (`FirebaseAuthRepository`): correo y contraseña; el rol y el alias se leen del perfil en Firestore. La sesión se recupera al abrir la app. Recuperar contraseña usa el correo de Firebase.
- **Base de datos**: cada contrato del dominio tiene su repositorio de Firestore. Los modelos `@Serializable` se convierten en documentos con `FirestoreMapper` (kotlinx.serialization), sin anotaciones de Firebase en el dominio.
- **Consistencia**: transacciones para contadores, votos, mensajes, verificaciones y puntos; ids deterministas para evitar duplicados.
- **Tiempo real**: *snapshot listeners* para el perfil de la sesión, catálogos y mensajes del chat. La caché local permite usar la app sin conexión.
- **Seguridad**: `firebase/firestore.rules` (cada quien escribe lo suyo, los chats solo los leen sus participantes, nadie se auto-verifica, los moderadores no se crean desde la app) e índices en `firebase/firestore.indexes.json`. Se publican con `npx firebase-tools deploy --only firestore`.
- **Datos de prueba**: `SeedExportTest` convierte `SeedData.kt` en `firebase/seed/seed.json`, y `firebase/seed/seed.mjs` (Firebase Admin SDK) los carga.

### Verificación en dos pasos con EmailJS
Firebase Auth no envía códigos por correo, y sus Cloud Functions requieren el plan de pago. Por eso:
1. Con correo y contraseña correctos, si la cuenta tiene A2F, se genera un código de **6 dígitos** con `SecureRandom`.
2. En `twoFactorChallenges/{uid}` se guarda solo su **hash SHA-256**, el vencimiento (5 min), los intentos (máx. 5) y si ya se usó.
3. `EmailJsCodeSender` llama a `POST https://api.emailjs.com/api/v1.0/email/send` (OkHttp) con la plantilla `firebase/emailjs-template.html`.
4. El código se valida en una **transacción**; solo entonces se abre la sesión de la app.

> Limitación: al generarse en el teléfono, esta A2F es adecuada para un proyecto académico; en producción el código debería generarse en un servidor.

### Cloudinary — almacenamiento de imágenes
- `CloudinaryImageRepository` sube cada foto con **upload preset sin firma** (`POST https://api.cloudinary.com/v1_1/{cloud}/image/upload`, multipart) a la carpeta `KogMaw-Adoption App/{posts|profiles|chats}` y guarda la `secure_url`.
- Antes de subirla, `LocalImageStore` la copia a la carpeta privada de la app y la reduce a 1600 px en JPEG.
- El *API secret* nunca está en la app; solo el *cloud name* y el nombre del preset.
- Las imágenes se muestran con **Coil**.

### MapLibre + OpenFreeMap — mapas
- `core/component/map/AppMap` integra el `MapView` de MapLibre en Compose (`AndroidView`) con el estilo gratuito **OpenFreeMap** (`tiles.openfreemap.org/styles/liberty`), sin llaves ni tarjeta.
- Marcadores en una `SymbolLayer` con íconos dibujados como *bitmaps* (`MarkerImages`); el ícono, el color y la forma salen de la **categoría guardada en Firestore**. Las áreas aproximadas son una `FillLayer`.
- Ubicación: permiso en tiempo de ejecución + `LocationManager`. Direcciones: `android.location.Geocoder` (`AndroidGeocodingRepository`), sin llaves.
- Se usa en el registro, al crear publicaciones (ubicación actual o elegida), en el detalle (zona aproximada de 400 m) y en Explorar.

### OpenRouter — inteligencia artificial
- `OpenRouterAiRepository` llama a `POST https://openrouter.ai/api/v1/chat/completions` (API compatible con OpenAI) con el modelo de `OPENROUTER_MODEL` (por defecto `openai/gpt-4o-mini`, que admite imágenes), `temperature 0.2` y respuesta en JSON.
- Envía el texto de la publicación, hasta 3 fotos y hasta 10 publicaciones parecidas (mismo autor o misma categoría a menos de 10 km) para detectar duplicados.
- `BackgroundPostAnalysisScheduler` lo ejecuta en segundo plano al crear o editar; los moderadores pueden analizar o re-analizar a demanda.
- La IA **sugiere**; la decisión de aprobar o rechazar siempre es de un moderador.

### Idiomas
`AppCompatDelegate.setApplicationLocales` con `res/xml/locales_config.xml` y traducciones en `values/`, `values-en/` y `values-fr/`. El cambio se aplica al instante, sin reiniciar la app.

<br>

## Puesta en marcha

1. Clona el repositorio y ábrelo en Android Studio.
2. Copia `app/google-services.json` de tu proyecto de Firebase (no está en el repositorio).
3. Completa `local.properties` con las llaves de la sección anterior.
4. Configura Firebase (Authentication, Firestore, reglas, índices y datos de prueba) siguiendo [firebase/README.md](firebase/README.md).
5. Ejecuta la app (`./gradlew :app:assembleDebug` o el botón Run).

<br>

## Seguridad

- Fuera del repositorio (`.gitignore`): `local.properties`, `app/google-services.json`, `firebase/seed/service-account.json` y `firebase/seed/.env` (contraseñas de las cuentas de prueba).
- Las llaves de `local.properties` sí quedan dentro del APK compilado. Para distribuir la app, conviene mover OpenRouter y EmailJS a un servidor (por ejemplo, Cloud Functions) y fijar límites de gasto.
- Las reglas de Firestore son la barrera real de seguridad: la app nunca confía solo en lo que valida la interfaz.

<br>

## Estructura del repositorio

```text
├── app/
│   ├── build.gradle.kts            Dependencias, BuildConfig con las llaves
│   └── src/main/
│       ├── java/co/edu/uniquindio/petadoptionapp/
│       │   ├── domain/             Modelos, casos de uso, contratos
│       │   ├── data/               Firebase, Cloudinary, OpenRouter, EmailJS, Geocoder
│       │   ├── di/                 Módulos de Hilt
│       │   ├── core/               Tema, componentes, mapa, utilidades
│       │   ├── features/           Pantallas y ViewModels
│       │   ├── navigation/         Rutas y NavHost
│       │   ├── MainActivity.kt
│       │   └── Application.kt
│       └── res/                    Textos (es/en/fr), íconos, imágenes
├── firebase/                       Reglas, índices, plantilla de EmailJS y script de datos
├── docs/                           Modelo de datos
├── firebase.json / .firebaserc     Configuración de Firebase CLI
└── gradle/libs.versions.toml       Catálogo de versiones
```
