═══ PROJECT_STATUS.md — Reconstruido desde git ═══
**Fecha:** 2026-07-13
**Commit actual:** c047b788 (HEAD -> main)
**Última actualización manual:** commit 5855d54b (2026-06-28)

## ── ESTADO GENERAL DEL PROYECTO ──
Fase actual: 3 – Comunicación Avanzada y Pre‑Mesh Discovery
Porcentaje estimado de avance: ~75 %
Estado de compilación: COMPILA (BUILD SUCCESSFUL) con warnings.
App funcional: navegación, chat persistente, notas de voz push-to-talk, barra inferior modularizada, indicador de grabación, perfil encriptado, temas dinámicos.

## ── ARQUITECTURA DE MÓDULOS ──
- :app → UI, ViewModels, Injector, Managers (dependencias circulares con :network por resolver)
- :core → Interfaces, modelos, utilidades base
- :data → Room, DAOs, repositorios
- :crypto → CryptoEngine, adaptador
- :events → MallaEventBus
- :identity → IdentityManager (almacenamiento encriptado)
- :transport → Transportes reales (BleTransport, WifiDirectTransport, TcpDirectTransport, MeshLinker)
- :media → VoiceRecorder, PttManager
- :network → BleManager, WifiDirectManager, ConnectivityMonitor, DhtService, MessageBridge, etc.

## ── RESUMEN DE CAMBIOS DESDE ÚLTIMO PROJECT_STATUS.md ──
(agrupados por funcionalidad, evidencia en git log)

### Modularización completa (commits dd7cc1fc, 2a8d936b)
- Se crearon los módulos :core, :data, :crypto, :events, :identity, :media, :network, :transport.
- Se movieron entidades Room, DAOs, interfaces y managers a sus módulos.
- `Injector` se adaptó para usar los nuevos módulos.
- Se eliminaron archivos duplicados (DoubleRatchet, IdenticonGenerator, PulseManager, transportes viejos, pantallas obsoletas).

### Reconstrucción de servicios de red y comunicaciones (f7df3d36..14caf107)
- `Injector` completamente reconstruido con FlashlightTransport, NetworkService, MessageBridge, SmsTransport, UnifiedMessageRouter.
- `MessageBridge` con callbacks de envío/recepción, forward y procesamiento unificado.
- `DhtService`, `SeedManager`, `ContactDiscoveryManager` integrados.
- `PulsoScreen` restaurado desde backup; switch SMS en pestaña MODOS.
- `FaroScreen` reactivado con comunicación óptica.

### ChatScreen y UI premium (20179170..95b8fc5b)
- ChatScreen funcional con burbujas premium, shake global, envío de imágenes (GalleryPickerPanel), zoom, zumbido, barra de texto animada.
- `ChatViewModel` con persistencia en Room.
- `CascadeRouter` y `MessageReceiver` para encaminamiento de mensajes.
- `MainTopBar` con avatar 44dp, borde cian, indicador de conexión inteligente.
- `PerfilScreen` con almacenamiento encriptado de identidad (IdentityManager).
- `Stickers` integrados con selector y visor a pantalla completa.
- Navegación corregida (BackHandler en submenús, doble toque para salir).
- Control automático de Bluetooth/WiFi con `RadioManager` y servicio foreground.

### Notas de voz push-to-talk (e601265d..cbd22ef8)
- `VoiceRecorder` mejorado (AAC) con `amplitude` StateFlow.
- `VoiceRecorderButton` modularizado con animación de onda sinusoidal.
- `ChatInputBar` integrado con campo de texto, zumbido y botón de micrófono.
- Indicador de grabación estilo ondas en barra inferior.
- Auto-scroll al último mensaje implementado.

### Estabilización y permisos (ebfb098e, 2803c05b, 50da606e, fe8efead)
- Protección del servicio foreground con permiso POST_NOTIFICATIONS.
- Permisos solicitados al iniciar (ubicación, Bluetooth, cámara, etc.).
- Restauración de estados de Bluetooth/WiFi en onDestroy.

