package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BarberGold
import com.example.ui.theme.TextDark

@Composable
fun BottomTicker(
    totalCutsToday: Int,
    barbersPresentCount: Int,
    isLiveSupabase: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BarberGold)
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = TextDark,
                modifier = Modifier.padding(end = 8.dp)
            )

            val statusMessage = if (isLiveSupabase) {
                "SISTEMA EN VIVO • Barberos Presentes Hoy: $barbersPresentCount • Cortes Completados Hoy: $totalCutsToday • Utilice 'Pasar Turno' para rotar al siguiente barbero"
            } else {
                "MODO DEMO SUPABASE • Barberos en Servicio: $barbersPresentCount • Presione 'Ajustes' para conectar su URL y API Key de Supabase"
            }

            Text(
                text = statusMessage,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )
        }
    }
}
