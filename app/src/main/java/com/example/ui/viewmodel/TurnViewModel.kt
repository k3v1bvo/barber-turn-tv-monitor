package com.example.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SettingsManager
import com.example.data.model.BackgroundMediaType
import com.example.data.model.SupabaseSettings
import com.example.data.model.TurnBoardState
import com.example.data.model.TvOverlayMode
import com.example.data.repository.TurnRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TurnViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TurnRepository()
    private val settingsManager = SettingsManager(application.applicationContext)

    private val _turnState = MutableStateFlow(TurnBoardState(isLoading = true))
    val turnState: StateFlow<TurnBoardState> = _turnState.asStateFlow()

    private val _settings = MutableStateFlow(SupabaseSettings())
    val settings: StateFlow<SupabaseSettings> = _settings.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    private var currentRotationOffset = 0
    private var pollingJob: Job? = null
    private var realtimeJob: Job? = null
    private var fetchJob: Job? = null

    init {
        viewModelScope.launch {
            combine(
                settingsManager.settingsFlow,
                settingsManager.rotationOffsetFlow
            ) { prefsSettings, offset ->
                Pair(prefsSettings, offset)
            }.collectLatest { (settings, offset) ->
                _settings.value = settings
                currentRotationOffset = offset
                restartRealtimeAndPolling()
            }
        }
    }

    private fun restartRealtimeAndPolling() {
        pollingJob?.cancel()
        realtimeJob?.cancel()

        // 1. Initial immediate fetch
        fetchState()

        val currentSettings = _settings.value

        // 2. Realtime Subscription for instantaneous updates from Supabase
        if (!currentSettings.isDemoMode && currentSettings.url.isNotBlank() && currentSettings.apiKey.isNotBlank()) {
            realtimeJob = viewModelScope.launch {
                try {
                    repository.subscribeToRealtimeChanges(currentSettings).collect {
                        // Real-time table change detected in Supabase (asistencias, citas, config_turnos, etc.)
                        fetchState()
                    }
                } catch (e: Exception) {
                    // Safe fallback if websocket drops
                }
            }
        }

        // 3. Fallback Periodic Polling Heartbeat
        pollingJob = viewModelScope.launch {
            while (true) {
                val intervalSec = currentSettings.refreshIntervalSec.coerceAtLeast(5)
                delay(intervalSec * 1000L)
                fetchState()
            }
        }
    }

    fun fetchState() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            val currentState = _turnState.value
            val newState = repository.fetchTurnBoardState(_settings.value, currentRotationOffset)

            // If remote offset changed from Web App, align local offset
            if (newState.rotationOffset != currentRotationOffset && newState.isLiveSupabase) {
                currentRotationOffset = newState.rotationOffset
                settingsManager.saveRotationOffset(newState.rotationOffset)
            }

            // Preserve local UI flags like overlayMode and bgMediaType
            _turnState.value = newState.copy(
                overlayMode = currentState.overlayMode,
                bgMediaType = currentState.bgMediaType,
                customMediaUrl = currentState.customMediaUrl,
                isVideoPlaying = currentState.isVideoPlaying
            )
        }
    }

    fun nextTurn() {
        viewModelScope.launch {
            val currentQueue = _turnState.value.queuedBarbers
            if (currentQueue.isNotEmpty()) {
                val newOffset = (currentRotationOffset + 1) % currentQueue.size
                currentRotationOffset = newOffset
                settingsManager.saveRotationOffset(newOffset)

                val activeBarberId = currentQueue.firstOrNull()?.id
                repository.pushRemoteTurnNext(_settings.value, newOffset, activeBarberId)

                fetchState()
            }
        }
    }

    fun previousTurn() {
        viewModelScope.launch {
            val currentQueue = _turnState.value.queuedBarbers
            if (currentQueue.isNotEmpty()) {
                val newOffset = (currentRotationOffset - 1 + currentQueue.size) % currentQueue.size
                currentRotationOffset = newOffset
                settingsManager.saveRotationOffset(newOffset)

                val activeBarberId = currentQueue.firstOrNull()?.id
                repository.pushRemoteTurnNext(_settings.value, newOffset, activeBarberId)

                fetchState()
            }
        }
    }

    fun setOverlayMode(mode: TvOverlayMode) {
        _turnState.update { it.copy(overlayMode = mode) }
    }

    fun setBgMediaType(mediaType: BackgroundMediaType) {
        _turnState.update { it.copy(bgMediaType = mediaType) }
    }

    fun setCustomMediaUrl(url: String) {
        _turnState.update { it.copy(customMediaUrl = url) }
    }

    fun toggleVideoPlaying() {
        _turnState.update { it.copy(isVideoPlaying = !it.isVideoPlaying) }
    }

    fun resetRotation() {
        viewModelScope.launch {
            currentRotationOffset = 0
            settingsManager.resetRotationOffset()
            repository.pushRemoteTurnNext(_settings.value, 0, null)
            fetchState()
        }
    }

    fun saveSettings(url: String, key: String, shopName: String, isDemoMode: Boolean) {
        viewModelScope.launch {
            settingsManager.saveSettings(url, key, shopName, isDemoMode)
            _showSettingsDialog.value = false
            fetchState()
        }
    }

    fun resetSettingsToDefaults() {
        viewModelScope.launch {
            settingsManager.resetToDefaultSettings()
            _showSettingsDialog.value = false
            fetchState()
        }
    }

    fun openSettingsDialog() {
        _showSettingsDialog.value = true
    }

    fun closeSettingsDialog() {
        _showSettingsDialog.value = false
    }
}
