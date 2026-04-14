package eu.florianbecker.baureihensammler.util

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

private val entryTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
private val fileTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS")

data class DebugLogEntry(
    val fileName: String,
    val timestamp: String,
    val source: String,
    val message: String,
    val fullText: String,
)

object DebugLogStore {
    private const val LOG_DIR_NAME = "runtime_debug_logs"

    fun resetForNewAppSession(context: Context) {
        val dir = logDir(context)
        if (dir.exists()) {
            dir.listFiles()?.forEach { it.delete() }
        }
        dir.mkdirs()
    }

    fun logError(
        context: Context,
        source: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        val now = LocalDateTime.now()
        val timestamp = now.format(entryTimeFormatter)
        val stack =
            throwable?.let {
                StringWriter().use { sw ->
                    PrintWriter(sw).use { pw ->
                        it.printStackTrace(pw)
                    }
                    sw.toString()
                }
            } ?: ""

        val payload =
            buildString {
                appendLine("timestamp=$timestamp")
                appendLine("source=$source")
                appendLine("message=$message")
                if (throwable != null) {
                    appendLine("exception=${throwable::class.java.name}: ${throwable.message ?: "(no message)"}")
                    appendLine("--- stacktrace ---")
                    append(stack)
                }
            }.trim()

        val fileName = "${now.format(fileTimeFormatter)}_${UUID.randomUUID()}.log"
        runCatching {
            val target = File(logDir(context), fileName)
            target.parentFile?.mkdirs()
            target.writeText(payload)
        }
    }

    fun listLogs(context: Context): List<DebugLogEntry> {
        val files =
            logDir(context).listFiles { file -> file.isFile && file.extension.equals("log", true) }
                ?.sortedByDescending { it.lastModified() }
                ?: return emptyList()

        return files.mapNotNull { file ->
            val txt = runCatching { file.readText() }.getOrNull() ?: return@mapNotNull null
            val lines = txt.lineSequence().toList()
            val timestamp =
                lines.firstOrNull { it.startsWith("timestamp=") }?.removePrefix("timestamp=")
                    ?: "(unbekannt)"
            val source =
                lines.firstOrNull { it.startsWith("source=") }?.removePrefix("source=")
                    ?: "(unbekannt)"
            val message =
                lines.firstOrNull { it.startsWith("message=") }?.removePrefix("message=")
                    ?: "(keine Nachricht)"
            DebugLogEntry(
                fileName = file.name,
                timestamp = timestamp,
                source = source,
                message = message,
                fullText = txt
            )
        }
    }

    private fun logDir(context: Context): File = File(context.cacheDir, LOG_DIR_NAME)
}
