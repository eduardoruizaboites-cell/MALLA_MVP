# 📊 Estado del Proyecto – MALLA MVP

**Fecha/Hora:** 2026-08-13 (actual)

## 🎯 Cambios realizados en esta sesión

- ✅ Permisos solicitados nativamente (sin ir a Ajustes)
- ✅ Pantalla de explicación de permisos solo primer inicio
- ✅ Estilos de burbuja simplificados a una sola forma redondeada
- ✅ Personalización individual por conversación (colores, sonidos, fondo, tamaño)
- ✅ Selector de colores con paleta de 24 colores arcoíris
- ✅ Menú de avatar en chat con "Ver perfil" y "Personalizar chat"
- ✅ Indicador de escritura alineado a la izquierda
- ⏳ Animación premium de burbujas (pendiente de confirmar)

## 🧾 Bitácora de errores encontrados y soluciones

| Fecha | Error | Causa | Solución |
|-------|-------|-------|----------|
| 2026-08-13 | `NoSuchMethodError: smsTransport` | Falta inicialización de `Injector` al usar `MallaApplication` | Se cambió el `AndroidManifest` para usar `.App` (que inicializa `Injector`) |
| 2026-08-13 | `lateinit property smsTransport` | Idem | Idem |
| 2026-08-13 | Menú del avatar no funcionaba | Faltaban variables `showChatMenu` y `showChatSettings` en `ChatScreen` | Se insertaron estados y `DropdownMenu` |
| 2026-08-13 | Pantalla de permisos aparecía siempre | No se guardaba el flag `permission_explanation_shown` | Se añadió lectura/escritura en `SharedPreferences` y se corrigió la lógica en `MainActivity` |
| 2026-08-13 | Permisos enviaban a Ajustes | Falta llamada a `permissionLauncher.launch()` en `requestPermissions()` | Se añadió `permissionLauncher.launch(requiredPermissions.toTypedArray())` |
| 2026-08-13 | `initialAnimation` no declarado | Se insertó en lugar equivocado | Se restauró `ChatScreen` y se aplicaron cambios mínimos |
| 2026-08-13 | `animateItem()` no reconocido | `MessageBubbleV2` no tiene parámetro `modifier` | Se eliminó esa modificación |
| 2026-08-13 | Estilos de burbuja no se simplificaban | Referencias a `BubbleStyle` antiguas en varios archivos | Se ejecutó script de reemplazo global de estilos |

## 🚀 Próximos pasos

- Verificar en dos dispositivos: descubrimiento, mensajería, cambios individuales.
- Mejorar animación de entrada de burbujas (rebote más notorio).
- Ajustar la barra de entrada (ChatInputBar) si es necesario.
- Integrar transporte global WebRTC y servidor de señalización.

---

*Bitácora generada automáticamente para seguimiento del desarrollo.*

── ENTRADA — 2026-08-16 05:45 (reversión a commit estable b58ebb61) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se revirtió la rama feature/chat-yo al commit b58ebb61 (estable previo a la tanda de UI premium y ANR). Se eliminaron archivos no rastreados MallaApp.kt, MallaApplication.kt, anr.txt, generar y .AndroidManifest.xml.swp para restaurar limpieza. Se forzó actualización del remoto. Se compiló exitosamente.
¿ERA UN FIX DE ERROR?: ERROR: ANR persistente en pantalla de conversaciones introducido en la tanda premium → SOLUCIÓN APLICADA: revertir a commit estable y limpiar artefactos → ¿FUNCIONÓ?: compilación exitosa; pendiente confirmar en dispositivo.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): múltiples hipótesis (Badge, ConversationCard, RadarAnimation, flujos, spinner, animaciones top bar) no resolvieron; se optó por reversión total.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, funcionalidad base estable.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): reintroducir features premium de forma incremental con prueba en dispositivo entre cada una; consolidar App duplicadas; limpiar warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: se pierden temporalmente funciones premium (respuesta, edición, encuestas, efímeros, formato, exportar, personalización avanzada, sonidos, fondo imagen, avatar degradado). Se guardaron en rama de respaldo (ver git branch).
──────────────────────────────

── ENTRADA — 2026-08-18 05:15 (implementación ANRWatchDog) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se creó ANRWatchDog.kt y se integró en App.kt. El watchdog detecta bloqueos del hilo principal (>5s) y genera un reporte automático en Descargas con el stack trace del hilo principal y todos los hilos. Se mantiene la base estable b58ebb61.
¿ERA UN FIX DE ERROR?: no era fix; fue implementación de utilidad de diagnóstico solicitada.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, MediaStore.Downloads Android 10+, almacenamiento externo anterior.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): probar watchdog simulando un bloqueo; reintroducir mejoras premium una por una; consolidar App duplicadas; limpiar warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: watchdog sin prueba real; MallaApplication.kt borrada pendiente de commit; warnings de deprecación existentes.
──────────────────────────────

── ENTRADA — 2026-08-18 05:30 (mejora ANRWatchDog) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se amplió ANRWatchDog para incluir dump del Looper principal (cola de mensajes) y metadatos del dispositivo (marca, modelo, Android). Se mantiene el stack trace del hilo principal y de todos los hilos.
¿ERA UN FIX DE ERROR?: no era fix; fue mejora de utilidad de diagnóstico solicitada.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Looper.dump disponible desde API 1, MediaStore.Downloads Android 10+.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): probar watchdog simulando bloqueo; reintroducir premium incremental; consolidar App duplicadas; limpiar warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: watchdog sin prueba real; warnings de deprecación; premium por reintroducir.
──────────────────────────────

## 🔴 FUNCIÓN PREMIUM CAUSANTE DEL ANR (ANÁLISIS)

**Fecha de análisis:** 2026-08-18

**Función señalada en rojo:**  
La combinación de **personalización avanzada de chat** y **animaciones continuas** introducidas en los commits:

- `c90418a7` feat(ui): reorganizar barra superior y rediseñar personalización de chat  
- `dcda77ec` feat(ui): sonidos reales, animación sutil de burbujas y fondo degradado  
- `a38a4c88` feat(ui): fondo con imagen, más sonidos, animación de escritura y menú elegante  
- `6301f815` feat(ui): avatar degradado, reacciones en chips y botón enviar premium  

**Por qué:**  
Estos commits introdujeron múltiples animaciones infinitas (`rememberInfiniteTransition`) y un `CircularProgressIndicator` en la pantalla principal. Al combinarse con el flujo de conversaciones que emitía con frecuencia, saturaron el hilo principal y provocaron el ANR.

**Lección aprendida:**  
Reintroducir estas funciones **una por una**, compilando y probando en dispositivo real entre cada adición. Evitar animaciones infinitas cuando no sean esenciales y controlar las emisiones del flujo de datos.

**Estado:** Pendiente de reintroducción incremental.
──────────────────────────────

── ENTRADA — 2026-08-18 06:00 (creación ConversationsViewModel) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se creó ConversationsViewModel para exponer un StateFlow de conversaciones con distinctUntilChanged y catch. Aún no integrado en la pantalla.
¿ERA UN FIX DE ERROR?: no era fix; fue preparación para estabilizar la pantalla principal antes de reintroducir mejoras.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, ViewModel, StateFlow.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): integrar ViewModel en ConversationsScreen; reintroducir premium incremental; aplicar principios premium visual.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: pantalla principal aún usa LaunchedEffect manual; ANR resuelto solo por reversión; premium pendiente.
──────────────────────────────

── ENTRADA — 2026-08-18 06:20 (integración ConversationsViewModel en pantalla) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se integró ConversationsViewModel en ConversationsScreen. La pantalla principal ahora recolecta el flujo de conversaciones desde el ViewModel con StateFlow y distinctUntilChanged. Se eliminó el CircularProgressIndicator animado y se reemplazó por un estado vacío estático. Se restauró conversationDao para acciones de UI.
¿ERA UN FIX DE ERROR?: ERROR: ANR previo causado por flujo inestable y animación de loading → SOLUCIÓN APLICADA: ViewModel + StateFlow estable y eliminación de animación infinita en pantalla principal → ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dispositivo.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; se confirma la causa raíz por reversión y se corrige estructuralmente.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, ViewModel, StateFlow, Compose.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): probar en dispositivo antes de continuar; reintroducir premium incremental con diseño premium; limpiar warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: sin prueba real; premium por reintroducir; warnings.
──────────────────────────────

── ENTRADA — 2026-08-18 06:45 (capa de datos para respuesta con cita) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se agregaron campos quotedMessageId y quotedMessageContent a MessageData y MessageMapper. Aún sin UI.
¿ERA UN FIX DE ERROR?: no era fix; fue preparación de datos para la función premium de respuesta con cita.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Room, Compose.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): implementar UI de cita; probar en dispositivo.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: sin UI de cita; warnings.
──────────────────────────────

── ENTRADA — 2026-08-18 07:10 (función premium: responder con cita) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se implementó la función premium de responder mensajes con cita. Se agregaron campos quotedMessageId/quotedMessageContent en MessageData y MessageMapper. En ChatScreen se añadió estado replyingTo, gesto de long-press en mensajes, y al enviar se incluyen los campos de cita. En ChatInputBar se creó una barra de cita animada con AnimatedVisibility, línea de acento degradado, texto truncado y botón de cerrar.
¿ERA UN FIX DE ERROR?: no era fix; fue implementación de feature premium.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; se manejó error de compilación por combinedClickable experimental con @OptIn.
VERIFICADO EN: solo compilación (falta dispositivo).
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, AnimatedVisibility, combinación de gestos.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): probar en dispositivo; reintroducir editar/eliminar; exportar; encuestas; etc.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: sin prueba real; warnings de deprecación; premium restante.
──────────────────────────────

── ENTRADA — 2026-08-18 07:40 (visualización de cita premium) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se completó la función premium de respuesta con cita. MessageReceiver ahora persiste quotedMessageId y quotedMessageContent al recibir. BubbleContent muestra una previsualización elegante de la cita con línea de acento y texto truncado. Se corrigió import faltante TextOverflow en ChatScreen.kt.
¿ERA UN FIX DE ERROR?: ERROR: no se visualizaba la cita en los mensajes → SOLUCIÓN APLICADA: persistir cita en receiver y agregar UI premium en burbuja → ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dispositivo.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; se encontró causa por inspección.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, visualización de cita.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): probar en dispositivo real; continuar con editar/eliminar; exportar; etc.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: sin prueba real; warnings; premium restante.
──────────────────────────────

── ENTRADA — 2026-08-18 08:00 (verificación en dispositivo de cita premium) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se verificó en dispositivo físico que la función premium de responder con cita funciona correctamente. El mensaje citado se visualiza dentro de la burbuja con línea de acento y texto truncado. No se presentaron bloqueos, cierres ni errores.
¿ERA UN FIX DE ERROR?: no era fix; fue confirmación de implementación.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: dispositivo real.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, gesto long-press, AnimatedVisibility.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): continuar con edición/eliminación premium; exportar conversación; encuestas; etc.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: warnings de deprecación; premium restante.
──────────────────────────────

── ENTRADA — 2026-08-18 08:30 (fase datos: campos de edición) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se agregaron isEdited e isDeleted a MessageEntity, MessageData y MessageMapper. Se actualizó AppDatabase a versión 14 con migración aditiva para las nuevas columnas.
¿ERA UN FIX DE ERROR?: no era fix; fue preparación para la función premium de edición/eliminación.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Room, migración aditiva.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): implementar lógica de edición/eliminación en ViewModel y receptor; UI premium.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: sin prueba real; premium restante.
──────────────────────────────

── ENTRADA — 2026-08-18 09:00 (función premium: editar y eliminar mensajes) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se implementaron editar mensajes propios y eliminar para todos. Se añadieron campos isEdited/isDeleted con migración Room 13→14. Se agregaron métodos editMessage y deleteForAll en MeshChatViewModel. MessageReceiver maneja los tipos edit y delete_for_all. ChatScreen incluye menú contextual premium (Responder, Editar, Eliminar), diálogos elegantes y visualización de estado editado/eliminado. BubbleContent muestra "editado" y "Mensaje eliminado".
¿ERA UN FIX DE ERROR?: no era fix; fue implementación de feature premium.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; errores de compilación por imports y tipos corregidos.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Room, Compose, AlertDialog.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): probar en dispositivo real; reintroducir encuestas; exportar conversación; personalización avanzada; etc.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: sin prueba real; warnings de deprecación; premium restante.
──────────────────────────────

── ENTRADA — 2026-08-18 09:40 (barra contextual premium para mensajes) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se reemplazó el menú DropdownMenu simple por una barra contextual premium que sustituye la top bar al mantener presionado un mensaje. Incluye iconos para Responder, Editar (si es propio) y Eliminar (si es propio), con botón de cerrar. Se eliminó el menú local dentro de MessageBubbleV2 y se gestiona todo desde ChatScreen con estado selectedMessage.
¿ERA UN FIX DE ERROR?: no era fix; fue mejora de UX/Motion premium.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; se resolvieron errores de imports y restos del menú anterior con git checkout y script limpio.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, Surface, TopAppBar condicional.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): probar en dispositivo real; reintroducir encuestas; exportar conversación; personalización avanzada; etc.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: sin prueba real; warnings de deprecación; premium restante.
──────────────────────────────

── ENTRADA — 2026-08-18 10:00 (verificación en dispositivo de edición/eliminación premium) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se verificó en dispositivo físico que la barra contextual premium funciona correctamente. Al mantener presionado un mensaje propio aparecen Responder, Editar y Eliminar; en mensajes ajenos solo Responder. Los diálogos de edición/eliminación funcionan. No se presentaron bloqueos ni errores.
¿ERA UN FIX DE ERROR?: no era fix; fue confirmación de implementación premium.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: dispositivo real.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, TopAppBar condicional.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): continuar con encuestas; exportar conversación; personalización avanzada; etc.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: warnings de deprecación; MallaApplication.kt borrada pendiente de commit; premium restante.
──────────────────────────────

