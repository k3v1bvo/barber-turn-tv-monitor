package com.example.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.viewmodel.TurnViewModel
import com.example.ui.components.ActiveCutsSection
import com.example.ui.components.BottomTicker
import com.example.ui.components.HeroNextTurnCard
import com.example.ui.components.QueueListSection
import com.example.ui.components.SupabaseSettingsDialog
import com.example.ui.theme.BarberGold
import com.example.ui.theme.BarberGoldVariant
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldLive
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TvBackground
import com.example.ui.theme.TvSurface
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainTvScreen(
    viewModel: TurnViewModel,
    isFloatingBubbleRunning: Boolean = false,
    onToggleFloatingBubble: () -> Unit = {}
) {
    val turnState by viewModel.turnState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsStateWithLifecycle()

    val nextBarberInTurn = turnState.queuedBarbers.firstOrNull()

    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = TvBackground
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isNarrowScreen = maxWidth < 650.dp || isPortrait

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TvBackground)
            ) {
                // =========================================================================
                // 1. TOP HEADER BAR (RESPONSIVE & OPTIMIZED)
                // =========================================================================
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = TvSurface,
                    border = BorderStroke(1.dp, Color(0x332E3D60))
                ) {
                    if (isNarrowScreen) {
                        // Portrait Header: 2 compact rows
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(BarberGold),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "💈", fontSize = 16.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = turnState.shopName.ifBlank { "BarberSite" },
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            LivePulseDot(size = 6.dp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "EN VIVO",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldLive
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Button(
                                        onClick = { viewModel.fetchState() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0x3338BDF8),
                                            contentColor = ElectricCyan
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Refrescar",
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Button(
                                        onClick = { viewModel.openSettingsDialog() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0x33FBBF24),
                                            contentColor = BarberGold
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Ajustes",
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Landscape Header: 1 wide row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(BarberGold),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "💈", fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = turnState.shopName.ifBlank { "BarberSite - Control de Turnos" },
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        LivePulseDot(size = 8.dp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "EN VIVO • SUPABASE REALTIME",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldLive
                                        )
                                        if (turnState.lastRefreshTime.isNotEmpty()) {
                                            Text(
                                                text = " (${turnState.lastRefreshTime})",
                                                fontSize = 11.sp,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Isolated Clock Composable for 0% root recomposition
                                TvHeaderClock(modifier = Modifier.padding(end = 16.dp))

                                Button(
                                    onClick = { viewModel.fetchState() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0x3338BDF8),
                                        contentColor = ElectricCyan
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refrescar",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Refrescar", fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = { viewModel.openSettingsDialog() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0x33FBBF24),
                                        contentColor = BarberGold
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Ajustes",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ajustes", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Error Notice Banner if any
                if (turnState.errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CrimsonAlert)
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = TextWhite
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = turnState.errorMessage ?: "",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            }
                            Button(
                                onClick = { viewModel.openSettingsDialog() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black,
                                    contentColor = TextWhite
                                )
                            ) {
                                Text("Configurar", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // =========================================================================
                // 2. HERO BANNER: FLOATING BUBBLE OVERLAY ACTION (RESPONSIVE)
                // =========================================================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (isNarrowScreen) 12.dp else 20.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                if (isFloatingBubbleRunning)
                                    listOf(Color(0xFF064E3B), Color(0xFF0F766E))
                                else
                                    listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                            )
                        )
                        .border(
                            1.5.dp,
                            if (isFloatingBubbleRunning) EmeraldLive else BarberGold.copy(alpha = 0.5f),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    if (isNarrowScreen) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🫧", fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "BURBUJA FLOTANTE",
                                        color = if (isFloatingBubbleRunning) EmeraldLive else BarberGold,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = if (isFloatingBubbleRunning)
                                            "🟢 Activo: Flotando sobre YouTube/Netflix"
                                        else
                                            "Muestra los turnos encima de otras apps",
                                        color = TextWhite.copy(alpha = 0.85f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onToggleFloatingBubble,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFloatingBubbleRunning) CrimsonAlert else BarberGold,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFloatingBubbleRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isFloatingBubbleRunning) "DESACTIVAR BURBUJA" else "ACTIVAR BURBUJA FLOTANTE",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "🫧", fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "MODO BURBUJA FLOTANTE (SOBRE OTRAS APPS)",
                                        color = if (isFloatingBubbleRunning) EmeraldLive else BarberGold,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = if (isFloatingBubbleRunning)
                                            "🟢 Activo: Los turnos se muestran flotando encima de YouTube, Netflix, o el menú de la TV"
                                        else
                                            "Muestra una burbuja flotante como Messenger encima de cualquier app mientras usas la TV o celular con normalidad.",
                                        color = TextWhite.copy(alpha = 0.9f),
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Button(
                                onClick = onToggleFloatingBubble,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFloatingBubbleRunning) CrimsonAlert else BarberGold,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(44.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFloatingBubbleRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isFloatingBubbleRunning) "DESACTIVAR BURBUJA" else "ACTIVAR BURBUJA FLOTANTE",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                // =========================================================================
                // 3. MAIN TURN BOARD BODY (ADAPTIVE: SCROLLABLE ON PORTRAIT / GRID ON LANDSCAPE)
                // =========================================================================
                if (isNarrowScreen) {
                    // MÓVIL VERTICAL: Scrollable Column with vertical sections
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(scrollState)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HeroNextTurnCard(
                            barber = nextBarberInTurn,
                            onNextTurn = { viewModel.nextTurn() },
                            modifier = Modifier.fillMaxWidth()
                        )

                        QueueListSection(
                            queuedBarbers = turnState.queuedBarbers,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 280.dp, max = 400.dp)
                        )

                        ActiveCutsSection(
                            activeBarbers = turnState.activeBarbers,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp, max = 340.dp)
                        )
                    }
                } else {
                    // TV BOX / HORIZONTAL: Side-by-Side Grid
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                    ) {
                        HeroNextTurnCard(
                            barber = nextBarberInTurn,
                            onNextTurn = { viewModel.nextTurn() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            QueueListSection(
                                queuedBarbers = turnState.queuedBarbers,
                                modifier = Modifier
                                    .weight(1.3f)
                                    .fillMaxSize()
                            )

                            ActiveCutsSection(
                                activeBarbers = turnState.activeBarbers,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                            )
                        }
                    }
                }

                // =========================================================================
                // 4. BOTTOM TICKER
                // =========================================================================
                BottomTicker(
                    totalCutsToday = turnState.totalCutsToday,
                    barbersPresentCount = turnState.barbersPresentCount,
                    isLiveSupabase = turnState.isLiveSupabase
                )
            }

            // Supabase Settings Modal Dialog
            if (showSettingsDialog) {
                SupabaseSettingsDialog(
                    currentSettings = settings,
                    onSave = { url, key, shopName, isDemo ->
                        viewModel.saveSettings(url, key, shopName, isDemo)
                    },
                    onResetToDefaults = {
                        viewModel.resetSettingsToDefaults()
                    },
                    onDismiss = { viewModel.closeSettingsDialog() }
                )
            }
        }
    }
}

