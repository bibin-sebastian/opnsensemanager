package com.bibin.opnsense.viewmodel

import app.cash.turbine.test
import com.bibin.opnsense.data.repository.OPNsenseRepository
import com.bibin.opnsense.ui.onboarding.OnboardingViewModel
import com.bibin.opnsense.util.CredentialManager
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class OnboardingViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val credentialManager = mockk<CredentialManager>(relaxed = true)
    private val repository = mockk<OPNsenseRepository>(relaxed = true)
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        viewModel = OnboardingViewModel(credentialManager, repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `testConnection success sets connectionSuccess true`() = runTest {
        coEvery { repository.testConnection() } returns true

        viewModel.uiState.test {
            viewModel.testConnection("https://192.168.10.1", "key", "secret")
            val loading = awaitItem()
            assertTrue(loading.isTesting)
            val success = awaitItem()
            assertFalse(success.isTesting)
            assertTrue(success.connectionSuccess)
            cancel()
        }
    }

    @Test
    fun `testConnection failure sets errorMessage`() = runTest {
        coEvery { repository.testConnection() } returns false

        viewModel.uiState.test {
            viewModel.testConnection("https://192.168.10.1", "key", "secret")
            skipItems(1) // loading state
            val error = awaitItem()
            assertFalse(error.isTesting)
            assertNotNull(error.errorMessage)
            assertFalse(error.connectionSuccess)
            cancel()
        }
    }

    @Test
    fun `testConnection failure clears stored credentials`() = runTest {
        coEvery { repository.testConnection() } returns false

        viewModel.testConnection("https://192.168.10.1", "key", "secret")

        coVerify { credentialManager.clear() }
    }
}
