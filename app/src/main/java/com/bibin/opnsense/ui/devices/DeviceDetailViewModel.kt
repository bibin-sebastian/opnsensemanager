package com.bibin.opnsense.ui.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bibin.opnsense.data.remote.dto.ConnectionEntry
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

data class DetailUiState(
    val connections: List<ConnectionEntry> = emptyList(),
    val isLoadingConnections: Boolean = false,
    val connectionsError: String? = null,
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
        startConnectionPolling()
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

    private fun startConnectionPolling() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingConnections = true) }
            while (true) {
                runCatching {
                    opnRepo.fetchDeviceConnections(device.ip)
                }.onSuccess { entries ->
                    _uiState.update {
                        it.copy(
                            isLoadingConnections = false,
                            connections = entries,
                            connectionsError = null,
                        )
                    }
                }.onFailure { e ->
                    _uiState.update {
                        it.copy(isLoadingConnections = false, connectionsError = e.message)
                    }
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
        private const val POLL_INTERVAL_MS = 10_000L
    }
}
