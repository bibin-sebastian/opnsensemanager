package com.bibin.opnsense.data.local

import androidx.room.*
import com.bibin.opnsense.data.local.entity.DeviceAliasEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceAliasDao {
    @Query("SELECT * FROM device_alias")
    fun observeAll(): Flow<List<DeviceAliasEntity>>

    @Query("SELECT * FROM device_alias WHERE mac = :mac LIMIT 1")
    suspend fun getByMac(mac: String): DeviceAliasEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DeviceAliasEntity)

    @Query("DELETE FROM device_alias WHERE mac = :mac")
    suspend fun deleteByMac(mac: String)
}
