package com.bibin.opnsense.data.remote

import com.bibin.opnsense.data.remote.dto.*
import retrofit2.http.*

interface OPNsenseApi {

    // DHCP leases
    @GET("api/dhcpv4/leases/searchLease")
    suspend fun getDhcpLeases(): DhcpLeaseResponse

    // Firewall aliases
    @GET("api/firewall/alias/searchItem")
    suspend fun searchAliases(): AliasSearchResponse

    @POST("api/firewall/alias/addItem")
    suspend fun createAlias(@Body request: AliasItemRequest): SaveItemResponse

    @POST("api/firewall/alias/setItem/{uuid}")
    suspend fun updateAlias(
        @Path("uuid") uuid: String,
        @Body request: AliasItemRequest,
    ): SaveItemResponse

    @POST("api/firewall/alias/reconfigure")
    suspend fun applyAliases(): ReconfigureResponse

    // Firewall filter rules
    @GET("api/firewall/filter/searchRule")
    suspend fun searchRules(): FirewallRuleSearchResponse

    @POST("api/firewall/filter/addRule")
    suspend fun createRule(@Body request: FirewallRuleRequest): SaveItemResponse

    @POST("api/firewall/filter/delRule/{uuid}")
    suspend fun deleteRule(@Path("uuid") uuid: String): SaveItemResponse

    @POST("api/firewall/filter/apply")
    suspend fun applyRules(): ReconfigureResponse

    // Firewall state table — core endpoint, used to derive per-device traffic
    // OPNsense maps snake_case action query_states → camelCase URL queryStates
    @POST("api/diagnostics/firewall/queryStates")
    suspend fun queryPfStates(@Body request: Map<String, String> = emptyMap()): PfStatesResponse
}
