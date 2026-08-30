package com.example.data.sync

import android.content.Context
import android.content.SharedPreferences

class SyncConfig(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("heirloom_sync_prefs", Context.MODE_PRIVATE)

    var isSyncEnabled: Boolean
        get() = prefs.getBoolean(KEY_SYNC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SYNC_ENABLED, value).apply()

    var serverUrl: String
        get() = prefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
        set(value) = prefs.edit().putString(KEY_SERVER_URL, value.trim()).apply()

    var syncToken: String
        get() = prefs.getString(KEY_SYNC_TOKEN, DEFAULT_SYNC_TOKEN) ?: DEFAULT_SYNC_TOKEN
        set(value) = prefs.edit().putString(KEY_SYNC_TOKEN, value.trim()).apply()

    var autoSyncWifi: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SYNC_WIFI, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_SYNC_WIFI, value).apply()

    var lastSyncTimestamp: Long
        get() = prefs.getLong(KEY_LAST_SYNC_TIMESTAMP, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_SYNC_TIMESTAMP, value).apply()

    var lastSyncStatusMessage: String
        get() = prefs.getString(KEY_LAST_SYNC_STATUS, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_SYNC_STATUS, value).apply()

    fun getCleanServerUrl(): String {
        var url = serverUrl.trim().ifBlank { DEFAULT_SERVER_URL }
        if (url.endsWith("/")) {
            url = url.substring(0, url.length - 1)
        }
        if (url.isNotBlank() && !url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }
        return url
    }

    companion object {
        const val DEFAULT_SERVER_URL = "https://api.cosmowhisper.com/cookbook"
        const val DEFAULT_SYNC_TOKEN = "heirloom-cookbook-sync-key-2026"

        private const val KEY_SYNC_ENABLED = "sync_enabled"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_SYNC_TOKEN = "sync_token"
        private const val KEY_AUTO_SYNC_WIFI = "auto_sync_wifi"
        private const val KEY_LAST_SYNC_TIMESTAMP = "last_sync_timestamp"
        private const val KEY_LAST_SYNC_STATUS = "last_sync_status"
    }
}