── ENTRADA — 2026-08-18 10:40 (fase datos: campo pollId) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se agregó pollId a MessageEntity, MessageData y MessageMapper. Se actualizó AppDatabase a versión 15 con migración aditiva 14→15 para la nueva columna pollId en messages.
¿ERA UN FIX DE ERROR?: no era fix; fue preparación de datos para encuestas premium.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Room, migración aditiva.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): implementar lógica de encuestas en ViewModel y receptor; UI premium de creación y votación.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: sin prueba real; premium restante.
──────────────────────────────

── ENTRADA — 2026-08-18 11:10 (fases 1-2: encuestas, datos y lógica) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se agregó pollId a MessageEntity/MessageData/MessageMapper con migración 14→15. En MeshChatViewModel, createPoll y votePoll envían JSON por red con tipos poll_create/poll_vote. MessageReceiver procesa poll_create y poll_vote, persiste encuestas, opciones y votos. Aún falta UI premium de creación y votación (Fase 3).
¿ERA UN FIX DE ERROR?: no era fix; fue implementación de encuestas premium.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Room, org.json, flujo de red.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): implementar UI premium de encuestas; probar en dispositivo; continuar con otras funciones.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: sin prueba real; warnings.
──────────────────────────────

── ENTRADA — 2026-08-18 11:40 (fase 3: UI premium de encuestas) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se implementó la interfaz premium de encuestas en ChatScreen. Se agregó recolección de polls y optionsMap desde el ViewModel, diálogo de creación de encuesta con opciones dinámicas, botón Encuesta en el panel de adjuntos premium y componente PollMessageBubble con votación, porcentajes y barras de progreso. Se bifurcó la lista de mensajes para mostrar encuestas con su propio composable.
¿ERA UN FIX DE ERROR?: no era fix; fue implementación de UI premium para encuestas.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, LinearProgressIndicator, AlertDialog.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): probar en dispositivo real; reintroducir exportar conversación; mensajes efímeros; etc.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: sin prueba real; warnings.
──────────────────────────────

── ENTRADA — 2026-08-18 12:00 (fix: encuesta no aparecía en historial) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se corrigió createPoll en MeshChatViewModel para insertar un MessageEntity local con pollId al crear una encuesta. Esto permite que la burbuja de encuesta aparezca en el historial y se renderice con PollMessageBubble.
¿ERA UN FIX DE ERROR?: ERROR: al crear encuesta no se mostraba en el chat → SOLUCIÓN APLICADA: insertar mensaje local con pollId y refrescar mensajes → ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dispositivo.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; causa identificada por inspección.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Room, flujo de mensajes.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): probar en dispositivo; reintroducir exportar conversación; etc.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: sin prueba real; warnings.
──────────────────────────────

── ENTRADA — 2026-08-18 12:30 (fix: opciones de encuesta no visibles) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se corrigió createPoll para actualizar _polls y _optionsMap inmediatamente y se cambió el renderizado de PollMessageBubble a usar optionsMap en lugar de un flujo por opción. Esto resuelve que las opciones no aparecieran en la burbuja de encuesta.
¿ERA UN FIX DE ERROR?: ERROR: las opciones de la encuesta no se mostraban → SOLUCIÓN APLICADA: actualizar estados de ViewModel y usar optionsMap → ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dispositivo.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; causa identificada por inspección.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, StateFlow.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): probar en dispositivo; reintroducir exportar conversación; etc.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: sin prueba real; warnings.
──────────────────────────────

── ENTRADA — 2026-08-18 13:10 (fix: reactividad de votación en encuestas) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se reemplazó PollMessageBubble por una versión reactiva que usa collectAsState sobre getOptionsForPoll(pollId) y vm.polls. Ahora los cambios de votación se reflejan en tiempo real sin salir del chat. Se eliminó la carga estática previa.
¿ERA UN FIX DE ERROR?: ERROR: el cambio de votación no se veía inmediatamente → SOLUCIÓN APLICADA: flujo reactivo de opciones desde Room → ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dispositivo.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; causa identificada por inspección.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, Room Flow, collectAsState.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): probar en dispositivo real; continuar con exportar conversación; etc.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: sin prueba real; warnings.
──────────────────────────────

── ENTRADA — 2026-08-18 13:30 (verificación en dispositivo de encuestas premium) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se verificó en dispositivo físico que las encuestas premium funcionan correctamente. Se crean con opciones, se visualizan con barras de progreso y porcentajes, y la votación se actualiza en tiempo real. No se presentaron bloqueos ni errores.
¿ERA UN FIX DE ERROR?: no era fix; fue confirmación de implementación premium.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: dispositivo real.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, Room Flow, votación reactiva.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): continuar con exportar conversación; mensajes efímeros; selección múltiple; personalización avanzada; etc.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: warnings de deprecación; premium restante.
──────────────────────────────

── ENTRADA — 2026-08-18 09:50 (mensajes efímeros / vista única - Fase 1: datos y red) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se extendió `MeshMessage` con `expireAt` y `viewOnce`. Se agregó `deleteExpiredMessages` en `MessageDao`. En `MeshChatViewModel`, `refreshMessages` limpia mensajes vencidos antes de cargar; `sendMessage` transmite `expireAt`/`viewOnce`; `handleIncomingMessage` los persiste. `MessageReceiver` guarda esos campos en mensajes entrantes.
¿ERA UN FIX DE ERROR?: no era fix; fue implementación base de feature premium.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
IDEAS DE MEJORA QUE SURGIERON (sin implementar aún): integrar UI de selección de expiración y vista única; programar limpieza en background; probar en dispositivo.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: UI no implementada; warnings nuevos en MessageReceiver y ViewModel; sin prueba real.
──────────────────────────────

── ENTRADA — 2026-08-18 09:53 (mensajes efímeros / vista única - Fase 2: UI premium) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se agregó botón de temporizador en ChatInputBar. Se creó diálogo premium `EphemeralOptionsDialog` en ChatScreen con selección de duración (Nunca, 24h, 7d, 90d) y opción de vista única. Los mensajes de texto y voz ahora pasan `expireAt` y `viewOnce` al ViewModel.
¿ERA UN FIX DE ERROR?: no era fix; fue implementación de UI premium.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación; pendiente prueba en dispositivo.
IDEAS DE MEJORA QUE SURGIERON (sin implementar aún): mostrar indicador visual de mensaje efímero en burbuja; probar vista única en imágenes/vídeo; programar limpieza en background.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: sin prueba real de la UI; no se ha implementado la visualización de caducidad/vista única en las burbujas; warnings existentes.
──────────────────────────────

── ENTRADA — 2026-08-18 09:57 (mensajes efímeros / vista única - Fase 3: indicadores y vista única) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se añadió `ViewOnceMessageBubble` para mensajes entrantes de un solo vistazo: muestra un contenedor premium con ícono de candado y botón para revelar; al revelar, se elimina el mensaje automáticamente tras 5 segundos usando `vm.deleteMessage`. Se añadió indicador de tiempo restante (`⏳`) en mensajes efímeros. Se importó el icono Lock.
¿ERA UN FIX DE ERROR?: no era fix; fue implementación de UI/comportamiento premium.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación; pendiente prueba en dispositivo.
IDEAS DE MEJORA QUE SURGIERON (sin implementar aún): probar en dispositivo; limpiar warnings; extender vista única a imágenes/vídeo/audio.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: sin prueba real de indicadores y eliminación automática; warnings existentes.
──────────────────────────────

── ENTRADA — 2026-08-18 10:49 (vista única en vista previa premium) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se eliminó el interruptor de vista única del panel de adjuntos. Se creó `MediaPreviewPanel`, una pantalla completa premium de vista previa que se abre automáticamente al seleccionar imágenes, con transición entre imágenes, interruptor de vista única por imagen, pie de foto y botón de envío. Se reemplazó `viewOnceEnabled` por `viewOnceMap` para manejar la vista única por URI. Los mensajes de texto y voz no usan vista única; solo el contenido multimedia visual.
¿ERA UN FIX DE ERROR?: no era fix; fue rediseño de flujo premium solicitado por el usuario.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación; pendiente prueba en dispositivo real.
IDEAS DE MEJORA QUE SURGIERON (sin implementar aún): añadir edición/marcadores en vista previa; mejorar transiciones entre imágenes; probar en dispositivo.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: sin prueba real; vista única no implementada en vídeo/audio; warnings existentes.
──────────────────────────────

── ENTRADA — 2026-08-18 11:19 (corrección: vista única movida a vista previa premium) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se corrigió la implementación anterior que no se había aplicado. Se eliminó definitivamente el interruptor de vista única del panel de adjuntos. Se añadió `MediaPreviewPanel` a pantalla completa que se abre al seleccionar imágenes; incluye vista única por imagen con `viewOnceMap`, eliminación individual, pie de foto y envío. La vista previa inline antigua fue eliminada. Mensajes de texto y voz siguen sin vista única.
¿ERA UN FIX DE ERROR?: sí; la implementación previa no se había aplicado correctamente y dejó la vista única en adjuntos.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): se confirmó mediante `grep` que `viewOnceEnabled` y `viewOnceMap` no existían, indicando que el script previo no se ejecutó.
VERIFICADO EN: solo compilación; pendiente prueba en dispositivo real.
IDEAS DE MEJORA QUE SURGIERON (sin implementar aún): probar en dispositivo; añadir edición/marcadores en vista previa; transiciones más fluidas.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: sin prueba real; vista única no aplica a vídeo/audio; warnings.
──────────────────────────────

── ENTRADA — 2026-08-18 11:51 (vista única real + botón agregar más en vista previa) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se corrigió el comportamiento de vista única: al tocar `ViewOnceMessageBubble` se abre la imagen en pantalla completa, se marca como revelada y se elimina automáticamente a los 5 segundos. Se añadió estado bloqueado "Contenido efímero revelado". Se agregó botón `Agregar más` al carrusel de vista previa con borde punteado semántico (border cian) y transición visual. `MediaPreviewPanel` recibe `onAddMore` para volver a abrir galería.
¿ERA UN FIX DE ERROR?: sí; la opción de vista única no bloqueaba ni eliminaba la imagen al abrirse, y no existía el botón para agregar más imágenes desde la vista previa.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; se confirmó por inspección de código.
VERIFICADO EN: solo compilación; pendiente prueba en dispositivo.
IDEAS DE MEJORA QUE SURGIERON (sin implementar aún): probar en dispositivo; mejorar transiciones del botón agregar; usar animaciones de carrusel más fluidas.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: sin prueba real; vista única no implementada en vídeo/audio; warnings.
──────────────────────────────

── ENTRADA — 2026-08-18 12:18 (carrusel premium en vista previa con HorizontalPager) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se reemplazó el carrusel LazyRow de `MediaPreviewPanel` por `HorizontalPager` con transiciones de escala y alpha entre páginas. Se añadió página final de "Agregar más" con tarjeta de borde cian e icono +. Se aplicó `@OptIn(ExperimentalFoundationApi::class)` a `MediaPreviewPanel` para resolver error de API experimental.
¿ERA UN FIX DE ERROR?: sí; se corrigió error de compilación por API experimental de HorizontalPager.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; el error fue identificado directamente por el compilador.
VERIFICADO EN: solo compilación; pendiente prueba en dispositivo.
IDEAS DE MEJORA QUE SURGIERON (sin implementar aún): probar carrusel en dispositivo; transiciones con parallax; probar vista única entre dos dispositivos.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo pendiente; vista única no implementada en vídeo/audio; warnings.
──────────────────────────────

── ENTRADA — 2026-08-19 05:22 (función premium: reacciones a mensajes) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se implementaron reacciones a mensajes. `MeshChatViewModel` ahora tiene `addReaction` y `removeReaction`; `MessageReceiver` maneja `type="reaction"` y actualiza la reacción en BD; `ChatScreen` incluye botón para reaccionar en la barra contextual, `ReactionPicker` premium con emojis animados, y badge de reacción en la burbuja del mensaje. Se corrigió smart cast de `msg.reaction` con variable local.
¿ERA UN FIX DE ERROR?: no era fix; fue implementación de feature premium.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación; pendiente prueba en dispositivo.
IDEAS DE MEJORA QUE SURGIERON (sin implementar aún): permitir múltiples reacciones por mensaje; mostrar reacción del otro usuario en tiempo real; probar en dispositivo.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo de comunicación sigue pendiente; warnings existentes.
──────────────────────────────

── ENTRADA — 2026-08-19 07:22 (reacciones premium: menú flotante compacto) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se rediseñó el menú de reacciones para que sea flotante, compacto y translúcido. Ahora aparece siempre debajo de la burbuja seleccionada con `DpOffset(0.dp, 4.dp)`. Incluye emojis recientes en scroll horizontal y botón "+" al final para abrir hoja extendida de reacciones. Se eliminó la lógica de alternar arriba/abajo según posición.
¿ERA UN FIX DE ERROR?: no era fix; fue mejora de UX/Motion premium solicitada.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación; pendiente prueba en dispositivo.
IDEAS DE MEJORA QUE SURGIERON (sin implementar aún): probar en dispositivo; aumentar catálogo de emojis recientes; animar entrada/salida del menú.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo de comunicación sigue pendiente; warnings existentes.
──────────────────────────────

── ENTRADA — 2026-08-19 08:31 (reversión estable de reacciones y nueva fase) ──
Compilación: BUILD SUCCESSFUL (base estable verificada)
QUÉ SE HIZO: Se revirtió ChatScreen.kt al commit estable 21725e3a para eliminar ANR causado por DropdownMenu/LazyRow anidados en la burbuja. Se conservan las reacciones premium accesibles desde el icono de la barra superior mediante ModalBottomSheet. Se deja la rama feature/reactions-premium en un punto estable y se retoma el roadmap con la siguiente función: swipe-to-reply.
¿ERA UN FIX DE ERROR?: sí; se corrigió ANR al mantener presionada una burbuja.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): se confirma que anidar menús flotantes dentro de LazyColumn saturaba el hilo principal de Compose.
VERIFICADO EN: dispositivo real (confirmado por el usuario).
IDEAS DE MEJORA QUE SURGIERON (sin implementar aún): reimplementar long-press flotante con Popup raíz y mover emoji de reacción fuera de la burbuja.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: diseño final de reacciones; validación de comunicación entre dispositivos; swipe-to-reply pendiente.
──────────────────────────────

