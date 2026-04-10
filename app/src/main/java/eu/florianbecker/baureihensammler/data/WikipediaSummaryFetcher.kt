package eu.florianbecker.baureihensammler.data

import androidx.core.text.HtmlCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

suspend fun fetchWikipediaSummary(apiUrl: String): String? = withContext(Dispatchers.IO) {
    var conn: HttpURLConnection? = null
    try {
        conn = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 12_000
            readTimeout = 12_000
            setRequestProperty("Accept", "application/json; charset=utf-8")
            setRequestProperty(
                "User-Agent",
                "Baureihensammler/1.0 (Android; eu.florianbecker.baureihensammler)"
            )
        }
        if (conn.responseCode != HttpURLConnection.HTTP_OK) return@withContext null
        val body = conn.inputStream.bufferedReader().use { it.readText() }
        JSONObject(body).optString("extract").trim().takeIf { it.isNotEmpty() }?.let { raw ->
            HtmlCompat.fromHtml(raw, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()
        }
    } catch (_: Exception) {
        null
    } finally {
        conn?.disconnect()
    }
}
