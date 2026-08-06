package com.malla.mvp.emoji.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malla.mvp.emoji.EmojiCategory
import com.malla.mvp.emoji.emojiCategories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiPicker(
    onEmojiSelected: (String) -> Unit,
    onDismissKeyboard: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val categories = emojiCategories
    val recentEmojis = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()  // Ocupar todo el espacio disponible del ModalBottomSheet
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 8.dp
        ) {
            categories.forEachIndexed { index, category ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    icon = {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = category.label,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        // Contenido del panel: usa weight(1f) para ocupar el resto verticalmente
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val selectedCategory = if (selectedTabIndex in categories.indices) categories[selectedTabIndex] else null
            if (selectedCategory != null) {
                if (selectedCategory.label == "GIFs") {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Keyboard,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "GIFs próximamente",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    val currentEmojis = if (selectedCategory.label == "Recientes") {
                        if (recentEmojis.isEmpty()) selectedCategory.emojis.take(30) else recentEmojis.toList()
                    } else {
                        selectedCategory.emojis
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier
                            .fillMaxSize()  // ocupar todo el espacio del Box
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(currentEmojis) { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 24.sp,
                                modifier = Modifier
                                    .clickable {
                                        onEmojiSelected(emoji)
                                        if (!recentEmojis.contains(emoji)) {
                                            recentEmojis.add(0, emoji)
                                            if (recentEmojis.size > 30) recentEmojis.removeAt(recentEmojis.size - 1)
                                        }
                                    }
                                    .padding(4.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Botón para cerrar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDismissKeyboard) {
                    Icon(
                        Icons.Default.Keyboard,
                        contentDescription = "Teclado",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
