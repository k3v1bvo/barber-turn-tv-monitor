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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Barber
import com.example.ui.theme.BarberGold
import com.example.ui.theme.EmeraldLive
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TvBorder
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceVariant

@Composable
fun ActiveCutsSection(
    activeBarbers: List<Barber>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(TvSurface)
            .border(1.dp, TvBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ContentCut,
                        contentDescription = null,
                        tint = EmeraldLive,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ATENDIENDO EN SILLÓN",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite,
                        letterSpacing = 0.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(EmeraldLive.copy(alpha = 0.2f))
                        .border(1.dp, EmeraldLive, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${activeBarbers.size} activos",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldLive
                    )
                }
            }

            if (activeBarbers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No hay barberos atendiendo en este momento.",
                            fontSize = 14.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "Todos los barberos están disponibles en la fila.",
                            fontSize = 12.sp,
                            color = BarberGold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = activeBarbers,
                        key = { it.id }
                    ) { barber ->
                        ActiveBarberItem(barber = barber)
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveBarberItem(barber: Barber) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .testTag("active_barber_${barber.id}")
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isFocused) Color(0xFF1E2E3E) else TvSurfaceVariant)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) EmeraldLive else Color(0x2510B981),
                shape = RoundedCornerShape(14.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar
                BarberAvatar(
                    fullName = barber.fullName,
                    photoUrl = barber.avatarUrl ?: barber.selfieUrl,
                    size = 42.dp,
                    borderWidth = 2.dp,
                    borderColor = EmeraldLive,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BarberAvailabilityDot(
                            status = barber.status,
                            size = 8.dp,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = barber.fullName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        BarberAvailabilityBadge(status = barber.status)
                    }
                    Text(
                        text = "Cliente: ${barber.activeClientName ?: "Cliente en sillón"}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldLive
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(EmeraldLive.copy(alpha = 0.15f))
                    .border(1.dp, EmeraldLive.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = barber.currentService ?: "EN CORTE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = EmeraldLive
                )
            }
        }
    }
}

