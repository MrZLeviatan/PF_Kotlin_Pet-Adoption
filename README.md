<h1 align="center">

🐾 _Huellas — KogMaw Adoption App_ 🐾

</h1>

<p align="center">
Aplicación Android para la <b>adopción responsable de mascotas</b>, el reporte de animales <b>perdidos y encontrados</b>, los <b>hogares de paso</b> y las <b>jornadas veterinarias</b>.
</p>

<p align="center">
  <img alt="Android" src="https://img.shields.io/badge/Android-7.0%2B-3DDC84?logo=android&logoColor=white">
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.4-7F52FF?logo=kotlin&logoColor=white">
  <img alt="Jetpack Compose" src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white">
  <img alt="Firebase" src="https://img.shields.io/badge/Firebase-Auth%20%7C%20Firestore-FFCA28?logo=firebase&logoColor=black">
  <img alt="Licencia" src="https://img.shields.io/badge/Licencia-Apache%202.0-blue">
</p>

<br>

> **Universidad del Quindío**  
> **Programa:** Ingeniería de Sistemas y Computación  
> **Curso:** Desarrollo de Aplicaciones para Dispositivos Móviles (2026-2)  
> **Autores:** Jhon Stivenson Mendez, Nicolas Cabrera Serrano, Robinson Daniel Gañan

<br>

## Contenido

