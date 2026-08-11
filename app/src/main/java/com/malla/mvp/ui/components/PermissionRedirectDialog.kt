package com.malla.mvp.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun PermissionRedirectDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Permisos necesarios", fontWeight = FontWeight.Bold)
        },
        text = {
            Text("Para que MALLA funcione correctamente, debes conceder los permisos solicitados. Ve a Ajustes para activarlos manualmente.")
        },
        confirmButton = {
            TextButton(onClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
                onDismiss()
            }) {
                Text("Abrir Ajustes", color = Color(0xFF00E5FF))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF161B22),
        titleContentColor = Color(0xFFE6EDF3),
        textContentColor = Color(0xFF8B949E)
    )
}
