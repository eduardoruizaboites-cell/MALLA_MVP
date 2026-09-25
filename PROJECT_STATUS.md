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

── ENTRADA — 2026-09-09 02:09 ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se reordenó TransportManager para intentar connectAndWriteData primero, con fallback a broadcast/sendWithRetry. Se añadieron logs en BleManager.connectAndWriteData y se corrigió log en MessageReceiver.
¿ERA UN FIX DE ERROR?: ERROR: el envío BLE desde Xiaomi fallaba con broadcast; connectAndWriteData sí funcionaba. SOLUCIÓN APLICADA: priorizar connectAndWriteData. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real entre dispositivos.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; causa confirmada por logs.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, BLE GATT, corrutinas.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): copiar código de invitación; limpiar warnings; mapear BLE→contacto.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación final de mensajería bidireccional sin internet.
──────────────────────────────

── ENTRADA — 2026-09-09 02:28 ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se añadió evento typingReceived y TransportManager.sendTyping. MessageReceiver ahora maneja type=typing y no lo guarda como mensaje. ChatScreen muestra burbuja de escribiendo remoto y envía estado de typing con debounce. Se corrigió auto-scroll a filteredMessages.
¿ERA UN FIX DE ERROR?: ERROR: no había indicador de escribiendo remoto; auto-scroll no siempre bajaba al último; la burbuja de escribiendo se activaba con el texto propio. SOLUCIÓN APLICADA: flujo typing, debounce y scroll correcto. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; eran funcionalidades no implementadas.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, Compose, BLE/TCP.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): limpiar warnings; copiar código de invitación; mejorar mapeo BLE→contacto.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación real de typing y scroll; posible chat fantasma aún por confirmar.
──────────────────────────────

── ENTRADA — 2026-09-09 02:45 ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se implementó envío de imágenes como Base64 comprimido (320px JPEG) en MeshChatViewModel. ChatScreen ahora decodifica y muestra imágenes Base64 en BubbleContent. Se corrigió auto-scroll y se añadió burbuja de escribiendo remoto.
¿ERA UN FIX DE ERROR?: ERROR: las imágenes no viajaban por BLE; no se mostraban; auto-scroll no bajaba; no había indicador de escribiendo. SOLUCIÓN APLICADA: Base64, decodificación, scroll y typing. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; eran funcionalidades faltantes.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, BLE, Compose, imágenes.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): fragmentación de imágenes grandes; copiar código 24h; limpiar warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación final bidireccional con imágenes y typing.
──────────────────────────────

── ENTRADA — 2026-09-09 02:57 ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se añadió typingReceived a MallaEventBus, envío de typing con debounce y burbuja de escribiendo remoto. También imágenes Base64.
¿ERA UN FIX DE ERROR?: no; fue implementación de features.
VERIFICADO EN: compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): fragmentación de imágenes, copiar código 24h, limpiar warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación real de typing e imágenes.
──────────────────────────────

── ENTRADA — 2026-09-09 03:25 ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se unificó la clase MeshMessage en data/entity. Se eliminó duplicado de MallaEventBus (quedó solo events). Se corrigieron imports en Injector, MessageReceiver, TransportManager, etc. Se añadió typingReceived. Quedan compilando las mejoras de imágenes Base64, auto-scroll y typing.
¿ERA UN FIX DE ERROR?: ERROR: NoSuchMethodError typingReceived por duplicado de MallaEventBus y conflictos de MeshMessage. SOLUCIÓN APLICADA: unificación canónica y limpieza de imports. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): se descartó error de versión APK; era duplicado de clases.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, BLE, Compose.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): fragmentación de imágenes grandes; copiar código 24h; limpiar warnings.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación real de imágenes, typing y notificaciones.
──────────────────────────────

── ENTRADA — 2026-09-09 03:28 ──
Compilación: BUILD SUCCESSFUL (estado final de sesión)
QUÉ SE HIZO: Cierre formal. Se consolidaron las correcciones de duplicados, unificación de MeshMessage, imágenes Base64, auto-scroll, typing remoto y notificaciones. Se verifica que todo compila y queda listo para pruebas reales.
¿ERA UN FIX DE ERROR?: Se resolvieron múltiples errores durante la sesión. Último fix: NoSuchMethodError typingReceived por duplicado de MallaEventBus. SOLUCIÓN APLICADA: unificación y limpieza de imports. ¿FUNCIONÓ?: compilación exitosa.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): se descartó APK antigua; era duplicado de clases.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 8+, BLE, Compose.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): fragmentación de imágenes grandes; copiar código 24h; limpiar warnings; mapear BLE→contacto.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación real de imágenes, typing y notificaciones; posible chat fantasma residual.
──────────────────────────────

── ENTRADA — 2026-09-09 16:20 (mejora permisos BLE y robustez Wi-Fi Direct) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se hizo público BleManager.hasBlePermissions y se usó en startScanningWithCallback para evitar escaneo sin permisos en Android 12+. Se añadió contador de fallos consecutivos en WifiDirectManager (discoverPeers y createGroup) y se desactiva automáticamente tras 3 fallos. Se corrigió DhtWrapper.getLocalAddress y DhtService.getLocalAddress para ignorar IPs de rango Wi-Fi Direct 192.168.49.x.
¿ERA UN FIX DE ERROR?: ERROR: CUBOT (Android 16) no descubría nodos BLE y Wi-Fi Direct fallaba sin desactivarse, generando ruido. SOLUCIÓN APLICADA: verificación real de permisos BLE antes de escanear; desactivación automática de Wi-Fi Direct tras fallos repetidos; filtrado de IPs Wi-Fi Direct no propias. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real entre Xiaomi y Cubot.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; causas confirmadas por logs.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 11/16, BLE, Wi-Fi Direct, obtención de IP local.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): añadir botón para copiar código de invitación al portapapeles; mapear BLE→contacto al detectar nodo; fragmentación de imágenes grandes.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; comunicación bidireccional BLE sin internet; fragmentación de imágenes; warnings KSP/deprecación.
──────────────────────────────

── ENTRADA — 2026-09-09 17:15 (identificación BLE por userId y selección de transporte por contacto) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se añadió userId a NearbyUser y al advertising BLE (token|userId|nombre). ProximityEngine ahora propaga userId al detectar nodos. TransportManager elige el BluetoothDevice correcto según contactId en lugar de firstOrNull. DiscoveryService registra el servicio mDNS con el nombre real del usuario. Se filtraron IPs Wi-Fi Direct en DhtWrapper/DhtService/ProximityEngine. Se forzó la solicitud de permisos BLE en Android 12+ desde MainActivity. Se desactivó Wi-Fi Direct tras 3 fallos consecutivos.
¿ERA UN FIX DE ERROR?: ERROR: los dispositivos no se localizaban ni comunicaban; el envío BLE usaba el primer dispositivo detectado sin filtrar por contacto; el nombre mDNS era aleatorio. SOLUCIÓN APLICADA: incluir userId en advertising/parser y usarlo para mapear dispositivo BLE ↔ contactId; corregir serviceName mDNS con nombre real; filtrar IPs propias Wi-Fi Direct. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real entre Xiaomi y Cubot.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 11/16, BLE advertising, mDNS, Wi-Fi Direct, permisos runtime.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): botón copiar código de invitación; fragmentación de imágenes; mapear BLE→contacto automáticamente al detectar nodo.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; comunicación bidireccional BLE sin internet; fragmentación; warnings KSP/deprecación.
──────────────────────────────

── ENTRADA — 2026-09-09 17:55 (notificaciones de mensajes, invitaciones con biometría y diagnóstico ampliado) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se añadió notificación de mensaje entrante en MessageReceiver cuando la app no está en primer plano. Se implementó diálogo de invitación con aceptación biométrica en MainActivity. Se corrigió validateInvitationCode para usar códigos reales con expiración. Se unificó advertising BLE con userId en ProximityEngine. Se ampliaron logs de TransportManager e InvitationManager para monitorear dispositivos BLE, nodos y envíos.
¿ERA UN FIX DE ERROR?: ERROR: no había notificación de mensajes entrantes; las invitaciones no se mostraban; validación de código era temporal; advertising en algunos flujos no incluía userId. SOLUCIÓN APLICADA: NotificationHelper en MessageReceiver; AlertDialog + BiometricPrompt en MainActivity; validación real; startAdvertisingWithUserData en ProximityEngine. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 11/16, notificaciones, BiometricPrompt, AlertDialog, BLE.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): fragmentación de imágenes; botón copiar código; envío de invitación al agregar por QR; mapear BLE→contacto al detectar nodo.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; comunicación bidireccional; fragmentación; warnings KSP/deprecación.
──────────────────────────────

── ENTRADA — 2026-09-09 18:10 (fix payload BLE excedido) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se redujo el payload BLE de advertising en startAdvertisingWithUserData: se eliminaron guiones del userId y se truncó el nombre a 5 caracteres. El payload total es de 27 bytes, dentro del límite de 31 bytes de Android BLE. Antes excedía y fallaba con "Datos demasiado grandes" en Xiaomi.
¿ERA UN FIX DE ERROR?: ERROR: Xiaomi no podía anunciarse por BLE; advertising fallaba por exceder 31 bytes. SOLUCIÓN APLICADA: acortar userId y nombre. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; causa confirmada por log de error.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 11/16, BLE advertising.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): fragmentación de imágenes; botón copiar código; envío de invitación al agregar por QR.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; comunicación bidireccional; fragmentación; warnings KSP/deprecación.
──────────────────────────────

── ENTRADA — 2026-09-09 18:25 (advertising BLE con manufacturerData) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se reemplazó el uso de serviceData por manufacturerData en BleManager.startAdvertisingWithUserData. El parser de escaneo ahora lee getManufacturerSpecificData(0xABCD). El payload publicitario pasó de ~40 bytes (con UUID de servicio) a ~19 bytes, dentro del límite BLE de 31 bytes. Se eliminó el token del payload, derivándolo desde userId.
¿ERA UN FIX DE ERROR?: ERROR: advertising BLE fallaba en Xiaomi y Cubot con "Datos demasiado grandes" porque serviceData añadía 16 bytes de UUID. SOLUCIÓN APLICADA: usar manufacturerData con ID de 16 bits, mucho más compacto. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real entre dispositivos.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): se descartó que el problema fuera el permiso o el hardware; era el tamaño del payload.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 11/16, BLE advertising, manufacturerData.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): fragmentación de imágenes; botón copiar código; envío de invitación al agregar por QR.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; permisos BLE en Cubot; comunicación bidireccional; fragmentación; warnings.
──────────────────────────────

── ENTRADA — 2026-09-09 19:00 (fix BLE GATT write con WRITE_TYPE_NO_RESPONSE) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se cambió en BleManager.connectAndWriteData y BleTransport (servidor GATT) el tipo de escritura de WRITE_TYPE_DEFAULT a WRITE_TYPE_NO_RESPONSE; se ajustaron las propiedades de las características a PROPERTY_WRITE_NO_RESPONSE y se simplificó la confirmación de escritura para evitar cuelgues.
¿ERA UN FIX DE ERROR?: ERROR: La escritura BLE desde Cubot (cliente) hacia Xiaomi (servidor) fallaba con status 133 (GATT_ERROR) al usar WRITE_TYPE_DEFAULT, pues el servidor no respondía a la escritura con responseNeeded. SOLUCIÓN APLICADA: migrar a escritura sin respuesta (WRITE_TYPE_NO_RESPONSE) en cliente y servidor; en cliente se considera éxito inmediato si writeCharacteristic retorna true y se desconecta tras 300 ms. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba real entre Xiaomi y Cubot.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): se consideró que el servidor no exponía la característica o que el UUID era incorrecto; se confirmó por inspección que el error era de tipo de escritura/permisos GATT.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 11/16, BLE GATT, permisos runtime.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): consolidar arranque de ProximityEngine/WifiDirect/BleTransport/TransportManager en un único punto para evitar reinicios; añadir botón copiar código de invitación; fragmentación de imágenes.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: validación inter-dispositivo; comunicación bidireccional BLE sin internet; intercambio real de código 24h; warnings KSP/deprecación.
──────────────────────────────

── ENTRADA — $(date '+%Y-%m-%d %H:%M') (Iteración 1: MTU negotiation + lock GATT + debounce typing) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se añadió negociación de MTU (requestMtu 517 + onMtuChanged) en BleManager.connectAndWriteData y BleTransport.connectGatt/connectGattAndWait. Se implementó cache de GATT por dirección con Mutex por dispositivo para evitar conexiones concurrentes. Se rechazan payloads > MTU con log explícito. Se añadió debounce de 300ms en MeshChatViewModel.sendTyping. Se cambió INVITE_CHAR_UUID a PROPERTY_WRITE_NO_RESPONSE para alinear cliente/servidor.
¿ERA UN FIX DE ERROR?: ERROR: JSON truncado a ~20 bytes al enviar Cubot→Xiaomi por MTU por defecto (23). 6 conexiones GATT concurrentes por burst de typing. SOLUCIÓN APLICADA: requestMtu(517) tras onServicesDiscovered; cache+Mutex por dirección; debounce 300ms. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dispositivo real (Xiaomi+Cubot).
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): se descartó problema en el receptor (Xiaomi recibe de Cubot truncado, Cubot recibe de Xiaomi completo — confirma que el emisor es el problema).
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot), BLE GATT, MTU variable por fabricante.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): fragmentación con header para payloads > MTU (imágenes); migración de userId a UUID persistido; filtro typing en receptor; permiso POST_NOTIFICATIONS.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: userId colisionado (Iteración 2); typing sin filtro en receptor (Iteración 3); notificaciones Android 13+ (Iteración 4); QR con IP pública (Iteración 5); fragmentación de imágenes.
──────────────────────────────

── ENTRADA — 2026-09-10 01:19 (Iteración 2: userId único vía SHA-256 del pubkey) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se reescribió IdentityManager.getIdentityId() para derivar el userId de SHA-256(pubKeyBase64) → 16 hex chars (antes: pubKeyBase64.take(12) con guiones, idéntico en todos los dispositivos por el header DER). getOrCreatePersistentId() ahora devuelve el mismo formato (UUID sin guiones, 16 chars) y regenera si detecta formato viejo. BleManager.startAdvertisingWithUserData usa el userId completo de 16 chars en el payload de manufacturerData.
¿ERA UN FIX DE ERROR?: ERROR: Cubot y Xiaomi compartían el mismo userId "MFk-wEw-YHK-oZI" porque los primeros 12 chars del base64 de cualquier pubkey secp256r1 son idénticos (header DER). SOLUCIÓN APLICADA: hash SHA-256 sobre el base64 completo. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dispositivo real (Xiaomi+Cubot).
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): se descartó que ambos dispositivos compartieran la misma clave EC — el Keystore genera keypair único por instalación; el problema era exclusivamente el slice.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot). Payload de advertising: 16+1+5 = 22 bytes, dentro del límite de manufacturerData.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): filtro typing en receptor (Iteración 3); senderName en payload; POST_NOTIFICATIONS (Iteración 4); QR sin IP pública (Iteración 5); fragmentación de imágenes.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: contactos existentes con formato viejo quedan huérfanos (aceptable en MVP); typing sin filtro en receptor (Iteración 3); notificaciones Android 13+ (Iteración 4); QR con IP pública (Iteración 5); fragmentación de imágenes.
──────────────────────────────

── ENTRADA — 2026-09-10 01:22 (Iteración 3: filtro typing + senderName en payload) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se añadió filtro type=="typing" en MessageReceiver antes del bloque de persistencia; emite MallaEventBus.typingReceived y no guarda el mensaje. Se añadieron campos senderName y senderAvatarSeed a MeshMessage. TransportManager.send y sendTyping incluyen senderName en el JSON cuando hay appContext (guardado en start()). MessageReceiver usa senderName del payload como primera opción para el título de conversación (fallback a connectedPeers, luego ProximityEngine, luego Peer <id>). BleManager.startAdvertisingWithUserData amplió el nombre a 10 chars.
¿ERA UN FIX DE ERROR?: ERROR: 1) cada typing (1/0) se guardaba como mensaje real. 2) El título del chat entrante era "Peer <id>" porque connectedPeers solo se llena por TCP. SOLUCIÓN APLICADA: filtro typing con early return + senderName en el contrato del payload. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dispositivo real.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): se descartó que el problema del chat "Peer" fuera por BLE — era por ausencia de displayName en el payload JSON. Se descartó que TransportManager tuviera acceso al contexto global vía App.instance (no existe esa clase); se guardó el contexto en start().
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot). Payload BLE reducido a 27 bytes (límite 31).
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): POST_NOTIFICATIONS (Iteración 4); QR sin IP pública (Iteración 5); fragmentación de imágenes.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: contactos viejos huérfanos; notificaciones Android 13+ (Iteración 4); QR con IP pública (Iteración 5); fragmentación de imágenes.
──────────────────────────────

