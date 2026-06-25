# Huellitas

<img width="510" height="185" alt="image" src="https://github.com/user-attachments/assets/4b35d2a0-664c-4d38-8361-d331f9468339" />


App Android comunitaria para reportar mascotas perdidas o encontradas. Los usuarios pueden publicar avisos, consultar alertas activas, ver sus propios reportes, explorar un mapa interactivo y contactar directamente al duenio de una mascota.

**TP Integrador - Desarrollo de Aplicaciones I - UADE - 2026**  
Prof. Narducci Adrian Alberto

---

## Stack tecnologico

| Capa | Tecnologia |
|---|---|
| Lenguaje | Kotlin |
| UI | Jetpack Compose + Material Design 3 |
| Arquitectura | Clean Architecture + MVVM |
| Estado | StateFlow / MutableStateFlow |
| Persistencia local | Room (SQLite) + KSP |
| Preferencias locales | DataStore Preferences |
| Autenticacion | Firebase Authentication (email/contrasena) |
| Base de datos remota | Cloud Firestore |
| Almacenamiento de imagenes | Firebase Storage |
| Ubicacion y geocodificacion | LocationManager + Geocoder |
| Carga de imagenes | Coil |
| Navegacion | Navigation Compose |
| Corrutinas | Kotlin Coroutines |
| Inyeccion de dependencias | AppContainer manual (Application + Service Locator) |
| Analytics | Firebase Analytics |

---

<img width="746" height="390" alt="image" src="https://github.com/user-attachments/assets/10493a29-0464-4459-aae1-02ba98f60465" />


## Arquitectura - Clean Architecture + MVVM

La app sigue una separacion estricta en tres capas:

```text
presentation  ->  domain  <-  data
```

El dominio no depende de Compose, Room ni Firebase. Los `ViewModel` trabajan contra `use cases`, y los `use cases` dependen de contratos definidos en `domain/repository`.

```text
Composable (View)
  -> observa StateFlow y dispara eventos
ViewModel
  -> transforma eventos de UI en acciones
  -> expone UiState
UseCase
  -> representa una accion de negocio puntual
Repository (interface en domain)
  -> define el contrato
Repository (implementacion en data)
  -> resuelve si usa Room, Firebase o APIs Android
DAO / DataSource / Android service
  -> acceso concreto a datos
```

### Reglas aplicadas

- Un `Composable` no contiene logica de negocio.
- Un `ViewModel` no conoce Room ni Firebase directamente.
- Un `ViewModel` usa `use cases`, no implementaciones concretas de repositorio.
- Los contratos viven en `domain/repository`.
- Las implementaciones concretas viven en `data/repository`.
- Los `DAO` usan `Flow<T>` para lecturas reactivas y `suspend fun` para escrituras.
- Los `ViewModel` exponen `StateFlow<UiState>`.
- `AppContainer` centraliza el armado de dependencias sin Hilt.

---

<img width="750" height="420" alt="image" src="https://github.com/user-attachments/assets/30d79b11-3ad3-4179-a230-6d505a27de05" />


## Estructura de packages

```text
app/src/main/java/com/uade/huellitas/
|
|-- HuellitasApplication.kt              Entry point + AppContainer
|-- MainActivity.kt                      Host principal de Compose
|
|-- data/
|   |-- local/                           Room + DataStore
|   |-- mapper/                          Entity <-> domain model
|   |-- remote/                          Firebase Auth / Firestore
|   `-- repository/                      Implementaciones concretas
|       |-- AlertRepository.kt
|       |-- UserRepository.kt
|       |-- PetRepository.kt
|       |-- PreferencesSettingsRepository.kt
|       |-- FirebasePhotoStorageRepository.kt
|       |-- AndroidGeocodingRepository.kt
|       `-- AndroidDeviceLocationRepository.kt
|
|-- di/
|   `-- AppContainer.kt                  Construye repositorios y use cases
|
|-- domain/
|   |-- model/                           Alert, Pet, User, AppSettings, ReferenceLocation
|   |-- repository/                      Contratos del dominio
|   |   |-- AlertRepository.kt
|   |   |-- UserRepository.kt
|   |   |-- PetRepository.kt
|   |   |-- SettingsRepository.kt
|   |   |-- PhotoStorageRepository.kt
|   |   |-- GeocodingRepository.kt
|   |   `-- DeviceLocationRepository.kt
|   `-- usecase/
|       |-- alert/
|       |-- auth/
|       |-- location/
|       |-- media/
|       |-- onboarding/
|       |-- pet/
|       |-- settings/
|       `-- user/
|
|-- navigation/
|   |-- Screen.kt
|   `-- NavGraph.kt
|
|-- presentation/
|   |-- alert/                           create, detail, express
|   |-- auth/                            login, register
|   |-- home/                            feed + filtros
|   |-- location/                        permisos de ubicacion runtime
|   |-- map/                             mapa + filtro por radio
|   |-- onboarding/
|   |-- profile/                         perfil, reportes, mascotas, edicion
|   `-- splash/
|
`-- ui/
    |-- components/                      componentes reutilizables
    `-- theme/                           Color, Theme, Type, ThemeState
```

