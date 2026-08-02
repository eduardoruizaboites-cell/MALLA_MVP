# Lee los archivos
with open('bottom_bar.txt', 'r') as f:
    bottom_bar = f.read()
with open('content.txt', 'r') as f:
    new_content = f.read()

# Lee el archivo original
with open('app/src/main/java/com/malla/mvp/ui/screen/ChatScreen.kt', 'r') as f:
    lines = f.readlines()

# 1. Insertar bottomBar después de la línea que contiene "            }," justo antes de containerColor
# Buscamos la línea que coincide exactamente con "            }," después del topBar
output = []
inserted_bottom = False
skip_old = False
found_padding = False
brace_depth = 0
i = 0
while i < len(lines):
    line = lines[i]
    # Insertar justo después de la línea que contiene "            }," y antes de "containerColor"
    if not inserted_bottom and line.strip() == '},' and i+1 < len(lines) and 'containerColor' in lines[i+1]:
        output.append(line)
        output.append(bottom_bar)
        inserted_bottom = True
        i += 1
        continue

    # Al encontrar ') { padding ->' comenzamos a saltar hasta el cierre del bloque Scaffold antes de '// Paneles externos'
    if ') { padding ->' in line:
        output.append(line)
        found_padding = True
        i += 1
        # Saltar líneas hasta encontrar "    // Paneles externos"
        while i < len(lines):
            if lines[i].strip().startswith('// Paneles externos'):
                # Insertar nuevo contenido y luego la línea actual (// Paneles externos)
                output.append(new_content)
                output.append(lines[i])
                found_padding = False
                i += 1
                break
            i += 1
        continue

    output.append(line)
    i += 1

with open('app/src/main/java/com/malla/mvp/ui/screen/ChatScreen.kt', 'w') as f:
    f.writelines(output)
print("Modificaciones aplicadas correctamente.")
