package com.bibin.opnsense.ui.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bibin.opnsense.data.repository.LocalRepository
import com.bibin.opnsense.data.repository.OPNsenseRepository
import com.bibin.opnsense.domain.model.Device
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeviceListUiState(
    val devices: List<Device> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class DeviceListViewModel @Inject constructor(
    private val opnRepo: OPNsenseRepository,
    private val localRepo: LocalRepository,
) : ViewModel() {

    val uiState = MutableStateFlow(DeviceListUiState())

    init {
        loadDevices()
    }

    fun loadDevices() {
        viewModelScope.launch {
            uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val devices = opnRepo.fetchDevices().map { device ->
                    device.copy(friendlyName = localRepo.getFriendlyName(device.mac))
                }
                uiState.update { it.copy(isLoading = false, devices = devices) }
            }.onFailure { e ->
                uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun toggleBlock(device: Device) {
        viewModelScope.launch {
            runCatching {
                if (device.isBlocked) opnRepo.unblockDevice(device.ip)
                else opnRepo.blockDevice(device.ip)
                loadDevices()
            }.onFailure { e ->
                uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }
}
