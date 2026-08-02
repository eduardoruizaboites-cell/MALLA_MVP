with open('app/src/main/java/com/malla/mvp/ui/screen/ChatScreen.kt', 'r') as f:
    lines = f.readlines()
with open('bottom_bar.txt', 'r') as f:
    bottom_bar = f.read()
with open('content.txt', 'r') as f:
    new_content = f.read()

idx_container = None
idx_padding = None
idx_extern = None
for i, line in enumerate(lines):
    if 'containerColor = Color(0xFF0A1118)' in line:
        idx_container = i
    if ') { padding ->' in line:
        idx_padding = i
    if line.strip().startswith('// Paneles externos'):
        idx_extern = i

if None in (idx_container, idx_padding, idx_extern):
    print("No se encontraron las líneas necesarias")
    exit(1)

head = lines[:idx_container]
bb = bottom_bar if bottom_bar.endswith('\n') else bottom_bar + '\n'
middle = lines[idx_container:idx_padding + 1]
body = new_content if new_content.endswith('\n') else new_content + '\n'
closing_brace = '        }\n    }\n\n'   # cierra Scaffold (8sp) y luego el Box del shake (4sp)
tail = lines[idx_extern:]

with open('app/src/main/java/com/malla/mvp/ui/screen/ChatScreen.kt', 'w') as f:
    f.writelines(head)
    f.write(bb)
    f.writelines(middle)
    f.write(body)
    f.write(closing_brace)
    f.writelines(tail)

print("Archivo actualizado correctamente.")