---

## Responsabilidad por capa

### `domain`
Contiene la logica de negocio, independiente de Android, Compose y Firebase.
- `model/`: modelos puros del negocio.
- `repository/`: contratos que necesita el dominio.
- `usecase/`: acciones concretas de la aplicacion, una por responsabilidad.

### `data`
Resuelve de donde salen los datos y como se persisten.
- `local/`: Room, DataStore, entidades, DAOs y converters.
- `remote/`: Firebase Auth, Firestore y sincronizacion remota.
- `repository/`: implementaciones concretas de los contratos del dominio.
- repositorios Android-specific: geocodificacion, almacenamiento de fotos y ubicacion del dispositivo.

### `presentation`
Contiene estado de UI y coordinacion de pantalla.
- Composables: renderizan la UI, no contienen logica de negocio.
- ViewModels: usan `use cases`, emiten `UiState` y procesan eventos de la UI.
- helpers de permisos: encapsulan comportamiento de runtime permissions.

### `di`
Centraliza la construccion manual de dependencias.
- `HuellitasApplication`: expone el container.
- `AppContainer`: instancia repositorios y use cases consumidos por los ViewModels.

---

## Flujo de una funcionalidad

Ejemplo: publicar un aviso.

```text
CreateAlertScreen
  -> CreateAlertViewModel.submitAlert()
  -> CreateAlertUseCase
  -> AlertRepository (interface en domain)
  -> data.repository.AlertRepository
  -> AlertDao (Room) + FirestoreAlertDataSource (Firestore)
```

Ejemplo: filtrar avisos por distancia GPS real.

```text
HomeScreen / MapScreen
  -> RequestLocationPermissionEffect
  -> HomeViewModel / MapViewModel
  -> ResolveReferenceLocationUseCase
  -> GetCurrentDeviceLocationUseCase
  -> DeviceLocationRepository
  -> AndroidDeviceLocationRepository (LocationManager)
```

---

## Modelo de datos

```text
USER
  uid        String   PK
  name       String
  email      String
  phone      String?
  avatarUrl  String?
  location   String?
  createdAt  Long

PET
  id          String   PK
  ownerId     String   FK -> USER.uid
  name        String
  petType     String   "DOG" | "CAT" | "OTHER"
  breed       String?
  color       String?
  description String?
  photoUrls   String
  microchipId String?
  createdAt   Long

ALERT
  id           String   PK
  ownerId      String   FK -> USER.uid
  petId        String?  FK -> PET.id (nullable)
  type         String   "LOST" | "FOUND"
  status       String   "ACTIVE" | "RESOLVED"
  petName      String
  petType      String
  breed        String?
  color        String?
  description  String
  photoUrls    String
  latitude     Double
  longitude    Double
  address      String?
  contactPhone String?
  createdAt    Long
  updatedAt    Long
  pendingSync  Boolean
```

### Decisiones de modelado

- `Alert` denormaliza datos de mascota para que el aviso sea autonomo y no requiera un join.
- `petId` es nullable porque un aviso puede publicarse aunque la mascota no este registrada en la app.
- `pendingSync` sostiene la estrategia offline-first: permite saber que alertas aun no se sincronizaron con Firestore.

---

## Logica por ViewModel