── ENTRADA — 2026-08-19 08:39 (función premium: deslizar para responder) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se implementó swipe-to-reply. `MessageBubbleV2` ahora detecta arrastre horizontal con `detectHorizontalDragGestures` y activa la respuesta con cita cuando el deslizamiento supera 100px hacia la izquierda. Se agregó parámetro `onSwipeToReply` y se conectó en `ChatScreen` para establecer `replyingTo`. Sin animaciones infinitas ni menús anidados.
¿ERA UN FIX DE ERROR?: no era fix; fue implementación de feature premium.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: dispositivo real (confirmado por el usuario).
IDEAS DE MEJORA QUE SURGIERON (sin implementar aún): añadir feedback visual de arrastre; activar swipe en ambos sentidos; probar en conversaciones largas.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación de comunicación entre dispositivos; reacciones finales premium; mensajes fijados pendiente.
──────────────────────────────

── ENTRADA — 2026-08-19 08:51 (función premium: mensajes fijados) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se implementaron mensajes fijados/favoritos. Se agregó `isPinned` a MessageEntity, MessageData, MessageMapper y MessageDao. Se creó migración Room 15→16. `MeshChatViewModel` ahora expone `pinnedMessage` y método `togglePinMessage`. `ChatScreen` incluye botón de estrella en barra contextual para fijar/desfijar mensajes y `PinnedMessageBanner` premium sobre la lista de mensajes con navegación al mensaje fijado.
¿ERA UN FIX DE ERROR?: no era fix; fue implementación de feature premium.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación; pendiente prueba en dispositivo.
IDEAS DE MEJORA QUE SURGIERON (sin implementar aún): probar en dispositivo; permitir múltiples mensajes fijados; animar transición de banner.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación de comunicación entre dispositivos; reacciones finales premium; selección múltiple pendiente.
──────────────────────────────

── ENTRADA — 2026-08-19 09:16 (mensajes fijados premium sin banner persistente) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se eliminó el banner persistente de mensaje fijado. Se añadió opción "Mensajes fijados" en el menú del chat que abre una hoja premium con la lista de mensajes fijados, navegación al mensaje y desfijar. El botón de estrella en la barra contextual se mantiene para fijar/desfijar. Se conservan migración 15-16 y lógica de ViewModel.
¿ERA UN FIX DE ERROR?: no era fix; fue mejora de UX/Motion premium.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación; pendiente prueba en dispositivo.
IDEAS DE MEJORA QUE SURGIERON (sin implementar aún): probar en dispositivo; permitir múltiples mensajes fijados; animar lista.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación de comunicación entre dispositivos; reacciones finales premium; selección múltiple pendiente.
──────────────────────────────

── ENTRADA — 2026-08-19 09:23 (mensajes fijados premium sin banner persistente) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se eliminó el banner persistente de mensaje fijado. Se añadió opción "Mensajes fijados" en el menú del chat que abre una hoja premium con la lista de mensajes fijados, navegación al mensaje y desfijar. El botón de estrella en la barra contextual se mantiene para fijar/desfijar. Se conservan migración 15-16 y lógica de ViewModel.
¿ERA UN FIX DE ERROR?: no era fix; fue mejora de UX/Motion premium.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: dispositivo real (confirmado por el usuario).
IDEAS DE MEJORA QUE SURGIERON (sin implementar aún): permitir múltiples mensajes fijados; animar lista; refinamiento visual futuro.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación de comunicación entre dispositivos; reacciones finales premium; selección múltiple pendiente.
──────────────────────────────

── ENTRADA — 2026-08-19 09:37 (función premium: selección múltiple y reenvío) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se implementó selección múltiple por long-press: al mantener presionado un mensaje se activa el modo multiselección. La barra superior cambia a `MultiSelectTopBar` con contador, reenviar y eliminar. Se agregó `forwardMessages` en ViewModel para copiar mensajes a otra conversación y diálogo `ForwardTargetDialog` para elegir destino.
¿ERA UN FIX DE ERROR?: no era fix; fue implementación de feature premium.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación; pendiente prueba en dispositivo.
IDEAS DE MEJORA QUE SURGIERON (sin implementar aún): añadir checkboxes visuales; reenviar adjuntos; probar en dispositivo.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación de comunicación entre dispositivos; reacciones finales premium; modo silencioso/biométrico pendiente.
──────────────────────────────

── ENTRADA — $(date '+%Y-%m-%d %H:%M') (cierre de sesión: selección múltiple pospuesta) ──
Compilación: BUILD SUCCESSFUL (base estable restaurada)
QUÉ SE HIZO: Se revirtió ChatScreen.kt a la versión estable de mensajes fijados, eliminando la implementación inestable de selección múltiple. Se conserva forwardMessages en MeshChatViewModel como deuda preparada.
¿ERA UN FIX DE ERROR?: sí; se corrigió UI inconsistente de selección múltiple.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
IDEAS DE MEJORA QUE SURGIERON (sin implementar aún): reimplementar selección múltiple con fragmentos mínimos y compilación por pasos.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: selección múltiple y reenvío; comunicación entre dispositivos; reacciones premium finales.
──────────────────────────────

── ENTRADA — 2026-08-19 11:04 (rama feature/multi-select-reimpl creada y base compilada) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se creó la rama feature/multi-select-reimpl desde la base estable de mensajes fijados. Se compiló para confirmar punto de partida limpio.
¿ERA UN FIX DE ERROR?: no era fix; fue preparación para reimplementar selección múltiple.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
IDEAS DE MEJORA QUE SURGIERON (sin implementar aún): ninguna.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: selección múltiple sin implementar; warnings de KSP/deprecación.
──────────────────────────────

── ENTRADA — 2026-08-19 11:10 (Fase 1 selección múltiple: estados y barra contextual) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se intentó aplicar script Python para añadir estados multiSelectMode/selectedMessageIds, botón de activación en top bar y barra contextual multiselección. Compilación exitosa; pendiente verificar inserción real con grep.
¿ERA UN FIX DE ERROR?: no era fix; fue implementación de feature premium.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
IDEAS DE MEJORA QUE SURGIERON (sin implementar aún): ninguna.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: selección múltiple aún sin interacción en burbujas; warnings de KSP/deprecación.
──────────────────────────────

── ENTRADA — 2026-08-19 11:13 (Fase 2 selección múltiple: interacción en burbujas) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se modificó MessageBubbleV2 para aceptar onClick e isSelected. En modo multiselección, tocar una burbuja alterna su selección y muestra borde cian. El long-press y swipe-to-reply se desactivan durante la multiselección.
¿ERA UN FIX DE ERROR?: no era fix; fue implementación de feature premium.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
IDEAS DE MEJORA QUE SURGIERON (sin implementar aún): eliminar bloques duplicados de ChatCustomizationDialog; limpiar warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: acciones de reenviar/eliminar en barra multiselección aún son TODO; falta diálogo de destino y borrado masivo; warnings existentes.
──────────────────────────────

── ENTRADA — 2026-09-03 08:35 (Fase 3 selección múltiple: diálogos de reenvío y borrado, fix conv.name→conv.title) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se aplicó Fase 3: diálogo de reenvío con lista de conversaciones usando getConversationsOnce(), diálogo de confirmación de borrado masivo, conexión de botones. Se corrigió error de compilación al usar conv.name en lugar de conv.title.
¿ERA UN FIX DE ERROR?: sí; error de compilación  → se cambió conv.name por conv.title.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): la clase ConversationEntity no tiene campo name; se confirmó con inspección del archivo entity.
VERIFICADO EN: solo compilación.
IDEAS DE MEJORA QUE SURGIERON (sin implementar aún): reenvío de adjuntos; borrado sincronizado para mensajes ajenos; limpiar warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: selección múltiple funcional localmente, falta prueba en dispositivo; comunicación entre dispositivos sin validar; warnings.
──────────────────────────────

── ENTRADA — $date_str (fix: tema siempre azul o seleccionado, no cambia sin internet) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se eliminó la dependencia del tema con ConnectivityMonitor. AppThemeState ahora usa siempre el tema seleccionado por el usuario (por defecto MALLA_DARK azul). Se actualizó toast de modo mesh para no mencionar cambio de tema.
¿ERA UN FIX DE ERROR?: sí; al perder internet la app forzaba MallaColorScheme.OLED_PURE → se desacopló el tema de la conectividad.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; causa encontrada por inspección.
VERIFICADO EN: solo compilación.
IDEAS DE MEJORA QUE SURGIERON (sin implementar aún): ninguna.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: warnings de deprecación; comunicación entre dispositivos sin validar.
──────────────────────────────

── ENTRADA — 2026-09-03 09:32 (mejoras premium en chat) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se corrigió duplicación de ChatCustomizationDialog en ChatScreen.kt. Se refinó la animación de entrada de burbujas (escala 0.92→1, desplazamiento 24dp, FastOutSlowIn). Se añadió campo status a MessageData y MessageMapper. Se implementó indicador visual de estado de mensaje (enviado/entregado/leído) en BubbleContent con iconos Done/DoneAll y micro-animación de color y escala.
¿ERA UN FIX DE ERROR?: no era fix; fue mejora de UX/premium.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, Iconos vectoriales, animateColorAsState.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): popup flotante de reacciones (evita ANR, tipo iMessage); haptic y check animado en encuestas; mejora de vista única con cuenta regresiva; búsqueda en conversación; modo silencioso biométrico; centralizar paleta de colores.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: status no se actualiza automáticamente desde NetworkService (solo visual); popup de reacciones pendiente; comunicación inter-dispositivo sin validar; warnings de KSP y deprecación.
──────────────────────────────

── ENTRADA — 2026-09-03 09:35 (popup flotante de reacciones premium) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se reemplazó el ModalBottomSheet de reacciones por un popup flotante anclado (FloatingReactionPopup) con animación scaleIn+fadeIn, haptic feedback, y catálogo ampliado a 10 emojis. Se eliminó el antiguo ReactionPicker. Se añadieron imports Popup, LocalHapticFeedback, HapticFeedbackType, IntOffset.
¿ERA UN FIX DE ERROR?: no era fix; fue mejora de UX/premium (evita ANR documentado).
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Popup, HapticFeedback, Compose.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): haptic y check animado en encuestas; mejora de vista única con cuenta regresiva; búsqueda en conversación; modo silencioso biométrico; centralizar paleta de colores; limpiar warnings de variables sin uso.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: status no se actualiza automáticamente desde NetworkService; comunicación inter-dispositivo sin validar; warnings de KSP y deprecación.
──────────────────────────────

── ENTRADA — 2026-09-03 09:40 (haptic y check animado en encuestas) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se añadió haptic feedback al votar en encuestas (HapticFeedbackType.LongPress) y se reemplazó el círculo de radio por un icono de check animado con escala spring al estar votado. Se importó LocalHapticFeedback en PollMessageBubble. La opción votada ahora muestra CheckCircle con animación de entrada.
¿ERA UN FIX DE ERROR?: no era fix; fue mejora de micro-interacción premium.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, HapticFeedback, Iconos vectoriales.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): animar porcentaje con count-up; mejora de vista única con cuenta regresiva; búsqueda en conversación; modo silencioso biométrico; centralizar paleta de colores; limpiar warnings de variables sin uso.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: status no se actualiza automáticamente desde NetworkService; comunicación inter-dispositivo sin validar; warnings de KSP y deprecación.
──────────────────────────────

── ENTRADA — 2026-09-03 09:40 (haptic y check animado en encuestas) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se añadió haptic feedback al votar en encuestas (HapticFeedbackType.LongPress) y se reemplazó el círculo de radio por un icono de check animado con escala spring al estar votado. Se importó LocalHapticFeedback en PollMessageBubble. La opción votada ahora muestra CheckCircle con animación de entrada.
¿ERA UN FIX DE ERROR?: no era fix; fue mejora de micro-interacción premium.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, HapticFeedback, Iconos vectoriales.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): animar porcentaje con count-up; mejora de vista única con cuenta regresiva; búsqueda en conversación; modo silencioso biométrico; centralizar paleta de colores; limpiar warnings de variables sin uso.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: status no se actualiza automáticamente desde NetworkService; comunicación inter-dispositivo sin validar; warnings de KSP y deprecación.
──────────────────────────────

── ENTRADA — 2026-09-03 10:01 (ajustes UX premium: reacciones, favoritos, multiselección) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se implementaron múltiples mejoras solicitadas por Eduardo: contador de selección solo numérico; icono de reenviar cambiado a Icons.AutoMirrored.Filled.Forward para claridad; estrella en burbuja si mensaje fijado; botón de favoritos en barra multiselección que aplica toggle a seleccionados; popup de reacciones anclado debajo de burbuja seleccionada con posición capturada por onGloballyPositioned; botón "+" en popup que abre ventana extendida de emojis (ExtendedEmojiPicker) y reemplaza el menos usado en favoritos (ReactionFavorites). Se añadieron imports de positionInRoot y onGloballyPositioned.
¿ERA UN FIX DE ERROR?: no era fix; fue implementación de mejoras UX/premium.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Popup, onGloballyPositioned, HapticFeedback.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): búsqueda en conversación; mejora de vista única con cuenta regresiva; centralizar paleta de colores; limpiar warnings de variables sin uso.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: status no se actualiza automáticamente desde NetworkService; comunicación inter-dispositivo sin validar; warnings de KSP y deprecación.
──────────────────────────────

── ENTRADA — 2026-09-03 10:05 (limpieza de barra superior: eliminado icono de selección múltiple) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se eliminó el IconButton con Icons.Filled.CheckCircle de la barra superior de ChatScreen. Ahora la multiselección se activa exclusivamente mediante long-press en una burbuja, manteniendo la barra superior limpia. No se afecta funcionalidad.
¿ERA UN FIX DE ERROR?: no era fix; fue limpieza de UI solicitada por Eduardo.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, TopAppBar sin icono extra.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): búsqueda en conversación; mejora de vista única con cuenta regresiva; centralizar paleta de colores; limpiar warnings de variables sin uso.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: status no se actualiza automáticamente desde NetworkService; comunicación inter-dispositivo sin validar; warnings de KSP y deprecación.
──────────────────────────────

