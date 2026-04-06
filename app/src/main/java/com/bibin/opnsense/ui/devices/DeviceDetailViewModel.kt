package com.bibin.opnsense.ui.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bibin.opnsense.data.remote.dto.ConnectionEntry
import com.bibin.opnsense.data.repository.LocalRepository
import com.bibin.opnsense.data.repository.OPNsenseRepository
import com.bibin.opnsense.domain.model.Device
import com.bibin.opnsense.domain.model.displayName
import com.bibin.opnsense.domain.model.stableId
import com.bibin.opnsense.util.DnsResolver
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
    private val dnsResolver: DnsResolver,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState = _uiState.asStateFlow()

    /** ip → display name for all known LAN devices. Built once on init. */
    private val localNames = mutableMapOf<String, String>()

    /** connectionKey → epoch millis when we first observed this connection. */
    private val firstSeenMap = mutableMapOf<String, Long>()

    init {
        viewModelScope.launch { buildLocalNameMap() }
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

    private suspend fun buildLocalNameMap() {
        runCatching { opnRepo.fetchDevices() }.onSuccess { devices ->
            devices.forEach { d ->
                val name = localRepo.getFriendlyName(d.stableId)?.takeIf { it.isNotBlank() }
                    ?: d.displayName
                localNames[d.ip] = name
            }
        }
    }

    /** Returns a friendly label for [ip]: local device name → reverse DNS → null. */
    private suspend fun resolveName(ip: String): String? =
        localNames[ip] ?: dnsResolver.resolve(ip)

    private fun startConnectionPolling() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingConnections = true) }
            while (true) {
                runCatching {
                    val entries = opnRepo.fetchDeviceConnections(device.ip)
                    // Resolve unique IPs concurrently (cache makes repeated calls cheap)
                    val uniqueIps = (entries.map { it.srcAddr } + entries.map { it.dstAddr }).toSet()
                    val nameMap = uniqueIps.associateWith { resolveName(it) }
                    val now = System.currentTimeMillis()
                    entries.map { e ->
                        val seen = firstSeenMap.getOrPut(e.key) { now }
                        e.copy(
                            srcName = nameMap[e.srcAddr],
                            dstName = nameMap[e.dstAddr],
                            firstSeenMs = seen,
                        )
                    }
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
