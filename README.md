# Cumple-Ana

Aplicación Android para organizar actividades y generar un video resumen del cumpleaños de Ana.

## Requisitos previos

- Android Studio Ladybug (o más reciente) con el Android Gradle Plugin 8.7 y JDK 17 instalados.
- Kotlin 1.9.22 (ya configurado en el proyecto).
- Android SDK 34 y herramientas de compilación actualizadas.
- Conexión a internet para la sincronización inicial de dependencias.

## Pasos antes de ejecutar la app

1. Clona este repositorio y ábrelo en Android Studio.
2. Sincroniza Gradle cuando se solicite para descargar todas las dependencias.
3. (Opcional) Si quieres habilitar la generación de video con **ffmpeg-kit**, edita el archivo `gradle.properties` y cambia la línea:
   ```
   enableFfmpegKit=false
   ```
   por
   ```
   enableFfmpegKit=true
   ```
   Esto añadirá el repositorio de Arthenica y las dependencias nativas necesarias. Asegúrate de tener acceso a `https://maven.arthenica.com` antes de activar la opción.
4. Compila el proyecto con `./gradlew :app:assembleDebug` o ejecuta la app desde Android Studio.

Si tu ruta contiene caracteres no ASCII en Windows, este proyecto ya establece `android.overridePathCheck=true` para evitar errores al sincronizar.

## Estructura de generación de video

El módulo incluye dos implementaciones de `VideoGenerator`:

- `src/noFfmpeg/java`: implementación de reserva que muestra un mensaje cuando ffmpeg no está disponible.
- `src/withFfmpeg/java`: implementación completa basada en ffmpeg-kit que se añade solo cuando `enableFfmpegKit=true`.

Esto permite compilar y probar la aplicación incluso sin la dependencia nativa.

## Problemas conocidos

- Las tareas que requieren el SDK de Android (por ejemplo `:app:assembleDebug`) fallarán en entornos donde el SDK no esté instalado. Instala el SDK mediante Android Studio o configura la variable `ANDROID_HOME` para resolverlo.
- El proyecto usa una versión de Kotlin compatible con el compilador de Compose incluido, por lo que no es necesario aplicar banderas adicionales.

