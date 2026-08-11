package com.malla.mvp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun AdvancedColorPicker(
    currentColor: Color?,
    onColorSelected: (Color?) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(currentColor ?: Color(0xFF1A3B4A)) }
    var hue by remember { mutableStateOf(0f) }
    var saturation by remember { mutableStateOf(0.7f) }
    var brightness by remember { mutableStateOf(0.8f) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Botón circular con color actual
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(selectedColor)
                .clickable { showPicker = !showPicker }
        )

        if (showPicker) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.width(260.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    // Círculo cromático (matiz + saturación)
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .pointerInput(Unit) {
                                detectDragGestures { change, _ ->
                                    val cx = size.width / 2
                                    val cy = size.height / 2
                                    val dx = change.position.x - cx
                                    val dy = change.position.y - cy
                                    val dist = sqrt(dx * dx + dy * dy).coerceAtMost(100f)
                                    saturation = (dist / 100f).coerceIn(0f, 1f)
                                    hue = (atan2(dy, dx) * 180f / PI.toFloat() + 360f) % 360f / 360f
                                    selectedColor = Color.hsv(hue * 360f, saturation, brightness)
                                    onColorSelected(selectedColor)
                                }
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Dibujar círculo cromático
                            val center = Offset(size.width / 2, size.height / 2)
                            val radius = size.width / 2
                            for (angle in 0..360 step 5) {
                                val rad = Math.toRadians(angle.toDouble())
                                val x = center.x + radius * cos(rad).toFloat()
                                val y = center.y + radius * sin(rad).toFloat()
                                drawCircle(
                                    color = Color.hsv(angle.toFloat(), 1f, 1f),
                                    radius = 8f,
                                    center = Offset(x, y)
                                )
                            }
                            // Indicador de selección
                            val selAngle = hue * 2 * PI
                            val selRadius = saturation * radius
                            val selX = center.x + selRadius * cos(selAngle).toFloat()
                            val selY = center.y + selRadius * sin(selAngle).toFloat()
                            drawCircle(Color.White, radius = 6f, center = Offset(selX, selY))
                            drawCircle(Color.Black, radius = 4f, center = Offset(selX, selY), style = Stroke(2f))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Barra de brillo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Black, Color.hsv(hue * 360f, saturation, 1f), Color.White)
                                )
                            )
                            .pointerInput(Unit) {
                                detectDragGestures { change, _ ->
                                    brightness = (change.position.x / size.width).coerceIn(0f, 1f)
                                    selectedColor = Color.hsv(hue * 360f, saturation, brightness)
                                    onColorSelected(selectedColor)
                                }
                            }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botón de tema automático
                    TextButton(onClick = {
                        selectedColor = Color(0xFF1A3B4A)
                        onColorSelected(null)
                        showPicker = false
                    }) {
                        Text("Tema automático", color = Color(0xFF00E5FF))
                    }
                }
            }
        }
    }
}