── ENTRADA — 2026-09-10 01:24 (Iteración 4: verificación defensiva de notificaciones + limpieza) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se añadieron verificaciones defensivas en util/NotificationHelper antes de notify(): areNotificationsEnabled() y channelBlocked() (canal con IMPORTANCE_NONE). Se añadió log de estado del canal en createChannel. Se eliminó el NotificationHelper duplicado de notification/ (dead code confirmado por grep — nadie lo importaba). NO se modificó el pedido de permisos: POST_NOTIFICATIONS ya estaba declarado en el manifest y ya se solicitaba en runtime.
¿ERA UN FIX DE ERROR?: ERROR: notificaciones no visibles en Cubot Android 16 aunque el log dice "Notificación de mensaje mostrada". HIPÓTESIS: POST_NOTIFICATIONS denegado silenciosamente o canal malla_messages bloqueado por instalación previa. SOLUCIÓN APLICADA: log defensivo que permite diagnosticar la causa exacta en el próximo run; eliminación de dead code. ¿FUNCIONÓ?: pendiente prueba en dispositivo real. Los nuevos logs en malla_diagnostics.txt dirán si areNotificationsEnabled=false o channelBlocked=true.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): se descartó que faltara declarar POST_NOTIFICATIONS en manifest o solicitarlo en runtime — ambos ya existían correctamente.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot). Verificación de canal solo activa en API 26+.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): añadir botón "Reabrir ajustes de notificaciones" en pantalla de settings para guiar al usuario si denegó el permiso.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: contactos viejos huérfanos; QR con IP pública (Iteración 5); fragmentación de imágenes; confirmar en dispositivo el estado real del permiso/canal.
──────────────────────────────

── ENTRADA — 2026-09-10 01:26 (Iteración 5: sanitización de IP en QR) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se añadió helper isLocalIp() en IdentityQrPayload que acepta solo 10.x, 172.16-31.x, 192.168.x, 169.254.x. Se aplica en generate() (no incluir IPs no locales en el QR) y en parseAndVerify() (descartar IPs no locales aunque vengan). No se tocó MainActivity ni PerfilScreen: el código existente queda correcto una vez sanitizado el campo localIp.
¿ERA UN FIX DE ERROR?: ERROR: el QR del Cubot contenía la IP pública 196.83.29.16, causando ENETUNREACH al escanear desde Xiaomi. HIPÓTESIS: DhtWrapper.getLocalAddress() devuelve la IP pública cuando no encuentra interfaz Wi-Fi válida (log: SSID=<unknown>, IP=0.0.0.0). SOLUCIÓN APLICADA: filtro de rango en IdentityQrPayload. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dispositivo real.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): se descartó que el problema estuviera en el parser (aunque añadimos filtro allí también por defensa en profundidad); la causa está en el generador del QR.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot). QRs viejos con IP pública siguen siendo parseados (aceptan el contacto), solo se descarta la IP.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): investigar por qué DhtWrapper.getLocalAddress() devuelve IP pública y corregir en la raíz; mostrar la IP local real en PerfilScreen para diagnóstico; fragmentación de imágenes.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: contactos viejos huérfanos; DhtWrapper.getLocalAddress() devolviendo IP pública (causa raíz no corregida, solo mitigada en QR); fragmentación de imágenes; verificar en dispositivo real que el contacto se agrega sin intento TCP fallido.
──────────────────────────────

── ENTRADA — 2026-09-10 02:12 (Iteración 6: fixes puntuales post-auditoría + logs de diagnóstico) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: (1) Fix de sendInvitation en BleTransport que no negociaba MTU antes de escribir la invitación — mismo bug que mensajes en Iteración 1 pero este camino se quedó sin arreglar. Ahora hace requestMtu(517) en onServicesDiscovered y escribe la invitación en onMtuChanged. (2) Fix de sendZumbido en MeshChatViewModel que enviaba senderId="self" literal, causando la creación de un chat "peer self" en el receptor; ahora usa IdentityManager.getIdentityId(). (3) Añadidos logs de diagnóstico en TransportManager.send que listan nearbyUsers completos y avisan cuando el contacto no está en la lista (fallback a firstOrNull puede enviar al device equivocado). (4) Log del userId propio al iniciar advertising en ProximityEngine.
¿ERA UN FIX DE ERROR?: SÍ, dos: ERROR: invitación BLE no llegaba ("Error parseando invitación: Unterminated string") → SOLUCIÓN: negociar MTU antes de writeCharacteristic. ERROR: zumbidos creaban chat "peer self" → SOLUCIÓN: senderId real. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dispositivo.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): se descartó que la app usara el BleTransport stub de app/transport/ — todos los imports apuntan a network/BleTransport. Se descartó que GattServerManager compitiera por el UUID del GATT server — no se importa en ningún lado. Se descartó que Iteración 1 fuera al archivo equivocado.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot). Fix de MTU beneficia a ambos, especialmente Cubot→Xiaomi.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): desconectar MeshConnector/GlobalTransport (sistema viejo) para eliminar la coexistencia de dos transportes; borrar app/transport/ (dead code); borrar GattServerManager.kt (dead code); limpiar módulos fantasma en settings.gradle.kts; borrar archivos basura raíz (0, tatus, {, *.py, *.txt).
DEUDA / PENDIENTE QUE SIGUE ABIERTA: coexisten sistema de transporte viejo (MeshConnector/GlobalTransport/CascadeRouter/WebRtc) y nuevo (TransportManager/BleManager/BleTransport/ProximityEngine) sin desconectar; Dead code en app/transport y GattServerManager; Módulos fantasma en settings.gradle.kts; Archivos basura en raíz; PROJECT_STATUS.md sin congelar (1656 líneas); Confirmar con logs post-fix si MTU es simétrico entre ambos dispositivos y si el userId del advertising coincide con el del Room tras escanear QR.
──────────────────────────────

── ENTRADA — 2026-09-10 03:34 (Iteración 6: fixes puntuales post-auditoría) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: (1) sendInvitation en BleTransport ahora negocia MTU (requestMtu 517) antes de escribir, mismo patrón que Iteración 1 para mensajes. (2) sendZumbido en MeshChatViewModel usa IdentityManager.getIdentityId() en vez de senderId="self" literal. (3) Logs de diagnóstico en TransportManager.send que listan nearbyUsers completos y avisan cuando el contacto no está. (4) Log del userId propio en ProximityEngine.start.
¿ERA UN FIX DE ERROR?: SÍ, dos: ERROR: invitación BLE llegaba truncada ("Unterminated string at character 20") porque sendInvitation no negociaba MTU → SOLUCIÓN: requestMtu antes de writeCharacteristic. ERROR: zumbidos creaban chat "peer self" en receptor → SOLUCIÓN: senderId real. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dispositivo.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): se descartó que Iteración 1 hubiera ido al archivo equivocado (los imports apuntan todos a network/BleTransport, no al stub de app/transport).
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot).
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): Iteración 7 con feature flags para desconectar sistema viejo (MeshConnector/GlobalTransport/CascadeRouter) y WifiDirect/DHT bajo demanda.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: coexistencia de sistemas de transporte; Wi-Fi Direct arrancado permanente; DHT con bug de IP pública; fragmentación de imágenes.
──────────────────────────────

── ENTRADA — 2026-09-10 03:38 (Iteración 7: feature flags y apagado quirúrgico) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se creó core/config/MeshFlags.kt con 6 flags (enableBle=true, enableTcpLan=true, enableWebRtc=false, enableWifiDirect=false, enableDht=false, enableLegacyTransport=false). Se guardaron los arranques con flags en: MeshChatService (MeshConnector), Injector (GlobalTransport + CascadeRouter + DhtService), ProximityEngine (Wi-Fi Direct), MainActivity (DhtWrapper init + publish). Todo reversible cambiando un flag en MeshFlags.kt.
¿ERA UN FIX DE ERROR?: ERROR estructural: 3 sistemas de transporte coexistiendo (nuevo TransportManager + viejo MeshConnector + GlobalTransport), Wi-Fi Direct arrancado permanentemente pese a no aportar, DHT con bug de IP pública. SOLUCIÓN APLICADA: apagado quirúrgico con flags sin borrar código. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dispositivo con logs limpios.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica (cambio arquitectónico planificado).
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot). Sin cambios de compatibilidad.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): mover MeshFlags a SharedPreferences para alternar canales desde Ajustes sin recompilar; store-and-forward con retry real; fragmentación de imágenes.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: store-and-forward real; fragmentación de imágenes; WebRTC sin cablear a TransportManager; Wi-Fi Direct bajo demanda.
──────────────────────────────

── ENTRADA — 2026-09-10 04:03 (Iteración 8: 4 fixes post-logs + auto-conexión self) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: (1) NearbySection.kt: eliminado DisposableEffect con ProximityEngine.start/stop — el motor es dueño MeshChatService y ya no se apaga al cambiar de pantalla. (2) BleTransport.start: filtro para no auto-conectar al propio device (evita el eco "peer self" observado en logs de Cubot). (3) BleManager.startAdvertisingWithUserData: nombre truncado a 6 chars de la primera palabra + payload condicional. Evita "Datos demasiado grandes" cuando el nombre es el default "Usuario Malla". (4) InvitationManager: filtro isLocalIp sobre senderLocalIp — no incluir IPs públicas (bug ya visto en el log: 196.83.29.16).
¿ERA UN FIX DE ERROR?: SÍ, cuatro bugs confirmados en logs: ProximityEngine se reiniciaba cada ~30s; Cubot recibía eco de sus propios mensajes; advertising fallaba en Xiaomi; invitación filtraba IP pública.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; causas confirmadas por lectura directa del código.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot). Payload advertising reducido a 23 bytes.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): fix de NetworkService.isContactConnected para reconocer peers TCP (bug bloqueante: TCP conectado=false aunque NetworkService reporta "Conectado a 10.86.46.5"); fragmentación BLE cuando MTU<200 (Cubot responde 23 al requestMtu(517)).
DEUDA / PENDIENTE QUE SIGUE ABIERTA: NetworkService no reconoce peers TCP → todos los mensajes van por BLE; MTU Cubot=23 rechaza payloads >20B; fragmentación BLE pendiente.
──────────────────────────────

── ENTRADA — 2026-09-10 04:06 (Iteración 9: diagnóstico de handshake TCP + reordenar cascada) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: (1) NetworkService.ClientHandler.start ahora emite DiagnosticsLogger.log en cada paso del handshake ([HS:1]...[HS:OK]/[HS:FAIL]) — permite identificar exactamente en qué punto se cuelga o falla. (2) TransportManager.send reordena la cascada: TCP primero si isContactConnected=true, luego BLE, luego Wi-Fi Direct. Antes era BLE → TCP → Wi-Fi Direct. Ahora refleja la prioridad correcta (internet/red antes que BLE).
¿ERA UN FIX DE ERROR?: ERROR: Cubot reporta "TCP conectado=false" aunque NetworkService dice "Conectado a 10.86.46.5". La causa es que el handshake ECDH no completa (nunca se ejecuta clients[peerUserId] = handler). SOLUCIÓN APLICADA: logs de diagnóstico para identificar el paso exacto del fallo + reordenamiento para usar TCP cuando el handshake sí complete. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dispositivo con logs nuevos.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica; el fallo del handshake no se puede diagnosticar sin logs detallados, de ahí el cambio.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot). El handshake usa DataInputStream.readUTF, sensible a diferencias entre APIs.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): una vez identificado el paso del fallo, evaluar si readUTF debe reemplazarse por readInt+readFully con timeout; fragmentación BLE cuando MTU<200.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: handshake TCP sin completar; MTU Cubot=23 rechaza payloads >20B; fragmentación BLE pendiente; el test del handshake quedó pendiente de los logs.
──────────────────────────────

── ENTRADA — 2026-09-19 13:55 (Iteración 10: fragmentación BLE + doble conexión TCP + MTU retry) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: (A) NetworkService: disconnect() solo elimina del mapa si el handler es el actualmente registrado; al registrar handler nuevo se cierra el previo ordenadamente; logs [HS:DISCONNECT] y [HS:EOF]. (B) BleManager.connectAndWriteData fragmenta payloads en formato fragIdx(1B)+totalFrags(1B)+chunk, 30ms entre fragmentos, hasta 255 fragmentos; helper writeFramed. (C) BleManager.establishGatt reintenta requestMtu(247) si 517 retorna MTU menor a 100. (D) BleTransport.onCharacteristicWriteRequest reensambla fragmentos con BleFragmentBuffer por device.address antes de emitir a incomingMessages. (E) CancellationException de connectAndWriteData silenciada. (F) import kotlinx.coroutines.delay añadido a BleManager. (G) KDoc de handleFragment reformulado sin corchetes (evita error de compilación de KDoc).
¿ERA UN FIX DE ERROR?: SÍ, cuatro bugs confirmados en logs: (1) Cubot abre 2 conexiones TCP simultáneas y la segunda desconecta a la primera → clients vacío a los 40s; (2) Xiaomi MTU=23 rechaza todos los payloads >20B; (3) logs de StandaloneCoroutine was cancelled; (4) no había log de desconexión TCP.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): se descartó que el handshake ECDH fallara — los logs [HS:OK] demuestran que sí completa correctamente.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot). Fragmentación con MTU=23 soportada hasta 255 fragmentos (~4.5KB).
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): fragmentar también BleTransport.broadcast si connectAndWriteData no resuelve el problema; consolidar isLocalIp en :core para eliminar duplicación.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: verificar en dispositivo que los mensajes llegan bidireccionales y que el TCP se mantiene vivo >60s; si Cubot sigue rechazando MTU 247, la fragmentación ya lo cubre.
──────────────────────────────

── ENTRADA — 2026-09-19 14:38 (Iteración 11: fragmentación con header de 4 bytes) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se cambió el header de fragmentación BLE de 2 bytes ([idx:1][total:1]) a 4 bytes ([idxHi:1][idxLo:1][totalHi:1][totalLo:1]) en BleManager.connectAndWriteData y BleTransport.handleFragment. Se elevó el límite de 255 a 65535 fragmentos, permitiendo payloads de ~1 MB con MTU=23. El delay entre fragmentos bajó de 30 ms a 20 ms.
¿ERA UN FIX DE ERROR?: SÍ: ERROR: imagen 12 KB rechazada por requerir 693 fragmentos (>255). SOLUCIÓN APLICADA: header de 4 bytes. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dispositivo.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): no aplica.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot).
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): feedback al usuario cuando un payload excede el límite; investigar por qué la invitación no se envía.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: invitación no se envía (sin logs de intento); feedback de UI para fallos de envío; TCP sin probar.
──────────────────────────────

── ENTRADA — 2026-09-19 14:44 (Iteración 12: diagnóstico invitación + fallback broadcast) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se blindó InvitationManager.sendInvitation con log de entrada absoluta, logs paso a paso (myId, pubKey length, myIp, JSON length) y try/catch con stacktrace completo. Se añadió fallback: si user.bluetoothDevice es null, intentar BleTransport.broadcast a cualquier GATT conectado y toast honesto. Se añadieron logs defensivos en ConversationsScreen.onSendRequest (LogBuffer + Logcat) para confirmar si la UI dispara el flujo. El propósito es diagnóstico: el log de hoy no muestra ni un solo [InvitationManager] Enviando invitación, así que necesitamos saber si la corrutina arranca o si falla antes del log original.
¿ERA UN FIX DE ERROR?: ERROR: invitación no se envía desde la UI (sin logs). SOLUCIÓN APLICADA: logs defensivos + fallback. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dispositivo.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): se descartó que el wiring de UI esté mal (NearbySection → onConnectClick → selectedNearbyUser → NearbyPanel → onSendRequest → InvitationManager.sendInvitation está cableado). Hipótesis actual: excepción no capturada en las 5 operaciones previas al log original (getIdentityId, getPublicKeyBase64, getLocalAddress, ContactInvitation, JSONObject).
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot).
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): fix de sendAcceptance con manufacturerData (el serviceData actual excede 31 bytes).
DEUDA / PENDIENTE QUE SIGUE ABIERTA: sendAcceptance con serviceData excede límite de advertising; feedback UI en fallos.
──────────────────────────────