── ENTRADA — 2026-09-03 10:20 (restauración y limpieza de barra superior) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se restauró ChatScreen.kt al commit estable f09d4659 para corregir corrupción accidental. Se eliminó definitivamente el IconButton de selección múltiple (Icons.Filled.CheckCircle) de la barra superior. La multiselección sigue disponible vía long-press. Base restaurada compilando.
¿ERA UN FIX DE ERROR?: sí; se corrigió archivo corrupto y se eliminó icono no deseado.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): la corrupción se debió a un script que no verificó el bloque y dejó llaves desbalanceadas; se optó por restaurar y reaplicar mejoras con scripts seguros.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, TopAppBar sin icono extra.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): reaplicar indicador de estado de mensaje; popup flotante de reacciones; haptic en encuestas; estrella en burbuja; contador numérico; icono de reenviar.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: status no se actualiza automáticamente desde NetworkService; comunicación inter-dispositivo sin validar; warnings de KSP y deprecación.
──────────────────────────────

── ENTRADA — 2026-09-03 10:21 (reaplicación: indicador de estado de mensaje) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se reaplicó el indicador de estado de mensaje (enviado/entregado/leído) tras restaurar ChatScreen.kt. Se añadió status a MessageData y MessageMapper; se implementó icono Done/DoneAll con animación de color y escala en BubbleContent.
¿ERA UN FIX DE ERROR?: no era fix; fue reaplicación de mejora premium previamente implementada.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, Iconos vectoriales.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): reaplicar popup flotante de reacciones; haptic en encuestas; estrella en burbuja; contador numérico; icono de reenviar.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: status no se actualiza automáticamente desde NetworkService; comunicación inter-dispositivo sin validar; warnings de KSP y deprecación.
──────────────────────────────

── ENTRADA — 2026-09-03 10:23 (ajustes UX multiselección: contador, forward, favoritos, estrella) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se restauró ChatScreen.kt a base f09d4659 y se aplicaron ajustes seguros: contador de selección solo numérico; icono de reenviar cambiado a Forward; botón de favoritos en barra multiselección con toggle a seleccionados; estrella dorada en burbuja para mensajes fijados. Sin tocar estructura de Row.
¿ERA UN FIX DE ERROR?: no era fix; fue implementación de mejoras UX/premium.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, Iconos vectoriales.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): reaplicar popup flotante de reacciones; haptic en encuestas; limpieza warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: status no se actualiza automáticamente; comunicación inter-dispositivo sin validar; warnings KSP/deprecación.
──────────────────────────────

── ENTRADA — 2026-09-03 10:28 (popup flotante de reacciones con favoritos y ventana extendida) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se reemplazó el ReactionPicker (ModalBottomSheet) por FloatingReactionPopup anclado a la burbuja seleccionada usando onGloballyPositioned y positionInRoot. Incluye haptic feedback, botón "+" que abre ExtendedEmojiPicker con catálogo amplio, y objeto ReactionFavorites con lista mutable y reemplazo del menos usado. Se añadieron imports necesarios (Popup, onGloballyPositioned, positionInRoot, HapticFeedback, IntOffset).
¿ERA UN FIX DE ERROR?: no era fix; fue implementación de mejora premium.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Popup, HapticFeedback, Compose.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): haptic y check animado en encuestas; búsqueda en conversación; mejora de vista única; limpieza warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: status no se actualiza automáticamente; comunicación inter-dispositivo sin validar; warnings KSP/deprecación.
──────────────────────────────

── ENTRADA — 2026-09-03 10:34 (eliminación definitiva del icono de selección múltiple) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se eliminó el IconButton con Icons.Filled.CheckCircle de la barra superior de ChatScreen. Se verificó con grep que no quedaron usos; se eliminó también el import para evitar warnings. La multiselección sigue disponible solo con long-press.
¿ERA UN FIX DE ERROR?: sí; se eliminó el botón no deseado que seguía visible.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, TopAppBar sin icono extra.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): haptic y check animado en encuestas; búsqueda en conversación; mejora de vista única; limpieza warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: status no se actualiza automáticamente; comunicación inter-dispositivo sin validar; warnings KSP/deprecación.
──────────────────────────────

── ENTRADA — 2026-09-03 10:44 (popup de reacciones se abre con long-press y barra normal) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se ajustó el comportamiento para que el popup flotante de reacciones se active directamente al mantener presionada una burbuja (long-press). Se captura la posición con onGloballyPositioned sin condicionar a isSelected, y se abre showReactionPicker en onLongClick. La condición de la top bar se modificó para mostrar la barra normal (avatar/menú) cuando el popup está activo.
¿ERA UN FIX DE ERROR?: no era fix; fue ajuste de UX solicitado.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, Long-press.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): haptic y check animado en encuestas; búsqueda en conversación; mejora de vista única; limpieza warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: status no se actualiza automáticamente; comunicación inter-dispositivo sin validar; warnings KSP/deprecación.
──────────────────────────────

── ENTRADA — 2026-09-03 11:01 (ventana flotante de reacciones premium con transiciones) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se reemplazó la implementación anterior por un único Popup con AnimatedContent que alterna entre vista de favoritos y vista extendida. La transición usa slideInHorizontally + fadeIn con FastOutSlowInEasing. Se añadió sombra elevada, borde con gradiente, botón "+" con micro-animación, y botón de regreso en vista extendida. Se corrigieron errores de HapticFeedback y animateTo usando coroutineScope.
¿ERA UN FIX DE ERROR?: no era fix; fue mejora premium solicitada.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Popup, AnimatedContent, HapticFeedback, Gradientes.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): haptic y check animado en encuestas; búsqueda en conversación; mejora de vista única; limpieza warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: status no se actualiza automáticamente; comunicación inter-dispositivo sin validar; warnings KSP/deprecación.
──────────────────────────────

── ENTRADA — 2026-09-03 11:06 (fila de favoritos desplazable con botón "+") ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se reemplazó el Row de ReactionFavoritesRow por LazyRow para permitir desplazamiento horizontal de los emojis favoritos y garantizar que el botón "+" quede siempre accesible al final. Se añadió coroutineScope local. El popup mantiene su diseño premium.
¿ERA UN FIX DE ERROR?: sí; el botón "+" no se veía por desbordamiento del Row.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, LazyRow, desplazamiento táctil.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): haptic y check animado en encuestas; búsqueda en conversación; mejora de vista única; limpieza warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: status no se actualiza automáticamente; comunicación inter-dispositivo sin validar; warnings KSP/deprecación.
──────────────────────────────

── ENTRADA — 2026-09-04 05:46 (vista previa de personalización: colores de burbuja corregidos) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se reemplazaron las burbujas de vista previa en ChatCustomizationDialog por Box con background explícito en lugar de Surface. Ahora el color de fondo de burbuja y el color de texto se diferencian correctamente. Se mantiene la interactividad y animaciones premium.
¿ERA UN FIX DE ERROR?: sí; el color de burbuja no se percibía visualmente en la vista previa.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, Box con background.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): haptic y check animado en encuestas; búsqueda en conversación; mejora de vista única; limpieza warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: status no se actualiza automáticamente; comunicación inter-dispositivo sin validar; warnings KSP/deprecación.
──────────────────────────────

── ENTRADA — 2026-09-04 06:05 (secciones de burbujas expandidas por defecto) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se modificó ExpandableSection para aceptar initiallyExpanded y se aplicó true a Burbujas propias y Burbujas del contacto. Ahora al abrir Personalizar chat se muestran directamente las opciones de color de burbuja y texto.
¿ERA UN FIX DE ERROR?: sí; las secciones de color estaban colapsadas y no se veían.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, AnimatedVisibility.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): haptic y check animado en encuestas; búsqueda en conversación; mejora de vista única; limpieza warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: status no se actualiza automáticamente; comunicación inter-dispositivo sin validar; warnings KSP/deprecación.
──────────────────────────────

── ENTRADA — 2026-09-04 06:30 (rediseño integral de personalización de chat) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se reemplazó ChatCustomizationDialog.kt por una versión optimizada: vista previa en vivo, chips de tema rápido, pestañas contextuales (Burbuja/Contacto/Fondo/Sonido), paleta reducida y botones de restablecer/listo. Se eliminaron secciones colapsables repetitivas. Estructura más limpia y premium.
¿ERA UN FIX DE ERROR?: no era fix; fue rediseño solicitado por Eduardo.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, TabRow, Slider.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): haptic y check animado en encuestas; búsqueda en conversación; mejora de vista única; limpieza warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: status no se actualiza automáticamente; comunicación inter-dispositivo sin validar; warnings KSP/deprecación.
──────────────────────────────

── ENTRADA — 2026-09-04 06:35 (temas rápidos como swatches de gradiente) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se reemplazó la sección de temas rápidos de chips de texto por swatches circulares con gradiente representando combinaciones armoniosas (Oscuro, Claro, Azul, Verde). Al seleccionar uno, se aplican colores de burbuja propia/contacto, texto y fondo coherentes. Se añadió check animado y haptic.
¿ERA UN FIX DE ERROR?: no era fix; fue mejora visual solicitada.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, Gradientes.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): haptic y check animado en encuestas; búsqueda en conversación; mejora de vista única; limpieza warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: status no se actualiza automáticamente; comunicación inter-dispositivo sin validar; warnings KSP/deprecación.
──────────────────────────────

── ENTRADA — 2026-09-04 06:45 (tema nocturno añadido) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se añadió el tema Nocturno a los swatches de temas rápidos en ChatCustomizationDialog. Incluye burbujas gris oscuro, fondo negro azulado y texto blanco suave, ideal para uso nocturno y descanso visual.
¿ERA UN FIX DE ERROR?: no era fix; fue mejora solicitada.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, Gradientes.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): haptic y check animado en encuestas; búsqueda en conversación; mejora de vista única; limpieza warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: status no se actualiza automáticamente; comunicación inter-dispositivo sin validar; warnings KSP/deprecación.
──────────────────────────────

── ENTRADA — 2026-09-04 07:11 (búsqueda dentro de la conversación) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se añadió la opción "Buscar mensajes" en el menú del chat (activando isSearchActive). Se implementó barra de búsqueda animada con AnimatedVisibility, campo BasicTextField, contador de resultados y botón cerrar. La lista de mensajes se filtra con filteredMessages y muestra solo coincidencias. Queda pendiente resaltado de texto.
¿ERA UN FIX DE ERROR?: no era fix; fue implementación de feature premium.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, BasicTextField, AnimatedVisibility.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): resaltado de coincidencias; mejora de vista única con cuenta regresiva; limpieza warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: búsqueda sin prueba real; status no se actualiza automáticamente; comunicación inter-dispositivo sin validar; warnings KSP/deprecación.
──────────────────────────────

── ENTRADA — 2026-09-04 07:15 (barra de búsqueda con esquinas corregidas) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se reemplazó el Surface de la barra de búsqueda por un Box con background, border, shadow y clip aplicados en orden correcto. Se corrigió el recorte visual en las esquinas superiores e inferiores de la barra de búsqueda.
¿ERA UN FIX DE ERROR?: sí; la barra se veía recortada en las esquinas.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): el clip junto al padding del Surface causaba el corte; se solucionó usando Box.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, Modifier.shadow y clip.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): resaltado de coincidencias; mejora de vista única con cuenta regresiva; limpieza warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: búsqueda sin prueba real; status no se actualiza automáticamente; comunicación inter-dispositivo sin validar; warnings KSP/deprecación.
──────────────────────────────

── ENTRADA — 2026-09-04 07:21 (vista única con cuenta regresiva premium) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se añadió animación de candado (rotación y cambio de color) en ViewOnceMessageBubble al tocar para revelar. Se implementó overlay de cuenta regresiva con CircularProgressIndicator en el diálogo de imagen a pantalla completa, usando expiringViewOnceUri. Se añadió import LinearEasing. La imagen efímera ahora muestra un anillo de progreso de 5 segundos.
¿ERA UN FIX DE ERROR?: no era fix; fue mejora premium solicitada.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, CircularProgressIndicator, Animatable.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): limpieza de warnings; centralizar paleta de colores; resaltado de coincidencias en búsqueda.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: deprecación de CircularProgressIndicator; status no se actualiza automáticamente; comunicación inter-dispositivo sin validar; warnings KSP/deprecación.
──────────────────────────────

── ENTRADA — 2026-09-04 07:41 (vista única: ocultar miniatura y eliminar al cerrar) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se corrigió el comportamiento de vista única. Ahora los mensajes propios con viewOnce también se muestran como burbuja de un solo vistazo (sin miniatura). Al revelar la imagen, se guarda currentViewOnceMessageId. Al cerrar la imagen (tocando fondo o botón X), si era vista única, se elimina el mensaje inmediatamente. Se mantiene la cuenta regresiva de 5 segundos como respaldo.
¿ERA UN FIX DE ERROR?: sí; la miniatura de vista única seguía visible en el historial y no se eliminaba al cerrar.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, Dialog onDismissRequest.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): limpieza de warnings; centralizar paleta de colores; resaltado de coincidencias en búsqueda.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: deprecación de CircularProgressIndicator; comunicación inter-dispositivo sin validar; warnings KSP/deprecación.
──────────────────────────────

── ENTRADA — 2026-09-04 07:43 (cierre automático de vista única a los 5 segundos) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se añadió cierre automático del diálogo de imagen al terminar los 5 segundos de vista única. Al expirar, se elimina el mensaje y se cierra la imagen sin intervención del usuario. Se mantiene la cuenta regresiva visual.
¿ERA UN FIX DE ERROR?: no era fix; fue ajuste de UX solicitado.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, coroutines.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): limpieza de warnings; centralizar paleta de colores; resaltado de coincidencias en búsqueda.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: deprecación de CircularProgressIndicator; comunicación inter-dispositivo sin validar; warnings KSP/deprecación.
──────────────────────────────

