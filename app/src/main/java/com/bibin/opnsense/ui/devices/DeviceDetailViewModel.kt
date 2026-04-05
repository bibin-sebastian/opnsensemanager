package com.bibin.opnsense.ui.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bibin.opnsense.data.repository.LocalRepository
import com.bibin.opnsense.domain.model.BlockSchedule
import com.bibin.opnsense.domain.model.Device
import com.bibin.opnsense.worker.ScheduleManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DeviceDetailViewModel.Factory::class)
class DeviceDetailViewModel @AssistedInject constructor(
    @Assisted val device: Device,
    private val localRepo: LocalRepository,
    private val scheduleManager: ScheduleManager,
) : ViewModel() {

    val schedules = localRepo.observeSchedules(device.mac)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveFriendlyName(name: String) {
        viewModelScope.launch {
            localRepo.saveFriendlyName(device.mac, name)
        }
    }

    fun saveSchedule(schedule: BlockSchedule) {
        viewModelScope.launch {
            scheduleManager.schedule(schedule)
        }
    }

    fun deleteSchedule(id: Long) {
        viewModelScope.launch {
            scheduleManager.cancel(id)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(device: Device): DeviceDetailViewModel
    }
}
