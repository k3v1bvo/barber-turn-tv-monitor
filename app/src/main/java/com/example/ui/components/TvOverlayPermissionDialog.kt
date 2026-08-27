package com.example.ui.components

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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    onSaveTargetIp: (String) -> Unit = {},
    onOpenSettings: () -> Unit,
    onTryDirectStart: () -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var isCancelFocused by remember { mutableStateOf(false) }
    var isDirectFocused by remember { mutableStateOf(false) }
    var isSettingsFocused by remember { mutableStateOf(false) }
    var isAutoLocalFocused by remember { mutableStateOf(false) }
    var isSendRemoteFocused by remember { mutableStateOf(false) }

    var targetIpInput by remember(initialTargetIp) { mutableStateOf(initialTargetIp) }
    var isExecutingAdb by remember { mutableStateOf(false) }
    var adbStatusMessage by remember { mutableStateOf<String?>(null) }
    var isAdbSuccess by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .border(2.dp, BarberGold, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = TvSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Brush.radialGradient(listOf(BarberGold, Color(0xFFD97706))), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🫧", fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Modo Burbuja en Android TV",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Superposición sobre YouTube, Netflix y otras apps",
                            fontSize = 12.sp,
                            color = BarberGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Para que la burbuja de turnos flote encima de otras aplicaciones en tu Xiaomi TV Box o Smart TV, se requiere el permiso de superposición.",
                    fontSize = 13.sp,
                    color = TextWhite,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // =========================================================================
                // 1-CLICK AUTO ADB PERMISSION HELPER (NEW FEATURE)
                // =========================================================================
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
                                text = "Auto-Conceder Permiso por Red (Sin apps extras)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Si activaste 'Depuración USB/Red' en la TV (los 7 clics), la app puede enviarse el permiso a sí misma o recibirlo desde tu celular:",
                            fontSize = 11.sp,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Button: Auto-Grant directly on this TV
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
                                        val result = AdbHelper.grantOverlayPermission("127.0.0.1")
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

                        Spacer(modifier = Modifier.height(10.dp))

                        // Remote Mobile Sending Input (If user is using phone app to trigger on TV)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = targetIpInput,
                                onValueChange = { targetIpInput = it },
                                placeholder = { Text("IP de la TV (ej: 192.168.1.45)", fontSize = 11.sp, color = TextMuted) },
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
                                            val result = AdbHelper.grantOverlayPermission(cleanIp)
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
                                    .padding(horizontal = 12.dp, vertical = 12.dp)
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

                // Standard Action Buttons
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
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("Cancelar", fontSize = 12.sp, color = if (isCancelFocused) TextWhite else TextMuted)
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
                            Text("Iniciar Directo", fontSize = 12.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Open Settings Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSettingsFocused) Color.White else BarberGold)
                            .border(
                                width = if (isSettingsFocused) 3.dp else 1.dp,
                                color = if (isSettingsFocused) ElectricCyan else BarberGold,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .onFocusChanged { isSettingsFocused = it.isFocused }
                            .focusable()
                            .clickable { onOpenSettings() }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Abrir Ajustes", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}
