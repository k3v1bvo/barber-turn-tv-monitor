package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.viewmodel.TurnViewModel
import com.example.ui.screens.MainTvScreen
import com.example.ui.theme.BarberTvTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TurnViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Prevent screen timeout / sleep on Xiaomi TV Box
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        enableEdgeToEdge()

        setContent {
            BarberTvTheme {
                MainTvScreen(viewModel = viewModel)
            }
        }
    }
}