| ViewModel | Responsabilidad |
|---|---|
| `SplashViewModel` | Decide la navegacion inicial segun onboarding, sesion y sincronizacion del perfil actual. |
| `OnboardingViewModel` | Marca el onboarding como completado y habilita el primer acceso al login. |
| `LoginViewModel` | Usa `LoginUseCase` y `SendPasswordResetEmailUseCase`, y expone `AuthUiState`. |
| `RegisterViewModel` | Usa `RegisterUserUseCase` para crear cuenta y guardar perfil local. |
| `HomeViewModel` | Usa `GetActiveAlertsUseCase`, `ResolveReferenceLocationUseCase` y `FilterAlertsByRadiusUseCase` para combinar busqueda por nombre, filtros de tipo, especie y radio GPS. |
| `CreateAlertViewModel` | Usa `GetCurrentUserIdUseCase`, `CreateAlertUseCase` y `UploadAlertPhotoUseCase`. |
| `ExpressAlertViewModel` | Crea un aviso rapido sin foto, solo con zona y tipo. |
| `AlertDetailViewModel` | Usa `GetAlertByIdUseCase`, `UpdateAlertUseCase`, `ResolveAlertUseCase` y `DeleteAlertUseCase`. |
| `MapViewModel` | Usa `GetActiveAlertsUseCase`, `ResolveReferenceLocationUseCase` y `CalculateDistanceMetersUseCase` para poblar el mapa y calcular cercania real. |
| `ProfileViewModel` | Usa `GetCurrentUserUseCase`, `GetMyAlertsUseCase` y `LogoutUseCase`. |
| `EditProfileViewModel` | Usa `UpdateUserProfileUseCase`, `UploadProfilePhotoUseCase` y `ChangePasswordUseCase` para persistir nombre, telefono, avatar y credenciales. |
| `MyAlertsViewModel` | Usa `GetMyAlertsUseCase` filtrado por el usuario autenticado. |
| `MyPetsViewModel` | Usa `GetMyPetsUseCase` y `DeletePetUseCase`. |
| `CreatePetViewModel` | Usa `SavePetUseCase`, `GetCurrentUserIdUseCase` y `UploadPetPhotoUseCase` para registrar mascotas del usuario. |

---

## Estrategia offline en alertas

1. Se guarda primero en Room con `pendingSync = true`.
2. Luego se intenta sincronizar con Firestore.
3. Si la sincronizacion remota sale bien, se actualiza Room con `pendingSync = false`.
4. Si falla, el dato sigue disponible localmente y puede resincronizarse mas tarde.
5. `SyncAlertsUseCase` delega en `AlertRepository.syncFromFirestore()` para recuperar el estado remoto.
6. El comportamiento offline es automatico: no depende de un toggle manual en configuracion.

---

## Historias de usuario

### Primera entrega (H1)

| ID | Historia | Estado |
|---|---|---|
| HU-01 | Como usuario, quiero ver un onboarding al abrir la app por primera vez para entender que hace Huellitas. | Completa |
| HU-02 | Como usuario, quiero registrarme con email y contrasena para crear mi cuenta. | Completa |
| HU-03 | Como usuario, quiero iniciar sesion con mis credenciales para acceder a la app. | Completa |
| HU-04 | Como usuario, quiero ver un listado de avisos cercanos en formato cards para explorar reportes activos. | Completa |
| HU-05 | Como usuario, quiero publicar un aviso con foto, descripcion y datos de contacto para reportar una mascota. | Completa |
| HU-06 | Como usuario, quiero editar los datos de un aviso que publique para corregir informacion. | Completa |
| HU-07 | Como usuario, quiero marcar un aviso como resuelto para indicar que la mascota fue encontrada. | Completa |
| HU-08 | Como usuario, quiero eliminar un aviso propio que ya no es relevante. | Completa |
| HU-09 | Como usuario, quiero filtrar el feed por tipo de animal, tipo de aviso y radio en km para ver solo lo que me interesa. | Completa |
| HU-10 | Como usuario, quiero poder usar la app sin conexion y ver los avisos cacheados localmente. | Completa |
| HU-11 | Como usuario, quiero que la app respete el modo oscuro de mi sistema operativo. | Completa |
| HU-12 | Como usuario, quiero ver el detalle completo de un aviso (foto, descripcion, especie, color, zona, contacto) para decidir si puedo ayudar. | Completa |
| HU-13 | Como usuario, quiero contactar al duenio de una mascota directamente por WhatsApp desde el aviso. | Completa |
| HU-14 | Como usuario, quiero ver los avisos de mi zona en un mapa para entender visualmente donde estan las mascotas reportadas. | Completa |
| HU-15 | Como usuario, quiero publicar un aviso express sin foto, solo indicando la zona, cuando veo algo rapido. | Completa |
| HU-18 | Como usuario, quiero cerrar sesion para proteger mi cuenta en dispositivos compartidos. | Completa |
| HU-20 | Como usuario, quiero usar la galeria de mi celular para adjuntar una foto al aviso que estoy publicando. | Completa |

