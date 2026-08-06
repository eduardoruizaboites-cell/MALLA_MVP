# PROJECT_STATUS.md — MALLA MVP
**Última actualización:** 2026-08-02 (Sesión de cierre tras implementación de zumbido premium y caja de texto en bottomBar)

## Resumen Ejecutivo
La app alcanzó un estado estable y visualmente premium. La caja de texto ahora reside en el `bottomBar` del `Scaffold`, crece multilínea con animación fluida, y todos los íconos están centrados y tematizados. El botón de zumbido activa una experiencia completa estilo MSN (vibración, sonido personalizado, shake de pantalla y overlay animado) sin dejar burbuja en el historial. No se introdujeron regresiones; el flujo de mensajería normal funciona sin interferencias.

## Estado General
- **Fase actual:** 3 – Comunicación Avanzada y Pre‑Mesh Discovery (ampliada con UX premium)
- **Compilación:** BUILD SUCCESSFUL (243 tareas)
- **Último commit:** `ba19d9bb` (tag: `checkpoint-20260802-zumbido-premium`)
- **Archivos modificados en esta sesión:**
  - `ChatScreen.kt` — Reestructuración completa con `bottomBar`, overlay de zumbido, puente de red, vibración y sonido.
  - `ChatInputBar.kt` — Rediseño premium: crecimiento multilínea, borde neón adaptativo, háptica, iconos externos centrados, colores dinámicos del tema.
  - `MeshChatViewModel.kt` — `sendZumbido()` ahora emite evento local sin persistir burbuja.

## Funcionalidades implementadas (sesión actual)
- ✅ Caja de texto en `bottomBar` con `animateContentSize`, `heightIn(min = 48.dp)`, `maxLines = 5`.
- ✅ Borde neón con gradiente usando `drawBehind` y `LocalColorScheme`.
- ✅ Íconos de vibrar, micrófono y enviar fuera de la burbuja de texto.
- ✅ Todos los íconos (emoji, adjuntar, cámara) unificados al color primario del tema.
- ✅ Zumbido MSN:
  - Vibración patrón (100ms on, 80ms off x3).
  - Reproducción de `res/raw/zumbido.mp3`.
  - Shake de pantalla (4 ciclos, 20dp).
  - Overlay `ZumbidoOverlay` con animación de escala y fade.
  - Sin inserción de burbuja en el chat.
- ✅ Puente de red: `NetworkService.messages` redirige zumbidos remotos a `MallaEventBus`.
- ✅ Háptica sutil en botón enviar (`HapticFeedbackConstants.KEYBOARD_TAP`).
- ✅ Indicador de escritura (`ComposingBubble`) con fondo translúcido.

## Estructura de módulos (recordatorio)
(app → core, data, crypto, events, identity, media, network, transport)
(data → core, room)
(crypto → core)
(transport → core, network)
(network → core, data)

## Deuda técnica pendiente
1. Pruebas de comunicación BLE/Wi‑Fi Direct entre dispositivos reales — NO REALIZADAS.
2. Refactorización de `Injector` para romper dependencias circulares con `:network` — PENDIENTE.
3. Búsqueda en el chat — PENDIENTE (implementada antes, se puede re‑aplicar).
4. Indicador de ondas en grabación de voz a veces no se mueve — POSIBLE BUG.
5. Icono de notificación grande (`setLargeIcon`) no implementado.
6. Cobertura de pruebas unitarias — INEXISTENTE.
7. `R.raw.zumbido` debe existir; si se elimina, el zumbido no sonará.

## Próxima sesión – Plan de acción
**Objetivo:** Implementar los iconos de cámara, adjuntar y emojis/GIFs como módulos independientes, sin afectar la app compilada.

1. **Módulo de Cámara Premium (`:camera`)**
   - Crear un módulo `:camera` que encapsule toda la lógica de captura.
   - Detectar la resolución máxima soportada por el hardware.
   - Aplicar mejoras automáticas (HDR, balance de blancos, reducción de ruido) aprovechando `CameraX`.
   - Funciones selfie premium: modo belleza, filtros sutiles, temporizador.
   - Conectar el icono de cámara en `ChatInputBar` para abrir este módulo.

2. **Módulo de Adjuntos (`:attachments`)**
   - Permitir seleccionar documentos, imágenes de galería y audio.
   - Integrar con el `GalleryPickerPanel` existente o mejorarlo.
   - Asegurar que los archivos se compriman/optimicen antes de enviar.

3. **Módulo de Emojis y GIFs (`:emoji`)**
   - Crear un módulo `:emoji` con una UI deslizable por categorías.
   - Incluir todos los emojis del estándar Unicode (usar librería `emoji-java` o similar).
   - Sección de "Recientes" y "Más usados".
   - Integración de GIFs mediante API de Giphy o Tenor, con búsqueda.
   - Reemplazar el emoji picker básico en `ChatInputBar` por este nuevo módulo.

4. **Integración y aislamiento**
   - Cada módulo expondrá una interfaz (`Contract`) para que `:app` solo dependa de ella.
   - No se modificará la estructura del `Scaffold` ni del `bottomBar` existente; los nuevos módulos se abrirán como pantallas completas o `ModalBottomSheet`.
   - Se mantendrá la compilación exitosa tras cada paso.

## Checkpoint creado
`git tag checkpoint-20260802-zumbido-premium` (commit `ba19d9bb`)
