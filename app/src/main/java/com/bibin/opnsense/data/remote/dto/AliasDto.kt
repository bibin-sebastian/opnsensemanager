package com.bibin.opnsense.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AliasSearchResponse(
    val rows: List<AliasRow> = emptyList(),
    val total: Int = 0,
    val rowCount: Int = 0,
)

@JsonClass(generateAdapter = true)
data class AliasRow(
    val uuid: String = "",
    val name: String = "",
    val type: String = "",
    val content: String = "",
    val description: String = "",
    val enabled: String = "0",
    // OPNsense returns "%type" — mapped here, ignored in logic
    @Json(name = "%type") val displayType: String? = null,
)

@JsonClass(generateAdapter = true)
data class AliasItemRequest(
    val alias: AliasPayload,
)

@JsonClass(generateAdapter = true)
data class AliasPayload(
    val name: String,
    val type: String = "host",
    val content: String,
    val description: String = "Managed by OPNsense Manager",
    val enabled: String = "1",
)

@JsonClass(generateAdapter = true)
data class SaveItemResponse(
    val result: String = "",
    val uuid: String? = null,
)

@JsonClass(generateAdapter = true)
data class ReconfigureResponse(
    val status: String = "",
)
