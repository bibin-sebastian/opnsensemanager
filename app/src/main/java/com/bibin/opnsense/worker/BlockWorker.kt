package com.bibin.opnsense.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bibin.opnsense.data.repository.OPNsenseRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class BlockWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val opnRepo: OPNsenseRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val mac = inputData.getString(KEY_DEVICE_MAC) ?: return Result.failure()
        val action = inputData.getString(KEY_ACTION) ?: return Result.failure()

        // Find the current IP for this MAC from DHCP leases
        val device = opnRepo.fetchDevices().firstOrNull { it.mac == mac }
            ?: return Result.failure()

        return runCatching {
            when (action) {
                ACTION_BLOCK -> opnRepo.blockDevice(device.ip)
                ACTION_UNBLOCK -> opnRepo.unblockDevice(device.ip)
                else -> return Result.failure()
            }
            Result.success()
        }.getOrElse { Result.retry() }
    }

    companion object {
        const val KEY_DEVICE_MAC = "device_mac"
        const val KEY_ACTION = "action"
        const val ACTION_BLOCK = "block"
        const val ACTION_UNBLOCK = "unblock"
    }
}
