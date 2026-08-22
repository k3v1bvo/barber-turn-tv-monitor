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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SupabaseSettings
import com.example.ui.theme.BarberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TvBorder
import com.example.ui.theme.TvSurface

@Composable
fun SupabaseSettingsDialog(
    currentSettings: SupabaseSettings,
    onSave: (url: String, key: String, shopName: String, isDemo: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var url by remember { mutableStateOf(currentSettings.url) }
    var apiKey by remember { mutableStateOf(currentSettings.apiKey) }
    var shopName by remember { mutableStateOf(currentSettings.shopName) }
    var isDemoMode by remember { mutableStateOf(currentSettings.isDemoMode) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(580.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(TvSurface)
                .border(2.dp, BarberGold, RoundedCornerShape(24.dp))
                .padding(28.dp)
        ) {
            Column {
                // Title Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⚡ Configuración Supabase TV",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Switch Modo Demo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Modo Demo / Simulación",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = "Desactiva para conectar tu base de datos Supabase en tiempo real",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }

                        Switch(
                            checked = isDemoMode,
                            onCheckedChange = { isDemoMode = it },
                            modifier = Modifier.testTag("switch_demo_mode"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = BarberGold,
                                checkedTrackColor = ElectricCyan
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Realtime Sync explanation card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x1A10B981))
                        .border(1.dp, Color(0x4010B981), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "⚡ Realtime activo: Escucha cambios automáticos en tablas 'barbers', 'queue', 'asistencias' y 'citas'.",
                            color = Color(0xFF6EE7B7),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Shop Name
                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = { Text("Nombre de la Barbería") },
                    leadingIcon = { Icon(Icons.Default.Store, null, tint = BarberGold) },
                    singleLine = true,
                    modifier = Modifier
                        .testTag("input_shop_name")
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BarberGold,
                        unfocusedBorderColor = TvBorder,
                        focusedLabelColor = BarberGold,
                        unfocusedLabelColor = TextMuted,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Supabase / Central API URL
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL Servidor API / Supabase (e.g. https://tu-barberia.com o https://xyz.supabase.co)") },
                    leadingIcon = { Icon(Icons.Default.Link, null, tint = ElectricCyan) },
                    singleLine = true,
                    modifier = Modifier
                        .testTag("input_supabase_url")
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = TvBorder,
                        focusedLabelColor = ElectricCyan,
                        unfocusedLabelColor = TextMuted,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Supabase Anon API Key
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("Supabase Anon Key / Bearer Token (Opcional si usas tu API Web)") },
                    leadingIcon = { Icon(Icons.Default.Key, null, tint = BarberGold) },
                    singleLine = true,
                    modifier = Modifier
                        .testTag("input_supabase_key")
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BarberGold,
                        unfocusedBorderColor = TvBorder,
                        focusedLabelColor = BarberGold,
                        unfocusedLabelColor = TextMuted,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF334155),
                            contentColor = TextWhite
                        )
                    ) {
                        Text("Cancelar")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            onSave(url, apiKey, shopName, isDemoMode)
                        },
                        modifier = Modifier.testTag("btn_save_settings"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BarberGold,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Guardar y Conectar",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
