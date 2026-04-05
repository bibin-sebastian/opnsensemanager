package com.bibin.opnsense.worker

import android.content.Context
import androidx.work.*
import com.bibin.opnsense.data.repository.LocalRepository
import com.bibin.opnsense.domain.model.BlockSchedule
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localRepo: LocalRepository,
) {
    suspend fun schedule(blockSchedule: BlockSchedule) {
        val id = localRepo.saveSchedule(blockSchedule)
        enqueueBlockWork(blockSchedule.copy(id = id))
    }

    suspend fun cancel(scheduleId: Long) {
        localRepo.deleteSchedule(scheduleId)
        WorkManager.getInstance(context).cancelUniqueWork("block_$scheduleId")
        WorkManager.getInstance(context).cancelUniqueWork("unblock_$scheduleId")
    }

    private fun enqueueBlockWork(schedule: BlockSchedule) {
        val blockDelay = minutesUntil(schedule.startHour, schedule.startMinute)
        val unblockDelay = minutesUntil(schedule.endHour, schedule.endMinute)

        val blockData = workDataOf(
            BlockWorker.KEY_DEVICE_MAC to schedule.deviceMac,
            BlockWorker.KEY_ACTION to BlockWorker.ACTION_BLOCK,
        )
        val unblockData = workDataOf(
            BlockWorker.KEY_DEVICE_MAC to schedule.deviceMac,
            BlockWorker.KEY_ACTION to BlockWorker.ACTION_UNBLOCK,
        )

        WorkManager.getInstance(context).enqueueUniqueWork(
            "block_${schedule.id}",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<BlockWorker>()
                .setInitialDelay(blockDelay, TimeUnit.MINUTES)
                .setInputData(blockData)
                .build(),
        )
        WorkManager.getInstance(context).enqueueUniqueWork(
            "unblock_${schedule.id}",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<BlockWorker>()
                .setInitialDelay(unblockDelay, TimeUnit.MINUTES)
                .setInputData(unblockData)
                .build(),
        )
    }

    private fun minutesUntil(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.before(now)) target.add(Calendar.DAY_OF_YEAR, 1)
        return (target.timeInMillis - now.timeInMillis) / 60_000
    }
}
