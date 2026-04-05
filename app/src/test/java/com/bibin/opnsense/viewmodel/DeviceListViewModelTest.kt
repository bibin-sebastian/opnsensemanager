package com.bibin.opnsense.viewmodel

import app.cash.turbine.test
import com.bibin.opnsense.data.repository.LocalRepository
import com.bibin.opnsense.data.repository.OPNsenseRepository
import com.bibin.opnsense.domain.model.Device
import com.bibin.opnsense.ui.devices.DeviceListViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DeviceListViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val opnRepo = mockk<OPNsenseRepository>()
    private val localRepo = mockk<LocalRepository>()
    private lateinit var viewModel: DeviceListViewModel

    private val fakeDevice = Device(
        mac = "aa:bb:cc:dd:ee:ff",
        ip = "192.168.10.10",
        hostname = "desktop",
        friendlyName = null,
        isOnline = true,
        isBlocked = false,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadDevices merges friendly names from local repo`() = runTest {
        coEvery { opnRepo.fetchDevices() } returns listOf(fakeDevice)
        coEvery { localRepo.getFriendlyName("aa:bb:cc:dd:ee:ff") } returns "My Desktop"

        viewModel = DeviceListViewModel(opnRepo, localRepo)

        viewModel.uiState.test {
            // Skip initial loading state (init calls loadDevices)
            val state = awaitItem()
            if (state.isLoading) {
                val loaded = awaitItem()
                assertEquals("My Desktop", loaded.devices.first().friendlyName)
            } else {
                assertEquals("My Desktop", state.devices.first().friendlyName)
            }
            cancel()
        }
    }

    @Test
    fun `toggleBlock calls blockDevice when device is not blocked`() = runTest {
        coEvery { opnRepo.fetchDevices() } returns listOf(fakeDevice)
        coEvery { localRepo.getFriendlyName(any()) } returns null
        coEvery { opnRepo.blockDevice(any()) } just Runs

        viewModel = DeviceListViewModel(opnRepo, localRepo)
        viewModel.toggleBlock(fakeDevice)

        coVerify { opnRepo.blockDevice("192.168.10.10") }
    }

    @Test
    fun `toggleBlock calls unblockDevice when device is blocked`() = runTest {
        val blockedDevice = fakeDevice.copy(isBlocked = true)
        coEvery { opnRepo.fetchDevices() } returns listOf(blockedDevice)
        coEvery { localRepo.getFriendlyName(any()) } returns null
        coEvery { opnRepo.unblockDevice(any()) } just Runs

        viewModel = DeviceListViewModel(opnRepo, localRepo)
        viewModel.toggleBlock(blockedDevice)

        coVerify { opnRepo.unblockDevice("192.168.10.10") }
    }
}
