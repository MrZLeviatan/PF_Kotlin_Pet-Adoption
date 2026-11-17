<h1 align="center">

_Núcleo compartido (core)_

</h1>

**En pocas palabras:** aquí están las piezas que todas las pantallas comparten: los **colores y letras** de la app, los **componentes** reutilizables (botones, tarjetas, campos, diálogos), el **mapa** y algunas **utilidades** del teléfono. Gracias a esto toda la app se ve igual y un cambio de diseño se hace en un solo lugar.

<br>

## Qué hay aquí

```text
core/
├── theme/        Colores, tipografía y modo claro/oscuro
├── component/    Piezas de interfaz reutilizables
│   ├── button/   card/   dialog/   form/   header/   icon/
│   ├── indicator/   list/   navigation/   photo/   text/
│   └── map/      Mapa, marcadores y selector de ubicación
└── util/         Idioma, ubicación, fotos, compartir, fechas
```

<br>

## `theme/` — la identidad visual

| Archivo | Qué define |
|---|---|
| `Color.kt` | La paleta base: cafés, cremas, amarillo y tonos nocturnos |
| `AppColors.kt` | Colores **con significado** (`actionPrimary`, `surface`, `textPrimary`, `stateError`…), en versión clara y oscura |
| `Type.kt` | Tipografía |
| `Theme.kt` | `PetAdoptionAppTheme { … }`, que envuelve toda la app, y el acceso `AppTheme.colors` |

> Regla de oro: las pantallas nunca usan un color directo. Usan `AppTheme.colors.surface`, y el tema decide el valor según el modo claro u oscuro.

<br>

## `component/` — piezas reutilizables

| Carpeta | Ejemplos |
|---|---|
| `button/` | `PrimaryButton`, `SecondaryButton`, `DangerButton`, `BackButton` |
| `card/` | `AppCard`, `SectionCard`, `StatCard`, `PostSummaryCard`, `NotificationCard` |
| `dialog/` | `AppInfoDialog`, `AppConfirmDialog`, `AppFormDialog` |
| `form/` | `AppTextField`, `AppChipGroup`, `FormDropdown`, `OptionSelector`, `SwitchRow` |
| `header/` · `icon/` · `text/` | Barras superiores, avatares, textos de error y enlaces |
| `photo/` | Elegir foto de cámara o galería y verla en grande |
| `map/` | Mapa, marcadores, selector de ubicación y ubicación actual |

Todos siguen las mismas reglas:
- **No guardan lógica:** reciben datos y avisan de lo que pasa (`onClick`, `onValueChange`). La pantalla decide qué hacer.
- **Son accesibles:** área táctil mínima de 48 dp y descripciones para TalkBack.
- **Tienen `@Preview`** para verlos en Android Studio sin ejecutar la app.

<br>

## `component/map/` — mapas con MapLibre

| Archivo | Qué hace |
|---|---|
| `AppMap.kt` | Muestra el mapa (MapLibre con el estilo gratuito **OpenFreeMap**, sin llaves), sus marcadores y zonas, y detecta toques |
| `MarkerImages.kt` | Dibuja cada marcador con el color, el ícono y la forma de su categoría |
| `LocationPicker.kt` | Selector de ubicación: ciudad, búsqueda de dirección y toque en el mapa |
| `CurrentLocationRequester.kt` | Pide el permiso y entrega la ubicación actual del teléfono |
| `MapModels.kt` | Datos simples que el mapa sabe dibujar (`GeoPoint`, `MapMarker`…) |

El selector de ubicación se usa en el registro, al publicar y en Editar perfil.

<br>

## `util/` — utilidades

| Archivo | Para qué sirve |
|---|---|
| `AppLanguage.kt` | Cambiar el idioma (español, inglés, francés) sin reiniciar |
| `DeviceLocation.kt` | Leer la ubicación del teléfono |
| `ApproximateLocation.kt` | Mover el punto de una publicación unos 400 m para no revelar la dirección exacta |
| `LocalImageStore.kt` | Guardar y reducir las fotos elegidas antes de subirlas |
| `ShareImage.kt` | Compartir una imagen (por ejemplo, tus métricas) |
| `TimeAgo.kt` | Mostrar fechas como "hace 5 min" |
| `UserNameGenerator.kt` | Crear el alias automático al registrarse |

<br>

## Para desarrolladores

- **¿Necesitas un botón, campo o diálogo?** Busca primero aquí antes de crear uno nuevo.
- **¿Un componente nuevo?** Ponlo en la carpeta que corresponda, usa solo colores de `AppTheme.colors` y agrega su `@Preview`.
- `core` no depende de las pantallas (`features`) ni de los datos (`data`). La única excepción es el selector de ubicación, que usa los casos de uso de geocodificación.
