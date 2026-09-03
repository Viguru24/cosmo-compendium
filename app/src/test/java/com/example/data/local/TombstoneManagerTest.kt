package com.example.data.local

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TombstoneManagerTest {

    private class FakeSharedPreferences : android.content.SharedPreferences {
        val map = mutableMapOf<String, Any>()

        override fun getAll(): MutableMap<String, *> = map
        override fun getString(key: String?, defValue: String?): String? = map[key] as? String ?: defValue
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = (map[key] as? Set<String>)?.toMutableSet() ?: defValues
        override fun getInt(key: String?, defValue: Int): Int = map[key] as? Int ?: defValue
        override fun getLong(key: String?, defValue: Long): Long = map[key] as? Long ?: defValue
        override fun getFloat(key: String?, defValue: Float): Float = map[key] as? Float ?: defValue
        override fun getBoolean(key: String?, defValue: Boolean): Boolean = map[key] as? Boolean ?: defValue
        override fun contains(key: String?): Boolean = map.containsKey(key)
        override fun edit(): android.content.SharedPreferences.Editor = FakeEditor(this)
        override fun registerOnSharedPreferenceChangeListener(listener: android.content.SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: android.content.SharedPreferences.OnSharedPreferenceChangeListener?) {}

        class FakeEditor(private val parent: FakeSharedPreferences) : android.content.SharedPreferences.Editor {
            private val temp = mutableMapOf<String, Any>()
            private val removals = mutableSetOf<String>()

            override fun putString(key: String?, value: String?): android.content.SharedPreferences.Editor { if (key != null && value != null) temp[key] = value; return this }
            override fun putStringSet(key: String?, values: MutableSet<String>?): android.content.SharedPreferences.Editor { if (key != null && values != null) temp[key] = values; return this }
            override fun putInt(key: String?, value: Int): android.content.SharedPreferences.Editor { if (key != null) temp[key] = value; return this }
            override fun putLong(key: String?, value: Long): android.content.SharedPreferences.Editor { if (key != null) temp[key] = value; return this }
            override fun putFloat(key: String?, value: Float): android.content.SharedPreferences.Editor { if (key != null) temp[key] = value; return this }
            override fun putBoolean(key: String?, value: Boolean): android.content.SharedPreferences.Editor { if (key != null) temp[key] = value; return this }
            override fun remove(key: String?): android.content.SharedPreferences.Editor { if (key != null) removals.add(key); return this }
            override fun clear(): android.content.SharedPreferences.Editor { temp.clear(); removals.addAll(parent.map.keys); return this }
            override fun commit(): Boolean { apply(); return true }
            override fun apply() {
                removals.forEach { parent.map.remove(it) }
                parent.map.putAll(temp)
            }
        }
    }

    private class TestContext(private val fakePrefs: FakeSharedPreferences) : android.content.ContextWrapper(null) {
        override fun getSharedPreferences(name: String?, mode: Int): android.content.SharedPreferences = fakePrefs
    }

    @Test
    fun testDeletedRecipeIsTombstonedAndRejected() {
        val fakePrefs = FakeSharedPreferences()
        val context = TestContext(fakePrefs)
        val manager = TombstoneManager(context)
        val now = 100000L

        manager.recordTombstone("Grandma's Elderflower Syrup", "Elderflower Syrup")

        // 1. Remote item with timestamp older than deletion -> REJECT (isTombstoned = true)
        assertTrue(manager.isTombstoned("Elderflower Syrup", now - 5000L))
        assertTrue(manager.isTombstoned("Grandma's Elderflower Syrup", now - 1000L))

        // 2. Unrelated recipe -> NOT tombstoned
        assertFalse(manager.isTombstoned("Chocolate Cake", now - 5000L))
    }

    @Test
    fun testIntentionalRecreationClearsTombstone() {
        val fakePrefs = FakeSharedPreferences()
        val context = TestContext(fakePrefs)
        val manager = TombstoneManager(context)
        manager.recordTombstone("Bobotie South Africa")

        assertTrue(manager.isTombstoned("Bobotie South Africa", 50000L))

        // User intentionally recreates or restores Bobotie
        manager.removeTombstone("Bobotie South Africa")

        assertFalse(manager.isTombstoned("Bobotie South Africa", 50000L))
    }
}
