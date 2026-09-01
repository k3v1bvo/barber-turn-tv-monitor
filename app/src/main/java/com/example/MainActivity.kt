package com.example

import android.app.UiModeManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.viewmodel.TurnViewModel
import com.example.service.FloatingTurnBubbleService
import com.example.ui.components.TvOverlayPermissionDialog
import com.example.ui.screens.MainTvScreen
import com.example.ui.theme.BarberTvTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TurnViewModel by viewModels()
    private var showTvOverlayDialog by mutableStateOf(false)
    private var hasPendingBubbleStart = false

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (checkOverlayPermission()) {
            startBubbleServiceSafely()
        } else {
            startBubbleServiceSafely()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Notification permission handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen awake for barber shop display
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()

        val isTv = isTvDevice()

        setContent {
            val lastTvIp by viewModel.lastTvIp.collectAsStateWithLifecycle()

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
                        initialTargetIp = lastTvIp,
                        isTvDevice = isTv,
                        onSaveTargetIp = { viewModel.saveLastTvIp(it) },
                        onOpenSettings = {
                            showTvOverlayDialog = false
                            hasPendingBubbleStart = true
                            openOverlaySettingsSafely()
                        },
                        onOpenXiaomiSettings = {
                            showTvOverlayDialog = false
                            hasPendingBubbleStart = true
                            openXiaomiPermissionsSafely()
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

    override fun onResume() {
        super.onResume()
        // If returning from system settings after granting permission, auto-launch the bubble
        if (hasPendingBubbleStart && checkOverlayPermission()) {
            hasPendingBubbleStart = false
            startBubbleServiceSafely()
        }
    }

    private fun isTvDevice(): Boolean {
        val uiModeManager = getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
        val isTelevision = uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
        val hasLeanback = packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
        val hasNoTouch = !packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)
        return isTelevision || hasLeanback || hasNoTouch
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
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
     * Safely opens overlay settings for standard Android, Android TV, and Xiaomi.
     */
    private fun openOverlaySettingsSafely() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            startBubbleServiceSafely()
            return
        }

        val intents = listOf(
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")),
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION),
            Intent().setClassName("com.android.tv.settings", "com.android.tv.settings.device.apps.specialaccess.SpecialAppAccessActivity"),
            Intent().setClassName("com.android.tv.settings", "com.android.tv.settings.device.apps.specialaccess.HighPriorityPermissionActivity"),
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")),
            Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
        )

        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                overlayPermissionLauncher.launch(intent)
                Toast.makeText(
                    this,
                    "Activa el interruptor 'Permitir mostrar sobre otras apps' para BarberSite.",
                    Toast.LENGTH_LONG
                ).show()
                return
            } catch (_: Exception) {}
        }

        startBubbleServiceSafely()
    }

    /**
     * Opens Xiaomi / MIUI / HyperOS specific permission editor ("Ventanas emergentes").
     */
    private fun openXiaomiPermissionsSafely() {
        val xiaomiIntents = listOf(
            Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity")
                putExtra("extra_pkgname", packageName)
            },
            Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                putExtra("extra_pkgname", packageName)
            },
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
        )

        for (intent in xiaomiIntents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                overlayPermissionLauncher.launch(intent)
                Toast.makeText(
                    this,
                    "En permisos de Xiaomi: Activa 'Mostrar ventanas emergentes en segundo plano'.",
                    Toast.LENGTH_LONG
                ).show()
                return
            } catch (_: Exception) {}
        }

        openOverlaySettingsSafely()
    }
}
