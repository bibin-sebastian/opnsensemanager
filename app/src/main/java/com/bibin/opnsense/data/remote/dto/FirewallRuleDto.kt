package com.bibin.opnsense.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FirewallRuleSearchResponse(
    val rows: List<FirewallRuleRow>,
    val total: Int,
)

@JsonClass(generateAdapter = true)
data class FirewallRuleRow(
    val uuid: String,
    val description: String,
    val action: String,
    val enabled: String,
)

@JsonClass(generateAdapter = true)
data class FirewallRuleRequest(
    val rule: FirewallRulePayload,
)

@JsonClass(generateAdapter = true)
data class FirewallRulePayload(
    val action: String = "block",
    @Json(name = "interface") val networkInterface: String = "lan",
    val source_net: String,
    val destination_net: String = "any",
    val description: String = "OPNsense Manager block rule",
    val enabled: String = "1",
    /** Optional: name of an OPNsense schedule object that controls when this rule is active. */
    val sched: String? = null,
)
