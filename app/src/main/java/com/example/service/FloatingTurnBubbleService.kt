package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.data.local.SettingsManager
import com.example.data.model.SupabaseSettings
import com.example.data.model.TurnBoardState
import com.example.data.repository.TurnRepository
import com.example.ui.components.FloatingBubbleUi
import com.example.ui.theme.BarberTvTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * FloatingTurnBubbleService: Foreground Service that injects a floating bubble
 * directly into the Android WindowManager.
 * Stays visible on top of YouTube, Netflix, TV Box Launcher, or any app on TV & Mobile.
 */
class FloatingTurnBubbleService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private var windowManager: WindowManager? = null
    private var floatingView: ComposeView? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private val repository = TurnRepository()
    private lateinit var settingsManager: SettingsManager

    private val _turnState = MutableStateFlow(TurnBoardState(isLoading = true))
    val turnState = _turnState.asStateFlow()

    private var currentSettings = SupabaseSettings()
    private var currentRotationOffset = 0
    private var realtimeJob: Job? = null
    private var pollingJob: Job? = null

    companion object {
        private const val TAG = "FloatingTurnBubble"
        const val CHANNEL_ID = "barber_floating_turn_channel"
        const val NOTIFICATION_ID = 9021
        const val ACTION_STOP = "com.example.service.ACTION_STOP_FLOATING"
        const val ACTION_OVERLAY_FAILED = "com.example.service.ACTION_OVERLAY_FAILED"

        var isRunning = false
            private set

        fun start(context: Context) {
            val intent = Intent(context, FloatingTurnBubbleService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingTurnBubbleService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true

        try {
            savedStateRegistryController.performAttach()
            savedStateRegistryController.performRestore(null)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing lifecycle", e)
        }

        settingsManager = SettingsManager(applicationContext)

        startForegroundNotification()
        initWindowManagerView()
        observeSettingsAndRealtime()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundNotification() {
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Turnos Barbería Flotante",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Muestra la burbuja flotante de turnos sobre otras apps"
                    setShowBadge(false)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val openAppIntent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("💈 BarberSite TV - Burbuja Flotante")
                .setContentText("Turno actual visible sobre todas las apps")
                .setSmallIcon(android.R.drawable.ic_menu_agenda)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()

            startForeground(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting foreground notification", e)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createComposeView(params: WindowManager.LayoutParams): ComposeView {
        val composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setViewTreeLifecycleOwner(this@FloatingTurnBubbleService)
            setViewTreeViewModelStoreOwner(this@FloatingTurnBubbleService)
            setViewTreeSavedStateRegistryOwner(this@FloatingTurnBubbleService)

            setContent {
                BarberTvTheme {
                    val state by turnState.collectAsState()
                    FloatingBubbleUi(
                        turnState = state,
                        onNextTurn = { onNextTurnClicked() },
                        onOpenApp = { openMainActivity() },
                        onCloseService = { stopSelf() }
                    )
                }
            }
        }

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isMoving = false
        val touchSlop = 12

        composeView.setOnTouchListener { _, event ->
            val currentParams = layoutParams ?: return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = currentParams.x
                    initialY = currentParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isMoving = false
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - initialTouchX).toInt()
                    val deltaY = (event.rawY - initialTouchY).toInt()

                    if (abs(deltaX) > touchSlop || abs(deltaY) > touchSlop) {
                        isMoving = true
                        currentParams.x = initialX + deltaX
                        currentParams.y = initialY + deltaY
                        try {
                            windowManager?.updateViewLayout(composeView, currentParams)
                        } catch (_: Exception) {}
                        true
                    } else {
                        false
                    }
                }
                MotionEvent.ACTION_UP -> {
                    isMoving
                }
                else -> false
            }
        }

        return composeView
    }

    private fun initWindowManagerView() {
        if (floatingView != null) {
            removeFloatingView()
        }

        try {
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

            val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                overlayType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 40
                y = 140
            }

            val composeView = createComposeView(params)
            windowManager?.addView(composeView, params)

            layoutParams = params
            floatingView = composeView

            android.os.Handler(android.os.Looper.getMainLooper()).post {
                Toast.makeText(applicationContext, "💈 Burbuja Flotante activada sobre todas las apps", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding floating overlay view", e)
            removeFloatingView()

            android.os.Handler(android.os.Looper.getMainLooper()).post {
                Toast.makeText(
                    applicationContext,
                    "No se pudo mostrar la burbuja: ${e.localizedMessage ?: "Verifica permisos en Ajustes"}",
                    Toast.LENGTH_LONG
                ).show()
            }

            val failIntent = Intent(ACTION_OVERLAY_FAILED).apply {
                setPackage(applicationContext.packageName)
            }
            sendBroadcast(failIntent)
            stopSelf()
        }
    }

    private fun removeFloatingView() {
        floatingView?.let { view ->
            try {
                if (view.isAttachedToWindow) {
                    windowManager?.removeViewImmediate(view)
                } else {
                    windowManager?.removeView(view)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error removing floating view", e)
            }
        }
        floatingView = null
    }

    private fun observeSettingsAndRealtime() {
        serviceScope.launch {
            combine(
                settingsManager.settingsFlow,
                settingsManager.rotationOffsetFlow
            ) { settings, offset ->
                Pair(settings, offset)
            }.collectLatest { (settings, offset) ->
                currentSettings = settings
                currentRotationOffset = offset

                realtimeJob?.cancel()
                pollingJob?.cancel()

                fetchState()
                setupRealtimeOrPolling()
            }
        }
    }

    private fun setupRealtimeOrPolling() {
        realtimeJob?.cancel()
        pollingJob?.cancel()

        if (!currentSettings.isDemoMode && currentSettings.url.isNotBlank() && currentSettings.apiKey.isNotBlank()) {
            realtimeJob = serviceScope.launch {
                try {
                    repository.subscribeToRealtimeChanges(currentSettings).collect {
                        fetchState()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Realtime subscription error in service", e)
                }
            }
        }

        pollingJob = serviceScope.launch {
            while (true) {
                delay(6000L)
                fetchState()
            }
        }
    }

    private fun fetchState() {
        serviceScope.launch {
            try {
                val newState = repository.fetchTurnBoardState(currentSettings, currentRotationOffset)
                if (newState.rotationOffset != currentRotationOffset && newState.isLiveSupabase) {
                    currentRotationOffset = newState.rotationOffset
                    settingsManager.saveRotationOffset(newState.rotationOffset)
                }
                _turnState.value = newState
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching state in service", e)
            }
        }
    }

    private fun onNextTurnClicked() {
        serviceScope.launch {
            val queue = _turnState.value.queuedBarbers
            if (queue.isNotEmpty()) {
                val newOffset = (currentRotationOffset + 1) % queue.size
                currentRotationOffset = newOffset
                settingsManager.saveRotationOffset(newOffset)

                val activeBarberId = queue.firstOrNull()?.id
                repository.pushRemoteTurnNext(currentSettings, newOffset, activeBarberId)
                fetchState()
            }
        }
    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        isRunning = false
        try {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            store.clear()
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying lifecycle in service", e)
        }

        realtimeJob?.cancel()
        pollingJob?.cancel()
        serviceScope.cancel()

        removeFloatingView()
        super.onDestroy()
    }
}
