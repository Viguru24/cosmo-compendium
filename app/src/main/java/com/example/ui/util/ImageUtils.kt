package com.example.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object ImageUtils {
    private const val TAG = "ImageUtils"
    private const val MAX_DIMENSION = 1200
    private const val LOW_RES_MAX_DIMENSION = 960

    fun loadAndDownscaleBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val contentResolver = context.contentResolver

            // 1. Measure dimensions without decoding pixel data into memory
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }

            var inSampleSize = 1
            if (options.outHeight > MAX_DIMENSION || options.outWidth > MAX_DIMENSION) {
                val halfHeight: Int = options.outHeight / 2
                val halfWidth: Int = options.outWidth / 2
                while (halfHeight / inSampleSize >= MAX_DIMENSION && halfWidth / inSampleSize >= MAX_DIMENSION) {
                    inSampleSize *= 2
                }
            }

            // 2. Decode scaled bitmap safely into SOFTWARE ARGB_8888 config
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inMutable = true
            }

            var bitmap: Bitmap? = null
            contentResolver.openInputStream(uri)?.use { inputStream ->
                bitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            }

            bitmap?.let { ensureSoftwareBitmap(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load and downscale bitmap from uri: $uri", e)
            null
        }
    }

    fun ensureSoftwareBitmap(bitmap: Bitmap): Bitmap {
        return try {
            if (bitmap.config == Bitmap.Config.HARDWARE || !bitmap.isMutable) {
                bitmap.copy(Bitmap.Config.ARGB_8888, true) ?: bitmap
            } else {
                // If dimensions are still greater than MAX_DIMENSION, scale down proportionally
                if (bitmap.width > MAX_DIMENSION || bitmap.height > MAX_DIMENSION) {
                    val ratio = minOf(
                        MAX_DIMENSION.toFloat() / bitmap.width,
                        MAX_DIMENSION.toFloat() / bitmap.height
                    )
                    val targetWidth = (bitmap.width * ratio).toInt().coerceAtLeast(1)
                    val targetHeight = (bitmap.height * ratio).toInt().coerceAtLeast(1)
                    Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
                } else {
                    bitmap
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error ensuring software bitmap", e)
            bitmap
        }
    }

    /**
     * Saves a lowish resolution / optimized compressed reference version of the captured recipe photo
     * to internal storage for long-term reference and visual comparison with transcribed recipe steps.
     */
    fun saveLowResReferenceImage(context: Context, bitmap: Bitmap): String? {
        return try {
            val photosDir = File(context.filesDir, "recipe_photos").apply {
                if (!exists()) mkdirs()
            }
            val filename = "recipe_ref_${System.currentTimeMillis()}.jpg"
            val file = File(photosDir, filename)

            val softwareBmp = ensureSoftwareBitmap(bitmap)
            val ratio = if (softwareBmp.width > LOW_RES_MAX_DIMENSION || softwareBmp.height > LOW_RES_MAX_DIMENSION) {
                minOf(
                    LOW_RES_MAX_DIMENSION.toFloat() / softwareBmp.width,
                    LOW_RES_MAX_DIMENSION.toFloat() / softwareBmp.height
                )
            } else 1.0f

            val targetWidth = (softwareBmp.width * ratio).toInt().coerceAtLeast(1)
            val targetHeight = (softwareBmp.height * ratio).toInt().coerceAtLeast(1)

            val finalBmp = if (ratio < 1.0f) {
                Bitmap.createScaledBitmap(softwareBmp, targetWidth, targetHeight, true)
            } else {
                softwareBmp
            }

            FileOutputStream(file).use { outStream ->
                finalBmp.compress(Bitmap.CompressFormat.JPEG, 82, outStream)
                outStream.flush()
            }

            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save low-res reference image", e)
            null
        }
    }
}
