package com.bibin.opnsense.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response from POST /api/diagnostics/firewall/query_states
 *
 * Returns the pf state table. Each row is one active connection.
 * We filter rows by source/destination IP and sum bytes to get per-device totals.
 */
@JsonClass(generateAdapter = true)
data class PfStatesResponse(
    val rows: List<PfStateRow> = emptyList(),
    val rowCount: Int = 0,
    val total: Int = 0,
)

@JsonClass(generateAdapter = true)
data class PfStateRow(
    @Json(name = "src_addr") val srcAddr: String = "",
    @Json(name = "src_port") val srcPort: String = "",
    @Json(name = "dst_addr") val dstAddr: String = "",
    @Json(name = "dst_port") val dstPort: String = "",
    val proto: String = "",
    val bytes: List<String> = emptyList(),
    val state: String = "",
) {
    val bytesIn:  Long get() = bytes.getOrNull(0)?.toLongOrNull() ?: 0L
    val bytesOut: Long get() = bytes.getOrNull(1)?.toLongOrNull() ?: 0L
    val totalBytes: Long get() = bytesIn + bytesOut
}

data class ConnectionEntry(
    val proto: String,
    val srcAddr: String,
    val srcPort: String,
    val dstAddr: String,
    val dstPort: String,
    val state: String,
    val totalBytes: Long,
)