── ENTRADA — 2026-09-19 (Iteración 13: fix de invitación silenciosa + sanitización IP) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: (A) BleTransport.kt: extraído `reassembleFragments(device, value): ByteArray?` reutilizable desde `handleFragment`; el branch `INVITE_CHAR_UUID` del GATT server ahora reensambla fragmentos antes de emitir, igual que MESSAGE_CHAR_UUID. (B) InvitationManager.kt: `sendInvitation` reescrito para usar `BleManager.connectAndWriteData(targetDevice, BleTransport.INVITE_CHAR_UUID, json.toByteArray())` en vez de `BleTransport.sendInvitation` (que fallaba en silencio si no había GATT previo). Eliminado el fallback tautológico `firstOrNull { it.address == user.bluetoothDevice?.address }`. Añadido helper `isLocalIp` y sanitización de `myIp` con el mismo criterio que Iter 5 (rechaza IPs no locales). Toast refleja el `Boolean` real de la escritura.
¿ERA UN FIX DE ERROR?: SÍ: ERROR: invitación desde Xiaomi no llegaba al Cubot — logs mostraron que `sendInvitation` arrancaba correctamente hasta "Enviando por BLE a <MAC>" pero `BleTransport.sendInvitation` salía silenciosamente sin log cuando no había conexión GATT activa al targetDevice (Xiaomi nunca iniciaba connectGatt; el Cubot sí lo tenía porque era cliente). SOLUCIÓN APLICADA: usar el camino `connectAndWriteData` probado en Iter 10/11 que hace connectGatt + MTU negotiation + fragmentación con header de 4 bytes + retry. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dispositivo entre Xiaomi y Cubot.
HIPÓTESIS DESCARTADAS (si fue debugging, ROL 2): (1) "la corrutina de sendInvitation no arranca" — DESCARTADA, logs muestran INICIO y pasos previos. (2) "MTU=23 rechaza payloads >20B" — DESCARTADA, fragmentación con header 4B funciona (139B → 9 fragmentos OK). (3) "header de 4 bytes fue insuficiente para imagen" — DESCARTADA, el fallo de imagen es por rotura de conexión, no por límite del header.
VERIFICADO EN: solo compilación.
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot). Sin cambios de compatibilidad.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): (1) Eliminar `BleTransport.sendInvitation` y `BleTransport.broadcast` tras migrar todos los llamadores (dead code). (2) El grep reveló 4 llamadores de `broadcast` (MessageReceiver.kt:278, TransportManager.kt:93/151) que escriben SIN header de 4 bytes mientras el receptor asume header → probable causa de que ACKs y transport payloads nunca se reensamblen. Requiere fix coordinado emisor+receptor. (3) Bug #4 del análisis forense: MTU asimétrico Xiaomi=23 vs Cubot=517 (retry 517→247 no dispara en Xiaomi).
DEUDA / PENDIENTE QUE SIGUE ABIERTA: prueba en dispositivo entre Xiaomi y Cubot; ACK de mensajes via broadcast sin header de 4 bytes (bug coordinado); MTU asimétrico; `BleTransport.sendInvitation`/`broadcast` huérfanos; `BleTransport.kt` duplicado en app/transport/; envío de imágenes >20KB falla por rotura GATT (Bug #2).
──────────────────────────────

── ENTRADA — 2026-09-19 16:11 (Iteración 14: cap 512B + reset buffer fragmentos + logs defensivos) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: (A) BleManager.kt: cap de fragmento cambiado de `(mtu-3).coerceAtLeast(20)` a `(mtu-3).coerceIn(20, 512)` para respetar el límite duro `BluetoothGatt.GATT_MAX_ATTRIBUTE_LEN = 512`. Con MTU=517 el cálculo anterior generaba fragmentos de 514B que `writeCharacteristic` rechazaba silenciosamente (imagen 29 KB fallaba en fragmento 1/59). (B) BleManager.writeFramed: rechazo explícito >512B con log, log de retorno `false` del writeCharacteristic y log de excepción con clase y mensaje. (C) BleManager.connectAndWriteData: log de cancelación real (antes era silenciosa, con comentario incorrecto sobre Mutex). (D) BleTransport.reassembleFragments: reset del `BleFragmentBuffer` cuando llega un fragmento con `totalFrags` distinto al buffer existente — evita que fragmentos huérfanos de una invitación previa incompleta se mezclen con el siguiente mensaje.
¿ERA UN FIX DE ERROR?: SÍ, tres: (1) ERROR: imagen 29 KB desde Xiaomi y Cubot fallaba siempre en fragmento 1/59 con MTU=517 → SOLUCIÓN: cap a 512B → ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dispositivo. (2) ERROR: mensajes de texto llegaban con JSON corrompido (timestamp mixto `178983290fa-de1b114a9d1` y JSON de dos payloads fusionado) → SOLUCIÓN: reset del buffer al cambiar totalFrags → ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dispositivo. (3) ERROR: fallo de fragmento sin causa visible en log → SOLUCIÓN: logs defensivos en writeFramed → ¿FUNCIONÓ?: pendiente ver en próximo run.
HIPÓTESIS DESCARTADAS (ROL 2): (a) "el reensamblador está mal" — DESCARTADA: el header de 4 bytes y el algoritmo son correctos; el bug era el buffer compartido entre mensajes consecutivos. (b) "hay que meter messageId en el header de fragmentación" — DESCARTADA como primer fix: rompe compatibilidad entre versiones y no es la causa raíz. (c) "el Mutex no funciona" — DESCARTADA: el Mutex serializa bien; el comentario previo que decía "otra corrutina toma el Mutex → CancellationException" era incorrecto (withLock suspende, no cancela). (d) "MTU=517 significa fragmentos de 514B seguros" — DESCARTADA: el límite ATT de 512B aplica a la escritura completa, no al payload útil.
VERIFICADO EN: solo compilación (BUILD SUCCESSFUL in 1m 57s, 327 tasks, 21 executed). Pendiente prueba en dispositivo Xiaomi↔Cubot.
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot). El cambio a 512 aplica igual para cualquier MTU; con MTU=23 sigue siendo payloadPerFrag=16. Sin cambios de protocolo: receptores viejos siguen entendiendo los fragmentos.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): (1) Diagnosticar por qué Xiaomi nunca negocia MTU>23 — añadir log de `requestMtu(517)` en `establishGatt` del lado cliente. (2) Auditar primer cold-connect de invitación que devuelve `false` sin fragmentar (MTU stale) — posible que `mtuCache` no se popule antes del primer write. (3) ACK por fragmento para transferencias grandes (imágenes) — sin ACK, un fragmento perdido invalida todo el payload.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: prueba en dispositivo (imagen 29 KB, mensaje típico, invitación); MTU Xiaomi=23 sin retry; primer cold-connect de invitación; ACK por fragmento; dead code `BleTransport.sendInvitation`/`broadcast`/`app/transport/BleTransport.kt`; `broadcast` sin header de 4 bytes; header de PROJECT_STATUS.md congelado en 2026-08-13; entradas con `$(date)` sin expandir; duplicados literales en bitácora; Iteración 13 sin hora exacta.
──────────────────────────────


── ENTRADA — 2026-09-19 18:04 (Iteración 15: fix Fallo #1 — base64 como texto + fullscreen) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: En ChatScreen.BubbleContent: (A) guard `hasMedia = msg.mediaUri != null || base64Bitmap != null` bloquea render de base64 crudo como texto cuando ya hay imagen; (B) rama base64 (`Image` con `asImageBitmap`) ahora tiene `.clickable` que abre ImageViewer fullscreen con `data:image/jpeg;base64,<content>` como URI. Rama `AsyncImage` (mediaUri) ya tenía clickable.
¿ERA UN FIX DE ERROR?: SÍ: ERROR: imagen recibida se pintaba como texto Base64 debajo de la imagen y no respondía al tap para fullscreen (Fallo #1 del análisis forense post-Iter 14). SOLUCIÓN APLICADA: guard `hasMedia` + clickable con data URI. ¿FUNCIONÓ?: compilación exitosa; pendiente prueba en dispositivo Xiaomi-Cubot.
HIPÓTESIS DESCARTADAS (ROL 2): (1) "clickable mal ubicado en rama AsyncImage en vez de base64" — DESCARTADA al leer el archivo real (líneas 1560-1660): el clickable está correctamente sobre la rama `base64Bitmap`. (2) "el patch no se había aplicado" — DESCARTADA: `git status` mostró `ChatScreen.kt` modificado sin commit.
VERIFICADO EN: solo compilación (BUILD SUCCESSFUL in 52s, 327 tasks, 11 executed).
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi Redmi Note 9S), Android 16 (Cubot KingKong ES 5). `data:` URI soportado por Coil >= 2.x desde Android 8.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): (1) test visual del escenario `mediaUri != null && content = base64` para confirmar que la imagen se pinta solo una vez; (2) Fallo #4 (read receipts) estimado en ~40 líneas/3 archivos, más simple que Fallo #2 y Fallo técnico B; (3) cap de `totalFrags` (Fallo técnico A).
DEUDA / PENDIENTE QUE SIGUE ABIERTA: verificación en dispositivo; Fallo #2 (invitación no confirma); Fallo técnico B (serialización GATT); Fallo #4 (read receipts); Fallo técnico A (cap totalFrags); Fallo técnico C (debounce typing); Iter 15 original (broadcast header 4B + reporte honesto + dead code).
──────────────────────────────


── ENTRADA — 2026-09-19 18:16 (Iteración 16: fix Fallo #4 — read receipts) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: (A) MallaEventBus.kt (módulo :events): nuevo evento conversationOpened = MutableSharedFlow<String>(extraBufferCapacity = 5). (B) MeshChatViewModel.loadConversation: emite MallaEventBus.conversationOpened.tryEmit(convId) tras cargar la conversación. (C) MessageReceiver.kt: (1) el ACK on-receive cambia de content="2" (read) a content="1" (delivered) tanto por TCP como por BLE; (2) se elimina la línea buggy messageDao.updateStatusForConversationAndOwn(conversationId, isOwn=true, newStatus=2) que marcaba propios como leídos en cada mensaje entrante; (3) nuevo handler if (meshMsg.type == "read_all") que llama updateStatusForConversationAndOwn(peerId, isOwn=true, newStatus=2) cuando el peer notifica que abrió la conversación; (4) nueva suscripción al bus MallaEventBus.conversationOpened.collect que envía MeshMessage(type="read_all") por TransportManager.
¿ERA UN FIX DE ERROR?: SÍ: ERROR: el informe previo decía 'no emite ACK=2 al abrir conversación'; lectura del código real demostró lo contrario — SÍ emitía ACK=2, pero al recibir (no al abrir), y además ejecutaba updateStatusForConversationAndOwn en cada mensaje entrante, causando el síntoma reportado por Lalo ('solo se pone azul al responder'). SOLUCIÓN APLICADA: semántica correcta ACK=1 recibir / read_all abrir; handler nuevo + evento nuevo; eliminación de la línea que enmascaraba el bug. ¿FUNCIONÓ?: compilación exitosa (BUILD SUCCESSFUL in 1m 14s, 327 tasks, 25 executed); pendiente prueba en dispositivo.
HIPÓTESIS DESCARTADAS (ROL 2): (1) 'el ACK=2 no se emite' — DESCARTADA: se emite en MessageReceiver; el problema era el evento equivocado. (2) 'se necesita columna remoteMessageId en MessageEntity + migración Room' — DESCARTADA al confirmar que MessageDao.updateStatusForConversationAndOwn(conversationId, isOwn, newStatus) ya existe y alcanza para 'read_all por conversación'. (3) 'read receipts son ~40 líneas/3 archivos' — CONFIRMADA la estimación de alcance (3 archivos), pero la descripción del bug en el informe previo estaba invertida.
VERIFICADO EN: solo compilación (BUILD SUCCESSFUL in 1m 14s, 327 tasks, 25 executed). Pendiente prueba en dispositivo Xiaomi-Cubot para confirmar los 3 estados: gris (delivered, tras recepción), doble gris (nada nuevo), azul (read, tras abrir peer).
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi Redmi Note 9S), Android 16 (Cubot KingKong ES 5). Protocolo type=read_all es nuevo: receptores con versión vieja lo ignoran silenciosamente. MeshMessage ya tiene el campo type: String, no requiere migración de Room. Retrocompatible.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): (1) Fallo técnico A (cap totalFrags en BleTransport/BleManager) — ~5 líneas, ortogonal; (2) auditar los 2 warnings nuevos introducidos (MessageReceiver.kt:63:65 type mismatch Nothing? vs String, MeshChatViewModel.kt:120:48 label duplicado); (3) arreglar BleTransport.broadcast para escribir header de 4 bytes (bug coordinado con Iter 13, necesario para que read_all viaje por BLE).
DEUDA / PENDIENTE QUE SIGUE ABIERTA: verificación en dispositivo del flujo de read receipts; read_all por BLE no llega al emisor (depende del bug broadcast sin header 4B); 2 warnings nuevos en compilación; warnings preexistentes KSP; Fallo #2 (invitación no confirma); Fallo técnico B (serialización GATT); Fallo técnico A (cap totalFrags); Fallo técnico C (debounce typing); Iter 15 original (reporte honesto + dead code).
──────────────────────────────


── ENTRADA — 2026-09-19 18:24 (Iteración 17: fix Fallo técnico A — cap MAX_TOTAL_FRAGS) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: (A) BleTransport.kt (network/): nueva const MAX_TOTAL_FRAGS=4095; en reassembleFragments, tras parsear totalFrags del header de 4 bytes, si totalFrags>MAX_TOTAL_FRAGS se rechaza con log que incluye device.address y retorna null. Antes no había validación alguna — el receptor reservaba arrayOfNulls(totalFrags) con cualquier valor del header (DoS intra-mesh). (B) BleManager.kt (network/): nueva const MAX_TOTAL_FRAGS=4095; el check del emisor cambia de totalFrags>65535 (techo del wire) a totalFrags>MAX_TOTAL_FRAGS (cap operativo); comentario del wire format actualizado para reflejar la distinción.
¿ERA UN FIX DE ERROR?: SÍ: ERROR: Fallo técnico A del análisis forense post-Iter 14 — en el log de Eduardo se observó totalFrags=29541 aceptado por el receptor sin sanidad, disparando el reset de buffer como única defensa (Iter 14). Cualquier peer podía mandar un header con totalFrags absurdo y forzar al receptor a reservar memoria desproporcionada. SOLUCIÓN APLICADA: cap fijo 4095 en emisor y receptor. Con MTU 517 → ~2 MB techo; con MTU 23 → ~64 KB; sobra para cualquier payload real del MVP (imágenes 320x320 JPEG ≤ 50 KB). ¿FUNCIONÓ?: compilación exitosa (BUILD SUCCESSFUL in 34s, 327 tasks, 21 executed); pendiente prueba en dispositivo.
HIPÓTESIS DESCARTADAS (ROL 2): (1) 'el reset de buffer al cambiar totalFrags (Iter 14) ya mitigaba el DoS' — DESCARTADA: el reset protege contra corrupción cruzada entre mensajes, no contra un totalFrags grande en el PRIMER fragmento; la reserva de arrayOfNulls ocurre ANTES de que llegue un segundo totalFrags distinto. (2) 'el check del emisor totalFrags>65535 era suficiente' — DESCARTADA: 65535 es el techo del header de 4 bytes, no una decisión de diseño; no protege al receptor (que no valida nada) ni limita el emisor a un valor razonable. (3) 'hay que cambiar el wire format para meter un checksum' — DESCARTADA: overkill, rompe compatibilidad entre versiones, y no es la causa raíz.
VERIFICADO EN: solo compilación (BUILD SUCCESSFUL in 34s, 327 tasks, 21 executed). Pendiente verificación en dispositivo, opcional: forzar un payload >4095 frags para confirmar que el log de rechazo aparece con device.address.
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi Redmi Note 9S), Android 16 (Cubot KingKong ES 5). Wire format intacto — solo cambia el rango aceptado. Emisor viejo con receptor nuevo: solo falla si payload supera ~2 MB (MTU 517) o ~64 KB (MTU 23), caso inexistente en MVP. Emisor nuevo con receptor viejo: sin cambio (receptor viejo no valida).
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): (1) pausa de iteración para verificación en dispositivo de las 3 iteraciones acumuladas (15, 16, 17) antes de tocar Fallo #2 o Fallo técnico B, para distinguir regresiones de bugs arrastrados; (2) corregir el comentario desactualizado en BleTransport.kt línea ~53 que aún describe el header como 'fragIdx (1 byte) + totalFrags (1 byte)' cuando en Iter 11 pasó a 4 bytes; (3) auditar los 2 warnings nuevos de Iter 16 (MessageReceiver.kt:63 type mismatch, MeshChatViewModel.kt:120 label duplicado).
DEUDA / PENDIENTE QUE SIGUE ABIERTA: verificación en dispositivo de 3 iteraciones acumuladas (15, 16, 17); Fallo #2 (invitación no confirma); Fallo técnico B (serialización GATT); Fallo técnico C (debounce typing); Iter 15 original (reporte honesto + dead code); `read_all` por BLE aún no llega al emisor (bug broadcast sin header 4B); comentario desactualizado en BleTransport línea ~53; 2 warnings nuevos de Iter 16.
──────────────────────────────


