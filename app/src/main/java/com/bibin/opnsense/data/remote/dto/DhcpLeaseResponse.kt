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
    @Json(name = "hwaddr") val hwaddr: String = "",  // older OPNsense / ISC DHCP
    val mac: String = "",                             // newer OPNsense / Kea DHCP
    val hostname: String = "",
    val state: String = "",
) {
    /** Whichever field the running OPNsense version populates. */
    val macAddress: String get() = hwaddr.ifBlank { mac }
}
