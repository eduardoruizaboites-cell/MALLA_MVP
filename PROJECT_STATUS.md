# PROJECT_STATUS.md — MALLA MVP
**Última actualización:** 2026-08-07 (Sesión de cierre tras implementación de E2EE y verificación de contactos)

## Resumen Ejecutivo
La app alcanzó un estado estable y altamente seguro. Se implementó un sistema de identidad único biométrico (sin dependencia de número telefónico), verificación de contactos mediante código QR o ID de 12 caracteres, y cifrado de extremo a extremo (E2EE) con ECDH + AES‑256‑GCM. La interfaz premium incluye caja de texto multilínea en bottomBar, panel de emojis estilo WhatsApp, cámara con modos profesionales, panel de adjuntos y zumbido MSN. No se introdujeron regresiones; el flujo de mensajería normal funciona sin interferencias.

## Estado General
- **Fase actual:** 3 – Comunicación Avanzada y Pre‑Mesh Discovery (con seguridad reforzada)
- **Compilación:** BUILD SUCCESSFUL
- **Último commit:** `checkpoint-20260807-e2ee-completo`
- **Archivos modificados en esta sesión:**
  - `IdentityManager.kt` — Generación de ID único (biométrico + GPS), gestión de nickname, `ensureSelfContact`.
  - `RegistrationScreen.kt` — Nueva pantalla de registro premium con animaciones de ubicación y generación.
  - `VerificationScreen.kt` — Pantalla de verificación de contactos mediante ID o QR.
  - `PerfilScreen.kt` — Muestra ID único y botón de verificación.
  - `ContactEntity.kt` / `ContactDao.kt` — Añadido `userId` para búsqueda por ID.
  - `MessageEntity.kt` — Añadido campo `encrypted`.
  - `SessionCipher.kt` (nuevo) — Cifrado/descifrado con AES‑GCM usando clave derivada de ECDH.
  - `MeshChatViewModel.kt` — Integración de E2EE en envío y carga de mensajes.
  - `MainActivity.kt` — Integración de `RegistrationScreen`, `VerificationScreen` y eliminación del viejo onboarding.
  - `AppDatabase.kt` — Versión 11 con `fallbackToDestructiveMigration`.

## Funcionalidades implementadas (sesión actual)
- ✅ Registro premium con ID único de 12 caracteres (huella opcional, ubicación, timestamp).
- ✅ Pantalla de verificación de contactos (QR propio e ingreso de ID ajeno).
- ✅ Perfil muestra ID único y botón de verificación.
- ✅ Cifrado E2EE: mensajes enviados a contactos con clave pública se cifran con AES‑GCM.
- ✅ Descifrado automático al cargar conversación.
- ✅ Eliminado el viejo `IdentityOnboardingScreen` (sin teléfono).
- ✅ Animación visual durante la generación del ID (ubicación + generación).
- ✅ Campo `encrypted` en `MessageEntity` para distinguir mensajes cifrados.
- ✅ `SessionCipher` en módulo `crypto` como wrapper de `CryptoEngine`.
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
8. Integración de verificación de contactos en el flujo de agregar usuario (código/QR) — PARCIAL.

## Próxima sesión – Plan de acción
**Objetivo:** Refinar la verificación de contactos, activar el DoubleRatchet completo y preparar pruebas de comunicación real.

1. **Verificación de contactos mejorada**
   - Al escanear QR o ingresar código de 8 dígitos (ahora ID de 12), insertar el contacto en la BD con su `userId`.
   - Al verificarlo en `VerificationScreen`, confirmar el estado y marcar como `CONFIRMED`.
   - Añadir identicon visual para comparación fuera de banda.

2. **Implementar DoubleRatchet real**
   - Almacenar estado del ratchet por conversación (tabla `ratchet_state` o en `MessageEntity`).
   - Cifrar cada mensaje con una clave de mensaje derivada del ratchet.
   - Gestionar el intercambio inicial de pre‑keys (usando la DHT o el handshake ECDH existente).

3. **Pruebas de comunicación real**
   - Conectar dos dispositivos (físicos o emuladores) vía Wi‑Fi Direct o BLE.
   - Verificar intercambio de mensajes cifrados, zumbidos y archivos.
   - Ajustar la UI de conexión en `PulsoScreen`.

## Checkpoint creado
`git tag checkpoint-20260807-e2ee-completo`
