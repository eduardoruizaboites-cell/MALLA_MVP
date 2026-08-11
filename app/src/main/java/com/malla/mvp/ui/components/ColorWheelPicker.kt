package com.malla.mvp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ColorWheelPicker(
    currentColor: Color?,
    onColorSelected: (Color?) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(currentColor ?: Color(0xFF1A3B4A)) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(selectedColor)
                .clickable { showPicker = !showPicker }
        )

        if (showPicker) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val colors = listOf(
                    Color(0xFF1A3B4A) to "Azul",
                    Color(0xFF4CAF50) to "Verde",
                    Color(0xFFFF7043) to "Naranja",
                    Color(0xFF9575CD) to "Morado",
                    Color(0xFF78909C) to "Gris",
                    Color(0xFF00E5FF) to "Cian",
                    Color(0xFFE74C3C) to "Rojo",
                    Color(0xFFFFEB3B) to "Amarillo",
                    null to "Auto"
                )
                colors.forEach { (color, name) ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color ?: Color.Gray)
                            .then(
                                if (color == selectedColor || (color == null && selectedColor == null))
                                    Modifier.border(3.dp, Color.White, CircleShape)
                                else Modifier
                            )
                            .clickable {
                                selectedColor = color ?: Color(0xFF1A3B4A)
                                onColorSelected(color)
                                showPicker = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (color == null) {
                            Text("A", color = Color.White, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
