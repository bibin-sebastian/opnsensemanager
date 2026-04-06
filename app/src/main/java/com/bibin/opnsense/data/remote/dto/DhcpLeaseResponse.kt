package com.bibin.opnsense.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DhcpLeaseResponse(
    val total: Int = 0,
    val rowCount: Int = 0,
    val rows: List<DhcpLeaseRow> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class DhcpLeaseRow(
    val address: String = "",
    @Json(name = "hwaddr") val mac: String = "",   // may be absent on static leases
    val hostname: String = "",
    val state: String = "",
    val descr: String? = null,
    val man: String? = null,     // manufacturer
    val starts: String? = null,
    val ends: String? = null,
    @Json(name = "if") val iface: String? = null,
    @Json(name = "if_descr") val ifDescr: String? = null,
    val type: String? = null,
)
