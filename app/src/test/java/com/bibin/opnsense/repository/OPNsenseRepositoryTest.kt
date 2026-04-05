package com.bibin.opnsense.repository

import com.bibin.opnsense.data.remote.OPNsenseApi
import com.bibin.opnsense.data.remote.dto.*
import com.bibin.opnsense.data.repository.BLOCK_ALIAS_NAME
import com.bibin.opnsense.data.repository.OPNsenseRepository
import com.bibin.opnsense.util.CredentialManager
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class OPNsenseRepositoryTest {

    private val api = mockk<OPNsenseApi>()
    private val credentialManager = mockk<CredentialManager>()
    private lateinit var repository: OPNsenseRepository

    @Before
    fun setup() {
        every { credentialManager.firewallUrl } returns "https://192.168.10.1"
        every { credentialManager.apiKey } returns "testkey"
        every { credentialManager.apiSecret } returns "testsecret"
        repository = OPNsenseRepository(api, credentialManager)
    }

    @Test
    fun `fetchDevices returns mapped Device list`() = runTest {
        coEvery { api.getDhcpLeases() } returns DhcpLeaseResponse(
            total = 1,
            rows = listOf(DhcpLeaseRow("192.168.10.10", "aa:bb:cc:dd:ee:ff", "desktop", "active"))
        )
        coEvery { api.searchAliases() } returns AliasSearchResponse(rows = emptyList(), total = 0)

        val devices = repository.fetchDevices()

        assertEquals(1, devices.size)
        assertEquals("192.168.10.10", devices[0].ip)
        assertEquals("aa:bb:cc:dd:ee:ff", devices[0].mac)
        assertTrue(devices[0].isOnline)
        assertFalse(devices[0].isBlocked)
    }

    @Test
    fun `fetchDevices marks device blocked when IP is in alias content`() = runTest {
        coEvery { api.getDhcpLeases() } returns DhcpLeaseResponse(
            total = 1,
            rows = listOf(DhcpLeaseRow("192.168.10.10", "aa:bb:cc:dd:ee:ff", "desktop", "active"))
        )
        coEvery { api.searchAliases() } returns AliasSearchResponse(
            rows = listOf(AliasRow("uuid1", BLOCK_ALIAS_NAME, "host", "192.168.10.10")),
            total = 1,
        )

        val devices = repository.fetchDevices()

        assertTrue(devices[0].isBlocked)
    }

    @Test
    fun `testConnection returns true on successful lease fetch`() = runTest {
        coEvery { api.getDhcpLeases() } returns DhcpLeaseResponse(total = 0, rows = emptyList())

        assertTrue(repository.testConnection())
    }

    @Test
    fun `testConnection returns false on exception`() = runTest {
        coEvery { api.getDhcpLeases() } throws RuntimeException("timeout")

        assertFalse(repository.testConnection())
    }

    @Test
    fun `blockDevice adds IP to existing alias`() = runTest {
        coEvery { api.searchAliases() } returns AliasSearchResponse(
            rows = listOf(AliasRow("uuid1", BLOCK_ALIAS_NAME, "host", "")),
            total = 1,
        )
        coEvery { api.updateAlias(any(), any()) } returns SaveItemResponse("saved")
        coEvery { api.applyAliases() } returns ReconfigureResponse("ok")
        coEvery { api.searchRules() } returns FirewallRuleSearchResponse(rows = emptyList(), total = 0)
        coEvery { api.createRule(any()) } returns SaveItemResponse("saved", "rule-uuid")
        coEvery { api.applyRules() } returns ReconfigureResponse("ok")

        repository.blockDevice("192.168.10.10")

        coVerify { api.updateAlias("uuid1", match { it.alias.content.contains("192.168.10.10") }) }
    }
}
