package com.malla.mvp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.platform.LocalDensity
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

    // Inicializar HSV desde el color actual
    LaunchedEffect(currentColor) {
        if (currentColor != null) {
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(currentColor.value.toInt(), hsv)
            hue = hsv[0] / 360f
            saturation = hsv[1]
            brightness = hsv[2]
        }
    }

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
                modifier = Modifier.width(280.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Mapa de tono/saturación (200x200 dp aprox)
                    val mapSize = 200.dp
                    val density = LocalDensity.current
                    val mapSizePx = with(density) { mapSize.toPx() }

                    Box(
                        modifier = Modifier
                            .size(mapSize)
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    val x = offset.x / mapSizePx
                                    val y = 1f - offset.y / mapSizePx
                                    saturation = x.coerceIn(0f, 1f)
                                    brightness = y.coerceIn(0f, 1f)
                                    selectedColor = Color.hsv(hue * 360f, saturation, brightness)
                                    onColorSelected(selectedColor)
                                }
                                detectDragGestures { change, _ ->
                                    val x = change.position.x / mapSizePx
                                    val y = 1f - change.position.y / mapSizePx
                                    saturation = x.coerceIn(0f, 1f)
                                    brightness = y.coerceIn(0f, 1f)
                                    selectedColor = Color.hsv(hue * 360f, saturation, brightness)
                                    onColorSelected(selectedColor)
                                }
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Gradiente de blanco a color puro (horizontal)
                            val colorPure = Color.hsv(hue * 360f, 1f, 1f)
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    listOf(Color.White, colorPure)
                                ),
                                size = size
                            )
                            // Gradiente de transparente a negro (vertical)
                            drawRect(
                                brush = Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black)
                                ),
                                size = size
                            )
                            // Indicador de selección
                            val selX = saturation * size.width
                            val selY = (1f - brightness) * size.height
                            drawCircle(
                                color = Color.White,
                                radius = 8f,
                                center = Offset(selX, selY),
                                style = Stroke(2f)
                            )
                            drawCircle(
                                color = Color.Black.copy(alpha = 0.5f),
                                radius = 6f,
                                center = Offset(selX, selY)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Barra de tono (hue)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(
                                    (0..360 step 30).map { Color.hsv(it.toFloat(), 1f, 1f) }
                                )
                            )
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    hue = (offset.x / size.width).coerceIn(0f, 1f)
                                    selectedColor = Color.hsv(hue * 360f, saturation, brightness)
                                    onColorSelected(selectedColor)
                                }
                                detectDragGestures { change, _ ->
                                    hue = (change.position.x / size.width).coerceIn(0f, 1f)
                                    selectedColor = Color.hsv(hue * 360f, saturation, brightness)
                                    onColorSelected(selectedColor)
                                }
                            }
                    ) {
                        // Indicador de tono
                        Canvas(modifier = Modifier.fillMaxSize().padding(vertical = 2.dp)) {
                            val indX = hue * size.width
                            drawCircle(
                                color = Color.White,
                                radius = 10f,
                                center = Offset(indX, size.height / 2),
                                style = Stroke(2f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón de tema automático
                    TextButton(
                        onClick = {
                            onColorSelected(null)
                            selectedColor = Color(0xFF1A3B4A)
                            showPicker = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tema automático", color = Color(0xFF00E5FF))
                    }
                }
            }
        }
    }
}