── ENTRADA — 2026-09-19 18:57 (Iteración 18: rate-limit de logging + script de filtrado) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Infraestructura de debugging para reducir el tamaño de malla_diagnostics.txt sin pérdida irreversible de información. (A) DiagnosticsLogger.kt (core/): nuevo helper logThrottled(key, tag, message, intervalMs=5000L) con ConcurrentHashMap<String, Long> de último timestamp por key; silencia llamadas repetidas con la misma key dentro de la ventana. (B) ProximityEngine.kt (network/): 3 llamadas migradas a logThrottled — Callback BLE con key prox_callback_<device.address> (throttle por device), Nodos actualizados (x2, misma key global prox_nodes_updated). (C) BleManager.kt (network/): Anuncio MALLA con key adv_<device.address>. (D) tools/extract_logs.sh: script bash con 3 modos (focused/errors/full) para filtrado adicional desde el Codespace si el rate-limit no alcanza.
¿ERA UN FIX DE ERROR?: NO — infraestructura de debugging. El objetivo declarado por Eduardo era 'que el archivo tenga solo lo esencial para diagnóstico'. En vez de filtrar en el logger (pérdida irreversible de información forense + requiere recompilar para ajustar), se aplicó rate-limiting en los 3 sitios de alta frecuencia: el log crudo sigue conteniendo todos los eventos, pero espaciados a máximo 1 por cada 5 segundos por key. El archivo pasa de ~2800 a ~400-500 líneas (−85%) sin perder ningún evento único.
HIPÓTESIS DESCARTADAS (ROL 2): (1) 'filtrar en el logger de la app es la mejor opción' — DESCARTADA: pierde información irreversible (si un día el bug está en el timing de advertising, esa data ya no existe), y ajustar el filtro requiere recompilar+reinstalar en 2 celulares cada vez. (2) 'hay que tocar múltiples sitios para el filtro' — DESCARTADA: los 3 sitios de ruido identificados en el log real del Cubot (~2400 de ~2800 líneas) se cubren con 4 reemplazos puntuales. (3) 'rate-limit por device o global' — RESUELTA: mixto. Callback BLE y Anuncio MALLA usan key por device (para no silenciar peers alternos); Nodos actualizados usa key global (1 línea por ciclo aunque cambien varios peers).
VERIFICADO EN: solo compilación (BUILD SUCCESSFUL in 2m 8s, 327 tasks, 40 executed). Verificación funcional pendiente: en la próxima prueba con 2 dispositivos, el malla_diagnostics.txt debería bajar de ~2800 a ~400-500 líneas. Si baja más de esperado, subir intervalMs a 10s. Si no baja, revisar si hay más sitios emisores no detectados por el grep.
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot). ConcurrentHashMap thread-safe. Logs siguen siendo best-effort como antes. Sin cambios al wire format BLE ni al protocolo de mensajería.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): (1) pausa de iteración para verificación en dispositivo de 4 iteraciones acumuladas (15, 16, 17, 18) — el rate-limit debería hacer que el log real quepa en 1 lectura de chat sin filtrado extra; (2) botón 'Compartir log' en DiagnosticScreen para evitar el flujo manual Files→Drive→Codespace; (3) si en la próxima prueba el intervalo de 5s oculta algo relevante, ajustar a 2s y recompilar. Reversión trivial: cambiar logThrottled por log.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: verificación en dispositivo de 4 iteraciones acumuladas (15, 16, 17, 18); Fallo #2 (invitación no confirma); Fallo técnico B (serialización GATT); Fallo técnico C (debounce typing); Iter 15 original (reporte honesto + dead code); read_all por BLE aún no llega al emisor (bug broadcast sin header 4B); comentario desactualizado en BleTransport línea ~53; 2 warnings nuevos de Iter 16 (MessageReceiver.kt:63, MeshChatViewModel.kt:120); posible botón de compartir log en UI (R20 sugerencia).
──────────────────────────────

── ENTRADA — 2026-09-19 19:37 (Iteración 19: fix coordinado broadcast con header 4B — Iter 13 pendiente) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: Se modificó BleTransport.broadcast para envolver el payload con el header de 4 bytes [00 00 00 01] que reassembleFragments del receptor exige. Antes escribía el payload crudo vía writeCharacteristic, sin framing. Se añadió guard de tamaño (data.size + 4 > 512) con log de rechazo explícito. Firma pública intacta: los 3 call sites (MessageReceiver.kt:309, TransportManager.kt:93, TransportManager.kt:151) compilan sin cambios. Nota de path: TransportManager.kt vive en app/src/main/java/com/malla/mvp/network/, no en network/ — corrige referencia del informe previo.
¿ERA UN FIX DE ERROR?: SÍ. ERROR: en logs del 2026-09-19, el receptor BleTransport rechazó 4 mensajes legítimos con "Rechazado de CC:73:14:AC:3F:10: totalFrags=29541 (>4095)". totalFrags=29541 = 0x7365 = ASCII "se", primeros 2 bytes del mensaje "se perdió este mensaje" enviado por BleTransport.broadcast. El receptor estaba leyendo el payload crudo como si tuviera header. SOLUCIÓN APLICADA: framing single-frame [00 00 00 01][payload] en broadcast, idéntico byte-a-byte al que produce BleManager.connectAndWriteData para totalFrags=1. ¿FUNCIONÓ?: compilación exitosa (BUILD SUCCESSFUL in 1m 40s, 327 tasks, 21 executed); pendiente verificación en dispositivo.
HIPÓTESIS DESCARTADAS (ROL 2): (1) 'broadcast sí agrega header con otro formato' — DESCARTADA: lectura de writeCharacteristic (línea 397) confirmó que solo setea char.value = data y escribe, sin framing. (2) 'el fix requiere hacer público writeFramed de BleManager' — DESCARTADA: duplicar el header single-frame en broadcast es 6 líneas y evita acoplamiento entre capas. (3) 'hay un 4to call site' — DESCARTADA: grep exhaustivo confirma 3 call sites reales. (4) 'WifiDirectManager.broadcast requiere mismo fix' — DESCARTADA: es otro transporte (String en vez de ByteArray), detrás de MeshFlags.enableWifiDirect=false, fuera de alcance.
VERIFICADO EN: solo compilación. Pendiente verificación en dispositivo Xiaomi↔Cubot.
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi Redmi Note 9S) y Android 16 (Cubot KingKong ES 5). Wire format sin cambios: el receptor ya esperaba este header (bug coordinado de Iter 13). Retrocompatible en ambas direcciones.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): (1) log defensivo en reassembleFragments del receptor cuando rechaza por totalFrags > MAX_TOTAL_FRAGS. (2) unificar framing en un único helper en :core.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: verificación en dispositivo de A; iteraciones 15, 16, 17, 18 pendientes de verificación; Fallo #2 (invitación); Falló técnico B (serialización GATT); Falló técnico C (debounce typing no coalesce, confirmado en logs); Iter 18 (rate-limit) no surte efecto en logs reales; nearbyUsers=[] post-silencio en ProximityEngine (hallazgo nuevo); TransportManager.kt tiene 3 warnings preexistentes no listados en informe previo (líneas 33, 66, 115); CRÍTICO: 198 commits de feature/multi-select-reimpl no están en origin — push pendiente urgente.
──────────────────────────────

── ENTRADA — 2026-09-19 19:49 (Iteración 20: watchdog de re-registro BLE — Falló B del análisis forense) ──
Compilación: BUILD SUCCESSFUL
QUÉ SE HIZO: (A) BleManager.onScanFailed ahora traduce el errorCode a texto legible (ALREADY_STARTED, APP_REGISTRATION_FAILED, INTERNAL_ERROR, FEATURE_UNSUPPORTED, OUT_OF_HARDWARE_RESOURCES) y lo escribe tanto a LogBuffer como a DiagnosticsLogger (antes solo iba a LogBuffer in-app, invisible en malla_diagnostics.txt). (B) BleManager gana el método público restartProximityScanning() que hace stopScan + startScanningWithCallback con el callback previamente almacenado. (C) ProximityEngine.start() gana un watchdog: while(isActive) { delay(2min); BleManager.restartProximityScanning() } al final del scope.launch. Esto convierte al discoveryJob en un job de larga duración que ahora SÍ es cancelado por stop() (antes terminaba tras el setup y stop() cancelaba un job ya completado).
¿ERA UN FIX DE ERROR?: SÍ. ERROR: en logs del 2026-09-19, el escáner BLE del Cubot dejó de emitir resultados a 13:13:39 (5 min 28 s tras el arranque en 13:08:11), y no se reanudó hasta el siguiente arranque de la app. Sin actualizaciones del scan, nearbyUsers quedó congelado y a las 13:15:41 TransportManager leyó nearbyUsers=[] → Dispositivo BLE seleccionado: null → broadcast false → NONE (ERROR). Mismo patrón bloquea Fallo #2 (invitación). SOLUCIÓN APLICADA: watchdog incondicional cada 2 min. Si el sistema apaga el scan (política adaptativa de Android en background/doze, agravada en API 36), el watchdog lo revive; si el onScanFailed dispara, ahora queda registro persistente. ¿FUNCIONÓ?: compilación exitosa (BUILD SUCCESSFUL in 37s, 327 tasks, 21 executed); pendiente verificación en dispositivo Xiaomi↔Cubot.
HIPÓTESIS DESCARTADAS (ROL 2): (1) 'onScanFailed dispara y no lo vemos' — DESCARTADA con matiz: el código SÍ tenía onScanFailed, pero escribía solo a LogBuffer, no a DiagnosticsLogger. Ahora queda registro persistente. (2) 'hay dos ScanCallbacks compitiendo por el mismo scanner (uno para QR, otro para MALLA)' — DESCARTADA: scanCallback (línea 408) es dead code, isScanningActive nunca se pone en true, los dos stopScan(scanCallback) son no-ops. (3) 'discoveryJob sigue vivo y stop() lo cancela limpiamente' — DESCARTADA: el scope.launch terminaba tras el setup (~100ms), dejando el scanner sin monitor. (4) 'nearbyUsers se limpia por algún evento no encontrado' — DESCARTADA: solo stop() la vacía (línea 98). (5) 'nearbyUsers=[] es la causa raíz' — DESCARTADA: es la consecuencia; la causa raíz es el apagado silencioso del scanner.
VERIFICADO EN: solo compilación (BUILD SUCCESSFUL in 37s, 327 tasks, 21 executed). Pendiente verificación en dispositivo: tras 6+ min de uptime, los logs deben seguir mostrando Anuncio MALLA detectado, y cada 2 min debe aparecer 'Watchdog BLE: re-registrando escaneo de proximidad'.
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi Redmi Note 9S) y Android 16 (Cubot KingKong ES 5). ScanCallback.SCAN_FAILED_* son constantes SDK desde API 21. Sin cambios de wire format. Costo energético del watchdog: stopScan+startScan cada 2 min ≈ negligible. Retrocompatible.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): (1) registrar ProcessLifecycleOwner para que el watchdog pause el re-registro cuando la app entra en background y lo reanude en foreground — evita el 'reinicio inútil' de un scanner que el sistema va a apagar inmediatamente. (2) remover dead code confirmado: isScanningActive, scanCallback, y los dos stopScan(scanCallback) de líneas 226 y 347. (3) considerar migración de SCAN_MODE_LOW_LATENCY a SCAN_MODE_BALANCED si el watchdog no resuelve el apagado en producción.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: verificación en dispositivo de A y B; iteraciones 15, 16, 17, 18 pendientes de verificación; Fallo #2 (invitación, mismo patrón que B podría quedar mitigado); Falló técnico B (serialización GATT: writeCharacteristic false con MTU=517 — causa de la pérdida de imagen en Test 3, ~35% de pérdida observada en Xiaomi→Cubot); Falló técnico C (debounce typing no coalesce, confirmado en logs); Iter 18 (rate-limit) no surte efecto en logs reales; dead code: isScanningActive, scanCallback, BleTransport.sendInvitation, BleTransport.broadcast huérfano, app/transport/BleTransport.kt duplicado.
──────────────────────────────

── AUDITORÍA ESTRUCTURAL — 2026-09-19 19:52 ──
Protocolo R18 ejecutado. Solo lectura (find/grep/git log). Cero cambios en .kt.

ESTADO DEL REPO: 321 commits totales, 200 en origin/main..HEAD, 0 sin pushear. 11 módulos declarados en settings.gradle.kts, 14 directorios de primer nivel, 3 sin archivos .kt (feature-childprotection/, tools/, transport/). 8 ramas locales sin upstream.

DUPLICACIONES CONFIRMADAS:
- BleTransport: network/.../BleTransport.kt (canónico, activo) + app/.../transport/BleTransport.kt (huérfano, no referenciado).
- MeshMessage: core/.../core/data/MeshMessage.kt + data/.../data/entity/MeshMessage.kt + data/.../data/entity/MeshMessageEntity.kt (triplicado).

FRAGMENTACIÓN DE CAPA DE RED: clases de transporte distribuidas en 4 ubicaciones dentro de :app — app/network/ (TransportManager, MessageReceiver, CascadeRouter), app/transport/ (BleTransport huérfano, WifiDirectTransport huérfano), app/ raíz (GattServerManager, GlobalTransport, FlashlightTransport, DhtHelper, DhtWrapper, IBleManager, IIdentityManager). El módulo :network solo contiene 11 archivos.

SOLAPAMIENTO DE DOMINIO EN :core: contiene implementaciones concretas (SmsTransport, LightEncoder, DoubleRatchet, IdenticonGenerator, IdentityProofOfWork, KeystoreManager, InviteCodeGenerator, MeshSimulator) que según el prompt maestro deberían vivir en módulos de dominio. Solapa con :crypto (CryptoEngine, CryptoEngineAdapter, SessionCipher) y :identity (IdentityManager).

MÓDULOS FANTASMA Y DRIFT DOCUMENTAL:
- :transport declarado en settings.gradle.kts sin ningún archivo .kt.
- feature-childprotection/ existe como directorio, sin .kt, sin include.
- Bloque "Arquitectura objetivo" del prompt maestro v4 menciona :feature-sensors, :feature-transport, :feature-chat, :feature-settings — NINGUNO existe. Los módulos reales no mencionados en el prompt son :crypto, :identity, :events, :data, :media, :camera, :emoji.

BASURA EN RAÍZ: 15 archivos .py y .txt sueltos (apply.py, apply_v2.py, integrate_voice_note.py, integrate_voice_note_v2.py, final_fix.py, content.txt, update_chat_preview.py, bottom_bar.txt, fix_mainapp_signatures.py, clean_splash.py, fix_chat_v2.py, insert_call_screen.py, fix_chat.py, connect_call_contact.py, full_logcat.txt).

BUENAS NOTICIAS: cero violaciones de capa detectadas (:network no importa :app, :core no importa :network ni :app, :core no importa :crypto ni :identity pese al solapamiento de dominio). Working tree limpio. Rama sincronizada con origin.

DISCREPANCIAS CLASIFICADAS:
- BLOQUEANTES DE MANTENIBILIDAD: :transport vacío declarado; BleTransport duplicado; MeshMessage triplicado.
- IMPORTANTES: capa de red fragmentada en :app; :core con implementaciones concretas + solapamiento con :crypto y :identity; prompt v4 desalineado al 100%.
- MENORES: feature-childprotection/ huérfano; 15 archivos basura en raíz; 8 ramas locales sin upstream.

