# 💈 GUÍA OFICIAL DE INSTALACIÓN Y CONFIGURACIÓN
## BarberSite TV Monitor & Burbuja Flotante
**Versión:** 1.0.2 • **Dispositivos:** Android TV, Xiaomi Mi Box, Smart TV Skyworth/Genéricas y Celulares Android.

---

## 📌 ÍNDICE RÁPIDO
1. [Métodos de Instalación del APK](#1-métodos-de-instalación-del-apk)
   - [Método A: Conexión Inalámbrica (Send Files to TV - Recomendado)](#método-a-conexión-inalámbrica-send-files-to-tv---recomendado)
   - [Método B: Mediante Pendrive / Memoria USB](#método-b-mediante-pendrive--memoria-usb)
   - [Método C: Instalación en Celular Android](#método-c-instalación-en-celular-android)
2. [Conexión y Sincronización con Supabase](#2-conexión-y-sincronización-con-supabase)
3. [Activación de la Burbuja Flotante (Sobre YouTube/Netflix)](#3-activación-de-la-burbuja-flotante-sobre-youtubenetflix)
4. [Guía de Emergencia por ADB (Para TVs con Menú de Permisos Bloqueado)](#4-guía-de-emergencia-por-adb-por-si-acaso)
5. [Preguntas Frecuentes y Solución de Problemas](#5-preguntas-frecuentes-y-solución-de-problemas)

---

## 1. MÉTODOS DE INSTALACIÓN DEL APK

### Método A: Conexión Inalámbrica («Send Files to TV» - Recomendado)
*Ideal para televisores montados en pared donde es incómodo conectar una memoria USB.*

1. **En la Smart TV / TV Box**:
   - Abre la **Google Play Store**.
   - Busca e instala: **«Send Files to TV»** y **«File Commander»** (o *X-plore*).
   - Abre *Send Files to TV* en la TV y selecciona **RECEIVE (Recibir)**.
2. **En tu Celular**:
   - Conéctate al **mismo Wi-Fi** que la TV.
   - Instala y abre **«Send Files to TV»** en tu celular.
   - Toca **SEND (Enviar)**, busca el archivo `app-debug.apk` y selecciona tu TV en la lista.
3. **Instalación**:
   - Una vez transferido, tócalo directamente en *Send Files to TV* o abre *File Commander* ➔ Carpeta *Download* ➔ Selecciona `app-debug.apk` ➔ Dale **Instalar**.

---

### Método B: Mediante Pendrive / Memoria USB
1. Copia el archivo `app-debug.apk` a tu memoria USB.
2. Conecta el USB en el puerto de la Smart TV o TV Box.
3. Abre el **Explorador de Archivos** de la TV (*Media Player*, *Archivos*, etc.).
4. Selecciona `app-debug.apk` y pulsa **OK** en el control remoto.
5. Si la TV muestra el aviso de seguridad *"Instalación de orígenes desconocidos"*:
   - Pulsa **Ajustes / Configuración** ➔ Activa el switch para el explorador ➔ Vuelve y pulsa **Instalar**.

---

### Método C: Instalación en Celular Android
1. Pasa el archivo `app-debug.apk` a tu celular (por WhatsApp, Telegram o cable).
2. Toca el archivo desde tus descargas y pulsa **Instalar**.
3. Si el sistema te lo pide, dale a *«Permitir instalar desde esta fuente»*.

---

## 2. CONEXIÓN Y SINCRONIZACIÓN CON SUPABASE

La aplicación puede funcionar en modo demostración o conectada a la base de datos central de la barbería en tiempo real.

1. Abre la aplicación **BarberSite TV** en la TV o celular.
2. Con el control remoto, ve a la esquina superior derecha y presiona el botón **«Ajustes»** (icono de engranaje).
3. Configura los datos de tu barbería:
   - **Modo Demo:** *Desactívalo* para conectar en vivo.
   - **Nombre de la Barbería:** Escribe el nombre del local.
   - **URL Servidor / Supabase:** Ingresa tu URL de Supabase (ej: `https://xxxx.supabase.co`).
   - **Supabase Anon Key:** Pega tu clave de API pública.
4. Presiona **«Guardar y Conectar»**.

> 💡 **Tip Pro para escribir rápido con el control remoto:**  
> Instala en tu celular la app **Google TV** (o *Android TV Remote*). Podrás usar el teclado táctil de tu celular para copiar y pegar la URL y la API Key en la pantalla de la TV en 1 segundo.

---

## 3. ACTIVACIÓN DE LA BURBUJA FLOTANTE (SOBRE YOUTUBE/NETFLIX)

La burbuja flotante permite que el turno actual del barbero se mantenga flotando en una esquina de la pantalla mientras la TV reproduce videos musicales de YouTube, series de Netflix, Spotify o canales de IPTV.

### Pasos para Activarla:
1. En la pantalla principal, selecciona el botón dorado: **«ACTIVAR BURBUJA FLOTANTE»**.
2. **Si es la primera vez que se activa:**  
   Se abrirá la ventana de asistencia para TV:
   - **Opción 1 («Iniciar Directo»):** Púlsalo primero. En la gran mayoría de TV Boxes y TVs genéricas, la burbuja empezará a flotar de inmediato.
   - **Opción 2 («Abrir Ajustes»):** Si la TV lo exige, te llevará a los ajustes de la app. Ve a *Permisos* (o *Acceso Especial*) ➔ Activa **«Mostrar sobre otras aplicaciones»**.
3. ¡Listo! Al abrir YouTube o cualquier app, la burbuja permanecerá flotando en pantalla en tiempo real.

---

## 4. GUÍA DE EMERGENCIA POR ADB (POR SI ACASO)

*Utiliza este método únicamente si te encuentras con una Smart TV con un sistema operativo ultra-restringido de fábrica que no muestre el menú de permisos en pantalla.*

### Paso 1: Obtener la Dirección IP de la TV
1. En la TV ve a: **Ajustes ⚙️ ➔ Red e Internet (o Wi-Fi)**.
2. Toca sobre la red Wi-Fi conectada.
3. Anota la dirección IP que aparece en pantalla (Ejemplo: `192.168.1.45`).
   *(También puedes ver la IP en grande arriba al abrir la app Send Files to TV).*

### Paso 2: Activar las Opciones de Desarrollador en la TV
1. En la TV ve a: **Ajustes ⚙️ ➔ Preferencias del dispositivo ➔ Información (Acerca de)**.
2. Baja hasta **«Número de compilación»** y presiona el botón **OK** del control **7 veces seguidas** hasta que salga el aviso: *«¡Ya eres desarrollador!»*.
3. Vuelve atrás, entra a **Opciones de desarrollador** y activa **«Depuración por USB / Depuración de red»**.

### Paso 3: Conceder el Permiso en 5 Segundos desde tu Celular
1. En tu celular (conectado al mismo Wi-Fi que la TV), descarga gratis de la Play Store la app **«Bugjaeger»** o **«Remote ADB Shell»**.
2. Abre la app, escribe la IP de la TV (ej: `192.168.1.45`) y pulsa **Connect**.
3. En la pantalla de la TV saldrá un mensaje: *«¿Permitir depuración?»* ➔ Marca la casilla y dale **Aceptar** con el control.
4. En la pestaña de consola de comandos de tu celular, escribe o pega esta línea exacta y pulsa **Enter**:
   ```bash
   appops set com.aistudio.barberturnostv.kxmpzq SYSTEM_ALERT_WINDOW allow
   ```
5. ¡Listo! El permiso queda concedido permanentemente en la TV.

---

## 5. PREGUNTAS FRECUENTES Y SOLUCIÓN DE PROBLEMAS

### ¿La pantalla de la TV se apaga sola después de unos minutos?
* **No.** La aplicación incluye protección nativa `FLAG_KEEP_SCREEN_ON` para evitar que la Smart TV entre en suspensión o protector de pantalla mientras la barbería está abierta.

### ¿Qué pasa si cambia el orden de llegada o un barbero sale a almorzar?
* La app se conecta mediante **Supabase Realtime (WebSockets)**. En cuanto el barbero marca asistencia, entra a almorzar (`en_almuerzo = true`) o se rota un turno desde la web, la TV y la burbuja flotante se actualizan de forma automática en milisegundos sin necesidad de refrescar manualmente.

### ¿Las horas de llegada salen desfasadas?
* **No.** La app utiliza el módulo oficial `TimeUtils` con zona horaria forzada **`America/La_Paz` (UTC-4)**, mostrando siempre la hora local exacta (ej: `08:30 AM` / `02:15 PM`).

### ¿Las fotos de los barberos no cargan?
* La app tiene soporte para:
  1. Enlaces públicos directos HTTPS.
  2. Fotos de perfil (`avatar_url`) de la tabla `profiles`.
  3. Respaldo automático con la selfie de asistencia diaria (`selfie_url`).
  4. Reducción inteligente de tamaño en memoria para no saturar TVs con 1 GB de RAM.

---
*Documento generado para BarberSite TV Monitor • 2026*
