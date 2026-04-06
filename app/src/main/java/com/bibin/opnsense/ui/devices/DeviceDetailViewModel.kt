package com.bibin.opnsense.ui.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bibin.opnsense.data.repository.LocalRepository
import com.bibin.opnsense.data.repository.OPNsenseRepository
import com.bibin.opnsense.domain.model.Device
import com.bibin.opnsense.domain.model.stableId
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrafficPoint(
    val timestampMs: Long,
    val kbpsIn: Float,
    val kbpsOut: Float,
)

data class DetailUiState(
    val isLoadingTraffic: Boolean = false,
    val trafficHistory: List<TrafficPoint> = emptyList(),
    val totalBytesIn: Long = 0L,
    val totalBytesOut: Long = 0L,
    val trafficError: String? = null,
    val errorMessage: String? = null,
)

@HiltViewModel(assistedFactory = DeviceDetailViewModel.Factory::class)
class DeviceDetailViewModel @AssistedInject constructor(
    @Assisted val device: Device,
    private val localRepo: LocalRepository,
    private val opnRepo: OPNsenseRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        startTrafficPolling()
    }

    fun saveFriendlyName(name: String) {
        viewModelScope.launch {
            runCatching {
                localRepo.saveFriendlyName(device.stableId, name)
            }.onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    /**
     * Polls the pf state table every [POLL_INTERVAL_MS].
     * On each poll, diffs the total bytes against the previous sample to derive kbps.
     * Because pf states don't separate in/out, we show combined rate on the chart
     * and label it as "activity".
     */
    private fun startTrafficPolling() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTraffic = true) }
            var prevSnapshot: OPNsenseRepository.DeviceStateSnapshot? = null
            var prevTimeMs = System.currentTimeMillis()
            while (true) {
                runCatching {
                    opnRepo.fetchDeviceStateBytes(device.ip)
                }.onSuccess { snapshot ->
                    val now = System.currentTimeMillis()
                    val elapsedSec = ((now - prevTimeMs) / 1000.0).coerceAtLeast(1.0)

                    val kbpsIn: Float
                    val kbpsOut: Float
                    if (prevSnapshot != null && snapshot != null) {
                        val diffIn  = (snapshot.bytesIn  - prevSnapshot!!.bytesIn).coerceAtLeast(0L)
                        val diffOut = (snapshot.bytesOut - prevSnapshot!!.bytesOut).coerceAtLeast(0L)
                        kbpsIn  = (diffIn  * 8 / elapsedSec / 1000).toFloat()
                        kbpsOut = (diffOut * 8 / elapsedSec / 1000).toFloat()
                    } else {
                        kbpsIn  = 0f
                        kbpsOut = 0f
                    }

                    prevSnapshot = snapshot
                    prevTimeMs = now

                    val point = TrafficPoint(now, kbpsIn, kbpsOut)
                    _uiState.update { state ->
                        val history = (state.trafficHistory + point).takeLast(MAX_HISTORY)
                        state.copy(
                            isLoadingTraffic = false,
                            trafficHistory = history,
                            totalBytesIn  = snapshot?.bytesIn  ?: state.totalBytesIn,
                            totalBytesOut = snapshot?.bytesOut ?: state.totalBytesOut,
                            trafficError = null,
                        )
                    }
                }.onFailure { e ->
                    _uiState.update { it.copy(isLoadingTraffic = false, trafficError = e.message) }
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(device: Device): DeviceDetailViewModel
    }

    companion object {
        private const val POLL_INTERVAL_MS = 10_000L  // 10 seconds
        private const val MAX_HISTORY = 360            // 360 × 10 s = 1 hour
    }
}
