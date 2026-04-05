package com.bibin.opnsense.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DhcpLeaseResponse(
    val total: Int,
    val rows: List<DhcpLeaseRow>,
)

@JsonClass(generateAdapter = true)
data class DhcpLeaseRow(
    val address: String,
    @Json(name = "hwaddr") val mac: String,
    val hostname: String,
    val state: String, // "active" | "expired"
)