/**
 * Isolated Digital Clock Composable:
 * Confines 1-second recomposition strictly to this text view,
 * avoiding recomposition of the main TV board components.
 */
@Composable
fun TvHeaderClock(modifier: Modifier = Modifier) {
    var currentTimeStr by remember { mutableStateOf("") }
    var currentDateStr by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, d 'de' MMMM", Locale.forLanguageTag("es-ES"))
        while (true) {
            val now = Date()
            currentTimeStr = timeFormat.format(now)
            currentDateStr = dateFormat.format(now).replaceFirstChar { it.uppercase() }
            delay(1000L)
        }
    }

    Column(
        horizontalAlignment = Alignment.End,
        modifier = modifier
    ) {
        Text(
            text = currentTimeStr,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BarberGold
        )
        Text(
            text = currentDateStr,
            fontSize = 11.sp,
            color = TextMuted
        )
    }
}

/**
 * Isolated Live Pulse Dot:
 * Runs pulse animation directly on GPU layer without triggering Jetpack Compose layout passes.
 */
@Composable
fun LivePulseDot(size: Dp = 8.dp) {
    val infiniteTransition = rememberInfiniteTransition(label = "livePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .graphicsLayer { alpha = pulseAlpha }
            .background(EmeraldLive)
    )
}
