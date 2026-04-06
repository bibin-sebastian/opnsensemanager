package com.bibin.opnsense.data.repository

import com.bibin.opnsense.data.local.DeviceAliasDao
import com.bibin.opnsense.data.local.entity.DeviceAliasEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalRepository @Inject constructor(
    private val aliasDao: DeviceAliasDao,
) {
    suspend fun getFriendlyName(stableId: String): String? =
        aliasDao.getByMac(stableId)?.friendlyName

    suspend fun saveFriendlyName(stableId: String, name: String) {
        aliasDao.upsert(DeviceAliasEntity(mac = stableId, friendlyName = name))
    }
}
