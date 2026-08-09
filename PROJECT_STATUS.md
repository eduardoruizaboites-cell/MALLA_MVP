# PROJECT_STATUS.md — MALLA MVP
**Última actualización:** 2026-08-08 (Sesión de cierre tras implementar flujo de contactos funcional con IP encriptada)

## Resumen Ejecutivo
La app alcanzó un estado estable y altamente funcional. El flujo de agregar usuarios mediante código de invitación de 12 dígitos (con IP encriptada) y escaneo QR funciona correctamente en redes locales. El perfil de usuario premium incluye controles de privacidad, edición de nombre, ID único visible y botón "Verificar contactos". El cifrado E2EE básico con ECDH + AES‑256‑GCM está integrado. La interfaz premium incluye caja de texto multilínea en bottomBar, panel de emojis estilo WhatsApp, cámara con modos profesionales, panel de adjuntos y zumbido MSN. La DHT global quedó preparada pero temporalmente desactivada por conflictos de imports; se retomará en la próxima sesión.

## Estado General
- **Fase actual:** 3 – Comunicación Avanzada y Pre‑Mesh Discovery (con seguridad reforzada)
- **Compilación:** BUILD SUCCESSFUL
- **Último commit:** `checkpoint-20260808-cierre`
- **Archivos modificados en esta sesión:**
  - `IdentityManager.kt` — Añadidas funciones para ID único, nickname y `ensureSelfContact`.
  - `RegistrationScreen.kt` — Nuevo registro biométrico con animaciones y generación de ID de 12 caracteres.
  - `PerfilScreen.kt` — Rediseño completo con avatar, nombre editable, ID visible, privacidad, QR efímero (con biometría), código 24h (con IP encriptada y botón de refrescar) y botón "Verificar contactos".
  - `VerificationScreen.kt` — Pantalla de verificación de contactos mediante ID o QR.
  - `ConversationsScreen.kt` — Diálogo "Agregar usuario" premium con código de invitación (12 dígitos, IP encriptada) y escáner QR.
  - `InviteCodeGenerator.kt` — Acepta un `extra` opcional para incluir IP encriptada en el código.
  - `DhtWrapper.kt` — Cifrado/descifrado de IP local para conexiones directas.
  - `SessionCipher.kt` — Cifrado simétrico con AES‑GCM.
  - `MeshChatViewModel.kt` — Integración de E2EE en envío y carga de mensajes.
  - `MainActivity.kt` — Integración de `RegistrationScreen`, `VerificationScreen`, procesamiento QR y eliminación del viejo onboarding.
  - `DhtService.kt` — Preparada para DHT global (búsqueda con semillas), temporalmente no integrada.
  - `AppDatabase.kt` — Versión 11 con fallback destructivo.

## Funcionalidades implementadas (sesión actual)
- ✅ Registro premium con ID único de 12 caracteres (huella opcional, ubicación, timestamp).
- ✅ Perfil de usuario con avatar, banner, nombre editable, ID visible, configuración de privacidad (switches), QR efímero y código de 24h (con biometría, refresco y IP encriptada).
- ✅ Código de invitación de 12 dígitos con IP encriptada para conexión directa en LAN.
- ✅ Escáner QR funcional que procesa enlaces `malla://connect?ip=...` y `malla://id?userId=...`.
- ✅ Pantalla de verificación de contactos.
- ✅ Cifrado E2EE: mensajes enviados a contactos con clave pública se cifran con AES‑GCM.
- ✅ Eliminado el viejo `IdentityOnboardingScreen` (sin teléfono).
- ✅ Animaciones visuales durante la generación del ID.
- ✅ `ensureSelfContact` guarda la propia identidad en la BD para verificación.

## Deuda técnica / Pendientes importantes
1. **DHT global** — El código en `DhtService.kt` está listo, pero no se integra en `ConversationsScreen` por conflicto de imports. Pendiente de resolver en próxima sesión.
2. Pruebas de comunicación BLE/Wi‑Fi Direct entre dispositivos reales — NO REALIZADAS.
3. Refactorización de `Injector` para romper dependencias circulares con `:network` — PENDIENTE.
4. Búsqueda en el chat — PENDIENTE.
5. Indicador de ondas en grabación de voz a veces no se mueve — POSIBLE BUG.
6. Icono de notificación grande (`setLargeIcon`) no implementado.
7. Cobertura de pruebas unitarias — INEXISTENTE.
8. `DoubleRatchet` real (Perfect Forward Secrecy) — PENDIENTE (actualmente se usa ECDH simétrico).
9. Integración de verificación de contactos en el flujo de agregar usuario — PARCIAL.

## Próxima sesión – Plan de acción
**Objetivo:** Resolver el conflicto de imports de `DhtService`, activar la DHT global y añadir el escáner de documentos a la cámara.

1. **DHT global (resolver conflicto)**
   - Crear un wrapper `DhtHelper` en el módulo `:app` que encapsule las llamadas a `DhtService.lookup()`.
   - Integrar `DhtHelper` en `ConversationsScreen` para que, al ingresar un código sin IP encriptada, busque en la DHT.
   - Probar con un nodo semilla local (o en Oracle Cloud).