1. [¿Qué es Huellas?](#qué-es-huellas)
2. [Instalar la app (usuarios)](#instalar-la-app-usuarios)
3. [Poner en marcha el proyecto (desarrolladores)](#poner-en-marcha-el-proyecto-desarrolladores)
4. [Generar y publicar el APK](#generar-y-publicar-el-apk)
5. [Cómo está construida](#cómo-está-construida)
6. [Seguridad](#seguridad)
7. [Licencia](#licencia)

<br>

## ¿Qué es Huellas?

Huellas conecta a personas que quieren ayudar a los animales. Cualquiera puede publicar una mascota en adopción, avisar que perdió o encontró un animal, ofrecer un hogar de paso o difundir una jornada veterinaria. Un equipo de **moderadores** revisa cada publicación antes de que sea pública, con ayuda de **inteligencia artificial**.

| Para quien usa la app | Para quien modera |
|---|---|
| 📝 Crea publicaciones con fotos (cámara o galería) y ubicación | ✅ Revisa la cola ordenada por prioridad |
| 🗺️ Explora el mapa con un ícono por categoría y filtros | 🤖 Ve el análisis de IA: resumen, alertas y posibles duplicados |
| 💬 Comenta, marca "Es importante" y chatea en vivo | ❌ Aprueba o rechaza con un motivo |
| 🏠 Envía y responde solicitudes de adopción | 📊 Consulta sus estadísticas |
| 🏅 Gana puntos, niveles e insignias | 👤 Ve el perfil completo de quien publica |
| 🔐 Inicia sesión con verificación en dos pasos por correo | |
| 🌎 Usa la app en español, inglés o francés | |

<br>

## Instalar la app (usuarios)

**Opción 1 — GitHub:** descarga el archivo `app-release.apk` de la última versión en la sección [**Releases**](https://github.com/MrZLeviatan/PF_Kotlin_Pet-Adoption/releases) del repositorio.

**Opción 2 — Firebase App Distribution:** si te invitaron como tester, recibirás un correo. Ábrelo en el teléfono, acepta la invitación y descarga la app desde ahí.

**Para instalar el APK:**
1. Abre el archivo descargado en el teléfono (Android 7.0 o superior).
2. Si Android lo pide, permite **"Instalar apps desconocidas"** para el navegador o el gestor de archivos.
3. Abre Huellas, crea tu cuenta y listo.

> La app pide permiso de **ubicación** (para el mapa y las publicaciones) y de **cámara** (para las fotos). Puedes negarlos y elegir la ubicación o las fotos a mano.

<br>

## Poner en marcha el proyecto (desarrolladores)

### Requisitos

| Herramienta | Para qué |
|---|---|
| Android Studio (reciente) con JDK 17 o superior | Compilar y ejecutar la app |
| Un proyecto de Firebase (plan gratuito) | Cuentas y base de datos |
| Node.js 22 o superior | Publicar reglas y cargar los datos de prueba |
| Cuentas gratuitas de Cloudinary, EmailJS y OpenRouter | Fotos, correo del código A2F e IA |

### Paso a paso

**1. Clonar el repositorio**
```bash
git clone https://github.com/MrZLeviatan/PF_Kotlin_Pet-Adoption.git
```

**2. Conectar Firebase.** En la [consola de Firebase](https://console.firebase.google.com), registra una app Android con el paquete `co.edu.uniquindio.petadoptionapp`, descarga `google-services.json` y cópialo en `app/google-services.json`.

**3. Escribir las llaves en `local.properties`** (en la raíz; git lo ignora). Android Studio crea este archivo con la ruta del SDK; agrega debajo:

```properties
CLOUDINARY_CLOUD_NAME=tu_cloud_name
CLOUDINARY_UPLOAD_PRESET=tu_preset_sin_firma
OPENROUTER_API_KEY=sk-or-...
OPENROUTER_MODEL=openai/gpt-4o-mini
EMAILJS_SERVICE_ID=service_...
EMAILJS_TEMPLATE_ID=template_...
EMAILJS_PUBLIC_KEY=...
EMAILJS_PRIVATE_KEY=...
```

| Llave | Dónde se consigue | ¿Obligatoria? |
|---|---|---|
| `CLOUDINARY_*` | Cloudinary → *Settings → Upload → Upload presets* (crea uno **Unsigned**) | Sí, para subir fotos |
| `OPENROUTER_*` | openrouter.ai → *Keys*. El modelo es opcional | No: sin ella la IA queda desactivada |
| `EMAILJS_*` | emailjs.com → *Email Services*, *Email Templates* y *Account* | Sí, para la verificación en dos pasos |

**4. Configurar Firebase** (Authentication, Firestore, reglas, índices y la plantilla de EmailJS). Sigue la guía [**firebase/README.md**](firebase/README.md). En resumen:
```bash
npx firebase-tools login
```
```bash
npx firebase-tools deploy --only firestore
```

**5. Cargar los datos de prueba** (opcional, pero recomendado): 4 usuarios, 5 moderadores, 5 categorías y 14 publicaciones. También está explicado en [firebase/README.md](firebase/README.md#3-datos-de-prueba).

**6. Ejecutar.** Abre el proyecto en Android Studio, espera la sincronización de Gradle y pulsa **Run ▶** con un emulador o un teléfono conectado.

<br>

## Generar y publicar el APK

### 1. Preparar la versión
En `app/build.gradle.kts`, sube los números en cada entrega:
```kotlin
versionCode = 2        // entero, siempre mayor que el anterior
versionName = "1.1"    // el texto que ve la persona
```

### 2. Crear el APK firmado
1. En Android Studio: **Build → Generate Signed App Bundle / APK → APK → Next**.
2. La primera vez pulsa **Create new…** y crea tu *keystore* (`.jks`): ruta, contraseña, alias y datos del autor.
   > ⚠️ Guarda el `.jks` y sus contraseñas **fuera del repositorio** y haz una copia: sin ellos no podrás publicar actualizaciones.
3. Elige la variante **release** y pulsa **Create**. El APK queda en `app/release/app-release.apk`.
4. **Pruébalo antes de repartirlo**: la versión release reduce y ofusca el código (R8). Instálalo y revisa el inicio de sesión, el mapa, las fotos y el chat.

### 3. Distribuirlo con Firebase App Distribution
Sirve para enviar la app a testers por correo, sin publicarla en Play Store.

**Desde la consola:**
1. Firebase → **App Distribution → Comenzar**.
2. Pestaña **Testers y grupos**: crea un grupo (por ejemplo `testers`) y agrega los correos.
3. Pestaña **Releases**: arrastra `app-release.apk`, escribe las notas de la versión, elige el grupo y pulsa **Distribuir**.

**Desde la terminal** (con `firebase-tools` ya iniciado con `login`):
```bash
npx firebase-tools appdistribution:distribute app/release/app-release.apk --app 1:836401713678:android:456aff273f61d634ba225f --groups testers --release-notes "Versión 1.1"
```
> El valor de `--app` es el *App ID* de Firebase (Configuración del proyecto → Tus apps).

### 4. Publicarlo en GitHub
El APK no se guarda en el código: se adjunta a una **Release**.
1. En el repositorio: **Releases → Draft a new release**.
2. Crea la etiqueta (por ejemplo `v1.1.0`), escribe el título y las novedades.
3. Arrastra `app-release.apk` a *Attach binaries* y pulsa **Publish release**.

<br>

## Cómo está construida

La app sigue **Clean Architecture** con **MVVM**: cada capa tiene una sola responsabilidad y solo conoce a la de abajo.

```text
  Pantallas (features/ + core/)      Lo que ves: Jetpack Compose y ViewModels
              │ piden acciones
              ▼
  Dominio (domain/)                  Las reglas: modelos, casos de uso y contratos
              ▲ cumplen los contratos
              │
  Datos (data/)                      Los servicios: Firebase, Cloudinary, OpenRouter, EmailJS, GPS
```

Hilt (`di/`) une las piezas y `navigation/` mueve entre pantallas. Cada carpeta tiene su propia guía:

| Capa | Qué encontrarás |
|---|---|
| [domain](app/src/main/java/co/edu/uniquindio/petadoptionapp/domain/README.md) | Reglas del negocio: [modelos](app/src/main/java/co/edu/uniquindio/petadoptionapp/domain/model/README.md), [casos de uso](app/src/main/java/co/edu/uniquindio/petadoptionapp/domain/usecase/README.md) y [repositorios](app/src/main/java/co/edu/uniquindio/petadoptionapp/domain/repository/README.md) |
| [data](app/src/main/java/co/edu/uniquindio/petadoptionapp/data/README.md) | Cómo se guardan y traen los datos (Firestore y servicios externos) |
| [features](app/src/main/java/co/edu/uniquindio/petadoptionapp/features/README.md) | Todas las pantallas y cómo están hechas |
| [core](app/src/main/java/co/edu/uniquindio/petadoptionapp/core/README.md) | Tema visual, componentes reutilizables y mapa |
| [di](app/src/main/java/co/edu/uniquindio/petadoptionapp/di/README.md) · [navigation](app/src/main/java/co/edu/uniquindio/petadoptionapp/navigation/README.md) | Inyección de dependencias y rutas |
| [firebase](firebase/README.md) · [modelo de datos](docs/modelo-datos.md) | Configuración de Firebase y diagrama de entidades |

### Tecnologías y servicios

| Tecnología | Uso en la app |
|---|---|
| **Kotlin 2.4 + Jetpack Compose (Material 3)** | Lenguaje e interfaz |
| **Hilt** | Inyección de dependencias |
| **Navigation Compose** | Navegación con rutas tipadas |
| **Coroutines / Flow** | Tareas en segundo plano y datos en vivo |
| **Firebase Authentication** | Cuentas, sesión y recuperación de contraseña |
| **Cloud Firestore** | Base de datos en tiempo real, con caché sin conexión |
| **EmailJS** | Envío del código de verificación en dos pasos |
| **Cloudinary** | Almacenamiento de fotos (subida sin firma) |
| **MapLibre + OpenFreeMap** | Mapas gratuitos, sin llaves |
| **OpenRouter** | IA que analiza las publicaciones para los moderadores |
| **Coil · OkHttp · kotlinx.serialization** | Imágenes, llamadas web y JSON |

¿Cómo se integró cada servicio? Está explicado paso a paso en [data/README.md](app/src/main/java/co/edu/uniquindio/petadoptionapp/data/README.md#servicios-externos).

<br>

## Seguridad

- **Nunca se suben al repositorio:** `local.properties`, `app/google-services.json`, `firebase/seed/service-account.json`, `firebase/seed/.env` y el keystore `.jks`.
- **Las reglas de Firestore** (`firebase/firestore.rules`) son la protección real: cada quien edita solo lo suyo, los chats solo los ven sus participantes y nadie se auto-aprueba.
- **Las llaves de `local.properties` viajan dentro del APK.** Antes de publicarlo, pon un límite de gasto a la llave de OpenRouter. En un producto real, OpenRouter y EmailJS deberían llamarse desde un servidor.

<br>

## Licencia

Distribuido bajo la licencia **Apache 2.0**. Consulta el archivo [LICENSE](LICENSE).
