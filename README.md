# Huellas — KogMaw Adoption App

Aplicación móvil nativa para Android orientada a la **adopción responsable de mascotas**, el **reporte de animales perdidos y encontrados**, la oferta de **hogares de paso** y la difusión de **jornadas veterinarias**.

> **Universidad del Quindío**  
> **Programa:** Ingeniería de Sistemas y Computación  
> **Curso:** Desarrollo de Aplicaciones para Dispositivos Móviles (2026-2)  
> **Autores:** Jhon Stivenson Mendez, Nicolas Cabrera Serrano, Robinson Daniel Gañan

---

## Tabla de contenido

1. [Descripción](#descripción)
2. [Funcionalidades](#funcionalidades)
3. [Uso de la aplicación](#uso-de-la-aplicación)
4. [Requisitos](#requisitos)
5. [Ejecución del proyecto](#ejecución-del-proyecto)
6. [Configuración de llaves](#configuración-de-llaves)
7. [Generación y distribución del APK](#generación-y-distribución-del-apk)
8. [Arquitectura](#arquitectura)
9. [Estructura del proyecto](#estructura-del-proyecto)
10. [Librerías y servicios externos](#librerías-y-servicios-externos)
11. [Seguridad](#seguridad)
12. [Licencia](#licencia)

---

## Descripción

Huellas es una plataforma comunitaria que conecta a personas interesadas en el bienestar animal. Cualquier persona registrada puede publicar una mascota en adopción, reportar un animal perdido o encontrado, ofrecer un hogar temporal o difundir una jornada veterinaria. Cada publicación incluye fotografías, datos del animal y una ubicación en el mapa.

Para garantizar la calidad y la seguridad del contenido, ninguna publicación es visible hasta que un **moderador** la revisa y la aprueba. Los moderadores cuentan con un **análisis automático mediante inteligencia artificial** que sugiere una prioridad, resume la publicación, señala inconsistencias o contenido inapropiado y detecta posibles duplicados. La decisión final siempre corresponde a una persona.

La aplicación fomenta la participación mediante un sistema de **reputación** (puntos, niveles e insignias), facilita el contacto directo mediante **chat en tiempo real** con solicitudes de adopción, y protege la privacidad mostrando solo la **ubicación aproximada** de cada publicación.

La aplicación está disponible en **español, inglés y francés**, y admite modo claro y oscuro.

---

## Funcionalidades

### Personas usuarias

| Área | Funcionalidad |
|---|---|
| Cuenta | Registro en dos pasos con validación de correo y teléfono únicos; inicio de sesión con verificación en dos pasos (A2F) por correo; sesión persistente; recuperación y cambio de contraseña; eliminación de cuenta. |
| Publicaciones | Creación y edición en cuatro pasos (información, mascota, fotos y ubicación); cinco categorías; fotos desde cámara o galería; ubicación actual o elegida en el mapa. |
| Exploración | Feed con filtros y orden; mapa con un marcador distinto por categoría, filtros por distancia, tamaño, vacunación y ciudad. |
| Interacción | Comentarios con respuestas y "me gusta"; votos "Es importante"; solicitudes de adopción; chat en tiempo real con envío de fotos. |
| Perfil | Edición de foto, datos personales y ubicación; perfil público o privado; métricas, niveles e insignias; notificaciones. |

### Moderadores

| Área | Funcionalidad |
|---|---|
| Revisión | Cola de publicaciones ordenada por prioridad; tomar, aprobar o rechazar con motivo; finalizar publicaciones. |
| Inteligencia artificial | Resumen, puntos clave, relevancia, inconsistencias, revisión de fotos, contenido inapropiado, posibles duplicados y acciones recomendadas; análisis individual o por lotes. |
| Seguimiento | Estadísticas personales de desempeño y acceso al perfil completo de quien publica. |

---

## Uso de la aplicación

### Instalación

El archivo de instalación (`app-release.apk`) se obtiene de dos formas:

- **GitHub:** en la sección [Releases](https://github.com/MrZLeviatan/PF_Kotlin_Pet-Adoption/releases) del repositorio.
- **Firebase App Distribution:** mediante el correo de invitación que recibe cada tester.

Para instalarlo, se abre el archivo en un dispositivo con **Android 7.0 o superior** y se autoriza la instalación de aplicaciones de origen desconocido cuando el sistema lo solicite.

### Flujo de uso

1. **Registro.** La persona ingresa sus datos personales y elige su ubicación en el mapa. El alias se genera automáticamente.
2. **Inicio de sesión.** Con correo y contraseña. Si la verificación en dos pasos está activa, se solicita el código de 6 dígitos enviado al correo.
3. **Publicación.** Desde el botón de publicar, se completa el formulario de cuatro pasos. La publicación queda **pendiente de revisión**.
4. **Moderación.** Un moderador revisa la publicación, apoyado por el análisis de IA, y la aprueba o la rechaza con un motivo. El autor recibe una notificación.
5. **Interacción.** Las publicaciones aprobadas aparecen en el feed y en el mapa. Otras personas pueden comentarlas, votarlas, iniciar un chat o enviar una solicitud de adopción.
6. **Cierre.** Cuando la mascota encuentra hogar o regresa a casa, el autor finaliza la publicación y recibe puntos de reputación.

### Permisos solicitados

| Permiso | Uso | Si se rechaza |
|---|---|---|
| Ubicación | Centrar el mapa y obtener la ubicación actual al publicar o editar el perfil | La ubicación se elige manualmente en el mapa |
| Cámara | Tomar fotografías para publicaciones, perfil y chat | Las fotos se eligen desde la galería |

---

## Requisitos

| Herramienta | Versión | Uso |
|---|---|---|
| Android Studio | Reciente, con JDK 17 o superior | Compilación y ejecución |
| Android SDK | API 37 (compilación), API 24 mínima | Plataforma |
| Node.js | 22 o superior | Publicación de reglas y carga de datos de prueba |
| Proyecto de Firebase | Plan gratuito (Spark) | Autenticación y base de datos |
| Cuentas de Cloudinary, EmailJS y OpenRouter | Planes gratuitos | Imágenes, correo del código A2F e inteligencia artificial |

---

## Ejecución del proyecto

**1. Clonar el repositorio**

```bash
git clone https://github.com/MrZLeviatan/PF_Kotlin_Pet-Adoption.git
```

**2. Conectar Firebase.** En la consola de Firebase, registrar una aplicación Android con el paquete `co.edu.uniquindio.petadoptionapp`, descargar `google-services.json` y copiarlo en `app/google-services.json`.

**3. Configurar las llaves** en `local.properties`, según la sección [Configuración de llaves](#configuración-de-llaves).

**4. Configurar Firebase:** activar Authentication y Firestore, publicar las reglas y los índices, y configurar la plantilla de EmailJS. El procedimiento completo está en [firebase/README.md](firebase/README.md).

```bash
npx firebase-tools login
```
```bash
npx firebase-tools deploy --only firestore
```

**5. Cargar los datos de prueba** (opcional): 4 usuarios, 5 moderadores, 5 categorías y 14 publicaciones. Ver [firebase/README.md](firebase/README.md#3-datos-de-prueba).

**6. Ejecutar.** Abrir el proyecto en Android Studio, esperar la sincronización de Gradle y ejecutar la configuración `app` en un emulador o dispositivo. Desde la terminal:

```bash
./gradlew :app:installDebug
```

---

## Configuración de llaves

Las llaves de los servicios externos se escriben en `local.properties`, en la raíz del proyecto. Este archivo está excluido de git. Durante la compilación, `app/build.gradle.kts` las lee y las expone como constantes de `BuildConfig`; el código fuente nunca contiene una llave escrita.

```properties
CLOUDINARY_CLOUD_NAME=nombre_de_la_nube
CLOUDINARY_UPLOAD_PRESET=preset_sin_firma
OPENROUTER_API_KEY=sk-or-...
OPENROUTER_MODEL=openai/gpt-4o-mini
EMAILJS_SERVICE_ID=service_...
EMAILJS_TEMPLATE_ID=template_...
EMAILJS_PUBLIC_KEY=...
EMAILJS_PRIVATE_KEY=...
```

| Llave | Origen | Obligatoria |
|---|---|---|
| `CLOUDINARY_CLOUD_NAME` | Panel de Cloudinary | Sí, para subir imágenes |
| `CLOUDINARY_UPLOAD_PRESET` | Cloudinary, *Settings > Upload > Upload presets*, en modo **Unsigned** | Sí, para subir imágenes |
| `OPENROUTER_API_KEY` | openrouter.ai, sección *Keys* | No; sin ella el análisis de IA queda desactivado |
| `OPENROUTER_MODEL` | Identificador de un modelo con soporte de imágenes | No; por defecto `openai/gpt-4o-mini` |
| `EMAILJS_SERVICE_ID`, `EMAILJS_TEMPLATE_ID`, `EMAILJS_PUBLIC_KEY` | emailjs.com: *Email Services*, *Email Templates* y *Account* | Sí, para la verificación en dos pasos |
| `EMAILJS_PRIVATE_KEY` | emailjs.com, *Account > API Keys* | Solo si la cuenta usa modo estricto |

---

## Generación y distribución del APK

### 1. Versión

En `app/build.gradle.kts`, incrementar los valores en cada entrega:

```kotlin
versionCode = 2        // Entero que siempre debe aumentar
versionName = "1.1"    // Versión visible para la persona usuaria
```

### 2. APK firmado

1. En Android Studio: **Build > Generate Signed App Bundle / APK > APK**.
2. La primera vez, crear un *keystore* (`.jks`) con su contraseña y alias. El archivo y sus contraseñas deben guardarse fuera del repositorio y respaldarse: sin ellos no es posible publicar actualizaciones.
3. Seleccionar la variante **release**. El resultado queda en `app/release/app-release.apk`.
4. Probar el APK antes de distribuirlo. La variante release aplica optimización y ofuscación (R8), por lo que conviene verificar el inicio de sesión, el mapa, la carga de imágenes y el chat.

### 3. Firebase App Distribution

Permite distribuir la aplicación a testers por correo, sin publicarla en Google Play.

**Desde la consola de Firebase**

1. Abrir **App Distribution** y pulsar **Comenzar**.
2. En **Testers y grupos**, crear un grupo (por ejemplo, `testers`) y agregar los correos.
3. En **Versiones**, cargar `app-release.apk`, escribir las notas de la versión, elegir el grupo y pulsar **Distribuir**.

**Desde la terminal**

```bash
npx firebase-tools appdistribution:distribute app/release/app-release.apk --app 1:836401713678:android:456aff273f61d634ba225f --groups testers --release-notes "Versión 1.1"
```

El valor de `--app` es el *App ID* de Firebase, disponible en *Configuración del proyecto > Tus apps*.

### 4. GitHub Releases

El APK no se guarda en el código fuente; se adjunta a una versión publicada:

1. En el repositorio, abrir **Releases > Draft a new release**.
2. Crear la etiqueta de la versión (por ejemplo, `v1.1.0`) y redactar las novedades.
3. Adjuntar `app-release.apk` y pulsar **Publish release**.

---

## Arquitectura

El proyecto aplica **Clean Architecture** con el patrón **MVVM** en la presentación. Las dependencias apuntan siempre hacia el dominio, que no depende de ninguna otra capa.

```text
┌─────────────────────────────────────────────────────────────────┐
│ Presentación    features/ (pantallas y ViewModels)              │
│                 core/ (sistema de diseño, componentes, mapa)    │
│                 navigation/ (rutas)                              │
└──────────────────────────────┬──────────────────────────────────┘
                               │ invoca casos de uso
┌──────────────────────────────▼──────────────────────────────────┐
│ Dominio         domain/ (modelos, casos de uso, contratos)      │
└──────────────────────────────▲──────────────────────────────────┘
                               │ implementa los contratos
┌──────────────────────────────┴──────────────────────────────────┐
│ Datos           data/ (Firebase, Cloudinary, OpenRouter,        │
│                 EmailJS, geocodificador)                         │
└─────────────────────────────────────────────────────────────────┘
                 di/ (Hilt) enlaza las capas
```

**Ejemplo de flujo: aprobación de una publicación**

1. El moderador pulsa "Aprobar" en `PostDetailScreen`.
2. `PostDetailViewModel` invoca `ApprovePostUseCase`.
3. El caso de uso verifica que la persona sea un moderador activo, solicita a `PostRepository` guardar la verificación, otorga puntos al autor y le envía una notificación.
4. `FirestorePostRepository` realiza la escritura en una transacción de Firestore: si otro moderador ya decidió, la operación falla con `PostException.AlreadyReviewed`.
5. El ViewModel actualiza su estado y la pantalla se redibuja.

### Documentación por módulo

| Módulo | Contenido |
|---|---|
| [domain](app/src/main/java/co/edu/uniquindio/petadoptionapp/domain/README.md) | Reglas de negocio y errores del dominio |
| [domain/model](app/src/main/java/co/edu/uniquindio/petadoptionapp/domain/model/README.md) | Entidades y sus relaciones |
| [domain/usecase](app/src/main/java/co/edu/uniquindio/petadoptionapp/domain/usecase/README.md) | Casos de uso y flujos |
| [domain/repository](app/src/main/java/co/edu/uniquindio/petadoptionapp/domain/repository/README.md) | Contratos de acceso a datos |
| [data](app/src/main/java/co/edu/uniquindio/petadoptionapp/data/README.md) | Implementaciones, modelo de Firestore y servicios externos |
| [features](app/src/main/java/co/edu/uniquindio/petadoptionapp/features/README.md) | Pantallas y ViewModels |
| [core](app/src/main/java/co/edu/uniquindio/petadoptionapp/core/README.md) | Sistema de diseño, componentes, mapa y utilidades |
| [di](app/src/main/java/co/edu/uniquindio/petadoptionapp/di/README.md) | Inyección de dependencias |
| [navigation](app/src/main/java/co/edu/uniquindio/petadoptionapp/navigation/README.md) | Rutas y flujo de navegación |
| [firebase](firebase/README.md) | Configuración de Firebase, reglas, índices y datos de prueba |
| [docs/modelo-datos.md](docs/modelo-datos.md) | Diagrama entidad-relación |

---

## Estructura del proyecto

```text
PF_Kotlin_Pet-Adoption/
├── app/
│   ├── build.gradle.kts              Dependencias, versión y lectura de llaves (BuildConfig)
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml   Permisos, actividad principal y FileProvider
│       │   ├── java/co/edu/uniquindio/petadoptionapp/
│       │   │   ├── Application.kt    Punto de arranque de Hilt
│       │   │   ├── MainActivity.kt   Actividad única: tema y navegación
│       │   │   ├── domain/           Modelos, casos de uso, contratos y errores
│       │   │   ├── data/             Implementaciones de los contratos
│       │   │   ├── di/               Módulos de Hilt
│       │   │   ├── core/             Sistema de diseño, componentes, mapa y utilidades
│       │   │   ├── features/         Pantallas y ViewModels
│       │   │   └── navigation/       Rutas y grafo de navegación
│       │   └── res/                  Textos (es, en, fr), imágenes y configuración
│       └── test/                     Datos de prueba y su exportador
├── firebase/
│   ├── firestore.rules               Reglas de seguridad
│   ├── firestore.indexes.json        Índices compuestos
│   ├── emailjs-template.html         Plantilla del correo del código A2F
│   └── seed/                         Script de carga de datos de prueba
├── docs/                             Modelo de datos
├── gradle/libs.versions.toml         Catálogo de versiones
├── firebase.json, .firebaserc        Configuración de Firebase CLI
└── LICENSE
```

---

## Librerías y servicios externos

### Resumen

| Tecnología | Versión | Función en la aplicación |
|---|---|---|
| Kotlin | 2.4.20 | Lenguaje de programación |
| Android Gradle Plugin | 9.4.1 | Compilación |
| Jetpack Compose (BoM) y Material 3 | 2026.09.00 | Interfaz declarativa |
| Navigation Compose | 2.10.2 | Navegación con rutas tipadas |
| Hilt (Dagger) y KSP | 2.60.1 | Inyección de dependencias |
| Kotlin Coroutines y Flow | 1.10.2 | Asincronía y estado reactivo |
| kotlinx.serialization | 1.11.0 | JSON para rutas, Firestore, OpenRouter y EmailJS |
| Firebase BoM (Authentication, Firestore) | 35.1.0 | Cuentas y base de datos |
| OkHttp | 4.12.0 | Peticiones HTTP a Cloudinary, OpenRouter y EmailJS |
| Coil | 3.6.3 | Carga y caché de imágenes |
| MapLibre Android | 11.13.5 | Mapas |
| AppCompat | 1.7.1 | Idioma por aplicación |

### Firebase

**Authentication.** Gestiona las cuentas con correo y contraseña. `FirebaseAuthRepository` inicia sesión, registra cuentas, restaura la sesión al abrir la aplicación, envía el correo de restablecimiento de contraseña, reautentica antes de operaciones sensibles y elimina cuentas. La protección contra enumeración de correos está activa, por lo que la disponibilidad de un correo se consulta en un índice propio de Firestore.

**Cloud Firestore.** Base de datos documental en tiempo real. Cada contrato del dominio tiene una implementación en `data/repository/firebase/`. Los modelos se convierten en documentos mediante `FirestoreMapper`, que usa kotlinx.serialization para respetar la inmutabilidad del dominio. Las operaciones que modifican varios documentos se ejecutan en transacciones, y los identificadores deterministas evitan duplicados. Los *snapshot listeners* mantienen actualizados el perfil, los catálogos y los mensajes, y la caché local permite el uso sin conexión.

**Reglas de seguridad e índices.** `firebase/firestore.rules` define quién puede leer y escribir cada colección; `firebase/firestore.indexes.json` declara los índices compuestos. Ambos se publican con Firebase CLI.

**Admin SDK.** El script `firebase/seed/seed.mjs` usa el SDK de administración para crear las cuentas de prueba con identificadores fijos y cargar los documentos iniciales.

**App Distribution.** Distribución del APK a testers (ver [Generación y distribución del APK](#generación-y-distribución-del-apk)).

**Integración.** El plugin `com.google.gms.google-services` procesa `app/google-services.json`; `di/FirebaseModule.kt` entrega `FirebaseAuth` y `FirebaseFirestore` como instancias únicas.

### EmailJS — verificación en dos pasos

Firebase Authentication no envía códigos personalizados por correo, y las Cloud Functions requieren un plan de pago. Por ello, la verificación en dos pasos se implementó con EmailJS:

1. Tras validar el correo y la contraseña, si la cuenta tiene A2F activa, se genera un código de 6 dígitos con `SecureRandom`.
2. Firestore guarda únicamente la huella SHA-256 del código en `twoFactorChallenges/{uid}`, con vencimiento de 5 minutos y un máximo de 5 intentos.
3. `EmailJsCodeSender` envía el correo mediante `POST https://api.emailjs.com/api/v1.0/email/send`, con la plantilla `firebase/emailjs-template.html`.
4. El código ingresado se valida en una transacción y, si es correcto, se abre la sesión de la aplicación.

Al generarse el código en el dispositivo, esta implementación es adecuada para un entorno académico; en producción el código debería generarse en un servidor.

### MapLibre y OpenFreeMap — mapas

El proyecto utilizó inicialmente Mapbox y migró a **MapLibre**, su alternativa de código abierto, porque no requiere tokens ni datos de facturación. El estilo de los mapas proviene de **OpenFreeMap** (`https://tiles.openfreemap.org/styles/liberty`), también gratuito y sin llaves.

- `core/component/map/AppMap.kt` integra el `MapView` en Compose mediante `AndroidView`, dibuja los marcadores en una `SymbolLayer` y las zonas aproximadas en una `FillLayer` a partir de fuentes GeoJSON.
- `MarkerImages.kt` genera la imagen de cada marcador a partir de íconos de Material, con el color, el ícono y la forma definidos en la categoría guardada en Firestore.
- `LocationPicker.kt` permite elegir una ubicación por ciudad, dirección o punto en el mapa.
- La conversión entre direcciones y coordenadas usa el `Geocoder` de Android, que no requiere llaves.

### OpenRouter — inteligencia artificial

OpenRouter ofrece acceso a múltiples modelos de lenguaje mediante una API compatible con OpenAI.

- `OpenRouterAiRepository` envía `POST https://openrouter.ai/api/v1/chat/completions` con el modelo configurado (por defecto `openai/gpt-4o-mini`, con soporte de imágenes), temperatura 0.2 y respuesta en formato JSON.
- La solicitud incluye el texto de la publicación, hasta tres fotografías y hasta diez publicaciones similares (del mismo autor o de la misma categoría a menos de 10 km) para detectar duplicados.
- `BackgroundPostAnalysisScheduler` ejecuta el análisis en segundo plano al crear o editar una publicación; los moderadores pueden solicitarlo de nuevo de forma individual o por lotes.
- El resultado se guarda en `Post.aiReview` y la prioridad sugerida ordena la cola de moderación. La IA nunca aprueba ni rechaza.

### Cloudinary — almacenamiento de imágenes

- `CloudinaryImageRepository` sube cada imagen con un *upload preset* sin firma (`POST https://api.cloudinary.com/v1_1/{cloud_name}/image/upload`) a la carpeta `KogMaw-Adoption App/{posts|profiles|chats}` y guarda la URL segura devuelta.
- Antes de subirla, `LocalImageStore` reduce la imagen a 1600 píxeles en formato JPEG.
- El *API secret* de Cloudinary nunca se incluye en la aplicación.
- Las imágenes se muestran con Coil.

### Idiomas

La aplicación usa la API de idiomas por aplicación de AppCompat (`AppCompatDelegate.setApplicationLocales`) junto con `res/xml/locales_config.xml`. Las traducciones se encuentran en `values/` (español), `values-en/` y `values-fr/`, y el cambio se aplica sin reiniciar la aplicación.

---

## Seguridad

- **Archivos excluidos del repositorio:** `local.properties`, `app/google-services.json`, `firebase/seed/service-account.json`, `firebase/seed/.env` y el *keystore* de firma.
- **Reglas de Firestore.** Constituyen la protección efectiva de los datos: cada persona modifica únicamente su información, los chats y las solicitudes solo son visibles para sus participantes, nadie puede aprobar su propia publicación y los moderadores no pueden crearse desde la aplicación.
- **Llaves en el APK.** Las llaves de `local.properties` quedan incluidas en el APK compilado. Antes de distribuirlo, se recomienda establecer límites de gasto en OpenRouter. En un entorno de producción, las llamadas a OpenRouter y EmailJS deberían realizarse desde un servidor.

---

## Licencia

Este proyecto se distribuye bajo la licencia **Apache 2.0**. Consultar el archivo [LICENSE](LICENSE).
