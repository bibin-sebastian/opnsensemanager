package com.bibin.opnsense.data.repository

import com.bibin.opnsense.data.local.DeviceAliasDao
import com.bibin.opnsense.data.local.ScheduleDao
import com.bibin.opnsense.data.local.entity.DeviceAliasEntity
import com.bibin.opnsense.data.local.entity.ScheduleEntity
import com.bibin.opnsense.domain.model.BlockSchedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalRepository @Inject constructor(
    private val aliasDao: DeviceAliasDao,
    private val scheduleDao: ScheduleDao,
) {
    suspend fun getFriendlyName(mac: String): String? =
        aliasDao.getByMac(mac)?.friendlyName

    suspend fun saveFriendlyName(mac: String, name: String) {
        aliasDao.upsert(DeviceAliasEntity(mac = mac, friendlyName = name))
    }

    fun observeSchedules(mac: String): Flow<List<BlockSchedule>> =
        scheduleDao.observeByMac(mac).map { entities -> entities.map { it.toDomain() } }

    suspend fun saveSchedule(schedule: BlockSchedule): Long =
        scheduleDao.upsert(schedule.toEntity())

    suspend fun deleteSchedule(id: Long) = scheduleDao.deleteById(id)

    suspend fun getAllSchedules(): List<BlockSchedule> =
        scheduleDao.getAll().map { it.toDomain() }

    private fun BlockSchedule.toEntity() = ScheduleEntity(
        id = id,
        deviceMac = deviceMac,
        startHour = startHour,
        startMinute = startMinute,
        endHour = endHour,
        endMinute = endMinute,
        daysOfWeek = daysOfWeek.joinToString(","),
    )

    private fun ScheduleEntity.toDomain() = BlockSchedule(
        id = id,
        deviceMac = deviceMac,
        startHour = startHour,
        startMinute = startMinute,
        endHour = endHour,
        endMinute = endMinute,
        daysOfWeek = daysOfWeek.split(",").map { it.trim().toInt() }.toSet(),
    )
}