### Segunda entrega (H2)

| ID | Historia | Estado | Notas |
|---|---|---|---|
| HU-16 | Como usuario, quiero ver un listado de los avisos que yo publique para hacer seguimiento de mis reportes activos. | Completa | Perfil, acceso dedicado a `Mis avisos` y filtros para activos, resueltos y todos. |
| HU-17 | Como usuario, quiero editar mi nombre y datos de contacto en mi perfil. | Completa | Permite editar nombre, telefono, ubicacion y contrasena. |
| HU-19 | Como usuario, quiero recibir un email para restablecer mi contrasena si la olvide. | Completa | Integrado con Firebase Authentication. |
| HU-21 | Como usuario, quiero ver los avisos filtrados por distancia GPS real desde mi ubicacion actual. | Completa | Requiere permiso de ubicacion; feed, mapa y detalle usan la misma ubicacion de referencia con fallback a ubicacion guardada. |
| HU-22 | Como usuario, quiero buscar avisos por nombre de mascota desde el feed. | Completa | Incluye busqueda en tiempo real y convivencia con filtros del feed. |
| HU-23 | Como usuario, quiero subir una foto de perfil para que otros usuarios puedan identificarme. | Completa | Incluye picker de imagen, upload a Firebase Storage y persistencia del avatar. |

---

## Casos de uso formales

### CU-01: Publicar aviso de mascota perdida o encontrada

**Actor principal:** Usuario autenticado.  
**Objetivo:** Permitir que el usuario publique un aviso comunitario con informacion descriptiva, foto, ubicacion y datos de contacto.

**Precondiciones:** El usuario debe haber iniciado sesion. Si el aviso incluye foto, la app debe tener acceso a la galeria. La base local debe estar disponible para registrar el aviso incluso sin conexion.

**Disparador:** El usuario selecciona "Reportar mascota" desde la navegacion principal.

**Flujo principal:**
1. El usuario accede a la pantalla de creacion desde la barra de navegacion o el banner principal.
2. El sistema muestra el formulario de creacion.
3. El usuario selecciona el tipo de aviso (Perdido / Encontrado).
4. El usuario completa los datos: nombre, especie, raza, color, descripcion, barrio y telefono de contacto.
5. El usuario adjunta una foto desde la galeria.
6. El sistema valida que los campos obligatorios esten completos y con formato correcto.
7. El usuario confirma la publicacion.
8. El sistema guarda el aviso localmente (Room) y lo sincroniza con Firestore.
9. La app muestra confirmacion y regresa al feed.

**Flujos alternativos:**
1. Si faltan datos obligatorios, el sistema informa los errores sin perder la informacion cargada.
2. Si no hay conexion, el aviso se guarda localmente con `pendingSync = true` y se sincroniza al reconectar.
3. Si el usuario abandona el formulario, no se registra ningun aviso.

**Postcondiciones:** El aviso queda publicado y visible en el feed de otros usuarios.

**Criterios de aceptacion:**
- Dado un usuario autenticado que completa el formulario correctamente, cuando confirma, el sistema debe crear el aviso y mostrarlo en el feed.
- Dado un formulario invalido, cuando el usuario intenta publicar, el sistema debe mostrar errores claros sin perder la informacion ya cargada.
- Dado un usuario sin conexion, cuando publica un aviso, el sistema debe guardarlo localmente y sincronizarlo automaticamente al recuperar la conexion.

