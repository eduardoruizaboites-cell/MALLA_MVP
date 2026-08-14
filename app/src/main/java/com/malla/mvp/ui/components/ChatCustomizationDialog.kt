package com.malla.mvp.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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

            ExpandableSection(title = "Color de burbujas propias") {
                AdvancedColorPicker(
                    currentColor = prefs.ownBubbleColor?.let { Color(it) },
                    onColorSelected = { color ->
                        prefs = prefs.copy(ownBubbleColor = color?.toArgb())
                        ConversationPreferences.save(context, conversationId, prefs)
                    }
                )
            }

            ExpandableSection(title = "Color de burbujas del contacto") {
                AdvancedColorPicker(
                    currentColor = prefs.otherBubbleColor?.let { Color(it) },
                    onColorSelected = { color ->
                        prefs = prefs.copy(otherBubbleColor = color?.toArgb())
                        ConversationPreferences.save(context, conversationId, prefs)
                    }
                )
            }

            ExpandableSection(title = "Color de texto propio") {
                AdvancedColorPicker(
                    currentColor = prefs.ownTextColor?.let { Color(it) },
                    onColorSelected = { color ->
                        prefs = prefs.copy(ownTextColor = color?.toArgb())
                        ConversationPreferences.save(context, conversationId, prefs)
                    }
                )
            }

            ExpandableSection(title = "Color de texto del contacto") {
                AdvancedColorPicker(
                    currentColor = prefs.otherTextColor?.let { Color(it) },
                    onColorSelected = { color ->
                        prefs = prefs.copy(otherTextColor = color?.toArgb())
                        ConversationPreferences.save(context, conversationId, prefs)
                    }
                )
            }

            ExpandableSection(title = "Estilo de burbuja") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(BubbleStyle.values().toList()) { style ->
                        val isSelected = style.name == prefs.bubbleStyle
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) Color(0xFF4CE6FF).copy(alpha = 0.15f)
                                    else Color.White.copy(alpha = 0.05f)
                                )
                                .clickable {
                                    prefs = prefs.copy(bubbleStyle = style.name)
                                    ConversationPreferences.save(context, conversationId, prefs)
                                }
                                .padding(12.dp)
                        ) {
                            Surface(
                                color = Color(0xFF1A3B4A),
                                shape = BubbleShapes.getShape(style, true),
                                shadowElevation = 2.dp,
                                modifier = Modifier.size(width = 80.dp, height = 44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("Hola", color = Color.White, fontSize = 10.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                style.label,
                                color = if (isSelected) Color.White else Color.LightGray,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

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

            ExpandableSection(title = "Sonido de mensaje entrante") {
                SoundSelector(
                    current = prefs.incomingSound,
                    onSelected = { sound ->
                        prefs = prefs.copy(incomingSound = sound)
                        ConversationPreferences.save(context, conversationId, prefs)
                    }
                )
            }

            ExpandableSection(title = "Sonido de mensaje saliente") {
                SoundSelector(
                    current = prefs.outgoingSound,
                    onSelected = { sound ->
                        prefs = prefs.copy(outgoingSound = sound)
                        ConversationPreferences.save(context, conversationId, prefs)
                    }
                )
            }

            ExpandableSection(title = "Color de fondo del chat") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        val isDefault = prefs.chatBackgroundColor == null
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0A1118))
                                .border(
                                    width = if (isDefault) 3.dp else 0.dp,
                                    color = if (isDefault) Color(0xFF4CE6FF) else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    prefs = prefs.copy(chatBackgroundColor = null)
                                    ConversationPreferences.save(context, conversationId, prefs)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Def", color = Color.White, fontSize = 12.sp)
                        }
                    }
                    items(listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460), Color(0xFF533483), Color(0xFF111111))) { color ->
                        val isSelected = prefs.chatBackgroundColor == color.toArgb()
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) Color(0xFF4CE6FF) else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    prefs = prefs.copy(chatBackgroundColor = color.toArgb())
                                    ConversationPreferences.save(context, conversationId, prefs)
                                }
                        )
                    }
                }
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
private fun SoundSelector(current: String, onSelected: (String) -> Unit) {
    val sounds = listOf(
        "system" to "Sistema",
        "zumbido" to "Zumbido",
        "none" to "Silencio"
    )
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