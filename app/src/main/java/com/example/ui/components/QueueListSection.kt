package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    // Show barbers from position #2 onwards (position #1 is already in Hero card)
    val remainingQueue = if (queuedBarbers.size > 1) queuedBarbers.drop(1) else emptyList()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(TvSurface)
            .border(1.dp, TvBorder, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FormatListNumbered,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SIGUIENTES EN LA FILA (${remainingQueue.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }

            if (remainingQueue.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay más barberos esperando en la fila.",
                        fontSize = 15.sp,
                        color = TextMuted
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
    Box(
        modifier = Modifier
            .testTag("queue_item_$position")
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TvSurfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp)
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
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (position == 2) ElectricCyan else Color(0xFF334155)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#$position",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    if (!barber.avatarUrl.isNull_or_blank()) {
                        AsyncImage(
                            model = barber.avatarUrl,
                            contentDescription = barber.fullName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = TextWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BarberAvailabilityDot(
                            status = barber.status,
                            size = 10.dp,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = barber.fullName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BarberAvailabilityBadge(status = barber.status)
                    }
                    Text(
                        text = "Entrada: ${barber.horaEntrada ?: "---"}",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }
            }

            // Right: Cuts today & Reason info
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${barber.completedCountToday} cortes hoy",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BarberGold
                )

                val infoLabel = if (barber.completedCountToday == 0) {
                    "Sin cortes aún (Llegada)"
                } else {
                    "Atendió anteriormente"
                }

                Text(
                    text = infoLabel,
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }
    }
}

private fun String?.isNull_or_blank(): Boolean {
    return this == null || this.isBlank()
}
