# Núcleo Compartido (core)

## Propósito

El módulo `core` agrupa los elementos que comparten todas las pantallas: el **sistema de diseño** (paleta, colores semánticos y tipografía), los **componentes de interfaz reutilizables**, el **mapa** y un conjunto de **utilidades del dispositivo**. Centralizar estos elementos garantiza una apariencia consistente y permite modificar el diseño en un único lugar.

Los componentes de `core` no contienen reglas de negocio ni acceden a Firebase. Reciben los datos ya preparados por la pantalla y comunican las acciones de la persona usuaria mediante funciones (`onClick`, `onValueChange`).

## Estructura

```text
core/
├── theme/               Sistema de diseño
├── component/
│   ├── button/          Botones
│   ├── card/            Tarjetas y contenedores
│   ├── dialog/          Diálogos
│   ├── form/            Campos y controles de formulario
│   ├── header/          Encabezados de pantalla
│   ├── icon/            Avatares e íconos de marca
│   ├── indicator/       Indicadores de estado y progreso
│   ├── list/            Listas reutilizables
│   ├── map/             Mapa, marcadores y selección de ubicación
│   ├── navigation/      Pestañas y chips de navegación secundaria
│   ├── photo/           Selección y visualización de fotos
│   └── text/            Componentes de texto
└── util/                Utilidades del dispositivo y de formato
```

## `theme/` — Sistema de diseño

El sistema de diseño se organiza en tres capas. Las pantallas solo utilizan la tercera.

| Archivo | Capa | Responsabilidad |
|---|---|---|
| `Color.kt` | 1. Paleta | `PetPalette`: valores hexadecimales de la marca. Los nombres describen el color (`Sand200`), nunca su uso. |
| `AppColors.kt` | 2. Roles | `AppColors`: colores semánticos (`actionPrimary`, `surface`, `textPrimary`, `stateError`, `border`…) en versiones clara (`LightAppColors`) y oscura (`DarkAppColors`). |
| `Theme.kt` | 3. Punto de entrada | `PetAdoptionAppTheme`: selecciona la paleta según el tema, la publica en el árbol de composición y deriva el esquema de Material 3. `AppTheme.colors` da acceso a los roles. |
| `Type.kt` | Tipografía | Dos familias: **Gabarito** para títulos y **Rubik** para botones, formularios e información. |
| `LegacyColors.kt` | Compatibilidad | Alias de la nomenclatura anterior, marcados como obsoletos durante la migración. |

**Regla de uso:** una pantalla nunca utiliza un valor hexadecimal ni un color de la paleta directamente. Siempre solicita un rol (`AppTheme.colors.surface`), lo que garantiza la compatibilidad con el modo oscuro.

## `component/` — Componentes reutilizables

### `button/`

| Archivo | Descripción |
|---|---|
| `AppButtonDefaults.kt` | Forma de píldora y altura mínima compartidas por todos los botones. |
| `PrimaryButton.kt` | Acción principal de una pantalla ("Iniciar sesión", "Publicar"): relleno y a todo el ancho. |
| `SecondaryButton.kt` | Acción secundaria delineada, con ícono opcional. |
| `DangerButton.kt` | Acción destructiva o de cancelación, con el color de error. |
| `TextActionButton.kt` | Acción de menor peso visual: solo texto e ícono. |
| `MenuActionButton.kt` | Fila de menú con ícono, texto y flecha, para accesos rápidos. |
| `BackButton.kt` | Botón de regreso de las barras superiores. |

### `card/`

| Archivo | Descripción |
|---|---|
| `AppCard.kt` | Tarjeta base: fondo, borde y esquinas de 24 dp. Las demás tarjetas se construyen sobre ella. |
| `SectionCard.kt` | Tarjeta con encabezado (título, subtítulo e ícono opcionales) y contenido libre. |
| `FormCard.kt` | Agrupa un paso de un formulario por pasos, con título accesible como encabezado. |
| `StatCard.kt` | Métrica individual: etiqueta y valor destacado. |
| `LevelProgressCard.kt` | Nivel de reputación, puntos y barra de progreso al siguiente nivel. |
| `PostSummaryCard.kt` | Resumen de una publicación: miniatura, tipo, estado, título y detalles. Incluye `PostMetaLine`. |
| `NotificationCard.kt` | Elemento de la bandeja de notificaciones con ícono, título, tiempo y etiqueta de nuevo. |
| `UserSummaryCard.kt` | Cabecera de perfil con avatar, nombre y ubicación. |
| `BadgeItem.kt` | Fila de una insignia obtenida. |
| `NoticeBanner.kt` | Aviso destacado sobre el estado de un elemento. |
| `InfoTip.kt` | Consejo breve dentro de un formulario. |
| `SwipeToDeleteBox.kt` | Contenedor que añade el gesto de deslizar para eliminar, con acción accesible equivalente. |
| `LocationMapPlaceholder.kt` | Recuadro con las dimensiones de un mapa, para diseños donde aún no se muestra el mapa real. |

