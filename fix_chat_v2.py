import re

with open('app/src/main/java/com/malla/mvp/ui/screen/ChatScreen.kt', 'r') as f:
    lines = f.readlines()

# 1. Insertar bottomBar justo antes de la línea que contiene "containerColor = Color(0xFF0A1118)"
bottom_bar_code = [
    '            bottomBar = {\n',
    '                Column(\n',
    '                    modifier = Modifier\n',
    '                        .fillMaxWidth()\n',
    '                        .background(Color(0xFF0A1118))\n',
    '                        .imePadding()\n',
    '                ) {\n',
    '                    AnimatedVisibility(\n',
    '                        visible = typingText.isNotEmpty() && pendingMediaUris.isEmpty(),\n',
    '                        enter = expandVertically(animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f)) + fadeIn(tween(200)),\n',
    '                        exit = shrinkVertically(tween(150)) + fadeOut(tween(150))\n',
    '                    ) {\n',
    '                        val avatarBitmap = IdentityManager.avatarBitmap.collectAsState().value\n',
    '                        ComposingBubble(\n',
    '                            isOwn = true,\n',
    '                            avatarBitmap = avatarBitmap,\n',
    '                            userName = contactName.take(1).uppercase(),\n',
    '                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)\n',
    '                        )\n',
    '                    }\n',
    '\n',
    '                    if (pendingMediaUris.isNotEmpty()) {\n',
    '                        Surface(\n',
    '                            modifier = Modifier.fillMaxWidth(),\n',
    '                            shadowElevation = 8.dp,\n',
    '                            color = Color(0xFF1A1A1A)\n',
    '                        ) {\n',
    '                            Column(modifier = Modifier.padding(8.dp)) {\n',
    '                                LazyRow(\n',
    '                                    modifier = Modifier.fillMaxWidth(),\n',
    '                                    horizontalArrangement = Arrangement.spacedBy(8.dp)\n',
    '                                ) {\n',
    '                                    items(pendingMediaUris) { uri ->\n',
    '                                        Box(modifier = Modifier.size(48.dp)) {\n',
    '                                            AsyncImage(\n',
    '                                                model = uri,\n',
    '                                                contentDescription = "Miniatura",\n',
    '                                                modifier = Modifier\n',
    '                                                    .fillMaxSize()\n',
    '                                                    .clip(RoundedCornerShape(8.dp)),\n',
    '                                                contentScale = ContentScale.Crop\n',
    '                                            )\n',
    '                                            Box(\n',
    '                                                modifier = Modifier\n',
    '                                                    .align(Alignment.TopEnd)\n',
    '                                                    .size(16.dp)\n',
    '                                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))\n',
    '                                                    .clickable { pendingMediaUris.remove(uri) },\n',
    '                                                contentAlignment = Alignment.Center\n',
    '                                            ) {\n',
    '                                                Icon(Icons.Filled.Close, contentDescription = "Eliminar", tint = Color.White, modifier = Modifier.size(12.dp))\n',
    '                                            }\n',
    '                                        }\n',
    '                                    }\n',
    '                                    item {\n',
    '                                        IconButton(\n',
    '                                            onClick = { showGalleryPanel = true },\n',
    '                                            modifier = Modifier\n',
    '                                                .size(48.dp)\n',
    '                                                .background(Color(0xFF2A2A2A), RoundedCornerShape(8.dp))\n',
    '                                        ) {\n',
    '                                            Icon(Icons.Filled.Add, "Agregar más", tint = Color(0xFF4CE6FF))\n',
    '                                        }\n',
    '                                    }\n',
    '                                }\n',
    '                                Row(\n',
    '                                    modifier = Modifier\n',
    '                                        .fillMaxWidth()\n',
    '                                        .padding(top = 8.dp),\n',
    '                                    verticalAlignment = Alignment.Bottom\n',
    '                                ) {\n',
    '                                    OutlinedTextField(\n',
    '                                        value = captionText,\n',
    '                                        onValueChange = { captionText = it },\n',
    '                                        modifier = Modifier.weight(1f),\n',
    '                                        placeholder = { Text("Añade un pie de foto...", color = Color.Gray) },\n',
    '                                        maxLines = 2,\n',
    '                                        textStyle = MaterialTheme.typography.bodySmall.copy(color = Color.White),\n',
    '                                        leadingIcon = {\n',
    '                                            IconButton(onClick = { showEmojiPicker = !showEmojiPicker }) {\n',
    '                                                Icon(Icons.Filled.InsertEmoticon, "Emoji", tint = Color(0xFF4CE6FF))\n',
    '                                            }\n',
    '                                        },\n',
    '                                        colors = OutlinedTextFieldDefaults.colors(\n',
    '                                            focusedBorderColor = Color(0xFF4CE6FF),\n',
    '                                            unfocusedBorderColor = Color.Gray\n',
    '                                        )\n',
    '                                    )\n',
    '                                    Spacer(modifier = Modifier.width(8.dp))\n',
    '                                    IconButton(onClick = {\n',
    '                                        coroutineScope.launch {\n',
    '                                            pendingMediaUris.forEachIndexed { index, uri ->\n',
    '                                                val textToSend = if (index == 0 && captionText.isNotBlank()) captionText else ""\n',
    '                                                vm.sendMessage(textToSend, mediaUri = uri.toString())\n',
    '                                            }\n',
    '                                            pendingMediaUris.clear()\n',
    '                                            captionText = ""\n',
    '                                        }\n',
    '                                    }) {\n',
    '                                        Icon(Icons.AutoMirrored.Filled.Send, "Enviar", tint = Color(0xFF4CE6FF))\n',
    '                                    }\n',
    '                                }\n',
    '                            }\n',
    '                        }\n',
    '                    } else {\n',
    '                        ChatInputBar(\n',
    '                            voiceRecorder = voiceRecorder,\n',
    '                            onSendText = { msg -> vm.sendMessage(msg); typingText = "" },\n',
    '                            onSendVoice = { file -> vm.sendMessage("", mediaUri = file.absolutePath); typingText = "" },\n',
    '                            onSendZumbido = { vm.sendZumbido() },\n',
    '                            onTextChanged = { newText -> typingText = newText }\n',
    '                        )\n',
    '                    }\n',
    '                }\n',
    '            },\n',
]