## ── DECISIONES TÉCNICAS IDENTIFICADAS ──
1. No refactorizar `Injector` para romper ciclos con :network en esta fase.
2. Uso de AAC para grabación de voz (calidad y compatibilidad).
3. Extracción de la barra de chat a `ChatInputBar` independiente.
4. Indicador de grabación visual con onda sinusoidal animada.
5. Chat propio "Yo" (self_chat) añadido en ConversationsScreen.
6. Uso de `EncryptedSharedPreferences` para datos sensibles de identidad.
7. Eliminación de PulseManager y lógica de decisión automática; se delegó a ConnectivityMonitor y selección manual en PulsoScreen.

## ── ERRORES RESUELTOS (según commits) ──
- Crash por `AppDatabase.getInstance()` nulo → manejo de null y fallback.
- Grabación de audio vacía → cambio a codec AAC con fuente MIC.
- `AnimatedVisibility` en Row → reemplazado por Crossfade.
- `Injector.messageRepo` no inicializado → verificación `isInitialized` en ChatViewModel.
- Indentación en `ConversationsScreen` que ocultaba lista → corrección de inserción del chat propio.
- Crash temprano por `WifiDirectManager` no inicializado → se añadió inicialización en Injector (sesión actual).
- Duplicación de chat "Yo" en ConversationsScreen → eliminado.

## ── DEUDA TÉCNICA ACTUAL ──
1. **Pruebas de comunicación real** – BLE, Wi‑Fi Direct, TCP no se han probado entre dispositivos físicos.
2. **Refactorización de Injector** – pendiente para eliminar dependencias circulares con :network.
3. **Vista previa de imágenes estilo WhatsApp** – incompleta, se requiere `GetContent` y lista `pendingMediaUris`.
4. **Notas de voz** – el indicador visual de ondas a veces no se mueve (posible problema de permisos o amplitud cero).
5. **Icono de notificación grande** (`setLargeIcon`) no implementado.
6. **Cobertura de pruebas unitarias** – inexistente.
7. **Limpieza de warnings** – hay múltiples deprecaciones y variables sin uso.

## ── PRÓXIMOS PASOS PRIORITARIOS ──
1. Verificar que el chat "Yo" funciona correctamente.
2. Probar grabación y reproducción de notas de voz con permisos adecuados.
3. Implementar vista previa de imágenes adjuntas.
4. Pruebas de comunicación en dispositivos reales.
5. Refactorizar `Injector`.
6. Actualizar este archivo en cada commit significativo.

── RECONSTRUCCIÓN ──
Este archivo fue reconstruido el 2026-07-13 desde git log, cubriendo desde el commit 5855d54b hasta HEAD (c047b788), porque no se actualizó en tiempo real durante ese período. Cualquier decisión o contexto de esas sesiones que NO haya quedado reflejado en un commit (conversaciones, descartes, razones no documentadas en el mensaje del commit) se considera perdido y no está reflejado aquí.

── 2026-07-25 · Sesión N° 10 ──
CAMBIO: Añadida MallaApplication con crash logger, Room callback en AppDatabase y Toast de depuración en ConversationsScreen.
ARCHIVOS TOCADOS:
  - app/src/main/java/com/malla/mvp/MallaApplication.kt (creado)
  - app/src/main/AndroidManifest.xml
  - app/src/main/java/com/malla/mvp/data/AppDatabase.kt
  - app/src/main/java/com/malla/mvp/ui/screen/ConversationsScreen.kt
POR QUÉ: Mejorar trazabilidad de fallos en campo y preparar infraestructura para chat "Yo". Se justifica en confiabilidad offline (crash logs persistentes en dispositivo) y depuración visual.
ESTADO DE COMPILACIÓN: BUILD SUCCESSFUL — [pegar aquí la salida real de ./gradlew]
VERIFICADO EN: solo compilación (pendiente de prueba en dispositivo real para validar logs y Toast).
PROBLEMAS ENCONTRADOS: Ninguno durante la integración.
IDEAS / MEJORAS PENDIENTES:
  - El Toast de depuración debería eliminarse antes de release.
  - El Room Callback debe contener la lógica de creación de la conversación "Yo" (actualmente vacío/truncado).
PRÓXIMO PASO INMEDIATO: Probar en dispositivo real la creación del chat "Yo" y el funcionamiento del crash logger.
── 2026-07-25 · Sesión N° 10 ──
CAMBIO: Añadida MallaApplication con crash logger, Room callback en AppDatabase, Toast de depuración en ConversationsScreen, y registro en AndroidManifest.
ARCHIVOS TOCADOS:
  - app/src/main/java/com/malla/mvp/MallaApplication.kt (creado)
  - app/src/main/AndroidManifest.xml
  - data/src/main/java/com/malla/mvp/data/AppDatabase.kt
  - app/src/main/java/com/malla/mvp/ui/screen/ConversationsScreen.kt
