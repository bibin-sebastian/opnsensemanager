package com.bibin.opnsense.data.local

import androidx.room.*
import com.bibin.opnsense.data.local.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule WHERE deviceMac = :mac")
    fun observeByMac(mac: String): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedule")
    suspend fun getAll(): List<ScheduleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ScheduleEntity): Long

    @Query("DELETE FROM schedule WHERE id = :id")
    suspend fun deleteById(id: Long)
}