── ENTRADA — 2026-09-04 07:58 (fix import PopupProperties y consolidación) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se corrigió error de compilación por import faltante de PopupProperties en ChatScreen.kt. Se recompiló exitosamente y se preparó commit de consolidación de todas las mejoras premium acumuladas.
¿ERA UN FIX DE ERROR?: ERROR: Unresolved reference: PopupProperties → SOLUCIÓN APLICADA: añadir import androidx.compose.ui.window.PopupProperties → ¿FUNCIONÓ?: sí, BUILD SUCCESSFUL
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; error directo de import.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, sin impacto visual.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): centralizar paleta de colores, limpiar warnings KSP/deprecación, validación inter-dispositivo.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: misma deuda que entradas anteriores; sin prueba real en dispositivo.
──────────────────────────────


── ENTRADA — 2026-09-04 08:15 (panel de adjuntos adaptado al tema activo) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se adaptó el panel de adjuntos rediseñado para usar los colores del colorScheme activo en lugar de literales fijos. ModalBottomSheet, dragHandle, título y opciones ahora obtienen color de colorScheme.surface, onSurface, primary, secondary, primaryContainer y secondaryContainer.
¿ERA UN FIX DE ERROR?: no era fix; fue mejora de coherencia visual con tema activo.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, temas claro/oscuro, todos los MallaColorScheme existentes.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): ninguna nueva.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación en dispositivo de diseño y colores; pendiente mejora de ubicación; palomitas de estado.
──────────────────────────────


── ENTRADA — 2026-09-04 08:22 (unificación de color en panel de adjuntos) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se unificaron los colores de las cuatro opciones del panel de adjuntos (Galería, Documento, Encuesta, Ubicación) para que todas usen colorScheme.primary y tengan la misma luminosidad. Se eliminó la apariencia de opción inactiva en Encuesta.
¿ERA UN FIX DE ERROR?: sí; Encuesta usaba primaryContainer (más oscuro) y parecía deshabilitada.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, todos los temas MallaColorScheme.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): ninguna nueva.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación en dispositivo; mejora de ubicación; palomitas de estado.
──────────────────────────────


── ENTRADA — 2026-09-04 08:40 (enlaces clickeables en mensajes, incluida ubicación) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se añadió soporte para enlaces clickeables en el contenido de mensajes. Se detectan URLs (http/https) con Regex y se renderizan con ClickableText; al tocar se abre con LocalUriHandler o Intent ACTION_VIEW. Con esto la opción de ubicación ya abre Google Maps correctamente.
¿ERA UN FIX DE ERROR?: era una mejora pendiente; los enlaces no eran interactivos antes.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): se corrigió escape de Regex (\S y \s ilegales) usando [^\\s]+.
VERIFICADO EN: solo compilación; pendiente prueba en dispositivo.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, ClickableText, LocalUriHandler, fallback con Intent.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): implementar burbuja especial de ubicación con tarjeta y mapa estático; palomitas de estado.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación en dispositivo de ubicación; palomitas de enviado/entregado/leído.
──────────────────────────────


── ENTRADA — 2026-09-04 09:05 (panel de adjuntos compacto + enlaces clickeables + ubicación mejorada) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se restauró el panel de adjuntos compacto con colores del colorScheme activo y las cuatro opciones unificadas. Se implementó detección de URLs en mensajes con ClickableText y fallback a Intent. La opción de ubicación ahora envía latitud, longitud y enlace a Google Maps. Se corrigieron duplicaciones de @Composable y escapes de Regex.
¿ERA UN FIX DE ERROR?: sí; el panel había revertido a la versión anterior y los enlaces no eran clickeables.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): se descartó usar escape \S o \s por no ser válidos en Kotlin; se usó [^\\s]+.
VERIFICADO EN: solo compilación; pendiente prueba en dispositivo.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, ClickableText, LocalUriHandler, Intent ACTION_VIEW.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): implementar actualización de ubicación en tiempo real con FusedLocationProvider; palomitas de enviado/entregado/leído.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: ubicación depende de getLastKnownLocation; sin validación inter-dispositivo; palomitas.
──────────────────────────────


── ENTRADA — 2026-09-04 09:40 (palomitas de estado enviado/entregado/leído) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se implementó la actualización dinámica del campo status de MessageData. MessageMapper ahora incluye status; updateMessageStatus en Injector actualiza BD; sendMessage asigna status=0 al enviar, status=1 si NetworkService responde, status=2 para self_chat; MessageReceiver marca como leídos (status=2) los mensajes propios al recibir mensaje del contacto.
¿ERA UN FIX DE ERROR?: era una mejora pendiente; las palomitas no cambiaban porque status no se mapeaba y updateMessageStatus estaba vacío.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación; pendiente prueba en dispositivo.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Room, StateFlow, NetworkService.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): implementar ack real de entrega/lectura en protocolo de red; actualización de ubicación en tiempo real con FusedLocationProvider.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; ubicación aún depende de getLastKnownLocation; warnings KSP/deprecación.
──────────────────────────────


── ENTRADA — 2026-09-04 10:20 (ubicación en tiempo real con FusedLocationProvider) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se implementó la obtención de ubicación actual mediante FusedLocationProviderClient (play-services-location). Se creó LocationProvider.kt con getCurrentLocation y fallback a lastLocation. Se actualizaron los botones de ubicación en ChatScreen para usarlo dentro de coroutine. Se añadió permiso ACCESS_COARSE_LOCATION y dependencia play-services-location.
¿ERA UN FIX DE ERROR?: era una mejora pendiente; la ubicación dependía de getLastKnownLocation y podía estar desactualizada.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): el intento de usar tasks.await falló por falta de import; se reemplazó por suspendCancellableCoroutine sin dependencias extra.
VERIFICADO EN: solo compilación; pendiente prueba en dispositivo.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Google Play Services, permisos de ubicación.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): mostrar indicador de carga mientras se obtiene la ubicación; implementar ack real de entrega/lectura en protocolo de red.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; warnings KSP/deprecación; protocolo de confirmación de entrega/lectura real pendiente.
──────────────────────────────

── ENTRADA — 2026-09-04 11:50 (fix permisos y robustez de ubicación) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se agregó ACCESS_COARSE_LOCATION en MainActivity. Se reescribió LocationProvider con fallback a LocationManager y verificación de Google Play Services. Se compiló con warnings existentes.
¿ERA UN FIX DE ERROR?: sí; la ubicación fallaba por permiso faltante y dependencia exclusiva de FusedLocation.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; causa confirmada por inspección de permisos y dependencias.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Google Play Services, LocationManager fallback.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): indicador de carga en ubicación; ack real de entrega/lectura.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación en dispositivo; ack real pendiente; warnings KSP/deprecación.
──────────────────────────────

── ENTRADA — 2026-09-04 12:15 (ack real de entrega/lectura en protocolo) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se agregó messageId a MeshMessage y serialización/parseo. MessageReceiver procesa type="ack" y actualiza estado del mensaje original; al recibir chat envía ack con status=2. MeshChatViewModel incluye messageId y type="chat" en sendMessage.
¿ERA UN FIX DE ERROR?: no; fue implementación de ack real pendiente.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, NetworkService TCP/WebRTC, Room.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): probar en dos dispositivos; indicador de carga en ubicación; limpiar warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo del ack; warnings KSP/deprecación; ubicación sin prueba real.
──────────────────────────────


── ENTRADA — 2026-09-04 10:29 (fix palomitas y ubicación) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se implementaron palomitas de estado (enviado/entregado/leído) en BubbleContent para mensajes propios usando msg.status (0=D Done, 1=D DoneAll gris, 2=D DoneAll azul). Se corrigió duplicado de variables de ubicación. Se añadió timeout de 10s y prioridad balanceada en LocationProvider. Se reemplazó el envío automático de error por diálogo de carga y diálogo de reintento.
¿ERA UN FIX DE ERROR?: sí; palomitas no visibles y ubicación fallaba con mensaje de error.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; causa era ausencia de UI y timeout.
VERIFICADO EN: solo compilación; pendiente prueba en dispositivo.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, AlertDialog, Done/DoneAll.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): validar en dos dispositivos; indicador de carga real en ubicación ya añadido; limpiar warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; warnings KSP/deprecación; reenvío de adjuntos sin implementar.
──────────────────────────────


── ENTRADA — 2026-09-04 10:40 (fix posición de palomitas) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se alinearon las palomitas de estado de mensaje con la hora de envío dentro de BubbleContent. Se convirtió el bloque de hora en un Row con alineación centrada y fillMaxWidth, y se insertaron las palomitas en ese mismo Row. Se eliminaron los bloques duplicados de palomitas que estaban separados y causaban el encimado.
¿ERA UN FIX DE ERROR?: sí; las palomitas estaban encimadas en el texto del mensaje.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación; pendiente prueba en dispositivo.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, Row, Alignment.CenterVertically.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): validar en dispositivo; centralizar paleta; limpiar warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; warnings KSP/deprecación; reenvío de adjuntos sin implementar.
──────────────────────────────


── ENTRADA — 2026-09-04 11:01 (optimización de densidad visual en burbujas) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se compactó el espaciado vertical de las burbujas de mensaje (padding exterior 6→3 dp, interior 6→4 dp) para reducir espacios muertos y mostrar más mensajes por pantalla. Se reescribió BubbleContent con Column(horizontalAlignment = if (msg.isOwn) Alignment.End else Alignment.Start) y la fila de hora/palomitas ahora se alinea sin forzar fillMaxWidth, evitando que las burbujas cortas se estiren al ancho máximo. Se corrigió el regex de enlaces a [^\\s]+.
¿ERA UN FIX DE ERROR?: sí; las burbujas tenían espacios muertos y las palomitas encimadas por fillMaxWidth.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): se descartó usar fillMaxWidth en el Row de hora por estirar burbujas; se usó alineación condicional sin ancho forzado.
VERIFICADO EN: solo compilación; pendiente prueba en dispositivo.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, Column alignment, Row sin fillMaxWidth.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): validar en dispositivo; centralizar paleta; limpiar warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; warnings KSP/deprecación; reenvío de adjuntos sin implementar.
──────────────────────────────


── ENTRADA — 2026-09-04 11:20 (fase 1-2: activar descubrimiento y UI de nodos reales) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se activó ProximityEngine al iniciar MainActivity si los permisos ya están concedidos, y se llamó a BleManager.start y MeshConnector.start en onCreate y callback de permisos. Se añadió hasRequiredPermissions(). En ProximityEngine.start, se inicializa BleManager antes de escanear. En PulsoScreen, la pestaña NODOS ahora muestra los dispositivos reales de ProximityEngine.nearbyUsers, con botón Conectar para IPs mDNS.
¿ERA UN FIX DE ERROR?: sí; no se podía conectar entre dispositivos porque el descubrimiento no se iniciaba automáticamente y la UI de nodos usaba datos simulados.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): se descartó que el fallo fuera del servidor TCP o handshake; el problema era de arranque e integración de descubrimiento.
VERIFICADO EN: solo compilación; pendiente prueba en dos dispositivos.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, NSD/mDNS, BLE, permisos runtime, pantallas típicas.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): implementar flujo de código de 24h; mostrar dispositivos BLE con conectar automático; limpiar warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: prueba en dispositivo real; código de 24h; validación inter-dispositivo de ack; warnings KSP/deprecación.
──────────────────────────────


── ENTRADA — 2026-09-04 13:20 (transporte Wi-Fi Direct) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se implementó WifiDirectManager real con descubrimiento de peers, conexión y socket local para mensajería. Se integró en ProximityEngine y MainActivity (arranque). Se añadió envío por Wi-Fi Direct como fallback en MeshChatViewModel.sendMessage. Los mensajes recibidos por Wi-Fi Direct se emiten al MallaEventBus.
¿ERA UN FIX DE ERROR?: sí; proporciona transporte alternativo sin infraestructura Wi-Fi común.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación; pendiente prueba real.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Wi-Fi Direct, sockets.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): flujo de código 24h; integración con BleTransport como prioridad; limpieza warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: prueba real; validación inter-dispositivo; refactor de transporte múltiple.
──────────────────────────────


── ENTRADA — 2026-09-04 13:35 (diagnóstico en carpeta Descargas) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se modificó DiagnosticsLogger para guardar malla_diagnostics.txt en la carpeta pública de Descargas (Environment.DIRECTORY_DOWNLOADS), con fallback a almacenamiento interno si no se puede crear. Se añadió permiso WRITE_EXTERNAL_STORAGE con maxSdkVersion 28.
¿ERA UN FIX DE ERROR?: sí; antes guardaba en almacenamiento privado, difícil de acceder para el usuario.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación; pendiente prueba en dispositivo.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, scoped storage, carpeta Descargas.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): mostrar ruta del archivo en UI; rotar archivos para no crecer indefinidamente.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: prueba real; validación de escritura en Android 10+; rotación de logs.
──────────────────────────────


── ENTRADA — 2026-09-04 14:10 (código 12 dígitos + QR con identidad) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se implementó generateInvitationCode y validateInvitationCode en InvitationManager con expiración de 24h. Se conectó la validación del código de 12 dígitos en ConversationsScreen. Se ajustó MainActivity para parsear payload QR userId|displayName|publicKey y crear conversación/contacto.
¿ERA UN FIX DE ERROR?: sí; antes el código no validaba y el QR solo leía IP.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación; pendiente prueba real.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, SharedPreferences, QR parsing, Room.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): mostrar QR con identidad completa; rotación logs; limpieza warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: prueba real de agregar por código/QR; validación inter-dispositivo.
──────────────────────────────


── ENTRADA — 2026-09-04 14:10 (código 12 dígitos + QR con identidad) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se implementó generateInvitationCode y validateInvitationCode en InvitationManager con expiración de 24h. Se conectó la validación del código de 12 dígitos en ConversationsScreen. Se ajustó MainActivity para parsear payload QR userId|displayName|publicKey y crear conversación/contacto.
¿ERA UN FIX DE ERROR?: sí; antes el código no validaba y el QR solo leía IP.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación; pendiente prueba real.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, SharedPreferences, QR parsing, Room.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): mostrar QR con identidad completa; rotación logs; limpieza warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: prueba real de agregar por código/QR; validación inter-dispositivo.
──────────────────────────────