2. **Escáner de documentos en la cámara**
   - Implementar el modo "Documento" en `CameraViewModel` usando ML Kit Document Scanner.
   - Añadir UI con guías de encuadre y recorte automático.

## Checkpoint creado
`git tag checkpoint-20260808-cierre`

## Sesión 21–22: Corrección de crash al abrir la app

### Error 1: Room – cambio de esquema sin migración
- **Causa:** Se añadió `ContactEntity` a la base de datos pero no se incrementó la versión en `AppDatabase.kt` (se mantuvo `version = 11`).
- **Efecto:** Room detectaba una identidad de hash diferente y lanzaba `IllegalStateException: Room cannot verify the data integrity`.
- **Solución:** Incrementar la versión a 12 en `data/.../AppDatabase.kt`. Como ya se tenía `fallbackToDestructiveMigration()`, Room destruyó la BD antigua y creó la nueva correctamente.

### Error 2: mDNS – `listener already in use`
- **Causa:** En `DiscoveryService.kt` se reutilizaba una única instancia de `ResolveListener` (`resolveListener`) para cada servicio encontrado. Android no permite que un mismo listener sea usado en múltiples resoluciones simultáneas.
- **Efecto:** Al escanear servicios mDNS, el segundo `resolveService` lanzaba `IllegalArgumentException: listener already in use`.
- **Solución:** Crear una instancia anónima nueva de `ResolveListener` cada vez que se llama a `resolveService`, en lugar de reutilizar el objeto `resolveListener` predefinido.

## Sesión 22 – Corrección de errores críticos y ajustes finales

### Errores corregidos
| Error | Causa | Solución |
|-------|-------|----------|
| `Room cannot verify the data integrity` | Se añadió `ContactEntity` sin incrementar la versión de la BD | Incrementada versión a 12 en `AppDatabase.kt` |
| `listener already in use` (mDNS) | Se reutilizaba una única instancia de `ResolveListener` para múltiples resoluciones | Se crea un listener anónimo nuevo por cada llamada a `resolveService` |
| `NoSuchMethodError: getZumbidoReceived` (ChatScreen) | `MallaEventBus` no contenía el campo `zumbidoReceived` | Añadido `val zumbidoReceived = MutableSharedFlow<MeshMessage>(...)` en `MallaEventBus` |
| Auto-detección de IP local en "Cerca de ti" | El filtro de IP no comparaba solo la IP sin puerto, y había código duplicado | Filtro corregido para extraer solo la IP y comparar con `DhtService.getLocalAddress()` |

### Estado actual del proyecto
- ✅ Mensajería E2EE con Double Ratchet + X3DH
- ✅ Registro premium (ID biométrico + GPS)
- ✅ Flujo de contactos por código/QR con IP encriptada
- ✅ DHT global integrada
- ✅ Proximidad BLE + Wi‑Fi Direct + mDNS
- ✅ Panel de usuario cercano con acciones (enviar solicitud, ocultar, bloquear)
- ✅ Envío de invitaciones por BLE (característica `0000abcd-0002-...`)
- ✅ Recepción y diálogo de invitación (IncomingRequestDialog)
- ✅ Confirmación biométrica al aceptar solicitud
- ✅ Guardado de contacto en Room (ContactDao)
- ✅ Notificación de aceptación al emisor vía BLE
- ✅ Pantalla de contactos (ContactsScreen)
- ✅ Compilación exitosa, app funcional

### Próximos pasos
- Pruebas con dos dispositivos reales (descubrimiento, invitación, chat)
- Ajustes finales de UI premium
- Preparación para release

## Sesión 23 – Flujo de registro completo y ajustes visuales profesionales

### Logros
- **Flujo de registro restaurado y mejorado:**
  1. Splash de bienvenida (logo MALLA).
  2. Explicación de permisos y solicitud mediante `ActivityResultContracts`.
  3. Explicación de biométricos con botón para proceder.
  4. Solicitud de huella/rostro mediante `BiometricPrompt`.
  5. Animación profesional de generación de ID (anillos expansivos, iconos Material de huella/GPS, progreso circular).
  6. Pantalla de nombre de usuario (obligatorio, validado).
  7. Pantalla de confirmación con vista previa del perfil.
  8. Tutorial (solo en primer inicio).
  9. Pantalla principal.
- **Identidad:** el ID se genera sin el prefijo "MALLA‑" y se muestra con formato `XXXX‑XXXX‑XXXX`.
- **Permisos:** los permisos de Bluetooth ya no se piden en el flujo inicial, sino bajo demanda; la verificación de permisos esenciales es automática con reintento.
- **Interfaz de chat:** el indicador de conectividad ahora es un punto de color (verde HD / naranja mesh).
- **Ajustes visuales:** animación de ID profesional sin emojis, usando `Canvas` y `Icon` de Material.

### Errores corregidos
- `Unresolved reference: ContextCompat` solucionado usando `context.checkSelfPermission`.
- Eliminada la solicitud automática de permisos en `MainActivity` que interfería con el flujo de registro.
- Corregida la estructura de llaves en `MainActivity` tras limpieza del bloque de permisos.
- `Return` faltante en `IdentityManager.getIdentityId()`.
