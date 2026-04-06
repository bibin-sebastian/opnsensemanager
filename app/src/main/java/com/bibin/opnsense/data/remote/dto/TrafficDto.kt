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
    @Json(name = "src-addr") val srcAddr: String = "",
    @Json(name = "dst-addr") val dstAddr: String = "",
    @Json(name = "nat-addr") val natAddr: String = "",
    val proto: String = "",
    /** [bytes_in, bytes_out] as a two-element array from OPNsense */
    val bytes: List<String> = emptyList(),
    val packets: List<String> = emptyList(),
    val state: String = "",
    val label: String = "",
    val iface: String = "",
) {
    val bytesIn:  Long get() = bytes.getOrNull(0)?.toLongOrNull() ?: 0L
    val bytesOut: Long get() = bytes.getOrNull(1)?.toLongOrNull() ?: 0L
    val totalBytes: Long get() = bytesIn + bytesOut
}

/** Aggregated traffic stats for a single device IP. */
data class TrafficRecord(
    val address: String,
    val totalBytes: Long,          // sum of all state bytes where device is src or dst
    val rateBitsIn: Long = 0,      // calculated by caller from successive snapshots
    val rateBitsOut: Long = 0,
    val cumulativeBytesIn: Long = 0,
    val cumulativeBytesOut: Long = 0,
)