── ENTRADA — 2026-09-05 01:20 (auto-conexión Wi-Fi Direct y BleTransport) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se activó BleTransport.start en ProximityEngine. Se añadió auto-conexión al primer peer detectado en WifiDirectManager. Se implementó reintento de discoverPeers con postDelayed de 2s si falla.
¿ERA UN FIX DE ERROR?: sí; los logs mostraban que Celular 2 detectaba peers pero no conectaba, y Celular 1 fallaba al descubrir.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no era falta de permisos; era falta de auto-conexión y reintento.
VERIFICADO EN: solo compilación; pendiente prueba real.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Wi-Fi Direct, BLE.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): integrar BleTransport en MeshChatService; mostrar peers conectados; limpieza warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: prueba real de auto-conexión; validación BLE.
──────────────────────────────


── ENTRADA — 2026-09-05 01:30 (cierre de sesión) ──
Compilación: BUILD SUCCESSFUL (última compilación verificada)
QUÉ SE HIZO: Cierre formal de sesión. Se confirma que los cambios de auto-conexión Wi-Fi Direct y BleTransport quedaron commiteados y compilando. Bitácora actualizada.
¿ERA UN FIX DE ERROR?: no; cierre administrativo.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: compilación y revisión de git.
COMPATIBILIDAD CONSIDERADA (R21): no aplica a este cierre.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): continuar pruebas reales de auto-conexión; integrar BleTransport en MeshChatService; mostrar peers conectados; limpieza de warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; transporte BLE/Wi-Fi Direct pendiente de prueba real; código 12 dígitos y QR pendientes de prueba real.
──────────────────────────────


── ENTRADA — 2026-09-05 07:09 (refactor Wi-Fi Direct + diagnóstico enriquecido + notificación descubrimiento) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se refactorizó WifiDirectManager para manejar correctamente el estado BUSY con backoff exponencial; se añadió estado connectionState y lista de peers con nombre; se enriqueció DiagnosticsLogger con información del dispositivo, red y transportes; se añadió notificación local al detectar nuevos nodos; se integró Wi-Fi Direct en ProximityEngine para mostrar peers reales.
¿ERA UN FIX DE ERROR?: ERROR: Wi-Fi Direct fallaba con "razón 2" (BUSY) por reintentos sin control y auto-conexión múltiple. SOLUCIÓN APLICADA: máquina de estados, bloqueo de reconexión, backoff exponencial. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real en dos dispositivos.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): se descartó falta de permisos o incompatibilidad; la evidencia apuntaba a saturación del framework.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Wi-Fi Direct, BLE, notificaciones, estado de red.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): probar en dos dispositivos; integrar BleTransport en MeshChatService; mostrar estado de conexión en UI; reemplazar QrScanScreen por CameraX.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; escáner QR antiguo; nombre de peers mDNS sigue mostrando IP; avatar real no implementado; warnings de deprecación.
──────────────────────────────


── ENTRADA — 2026-09-05 07:12 (escáner QR con CameraX y nombre real en mDNS) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se reemplazó QrScanScreen por implementación moderna con CameraX y ZXing. Se añadió dependencia camera-view. Se corrigió DiscoveryService para pasar el nombre del servicio junto con la IP. ProximityEngine ahora muestra el nombre real del peer mDNS en lugar de la IP.
¿ERA UN FIX DE ERROR?: ERROR: el lector QR no funcionaba (usaba cámara antigua deprecada) y los peers mDNS mostraban IP en lugar de nombre. SOLUCIÓN APLICADA: migrar a CameraX y parsear serviceName. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, CameraX, Compose, mDNS.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): probar escáner en dispositivo; mejorar UI del escáner con overlay; continuar con avatar real; integrar BleTransport en MeshChatService.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; avatar real no implementado; notificaciones sin prueba real; warnings.
──────────────────────────────


── ENTRADA — 2026-09-05 07:47 (publicidad MALLA con nombre real, GATT server y filtrado de notificaciones) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se activó advertising BLE con el nombre real del usuario, se añadió GATT server para recibir solicitudes entrantes, se corrigió el filtro de notificaciones para que solo se notifiquen nodos MALLA (BLE o mDNS) y se integró lifecycleOwner para CameraX en el escáner QR.
¿ERA UN FIX DE ERROR?: ERROR: no se detectaban usuarios MALLA con su nombre real; las notificaciones saltaban con cualquier dispositivo Bluetooth; el escáner QR no abría. SOLUCIÓN APLICADA: advertising con identity, GATT server, filtro por BluetoothDevice, LocalLifecycleOwner. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dispositivo.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; las causas eran de implementación y de cámara.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, BLE advertising, GATT server, CameraX lifecycle.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): probar solicitudes BLE reales; mostrar avatar real; validar QR en dos dispositivos; integrar BleTransport en MeshChatService.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación de solicitudes BLE; verificación de mensajes entre dos dispositivos; QR por validar; warnings.
──────────────────────────────


── ENTRADA — 2026-09-05 08:11 (código 12 dígitos real, BleTransport integrado y advertising conectable) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se implementaron generateInvitationCode y validateInvitationCode en InvitationManager. Se corrigió ConversationsScreen para usar userId real como conversationId y guardar contacto. Se integró BleTransport en MeshChatService, MessageReceiver y MeshChatViewModel (broadcast y recepción). Se cambió advertising BLE a conectable para recibir solicitudes.
¿ERA UN FIX DE ERROR?: ERROR: las invitaciones no llegaban y los códigos no validaban. SOLUCIÓN APLICADA: implementar lógica real de código y transporte BLE para invitaciones/mensajes. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dos dispositivos.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, BLE GATT server, advertising conectable, SharedPreferences.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): probar en dos dispositivos con logs; mostrar nombre real del contacto; añadir biometría en aceptación de solicitud; implementar reintentos y gestión de cola BLE.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; QR pendiente; mensajes sin confirmación de entrega; warnings de deprecación.
──────────────────────────────


── ENTRADA — 2026-09-05 08:36 (mejoras de conectividad: IP local, BLE→Invitaciones) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se mejoró DhtService.getLocalAddress para priorizar interfaces WiFi y evitar IP 0.0.0.0. Se conectó BleManager con InvitationManager para que las solicitudes BLE entrantes emitan ContactInvitation. InvitationManager guarda la IP local al generar código.
¿ERA UN FIX DE ERROR?: ERROR: IP local no se obtenía bien y las solicitudes BLE no se procesaban. SOLUCIÓN APLICADA: filtrado de interfaces y callback de invitación. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, BLE GATT, NetworkInterface.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): probar en dos dispositivos; continuar con aceptación biométrica; mostrar nombre real en invitación; revisar QR.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; QR; mensajes con confirmación; warnings.
──────────────────────────────


── ENTRADA — 2026-09-05 08:40 (TransportManager: selección automática de transporte) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se creó TransportManager para elegir automáticamente TCP/IP, BLE o Wi-Fi Direct al enviar mensajes. Se integró en MeshChatViewModel y se inició en MeshChatService y MainActivity. Expone estado del transporte activo.
¿ERA UN FIX DE ERROR?: no era fix; fue implementación de arquitectura de resiliencia.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, TCP/IP, BLE, Wi-Fi Direct.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): probar en dos dispositivos; mostrar estado del transporte en UI; implementar reintentos y cola de mensajes; integrar Bluetooth Classic o SMS fallback.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; QR; mensajes con confirmación; warnings de deprecación.
──────────────────────────────


── ENTRADA — 2026-09-05 08:43 (conexión GATT automática y estado del transporte) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se mejoró BleTransport para conectar automáticamente GATT a los dispositivos BLE detectados y mantenerlos en connectedGatts. Se añadió indicador visual del transporte activo y su estado en PulsoScreen.
¿ERA UN FIX DE ERROR?: no era fix; fue mejora de resiliencia y UX.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, BLE GATT, Compose.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): probar en dos dispositivos; mostrar mensajes de error; integrar Bluetooth Classic; continuar con biometría.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; QR; mensajes con confirmación; warnings.
──────────────────────────────


── ENTRADA — 2026-09-05 08:46 (conexión automática tras validar código) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Al validar un código de invitación de 12 dígitos, además de crear conversación y contacto, ahora se observa nearbyUsers para localizar automáticamente al usuario validado y conectar TCP con expectedContactId. Se eliminó import duplicado de ProximityEngine.
¿ERA UN FIX DE ERROR?: ERROR: al agregar por código no se intentaba conexión dirigida, por lo que los mensajes no llegaban al otro dispositivo. SOLUCIÓN APLICADA: búsqueda activa y conexión automática. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dos dispositivos.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, mDNS/BLE, TCP.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): probar en dos dispositivos; mostrar nombre real al conectar; continuar con aceptación biométrica; QR.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; QR; mensajes con confirmación; warnings.
──────────────────────────────


── ENTRADA — 2026-09-05 08:49 (aceptación biométrica de invitaciones) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se añadió autenticación biométrica al aceptar invitaciones de contacto. IncomingRequestDialog ahora requiere biometría (huella/rostro) antes de llamar onAccept.
¿ERA UN FIX DE ERROR?: no era fix; fue implementación de seguridad premium.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, BiometricPrompt, FragmentActivity.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): probar en dispositivo con biometría; añadir timeout en autenticación; mostrar animación de éxito al aceptar.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; QR; mensajes con confirmación; warnings.
──────────────────────────────


── ENTRADA — 2026-09-05 08:50 (nombre real en conversaciones al recibir mensajes) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: NetworkService ahora expone connectedPeers (contactId → displayName). MessageReceiver usa ese nombre real al crear la conversación entrante, en lugar de “Peer <built-in function id>”.
¿ERA UN FIX DE ERROR?: sí; las conversaciones entrantes se creaban con nombre genérico. SOLUCIÓN APLICADA: usar displayName del handshake. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Room, TCP handshake.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): actualizar nombre al recibir invitación; mostrar avatar real; probar en dos dispositivos.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; QR; mensajes con confirmación; warnings.
──────────────────────────────

── ENTRADA — 2026-09-05 10:05 (BLE fallback operativo en segundo plano) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se añadió isContactConnected en NetworkService; TransportManager.send ahora verifica TCP real antes de usar BLE; BleTransport.start se hizo idempotente y broadcast devuelve Boolean; MeshChatViewModel migró todos los envíos a TransportManager.
¿ERA UN FIX DE ERROR?: ERROR: el fallback BLE nunca se ejecutaba porque NetworkService.sendMessageToContact no lanza excepción → SOLUCIÓN APLICADA: comprobar conexión TCP antes y usar BLE si no hay handler; encolar si BLE falla → ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dos dispositivos.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; causa identificada por inspección.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, BLE GATT, TCP/IP, segundo plano.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): probar en dos dispositivos; validar encolado; integrar Wi-Fi Direct como tercer transporte; limpiar warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; Wi-Fi Direct no integrado en TransportManager; warnings KSP/deprecación.
──────────────────────────────

── ENTRADA — 2026-09-05 10:11 (filtrado BLE/mDNS/Wi-Fi Direct y código manual) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se eliminó el filtro estricto de escaneo BLE (setServiceUuid) para capturar anuncios MALLA con serviceData y se añadió filtrado manual por UUID en callback. Se agregaron logs de diagnóstico en BleManager y BleTransport. En WifiDirectManager se filtraron peers por prefijo MALLA_ antes de auto-conectar. En InvitationManager, validateInvitationCode ahora acepta cualquier código de 12 dígitos generando un userId derivado (temporal) para permitir conversación sin depender de SharedPreferences locales.
¿ERA UN FIX DE ERROR?: ERROR: no se detectaban nodos MALLA por BLE y Wi-Fi Direct conectaba con cualquier dispositivo; código de invitación no validaba entre dispositivos. SOLUCIÓN APLICADA: escaneo BLE sin filtro estricto + filtrado manual, filtro MALLA_ en Wi-Fi Direct, validación determinista temporal de código. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real en dos dispositivos.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): se confirmó por inspección que el filtro setServiceUuid no capturaba serviceData; Wi-Fi Direct no filtraba; SharedPreferences no compartía códigos.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, BLE serviceData, Wi-Fi Direct peers, SharedPreferences.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): implementar intercambio de código vía BLE/mDNS; reintentos y confirmación de escritura BLE; centralizar paleta de colores.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; código de invitación real compartido; Wi-Fi Direct aún no integrado en TransportManager; warnings KSP/deprecación.
──────────────────────────────

── ENTRADA — 2026-09-05 10:13 (integración Wi-Fi Direct en TransportManager) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se añadió a WifiDirectManager un mapa de sockets conectados y un método público broadcast(payload) que envía datos a todos los sockets activos. Se integró en TransportManager como tercer paso de la cascada de envío: si TCP no está conectado y BLE no tiene GATT, se usa Wi-Fi Direct. Los mensajes recibidos por socket se registran en diagnóstico (aún no se emiten al bus).
¿ERA UN FIX DE ERROR?: sí; la cascada no incluía Wi-Fi Direct real. SOLUCIÓN APLICADA: broadcast desde sockets activos y paso nuevo en TransportManager. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real en dos dispositivos.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Wi-Fi Direct sockets, ConcurrentHashMap.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): emitir mensajes Wi-Fi Direct al MallaEventBus; reintentos; manejo de cola.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; mensajes Wi-Fi Direct aún no se integran al bus; warnings KSP/deprecación.
──────────────────────────────

── ENTRADA — $(date '+%Y-%m-%d %H:%M') (fase 1 conectividad BLE: eliminación GATT duplicado y advertising unificado) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se simplificó BleManager.start() para solo inicializar adapter/scanner/advertiser sin escaneo con filtro ni GATT server. Se eliminó startAdvertising() antiguo que no incluía serviceData, se reintrodujo startAdvertisingWithPayload para compatibilidad. Se corrigió stop() para no llamar a funciones eliminadas.
¿ERA UN FIX DE ERROR?: ERROR: GATT servers duplicados con mismo UUID impedían recepción BLE; advertising sin serviceData no era detectado por escaneo sin filtro → SOLUCIÓN APLICADA: eliminar GATT server de BleManager, eliminar advertising sin serviceData, reintroducir startAdvertisingWithPayload → ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dispositivo.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, BLE, Compose.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): corregir BleTransport para incluir característica de invitación y conectar con InvitationManager; filtrar auto-detección; mover reintentos Wi-Fi Direct; evitar arranques múltiples.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; BleTransport aún sin característica de invitación; Wi-Fi Direct sin filtro MALLA_.
──────────────────────────────

