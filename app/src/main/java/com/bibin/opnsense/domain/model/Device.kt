package com.bibin.opnsense.domain.model

data class Device(
    val mac: String,
    val ip: String,
    val hostname: String,
    val friendlyName: String?,
    val isOnline: Boolean,
    val isBlocked: Boolean,
)

val Device.displayName: String
    get() = friendlyName?.takeIf { it.isNotBlank() } ?: hostname.takeIf { it.isNotBlank() } ?: ip

/** Stable key for local storage: use MAC when available, fall back to IP for devices without one. */
val Device.stableId: String
    get() = mac.ifBlank { ip }
