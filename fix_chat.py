import re

with open('app/src/main/java/com/malla/mvp/ui/screen/ChatScreen.kt', 'r') as f:
    content = f.read()

# 1. Insertar bottomBar justo antes de containerColor
bottom_bar = '''\
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0A1118))
                        .imePadding()
                ) {
                    AnimatedVisibility(
                        visible = typingText.isNotEmpty() && pendingMediaUris.isEmpty(),
                        enter = expandVertically(animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f)) + fadeIn(tween(200)),
                        exit = shrinkVertically(tween(150)) + fadeOut(tween(150))
                    ) {
                        val avatarBitmap = IdentityManager.avatarBitmap.collectAsState().value
                        ComposingBubble(
                            isOwn = true,
                            avatarBitmap = avatarBitmap,
                            userName = contactName.take(1).uppercase(),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    if (pendingMediaUris.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shadowElevation = 8.dp,
                            color = Color(0xFF1A1A1A)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(pendingMediaUris) { uri ->
                                        Box(
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            AsyncImage(
                                                model = uri,
                                                contentDescription = "Miniatura",
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(16.dp)
                                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                                    .clickable { pendingMediaUris.remove(uri) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Filled.Close,
                                                    contentDescription = "Eliminar",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                    item {
                                        IconButton(
                                            onClick = { showGalleryPanel = true },
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
                                        ) {
                                            Icon(Icons.Filled.Add, "Agregar más", tint = Color(0xFF4CE6FF))
                                        }
                                    }
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    OutlinedTextField(
                                        value = captionText,
                                        onValueChange = { captionText = it },
                                        modifier = Modifier.weight(1f),
                                        placeholder = { Text("Añade un pie de foto...", color = Color.Gray) },
                                        maxLines = 2,
                                        textStyle = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                                        leadingIcon = {
                                            IconButton(onClick = { showEmojiPicker = !showEmojiPicker }) {
                                                Icon(Icons.Filled.InsertEmoticon, "Emoji", tint = Color(0xFF4CE6FF))
                                            }
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF4CE6FF),
                                            unfocusedBorderColor = Color.Gray
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(onClick = {
                                        coroutineScope.launch {
                                            pendingMediaUris.forEachIndexed { index, uri ->
                                                val textToSend = if (index == 0 && captionText.isNotBlank()) captionText else ""
                                                vm.sendMessage(textToSend, mediaUri = uri.toString())
                                            }
                                            pendingMediaUris.clear()
                                            captionText = ""
                                        }
                                    }) {
                                        Icon(Icons.AutoMirrored.Filled.Send, "Enviar", tint = Color(0xFF4CE6FF))
                                    }
                                }
                            }
                        }
                    } else {
                        ChatInputBar(
                            voiceRecorder = voiceRecorder,
                            onSendText = { msg -> vm.sendMessage(msg); typingText = "" },
                            onSendVoice = { file -> vm.sendMessage("", mediaUri = file.absolutePath); typingText = "" },
                            onSendZumbido = { vm.sendZumbido() },
                            onTextChanged = { newText -> typingText = newText }
                        )
                    }
                }
            },
'''

content = content.replace(
    '            containerColor = Color(0xFF0A1118)',
    bottom_bar + '\n            containerColor = Color(0xFF0A1118)'
)

# 2. Eliminar bloque viejo de barra inferior y panel de emojis (desde "// Barra inferior:" hasta "// Paneles externos")
content = re.sub(
    r'\n\s*// Barra inferior:.*?// Paneles externos',
    '',
    content,
    flags=re.DOTALL
)

# 3. Reemplazar el contenido del bloque { padding -> } dejando solo LazyColumn + LaunchedEffect
pattern = r'(\s*\)\s*\{ padding ->\n)(.*?)(\n\s*\}// Paneles externos)'
match = re.search(pattern, content, re.DOTALL)
if match:
    inner = match.group(2)
    lazy_match = re.search(r'(LazyColumn\(.*?\)\s*\{.*?\})', inner, re.DOTALL)
    launched_match = re.search(r'(LaunchedEffect\(.*?\)\s*\{.*?\})', inner, re.DOTALL)
    if lazy_match:
        new_lazy = lazy_match.group(1).replace('modifier = Modifier.weight(1f)', 'modifier = Modifier.fillMaxSize().padding(padding)')
        new_inner = '        ' + new_lazy.strip() + '\n'
        if launched_match:
            new_inner += '        ' + launched_match.group(1).strip() + '\n'
        content = content[:match.start(2)] + new_inner + content[match.end(2):]

# 4. Insertar panel de emojis después del LaunchedEffect
panel_emojis = '''\
                // Panel de emojis
                if (showEmojiPicker) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2A38))
                    ) {
                        val emojis = listOf("😀","😂","😍","😢","😡","👍","👋","🎉","❤️","🔥","😎","🙏","💪","🤔","😴","🥳")
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                                emojis.take(8).forEach { emoji ->
                                    Text(emoji, fontSize = 24.sp, modifier = Modifier.padding(4.dp).clickable {
                                        text = text + emoji; showEmojiPicker = false
                                    })
                                }
                            }
                            Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                                emojis.drop(8).forEach { emoji ->
                                    Text(emoji, fontSize = 24.sp, modifier = Modifier.padding(4.dp).clickable {
                                        text = text + emoji; showEmojiPicker = false
                                    })
                                }
                            }
                        }
                    }
                }
'''
content = content.replace('    // Paneles externos', panel_emojis + '\n    // Paneles externos')

with open('app/src/main/java/com/malla/mvp/ui/screen/ChatScreen.kt', 'w') as f:
    f.write(content)

print("ChatScreen.kt actualizado exitosamente.")
