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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.ui.theme.TvSurface

@Composable
fun TvOverlayPermissionDialog(
    onOpenSettings: () -> Unit,
    onTryDirectStart: () -> Unit,
    onDismiss: () -> Unit
) {
    var isCancelFocused by remember { mutableStateOf(false) }
    var isDirectFocused by remember { mutableStateOf(false) }
    var isSettingsFocused by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .border(2.dp, BarberGold, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = TvSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
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
                    text = "Para que la burbuja de turnos flote encima de otras aplicaciones en tu Xiaomi TV Box, se requiere el permiso de superposición.",
                    fontSize = 13.sp,
                    color = TextWhite,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "📺 Pasos para Xiaomi TV Box / Skyworth:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1. Pulsa 'Abrir Ajustes de TV'.\n2. En Ajustes de la app, ve a 'Permisos' o 'Acceso especial' y activa 'Mostrar sobre otras aplicaciones'.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 17.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // High-Contrast D-Pad Focusable Buttons
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
