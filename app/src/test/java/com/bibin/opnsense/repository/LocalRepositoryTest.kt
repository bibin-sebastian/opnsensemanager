package com.bibin.opnsense.repository

import com.bibin.opnsense.data.local.DeviceAliasDao
import com.bibin.opnsense.data.local.entity.DeviceAliasEntity
import com.bibin.opnsense.data.repository.LocalRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class LocalRepositoryTest {

    private val aliasDao = mockk<DeviceAliasDao>()
    private val repository = LocalRepository(aliasDao)

    @Test
    fun `saveFriendlyName calls dao upsert with correct entity`() = runTest {
        coEvery { aliasDao.upsert(any()) } just Runs

        repository.saveFriendlyName("aa:bb:cc", "My Phone")

        coVerify { aliasDao.upsert(DeviceAliasEntity(mac = "aa:bb:cc", friendlyName = "My Phone")) }
    }

    @Test
    fun `getFriendlyName returns null when dao returns null`() = runTest {
        coEvery { aliasDao.getByMac("xx:xx") } returns null

        assertNull(repository.getFriendlyName("xx:xx"))
    }

    @Test
    fun `getFriendlyName returns stored name`() = runTest {
        coEvery { aliasDao.getByMac("aa:bb") } returns DeviceAliasEntity("aa:bb", "Laptop")

        assertEquals("Laptop", repository.getFriendlyName("aa:bb"))
    }
}