---

### CU-02: Consultar feed de avisos

**Actor principal:** Usuario autenticado.  
**Objetivo:** Visualizar un listado de avisos activos filtrados por tipo de animal, tipo de aviso y radio de cercania, tomando la ubicacion actual del dispositivo cuando hay permiso disponible.

**Precondiciones:** El usuario debe tener sesion activa.

**Disparador:** El usuario ingresa a la app luego del Splash / Login.

**Flujo principal:**
1. El usuario ingresa a la pantalla de avisos.
2. El sistema carga y muestra los avisos activos en formato cards con foto, tipo, nombre y zona.
3. El usuario puede modificar los filtros de tipo de aviso, especie y radio en km.
4. Si el usuario otorgo permiso de ubicacion, el sistema usa GPS real para aplicar el radio.
5. El usuario selecciona un aviso para ver su detalle.

**Flujos alternativos:**
1. Si no hay conexion, el sistema muestra los avisos cacheados en Room.
2. Si no hay avisos que cumplan los filtros, el sistema muestra un estado vacio.
3. Si el permiso de ubicacion fue denegado, el sistema hace fallback a la ubicacion guardada en el perfil y, de no existir, a una referencia por defecto.

**Postcondiciones:** El usuario visualiza avisos activos, puede filtrar el feed y navegar al detalle.

**Criterios de aceptacion:**
- Dado un usuario autenticado, cuando ingresa al feed, el sistema debe mostrar los avisos activos disponibles.
- Dado un usuario sin conexion, cuando ingresa al feed, el sistema debe mostrar los avisos cacheados localmente.
- Dado un filtro aplicado, cuando el usuario lo modifica, el feed debe actualizarse mostrando solo los avisos que cumplen el criterio.
- Dado permiso de ubicacion concedido, cuando el usuario aplica un radio, el sistema debe calcular la cercania respecto de la ubicacion actual del dispositivo.

---

### CU-03: Ver detalle de un aviso y contactar al duenio

**Actor principal:** Usuario autenticado.  
**Objetivo:** Permitir que el usuario visualice la informacion completa de un aviso y contacte al duenio.

**Precondiciones:** El usuario tiene sesion activa. El aviso existe y esta activo. Fue seleccionado desde el feed o el mapa.

**Disparador:** El usuario toca una card del feed o un pin del mapa.

**Flujo principal:**
1. El usuario toca un aviso.
2. El sistema navega a la pantalla de detalle.
3. El sistema muestra foto, nombre, especie, raza, color, descripcion, zona y datos de contacto.
4. El usuario toca "Contactar por WhatsApp".
5. El sistema abre WhatsApp con un mensaje predefinido dirigido al numero de contacto.

**Flujos alternativos:**
1. Si el aviso no tiene numero de contacto, el boton aparece deshabilitado.
2. Si el usuario es el duenio del aviso, el sistema muestra ademas las opciones de editar, marcar como resuelto y eliminar.
3. Si el aviso fue eliminado entre que el usuario cargo el feed y accedio al detalle, el sistema muestra un error y permite volver.

**Postcondiciones:** El usuario visualizo el aviso completo. Si inicio contacto, WhatsApp se abre con el mensaje preparado.

**Criterios de aceptacion:**
- Dado un aviso activo, cuando el usuario lo selecciona, el sistema debe mostrar todos los campos disponibles con imagen.
- Dado un aviso con telefono de contacto, cuando el usuario toca "Contactar", el sistema debe abrir WhatsApp con numero y mensaje predefinido.
- Dado que el usuario es el duenio del aviso, cuando accede al detalle, el sistema debe mostrar las opciones de gestion.

---

### CU-04: Registrarse e iniciar sesion

**Actor principal:** Usuario no autenticado.  
**Objetivo:** Crear una cuenta o iniciar sesion para acceder a las funcionalidades de la app.

**Precondiciones:** La app debe tener conexion a internet. El usuario no debe tener sesion activa.

**Disparador:** El usuario abre la app por primera vez luego del onboarding, o fue redirigido al login por cerrar sesion.

