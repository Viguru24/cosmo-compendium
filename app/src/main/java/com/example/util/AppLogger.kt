package com.example.util

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR
}

data class LogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel,
    val tag: String,
    val message: String,
    val details: String? = null
) {
    val formattedTime: String
        get() = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(timestamp))
}

object AppLogger {
    private const val MAX_LOGS = 500
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    fun d(tag: String, message: String, details: String? = null) {
        try { Log.d(tag, message) } catch (_: Throwable) {}
        addEntry(LogLevel.DEBUG, tag, message, details)
    }

    fun i(tag: String, message: String, details: String? = null) {
        try { Log.i(tag, message) } catch (_: Throwable) {}
        addEntry(LogLevel.INFO, tag, message, details)
    }

    fun w(tag: String, message: String, tr: Throwable? = null) {
        try { Log.w(tag, message, tr) } catch (_: Throwable) {}
        addEntry(LogLevel.WARN, tag, message, tr?.stackTraceToString())
    }

    fun e(tag: String, message: String, tr: Throwable? = null) {
        try { Log.e(tag, message, tr) } catch (_: Throwable) {}
        addEntry(LogLevel.ERROR, tag, message, tr?.stackTraceToString())
    }

    @Synchronized
    private fun addEntry(level: LogLevel, tag: String, message: String, details: String?) {
        val entry = LogEntry(level = level, tag = tag, message = message, details = details)
        val current = _logs.value.toMutableList()
        current.add(0, entry) // Newest first
        if (current.size > MAX_LOGS) {
            _logs.value = current.take(MAX_LOGS)
        } else {
            _logs.value = current
        }
    }

    fun clear() {
        _logs.value = emptyList()
    }

    fun getFormattedLogs(): String {
        val sb = StringBuilder()
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        sb.append("=== COSMO COMPENDIUM - SYSTEM & DIAGNOSTIC LOG ===\n")
        sb.append("Generated at: $now\n")
        sb.append("Total entries: ${_logs.value.size}\n\n")

        for (log in _logs.value) {
            val symbol = when (log.level) {
                LogLevel.ERROR -> "[ERROR]"
                LogLevel.WARN  -> "[WARN ]"
                LogLevel.INFO  -> "[INFO ]"
                LogLevel.DEBUG -> "[DEBUG]"
            }
            sb.append("${log.formattedTime} $symbol [${log.tag}] ${log.message}\n")
            if (!log.details.isNullOrBlank()) {
                sb.append("  Details / Trace: ${log.details}\n")
            }
        }
        return sb.toString()
    }
}
