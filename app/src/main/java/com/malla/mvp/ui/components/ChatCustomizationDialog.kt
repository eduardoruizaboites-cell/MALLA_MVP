package com.malla.mvp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malla.mvp.ui.settings.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke

val coreColors = listOf(
    Color(0xFFFF3B30), Color(0xFFFF9500), Color(0xFFFFCC00), Color(0xFF34C759),
    Color(0xFF00C7BE), Color(0xFF007AFF), Color(0xFF5856D6), Color(0xFFAF52DE),
    Color(0xFF1A3B4A), Color(0xFF2A2A2A), Color(0xFF0A1118), Color(0xFF4CE6FF)
)

val backgroundColors = listOf(
    Color(0xFF0A1118), Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460),
    Color(0xFF533483), Color(0xFF111111), Color(0xFF202124), Color(0xFF1B2A3A)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatCustomizationDialog(
    conversationId: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var prefs by remember { mutableStateOf(ConversationPreferences.load(context, conversationId)) }
    var selectedTab by remember { mutableStateOf(0) }

    fun save(newPrefs: ConversationPrefs) {
        prefs = newPrefs
        ConversationPreferences.save(context, conversationId, newPrefs)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF0A1B2A),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Encabezado
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Personalizar chat",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Ajusta la apariencia de esta conversación",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, "Cerrar", tint = Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Vista previa en vivo
            LivePreviewCard(prefs = prefs)

            Spacer(Modifier.height(16.dp))

            // Selector rápido de temas (swatches de color)
            Text(
                "Temas rápidos",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val themes = listOf(
                    Triple("Oscuro", Color(0xFF1A3B4A), Color(0xFF2A2A2A)),
                    Triple("Claro", Color(0xFFDCF8C6), Color(0xFFFFFFFF)),
                    Triple("Azul", Color(0xFF1E88E5), Color(0xFF263238)),
                    Triple("Verde", Color(0xFF00C853), Color(0xFF2E7D32)),
                    Triple("Nocturno", Color(0xFF2A2A2E), Color(0xFF1E1E22))
                )
                themes.forEach { (name, ownColor, otherColor) ->
                    val isSelected = prefs.ownBubbleColor == ownColor.toArgb() &&
                            prefs.otherBubbleColor == otherColor.toArgb()
                    val scale = remember { Animatable(1f) }
                    LaunchedEffect(isSelected) {
                        if (isSelected) {
                            scale.animateTo(1.15f, spring(dampingRatio = 0.5f, stiffness = 600f))
                            scale.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = 400f))
                        } else {
                            scale.snapTo(1f)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .scale(scale.value)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(ownColor, otherColor)
                                )
                            )
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                val newPrefs = prefs.copy(
                                    ownBubbleColor = ownColor.toArgb(),
                                    otherBubbleColor = otherColor.toArgb(),
                                    ownTextColor = if (ownColor.luminance() > 0.5f) Color.Black.toArgb() else Color.White.toArgb(),
                                    otherTextColor = if (otherColor.luminance() > 0.5f) Color.Black.toArgb() else Color.White.toArgb(),
                                    chatBackgroundColor = when (name) {
                                        "Oscuro" -> Color(0xFF0A1118).toArgb()
                                        "Claro" -> Color(0xFFECE5DD).toArgb()
                                        "Azul" -> Color(0xFF0A1B2A).toArgb()
                                        "Verde" -> Color(0xFF0A1118).toArgb()
                                        "Nocturno" -> Color(0xFF0A0A0F).toArgb()
                                        else -> Color(0xFF0A1118).toArgb()
                                    }
                                )
                                save(newPrefs)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "Seleccionado",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))

            // Tabs de personalización
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color(0xFF4CE6FF)
            ) {
                listOf("Burbuja", "Contacto", "Fondo", "Sonido").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 13.sp) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Contenido contextual según pestaña
            when (selectedTab) {
                0 -> OwnBubbleSection(prefs, onPrefsChange = { save(it) })
                1 -> OtherBubbleSection(prefs, onPrefsChange = { save(it) })
                2 -> BackgroundSection(prefs, onPrefsChange = { save(it) })
                3 -> SoundSection(prefs, onPrefsChange = { save(it) })
            }

            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val defaultPrefs = ConversationPrefs()
                        save(defaultPrefs)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4CE6FF))
                ) {
                    Icon(Icons.Filled.Refresh, null, tint = Color(0xFF4CE6FF), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Restablecer", color = Color(0xFF4CE6FF))
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CE6FF))
                ) {
                    Text("Listo", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun LivePreviewCard(prefs: ConversationPrefs) {
    val ownBubbleColor = prefs.ownBubbleColor?.let { Color(it) } ?: Color(0xFF1A3B4A)
    val otherBubbleColor = prefs.otherBubbleColor?.let { Color(it) } ?: Color(0xFF2A2A2A)
    val ownTextColor = prefs.ownTextColor?.let { Color(it) } ?: Color.White
    val otherTextColor = prefs.otherTextColor?.let { Color(it) } ?: Color.White
    val bubbleOpacity = prefs.bubbleOpacity
    val fontSize = prefs.fontSize
    val background = prefs.chatBackgroundColor?.let { Color(it) } ?: Color(0xFF0A1118)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, Color(0xFF4CE6FF).copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .background(background)
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Vista previa",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(12.dp))
                // Burbuja propia (derecha)
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                ownBubbleColor.copy(alpha = bubbleOpacity),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "¡Hola! Así se ve mi burbuja.",
                            color = ownTextColor,
                            fontSize = fontSize.sp
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                // Burbuja contacto (izquierda)
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                otherBubbleColor.copy(alpha = bubbleOpacity),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "Y esta es la del contacto.",
                            color = otherTextColor,
                            fontSize = fontSize.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OwnBubbleSection(
    prefs: ConversationPrefs,
    onPrefsChange: (ConversationPrefs) -> Unit
) {
    Column {
        Text("Color de burbuja", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        Spacer(Modifier.height(6.dp))
        ColorPalettePicker(
            current = prefs.ownBubbleColor?.let { Color(it) },
            colors = coreColors,
            onSelected = { color ->
                onPrefsChange(prefs.copy(ownBubbleColor = color?.toArgb()))
            }
        )
        Spacer(Modifier.height(12.dp))
        Text("Color de texto", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        Spacer(Modifier.height(6.dp))
        ColorPalettePicker(
            current = prefs.ownTextColor?.let { Color(it) },
            colors = coreColors,
            onSelected = { color ->
                onPrefsChange(prefs.copy(ownTextColor = color?.toArgb()))
            }
        )
        Spacer(Modifier.height(12.dp))
        Text("Tamaño del texto", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        Slider(
            value = prefs.fontSize,
            onValueChange = { size -> onPrefsChange(prefs.copy(fontSize = size)) },
            valueRange = 10f..22f,
            steps = 11,
            colors = SliderDefaults.colors(thumbColor = Color(0xFF4CE6FF), activeTrackColor = Color(0xFF4CE6FF))
        )
        Text("${prefs.fontSize.toInt()} sp", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Text("Transparencia", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        Slider(
            value = prefs.bubbleOpacity,
            onValueChange = { opacity -> onPrefsChange(prefs.copy(bubbleOpacity = opacity)) },
            valueRange = 0.5f..1f,
            colors = SliderDefaults.colors(thumbColor = Color(0xFF4CE6FF), activeTrackColor = Color(0xFF4CE6FF))
        )
        Text("${(prefs.bubbleOpacity * 100).toInt()}%", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun OtherBubbleSection(
    prefs: ConversationPrefs,
    onPrefsChange: (ConversationPrefs) -> Unit
) {
    Column {
        Text("Color de burbuja", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        Spacer(Modifier.height(6.dp))
        ColorPalettePicker(
            current = prefs.otherBubbleColor?.let { Color(it) },
            colors = coreColors,
            onSelected = { color ->
                onPrefsChange(prefs.copy(otherBubbleColor = color?.toArgb()))
            }
        )
        Spacer(Modifier.height(12.dp))
        Text("Color de texto", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        Spacer(Modifier.height(6.dp))
        ColorPalettePicker(
            current = prefs.otherTextColor?.let { Color(it) },
            colors = coreColors,
            onSelected = { color ->
                onPrefsChange(prefs.copy(otherTextColor = color?.toArgb()))
            }
        )
    }
}

@Composable
private fun BackgroundSection(
    prefs: ConversationPrefs,
    onPrefsChange: (ConversationPrefs) -> Unit
) {
    Column {
        Text("Color de fondo", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        Spacer(Modifier.height(6.dp))
        ColorPalettePicker(
            current = prefs.chatBackgroundColor?.let { Color(it) },
            colors = backgroundColors + coreColors,
            onSelected = { color ->
                onPrefsChange(prefs.copy(chatBackgroundColor = color?.toArgb()))
            }
        )
    }
}

@Composable
private fun SoundSection(
    prefs: ConversationPrefs,
    onPrefsChange: (ConversationPrefs) -> Unit
) {
    Column {
        Text("Sonido entrante", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        Spacer(Modifier.height(6.dp))
        SoundSelector(
            current = prefs.incomingSound,
            onSelected = { sound -> onPrefsChange(prefs.copy(incomingSound = sound)) }
        )
        Spacer(Modifier.height(12.dp))
        Text("Sonido saliente", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        Spacer(Modifier.height(6.dp))
        SoundSelector(
            current = prefs.outgoingSound,
            onSelected = { sound -> onPrefsChange(prefs.copy(outgoingSound = sound)) }
        )
    }
}

@Composable
private fun ColorPalettePicker(
    current: Color?,
    colors: List<Color>,
    onSelected: (Color?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        colors.chunked(6).forEach { rowColors ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                rowColors.forEach { color ->
                    val isSelected = current == color
                    val scale = remember { Animatable(1f) }
                    LaunchedEffect(isSelected) {
                        if (isSelected) {
                            scale.animateTo(1.2f, spring(dampingRatio = 0.5f, stiffness = 600f))
                            scale.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = 400f))
                        } else {
                            scale.snapTo(1f)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .scale(scale.value)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { onSelected(color) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "Seleccionado",
                                tint = if (color.luminance() > 0.5f) Color.Black else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Automático",
            color = Color(0xFF00E5FF),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.clickable { onSelected(null) }
        )
    }
}

@Composable
private fun SoundSelector(current: String, onSelected: (String) -> Unit) {
    val sounds = listOf("system" to "Sistema", "zumbido" to "Zumbido", "none" to "Silencio")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        sounds.forEach { (value, label) ->
            val isSelected = current == value
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) Color(0xFF4CE6FF).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                border = if (isSelected) BorderStroke(1.dp, Color(0xFF4CE6FF)) else null,
                modifier = Modifier.clickable { onSelected(value) }
            ) {
                Text(
                    label,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    fontSize = 12.sp
                )
            }
        }
    }
}

private fun Color.luminance(): Float = 0.2126f * red + 0.7152f * green + 0.0722f * blue