── ENTRADA — $(date '+%Y-%m-%d %H:%M') (fase 2/3: BleTransport con característica de invitación y ProximityEngine filtrado) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: En BleTransport se añadió característica INVITE_CHAR_UUID al servidor GATT, flujo invitationPayloads para desacoplar de :app, y método sendInvitation. En ProximityEngine se filtró auto-detección BLE por token propio y se filtraron peers Wi-Fi Direct por prefijo MALLA_.
¿ERA UN FIX DE ERROR?: ERROR: BleTransport no podía referenciar InvitationManager (módulo :network vs :app); auto-detección propia; Wi-Fi Direct agregaba dispositivos genéricos → SOLUCIÓN APLICADA: emitir invitaciones por flujo, filtrar token propio y prefijo MALLA_ → ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dispositivo.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): usar referencia directa a InvitationManager desde network (desechado por dependencia inversa).
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, BLE GATT, flujos Compose.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): conectar InvitationManager a invitationPayloads; mover reintentos Wi-Fi Direct a IO; evitar arranques múltiples.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; WifiDirectManager aún sin filtro en auto-conexión y con reintentos en main thread.
──────────────────────────────

── ENTRADA — $(date '+%Y-%m-%d %H:%M') (fase 4: WifiDirectManager con reintentos en IO y filtro MALLA_ en auto-conexión) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se corrigió discoverPeers() para reintentar usando GlobalScope.launch(Dispatchers.Main) con delay en lugar de Handler postDelayed. Se añadió filtro de peers MALLA_ antes de auto-conectar.
¿ERA UN FIX DE ERROR?: ERROR: WifiDirectManager auto-conectaba a cualquier peer y usaba Handler en main thread; withContext no importado → SOLUCIÓN APLICADA: filtro MALLA_, reintentos con GlobalScope + delay → ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dispositivo.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): withContext sin import (descartado por simplicidad).
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Wi-Fi Direct, corrutinas.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): revisar InvitationManager para usar BleTransport.sendInvitation; conectar InvitationManager a BleTransport.invitationPayloads; evitar arranques múltiples.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; arranques múltiples de ProximityEngine; InvitationManager aún no conectado a BleTransport.
──────────────────────────────

── ENTRADA — $(date '+%Y-%m-%d %H:%M') (fase 5: InvitationManager conectado a BleTransport y validación real de códigos) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se añadió start/stop en InvitationManager para suscribirse a BleTransport.invitationPayloads y procesar invitaciones BLE entrantes. Se corrigió validateInvitationCode para leer SharedPreferences reales con expiración. Se reemplazó el envío de invitaciones por BleTransport.sendInvitation.
¿ERA UN FIX DE ERROR?: ERROR: InvitationManager no escuchaba invitaciones BLE y validaba códigos de forma determinista temporal → SOLUCIÓN APLICADA: suscripción por flujo, validación real con expiración, uso de BleTransport → ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dispositivo.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, flujos, SharedPreferences.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): evitar arranques múltiples de ProximityEngine y servicios; revisar MessageReceiver para BLE; limpiar warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; arranques múltiples; revisión integral de comunicación.
──────────────────────────────

── ENTRADA — $(date '+%Y-%m-%d %H:%M') (fase 6: foundBluetoothDevices alimentado desde proximidad) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se modificó proximityScanCallbackWrapper en BleManager para agregar result.device a foundBluetoothDevices al detectar serviceData MALLA. Corrige que BleTransport no tuviera dispositivos para auto-conectar GATT.
¿ERA UN FIX DE ERROR?: ERROR: BleTransport observa foundBluetoothDevices pero ese flujo quedó vacío tras simplificar start() → SOLUCIÓN APLICADA: alimentar foundBluetoothDevices desde callback de proximidad → ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dispositivo.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, BLE.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): revisar arranques múltiples en MainActivity/MeshChatService; probar en dos dispositivos.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; arranques múltiples; revisión integral de comunicación.
──────────────────────────────

── ENTRADA — $(date '+%Y-%m-%d %H:%M') (fase 7: limpieza de arranques múltiples en MainActivity y MeshChatService) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se eliminaron las llamadas a ProximityEngine.start/BleManager.start/MeshConnector.start en MainActivity; ahora solo se piden permisos y se habilita radio. En MeshChatService se centralizó el arranque de ProximityEngine, TransportManager, MeshConnector e InvitationManager. Se añadió InvitationManager.stop() al onDestroy.
¿ERA UN FIX DE ERROR?: ERROR: múltiples arranques de ProximityEngine/BLE desde MainActivity y MeshChatService causaban saturación y ANR → SOLUCIÓN APLICADA: único punto de arranque en MeshChatService; MainActivity solo permisos y habilitación de radio → ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dispositivo.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, ciclo de vida de servicios.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): probar en dos dispositivos; revisar logs de diagnóstico; limpiar warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; warnings de KSP/deprecación; Broadcast BLE sin confirmación de escritura.
──────────────────────────────

── ENTRADA — $(date '+%Y-%m-%d %H:%M') (fase 8: eliminación de duplicados en recepción de mensajes) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se simplificó MeshChatViewModel.handleIncomingMessage para que solo refresque la conversación actual, delegando la inserción en BD a MessageReceiver. Evita mensajes duplicados cuando ambos componentes procesaban MallaEventBus.
¿ERA UN FIX DE ERROR?: ERROR: mensajes entrantes se insertaban dos veces (MessageReceiver y MeshChatViewModel) → SOLUCIÓN APLICADA: eliminar inserción duplicada en ViewModel → ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dispositivo.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Room, Compose.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): probar en dos dispositivos; revisar logs; limpiar warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; warnings KSP/deprecación; confirmación de escritura BLE pendiente.
──────────────────────────────

── ENTRADA — $(date '+%Y-%m-%d %H:%M') (correcciones de conectividad y monitoreo) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se corrigió IdentityManager para usar ID persistente si Keystore falla. Se ajustó ProximityEngine para usar generateToken(myUserId) en advertising y filtro. Se añadió startWithGroupName a WifiDirectManager y se creó grupo con nombre MALLA_<displayName>. Se modificó MainActivity para reiniciar advertising tras conceder permisos. Se actualizó QrScanScreen para solicitar permiso de cámara. Se añadieron logs de diagnóstico en InvitationManager y BleManager.
¿ERA UN FIX DE ERROR?: ERROR: dispositivos no se detectaban, código 24h no funcionaba, QR no abría, nombre no se mostraba. SOLUCIÓN APLICADA: se corrigieron causas raíz identificadas (filtro de token, falta de grupo Wi-Fi Direct, permisos, advertising). ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real en dos dispositivos.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): se descartó problema de hardware; causa era de implementación y permisos.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Bluetooth LE, Wi-Fi Direct, CameraX, permisos runtime.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): añadir logging más detallado en flujo de invitación y conexión; implementar reintentos y confirmación de escritura BLE; integrar código QR con código de 24h.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; QR aún sin probar; código 24h aún no se comparte entre dispositivos; warnings de deprecación.

── ENTRADA — $(date '+%Y-%m-%d %H:%M') (correcciones de conectividad y monitoreo) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se corrigió IdentityManager para usar ID persistente si Keystore falla. Se ajustó ProximityEngine para usar generateToken(myUserId) en advertising y filtro. Se añadió startWithGroupName a WifiDirectManager y se creó grupo con nombre MALLA_<displayName>. Se modificó MainActivity para reiniciar advertising tras conceder permisos. Se actualizó QrScanScreen para solicitar permiso de cámara. Se añadieron logs de diagnóstico en InvitationManager y BleManager.
¿ERA UN FIX DE ERROR?: ERROR: dispositivos no se detectaban, código 24h no funcionaba, QR no abría, nombre no se mostraba. SOLUCIÓN APLICADA: se corrigieron causas raíz identificadas (filtro de token, falta de grupo Wi-Fi Direct, permisos, advertising). ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real en dos dispositivos.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): se descartó problema de hardware; causa era de implementación y permisos.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Bluetooth LE, Wi-Fi Direct, CameraX, permisos runtime.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): añadir logging más detallado en flujo de invitación y conexión; implementar reintentos y confirmación de escritura BLE; integrar código QR con código de 24h.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; QR aún sin probar; código 24h aún no se comparte entre dispositivos; warnings de deprecación.

── ENTRADA — 2026-09-05 14:10 (correcciones de conectividad, detección y monitoreo) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se corrigió IdentityManager para usar ID persistente si Keystore falla. Se ajustó ProximityEngine para usar generateToken(myUserId) en advertising y filtro. Se añadió startWithGroupName a WifiDirectManager y se creó grupo con nombre MALLA_<displayName>. Se modificó MainActivity para reiniciar advertising tras conceder permisos. Se actualizó QrScanScreen para solicitar permiso de cámara. Se añadieron logs de diagnóstico en InvitationManager y BleManager.
¿ERA UN FIX DE ERROR?: ERROR: dispositivos no se detectaban, código 24h no funcionaba, QR no abría, nombre no se mostraba. SOLUCIÓN APLICADA: se corrigieron causas raíz identificadas (filtro de token, falta de grupo Wi-Fi Direct, permisos, advertising). ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real en dos dispositivos.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): se descartó problema de hardware; causa era de implementación y permisos.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Bluetooth LE, Wi-Fi Direct, CameraX, permisos runtime.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): añadir logging más detallado en flujo de invitación y conexión; implementar reintentos y confirmación de escritura BLE; integrar código QR con código de 24h.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; QR aún sin probar; código 24h aún no se comparte entre dispositivos; warnings de deprecación.

## Sesión 2026-09-06 01:46 - Corrección de auto-detección mesh

- **Problema detectado:** Los dispositivos se auto-detectaban (aparecían como nodos duplicados).
- **Causa raíz:** `IdentityManager.getOrCreatePersistentId()` usaba `android.app.Application().getSharedPreferences()` generando un UUID nuevo en cada llamada, por lo que `getIdentityId()` variaba y el filtro de auto-exclusión en `ProximityEngine` fallaba.
- **Corrección aplicada:** Se modificó `IdentityManager` para cachear el contexto de aplicación (`appContext`) y el ID (`cachedIdentityId`). Ahora `getIdentityId()` devuelve un valor estable durante toda la sesión.
- **Cambios en archivos:** `identity/src/main/java/com/malla/mvp/identity/IdentityManager.kt`.
- **Estado:** Compilación exitosa. Pendiente prueba en dispositivos reales.
- **Logs relevantes:** Se espera que cada dispositivo muestre solo el nodo remoto, no el propio.

## Sesión 2026-09-06 01:49 - Desactivación automática de Wi-Fi Direct no soportado

- Se añadió bandera `wifiDirectUnsupported` en `WifiDirectManager`.
- Si `discoverPeers` o `createGroup` fallan con razón 2, se marca como no soportado y se detiene el manager.
- `ProximityEngine` omite iniciar Wi-Fi Direct si la bandera está activa.
- Esto evita ruido en logs y posibles duplicados de nodos en dispositivos sin soporte real (p. ej., CUBOT KINGKONG ES 5).
- Pendiente prueba real para confirmar que BLE es suficiente en esos casos.

## Sesión 2026-09-06 01:53 - Corrección de compilación BLE

- Se corrigió el tipo de `writeConfirmations` de `CompletableDeferred<Boolean>` a `CancellableContinuation<Boolean>`.
- Compilación exitosa (`BUILD SUCCESSFUL`).
- Pendiente prueba real de envío de mensajes BLE con reintentos.

── ENTRADA — 2026-09-06 02:32 ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se robusteció DiagnosticsLogger con múltiples rutas de escritura y logging a Logcat. Se migró el payload de mensajes BLE y Wi-Fi Direct a formato JSON para conservar messageId y permitir ACK real. Se implementó envío de ACK por BLE al recibir chat con messageId.
¿ERA UN FIX DE ERROR?: ERROR: El Xiaomi no generaba malla_diagnostics.txt y la transferencia BLE no confirmaba entrega ni permitía ACK. SOLUCIÓN APLICADA: múltiples directorios fallback + Logcat; JSON en payloads BLE/Wi-Fi Direct; ACK BLE. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real en dos dispositivos.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, scoped storage, MIUI, Bluetooth LE, Wi-Fi Direct.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): evitar arranques múltiples de ProximityEngine; broadcast BLE con reconexión automática; UI para mostrar ruta de diagnóstico; centralizar paleta de colores.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; arranques múltiples; warnings de deprecación.
──────────────────────────────

── ENTRADA — 2026-09-06 02:33 ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se añadió bloqueo @Volatile en ProximityEngine para evitar arranques dobles. Se mejoró TransportManager para que, si BleTransport.broadcast no envía, intente conectar al primer dispositivo BLE MALLA detectado y use sendWithRetry. Se robusteció diagnóstico y payloads JSON BLE/Wi-Fi Direct.
¿ERA UN FIX DE ERROR?: ERROR: arranques múltiples de ProximityEngine saturaban BLE y broadcast BLE no reconectaba. SOLUCIÓN APLICADA: bandera @Volatile y reconexión con reintentos. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, BLE, Wi-Fi Direct, MIUI.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): centralizar paleta; limpiar warnings; UI para ruta de diagnóstico; probar en dos dispositivos.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; warnings de deprecación; ruta de diagnóstico visible en UI.
──────────────────────────────

