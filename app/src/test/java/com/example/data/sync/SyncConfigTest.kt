package com.example.data.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SyncConfigTest {

    private lateinit var context: Context
    private lateinit var syncConfig: SyncConfig

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val prefs = context.getSharedPreferences("heirloom_sync_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        syncConfig = SyncConfig(context)
    }

    @Test
    fun testDefaultValues() {
        assertTrue(syncConfig.isSyncEnabled)
        assertTrue(syncConfig.autoSyncWifi)
        assertEquals(SyncConfig.DEFAULT_SERVER_URL, syncConfig.serverUrl)
        assertEquals(SyncConfig.DEFAULT_SYNC_TOKEN, syncConfig.syncToken)
        assertEquals(0L, syncConfig.lastSyncTimestamp)
        assertEquals("", syncConfig.lastSyncStatusMessage)
    }

    @Test
    fun testGetCleanServerUrlFormatting() {
        syncConfig.serverUrl = "api.example.com/cookbook/"
        assertEquals("https://api.example.com/cookbook", syncConfig.getCleanServerUrl())

        syncConfig.serverUrl = "http://localhost:8080/sync/"
        assertEquals("http://localhost:8080/sync", syncConfig.getCleanServerUrl())

        syncConfig.serverUrl = "https://custom-server.org/api/"
        assertEquals("https://custom-server.org/api", syncConfig.getCleanServerUrl())
    }

    @Test
    fun testPropertyPersistence() {
        syncConfig.isSyncEnabled = false
        syncConfig.serverUrl = "https://my-nas.local/cookbook"
        syncConfig.syncToken = "secret-token-123"
        syncConfig.lastSyncTimestamp = 1700000000000L
        syncConfig.lastSyncStatusMessage = "Sync successful"

        // Reload fresh instance from preferences
        val reloaded = SyncConfig(context)
        assertEquals(false, reloaded.isSyncEnabled)
        assertEquals("https://my-nas.local/cookbook", reloaded.serverUrl)
        assertEquals("secret-token-123", reloaded.syncToken)
        assertEquals(1700000000000L, reloaded.lastSyncTimestamp)
        assertEquals("Sync successful", reloaded.lastSyncStatusMessage)
    }
}