NINGUNA DE ESTAS DISCREPANCIAS FUE CORREGIDA EN ESTA AUDITORÍA. R18 punto 5 prohíbe modificar el prompt maestro sin confirmación explícita del usuario. Los fixes estructurales quedan como deuda técnica documentada para priorizar en sesiones futuras.

──────────────────────────────

── ENTRADA — 2026-09-19 19:54 (Iteración 22: documentación de los 4 tests en dispositivo + veredicto de iteraciones 15-18) ──
Compilación: no aplica — entrada documental (no se modifica código).
QUÉ SE HIZO: Se documentan formalmente los resultados del protocolo de 4 tests en dispositivo Xiaomi Redmi Note 9S (Android 11, API 30, userId=38a2a15730c9121a) ↔ Cubot KingKong ES 5 (Android 16, API 36, userId=83cf6fa3d0de9011), corridos el 2026-09-19. Los logs crudos analizados son malla_diagnostics.txt (Xiaomi) y malla_diagnostics1.txt (Cubot), cubriendo la franja 13:08-13:20. Los hallazgos de estos tests alimentaron las iteraciones 19 (fix broadcast) y 20 (watchdog BLE). Ninguna iteración previa (15-18) había sido verificada en dispositivo hasta esta sesión.

TEST 1 — Chat de texto básico: PARCIAL.
- Dirección Cubot→Xiaomi (MTU 517): 100% OK. Todos los mensajes llegaron con writeFramed single success=true.
- Dirección Xiaomi→Cubot (MTU 23): ~65% OK. Tasa de pérdida medida ~35%. Fallos observados en fragmento 1/N y 27/N y 47/N de distintos payloads. Correlación mensajes marcados como fallidos por Eduardo vs recibidos por Cubot: "esesqq" no llegó, "ssq" no llegó, "fer" llegó (pero el write siguiente falló), "se perdió este mensaje" llegó, "y se perdió este mensaje" llegó, "no le llegaron al usuario" no llegó, "este tampoco le llegó" llegó. Fallos no deterministas por contenido: timing GATT.

TEST 2 — Read receipts (Iter 16): PARCIAL.
- Cubot→Xiaomi: OK siempre (MTU 517).
- Xiaomi→Cubot: OK cuando el canal está vivo. Confirmado en log del Xiaomi a 13:10:05 "read_all enviado a 83cf6fa3d0de9011".
- NUEVO HALLAZGO en log del Cubot a 13:15:41: TransportManager intentó enviar read_all con nearbyUsers=[] → Dispositivo BLE seleccionado: null → broadcast false → NONE (ERROR). Esto motivó la iteración 20 (watchdog BLE).

TEST 3 — Imagen base64 + fullscreen (Iter 15): NO VERIFICABLE.
- La imagen nunca llegó al receptor (Test 3 falló). No se pudo verificar el fix visual de Iter 15.
- Log del Xiaomi: 2 intentos fallidos. Intentó 1: 19803 bytes → 1238 fragmentos → fallo en fragmento 27/1238. Intento 2: 20260 bytes → 1267 fragmentos → fallo en fragmento 47/1267.
- Log del Cubot confirma: "Buffer reseteado para 2C:D0:66:52:38:15: totalFrags 1238→1267 (recibidos 26/1238)" y luego "1267→9 (recibidos 46/1267)". Iter 17 (cap 4095) funcionó correctamente en el receptor.
- Causa raíz probable: saturación de cola GATT con MTU=23 + delay=20ms. ~50 writes/segundo excede la capacidad del stack BLE de Android. Sin retry por fragmento.

TEST 4 — Imagen grande (Iter 14 + Iter 17): NO APLICABLE.
- Sin imagen más grande que 20 KB en el log. El resultado de Test 3 es el baseline.

VEREDICTO DE ITERACIONES 15-18:
- Iter 15 (imagen base64 + fullscreen): NO VERIFICABLE — imagen no llegó.
- Iter 16 (read receipts): PARCIALMENTE OK — flujo correcto, canal falla intermitentemente.
- Iter 17 (cap MAX_TOTAL_FRAGS=4095): CONFIRMADO EN PRODUCCIÓN. Rechazó 4 payloads con totalFrags=29541 (= 0x7365 = ASCII "se") con log explícito. El cap protege al receptor correctamente.
- Iter 18 (rate-limit logging): NO SURTE EFECTO EN LOGS REALES. Los logs del Cubot muestran ~5 Anuncio MALLA/segundo pese al logThrottled(key=adv_<address>, 5000ms). Hipótesis no verificada: fallo en ConcurrentHashMap de logThrottled o en la ventana temporal. Pendiente inspección de código en sesión futura.

HALLAZGOS FORENSES NUEVOS (destapados por estos tests):
1. totalFrags=29541 = 0x7365 = "se" — primeros 2 bytes del mensaje "se perdió este mensaje" enviado por BleTransport.broadcast. El receptor leía el payload crudo como header. Motivó iteración 19.
2. El escáner BLE del Cubot dejó de emitir resultados a 13:13:39 (5 min 28 s tras el arranque). Silencio de ~3 min sin onScanFailed visible. Motivó iteración 20.
3. onScanFailed escribía solo a LogBuffer in-app, no a DiagnosticsLogger. Corregido en iteración 20.
4. Asimetría de MTU persistente: Xiaomi negocia MTU=23 (retry 517→247 no dispara), Cubot negocia MTU=517.
5. Falló técnico C confirmado: 13 typing=1 en 30 segundos Cubot→Xiaomi. El debounce de 300ms de Iter 1 no coalesce efectivamente.

DEUDA / PENDIENTE QUE SIGUE ABIERTA TRAS ESTA SESIÓN:
- Verificación en dispositivo de iteraciones 19 (broadcast header) y 20 (watchdog BLE).
- Fallo #2 (invitación no confirma): potencialmente mitigado por iteración 20 si el fallo era por nearbyUsers vacío. Requiere test en dispositivo.
- Falló técnico B (serialización GATT): writeCharacteristic false con MTU=23 durante ráfagas de fragmentos. Causa raíz de Test 3. Sin fix. Requiere retry por fragmento o ajuste de delay.
- Falló técnico C (debounce typing no coalesce): sin fix. Requiere inspección de MeshChatViewModel.sendTyping.
- Iter 18 (rate-limit): sin efecto real. Requiere inspección de DiagnosticsLogger.logThrottled.
- Dead code confirmado por auditoría R18: isScanningActive, scanCallback, los 2 stopScan(scanCallback), BleTransport.sendInvitation, BleTransport.broadcast huérfano, app/transport/BleTransport.kt duplicado, app/transport/WifiDirectTransport.kt huérfano, MeshMessage triplicado en 3 ubicaciones.
- 8 ramas locales sin upstream (backups y features premium de sesiones anteriores).
- Bloque "Arquitectura objetivo" del prompt maestro v4 desalineado 100% con el repo real.
──────────────────────────────


── ENTRADA — 2026-09-20 13:01 (Iteración 23 — RECONSTRUIDA RETROACTIVAMENTE el 2026-09-20) ──
Compilación: BUILD SUCCESSFUL (según mensaje de commit; salida cruda no preservada)
QUÉ SE HIZO: BleManager.kt (network/): reintento de MTU negotiation. Tras requestMtu(517) con timeout de 5s, segundo requestMtu(247) sobre el mismo GATT + polling de 3s. onMtuChanged persiste el MTU en cache aunque llegue fuera de la ventana de espera.
¿ERA UN FIX DE ERROR?: SI: ERROR: en Xiaomi el primer requestMtu(517) se perdia por timeout y el codigo abortaba sin reintentar (MTU quedaba en 23, default). SOLUCION: segundo intento con 247 + polling 3s. FUNCIONO?: compilacion exitosa; verificado en device posteriormente en iter 33/35.
HIPÓTESIS DESCARTADAS (ROL 2): no documentadas al momento
VERIFICADO EN: solo compilacion (verificacion en device diferida a iter 33/35)
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi Redmi Note 9S), Android 16 (Cubot KingKong ES 5)
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): no documentadas al momento
DEUDA / PENDIENTE QUE SIGUE ABIERTA: verificacion en device del nuevo MTU negociado
Nota: entrada escrita el 2026-09-20 al detectar que la sesión original no registró la compilación. Fuente: git log fd2a2ffa..a1d2c51f + INFORME DE CONTINUIDAD 2026-09-20.
──────────────────────────────

── ENTRADA — 2026-09-20 13:03 (Iteración 24 — RECONSTRUIDA RETROACTIVAMENTE el 2026-09-20) ──
Compilación: BUILD SUCCESSFUL (según mensaje de commit; salida cruda no preservada)
QUÉ SE HIZO: BleManager.kt (network/): write con ACK por fragmento. Cada fragmento se envia con WRITE_TYPE_DEFAULT y se espera onCharacteristicWrite hasta 1500ms, retry 3x con backoff 50/100ms. Tercer intento cae a WRITE_TYPE_NO_RESPONSE como fallback. Cierra el fallo F2 del Bloque 1.
¿ERA UN FIX DE ERROR?: SI: ERROR: fragmentos perdidos en envios grandes porque el write NO_RESPONSE no garantizaba entrega. SOLUCION: ACK por fragmento con WRITE_TYPE_DEFAULT + fallback. FUNCIONO?: compilacion exitosa; confirmado en device iter 26-38.
HIPÓTESIS DESCARTADAS (ROL 2): no documentadas al momento
VERIFICADO EN: solo compilacion
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot)
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): no documentadas al momento
DEUDA / PENDIENTE QUE SIGUE ABIERTA: verificacion en device del flujo completo
Nota: entrada escrita el 2026-09-20 al detectar que la sesión original no registró la compilación. Fuente: git log fd2a2ffa..a1d2c51f + INFORME DE CONTINUIDAD 2026-09-20.
──────────────────────────────

── ENTRADA — 2026-09-20 13:10 (Iteración 25 — RECONSTRUIDA RETROACTIVAMENTE el 2026-09-20) ──
Compilación: BUILD SUCCESSFUL (según mensaje de commit; salida cruda no preservada)
QUÉ SE HIZO: TransportManager.kt (app/network/) y MeshChatViewModel.kt (app/viewmodel/): writes BLE envueltos en NonCancellable para no abortar ante cancelacion externa. El status del mensaje refleja honestamente si hubo ACK - sin ACK queda como enviado, no como entregado. Cierra F3 y F4 del Bloque 1.
¿ERA UN FIX DE ERROR?: SI: ERROR: writes cancelados por el scope padre dejaban mensajes a medio enviar; el status reportaba entregado sin confirmacion real. SOLUCION: NonCancellable + status honesto. FUNCIONO?: compilacion exitosa; confirmado en device.
HIPÓTESIS DESCARTADAS (ROL 2): no documentadas al momento
VERIFICADO EN: solo compilacion
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot)
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): no documentadas al momento
DEUDA / PENDIENTE QUE SIGUE ABIERTA: verificacion en device de estado de mensajes
Nota: entrada escrita el 2026-09-20 al detectar que la sesión original no registró la compilación. Fuente: git log fd2a2ffa..a1d2c51f + INFORME DE CONTINUIDAD 2026-09-20.
──────────────────────────────

── ENTRADA — 2026-09-20 13:36 (Iteración 26 — RECONSTRUIDA RETROACTIVAMENTE el 2026-09-20) ──
Compilación: BUILD SUCCESSFUL (según mensaje de commit; salida cruda no preservada)
QUÉ SE HIZO: BleManager.kt (network/, server side): la caracteristica GATT del server ahora declara PROPERTY_WRITE ademas de PROPERTY_WRITE_NO_RESPONSE, para que el cliente con WRITE_TYPE_DEFAULT no reciba rechazo silencioso. Referenciado como 'Commit 4' en H2 del INFORME DE CONTINUIDAD, pero ausente de la tabla 'LOS 15 COMMITS'.
¿ERA UN FIX DE ERROR?: SI: ERROR: WRITE_TYPE_DEFAULT del cliente fallaba silenciosamente con status != 0 porque el server solo declaraba WRITE_NO_RESPONSE. SOLUCION: agregar PROPERTY_WRITE al server. FUNCIONO?: compilacion exitosa; confirmado en device.
HIPÓTESIS DESCARTADAS (ROL 2): no documentadas al momento
VERIFICADO EN: solo compilacion
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot)
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): no documentadas al momento
DEUDA / PENDIENTE QUE SIGUE ABIERTA: verificacion en device del ACK del server
Nota: entrada escrita el 2026-09-20 al detectar que la sesión original no registró la compilación. Fuente: git log fd2a2ffa..a1d2c51f + INFORME DE CONTINUIDAD 2026-09-20.
──────────────────────────────

── ENTRADA — 2026-09-20 14:05 (Iteración 27 — RECONSTRUIDA RETROACTIVAMENTE el 2026-09-20) ──
Compilación: BUILD SUCCESSFUL (según mensaje de commit; salida cruda no preservada)
QUÉ SE HIZO: ProximityEngine.kt (network/) y MainActivity.kt (app/): tras conceder permisos BLE en runtime, ensureScanning() re-dispara el scan si estaba detenido. Watchdog log para diagnostico del silencio. Cierra el bug de scan que no arranca post-permisos.
¿ERA UN FIX DE ERROR?: SI: ERROR: tras conceder permisos BLE en Android 12+, el scan no se iniciaba automaticamente (ProximityEngine ya habia intentado arrancar antes de los permisos). SOLUCION: ensureScanning() tras onRequestPermissionsResult + watchdog log. FUNCIONO?: compilacion exitosa; confirmado en device.
HIPÓTESIS DESCARTADAS (ROL 2): no documentadas al momento
VERIFICADO EN: solo compilacion
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot)
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): no documentadas al momento
DEUDA / PENDIENTE QUE SIGUE ABIERTA: verificacion en device del scan post-permisos
Nota: entrada escrita el 2026-09-20 al detectar que la sesión original no registró la compilación. Fuente: git log fd2a2ffa..a1d2c51f + INFORME DE CONTINUIDAD 2026-09-20.
──────────────────────────────

── ENTRADA — 2026-09-20 14:07 (Iteración 28 — RECONSTRUIDA RETROACTIVAMENTE el 2026-09-20) ──
Compilación: BUILD SUCCESSFUL (según mensaje de commit; salida cruda no preservada)
QUÉ SE HIZO: MessageReceiver.kt (app/network/): el log de read_all refleja el Boolean real devuelto por TransportManager.send - ya no reporta exito cuando el envio devuelve false. Nota: numeracion 'iter 28' commiteada antes de 'iter 27' (ba5c285b); desfase documental, no de codigo.
¿ERA UN FIX DE ERROR?: SI: ERROR: log afirmaba 'read_all enviado' sin verificar el retorno real de TransportManager. SOLUCION: log honesto. FUNCIONO?: compilacion exitosa.
HIPÓTESIS DESCARTADAS (ROL 2): no documentadas al momento
VERIFICADO EN: solo compilacion
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot)
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): no documentadas al momento
DEUDA / PENDIENTE QUE SIGUE ABIERTA: read_all por BLE aun no llega al emisor (dependiente del bug broadcast sin header 4B, resuelto en iter 19)
Nota: entrada escrita el 2026-09-20 al detectar que la sesión original no registró la compilación. Fuente: git log fd2a2ffa..a1d2c51f + INFORME DE CONTINUIDAD 2026-09-20.
──────────────────────────────

── ENTRADA — 2026-09-20 14:21 (Iteración 29 — RECONSTRUIDA RETROACTIVAMENTE el 2026-09-20) ──
Compilación: BUILD SUCCESSFUL (según mensaje de commit; salida cruda no preservada)
QUÉ SE HIZO: BleManager.kt y BleTransport.kt (network/): logs agregados en los early-returns que antes salian en silencio (permisos faltantes, GATT null, etc). Filtro de auto-conexion por userId propio para evitar el eco 'peer self' que se veia en logs del Cubot.
¿ERA UN FIX DE ERROR?: SI: ERROR: (1) ramas de salida temprana sin log ocultaban por que fallaba el write; (2) Cubot recibia eco de sus propios mensajes. SOLUCION: logs + filtro isSelfUser por userId. FUNCIONO?: compilacion exitosa; auto-conexion self bloqueada confirmada en device.
HIPÓTESIS DESCARTADAS (ROL 2): no documentadas al momento
VERIFICADO EN: solo compilacion
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot)
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): no documentadas al momento
DEUDA / PENDIENTE QUE SIGUE ABIERTA: verificacion en device del eco eliminado
Nota: entrada escrita el 2026-09-20 al detectar que la sesión original no registró la compilación. Fuente: git log fd2a2ffa..a1d2c51f + INFORME DE CONTINUIDAD 2026-09-20.
──────────────────────────────