### `dialog/`

| Archivo | Descripción |
|---|---|
| `AppInfoDialog.kt` | Comunica un resultado que requiere solo confirmación ("Cuenta creada"). |
| `AppConfirmDialog.kt` | Solicita aprobación antes de una acción (cerrar sesión, finalizar una publicación). |
| `AppFormDialog.kt` | Solicita datos adicionales antes de ejecutar una acción (cambiar contraseña, confirmar eliminación). |
| `AppActionDialog.kt` | Presenta el resultado de un flujo importante y ofrece hacia dónde continuar. |

### `form/`

| Archivo | Descripción |
|---|---|
| `AppTextField.kt` | Campo de texto de los formularios de cuenta, con soporte para contraseñas, ícono y mensaje de error. |
| `AppTextArea.kt` | Área de varias líneas con contador de caracteres. |
| `FormFields.kt` | Campos del formulario por pasos (`FormLabel`, `FormTextField`, `FormTextArea`) y utilidades de estilo. |
| `FormDropdown.kt` | Lista desplegable que informa la posición elegida. |
| `OptionSelector.kt` | Selector de opción única con apariencia de botones (`SelectorOption`). |
| `AppChipGroup.kt` | Grupo de chips de selección única que se ajusta en varias líneas; admite un elemento previo por chip (por ejemplo, el ícono de la categoría). |
| `SwitchRow.kt` | Fila con texto e interruptor. |
| `SettingRows.kt` | Filas de la pantalla de ajustes: con interruptor (`SettingSwitchRow`) o de acción (`SettingActionRow`). |
| `SearchField.kt` | Buscador con forma de píldora. |
| `MessageInputBar.kt` | Barra inferior para escribir comentarios o mensajes. |
| `PasswordStrengthBar.kt` | Indicador de seguridad de la contraseña, calculado con las mismas reglas de `AuthValidator`. |
| `RequiredLabel.kt` | Título de campo con asterisco de obligatoriedad. |
| `TermsCheckbox.kt` | Aceptación de términos y políticas con enlaces dentro del texto. |

### `header/`, `icon/`, `indicator/`, `list/`, `navigation/` y `text/`

| Archivo | Descripción |
|---|---|
| `header/AppTopBar.kt` | Barra superior de pantallas secundarias: regreso, título y acciones opcionales. |
| `header/HomeHeader.kt` | Cabecera del inicio: saludo, ciudad y campana con contador de notificaciones. |
| `header/IconHeader.kt` | Encabezado con ícono, título y explicación, para pantallas de un solo propósito. |
| `icon/UserAvatar.kt` | Avatar que carga la foto con Coil y, si no existe, muestra la inicial. |
| `icon/InitialAvatar.kt` | Avatar con la inicial del nombre. |
| `icon/IconCircle.kt` / `icon/PawCircle.kt` | Ícono dentro de un círculo; la versión con huella funciona como logotipo. |
| `icon/OrDivider.kt` | Separador horizontal con texto central. |
| `indicator/StatusIndicator.kt` | Píldora con punto de color para estados. |
| `indicator/StepIndicator.kt` | Progreso de un formulario por pasos ("PASO X DE N"). |
| `list/UserSearchResults.kt` | Resultados de la búsqueda de personas por alias. |
| `navigation/AppTabRow.kt` | Pestañas para dividir el contenido de una pantalla. |
| `navigation/AppChipRow.kt` | Fila desplazable de chips para filtrar listados. |
| `text/ErrorText.kt` | Mensaje de error general, anunciado por los lectores de pantalla. |
| `text/StatusMessage.kt` | Mensaje de lista vacía o error de carga. |
| `text/LinkText.kt` / `text/TextWithLink.kt` | Enlace de texto y frase con enlace. |
| `text/IconTag.kt` / `text/IconTextRow.kt` / `text/InfoRow.kt` | Etiquetas e información con ícono. |
| `text/PawTitle.kt` / `text/WelcomeTitle.kt` | Títulos con la identidad visual de la marca. |

### `photo/`

