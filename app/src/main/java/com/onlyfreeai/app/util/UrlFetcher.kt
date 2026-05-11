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

    suspend fun fetchMetadata(url: String): Result<UrlMetadata> = withContext(Dispatchers.IO) {
        try {
            if (!url.isValidUrl()) {
                return@withContext Result.failure(IllegalArgumentException("Invalid URL"))
            }

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "OnlyFreeAI/1.0")
                .build()

            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: ""
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