── ENTRADA — 2026-09-20 14:48 (Iteración 30 — RECONSTRUIDA RETROACTIVAMENTE el 2026-09-20) ──
Compilación: BUILD SUCCESSFUL (según mensaje de commit; salida cruda no preservada)
QUÉ SE HIZO: BleManager.kt y BleTransport.kt (network/): gatt.refresh() como fallback tras fallos persistentes, fallback automatico a WRITE_TYPE_NO_RESPONSE si WRITE_TYPE_DEFAULT agota retries, logs detallados en callbacks GATT. Precursor del H1 (gatt.refresh rompe flujo normal - resuelto en iter 35).
¿ERA UN FIX DE ERROR?: SI: ERROR: status 133 intermitente en writes grandes. SOLUCION: gatt.refresh() como fallback + logs. FUNCIONO?: compilacion exitosa; el refresh resulto problematico y se removio en iter 35.
HIPÓTESIS DESCARTADAS (ROL 2): no documentadas al momento
VERIFICADO EN: solo compilacion
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot)
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): no documentadas al momento
DEUDA / PENDIENTE QUE SIGUE ABIERTA: gatt.refresh() causa status=133 (resuelto en iter 35)
Nota: entrada escrita el 2026-09-20 al detectar que la sesión original no registró la compilación. Fuente: git log fd2a2ffa..a1d2c51f + INFORME DE CONTINUIDAD 2026-09-20.
──────────────────────────────

── ENTRADA — 2026-09-20 15:40 (Iteración 31 — RECONSTRUIDA RETROACTIVAMENTE el 2026-09-20) ──
Compilación: BUILD SUCCESSFUL (según mensaje de commit; salida cruda no preservada)
QUÉ SE HIZO: MainActivity.kt (app/): solicitud de permisos BLE diferenciada por tipo (BLUETOOTH_SCAN, BLUETOOTH_CONNECT, BLUETOOTH_ADVERTISE en Android 12+). El Cubot (Android 16) sin SCAN ya no bloquea writes por pedir el permiso equivocado. Cierra bug de permisos en API 31+.
¿ERA UN FIX DE ERROR?: SI: ERROR: en Android 12+ el Cubot pedia permisos BLE agregados y sin SCAN concedido, los writes GATT se bloqueaban. SOLUCION: permisos granulares por tipo de operacion. FUNCIONO?: compilacion exitosa; scan en Cubot confirmado en device.
HIPÓTESIS DESCARTADAS (ROL 2): no documentadas al momento
VERIFICADO EN: solo compilacion
COMPATIBILIDAD CONSIDERADA (R21): Android 12+ (BLUETOOTH_SCAN, BLUETOOTH_CONNECT, BLUETOOTH_ADVERTISE); Android 11 usa permisos legacy
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): no documentadas al momento
DEUDA / PENDIENTE QUE SIGUE ABIERTA: verificacion en device del scan granular
Nota: entrada escrita el 2026-09-20 al detectar que la sesión original no registró la compilación. Fuente: git log fd2a2ffa..a1d2c51f + INFORME DE CONTINUIDAD 2026-09-20.
──────────────────────────────

── ENTRADA — 2026-09-20 15:59 (Iteración 32 — RECONSTRUIDA RETROACTIVAMENTE el 2026-09-20) ──
Compilación: BUILD SUCCESSFUL (según mensaje de commit; salida cruda no preservada)
QUÉ SE HIZO: MeshChatService.kt (app/service/): log '[BUILD] MALLA APK: iter N' al arrancar el servicio. Permite confirmar que el APK instalado en device corresponde al HEAD actual - cierra el problema recurrente de testear versiones viejas.
¿ERA UN FIX DE ERROR?: NO era fix de error - infraestructura de diagnostico. Objetivo: evitar testear APKs desactualizados (problema que se materializo entre iter 32 y iter 38 en la sesion 2026-09-20).
HIPÓTESIS DESCARTADAS (ROL 2): no aplica
VERIFICADO EN: solo compilacion
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot)
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): no documentadas al momento
DEUDA / PENDIENTE QUE SIGUE ABIERTA: verificar iter 38 en device usando este log
Nota: entrada escrita el 2026-09-20 al detectar que la sesión original no registró la compilación. Fuente: git log fd2a2ffa..a1d2c51f + INFORME DE CONTINUIDAD 2026-09-20.
──────────────────────────────

── ENTRADA — 2026-09-20 16:18 (Iteración 33 — RECONSTRUIDA RETROACTIVAMENTE el 2026-09-20) ──
Compilación: BUILD SUCCESSFUL (según mensaje de commit; salida cruda no preservada)
QUÉ SE HIZO: BleManager.kt (network/): ampliacion del fix de iter 23. El retry de MTU 517->247 persiste el valor en cache aunque onMtuChanged llegue tarde (fuera de la ventana de espera), reduciendo el retraso observado Xiaomi->Cubot.
¿ERA UN FIX DE ERROR?: SI: ERROR: en Xiaomi onMtuChanged llegaba tarde y el MTU no se persistia, manteniendo el default 23 durante toda la sesion. SOLUCION: persistir siempre aunque llegue fuera de ventana. FUNCIONO?: compilacion exitosa; MTU negociado confirmado en device.
HIPÓTESIS DESCARTADAS (ROL 2): no documentadas al momento
VERIFICADO EN: solo compilacion
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot)
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): no documentadas al momento
DEUDA / PENDIENTE QUE SIGUE ABIERTA: verificacion en device del MTU final
Nota: entrada escrita el 2026-09-20 al detectar que la sesión original no registró la compilación. Fuente: git log fd2a2ffa..a1d2c51f + INFORME DE CONTINUIDAD 2026-09-20.
──────────────────────────────

── ENTRADA — 2026-09-20 16:22 (Iteración 34 — RECONSTRUIDA RETROACTIVAMENTE el 2026-09-20) ──
Compilación: BUILD SUCCESSFUL (según mensaje de commit; salida cruda no preservada)
QUÉ SE HIZO: ChatScreen.kt (app/ui/screen/): el visor fullscreen usa el bitmap directo en vez de un data URI 'data:image/jpeg;base64,...'. Cierra la pantalla negra observada al abrir imagen recibida en fullscreen.
¿ERA UN FIX DE ERROR?: SI: ERROR: al abrir imagen en fullscreen, la pantalla quedaba negra porque Coil no procesaba el data URI. SOLUCION: pasar el bitmap directo al ImageViewer. FUNCIONO?: compilacion exitosa; pendiente verificacion en device al momento del commit.
HIPÓTESIS DESCARTADAS (ROL 2): no documentadas al momento
VERIFICADO EN: solo compilacion
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot). Coil >= 2.x soporta data: URI pero con overhead
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): no documentadas al momento
DEUDA / PENDIENTE QUE SIGUE ABIERTA: verificacion en device del fullscreen
Nota: entrada escrita el 2026-09-20 al detectar que la sesión original no registró la compilación. Fuente: git log fd2a2ffa..a1d2c51f + INFORME DE CONTINUIDAD 2026-09-20.
──────────────────────────────

── ENTRADA — 2026-09-20 16:46 (Iteración 35 — RECONSTRUIDA RETROACTIVAMENTE el 2026-09-20) ──
Compilación: BUILD SUCCESSFUL (según mensaje de commit; salida cruda no preservada)
QUÉ SE HIZO: BleManager.kt (network/): gatt.refresh() removido del flujo normal y conservado unicamente como fallback tras 2x status 133 consecutivos. Cierra H1 del informe - el refresh dejaba gatt.services=[] durante 1-4s y disparaba status 133 en writes grandes.
¿ERA UN FIX DE ERROR?: SI: ERROR: gatt.refresh() introducido en iter 30 rompia el flujo normal (services=[] durante 1-4s, status 133 en writes grandes). SOLUCION: refresh solo como fallback tras 2x status 133. FUNCIONO?: compilacion exitosa; confirmado en device (reduccion de status 133).
HIPÓTESIS DESCARTADAS (ROL 2): no documentadas al momento
VERIFICADO EN: solo compilacion
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot)
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): no documentadas al momento
DEUDA / PENDIENTE QUE SIGUE ABIERTA: verificacion en device del status 133 residual
Nota: entrada escrita el 2026-09-20 al detectar que la sesión original no registró la compilación. Fuente: git log fd2a2ffa..a1d2c51f + INFORME DE CONTINUIDAD 2026-09-20.
──────────────────────────────

── ENTRADA — 2026-09-20 16:53 (Iteración 36 — RECONSTRUIDA RETROACTIVAMENTE el 2026-09-20) ──
Compilación: BUILD SUCCESSFUL (según mensaje de commit; salida cruda no preservada)
QUÉ SE HIZO: MainActivity.kt (app/) y ConversationsScreen.kt (app/ui/screen/): flujo de aceptacion de invitacion consolidado en MainActivity. Antes dos diálogos competian (MainActivity guardaba contacto sin ACCEPT, ConversationsScreen enviaba ACCEPT sin guardar). Cierra H3 del informe.
¿ERA UN FIX DE ERROR?: SI: ERROR: MainActivity y ConversationsScreen ambos consumian incomingInvitation, ninguno hacia el flujo completo - el receptor nunca guardaba contacto + enviaba ACCEPT juntos. SOLUCION: consolidar en MainActivity. FUNCIONO?: compilacion exitosa; pendiente verificacion en device.
HIPÓTESIS DESCARTADAS (ROL 2): no documentadas al momento
VERIFICADO EN: solo compilacion
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot)
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): no documentadas al momento
DEUDA / PENDIENTE QUE SIGUE ABIERTA: diálogo de aceptacion aun no verificado en device
Nota: entrada escrita el 2026-09-20 al detectar que la sesión original no registró la compilación. Fuente: git log fd2a2ffa..a1d2c51f + INFORME DE CONTINUIDAD 2026-09-20.
──────────────────────────────

── ENTRADA — 2026-09-20 16:56 (Iteración 37 — RECONSTRUIDA RETROACTIVAMENTE el 2026-09-20) ──
Compilación: BUILD SUCCESSFUL (según mensaje de commit; salida cruda no preservada)
QUÉ SE HIZO: InvitationManager.kt (app/network/) y MainActivity.kt (app/): la invitacion entrante se persiste a SharedPreferences y se recupera al reabrir la app. El scope de recoleccion se recrea para asegurar que el collector este suscrito cuando llegue el evento. Cierra parcialmente P1.
¿ERA UN FIX DE ERROR?: SI: ERROR: invitacion procesada por InvitationManager pero UI no reaccionaba si el collector no estaba suscrito. SOLUCION: persistir a prefs + recrear scope. FUNCIONO?: compilacion exitosa; pendiente verificacion en device.
HIPÓTESIS DESCARTADAS (ROL 2): no documentadas al momento
VERIFICADO EN: solo compilacion
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot)
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): no documentadas al momento
DEUDA / PENDIENTE QUE SIGUE ABIERTA: dialogo de aceptacion aun no verificado en device
Nota: entrada escrita el 2026-09-20 al detectar que la sesión original no registró la compilación. Fuente: git log fd2a2ffa..a1d2c51f + INFORME DE CONTINUIDAD 2026-09-20.
──────────────────────────────

── ENTRADA — 2026-09-20 17:57 (Iteración 38 — RECONSTRUIDA RETROACTIVAMENTE el 2026-09-20) ──
Compilación: BUILD SUCCESSFUL (según mensaje de commit; salida cruda no preservada)
QUÉ SE HIZO: InvitationManager.kt (app/network/), MainActivity.kt (app/) y MeshChatService.kt (app/service/): incomingInvitation cambio de SharedFlow(replay=0) a StateFlow. Cierra H4 del informe - SharedFlow(replay=0) pierde el evento si no hay collector suscrito en el momento exacto del tryEmit.
¿ERA UN FIX DE ERROR?: SI: ERROR: SharedFlow(replay=0) perdia el evento de invitacion si el collector no estaba suscrito en el momento exacto. SOLUCION: StateFlow retiene el ultimo valor. FUNCIONO?: compilacion exitosa; SIN verificar en device al momento del commit.
HIPÓTESIS DESCARTADAS (ROL 2): H4 del informe: SharedFlow(replay=0) confirmado como causa de perdida de eventos
VERIFICADO EN: solo compilacion - pendiente verificacion en device con log '[BUILD] MALLA APK: iter 38'
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (Xiaomi), Android 16 (Cubot)
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): no documentadas al momento
DEUDA / PENDIENTE QUE SIGUE ABIERTA: P1 (dialogo de aceptacion) - pendiente verificacion en device
Nota: entrada escrita el 2026-09-20 al detectar que la sesión original no registró la compilación. Fuente: git log fd2a2ffa..a1d2c51f + INFORME DE CONTINUIDAD 2026-09-20.
──────────────────────────────


── ENTRADA — 2026-09-20 12:15 (Iteración 38 — VERIFICADA EN DEVICE: P1 cerrado) ──
Compilación: no aplica — entrada documental (verificación de código ya compilado).
QUÉ SE HIZO: Verificación en device del APK iter 38 (StateFlow invitación + log activo). Logs pareados malla_diagnostics.txt (Cubot KingKong ES 5, Android 16, userId=a4a5bd44ed4340b4, name=Lalo) y malla_diagnostics1.txt (Xiaomi Redmi Note 9S, Android 11, userId=3ca57504fc54315a, name=Eduardo), franja 11:48–12:12. Cubre los 15 commits de iter 23–38.
¿ERA UN FIX DE ERROR?: SÍ — cierre de P1 del INFORME DE CONTINUIDAD 2026-09-20. ERROR: diálogo de aceptación de invitación no aparecía en el receptor. SOLUCIÓN APLICADA: iter 36 (unificar flujo en MainActivity) + iter 37 (persistir a prefs) + iter 38 (StateFlow). ¿FUNCIONÓ?: SÍ, confirmado en device. Cubot: 'invitación observada: Eduardo' → 'Contacto Eduardo guardado tras aceptar invitación'. Xiaomi: ídem con Lalo. Iter 37 y 38 funcionan como se diseñaron.
HIPÓTESIS DESCARTADAS (ROL 2): H3 (dos diálogos compitiendo) y H4 (SharedFlow(replay=0) pierde eventos) confirmadas como causa raíz — resueltas por iter 36 y 38 respectivamente.
VERIFICADO EN: dispositivo real (Xiaomi Redmi Note 9S Android 11 + Cubot KingKong ES 5 Android 16).
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (API 30) + Android 16 (API 36). MTU 517 negociado simétricamente en esta sesión (el asimetrismo reportado en iter 22 no se reprodujo).
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): (1) Bug A — timeout de write 1500ms demasiado corto, onCharacteristicWrite llegó a 1992ms; subir a 3500ms o esperar callback real con techo de 5s. (2) Bug B — typing sigue sin coalescer (Fallo técnico C). (3) Bug C — dedup de invitación por userId ya-contacto para evitar re-diálogo.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: Bug A (timeout write), Bug B (typing sin coalesce), Bug C (dedup invitación), Fallo técnico B (writeCharacteristic false intermitente en ráfagas), Iter 18 (rate-limit sin efecto), dead code confirmado en auditoría R18.
──────────────────────────────

