package com.bibin.opnsense.data.repository

import android.util.Log
import com.bibin.opnsense.data.remote.OPNsenseApi
import com.bibin.opnsense.data.remote.OPNsenseClient
import com.bibin.opnsense.data.remote.dto.*
import com.bibin.opnsense.domain.model.Device
import com.bibin.opnsense.util.CredentialManager
import javax.inject.Inject
import javax.inject.Singleton

const val BLOCK_ALIAS_NAME = "android_app_blocked"
const val BLOCK_RULE_DESCRIPTION = "OPNsense Manager block rule"

@Singleton
class OPNsenseRepository @Inject constructor(
    private var api: OPNsenseApi,
    private val credentialManager: CredentialManager,
) {
    /**
     * Rebuild the API client when credentials have changed (called from SettingsViewModel).
     */
    fun refreshApi() {
        api = OPNsenseClient.build(
            baseUrl = credentialManager.firewallUrl,
            apiKey = credentialManager.apiKey,
            apiSecret = credentialManager.apiSecret,
        )
    }

    /**
     * Fetch DHCP leases and cross-reference with the block alias
     * to determine blocked status per device.
     */
    suspend fun fetchDevices(): List<Device> {
        val leases = api.getDhcpLeases().rows.filter { it.address.isNotBlank() }
        val blockedIps = getBlockedIps()
        return leases.map { row ->
            Device(
                mac = row.mac,
                ip = row.address,
                hostname = row.hostname,
                friendlyName = null, // populated by caller from LocalRepository
                isOnline = row.state == "active",
                isBlocked = row.address in blockedIps,
            )
        }
    }

    suspend fun testConnection(): Boolean = runCatching {
        api.getDhcpLeases()
    }.onFailure { e ->
        Log.e("OPNsenseRepo", "testConnection failed: ${e::class.simpleName}: ${e.message}", e)
    }.isSuccess

    /**
     * Add device IP to the block alias, creating the alias and
     * a block firewall rule on first use.
     */
    suspend fun blockDevice(ip: String) {
        val aliasUuid = ensureBlockAliasExists()
        val currentAlias = getBlockAliasRow()
        val currentIps = currentAlias?.content
            ?.split("\n")?.map { it.trim() }?.filter { it.isNotBlank() }
            ?.toMutableSet() ?: mutableSetOf()
        currentIps.add(ip)
        api.updateAlias(aliasUuid, buildAliasRequest(currentIps.joinToString("\n")))
        api.applyAliases()
        ensureBlockRuleExists()
        api.applyRules()
    }

    suspend fun unblockDevice(ip: String) {
        val aliasRow = getBlockAliasRow() ?: return
        val remaining = aliasRow.content
            .split("\n").map { it.trim() }.filter { it.isNotBlank() && it != ip }
        api.updateAlias(aliasRow.uuid, buildAliasRequest(remaining.joinToString("\n")))
        api.applyAliases()
        api.applyRules()
    }

    private suspend fun getBlockedIps(): Set<String> {
        val aliases = api.searchAliases().rows
        val blockAlias = aliases.firstOrNull { it.name == BLOCK_ALIAS_NAME } ?: return emptySet()
        return blockAlias.content.split("\n").map { it.trim() }.filter { it.isNotBlank() }.toSet()
    }

    private suspend fun getBlockAliasRow(): AliasRow? =
        api.searchAliases().rows.firstOrNull { it.name == BLOCK_ALIAS_NAME }

    private suspend fun ensureBlockAliasExists(): String {
        val existing = getBlockAliasRow()
        if (existing != null) return existing.uuid
        val result = api.createAlias(buildAliasRequest(""))
        return result.uuid ?: error("Failed to create block alias")
    }

    private suspend fun ensureBlockRuleExists() {
        val rules = api.searchRules().rows
        if (rules.none { it.description == BLOCK_RULE_DESCRIPTION }) {
            api.createRule(
                FirewallRuleRequest(
                    FirewallRulePayload(
                        source_net = BLOCK_ALIAS_NAME,
                        description = BLOCK_RULE_DESCRIPTION,
                    )
                )
            )
        }
    }

    private fun buildAliasRequest(content: String) = AliasItemRequest(
        alias = AliasPayload(name = BLOCK_ALIAS_NAME, content = content)
    )

    suspend fun fetchDeviceConnections(deviceIp: String): List<ConnectionEntry> {
        val rows = api.queryPfStates().rows.filter { row ->
            row.srcAddr == deviceIp || row.dstAddr == deviceIp
        }
        return rows.map { row ->
            ConnectionEntry(
                proto      = row.proto.ifBlank { "?" },
                srcAddr    = row.srcAddr,
                srcPort    = row.srcPort,
                dstAddr    = row.dstAddr,
                dstPort    = row.dstPort,
                state      = row.state,
                totalBytes = row.totalBytes,
            )
        }
    }
}
