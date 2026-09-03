package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages persistent tombstones for deleted recipes.
 * Prevents deleted recipes from ever resurrecting from remote sync servers,
 * software updates, or stale local caches.
 */
class TombstoneManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "heirloom_recipe_tombstones",
        Context.MODE_PRIVATE
    )

    fun recordTombstone(vararg titles: String?) {
        val editor = prefs.edit()
        val now = System.currentTimeMillis()
        for (t in titles) {
            if (!t.isNullOrBlank()) {
                val clean = normalizeTitle(t)
                if (clean.isNotBlank()) {
                    editor.putLong(clean, now)
                }
            }
        }
        editor.apply()
    }

    fun isTombstoned(title: String, itemUpdatedAt: Long): Boolean {
        val clean = normalizeTitle(title)
        if (clean.isBlank()) return false
        val deletedAt = prefs.getLong(clean, 0L)
        if (deletedAt == 0L) return false
        // If the item was updated BEFORE it was deleted locally, it is a zombie and must be rejected!
        return itemUpdatedAt <= deletedAt
    }

    fun removeTombstone(vararg titles: String?) {
        val editor = prefs.edit()
        for (t in titles) {
            if (!t.isNullOrBlank()) {
                val clean = normalizeTitle(t)
                if (clean.isNotBlank()) {
                    editor.remove(clean)
                }
            }
        }
        editor.apply()
    }

    private fun normalizeTitle(title: String): String {
        return title.lowercase()
            .replace(Regex("(?i)grandma's|grandmas|omas|oma's|traditional|vintage|classic|authentic|homemade|recipe|card"), "")
            .replace(Regex("[^a-z0-9]"), "")
            .trim()
    }
}
