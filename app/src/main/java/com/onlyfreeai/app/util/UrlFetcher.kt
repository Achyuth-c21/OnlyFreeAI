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
 */
data class UrlMetadata(
    val title: String = "",
    val description: String = "",
    val logoUrl: String = "",
    val siteName: String = ""
)

object UrlFetcher {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private fun isPrivateAddress(address: java.net.InetAddress): Boolean {
        if (address.isLoopbackAddress || address.isLinkLocalAddress || address.isSiteLocalAddress) {
            return true
        }
        val bytes = address.address
        if (bytes.size == 4) {
            val b0 = bytes[0].toInt() and 0xFF
            val b1 = bytes[1].toInt() and 0xFF
            // RFC 1918: 10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16
            if (b0 == 10) return true
            if (b0 == 172 && (b1 in 16..31)) return true
            if (b0 == 192 && b1 == 168) return true
        }
        return false
    }

    suspend fun fetchMetadata(url: String): Result<UrlMetadata> = withContext(Dispatchers.IO) {
        try {
            if (!url.startsWith("https://")) {
                return@withContext Result.failure(IllegalArgumentException("Only HTTPS URLs are allowed"))
            }
            if (!url.isValidUrl()) {
                return@withContext Result.failure(IllegalArgumentException("Invalid URL"))
            }

            // Parse URL and resolve hostname to block private ranges / SSRF protection
            val parsedUrl = java.net.URL(url)
            val host = parsedUrl.host
            val addresses = java.net.InetAddress.getAllByName(host)
            for (address in addresses) {
                if (isPrivateAddress(address)) {
                    throw IllegalArgumentException("Private URLs are not allowed")
                }
            }

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "OnlyFreeAI/1.0")
                .build()

            val response = client.newCall(request).execute()
            val html = response.use { resp ->
                val contentType = resp.header("Content-Type") ?: ""
                if (!contentType.contains("text/html") && 
                    !contentType.contains("text/xml") && 
                    !contentType.contains("application/xhtml")) {
                    throw Exception("Invalid Content-Type: $contentType")
                }
                val body = resp.body
                val source = body?.source() ?: throw Exception("Empty response")
                source.request(2 * 1024 * 1024)
                source.buffer.readString(minOf(source.buffer.size, 2 * 1024 * 1024), Charsets.UTF_8)
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
