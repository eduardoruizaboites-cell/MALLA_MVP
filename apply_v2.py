with open('app/src/main/java/com/malla/mvp/ui/screen/ChatScreen.kt', 'r') as f:
    lines = f.readlines()

with open('bottom_bar.txt', 'r') as f:
    bottom_bar = f.read()

with open('content.txt', 'r') as f:
    new_content = f.read()

# Insertar bottomBar justo antes de "containerColor" (línea 184, índice 183)
output = lines[:183]  # líneas 0 a 182
output.append(bottom_bar)      # el nuevo bottomBar (ya incluye salto de línea)
output.append(lines[183])      # línea original: "            containerColor = Color(0xFF0A1118)"
output.append(lines[184])      # ") { padding ->"

# Añadir nuevo contenido
output.append(new_content)

# Buscar la línea que contiene "    // Paneles externos" y agregar desde ahí hasta el final
start_extern = None
for i, line in enumerate(lines):
    if line.strip().startswith('// Paneles externos'):
        start_extern = i
        break
if start_extern is not None:
    output.extend(lines[start_extern:])
else:
    # Si no se encuentra, añadir desde línea 357 (según el grep anterior)
    output.extend(lines[356:])  # índice 356

with open('app/src/main/java/com/malla/mvp/ui/screen/ChatScreen.kt', 'w') as f:
    f.writelines(output)
print("Archivo actualizado exitosamente.")