POR QUÉ: Mejorar trazabilidad de fallos en campo (confiabilidad offline) y preparar infraestructura para chat "Yo".
ESTADO DE COMPILACIÓN: BUILD SUCCESSFUL en ambos commits (85e9f480 y 5b77369b).
VERIFICADO EN: solo compilación (pendiente de prueba en dispositivo real para validar logs, Toast y callback).
PROBLEMAS ENCONTRADOS: Ruta incorrecta al añadir AppDatabase.kt en el primer commit; corregido en el segundo commit (5b77369b).
IDEAS / MEJORAS PENDIENTES:
  - El Toast de depuración debe eliminarse antes de release.
  - El Room Callback debe contener lógica real de creación de la conversación "Yo" (actualmente vacío/truncado).
PRÓXIMO PASO INMEDIATO: Probar en dispositivo real la creación del chat "Yo" y el funcionamiento del crash logger.
── CIERRE DE SESIÓN 2026-07-25 ──
ERRORES PENDIENTES / DEUDA TÉCNICA: 
  1. Pruebas de comunicación BLE/Wi-Fi Direct entre dispositivos reales — NO REALIZADAS.
  2. Refactorización de Injector para romper dependencias circulares con :network — PENDIENTE.
  3. Vista previa de imágenes estilo WhatsApp — INCOMPLETA.
  4. Indicador de ondas en grabación de voz a veces no se mueve — POSIBLE BUG.
  5. Icono de notificación grande (setLargeIcon) no implementado.
  6. Cobertura de pruebas unitarias — INEXISTENTE.
  7. Toast de depuración en ConversationsScreen debe eliminarse antes de release.
  8. El Room Callback añadido está vacío/truncado — debe contener la lógica de creación de la conversación "Yo" (pendiente de implementar/probar).
LO QUE NO PUDE VERIFICAR: 
  - Que el crash logger escriba correctamente en MediaStore en un dispositivo real.
  - Que el Toast de depuración muestre las conversaciones correctas.
  - El contenido completo del Room Callback (la definición está truncada en el diff, pero está commiteado).
  - Que el chat "Yo" se cree automáticamente al abrir la BD (depende de la lógica dentro del Callback).
DEPENDENCIAS ACTUALES: 
  (app → core, data, crypto, events, identity, media, network, transport)
  (data → core, room)
  (crypto → core)
  (transport → core, network)
  (network → core, data)
LÓGICA DE PROGRAMACIÓN CLAVE:
  - MallaApplication usa UncaughtExceptionHandler para capturar crashes y escribirlos en disco.
  - AppDatabase ahora tiene un Callback que se ejecuta al abrir la BD (probablemente para crear la conversación "Yo").
  - ConversationsScreen muestra un Toast con los IDs de conversación al recoger la lista desde Room.
PLAN DE ACCIÓN PARA LA PRÓXIMA SESIÓN:
  TAREA INMEDIATA: Probar en dispositivo real la creación del chat "Yo" (instalar APK, abrir la app, verificar Toast y que aparezca la conversación "Yo").
  TAREAS SIGUIENTES EN ORDEN DE PRIORIDAD:
    1. Validar que el crash logger escribe archivos en /Downloads en Android 10+.
    2. Eliminar el Toast de depuración y hacer un commit de limpieza.
    3. Probar grabación de voz y reproducción con permisos reales.
    4. Refactorizar Injector.

── 2026-07-25 · Sesión N° 10 (continuación) ──
CAMBIO: Diagnóstico y reparación del chat "Yo" que no se creaba.
DIAGNÓSTICO:
  - AppDatabase.getInstance() devolvía null porque Room no generaba AppDatabase_Impl.
  - Causa raíz: faltaba KSP y room-compiler en data/build.gradle.kts.
  - Errores adicionales detectados por KSP:
    * PollVoteEntity no estaba en la lista de entidades de @Database.
    * ConversationDao.getAllConversations() usaba "lastMessageTimestamp" en lugar de "timestamp".
  - Crash al dibujar ConversationCard por valor HSL negativo (hashCode de "Yo (Mensajes guardados)").
