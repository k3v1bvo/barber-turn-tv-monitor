package com.example

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.data.viewmodel.TurnViewModel
import com.example.service.FloatingTurnBubbleService
import com.example.ui.components.TvOverlayPermissionDialog
import com.example.ui.screens.MainTvScreen
import com.example.ui.theme.BarberTvTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TurnViewModel by viewModels()
    private var showTvOverlayDialog by mutableStateOf(false)

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (checkOverlayPermission()) {
            startBubbleServiceSafely()
        } else {
            // On TV Boxes (Xiaomi Mi Box / Google TV), canDrawOverlays might still report false
            // even after manual grant or without UI. We attempt a safe start.
            startBubbleServiceSafely()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Prevent screen timeout / sleep on Xiaomi TV Box
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        enableEdgeToEdge()

        setContent {
            BarberTvTheme {
                MainTvScreen(
                    viewModel = viewModel,
                    isFloatingBubbleRunning = FloatingTurnBubbleService.isRunning,
                    onToggleFloatingBubble = {
                        toggleFloatingBubble()
                    }
                )

                if (showTvOverlayDialog) {
                    TvOverlayPermissionDialog(
                        onOpenSettings = {
                            showTvOverlayDialog = false
                            openOverlaySettingsSafely()
                        },
                        onTryDirectStart = {
                            showTvOverlayDialog = false
                            startBubbleServiceSafely()
                        },
                        onDismiss = {
                            showTvOverlayDialog = false
                        }
                    )
                }
            }
        }
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                Settings.canDrawOverlays(this)
            } catch (e: Exception) {
                true
            }
        } else {
            true
        }
    }

    private fun toggleFloatingBubble() {
        if (FloatingTurnBubbleService.isRunning) {
            FloatingTurnBubbleService.stop(this)
            Toast.makeText(this, "Burbuja flotante desactivada", Toast.LENGTH_SHORT).show()
        } else {
            if (checkOverlayPermission()) {
                startBubbleServiceSafely()
            } else {
                showTvOverlayDialog = true
            }
        }
    }

    private fun startBubbleServiceSafely() {
        try {
            FloatingTurnBubbleService.start(this)
            Toast.makeText(this, "💈 Burbuja Flotante activada sobre todas las apps", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo iniciar la burbuja: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Safely attempts to open overlay permission settings.
     * Prevents ActivityNotFoundException crashes on Android TV / Xiaomi Mi Box ROMs
     * which lack standard mobile overlay settings activities.
     */
    private fun openOverlaySettingsSafely() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            startBubbleServiceSafely()
            return
        }

        // Tier 1: Direct package overlay settings (Phones & Standard Android)
        try {
            val intentPackage = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intentPackage)
            return
        } catch (_: Exception) {}

        // Tier 2: Generic overlay settings list
        try {
            val intentGeneric = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            overlayPermissionLauncher.launch(intentGeneric)
            return
        } catch (_: Exception) {}

        // Tier 3: App Details Settings (Android TV / Xiaomi Mi Box Leanback)
        try {
            val intentAppDetails = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:$packageName")
            )
            startActivity(intentAppDetails)
            Toast.makeText(
                this,
                "En la TV: Entra a Permisos > 'Mostrar sobre otras apps' y actívalo para BarberSite.",
                Toast.LENGTH_LONG
            ).show()
            return
        } catch (_: Exception) {}

        // Tier 4: Direct start if no settings screen is available in this TV ROM
        startBubbleServiceSafely()
    }
}
