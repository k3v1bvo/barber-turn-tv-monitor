package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import coil.compose.SubcomposeAsyncImage
import coil.compose.AsyncImage
import com.example.data.model.TurnBoardState
import com.example.ui.theme.BarberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldLive
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TvSurface

@Composable
fun MiniCornerPill(
    turnState: TurnBoardState,
    onCycleOverlayMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nextBarber = turnState.queuedBarbers.firstOrNull()
    var isFocused by remember { mutableStateOf(false) }

    androidx.compose.material3.Surface(
        modifier = modifier
            .testTag("mini_corner_pill")
            .clip(RoundedCornerShape(24.dp))
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onCycleOverlayMode() },
        color = if (isFocused) Color(0x80000000) else Color(0x40000000), // Low-alpha semi-transparent Surface
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isFocused) 2.dp else 1.dp,
            color = if (isFocused) Color.White else Color(0x33FFFFFF)
        ),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .animateContentSize(animationSpec = spring(stiffness = 500f))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = nextBarber,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                            scaleIn(initialScale = 0.92f, animationSpec = tween(300)))
                        .togetherWith(
                            fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing)) +
                                    scaleOut(targetScale = 0.96f, animationSpec = tween(200))
                        )
                },
                label = "pill_barber_anim"
            ) { barber ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Barber Avatar or Pole Icon
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF27272A)),
                        contentAlignment = Alignment.Center
                    ) {
                        val photoUrl = barber?.avatarUrl?.ifBlank { null } ?: barber?.selfieUrl
                        if (!photoUrl.isNullOrBlank()) {
                            SubcomposeAsyncImage(
                                model = photoUrl,
                                contentDescription = barber?.fullName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                error = {
                                    Text(text = barber?.fullName?.take(1)?.uppercase() ?: "💈", fontSize = 10.sp, color = BarberGold)
                                }
                            )
                        } else {
                            Text(text = barber?.fullName?.take(1)?.uppercase() ?: "💈", fontSize = 10.sp, color = BarberGold)
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Text info
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (barber != null) {
                            BarberAvailabilityDot(status = barber.status, size = 6.dp)
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldLive)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Turno: ${barber?.fullName ?: "Sin turnos"}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Expand icon hint
            Icon(
                imageVector = Icons.Default.Fullscreen,
                contentDescription = "Cambiar Vista",
                tint = if (isFocused) Color.White else Color(0x99FFFFFF),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
