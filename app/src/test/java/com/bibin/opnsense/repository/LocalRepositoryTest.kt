package com.bibin.opnsense.repository

import com.bibin.opnsense.data.local.DeviceAliasDao
import com.bibin.opnsense.data.local.ScheduleDao
import com.bibin.opnsense.data.local.entity.DeviceAliasEntity
import com.bibin.opnsense.data.repository.LocalRepository
import com.bibin.opnsense.domain.model.BlockSchedule
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class LocalRepositoryTest {

    private val aliasDao = mockk<DeviceAliasDao>()
    private val scheduleDao = mockk<ScheduleDao>()
    private val repository = LocalRepository(aliasDao, scheduleDao)

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

    @Test
    fun `saveSchedule encodes daysOfWeek as comma-separated string`() = runTest {
        coEvery { scheduleDao.upsert(any()) } returns 1L

        val schedule = BlockSchedule(
            deviceMac = "aa:bb:cc",
            startHour = 22, startMinute = 0,
            endHour = 7, endMinute = 0,
            daysOfWeek = setOf(Calendar.MONDAY, Calendar.TUESDAY),
        )
        val id = repository.saveSchedule(schedule)

        assertEquals(1L, id)
        coVerify {
            scheduleDao.upsert(
                match { entity ->
                    val days = entity.daysOfWeek.split(",").map { it.trim().toInt() }.toSet()
                    days == setOf(Calendar.MONDAY, Calendar.TUESDAY)
                }
            )
        }
    }
}
