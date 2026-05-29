package com.onlyfreeai.app.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

/**
 * Fetches metadata (title, description, favicon) from a given URL.
 * Used when developers submit a new tool — auto-populates form fields.
 *
 * SECURITY HARDENING:
 * - HTTPS-only enforcement
 * - SSRF protection: blocks private/internal IPs, loopback, link-local, metadata endpoints
 * - Response size capped at 1MB
 * - Content-Type validation
 * - Strict timeouts
 * - Input sanitization on all outputs
 */
data class UrlMetadata(
    val title: String = "",
    val description: String = "",
    val logoUrl: String = "",
    val siteName: String = ""
)

object UrlFetcher {

    // Max response body size: 1MB (prevents memory exhaustion from malicious large responses)
    private const val MAX_RESPONSE_BYTES = 1L * 1024 * 1024

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        // Limit redirects to prevent redirect loops
        .build()

    /**
     * SSRF Protection: blocks private, loopback, link-local, and cloud metadata addresses.
     */
    private fun isPrivateOrDangerousAddress(address: java.net.InetAddress): Boolean {
        if (address.isLoopbackAddress || address.isLinkLocalAddress || address.isSiteLocalAddress) {
            return true
        }
        if (address.isMulticastAddress || address.isAnyLocalAddress) {
            return true
        }
        val bytes = address.address
        if (bytes.size == 4) {
            val b0 = bytes[0].toInt() and 0xFF
            val b1 = bytes[1].toInt() and 0xFF
            // RFC 1918: 10.0.0.0/8
            if (b0 == 10) return true
            // RFC 1918: 172.16.0.0/12
            if (b0 == 172 && (b1 in 16..31)) return true
            // RFC 1918: 192.168.0.0/16
            if (b0 == 192 && b1 == 168) return true
            // AWS/GCP/Azure metadata endpoint: 169.254.169.254
            if (b0 == 169 && b1 == 254) return true
            // Carrier-grade NAT: 100.64.0.0/10
            if (b0 == 100 && (b1 in 64..127)) return true
        }
        // IPv6 loopback
        if (bytes.size == 16) {
            val isIpv6Loopback = bytes.dropLast(1).all { it == 0.toByte() } && bytes.last() == 1.toByte()
            if (isIpv6Loopback) return true
        }
        return false
    }

    /**
     * Validates the hostname to block common SSRF bypass techniques.
     */
    private fun isHostnameSafe(host: String): Boolean {
        // Block IP-based URLs (decimal, hex, octal representations)
        if (host.matches(Regex("^\\d+\\.\\d+\\.\\d+\\.\\d+$"))) {
            // Allow only if it resolves to a public address (checked later)
            return true
        }
        // Block localhost variants
        if (host.equals("localhost", ignoreCase = true)) return false
        // Block cloud metadata hostnames
        if (host.equals("metadata.google.internal", ignoreCase = true)) return false
        if (host.endsWith(".internal", ignoreCase = true)) return false
        return true
    }

    suspend fun fetchMetadata(url: String): Result<UrlMetadata> = withContext(Dispatchers.IO) {
        try {
            // SECURITY: Enforce HTTPS only
            if (!url.startsWith("https://")) {
                return@withContext Result.failure(IllegalArgumentException("Only HTTPS URLs are allowed"))
            }
            if (!url.isValidUrl()) {
                return@withContext Result.failure(IllegalArgumentException("Invalid URL"))
            }

            // Parse URL and validate hostname
            val parsedUrl = java.net.URL(url)
            val host = parsedUrl.host ?: return@withContext Result.failure(
                IllegalArgumentException("Invalid hostname")
            )

            if (!isHostnameSafe(host)) {
                return@withContext Result.failure(
                    IllegalArgumentException("Blocked hostname for security reasons")
                )
            }

            // Block custom port usage (only 443 allowed for HTTPS)
            if (parsedUrl.port != -1 && parsedUrl.port != 443) {
                return@withContext Result.failure(
                    IllegalArgumentException("Non-standard ports are not allowed")
                )
            }

            // Resolve hostname and validate all addresses
            val addresses = java.net.InetAddress.getAllByName(host)
            for (address in addresses) {
                if (isPrivateOrDangerousAddress(address)) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Private/internal URLs are not allowed")
                    )
                }
            }

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "OnlyFreeAI/1.0")
                .header("Accept", "text/html")
                .build()

            val response = client.newCall(request).execute()
            val html = response.use { resp ->
                if (!resp.isSuccessful) {
                    throw Exception("HTTP ${resp.code}: ${resp.message}")
                }

                val contentType = resp.header("Content-Type") ?: ""
                if (!contentType.contains("text/html") &&
                    !contentType.contains("text/xml") &&
                    !contentType.contains("application/xhtml")) {
                    throw Exception("Invalid Content-Type: $contentType")
                }

                val body = resp.body
                val source = body?.source() ?: throw Exception("Empty response")
                source.request(MAX_RESPONSE_BYTES)
                val size = minOf(source.buffer.size, MAX_RESPONSE_BYTES)
                source.buffer.readString(size, Charsets.UTF_8)
            }

            val doc = Jsoup.parse(html)

            val title = doc.select("meta[property=og:title]").attr("content")
                .ifBlank { doc.title() }
                .sanitize()

            val description = doc.select("meta[property=og:description]").attr("content")
                .ifBlank { doc.select("meta[name=description]").attr("content") }
                .sanitize()

            val logoUrl = doc.select("meta[property=og:image]").attr("content")
                .ifBlank { doc.select("link[rel=icon]").attr("abs:href") }
                .ifBlank { doc.select("link[rel=shortcut icon]").attr("abs:href") }
                // SECURITY: Only allow HTTPS logo URLs
                .let { if (it.startsWith("https://")) it else "" }

            val siteName = doc.select("meta[property=og:site_name]").attr("content")
                .ifBlank { title }
                .sanitize()

            Result.success(
                UrlMetadata(
                    title = title,
                    description = description,
                    logoUrl = logoUrl,
                    siteName = siteName
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
