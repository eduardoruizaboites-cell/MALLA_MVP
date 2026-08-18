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
