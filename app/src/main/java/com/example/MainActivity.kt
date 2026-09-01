package com.example

import android.app.UiModeManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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

    /**
     * Receives broadcast from FloatingTurnBubbleService when the overlay addView fails
     * (permission denied). Re-shows the overlay permission dialog.
     */
    private val overlayFailedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            showTvOverlayDialog = true
        }
    }

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (checkOverlayPermission()) {
            startBubbleServiceSafely()
        } else {
            // Permission still not granted; re-show dialog
            showTvOverlayDialog = true
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

        // Listen for overlay failure from the floating service
        val filter = IntentFilter(FloatingTurnBubbleService.ACTION_OVERLAY_FAILED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(overlayFailedReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(overlayFailedReceiver, filter)
        }

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
                        isAdbEnabled = isAdbDebuggingEnabled(),
                        onSaveTargetIp = { viewModel.saveLastTvIp(it) },
                        onOpenSettings = {
                            showTvOverlayDialog = false
                            hasPendingBubbleStart = true
                            openOverlaySettingsSafely()
                        },
                        onOpenDeveloperSettings = {
                            openDeveloperSettingsSafely()
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

    override fun onDestroy() {
        try {
            unregisterReceiver(overlayFailedReceiver)
        } catch (_: Exception) {}
        super.onDestroy()
    }

    private fun isTvDevice(): Boolean {
        val uiModeManager = getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
        val isTelevision = uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
        val hasLeanback = packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
        val hasNoTouch = !packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)
        return isTelevision || hasLeanback || hasNoTouch
    }

    /**
     * Checks whether ADB (USB debugging) is enabled on this device.
     */
    private fun isAdbDebuggingEnabled(): Boolean {
        return try {
            Settings.Global.getInt(contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        } catch (_: Exception) {
            false
        }
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
                // On some TV ROMs canDrawOverlays throws; assume allowed
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
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo iniciar la burbuja: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            showTvOverlayDialog = true
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

        // Nothing worked, re-show dialog
        showTvOverlayDialog = true
    }

    /**
     * Opens Developer Options on this device (for enabling USB Debugging on the TV).
     */
    private fun openDeveloperSettingsSafely() {
        val devIntents = listOf(
            Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS),
            Intent().setClassName("com.android.tv.settings", "com.android.tv.settings.system.DevelopmentActivity"),
            Intent().setClassName("com.android.tv.settings", "com.android.tv.settings.MainSettings"),
            Intent(Settings.ACTION_SETTINGS)
        )

        for (intent in devIntents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                Toast.makeText(
                    this,
                    "Activa 'Depuración USB' (o 'Depuración de red') y vuelve a la app.",
                    Toast.LENGTH_LONG
                ).show()
                return
            } catch (_: Exception) {}
        }
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