SOLUCIONES APLICADAS:
  - Agregado plugin KSP y dependencia room-compiler en data/build.gradle.kts.
  - Agregada PollVoteEntity a @Database en AppDatabase.kt.
  - Corregido nombre de columna en ConversationDao.kt.
  - Implementada creación del chat "Yo" en MainActivity.onCreate() con diagnóstico de errores vía Toasts.
  - Corregido cálculo de color HSL en ConversationCard.kt usando Math.abs().
  - Eliminados Toasts de depuración en ConversationsScreen.kt y ConversationCard.kt.
ARCHIVOS MODIFICADOS:
  - data/build.gradle.kts (plugin KSP, room-compiler)
  - data/src/main/java/com/malla/mvp/data/AppDatabase.kt (PollVoteEntity en @Database)
  - data/src/main/java/com/malla/mvp/data/dao/ConversationDao.kt (timestamp)
  - app/src/main/java/com/malla/mvp/MainActivity.kt (creación de chat "Yo" con diagnóstico)
  - app/src/main/java/com/malla/mvp/ui/components/ConversationCard.kt (Math.abs en HSL)
  - app/src/main/java/com/malla/mvp/ui/screen/ConversationsScreen.kt (limpieza de Toasts)
ESTADO DE COMPILACIÓN: BUILD SUCCESSFUL.
VERIFICADO EN: dispositivo real. El chat "Yo (Mensajes guardados)" aparece correctamente en la lista de conversaciones.
DEUDA TÉCNICA ACTUALIZADA:
  - Toast de depuración en ConversationsScreen: ELIMINADO.
  - Room Callback para chat "Yo": REEMPLAZADO por lógica directa en MainActivity (más confiable).
  - Crash logger: verificado que escribe en Downloads; se usó para diagnosticar el error de HSL.
PRÓXIMO PASO INMEDIATO: Probar comunicación BLE/Wi-Fi Direct entre dispositivos reales.

── 2026-08-01 · Sesión de estabilización y mejoras visuales ──
CAMBIO: Estabilización completa de la app y adición del indicador de escritura profesional.
DIAGNÓSTICO (cierres al enviar mensajes):
  - Existía un duplicado de la clase MessageData en los módulos app y core, con diferentes campos. El mapeador usaba la versión del módulo app pero en ejecución se cargaba la del core, provocando NoSuchMethodError.
  - Adicionalmente, ChatScreen referenciaba ChatViewModel (inexistente) en lugar de MeshChatViewModel.
SOLUCIONES APLICADAS:
  - Eliminada la definición duplicada de MessageData en el módulo app.
  - Añadido campo reaction a la definición de MessageData en el módulo core.
  - Creado MessageMapper para centralizar la conversión entre MessageEntity y MessageData.
  - ChatScreen actualizado para usar MeshChatViewModel.
  - Integrado ComposingBubble (indicador de escritura con puntos saltarines y avatar).
  - Añadido parámetro onTextChanged a ChatInputBar para conectar el indicador.
  - Corregido smart cast de mediaUri mediante variable local 'media'.
ARCHIVOS MODIFICADOS:
  - app/.../core/data/IMessageRepository.kt (eliminado duplicado)
  - core/.../core/data/IMessageRepository.kt (añadido campo reaction)
  - app/.../core/data/MessageMapper.kt (creado)
  - app/.../ui/screen/ChatScreen.kt (ViewModel corregido, indicador de escritura)
  - app/.../ui/components/ChatInputBar.kt (parámetro onTextChanged)
  - app/.../ui/components/ComposingBubble.kt (creado, componente del indicador)
  - data/.../entity/MessageEntity.kt (campo reaction)
ESTADO DE COMPILACIÓN: BUILD SUCCESSFUL.
VERIFICADO EN: dispositivo real. La app no se cierra al enviar mensajes; el indicador de escritura funciona correctamente.
DEUDA TÉCNICA ACTUALIZADA:
  - Reacciones (doble tap): pendiente, capa de datos lista (falta UI).
  - Caja de texto que crece hacia arriba: pendiente, requiere reestructurar layout (mover al bottomBar).
  - Búsqueda en el chat: pendiente (ya se implementó en sesión anterior pero fue revertida; se puede re-aplicar).
PRÓXIMO PASO INMEDIATO: Implementar reacciones con doble tap (picker flotante de emojis).
