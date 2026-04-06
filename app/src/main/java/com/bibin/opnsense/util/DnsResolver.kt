package com.bibin.opnsense.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DnsResolver @Inject constructor() {

    private val cache = ConcurrentHashMap<String, String>()

    /**
     * Reverse-resolves [ip] to a human-readable hostname.
     * Returns null if no PTR record exists or the lookup fails.
     * Results are cached in-memory for the lifetime of the app session.
     */
    suspend fun resolve(ip: String): String? {
        cache[ip]?.let { return it }
        return withContext(Dispatchers.IO) {
            runCatching {
                val host = InetAddress.getByName(ip).canonicalHostName
                // canonicalHostName returns the IP string itself when no PTR exists
                if (host == ip) null else simplify(host)
            }.getOrNull()
        }?.also { label -> cache[ip] = label }
    }

    /**
     * Collapses a full hostname down to its last two meaningful labels.
     * e.g. "edge-star.facebook.com" → "facebook.com"
     *      "ec2-18-210.compute-1.amazonaws.com" → "amazonaws.com"
     */
    private fun simplify(host: String): String {
        val parts = host.trimEnd('.').split('.')
        return if (parts.size >= 2) parts.takeLast(2).joinToString(".") else host
    }
}
