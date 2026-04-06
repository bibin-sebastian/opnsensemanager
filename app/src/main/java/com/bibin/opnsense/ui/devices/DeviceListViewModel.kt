package com.bibin.opnsense.ui.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bibin.opnsense.data.repository.LocalRepository
import com.bibin.opnsense.data.repository.OPNsenseRepository
import com.bibin.opnsense.domain.model.Device
import com.bibin.opnsense.domain.model.displayName
import com.bibin.opnsense.domain.model.stableId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
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

    private val _rawState = MutableStateFlow(DeviceListUiState())
    val searchQuery = MutableStateFlow("")

    /** Filtered view — what the UI observes. */
    val uiState = combine(_rawState, searchQuery) { state, query ->
        if (query.isBlank()) state
        else state.copy(devices = state.devices.filter { it.matches(query) })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DeviceListUiState())

    init { loadDevices() }

    fun loadDevices() {
        viewModelScope.launch {
            _rawState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val devices = opnRepo.fetchDevices().map { device ->
                    device.copy(friendlyName = localRepo.getFriendlyName(device.stableId))
                }
                _rawState.update { it.copy(isLoading = false, devices = devices) }
            }.onFailure { e ->
                _rawState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun toggleBlock(device: Device) {
        viewModelScope.launch {
            runCatching {
                if (device.isBlocked) opnRepo.unblockDevice(device.ip)
                else opnRepo.blockDevice(device.ip)
                loadDevices()
            }.onFailure { e ->
                _rawState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    private fun Device.matches(query: String): Boolean {
        val q = query.trim().lowercase()
        return displayName.lowercase().contains(q)
            || ip.contains(q)
            || mac.lowercase().contains(q)
            || hostname.lowercase().contains(q)
    }
}
