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
    @Json(name = "hwaddr") val mac: String = "",
    val hostname: String = "",
    val state: String = "",
)
