package com.malla.mvp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmojiReactionPicker(
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showFullPicker by remember { mutableStateOf(false) }
    val commonEmojis = listOf("👍", "❤️", "😂", "😮", "😢")

    if (showFullPicker) {
        // Selector completo de emojis
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text("Selecciona una reacción") },
            text = {
                val allEmojis = listOf("😀","😂","😍","😢","😡","👍","👋","🎉","❤️","🔥","😎","🙏","💪","🤔","😴","🥳","😮")
                Column {
                    Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                        allEmojis.take(8).forEach { emoji ->
                            Text(emoji, fontSize = 28.sp, modifier = Modifier.padding(4.dp).clickable {
                                onEmojiSelected(emoji)
                                onDismiss()
                            })
                        }
                    }
                    Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                        allEmojis.drop(8).forEach { emoji ->
                            Text(emoji, fontSize = 28.sp, modifier = Modifier.padding(4.dp).clickable {
                                onEmojiSelected(emoji)
                                onDismiss()
                            })
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancelar") }
            },
            containerColor = Color(0xFF161B22),
            titleContentColor = Color(0xFFE6EDF3),
            textContentColor = Color(0xFFE6EDF3)
        )
    } else {
        // Barra de reacciones rápidas (WhatsApp style)
        Card(
            modifier = Modifier.padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                commonEmojis.forEach { emoji ->
                    Text(
                        emoji,
                        fontSize = 28.sp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                onEmojiSelected(emoji)
                                onDismiss()
                            }
                            .padding(4.dp)
                    )
                }
                // Botón "+" para abrir el selector completo
                IconButton(
                    onClick = { showFullPicker = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Filled.Add, "Más emojis", tint = Color(0xFF8B949E))
                }
            }
        }
    }
}
