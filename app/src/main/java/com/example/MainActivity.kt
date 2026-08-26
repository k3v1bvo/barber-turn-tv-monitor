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
import com.example.data.viewmodel.TurnViewModel
import com.example.service.FloatingTurnBubbleService
import com.example.ui.screens.MainTvScreen
import com.example.ui.theme.BarberTvTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TurnViewModel by viewModels()

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (checkOverlayPermission()) {
            FloatingTurnBubbleService.start(this)
            Toast.makeText(this, "💈 Burbuja Flotante activada sobre todas las apps", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Se requiere permiso para mostrar la burbuja sobre otras apps", Toast.LENGTH_SHORT).show()
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
            }
        }
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
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
                FloatingTurnBubbleService.start(this)
                Toast.makeText(this, "💈 Burbuja Flotante activada sobre todas las apps", Toast.LENGTH_LONG).show()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    overlayPermissionLauncher.launch(intent)
                }
            }
        }
    }
}

