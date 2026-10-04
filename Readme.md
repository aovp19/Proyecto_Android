# ***Giraffe Chat***

Aplicación de chat en tiempo real para Android, desarrollada con Kotlin y Firebase.

Proyecto de **ICC-451-T Desarrollo de Aplicaciones Móviles** Pontificia Universidad Católica Madre y Maestra (PUCMM). 
Profesor: Freddy Peña.

## Integrantes

- Almy Ventura - 10153712
- María José Cruz - 10154963

## Funcionalidades

- **Registro e inicio de sesión** con correo y contraseña (Firebase Authentication), con validación de campos, formato de correo y contraseña, y mensajes de error en español.
- **Sesión persistente:** al cerrar y volver a abrir la app se mantiene la sesión, salvo que se cierre manualmente.
- **Mensajería en tiempo real** (Cloud Firestore): historial ordenado, hora de cada mensaje, etiquetas de día ("Hoy", "Ayer") y bloqueo de mensajes vacíos.
- **Envío de imágenes** (Firebase Storage): selección desde la galería, compresión, corrección de rotación y vista previa inmediata mientras se sube.
- **Notificaciones push** (Firebase Cloud Messaging) cuando llega un mensaje nuevo, enviadas por una Cloud Function.
- **Flujo de pantallas:** Login -> Registro -> Lista de usuarios -> Chat.

### Mejoras opcionales implementadas

- Avatar de perfil (emoji a elección del usuario).
- Búsqueda de usuarios por nombre en la lista.
- Tema claro/oscuro (sigue la configuración del dispositivo).

## Tecnologías

- Kotlin, XML Views, Activities con Intents, RecyclerView
- Arquitectura MVVM (ViewModel, LiveData, StateFlow, corrutinas)
- Firebase: Authentication, Cloud Firestore, Storage, Cloud Messaging
- Cloud Functions para Firebase 
- Coil para cargar imágenes
- Git y GitHub

## Estructura del proyecto

```
app/src/main/java/edu/pucmm/proyecto_android/
├── model/        Mensaje, User
├── view/         Activities: Login, Registro, Main (lista de usuarios), Chat
├── adapters/     MensajeAdapter, UsuarioAdapter (RecyclerView)
├── viewmodel/    AuthViewModel, ConversacionesViewModel, ChatViewModel y sus estados
├── repository/   AuthRepository, ChatRepository, UsuarioRepository (acceso a Firebase)
├── service/      FcmService (recepción de notificaciones)
└── util/         ImagenUtil, InsetsUtil
functions/        Cloud Function notificarMensaje
```

Las pantallas (View) solo muestran datos y reciben eventos, la lógica está en los ViewModel y el acceso a Firebase está en los Repository.

## Cómo ejecutar

**Requisitos:** Android Studio reciente (el proyecto usa `compileSdk 37`) y un dispositivo o emulador con Android 7.0 (API 24) o superior y Google Play Services (necesario para las notificaciones).

1. Clonar el repositorio.
2. Colocar el archivo `google-services.json` dentro de la carpeta `app/`. Este archivo contiene la configuración del proyecto de Firebase y **no está incluido en el repositorio**; se entrega aparte.
3. En Android Studio: **File > Sync Project with Gradle Files** y luego **Run**.

### Cómo probar

1. Registrar dos cuentas distintas en dos dispositivos (o un dispositivo y un emulador).
2. Desde la lista de usuarios, abrir el chat con la otra cuenta y enviar mensajes e imágenes.
3. Para ver las notificaciones, cerrar la app en un dispositivo y enviarle un mensaje desde el otro. En Android 13 o superior hay que aceptar el permiso de notificaciones.