**Flujo principal - Registro:**
1. El usuario selecciona "Crear cuenta".
2. El sistema muestra el formulario con nombre, email y contrasena.
3. El usuario completa los campos y confirma.
4. El sistema valida el formato del email y la contrasena.
5. El sistema crea la cuenta en Firebase Authentication y persiste el perfil localmente.
6. El sistema redirige al usuario al feed.

**Flujo principal - Login:**
1. El usuario ingresa email y contrasena.
2. El sistema valida las credenciales contra Firebase Authentication.
3. El sistema hidrata el perfil del usuario en la base local.
4. El sistema redirige al usuario al feed.

**Flujos alternativos:**
1. Si el email ya esta registrado al crear cuenta, el sistema informa el error sin borrar los campos.
2. Si las credenciales son incorrectas, el sistema muestra un mensaje de error.
3. Si el usuario olvida su contrasena, puede solicitar un email de recuperacion.
4. Si no hay conexion, el sistema informa que no es posible autenticarse.

**Postcondiciones:** El usuario queda autenticado con sesion activa y es redirigido al feed.

**Criterios de aceptacion:**
- Dado credenciales validas, cuando el usuario inicia sesion, el sistema debe autenticarlo y redirigirlo al feed.
- Dado un email no registrado al hacer login, el sistema debe mostrar un error sin cerrar la pantalla.
- Dado un usuario que completa el registro correctamente, el sistema debe crear su cuenta y llevarlo al feed sin pasos adicionales.
- Dado un usuario que solicita recuperar contrasena con un email registrado, el sistema debe enviar el email e informarlo.

---

## Setup del proyecto

### 1. Clonar

```bash
git clone https://github.com/desarrolloAplicaciones1/patitasPerdidasApp.git
cd patitasPerdidasApp
```

### 2. Configurar Firebase

