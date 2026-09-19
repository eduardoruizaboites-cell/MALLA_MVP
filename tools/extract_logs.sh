#!/usr/bin/env bash
# tools/extract_logs.sh — Filtro de malla_diagnostics.txt para debugging MALLA
#
# Uso:
#   ./tools/extract_logs.sh                    # lee malla_diagnostics.txt → logs_filtrados.txt
#   ./tools/extract_logs.sh ruta/log.txt       # path custom
#   ./tools/extract_logs.sh -o out.txt         # output custom
#   ./tools/extract_logs.sh --errors           # solo bloques de error con contexto
#   ./tools/extract_logs.sh --full             # sin filtro, solo quita ruido

set -eu

INPUT="malla_diagnostics.txt"
OUTPUT="logs_filtrados.txt"
MODE="focused"

while [ $# -gt 0 ]; do
  case "$1" in
    -o) OUTPUT="$2"; shift 2 ;;
    --errors) MODE="errors"; shift ;;
    --full) MODE="full"; shift ;;
    -h|--help)
      grep '^#' "$0" | sed 's/^# \?//' | head -12
      exit 0 ;;
    -*) echo "Flag desconocida: $1" >&2; exit 1 ;;
    *) INPUT="$1"; shift ;;
  esac
done

[ -f "$INPUT" ] || { echo "ERROR: no existe '$INPUT'" >&2; exit 1; }

TOTAL=$(wc -l < "$INPUT" | tr -d ' ')
NOW=$(date '+%Y-%m-%d %H:%M:%S')

# Ruido (advertising repetido — ~85-90% de los logs actuales)
NOISE='\[PROX\] Callback BLE:|\[PROX\] Nodos actualizados:|\[BLE\] Anuncio MALLA detectado:'

# Tags útiles
TAGS='\[BleManager\]|\[BleTransport\]|\[TransportManager\]|\[MessageReceiver\]|\[InvitationManager\]|\[NetworkService\]|\[NotifHelper\]|\[QrScan\]|\[QR\]|\[DIAG\]'

# Keywords de eventos clave
KEYWORDS='Fallo|Rechaz|ERROR|cancelado|excepción|Exception|Fragmentando|Reensamblado|Buffer reseteado|Enviados [0-9]+ fragmentos|MTU con|GATT conectado|Intentando connectAndWriteData|connectAndWriteData|Transporte activo|Sin canal activo|Intentando enviar|BLE recibido|BLE JSON|Notificación de mensaje|Typing de|read_all|Enviando mensaje|sendInvitation|Invitación BLE|writeCharacteristic|writeFramed'

RELEVANT="($TAGS)|($KEYWORDS)"

{
  echo "═══════════════ LOG FILTRADO MALLA ═══════════════"
  echo "Origen:      $INPUT"
  echo "Generado:    $NOW"
  echo "Líneas orig: $TOTAL"
  echo "Modo:        $MODE"
  echo ""
} > "$OUTPUT"

case "$MODE" in
  focused)
    echo "─── LÍNEAS RELEVANTES (ruido de advertising removido) ───" >> "$OUTPUT"
    grep -vE "$NOISE" "$INPUT" | grep -E "$RELEVANT" >> "$OUTPUT" || true
    ;;

  errors)
    echo "─── BLOQUES DE ERROR CON CONTEXTO (±25 líneas, ruido removido) ───" >> "$OUTPUT"
    echo "" >> "$OUTPUT"
    grep -nE 'Fallo|Rechaz|ERROR|cancelado|excepción|Exception' "$INPUT" 2>/dev/null | \
    while IFS=: read -r ln rest; do
      ln=${ln//[^0-9]/}
      [ -z "$ln" ] && continue
      start=$((ln - 25)); [ $start -lt 1 ] && start=1
      end=$((ln + 25))
      {
        echo "─── Error línea $ln ───"
        sed -n "${start},${end}p" "$INPUT" | grep -vE "$NOISE"
        echo ""
      } >> "$OUTPUT"
    done
    ;;

  full)
    echo "─── TODAS LAS LÍNEAS (solo ruido removido) ───" >> "$OUTPUT"
    grep -vE "$NOISE" "$INPUT" >> "$OUTPUT" || true
    ;;
esac

FILTERED=$(wc -l < "$OUTPUT" | tr -d ' ')
if [ "$TOTAL" -gt 0 ]; then
  RATIO=$(awk "BEGIN{printf \"%.1f\", 100.0*$FILTERED/$TOTAL}")
else
  RATIO="0"
fi

echo ""
echo "═══════ RESUMEN ═══════"
echo "Modo:              $MODE"
echo "Líneas originales: $TOTAL"
echo "Líneas filtradas:  $FILTERED ($RATIO%)"
echo "Salida:            $OUTPUT"
echo ""
echo "─── PREVIEW (primeras 12 líneas) ───"
head -12 "$OUTPUT"