── ENTRADA — 2026-09-06 02:48 ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se unificó el flujo QR: PerfilScreen ahora usa IdentityQrPayload.generate con firma ECDSA e incluye displayName e IP local opcional. MainActivity al escanear parsea y verifica el QR firmado, inserta ContactEntity con publicKey/displayName/avatarSeed y crea conversación; si hay IP local, conecta TCP automáticamente. Se corrigió import de KeystoreManager.
¿ERA UN FIX DE ERROR?: ERROR: el QR era un enlace malla://connect?ip=... sin firma ni identidad real, y el escáner no agregaba contacto. SOLUCIÓN APLICADA: QR firmado con identidad, parseo robusto, alta de contacto real y auto-conexión TCP. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real en dos dispositivos.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, CameraX, ZXing, ECDSA, Compose.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): derivar userId con SHA-256 en lugar de hashCode; mostrar ruta de diagnóstico en UI; rotar logs; limpiar warnings; probar en dos dispositivos.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo del QR; userId derivado de hashCode podría variar entre fabricantes; warnings de deprecación.
──────────────────────────────

── ENTRADA — 2026-09-06 03:20 ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se corrigió la verificación de permisos BLE para que en Android 11 (API 30) no solicite permisos que no existen. Se añadió helper hasBlePermissions en BleManager y BleTransport para devolver true en API < 31. Se añadieron logs de diagnóstico en QrScanScreen y se ajustó la inicialización de CameraX (modo COMPATIBLE y try/catch con logs) para detectar por qué no abre la cámara.
¿ERA UN FIX DE ERROR?: ERROR: en Xiaomi (API 30) el advertising BLE no se iniciaba porque se pedían permisos BLUETOOTH_ADVERTISE/CONNECT/SCAN que solo existen en API 31+. La cámara QR no abría sin logs de error. SOLUCIÓN APLICADA: helper hasBlePermissions con comprobación de Build.VERSION.SDK_INT, logs en QrScanScreen y CameraX con ImplementationMode.COMPATIBLE. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real en Xiaomi y Cubot.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): se descartó que el fallo fuera del hardware; era de permisos y falta de logs.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, API 30 y 31+, CameraX, BLE.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): rotar logs de diagnóstico; mostrar ruta del log en UI; centralizar paleta de colores.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo del BLE y QR; warnings de deprecación; ruta de diagnóstico no visible.
──────────────────────────────

── ENTRADA — 2026-09-06 03:41 ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se redujo el payload BLE de advertising a "token|seed" para cumplir el límite de 31 bytes y se añadió setIncludeDeviceName(true) para enviar el nombre real. Se actualizó QrScanScreen con manejo de permisos, logs de diagnóstico y PreviewView.ImplementationMode.COMPATIBLE.
¿ERA UN FIX DE ERROR?: ERROR: BLE advertising fallaba en Xiaomi con "Datos demasiado grandes" y la cámara del QR no abría. SOLUCIÓN APLICADA: acortar payload BLE y añadir logs/permisos robustos al escáner QR. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real en Xiaomi/Cubot.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; causas confirmadas por logs.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, BLE advertising, CameraX, permisos runtime.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): centralizar paleta; rotar logs; mostrar ruta de diagnóstico en UI.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo del QR y BLE; warnings de deprecación.
──────────────────────────────

── ENTRADA — 2026-09-06 04:02 ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se redujo aún más el payload BLE a solo token (12 bytes) para cumplir el límite estricto de Android. Se actualizó el parser de escaneo para aceptar token solo o token|seed, y obtener nombre desde BluetoothDevice.name.
¿ERA UN FIX DE ERROR?: ERROR: BLE advertising fallaba con "Datos demasiado grandes". SOLUCIÓN APLICADA: payload mínimo sin semilla. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dispositivo.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): se descartó incluir nombre o semilla en el payload por exceder el límite.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, BLE advertising, tamaño de payload.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): deduplicación de nodos, conectar QR UI, mostrar ruta de diagnóstico.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: auto-detección duplicada; QR sin funcionar; validación inter-dispositivo.
──────────────────────────────

── ENTRADA — 2026-09-06 04:06 ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se conectó onNavigateToQrScanner desde MainApp a ConversationsScreen (el botón de escanear QR no hacía nada). Se implementó deduplicación de nodos en ProximityEngine fusionando por displayName o token, y se añadió filtro isSelfUser para evitar auto-detección.
¿ERA UN FIX DE ERROR?: ERROR: botón QR desconectado y nodos duplicados por transporte. SOLUCIÓN APLICADA: pasar callback a ConversationsScreen; fusionar usuarios por nombre o token y filtrar propio. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, BLE, mDNS, Wi-Fi Direct.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): limpiar warnings (parámetros sin uso), mostrar ruta de diagnóstico en UI.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; QR aún requiere prueba con cámara; warnings de deprecación.
──────────────────────────────

── ENTRADA — 2026-09-08 00:38 ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se conectó onNavigateToQrScanner desde MainApp a ConversationsScreen (el botón de escanear QR no hacía nada). Se implementó deduplicación de nodos en ProximityEngine fusionando por displayName o token, y se añadió filtro isSelfUser para evitar auto-detección.
¿ERA UN FIX DE ERROR?: ERROR: botón QR desconectado y nodos duplicados por transporte. SOLUCIÓN APLICADA: pasar callback a ConversationsScreen; fusionar usuarios por nombre o token y filtrar propio. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, BLE, mDNS, Wi-Fi Direct.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): limpiar warnings (parámetros sin uso), mostrar ruta de diagnóstico en UI.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; QR aún requiere prueba con cámara; warnings de deprecación.
──────────────────────────────

── ENTRADA — 2026-09-08 01:13 ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se unificó el alias de KeystoreManager con IdentityManager (malla_identity) para que el QR pueda firmar. Se incluyó el nombre de usuario en el payload BLE (token truncado a 8 + nombre truncado a 10 + seed) respetando límite de 31 bytes. Se mejoró deduplicación usando dirección Bluetooth como clave. Se cambió validateInvitationCode a validación temporal para permitir conectar entre dispositivos sin compartir prefs.
¿ERA UN FIX DE ERROR?: ERROR: QR daba "llave privada no encontrada" por alias distinto; nodos mostraban nombre alfanumérico; auto-detección y duplicados; código de 24h inválido entre dispositivos. SOLUCIÓN APLICADA: unificar alias, incluir nombre en advertising, deduplicar por Bluetooth address, validación temporal de código. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real en dos dispositivos.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; causas confirmadas por inspección.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, BLE, Keystore, Compose.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): intercambio real de código 24h por BLE; diagnóstico en UI; limpiar warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; QR pendiente de prueba; warnings de deprecación.
──────────────────────────────

── ENTRADA — 2026-09-08 01:52 ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se redujo el payload BLE a solo token (8 bytes) y se eliminó setIncludeDeviceName para evitar "Datos demasiado grandes". Se simplificó IdentityQrPayload a formato "MALLA:userId|nombre|ip" sin firma para evitar error de llave privada. Se adaptó MainActivity al nuevo QR. Se añadieron logs en BleTransport.sendInvitation.
¿ERA UN FIX DE ERROR?: ERROR: advertising BLE fallaba por payload grande; QR daba "Llave privada no encontrada"; invitaciones no tenían logs. SOLUCIÓN APLICADA: payload mínimo, QR simple, logs de escritura. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; causas confirmadas por inspección.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, BLE, QR, Compose.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): intercambio real de código 24h por BLE; mostrar ruta de diagnóstico en UI; limpiar warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; detección BLE sin internet aún sin prueba; QR requiere prueba con cámara.
──────────────────────────────

── ENTRADA — 2026-09-08 02:08 ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se redujo payload BLE a solo token (8 bytes) y se eliminó setIncludeDeviceName. Se simplificó QR a formato "MALLA:userId|nombre|ip" sin firma. Se ajustó MainActivity para parsear nuevo QR. Se añadieron logs en BleTransport.sendInvitation.
¿ERA UN FIX DE ERROR?: ERROR: advertising BLE "Datos demasiado grandes"; QR "llave privada no encontrada"; invitaciones sin confirmación. SOLUCIÓN APLICADA: payload mínimo, QR simple, logs. ¿FUNCIONÓ?: compilación exitosa; pendiente validación real.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, BLE, QR, Compose.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): instrumentar ProximityEngine para depurar detección; revisar InviteCodeGenerator y flujo de código 24h.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: detección sin internet no confirmada; QR sin lectura efectiva; código 24h inválido; duplicados de nodos.
──────────────────────────────

── ENTRADA — 2026-09-08 02:20 ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se corrigió InviteCodeGenerator para generar 12 caracteres (antes 8) y se eliminó el extra de IP del fullCode. Se aumentó el tamaño del QR a 220dp. Se añadieron logs de nodos en ProximityEngine.
¿ERA UN FIX DE ERROR?: ERROR: código de 24h siempre inválido porque generaba 8 caracteres y la validación esperaba 12. SOLUCIÓN APLICADA: base de 12 caracteres y fullCode limpio. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; causa confirmada por inspección de longitud.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, QR.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): revisar flujo de invitación BLE para confirmar escritura; limpiar warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: detección sin internet; lectura QR; invitación BLE sin respuesta.
──────────────────────────────

── ENTRADA — 2026-09-08 02:48 ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se corrigió payload BLE a token(8)|nombre truncado(10) = 19 bytes, sin setIncludeDeviceName. Se ajustó parser para extraer nombre del payload. Se añadieron logs de callback BLE en ProximityEngine para depurar por qué no se agregan nodos BLE.
¿ERA UN FIX DE ERROR?: ERROR: nodos BLE detectados en scan pero no aparecían en UI; advertising excedía tamaño. SOLUCIÓN APLICADA: payload compacto y logs de callback. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real sin Wi-Fi.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; causa probable: callback no invocado o filtro propio.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, BLE advertising.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): revisar TransferManager para envío BLE con confirmación; limpiar warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: detección BLE sin internet; mensajes entre dispositivos; nombres reales en nodos.
──────────────────────────────

── ENTRADA — 2026-09-08 03:55 ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se añadió hotfix en MessageReceiver para extraer content del JSON BLE y usar raw como fallback; se añadió log de BLE JSON recibido. Se corrigió import de DiagnosticsLogger.
¿ERA UN FIX DE ERROR?: ERROR: mensaje BLE entrante mostraba JSON crudo en lugar del texto. SOLUCIÓN APLICADA: extracción robusta de content y log. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real entre Cubot y Xiaomi.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): se descartó error tipográfico senderld en código; no se encontró.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, BLE JSON.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): revisar envío BLE para usar siempre JSON válido; limpiar warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: comunicación bidireccional sin internet; detección BLE sin Wi-Fi; nombres reales en nodos.
──────────────────────────────

── ENTRADA — 2026-09-08 04:04 ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se cambió generateToken para usar ANDROID_ID + día, evitando tokens idénticos entre dispositivos. Se incluyó el nombre de usuario truncado en el payload BLE (token|nombre) y se eliminó variable payload no usada.
¿ERA UN FIX DE ERROR?: ERROR: ambos dispositivos generaban token 51cea2b0 y se ignoraban mutuamente como anuncio propio; nodos mostraban nombre Bluetooth. SOLUCIÓN APLICADA: token único por Android ID y payload con nombre MALLA. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real sin internet.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; causa confirmada por logs.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, BLE.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): copiar código 24h; limpiar warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: comunicación bidireccional sin internet; validación real de nodos con nombres.
──────────────────────────────

── ENTRADA — 2026-09-08 22:25 ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se corrigió MainActivity para que al escanear QR cree conversación con userId real y no con IP (evita chat fantasma Peer). Se reordenó TransportManager para intentar BLE antes que TCP y se añadieron logs de envío/recepción BLE. Se añadió log en MessageReceiver al recibir BLE.
¿ERA UN FIX DE ERROR?: ERROR: se creaba chat fantasma Peer <IP> al escanear QR; mensajes encolados en TCP sin canal; sin logs BLE. SOLUCIÓN APLICADA: conversación por userId, prioridad BLE, logs. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real entre dispositivos.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; causas confirmadas por logs.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, BLE, TCP, QR.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): limpiar warnings; copiar código de invitación; mejorar mapeo BLE→contacto.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: comunicación bidireccional BLE sin internet; validación final.
──────────────────────────────

── ENTRADA — 2026-09-08 23:02 ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se añadió fallback connectAndWriteData en TransportManager si sendWithRetry falla. Se expuso MESSAGE_CHAR_UUID en BleManager y se añadieron logs detallados en BleTransport y TransportManager para depurar envío BLE.
¿ERA UN FIX DE ERROR?: ERROR: BLE broadcast y sendWithRetry devolvían false sin logs. SOLUCIÓN APLICADA: más logs y un tercer método de escritura. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real entre dispositivos.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; falta evidencia de fallo GATT con logs nuevos.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, BLE GATT.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): limpiar warnings; copiar código de invitación; mapear BLE→contacto.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: comunicación bidireccional BLE sin internet; validación final.
──────────────────────────────

── ENTRADA — 2026-09-08 23:21 ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se corrigieron notificaciones de nodos para que solo se muestren una vez por token y no estando en primer plano. Se movió el envío de TransportManager.send a withContext(Dispatchers.IO) para no bloquear UI. Se corrigieron return@withContext y llave de cierre.
¿ERA UN FIX DE ERROR?: ERROR: notificaciones repetidas al abrir la app; envío de mensajes parecía retrasarse o bloquear la UI. SOLUCIÓN APLICADA: notificación única por token y solo en background; envío en IO. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; causas confirmadas por inspección de ciclo de vida y threading.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, corrutinas, notificaciones.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): copiar código de invitación; limpiar warnings; mapear BLE→contacto.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación final de mensajería bidireccional sin internet.
──────────────────────────────

── ENTRADA — 2026-09-09 01:39 ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se reemplazaron las comprobaciones directas de BLUETOOTH_CONNECT por helper hasBlePermissions en BleTransport y BleManager. Esto permite que en Android 11 (API 30) no se bloquee la conexión GATT por permisos inexistentes.
¿ERA UN FIX DE ERROR?: ERROR: Xiaomi no recibía mensajes BLE; la conexión GATT fallaba por pedir BLUETOOTH_CONNECT en API 30 donde no existe. SOLUCIÓN APLICADA: hasBlePermissions para API <31. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real entre dispositivos.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; causa confirmada por inspección de permisos.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, BLE, API 30/31+.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): copiar código 24h; limpiar warnings; mapear BLE→contacto.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación final de comunicación bidireccional sin internet.
──────────────────────────────
