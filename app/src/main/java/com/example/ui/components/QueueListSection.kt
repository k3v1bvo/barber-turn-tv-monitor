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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListNumbered
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Barber
import com.example.ui.theme.BarberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldLive
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TvBorder
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceVariant

@Composable
fun QueueListSection(
    queuedBarbers: List<Barber>,
    modifier: Modifier = Modifier
) {
    // Show barbers from position #2 onwards (position #1 is in Hero card)
    val remainingQueue = if (queuedBarbers.size > 1) queuedBarbers.drop(1) else emptyList()

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
                        imageVector = Icons.Default.FormatListNumbered,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SIGUIENTES EN LA FILA",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite,
                        letterSpacing = 0.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${remainingQueue.size} en espera",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }
            }

            if (remainingQueue.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay más barberos en espera en la fila.",
                        fontSize = 14.sp,
                        color = TextMuted
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        items = remainingQueue,
                        key = { _, barber -> barber.id }
                    ) { index, barber ->
                        QueueBarberItem(
                            position = index + 2, // #2, #3, #4...
                            barber = barber
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QueueBarberItem(
    position: Int,
    barber: Barber
) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .testTag("queue_item_$position")
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isFocused) Color(0xFF222F4B) else TvSurfaceVariant)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) ElectricCyan else Color(0x2038BDF8),
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
            // Left: Position Badge & Barber Avatar/Name
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Position Badge
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            if (position == 2) ElectricCyan else Color(0xFF334155)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#$position",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = if (position == 2) Color.Black else TextWhite
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Avatar
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF27272A)),
                    contentAlignment = Alignment.Center
                ) {
                    val photoUrl = barber.avatarUrl?.ifBlank { null } ?: barber.selfieUrl
                    if (!photoUrl.isNullOrBlank()) {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        AsyncImage(
                            model = coil.request.ImageRequest.Builder(context)
                                .data(photoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = barber.fullName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        val initial = barber.fullName.firstOrNull()?.uppercaseChar()?.toString() ?: "B"
                        Text(
                            text = initial,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                }

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
                        Spacer(modifier = Modifier.width(8.dp))
                        BarberAvailabilityBadge(status = barber.status)
                    }
                    Text(
                        text = "Entrada: ${barber.horaEntrada ?: "---"}",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            // Right: Cuts today & Reason info
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${barber.completedCountToday} cortes",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BarberGold
                )

                val infoLabel = if (barber.completedCountToday == 0) {
                    "Llegada reciente"
                } else {
                    "Turno completado"
                }

                Text(
                    text = infoLabel,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}

