package com.example.ui.components

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BarberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldLive
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TvBorder
import com.example.ui.theme.TvSurface
import com.example.util.AdbHelper
import kotlinx.coroutines.launch

@Composable
fun TvOverlayPermissionDialog(
    initialTargetIp: String = "",
    isTvDevice: Boolean = true,
    onSaveTargetIp: (String) -> Unit = {},
    onOpenSettings: () -> Unit,
    onOpenXiaomiSettings: () -> Unit = {},
    onTryDirectStart: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    var isCancelFocused by remember { mutableStateOf(false) }
    var isDirectFocused by remember { mutableStateOf(false) }
    var isSettingsFocused by remember { mutableStateOf(false) }
    var isXiaomiSettingsFocused by remember { mutableStateOf(false) }
    var isAutoLocalFocused by remember { mutableStateOf(false) }
    var isSendRemoteFocused by remember { mutableStateOf(false) }
    var isScanFocused by remember { mutableStateOf(false) }

    var targetIpInput by remember(initialTargetIp) { mutableStateOf(initialTargetIp) }
    var isExecutingAdb by remember { mutableStateOf(false) }
    var isScanningNetwork by remember { mutableStateOf(false) }
    var adbStatusMessage by remember { mutableStateOf<String?>(null) }
    var isAdbSuccess by remember { mutableStateOf(false) }
    var localDeviceIp by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        localDeviceIp = AdbHelper.getLocalIpAddress(context)
    }

    val isXiaomi = remember {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val brand = Build.BRAND.lowercase()
        manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco") ||
                brand.contains("xiaomi") || brand.contains("redmi") || brand.contains("poco")
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .border(2.dp, BarberGold, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = TvSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Brush.radialGradient(listOf(BarberGold, Color(0xFFD97706))), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🫧", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isTvDevice) "Modo Burbuja en Xiaomi TV Box" else "Modo Burbuja Flotante",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Superposición sobre YouTube, Netflix y otras apps",
                            fontSize = 11.sp,
                            color = BarberGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Info text
                Text(
                    text = if (isTvDevice) {
                        "En Android TV / Xiaomi TV Box los permisos de superposición se activan mediante depuración USB (1 solo clic) o enviándolo desde tu celular:"
                    } else {
                        "Para que la burbuja de turnos flote encima de tus aplicaciones, activa el permiso de 'Mostrar sobre otras apps' (o 'Ventanas emergentes'):"
                    },
                    fontSize = 12.sp,
                    color = TextWhite,
                    lineHeight = 16.sp
                )

                if (localDeviceIp != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "📡 IP de este dispositivo: $localDeviceIp",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ElectricCyan
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // =========================================================================
                // SECTION 1: TV BOX AUTO-ACTIVATION (LOCAL 127.0.0.1)
                // =========================================================================
                if (isTvDevice) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚡", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Auto-Activar en esta TV (1-Clic)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Con 'Depuración USB' activada en Ajustes de la TV, presiona este botón:",
                                fontSize = 11.sp,
                                color = TextMuted
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isAutoLocalFocused) Color(0xFF1E3A8A) else Color(0xFF1E293B))
                                    .border(
                                        width = if (isAutoLocalFocused) 2.dp else 1.dp,
                                        color = if (isAutoLocalFocused) ElectricCyan else Color(0xFF3B82F6),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .onFocusChanged { isAutoLocalFocused = it.isFocused }
                                    .focusable()
                                    .clickable(enabled = !isExecutingAdb) {
                                        isExecutingAdb = true
                                        adbStatusMessage = "Conectando al servicio de la TV..."
                                        coroutineScope.launch {
                                            val result = AdbHelper.grantOverlayPermission(context, "127.0.0.1")
                                            isExecutingAdb = false
                                            when (result) {
                                                is AdbHelper.AdbResult.Success -> {
                                                    isAdbSuccess = true
                                                    adbStatusMessage = result.message
                                                    onTryDirectStart()
                                                }
                                                is AdbHelper.AdbResult.NeedsAuth -> {
                                                    isAdbSuccess = false
                                                    adbStatusMessage = result.message
                                                }
                                                is AdbHelper.AdbResult.Error -> {
                                                    isAdbSuccess = false
                                                    adbStatusMessage = result.message
                                                }
                                            }
                                        }
                                    }
                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isExecutingAdb) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ElectricCyan, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                    } else {
                                        Icon(Icons.Default.Bolt, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = "⚡ AUTO-ACTIVAR EN ESTA TV (1-CLIC)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ElectricCyan
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // =========================================================================
                // SECTION 2: MOBILE PERMISSIONS (IF RUNNING ON PHONE)
                // =========================================================================
                if (!isTvDevice) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = BarberGold, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Permiso en este Celular",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BarberGold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Activa el interruptor 'Permitir mostrar sobre otras apps':",
                                fontSize = 11.sp,
                                color = TextMuted
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSettingsFocused) Color.White else BarberGold)
                                        .onFocusChanged { isSettingsFocused = it.isFocused }
                                        .focusable()
                                        .clickable { onOpenSettings() }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Settings, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Abrir Ajustes de Superposición", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
                                }

                                if (isXiaomi) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isXiaomiSettingsFocused) Color(0xFF047857) else Color(0xFF065F46))
                                            .onFocusChanged { isXiaomiSettingsFocused = it.isFocused }
                                            .focusable()
                                            .clickable { onOpenXiaomiSettings() }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("⚙️ Permisos Xiaomi / HyperOS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // =========================================================================
                // SECTION 3: REMOTE SEND TO TV OVER WI-FI (FOR PHONES OR OTHER TVS)
                // =========================================================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF131B2A))
                        .border(1.dp, Color(0xFF2E3D60), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Tv, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "📡 Activar Permiso en TV Box por Wi-Fi",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan
                                )
                            }

                            // Auto Scan button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isScanFocused) Color(0xFF0284C7) else Color(0xFF0369A1))
                                    .onFocusChanged { isScanFocused = it.isFocused }
                                    .focusable()
                                    .clickable(enabled = !isScanningNetwork && !isExecutingAdb) {
                                        isScanningNetwork = true
                                        adbStatusMessage = "Escaneando red Wi-Fi en busca de la TV..."
                                        coroutineScope.launch {
                                            val found = AdbHelper.scanLocalNetworkForAdb(context)
                                            isScanningNetwork = false
                                            if (found.isNotEmpty()) {
                                                targetIpInput = found.first()
                                                onSaveTargetIp(found.first())
                                                adbStatusMessage = "¡TV Box encontrada en ${found.first()}! Pulsa 'Enviar a TV'."
                                            } else {
                                                adbStatusMessage = "No se detectó automáticamente. Asegúrate de activar 'Depuración USB' en la TV y escribe su IP."
                                            }
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isScanningNetwork) {
                                        CircularProgressIndicator(modifier = Modifier.size(12.dp), color = TextWhite, strokeWidth = 1.5.dp)
                                    } else {
                                        Icon(Icons.Default.Search, contentDescription = null, tint = TextWhite, modifier = Modifier.size(14.dp))
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Auto-Buscar TV", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Si tienes la TV Box y este celular en el mismo Wi-Fi, envíale el permiso directamente:",
                            fontSize = 11.sp,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = targetIpInput,
                                onValueChange = { targetIpInput = it },
                                placeholder = { Text("IP de la TV (ej: 192.168.1.45)", fontSize = 11.sp, color = TextMuted) },
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            val clip = clipboardManager.getText()?.text
                                            if (!clip.isNullOrBlank()) {
                                                targetIpInput = clip.trim()
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.ContentPaste, contentDescription = "Pegar", tint = ElectricCyan, modifier = Modifier.size(16.dp))
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricCyan,
                                    unfocusedBorderColor = TvBorder,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSendRemoteFocused) Color(0xFF047857) else EmeraldLive)
                                    .border(
                                        width = if (isSendRemoteFocused) 2.dp else 1.dp,
                                        color = if (isSendRemoteFocused) Color.White else EmeraldLive,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .onFocusChanged { isSendRemoteFocused = it.isFocused }
                                    .focusable()
                                    .clickable(enabled = !isExecutingAdb && targetIpInput.isNotBlank()) {
                                        val cleanIp = targetIpInput.trim()
                                        onSaveTargetIp(cleanIp)
                                        isExecutingAdb = true
                                        adbStatusMessage = "Enviando permiso a $cleanIp..."
                                        coroutineScope.launch {
                                            val result = AdbHelper.grantOverlayPermission(context, cleanIp)
                                            isExecutingAdb = false
                                            when (result) {
                                                is AdbHelper.AdbResult.Success -> {
                                                    isAdbSuccess = true
                                                    adbStatusMessage = result.message
                                                }
                                                is AdbHelper.AdbResult.NeedsAuth -> {
                                                    isAdbSuccess = false
                                                    adbStatusMessage = result.message
                                                }
                                                is AdbHelper.AdbResult.Error -> {
                                                    isAdbSuccess = false
                                                    adbStatusMessage = result.message
                                                }
                                            }
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Enviar a TV", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                        }

                        // Feedback Status Message
                        if (adbStatusMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = adbStatusMessage ?: "",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isAdbSuccess) EmeraldLive else Color(0xFFFBBF24),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons at Bottom
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cancel Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCancelFocused) Color(0xFF334155) else Color.Transparent)
                            .border(
                                width = if (isCancelFocused) 2.dp else 1.dp,
                                color = if (isCancelFocused) ElectricCyan else Color(0x33FFFFFF),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .onFocusChanged { isCancelFocused = it.isFocused }
                            .focusable()
                            .clickable { onDismiss() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("Cerrar", fontSize = 12.sp, color = if (isCancelFocused) TextWhite else TextMuted)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Direct Start Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDirectFocused) Color(0xFF064E3B) else Color(0xFF1E293B))
                            .border(
                                width = if (isDirectFocused) 3.dp else 1.dp,
                                color = if (isDirectFocused) ElectricCyan else EmeraldLive.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .onFocusChanged { isDirectFocused = it.isFocused }
                            .focusable()
                            .clickable { onTryDirectStart() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = EmeraldLive, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Probar Iniciar Burbuja", fontSize = 12.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