1. Crear un proyecto en [Firebase Console](https://console.firebase.google.com/).
2. Registrar la app Android con el package `com.uade.huellitas`.
3. Descargar `google-services.json` y copiarlo en `app/`.
4. Habilitar **Authentication -> Email/Password**.
5. Crear la coleccion `alerts` en **Firestore**.
6. Crear un bucket en **Firebase Storage**.

### 3. Abrir en Android Studio

Abrir la carpeta raiz. Android Studio sincroniza Gradle automaticamente.

> Si ejecutas Gradle por terminal y falla por `JAVA_HOME`, apunta la variable al JBR embebido de Android Studio, por ejemplo: `C:\Program Files\Android\Android Studio\jbr`.

### 4. Ejecutar

1. Conectar un dispositivo o iniciar un emulador con API 24 o superior.
2. Ejecutar la app desde Android Studio o con `./gradlew assembleDebug`.
3. Iniciar sesion o registrarse.
4. Aceptar permisos de galeria y ubicacion cuando la app los solicite.

### 5. Probar la geolocalizacion (HU-21)

- En emulador Android Studio: **Extended Controls -> Location**.
- Cargar una latitud/longitud manual o elegir un punto del mapa.
- Volver a `Home` o `Mapa` para re-evaluar el radio de alertas.
- Si el permiso de ubicacion esta denegado, la app hace fallback a la ubicacion guardada en el perfil y, en ultima instancia, a una ubicacion por defecto.

---

## Validacion rapida

Comandos utiles:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew installDebug
```

Puntos a validar manualmente:

- Login, registro y recuperacion de contrasena.
- Creacion, edicion, resolucion y borrado de alertas.
- Feed con filtros por especie, tipo y radio.
- Mapa con pines y radio basado en ubicacion actual.
- Perfil, mis reportes, foto de perfil y edicion de perfil.

---

## QA manual detallado

### Precondiciones recomendadas

Antes de ejecutar las pruebas manuales:

- Tener `google-services.json` configurado en `app/`.
- Tener habilitado `Authentication -> Email/Password` en Firebase.
- Tener Firestore creado con permisos de lectura/escritura para usuarios autenticados.
- Tener Firebase Storage habilitado con reglas compatibles para upload.
- Tener `MAPS_API_KEY` configurada en `local.properties`.
- Tener un emulador o dispositivo con GPS disponible.
- Si se quiere probar onboarding desde cero, limpiar datos de la app.

### Flujos a chequear

| Flujo | Que hacer | Resultado esperado | Red flags / posible bug |
|---|---|---|---|
| Primer arranque / onboarding | Abrir la app con datos limpios. Avanzar onboarding y cerrarla. Reabrir. | El onboarding aparece solo la primera vez. Luego va a login o splash normal. | El onboarding vuelve a mostrarse siempre o nunca aparece. |
| Registro | Crear un usuario nuevo con email y contrasena validos. | La cuenta se crea en Firebase Auth y la app navega al feed. | Error `This operation is not allowed`, loop infinito, usuario no aparece en Firebase. |
| Login | Iniciar sesion con un usuario existente. | Ingresa al feed y el nombre del usuario queda visible en perfil/home. | Credenciales validas pero no navega, nombre vacio, perfil sin hidratar. |
| Recuperar contrasena | Desde login, tocar `Olvidaste tu contrasena?` con un email valido. | La app muestra confirmacion y Firebase envia el mail. | No hay feedback, error silencioso, Firebase no registra el envio. |
| Logout | Cerrar sesion desde perfil. | La app vuelve al login y no deja acceder a pantallas autenticadas. | Sigue entrando al feed sin loguearse o conserva estado inconsistente. |
| Home sin filtros | Entrar al feed sin aplicar filtros. | Muestra avisos activos disponibles cerca de la ubicacion de referencia. | Feed vacio con documentos existentes y ubicacion correcta. |
| Busqueda por nombre | Escribir parte del nombre de una mascota en el buscador. | El listado se filtra en tiempo real sin romper otros filtros. | No cambia el listado, no ignora mayusculas/acentos, queda trabado. |
| Filtro por especie / tipo | Aplicar filtros de perro/gato y perdido/encontrado. | Solo aparecen avisos que cumplan el criterio. | Mezcla avisos fuera del filtro o limpiar filtros no restaura la lista. |
| Filtro por radio GPS | Cambiar el radio de alertas en Home o Perfil. | El feed se recalcula en base a la ubicacion actual o fallback configurado. | No cambia nada, muestra resultados lejanos, o vacia el feed incorrectamente. |
| Home vacio | Forzar una combinacion sin resultados. | Se muestra estado vacio claro y opcion para limpiar filtros. | Pantalla en blanco, spinner infinito, mensaje ambiguo. |
| Abrir detalle | Tocar una alerta desde Home o Mapa. | Navega al detalle con foto, especie, raza, color, zona, contacto y estado. | Pantalla error, datos faltantes, campos mezclados. |
| Contactar por WhatsApp | Abrir un aviso con telefono y tocar `Contactar por WhatsApp`. | Abre intent a WhatsApp o navegador con numero y mensaje prearmado. | Boton activo sin telefono, URL invalida, crash. |
| Crear alerta sin foto | Publicar una alerta completando campos obligatorios sin imagen. | Se guarda, aparece en Firestore, y luego en feed/reportes segun ubicacion/filtros. | Spinner infinito, no aparece en Firestore, error silencioso. |
| Crear alerta con foto | Repetir publicacion pero adjuntando imagen. | Ademas del documento en Firestore, la foto sube a Firebase Storage y el detalle la muestra. | Documento sin imagen, upload fallido sin feedback, URL rota. |
| Crear alerta con ubicacion | Crear aviso con barrio/zona. | La direccion se geocodifica razonablemente y cae cerca de la ubicacion esperada. | Barrio ambiguo resuelto en otro pais o coordenadas absurdas. |
| Editar alerta propia | Desde un aviso propio, editar nombre, raza, color, tamano, collar, zona, telefono y descripcion. | Cambios persistidos local y remotamente; el detalle refleja los nuevos valores. | Solo se actualiza parte del formulario, no sincroniza, o rompe el detalle. |
| Resolver alerta | Marcar una alerta propia como resuelta. | Desaparece del Home, sigue visible en `Mis reportes`/`Mis avisos` como historial resuelto. | Sigue visible en Home, se puede resolver infinitamente, no cambia badge/estado. |
| Eliminar alerta | Eliminar una alerta propia. | Se borra del detalle, del feed y de Firestore. | Sigue apareciendo localmente o falla sin feedback. |
| Mis reportes / Mis avisos | Entrar a perfil y a la pantalla dedicada. Cambiar entre `Activos`, `Resueltos`, `Todos`. | Los filtros muestran subconjuntos correctos y permiten abrir el detalle. | Mezcla estados, no actualiza al resolver, contador visual incorrecto. |
| Editar perfil basico | Cambiar nombre, telefono y ubicacion. Guardar. | Se persiste en Room, Firestore y se refleja en el perfil. | El cambio desaparece al relanzar, no impacta en Firebase o queda inconsistente. |
| Cambiar foto de perfil | Elegir imagen desde galeria y guardar perfil. | La imagen se sube a Storage, se guarda URL, y aparece en perfil. | Upload sin feedback, avatar no cambia, URL invalida. |
| Cambiar contrasena | Editar perfil y completar nueva contrasena + confirmacion. | Si cumple validaciones, Firebase actualiza la contrasena y la app informa exito. | Acepta contrasenas invalidas, no confirma mismatch, falla silenciosamente. |
| Tema del sistema | Activar `Seguir tema del sistema`, cambiar el modo del dispositivo. | La app acompana el tema del sistema automaticamente. | No cambia al cambiar el sistema o queda clavada en un tema. |
| Tema manual | Desactivar `Seguir tema del sistema` y usar `Tema oscuro`. | La app respeta el toggle manual. | El switch no hace efecto o queda bloqueado cuando no deberia. |
| Modo offline lectura | Abrir Home online, luego cortar conexion y volver al feed. | La app sigue mostrando cache local de Room. | Feed vacio sin motivo o crash al no tener red. |
| Offline al crear aviso | Crear un aviso sin conexion. | Debe guardarse localmente y mostrar que quedo pendiente de sincronizacion o que no se pudo subir. | Se pierde el aviso, no hay feedback o queda cargando indefinidamente. |
| Sync al reconectar | Volver a tener red luego de trabajar offline. | Los avisos pendientes deberian intentar sincronizarse y quedar consistentes con Firestore. | El dato queda solo local permanentemente o se duplica. |
| Mapa con permiso | Abrir mapa aceptando permiso de ubicacion. | Carga tiles reales, centra cerca del usuario y muestra avisos/pins validos. | Fondo gris, sin pines, ANR o centro en ubicacion incoherente. |
| Mapa sin permiso | Denegar permiso y volver a entrar. | Usa fallback del perfil o default sin romper la pantalla. | Pantalla vacia, crash, loop pidiendo permiso. |
| Cambio de ubicacion GPS | Simular otra ubicacion en el emulador y volver a Home/Mapa. | Recalcula cercania y cambia el conjunto de avisos visibles. | Mantiene resultados viejos o ignora el GPS nuevo. |

### Casos de error que conviene forzar

- Intentar login con password incorrecta.
- Intentar registro con email ya usado.
- Crear aviso con campos obligatorios vacios.
- Editar perfil con contrasenas distintas.
- Publicar alerta sin permisos de red o con Firestore/Storage mal configurados.
- Denegar ubicacion y verificar fallback.
- Subir una imagen grande o cambiar avatar con conexion lenta.

### Sistemas externos a observar durante QA

Mientras se prueban los flujos, conviene mirar estos sistemas:

- Firebase Authentication:
  - alta de usuarios
  - login
  - password reset
- Firestore:
  - coleccion `alerts`
  - coleccion `users`
  - cambios al editar, resolver y eliminar
- Firebase Storage:
  - uploads de fotos de alertas
  - uploads de avatares
- Google Maps / API Key:
  - render del mapa
  - markers
- GPS / emulador:
  - ubicacion actual
  - cambios de radio y recenter del mapa

### Criterio practico para dar una funcionalidad por sana

Una funcionalidad deberia considerarse estable si:

- funciona en UI sin errores visibles
- persiste correctamente localmente
- sincroniza bien con Firebase cuando corresponde
- sobrevive a cerrar y reabrir la app
- no deja estados inconsistentes entre Home, Detalle, Perfil y Firestore
- tiene feedback claro cuando algo falla

---

*TP Integrador - Desarrollo de Aplicaciones I - UADE - 2026*