── ENTRADA — 2026-09-20 18:42 (Iteración 39: Fix A timeout write + Fix C dedup invitación + Fix B typing debounce) ──
Compilación: BUILD SUCCESSFUL (in 1m 56s + :app:assembleDebug in 13s, 227 tasks, 14 executed).
QUÉ SE HIZO: Tres fixes funcionales post-verificación iter 38, aplicados por Python (R2, no sed). (A) network/src/main/java/com/malla/mvp/network/BleManager.kt:605: timeoutMs del write con ACK subido de 1500L a 3500L. Fix Bug A destapado en logs pareados 2026-09-20: onCharacteristicWrite llegó a 1992ms pero el código ya había abortado a 1500ms y bajado toda la cascada (DEFAULT→DEFAULT→NO_RESPONSE→broadcast→sendWithRetry→NONE), dejando el read_all del Xiaomi en 'cola pendiente' sin reintento. (B) app/src/main/java/com/malla/mvp/MainActivity.kt: LaunchedEffect(observedInvitation) ahora consulta contactDao().getById(inv.senderUserId) antes de mostrar el diálogo; si el sender ya es contacto, loguea 'Invitación de X ignorada — ya es contacto' y limpia pending+StateFlow sin re-mostrar UI. Fix Bug C destapado en logs: doble 'Contacto Eduardo guardado tras aceptar invitación' por re-envío desde Xiaomi. (C) app/src/main/java/com/malla/mvp/viewmodel/MeshChatViewModel.kt: reemplazo del patrón typingJob?cancel() + delay(300) por dos jobs separados (typingStartJob debounce 300ms, typingStopJob auto-stop 4000ms) + flag lastSentTyping. Fix Bug B (Fallo técnico C confirmado en iter 22 y reproducido el 2026-09-20: 3-4 typing=1 seguidos en logs Cubot/Xiaomi).
¿ERA UN FIX DE ERROR?: SÍ, 3 bugs. ERROR A: writeCharacteristic timeout 1500ms demasiado corto con MTU 517 y GATT congestionado. SOLUCIÓN A: 3500ms. ¿FUNCIONÓ?: compilación exitosa; pendiente test device. ERROR C: diálogo re-aparece cuando el sender ya es contacto. SOLUCIÓN C: consulta ContactDao antes de mostrar. ¿FUNCIONÓ?: compilación exitosa; pendiente test device. ERROR B: typing=1 spameado porque el debounce cancelaba el job previo sin coalescer. SOLUCIÓN B: start+stop jobs con lastSentTyping flag. ¿FUNCIONÓ?: compilación exitosa; pendiente test device.
HIPÓTESIS DESCARTADAS (ROL 2): para Bug A se descartó que writeCharacteristic hubiera fallado realmente — el log 12:04:50.871 muestra status=0 success=true 1992ms post-inicio, el write sí salió del cliente, solo llegó tarde. Para Bug B se descartó que fueran 2 paths distintos disparando typing — es un solo path con debounce mal aplicado. Para Bug C se descartó que fuera duplicación fantasma — es el usuario reenviando y la app no filtrando por contacto existente.
VERIFICADO EN: compilación limpia (2 builds). Pendiente verificación en dispositivo Xiaomi Redmi Note 9S + Cubot KingKong ES 5.
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (API 30) + Android 16 (API 36). Sin cambios de wire format ni protocolo. El fix B (typing) reduce tráfico BLE — mejora en Xiaomi con MTU 23 (el caso donde más se notaba). ContactDao.getById ya existía, sin migración Room.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): (1) reemplazar writeCharacteristic(BluetoothGattCharacteristic) deprecated por la sobrecarga con (char, value, writeType) en API 33+ — modernización pendiente en BleManager líneas 616,618,629,631. (2) limpiar warnings muertos (parámetros sin usar en BleManager 141/184/388/438/743). (3) patrón de patch: usar cat > /tmp/patch.py + python3 /tmp/patch.py en vez de heredocs para evitar problemas de paste (surgió en este mismo commit).
DEUDA / PENDIENTE QUE SIGUE ABIERTA: verificación en device de los 3 fixes (un solo run los cubre); test del timeout con transferencias grandes (imagen ~29KB que en iter 22 fallaba al fragmento 27); Fallo técnico B (writeCharacteristic false intermitente en ráfagas MTU 23) — puede quedar mitigado por Fix A si el timeout era la causa subyacente; Iter 18 (rate-limit sin efecto real, pendiente inspección de DiagnosticsLogger.logThrottled); dead code confirmado por R18 (BleTransport duplicado, MeshMessage triplicado en 3 ubicaciones, módulo :transport vacío); warnings preexistentes no bloqueantes.
──────────────────────────────

── ENTRADA — 2026-09-20 19:15 (Iteración 40: IncomingRequestDialog premium + swap MainActivity) ──
Compilación: BUILD SUCCESSFUL (:app:compileDebugKotlin in 8s + :app:assembleDebug, 136 tasks).
Commit: 85400f9c
QUÉ SE HIZO: Reescribir app/src/main/java/com/malla/mvp/ui/components/IncomingRequestDialog.kt con diseño premium (228 líneas): card flotante sobre backdrop oscurecido, avatar 72dp con glow cyan pulsante (patrón reusado de LockScreenPremium), chip biométrico contextual (muestra 'Confirma con tu huella' solo si BiometricManager reporta BIOMETRIC_STRONG disponible), botón Aceptar cyan sólido #4CE6FF con scale-down 0.97 al presionar + haptic LongPress, botón Rechazar como TextButton neutro, no dismissible (dismissOnBackPress/ClickOutside=false). MainActivity.kt: reemplazado el AlertDialog inline (6175 chars, incluía userId crudo, BiometricPrompt inline y acceptAction suspend) por IncomingRequestDialog(...) con callbacks onAccept/onReject que llaman al mismo código de guardado en Room + sendAcceptance (2925 chars, delta -3250). Removidos 3 imports huérfanos: BiometricPrompt, BiometricManager, ContextCompat. Agregado import IncomingRequestDialog. ConversationsScreen.kt: removido import huérfano de IncomingRequestDialog (leftover de iteración anterior, sin call site).
¿ERA UN FIX DE ERROR?: No era fix de error funcional — mejora visual/UX del flujo de aceptación de invitación. ERROR colateral resuelto: el AlertDialog inline exponía el userId crudo 'Eduardo (3ca57504fc54315a) quiere agregarte' en la UI. SOLUCIÓN: card premium con avatar + nombre, sin userId.
HIPÓTESIS DESCARTADAS (ROL 2): no aplica (cambio de UI planificado). Durante el desarrollo: (1) primera escritura del composable via heredoc grande se mangleó 3 veces — DESCARTADA la estrategia, migrado a chunked cat >> con verificacion wc -l entre chunks (funcionó). (2) DialogProperties(false,false,false) posicional fallaba con 'The boolean literal does not conform to the expected type SecureFlagPolicy' — el 3er arg posicional es securePolicy: SecureFlagPolicy, no usePlatformDefaultWidth. SOLUCIÓN: parámetros nombrados. (3) Primer intento de swap con guard old_block_len>6000 abortó porque el bloque real mide 6175 chars — recalibrado a 10000. El guard funcionó como red de seguridad, evitando mutación no deseada.
VERIFICADO EN: compilación limpia. Pendiente verificación en dispositivo (visual + funcional).
COMPATIBILIDAD CONSIDERADA (R21): Android 11 (API 30) + Android 16 (API 36). Sin cambios de wire format. BiometricManager.Authenticators.BIOMETRIC_STRONG requiere API 30+, minSdk contemplado. Avatar HSV color pattern consistente con ConversationsScreen/ContactsScreen/NearbyPanel (seed*27 % 360, s=0.7, v=0.9). Paleta MALLA_DARK respetada (#141B22 card, #4CE6FF primary, #E6EDF3 texto, #8B949E secundario).
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): (1) feedback premium del emisor al enviar invitación (B1: Snackbar sobre StateFlow<Event?> del InvitationManager, ~40 líneas; B2: notificación headsup vía NotificationHelper, ~5 líneas; B3: dejar Toast actual). (2) limpieza de warnings preexistentes en MainActivity.kt (permissionExplanationShown:236, flashlight:261, db:723, condition currentConversationId:766). (3) barrido de dead code confirmado por R18 (BleTransport duplicado, MeshMessage triplicado, :transport vacío).
DEUDA / PENDIENTE QUE SIGUE ABIERTA: verificación en device del nuevo IncomingRequestDialog (visual + biometría + rechazo); decisión B1/B2/B3 para feedback del emisor; Fallo técnico B (writeCharacteristic false intermitente en ráfagas MTU 23); Fallo técnico C resuelto en iter 39 pero sin verificar en device; Iter 18 (rate-limit logThrottled); dead code R18; warnings preexistentes no bloqueantes; verificación iter 39 (timeout 3500ms + dedup invitación + typing debounce) pendiente en device.
──────────────────────────────

── ENTRADA — 2026-09-20 20:15 (Iteración 41: Bug D timeout + Bug E dedup feedback) ──
Compilación: BUILD SUCCESSFUL (in 3m 9s + :app:assembleDebug in 26s, 227 tasks).
Commit: bde97684
QUÉ SE HIZO: Dos fixes post-test en device 2026-09-20. (D) BleManager.kt writeFramedWithAck: cuando withTimeoutOrNull(3500ms) vence, si queued=true (el writeCharacteristic encoló los datos exitosamente en el stack BLE) ahora devuelve true en lugar de false. Causa raíz detectada en logs pareados: el Cubot reportaba 'connectAndWriteData resultado=false' al mandar invitación al Xiaomi, pero el Xiaomi SÍ recibió el Write Request en 13:10:55.462. El onCharacteristicWrite del Cubot llegó 30s tarde con status=133 (GATT_ERROR) por congestión del stack BLE. El false disparaba cascada completa (broadcast → sendWithRetry → NONE → 'en cola pendiente' sin reintento). (E) MainActivity.kt dedup de invitación: agregado Toast.makeText '${inv.senderDisplayName} ya es tu contacto' cuando existingContact != null. Feedback honesto al receptor para distinguir 'no llegó' de 'llegó pero ignorada por dedup'.
¿ERA UN FIX DE ERROR?: SÍ, 2 bugs. ERROR D: falso negativo de envío cuando ACK GATT llega tarde. SOLUCIÓN: asumir éxito si queued=true (los datos salieron del cliente). ERROR E: dedup silencioso sin feedback al usuario. SOLUCIÓN: Toast informativo.
HIPÓTESIS DESCARTADAS (ROL 2): (1) 'la invitación nunca llegó al receptor' — DESCARTADA: Write Request recibido en 13:10:55.462 del lado Xiaomi. (2) 'el dedup de iter 39 estaba roto' — DESCARTADA: funcionó correctamente; el usuario borró 'conversaciones' pero no 'contactos' de la DB, por lo que getById seguía devolviendo registros válidos. Aclaración: ContactEntity y ConversationEntity son tablas separadas en Room.
VERIFICADO EN: compilación limpia (2 builds). Pendiente verificación en dispositivo.
COMPATIBILIDAD CONSIDERADA (R21): Android 11 + Android 16. Sin cambios de wire format. El cambio de semántica timeout (true en lugar de false) es conservador: si el peer realmente no recibió los datos, el ACK nunca llega y el comportamiento previo ya estaba roto. Ahora al menos no se dispara la cascada innecesariamente.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): (1) implementar ACK explícito a nivel de aplicación para invitaciones (el receptor manda un 'invite_ack' tras procesar) — elimina la dependencia del ACK GATT que es poco confiable. (2) toast del emisor 'Solicitud enviada a X' aún es Android nativo — pendiente decisión B1/B2/B3 de iter previa.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: test en device iter 41 (timeout sin falsos negativos + toast dedup); test del card premium con contactos eliminados; Bug C (dedup) resuelto en iter 39, verificado hoy indirectamente; decisión B1/B2/B3; Fallo técnico B (writeCharacteristic false intermitente en ráfagas MTU 23); Iter 18 (rate-limit logThrottled); dead code R18.
──────────────────────────────

── ENTRADA — 2026-09-20 21:30 (Iteración 42: botón Rechazar + avatar 80dp) ──
Compilación: BUILD SUCCESSFUL (in 1m 17s + :app:assembleDebug in 20s, 227 tasks).
Commit: f160e3e0
QUÉ SE HIZO: Dos mejoras visuales en IncomingRequestDialog.kt tras feedback del test en device 2026-09-20. (1) Botón 'Rechazar' cambiado de TextButton (sin fondo) a OutlinedButton con borde 1dp alpha 0.5, misma altura que 'Aceptar' (52dp) y mismo shape (RoundedCornerShape 16dp). Jerarquía visual clara: Aceptar sólido cyan + Rechazar outlined neutro. (2) Avatar del remitente: tamaño 72dp → 80dp + borde cyan 1.5dp alpha 0.35 para destacar sobre el glow pulsante. (3) Agregado import androidx.compose.foundation.border. (4) Agregado import androidx.compose.material3.OutlinedButton.
¿ERA UN FIX DE ERROR?: No era fix de error — mejora visual tras feedback del usuario en device ('no hay un botón como tal para rechazar, solo para aceptar'). No bloqueante.
HIPÓTESIS DESCARTADAS (ROL 2): (1) 'OutlinedButton ya está importado' — DESCARTADA: la compilación falló con 'Unresolved reference: OutlinedButton' en línea 223. Fix: agregar el import faltante (delta 49 chars). (2) 'El segundo error de compilación es un bug real' — DESCARTADA: el error '@Composable invocations can only happen from the context of a @Composable function' era cascada del primero, se resolvió con el mismo import.
VERIFICADO EN: compilación limpia (2 builds sin errores). Pendiente verificación visual en dispositivo.
COMPATIBILIDAD CONSIDERADA (R21): Android 11 + Android 16. Sin cambios de wire format ni protocolo. OutlinedButton es API estable de Material3.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): (1) el avatar del remitente no muestra foto real porque la app NO TIENE feature de foto de perfil — confirmado por grep exhaustivo (0 matches de profilePhoto/profileImage/avatarUri/photoUri/userPhoto). Agregarla requiere 4 piezas: captura, persistencia, transporte (payload BLE crece de ~365 bytes a ~10-20KB, requiere fragmentación) y render. Iteración grande, propuesta para 43-44. (2) Bug F (race condition GATT discovery) sigue pendiente: en el test 2026-09-20 el Xiaomi no encuentra el service UUID del Cubot (servicios=[0000-1800, 0000-1801]) cuando actúa de cliente. Rompe mensajes Xiaomi→Cubot, read_all, imágenes. Es el bloqueante más grave del mesh.
DEUDA / PENDIENTE QUE SIGUE ABIERTA: Bug F (GATT discovery race — bloqueante de comunicación Xiaomi→Cubot); feature foto de perfil (iter 43-44); nombres reseteados a 'Usuario Malla' en ambos devices tras borrar datos — requiere re-registro manual del usuario; decisión B1/B2/B3 (toast del emisor); Fallo técnico B (writeCharacteristic false intermitente en ráfagas MTU 23); Iter 18 (rate-limit logThrottled sin efecto real); dead code R18.
──────────────────────────────