new_lines = []
inserted = False
skip_old = False
brace_count = 0
found_padding = False
new_content_lines = [
    '        LazyColumn(\n',
    '            modifier = Modifier\n',
    '                .fillMaxSize()\n',
    '                .padding(padding),\n',
    '            state = listState,\n',
    '            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)\n',
    '        ) {\n',
    '            items(messages) { msg ->\n',
    '                MessageBubbleV2(msg = msg, animate = vm.isMessageNew(msg.timestamp), onImageClick = { uri -> fullScreenImageUri = uri })\n',
    '            }\n',
    '        }\n',
    '\n',
    '        LaunchedEffect(messages.size, typingText) {\n',
    '            if (messages.isNotEmpty()) {\n',
    '                listState.animateScrollToItem(messages.size - 1)\n',
    '            }\n',
    '        }\n',
]

i = 0
while i < len(lines):
    line = lines[i]
    # Insertar bottomBar justo antes de containerColor
    if 'containerColor = Color(0xFF0A1118)' in line and not inserted:
        new_lines.extend(bottom_bar_code)
        inserted = True
        new_lines.append(line)
        i += 1
        continue

    # Al llegar a ') { padding ->' empezamos a capturar el bloque viejo para eliminarlo
    if ') { padding ->' in line:
        new_lines.append(line)  # conservamos ') { padding ->'
        found_padding = True
        i += 1
        # Saltar hasta que brace_count vuelva a 0 (fin del bloque Scaffold)
        while i < len(lines):
            l = lines[i]
            if '{' in l: brace_count += l.count('{')
            if '}' in l: brace_count -= l.count('}')
            if brace_count == 0 and found_padding:
                # Terminó el bloque interior del Scaffold
                # Insertamos nuestro nuevo contenido antes de la llave de cierre
                new_lines.extend(new_content_lines)
                new_lines.append(l)  # la línea con '}' que cierra el Scaffold
                i += 1
                found_padding = False
                break
            i += 1
        continue

    # Eliminar líneas desde "// Barra inferior:" hasta "// Paneles externos" (exclusive)
    if '// Barra inferior:' in line:
        while i < len(lines) and '// Paneles externos' not in lines[i]:
            i += 1
        # i está en la línea de '// Paneles externos', la conservamos
        new_lines.append(lines[i])
        i += 1
        continue

    new_lines.append(line)
    i += 1

with open('app/src/main/java/com/malla/mvp/ui/screen/ChatScreen.kt', 'w') as f:
    f.writelines(new_lines)

print("Archivo actualizado correctamente.")
