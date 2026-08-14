package com.malla.mvp.ui.components

import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malla.mvp.ui.settings.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke

val rainbowColors = listOf(
    Color(0xFFFF3B30), Color(0xFFFF9500), Color(0xFFFFCC00), Color(0xFF34C759),
    Color(0xFF00C7BE), Color(0xFF007AFF), Color(0xFF5856D6), Color(0xFFAF52DE),
    Color(0xFFFF2D55), Color(0xFFA2845E), Color(0xFF8E8E93), Color(0xFF1A3B4A),
    Color(0xFF4CD964), Color(0xFFFFD60A), Color(0xFFFF8C00), Color(0xFF32ADE6),
    Color(0xFF5E5CE6), Color(0xFFBF5AF2), Color(0xFFFF375F), Color(0xFF64D2FF),
    Color(0xFF30D158), Color(0xFF66D4CF), Color(0xFF0A84FF), Color(0xFFBF5AF2)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatCustomizationDialog(
    conversationId: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var prefs by remember { mutableStateOf(ConversationPreferences.load(context, conversationId)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF0A1B2A),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Personalizar chat",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Sección: Burbujas propias (burbuja + texto)
            ExpandableSection(title = "Burbujas propias") {
                Text("Color de burbuja", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                ColorPalettePicker(
                    current = prefs.ownBubbleColor?.let { Color(it) },
                    colors = rainbowColors,
                    onSelected = { color ->
                        prefs = prefs.copy(ownBubbleColor = color?.toArgb())
                        ConversationPreferences.save(context, conversationId, prefs)
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Color de texto", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                ColorPalettePicker(
                    current = prefs.ownTextColor?.let { Color(it) },
                    colors = rainbowColors,
                    onSelected = { color ->
                        prefs = prefs.copy(ownTextColor = color?.toArgb())
                        ConversationPreferences.save(context, conversationId, prefs)
                    }
                )
            }

            // Sección: Burbujas del contacto (burbuja + texto)
            ExpandableSection(title = "Burbujas del contacto") {
                Text("Color de burbuja", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                ColorPalettePicker(
                    current = prefs.otherBubbleColor?.let { Color(it) },
                    colors = rainbowColors,
                    onSelected = { color ->
                        prefs = prefs.copy(otherBubbleColor = color?.toArgb())
                        ConversationPreferences.save(context, conversationId, prefs)
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Color de texto", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                ColorPalettePicker(
                    current = prefs.otherTextColor?.let { Color(it) },
                    colors = rainbowColors,
                    onSelected = { color ->
                        prefs = prefs.copy(otherTextColor = color?.toArgb())
                        ConversationPreferences.save(context, conversationId, prefs)
                    }
                )
            }

            // Tamaño del texto
            ExpandableSection(title = "Tamaño del texto") {
                Slider(
                    value = prefs.fontSize,
                    onValueChange = { newSize ->
                        prefs = prefs.copy(fontSize = newSize)
                        ConversationPreferences.save(context, conversationId, prefs)
                    },
                    valueRange = 10f..22f,
                    steps = 11,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF4CE6FF),
                        activeTrackColor = Color(0xFF4CE6FF)
                    )
                )
                Text(
                    "${prefs.fontSize.toInt()} sp",
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            // Transparencia
            ExpandableSection(title = "Transparencia de burbujas") {
                Slider(
                    value = prefs.bubbleOpacity,
                    onValueChange = { newOpacity ->
                        prefs = prefs.copy(bubbleOpacity = newOpacity)
                        ConversationPreferences.save(context, conversationId, prefs)
                    },
                    valueRange = 0.5f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF4CE6FF),
                        activeTrackColor = Color(0xFF4CE6FF)
                    )
                )
                Text(
                    "${(prefs.bubbleOpacity * 100).toInt()}%",
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            // Sonido entrante
            ExpandableSection(title = "Sonido de mensaje entrante") {
                SoundSelector(
                    current = prefs.incomingSound,
                    onSelected = { sound ->
                        prefs = prefs.copy(incomingSound = sound)
                        ConversationPreferences.save(context, conversationId, prefs)
                    }
                )
            }

            // Sonido saliente
            ExpandableSection(title = "Sonido de mensaje saliente") {
                SoundSelector(
                    current = prefs.outgoingSound,
                    onSelected = { sound ->
                        prefs = prefs.copy(outgoingSound = sound)
                        ConversationPreferences.save(context, conversationId, prefs)
                    }
                )
            }

            // Color de fondo
            ExpandableSection(title = "Color de fondo del chat") {
                ColorPalettePicker(
                    current = prefs.chatBackgroundColor?.let { Color(it) },
                    colors = listOf(Color(0xFF0A1118), Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460), Color(0xFF533483), Color(0xFF111111), Color(0xFF202124)) + rainbowColors,
                    onSelected = { color ->
                        prefs = prefs.copy(chatBackgroundColor = color?.toArgb())
                        ConversationPreferences.save(context, conversationId, prefs)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CE6FF))
            ) {
                Text("Listo", color = Color.White)
            }
        }
    }
}

@Composable
private fun ColorPalettePicker(
    current: Color?,
    colors: List<Color>,
    onSelected: (Color?) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        colors.chunked(6).forEach { rowColors ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowColors.forEach { color ->
                    val isSelected = current == color
                    Box(
                        modifier = Modifier
                            .size(36.dp)
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
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Tema automático",
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
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun ExpandableSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF15202B)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Colapsar" else "Expandir",
                    tint = Color(0xFF4CE6FF)
                )
            }
            AnimatedVisibility(visible = expanded) {
                content()
            }
        }
    }
}

private fun Color.luminance(): Float = 0.2126f * red + 0.7152f * green + 0.0722f * blue