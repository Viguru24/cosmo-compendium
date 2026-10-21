package com.example

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache

/**
 * Application class providing optimized Coil ImageLoader singleton with:
 * - 25% JVM heap LRU Memory Cache for instant, zero-latency image reuse
 * - 250MB dedicated Disk Cache for recipe photography and scanned recipe cards
 * - Fast 150ms crossfade transitions for silky-smooth 60/120fps UI
 */
class CookbookApplication : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .strongReferencesEnabled(true)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(250L * 1024 * 1024)
                    .build()
            }
            .respectCacheHeaders(false)
            .crossfade(150)
            .build()
    }
}