| Archivo | Descripción | Funciones principales |
|---|---|---|
| `ImageSourcePicker.kt` | Gestiona el flujo completo para obtener fotos desde la cámara o la galería, incluidos los permisos. | `ImageSourcePicker` |
| `PhotoPicker.kt` | Cuadrícula de fotos elegidas y hoja inferior para elegir el origen. | `PhotoPickerGrid`, `PhotoSourceSheet` |
| `PhotoViewerDialog.kt` | Visor a pantalla completa con desplazamiento horizontal y zoom. | `PhotoViewerDialog` |

### `map/` — Mapas con MapLibre

| Archivo | Descripción | Funciones principales |
|---|---|---|
| `AppMap.kt` | Integra el `MapView` de MapLibre en Compose (`AndroidView`) y gestiona su ciclo de vida. Dibuja marcadores en una `SymbolLayer`, zonas en una `FillLayer` (fuentes GeoJSON) y detecta toques sobre los marcadores. | `AppMap` |
| `MapModels.kt` | Modelos independientes de la librería: `GeoPoint`, `MapMarker`, `MarkerShape`, `MarkerKind`, `MapArea`. Si se cambiara de proveedor de mapas, solo cambiaría este paquete. | `Location.toGeoPoint()` |
| `MarkerImages.kt` | Genera las imágenes de los marcadores a partir de íconos de Material: forma, color, borde y sombra. Usa ícono oscuro sobre colores claros. | `rememberMarkerImages`, `MapMarker.imageKey()` |
| `LocationPicker.kt` | Selector de ubicación: ciudad, búsqueda de dirección y punto tocado en el mapa. | `LocationPicker` |
| `LocationPickerViewModel.kt` | Estado del selector; usa `SearchAddressUseCase` y `ReverseGeocodeUseCase`. | `onCitySelected`, `onQueryChange`, `onSuggestionClick`, `onMapClick` |
| `CurrentLocationRequester.kt` | Solicita el permiso de ubicación y entrega la posición actual. | `rememberCurrentLocationRequester` |

**Estilo del mapa.** Se utiliza el estilo gratuito de OpenFreeMap (`https://tiles.openfreemap.org/styles/liberty`), que no requiere llaves ni cuenta.

**Uso en la aplicación.** El selector de ubicación aparece en el registro, al crear o editar publicaciones y en Editar perfil. El mapa con marcadores por categoría aparece en Explorar, y el mapa con zona aproximada en el detalle de una publicación.

## `util/` — Utilidades

| Archivo | Descripción | Funciones principales |
|---|---|---|
| `AppLanguage.kt` | Idioma de la aplicación (español, inglés, francés) mediante la API de idiomas por aplicación de AppCompat; el cambio se aplica sin reiniciar. | `currentAppLanguage`, `setAppLanguage` |
| `DeviceLocation.kt` | Lectura de la ubicación con `LocationManager`, sin depender de Google Play Services. | `currentDeviceLocation` |
| `ApproximateLocation.kt` | Desplaza el punto de una publicación entre 150 y 300 m en una dirección fija derivada de su id, para no revelar la dirección exacta. | `approximatePoint` |
| `LocalImageStore.kt` | Copia las fotos elegidas a la carpeta privada de la aplicación y las reduce a 1600 píxeles en JPEG antes de subirlas. | `newCameraPhoto`, `saveImageLocally`, `deleteQuietly` |
| `ShareImage.kt` | Comparte una imagen generada por la aplicación mediante `FileProvider`. | `Context.shareImage` |
| `TimeAgo.kt` | Texto relativo de antigüedad ("hace 3 horas") con recursos plurales. | `agoText` |
| `UserNameGenerator.kt` | Alias automático del registro: primer nombre normalizado y un sufijo aleatorio. | `UserNameGenerator.generate` |

## Interconexión

```text
features/*Screen ──usa──► core/component  (botones, tarjetas, formularios, mapa)
                 ──usa──► core/theme      (AppTheme.colors, tipografía)
                 ──usa──► core/util       (idioma, ubicación, fotos)

core/component/map/LocationPickerViewModel ──► domain/usecase/location ──► GeocodingRepository
core/component/photo/ImageSourcePicker ──────► core/util/LocalImageStore
```

`core` no depende de `features` ni de `data`. La única dependencia controlada hacia el dominio es el selector de ubicación, que necesita los casos de uso de geocodificación.

## Guía de extensión

- Antes de crear un componente nuevo, verificar si existe uno equivalente en esta carpeta.
- Los componentes nuevos deben utilizar únicamente roles de `AppTheme.colors`, respetar un área táctil mínima de 48 dp e incluir descripciones de accesibilidad y una vista previa (`@Preview`).
