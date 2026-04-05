package com.bibin.opnsense.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bibin.opnsense.data.local.entity.DeviceAliasEntity
import com.bibin.opnsense.data.local.entity.ScheduleEntity

@Database(
    entities = [DeviceAliasEntity::class, ScheduleEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceAliasDao(): DeviceAliasDao
    abstract fun scheduleDao(): ScheduleDao
}
