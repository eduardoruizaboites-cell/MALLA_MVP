# PROJECT_STATUS.md — MALLA MVP
**Última actualización:** 2026-08-07 (Sesión de cierre tras implementar flujo de contactos, perfil premium y E2EE)

## Resumen Ejecutivo
La app alcanzó un estado altamente funcional y seguro. Se implementó un sistema de registro biométrico sin dependencia de número telefónico, un perfil de usuario premium con controles de privacidad y compartir identidad (QR efímero y código de 24h con IP encriptada). El flujo de agregar usuarios ahora funciona mediante código de invitación (12 dígitos) y escaneo QR. Se activó el cifrado de extremo a extremo (E2EE) básico con ECDH + AES‑256‑GCM. La interfaz incluye caja de texto multilínea en bottomBar, panel de emojis estilo WhatsApp, cámara con modos profesionales, panel de adjuntos premium y zumbido MSN. No se introdujeron regresiones.

## Estado General
- **Fase actual:** 3 – Comunicación Avanzada y Pre‑Mesh Discovery (con seguridad reforzada)
- **Compilación:** BUILD SUCCESSFUL
- **Último commit:** `checkpoint-20260807-flujo-contactos`
- **Archivos modificados en esta sesión:**
  - `IdentityManager.kt` — Añadidas funciones para ID único, nickname y `ensureSelfContact`.
  - `RegistrationScreen.kt` — Nuevo registro biométrico con animaciones y generación de ID de 12 caracteres.
  - `PerfilScreen.kt` — Rediseño completo con avatar, nombre editable, ID visible, privacidad, QR efímero (con biometría), código 24h (con IP encriptada y botón de refrescar) y botón "Verificar contactos".
  - `VerificationScreen.kt` — Pantalla de verificación de contactos mediante ID o QR.
  - `ConversationsScreen.kt` — Diálogo "Agregar usuario" premium con código de invitación y escáner QR, eliminando opciones obsoletas.
  - `InviteCodeGenerator.kt` — Acepta un `extra` opcional para incluir IP encriptada en el código.
  - `DhtWrapper.kt` (nuevo) — Cifrado/descifrado de IP local para conexiones directas.
  - `SessionCipher.kt` (nuevo) — Cifrado simétrico con AES‑GCM.
  - `MeshChatViewModel.kt` — Integración de E2EE en envío y carga de mensajes.
  - `MainActivity.kt` — Integración de `RegistrationScreen`, `VerificationScreen`, procesamiento QR y eliminación del viejo onboarding.
  - `AppDatabase.kt` — Versión 11 con fallback destructivo.

## Funcionalidades implementadas (sesión actual)
- ✅ Registro premium con ID único de 12 caracteres (huella opcional, ubicación, timestamp).
- ✅ Perfil de usuario con avatar, banner, nombre editable, ID visible, configuración de privacidad (switches), QR efímero y código de 24h (con biometría y refresco).
- ✅ Código de invitación de 12 dígitos con IP encriptada para conexión directa.
- ✅ Escáner QR funcional que procesa enlaces `malla://connect?ip=...` y `malla://id?userId=...`.
- ✅ Pantalla de verificación de contactos.
- ✅ Cifrado E2EE: mensajes enviados a contactos con clave pública se cifran con AES‑GCM.
- ✅ Eliminado el viejo `IdentityOnboardingScreen` (sin teléfono).
- ✅ Animaciones visuales durante la generación del ID.
- ✅ `ensureSelfContact` guarda la propia identidad en la BD para verificación.

## Estructura de módulos (recordatorio)
(app → core, data, crypto, events, identity, media, network, transport, camera, emoji)
(data → core, room)
(crypto → core)
(transport → core, network)
(network → core, data)

## Deuda técnica pendiente
1. Pruebas de comunicación BLE/Wi‑Fi Direct entre dispositivos reales — NO REALIZADAS.
2. Refactorización de `Injector` para romper dependencias circulares con `:network` — PENDIENTE.
3. Búsqueda en el chat — PENDIENTE.
4. Indicador de ondas en grabación de voz a veces no se mueve — POSIBLE BUG.
5. Icono de notificación grande (`setLargeIcon`) no implementado.
6. Cobertura de pruebas unitarias — INEXISTENTE.
7. `DoubleRatchet` real (Perfect Forward Secrecy) — PENDIENTE (actualmente se usa ECDH simétrico).
8. Integración de verificación de contactos en el flujo de agregar usuario — PARCIAL.

## Próxima sesión – Plan de acción
**Objetivo:** Activar la DHT global para descubrimiento de contactos y añadir el escáner de documentos a la cámara.

1. **DHT global**
   - Configurar un nodo semilla público (Oracle Cloud Always Free).
   - Actualizar `DhtService` con la IP del nodo semilla real.
   - Publicar presencia (userId + IP) al iniciar la app.
   - Al agregar contacto por código/QR, buscar en la DHT y conectar directamente.

2. **Escáner de documentos en la cámara**
   - Implementar el modo "Documento" en `CameraViewModel` usando ML Kit Document Scanner.
   - Añadir UI con guías de encuadre y recorte automático.
   - Guardar el documento como PDF o imagen.

## Checkpoint creado
`git tag checkpoint-20260807-flujo-contactos`
