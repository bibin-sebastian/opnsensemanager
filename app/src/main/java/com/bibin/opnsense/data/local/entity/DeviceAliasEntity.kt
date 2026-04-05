package com.bibin.opnsense.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_alias")
data class DeviceAliasEntity(
    @PrimaryKey val mac: String,
    val friendlyName: String,
    val groupName: String? = null,
)