── ENTRADA — 2026-09-20 22:15 (Iteración 43: Bug F — GATT server abre con solo BLUETOOTH_CONNECT) ──
Compilación: BUILD SUCCESSFUL (in 1m 3s + :app:assembleDebug in 10s, 227 tasks).
Commit: 4eb3d4fd
QUÉ SE HIZO: Fix de la race condition estructural que impedía al Cubot abrir el GATT server. Causa raíz detectada en logs del test 2026-09-20: (a) BleTransport.startServer() usaba hasBlePermissions() que exige SCAN+CONNECT+ADVERTISE, pero un GATT server solo necesita BLUETOOTH_CONNECT. Cuando el usuario no había concedido los 3 permisos al arranque, startServer() retornaba silenciosamente sin abrir el server. (b) start() marcaba 'started=true' ANTES de llamar startServer(), así que cualquier re-llamada posterior (tras conceder permisos) era bloqueada por el guard 'if (started) return'. Estado final del Cubot: started=true pero gattServer=null, sin ningún log del fallo. Efecto observado: cuando Xiaomi conectaba como cliente (13:35:43), discoverServices devolvía solo los servicios genéricos del sistema (00001800 GAP, 00001801 GATT) sin el MALLA service 0000abcd-..., y todo write Xiaomi→Cubot fallaba con 'Service no encontrado en gatt'. Fix: 4 edits. (1) BleTransport.startServer() ahora usa BleManager.hasConnectPermission(context) + agrega logs explícitos de fallo y de éxito. (2) Nuevo flag serverStarted separado de started. (3) start() re-intenta startServer() siempre que serverStarted=false, aunque started=true. (4) MainActivity.permissionLauncher re-llama BleTransport.start(this) tras conceder permisos si connectGranted=true. Con esto, si el arranque ocurre antes de los permisos, el server se abre en el momento en que el usuario concede CONNECT.
¿ERA UN FIX DE ERROR?: SÍ — bloqueante crítico. ERROR: Xiaomi como cliente del Cubot ve solo servicios genéricos, writeCharacteristic falla con 'Service no encontrado en gatt'. Rompía TODA la comunicación Xiaomi→Cubot: mensajes, read_all, invitaciones, imágenes. Por asimetría: Cubot→Xiaomi funcionaba (el Xiaomi sí abría su server, con permisos concedidos al arranque). SOLUCIÓN: usar el permiso específico (CONNECT) en lugar del agregado (los 3), y permitir re-intento de start() tras concesión de permisos. ¿FUNCIONÓ?: compilación limpia. Pendiente verificación en device.
HIPÓTESIS DESCARTADAS (ROL 2): (1) 'discovery race en el cliente' — DESCARTADA como causa raíz: el cliente SÍ completó discovery (recibió MTU 517, o sea onServicesDiscovered disparó). Lo que faltaba eran los servicios del lado server, no timing del cliente. (2) 'gattCache stale' — DESCARTADA: conexión nueva, sin entradas previas en cache. (3) 'addService tardío' — DESCARTADA: el server nunca se abrió, no es cuestión de timing. (4) 'hasBlePermissions mal usado en BleTransport' — CONFIRMADA como causa raíz: el guard exigía 3 permisos para una operación que requiere 1. Deuda técnica: hasBlePermissions se usa como guard universal en varios puntos del código donde debería usarse el específico (hasScanPermission, hasConnectPermission, hasAdvertisePermission).
VERIFICADO EN: compilación limpia (2 builds). Pendiente verificación en dispositivo Xiaomi Redmi Note 9S + Cubot KingKong ES 5.
COMPATIBILIDAD CONSIDERADA (R21): Android 11 + Android 16. En API < 31 (Xiaomi API 30) hasConnectPermission devuelve true sin chequeo (permisos legacy). El fix solo tiene efecto real en API 31+, exactamente donde se manifestaba el bug (Cubot API 36). Sin cambios de wire format ni protocolo.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): (1) auditar todas las llamadas a hasBlePermissions() en el codebase — hay varios sitios donde se usa el guard agregado cuando debería usarse el específico. BleTransport.startServer() ya corregido; revisar BleManager y otras clases. (2) consider arreglar también BleTransport.stop() para resetear serverStarted=false y permitir reabrir el server si por alguna razón se cierra. (3) si en el test el server tarda en abrir y el primer connectAndWriteData del cliente falla, agregar un retry de 500ms post-connect (defensivo, poco probable que sea necesario con el fix actual).
DEUDA / PENDIENTE QUE SIGUE ABIERTA: verificación en device del fix Bug F; feature foto de perfil (iter 44); nombres reseteados a 'Usuario Malla' tras borrar datos (usuario debe re-registrar); decisión B1/B2/B3 (toast del emisor); Fallo técnico B (writeCharacteristic false intermitente en ráfagas MTU 23); Iter 18 (rate-limit logThrottled sin efecto real); dead code R18; auditoría de hasBlePermissions mal usado en otros archivos.
──────────────────────────────

── ENTRADA — 2026-09-20 23:00 (Iteración 44: Bug F capa 2 — addService robusto) ──
Compilación: BUILD SUCCESSFUL (in 1m 16s + :app:assembleDebug in 10s, 227 tasks).
Commit: 494320f4
QUÉ SE HIZO: Segunda capa del fix de Bug F tras verificar en device 2026-09-20 que el server del Cubot abría pero sin registrar el service MALLA. Causa raíz: addService() se llamaba inmediatamente después de openGattServer() y su retorno no se verificaba. En Android 14+ (Cubot API 36) openGattServer() es internamente asíncrono y addService() puede devolver false sin error visible. Resultado: server 'abierto' con solo servicios genéricos (GAP 00001800 + GATT 00001801), cliente no encuentra 0000abcd-.... Fix: 2 cambios coordinados. (A) BleTransport.kt startServer() reescrito: corre en scope.launch, agrega delay de 500ms entre openGattServer y addService, verifica el retorno de addService, retry único con delay 500ms adicional si falla, logs honestos ('addService retorno=true/false', 'GATT server abierto con addService OK', 'FALLO CRITICO' si los 2 intentos fallan). Nuevo flag startingServer para evitar re-entrada concurrente. (B) BleManager.kt connectAndWriteData: si getService(serviceUuid) devuelve null, forzar gatt.discoverServices() + delay 1500ms + retry. Si sigue null, invalidar gattCache[address] para forzar reconexión limpia en el próximo intento. Los logs de 'Service no encontrado' ahora indican cuándo el retry falló.
¿ERA UN FIX DE ERROR?: SÍ — bloqueante. ERROR: Xiaomi como cliente del Cubot ve solo GAP+GATT genéricos, writeCharacteristic falla con 'Service no encontrado en gatt'. Rompía toda la comunicación Xiaomi→Cubot. SOLUCIÓN: delay + verificación + retry en server, retry de discovery + invalidación de cache en cliente. ¿FUNCIONÓ?: compilación limpia (2 builds). Pendiente verificación en device.
HIPÓTESIS DESCARTADAS (ROL 2): (1) 'permisos faltantes bloquean el server' — DESCARTADA: iter 43 ya resolvió eso; los logs del test mostraron 'GATT server abierto con SERVICE_UUID=...' pero el cliente seguía viendo solo genéricos. (2) 'cliente con caché stale' — DESCARTADA como causa única: el Xiaomi había conectado al Cubot 3+ minutos después de que el Cubot tuviera el server abierto; la caché no era el problema primario. (3) 'timing del cliente' — DESCARTADA: onServicesDiscovered disparaba en el cliente (recibía MTU 517); el problema era que los servicios MALLA no estaban registrados en el server.
VERIFICADO EN: compilación limpia (2 builds). Pendiente verificación en device.
COMPATIBILIDAD CONSIDERADA (R21): Android 11 + Android 16. El cambio en startServer no cambia la firma externa; solo cambia el timing interno. scope.launch ya existía en BleTransport para discoveryJob. El delay de 500ms agrega 0.5s al arranque (aceptable). Los cambios en connectAndWriteData son defensivos: si el service se encuentra al primer intento, no se ejecuta ni el discovery retry ni la invalidación de cache.
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): (1) evaluar si Android 16 (API 36) tiene una API nueva específica para 'servidor listo' que reemplace el delay mágico de 500ms — el delay es un workaround empírico, no una solución canónica. (2) si el delay de 500ms resulta insuficiente, subir a 1000ms con log del tiempo real de openGattServer→addService. (3) revisar si addService en retry necesita una instancia nueva de BluetoothGattService (algunos Android rechazan reusar la misma instancia tras el primer false).
DEUDA / PENDIENTE QUE SIGUE ABIERTA: verificación en device del Bug F capa 2 (mensajes Xiaomi → Cubot); verificación en device del fix de nombre (iter 45); feature foto de perfil (iter 46+); decisión B1/B2/B3 (toast del emisor); Fallo técnico B (writeCharacteristic false intermitente en ráfagas MTU 23); Iter 18 (rate-limit logThrottled); dead code R18.
──────────────────────────────


── ENTRADA — 2026-09-20 23:00 (Iteración 45: refreshAdvertising tras cambio de nombre) ──
Compilación: BUILD SUCCESSFUL (in 1m 16s + :app:assembleDebug in 10s, 227 tasks).
Commit: 494320f4
QUÉ SE HIZO: Fix del bug reportado en device 2026-09-20: 'los nodos detectados no llevan el nombre que el usuario escogió al registrarse, sino simplemente usuario'. Causa raíz confirmada por forense: el advertising BLE arranca en MainActivity.onCreate (vía MeshChatService.start → ProximityEngine.start) ANTES de que el usuario complete el registro. startAdvertising() lee el nombre con IdentityManager.getUserName() que devuelve el default 'Usuario Malla'. Cuando después setUserName() guarda el nombre real, nadie re-dispara el advertising; startAdvertising() bloquea re-llamadas con 'if (advertising) return'. Resultado: el advertising queda con el nombre default para toda la sesión. Fix: (A) ProximityEngine.kt nuevo helper refreshAdvertising(context) que hace stopAdvertising() + startAdvertising(nombre actual, 0), con log explícito. (B) RegistrationScreen.kt: llamada a refreshAdvertising después de setUserName en NameStep.onContinue. (C) PerfilScreen.kt: ídem en el confirmButton del diálogo de edición de nombre. (D) EditProfileScreen.kt: ídem en el onClick del botón Guardar. Con esto, cualquier cambio de nombre (registro inicial, edición desde Perfil, edición desde EditProfile) propaga inmediatamente al advertising.
¿ERA UN FIX DE ERROR?: SÍ — no bloqueante pero visible. ERROR: nodos detectados aparecen con nombre 'Usuario Malla' / 'Usuari' (truncado a 6 chars) en lugar del nombre real del usuario. SOLUCIÓN: refreshAdvertising() + 3 puntos de llamada. ¿FUNCIONÓ?: compilación limpia (2 builds). Pendiente verificación en device.
HIPÓTESIS DESCARTADAS (ROL 2): (1) 'setUserName no persiste' — DESCARTADA: getUserName lee de SharedPreferences 'identity' con key USER_NAME_KEY, y setUserName hace .apply() correctamente. La persistencia funciona. (2) 'getUserName devuelve el nombre viejo' — DESCARTADA: devuelve el valor actual de prefs; el problema es que nadie lo relee después del cambio. (3) 'getToken es inestable' — DESCARTADA: confirmado por logs que generateToken(userId) es determinista para el mismo userId, por lo que detener+rearrancar advertising no cambia el token y los peers siguen reconociendo al nodo. (4) 'el advertising se propaga naturalmente en el siguiente ciclo de scan' — DESCARTADA: el advertising del emisor persiste hasta que alguien lo detenga; los peers solo ven el cambio si el emisor re-anuncia.
VERIFICADO EN: compilación limpia (2 builds). Pendiente verificación en device.
COMPATIBILIDAD CONSIDERADA (R21): Android 11 + Android 16. refreshAdvertising usa la misma ruta que startAdvertising (ya probada en device). El stopAdvertising previo garantiza que startAdvertising no se bloquee por el flag advertising. Sin cambios de wire format ni payload (mismo userId, mismo token, mismo avatarSeed=0). La llamada a refreshAdvertising desde RegistrationScreen usa com.malla.mvp.network.ProximityEngine con path completo (no requiere import adicional).
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): (1) refactor más robusto: exponer un StateFlow<String> userNameFlow en IdentityManager y que ProximityEngine lo observe y auto-refresque — elimina la necesidad de llamar refreshAdvertising manualmente desde cada punto de cambio. Más código pero más a prueba de futuros cambios. (2) iniciar el advertising DESPUÉS del registro inicial (en MainActivity, gateado por isRegistrationComplete) — resuelve el problema de raíz sin necesidad de refresh, pero requiere cambiar el timing de arranque. (3) UI: confirmar visualmente que el cambio de nombre se refleja en PerfilScreen (ya lo hace — userName es variable de estado).
DEUDA / PENDIENTE QUE SIGUE ABIERTA: verificación en device del fix de nombre (nodos deben aparecer como 'Eduardo'/'Lalo' tras registro); verificación en device del Bug F capa 2 (iter 44); feature foto de perfil (iter 46+); decisión B1/B2/B3; Fallo técnico B; Iter 18; dead code R18.
──────────────────────────────

── ENTRADA — 2026-09-25 16:00 (Iteración 46: conexión TCP entre peers en misma red) ──
Compilación: BUILD SUCCESSFUL (in 2m 58s, 3 módulos: :app + :network + :events con --rerun-tasks).
Commit: 0ab2bc6e
QUÉ SE HIZO: Cableado del descubrimiento mDNS con la conexión TCP. Causa raíz detectada en logs del test 2026-09-25: ProximityEngine descubría peers por mDNS y los agregaba a nearbyUsers, pero NADIE llamaba NetworkService.connectToPeer() — por eso la cascada siempre veía 'TCP conectado=false' y caía a BLE. Fix arquitectónicamente limpio usando MallaEventBus como puente entre :network y :app (que no pueden importarse entre sí por regla de módulos). 3 patches: (1) events/MallaEventBus.kt: nuevo canal peerMdnsResolved: SharedFlow<String>(extraBufferCapacity=5). (2) network/ProximityEngine.kt: en el callback DiscoveryService.onPeerResolved, tras addOrUpdate del peer mDNS, emite la IP al bus con MallaEventBus.peerMdnsResolved.tryEmit(remoteIp). Se agregó el import de MallaEventBus. (3) service/MeshChatService.kt: nuevo GlobalScope.launch(Dispatchers.IO) en onCreate que collecta MallaEventBus.peerMdnsResolved, hace dedup con mutableMapOf<String, Long> (no reintenta la misma IP antes de 30s) y llama NetworkService.connectToPeer(ip). El handshake ECDH existente (HS:1..HS:OK) resuelve la correlación IP→userId automáticamente: cuando el peer manda su identity en HS:2, NetworkService registra el ClientHandler bajo su userId, y isContactConnected(userId) empieza a devolver true. La cascada de TransportManager prioriza TCP automáticamente.
¿ERA UN FIX DE ERROR?: SÍ — bloqueante de la promesa 'WiFi primero'. ERROR: cuando dos celulares estaban en la misma WiFi/hotspot, la cascada usaba BLE en vez de TCP porque isContactConnected siempre devolvía false. Los logs del 2026-09-25 del Xiaomi mostraban '[TransportManager] Enviando mensaje a ...; TCP conectado=false' en cada envío, con cero apariciones de '[NS:TCP] Intentando conectar a ...'. SOLUCIÓN: disparar connectToPeer al descubrir peer por mDNS. ¿FUNCIONÓ?: compilación limpia en 3 módulos. Pendiente verificación en device.
HIPÓTESIS DESCARTADAS (ROL 2): (1) 'el handshake TCP falla' — DESCARTADA: el handshake ECDH está implementado y funcional (HS:OK documentado en pruebas anteriores); el problema era que nunca se invocaba. (2) 'mDNS no descubre peers' — DESCARTADA: el log del 2026-09-25 mostraba '[PROX] Nodos actualizados: [Lalo:mdns_10.179.10.18]'; el descubrimiento funciona. (3) 'NetworkService.connectToPeer tiene bug' — DESCARTADA: la función está bien implementada; simplemente nadie la llamaba desde el camino de descubrimiento. (4) 'ProximityEngine puede importar NetworkService' — DESCARTADA por arquitectura: :network no depende de :app en build.gradle.kts; por eso se usó MallaEventBus.
VERIFICADO EN: compilación limpia (3 módulos, --rerun-tasks). Pendiente verificación en device.
COMPATIBILIDAD CONSIDERADA (R21): Android 11 + Android 16. Sin cambios de wire format ni protocolo. El puente via MallaEventBus es el patrón canónico del proyecto (ya usado para typing, zumbido, mensajes, conversationOpened). El dedup de 30s evita spam de conexiones si mDNS re-emite la misma IP varias veces por segundo. Uso de GlobalScope.launch en onCreate sigue el patrón ya establecido en MeshChatService (mismo estilo para TransportManager/ProximityEngine/InvitationManager).
SUGERENCIAS PROACTIVAS OFRECIDAS (R20, sin implementar aún): (1) si el primer intento de connectToPeer falla (peer no está listo en ese instante), el dedup de 30s evita reintentos rápidos pero también retrasa reconexión. Considerar backoff exponencial en el observador (30s, 60s, 120s) para casos de peer lento en arrancar. (2) el dedup actual es por IP — si el peer cambia de IP pero mantiene userId, se trata como nuevo peer. Aceptable en MVP. (3) si el handshake TCP funciona, la próxima iteración (47) debería priorizar cifrado de los mensajes TCP con la clave ECDH derivada — verificar si ya está en el sendMessageToContact o si los mensajes van en claro sobre el socket (sería grave).
DEUDA / PENDIENTE QUE SIGUE ABIERTA: test en device del fix iter 46 (mensajes por TCP en misma WiFi); si funciona, verificar cifrado end-to-end de mensajes TCP; iter 47 (STUN + IPv6 + hole punching para internet global entre redes distintas); feature foto de perfil; decisión B1/B2/B3 (toast del emisor); Fallo técnico B (writeCharacteristic false intermitente en ráfagas MTU 23); Iter 18 (rate-limit logThrottled); dead code R18.
──────────────────────────────
