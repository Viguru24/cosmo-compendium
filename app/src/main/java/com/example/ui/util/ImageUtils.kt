package com.example.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import com.example.util.AppLogger
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageUtils {
    private const val TAG = "ImageUtils"
    // 1536px provides optimal balance: pin-sharp OCR reading of tiny handwritten text & fast transmission
    private const val MAX_AI_DIMENSION = 1536
    private const val LOW_RES_MAX_DIMENSION = 1080

    /**
     * Creates a temporary file Uri in the app's cache directory for camera capture.
     * Cleans up stale temp files to avoid disk bloat from high-megapixel camera sensors.
     */
    fun createTempCameraUri(context: Context): Uri {
        val cacheDir = File(context.cacheDir, "camera_captures").apply {
            if (!exists()) mkdirs()
        }
        cleanupStaleTempFiles(cacheDir)
        val tempFile = File(cacheDir, "temp_capture_${System.currentTimeMillis()}.jpg")
        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, tempFile)
    }

    private fun cleanupStaleTempFiles(directory: File) {
        try {
            val files = directory.listFiles() ?: return
            val now = System.currentTimeMillis()
            // Remove temp capture files older than 30 minutes
            for (f in files) {
                if (now - f.lastModified() > 30 * 60 * 1000) {
                    f.delete()
                }
            }
        } catch (_: Throwable) {
            // Ignore background cleanup errors
        }
    }

    /**
     * Robustly decodes ultra-high resolution camera photos (48MP, 108MP, 200MP+) safely into memory:
     * 1. Decodes only image bounds (zero memory allocation).
     * 2. Calculates power-of-2 inSampleSize sub-sampling to prevent OutOfMemoryError.
     * 3. Handles EXIF orientation so rotated camera photos are right-side-up.
     * 4. Downscales smoothly to sharp AI OCR resolution (MAX_AI_DIMENSION = 1536px).
     */
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

            val rawWidth = options.outWidth
            val rawHeight = options.outHeight
            if (rawWidth <= 0 || rawHeight <= 0) {
                AppLogger.e(TAG, "Invalid image dimensions: $rawWidth x $rawHeight")
                return null
            }

            // 2. Compute power-of-2 sub-sampling sample size
            var inSampleSize = 1
            val maxRawDim = maxOf(rawWidth, rawHeight)
            while (maxRawDim / (inSampleSize * 2) >= MAX_AI_DIMENSION) {
                inSampleSize *= 2
            }

            // 3. Decode sub-sampled bitmap safely into ARGB_8888 software memory
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inMutable = true
            }

            var decodedBitmap: Bitmap? = null
            contentResolver.openInputStream(uri)?.use { inputStream ->
                decodedBitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            }

            if (decodedBitmap == null) {
                AppLogger.e(TAG, "BitmapFactory failed to decode stream for uri: $uri")
                return null
            }

            // 4. Inspect EXIF orientation and rotate if necessary
            val orientation = getExifOrientation(context, uri)
            val orientedBitmap = applyExifOrientation(decodedBitmap!!, orientation)

            // 5. Ensure exact downscaling and software ARGB_8888 configuration
            ensureSoftwareBitmap(orientedBitmap)
        } catch (e: OutOfMemoryError) {
            AppLogger.e(TAG, "OutOfMemoryError loading high-res bitmap from uri: $uri, attempting fallback", e)
            System.gc()
            tryFallbackLowRes(context, uri)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to load and downscale bitmap from uri: $uri", e)
            null
        }
    }

    private fun getExifOrientation(context: Context, uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)
                exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (e: Exception) {
            AppLogger.w(TAG, "Could not read EXIF orientation: ${e.message}")
            ExifInterface.ORIENTATION_NORMAL
        }
    }

    private fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }

        return try {
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated != bitmap) {
                bitmap.recycle()
            }
            rotated
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to apply EXIF matrix rotation: ${e.message}")
            bitmap
        }
    }

    private fun tryFallbackLowRes(context: Context, uri: Uri): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inSampleSize = 8
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }
        } catch (e: Throwable) {
            AppLogger.e(TAG, "Fallback low-res decoding also failed", e)
            null
        }
    }

    fun ensureSoftwareBitmap(bitmap: Bitmap): Bitmap {
        return try {
            val maxDim = maxOf(bitmap.width, bitmap.height)
            if (maxDim > MAX_AI_DIMENSION) {
                val ratio = MAX_AI_DIMENSION.toFloat() / maxDim
                val targetWidth = (bitmap.width * ratio).toInt().coerceAtLeast(1)
                val targetHeight = (bitmap.height * ratio).toInt().coerceAtLeast(1)
                val scaled = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
                if (scaled != bitmap && !bitmap.isRecycled) {
                    bitmap.recycle()
                }
                scaled
            } else if (bitmap.config == Bitmap.Config.HARDWARE || !bitmap.isMutable) {
                bitmap.copy(Bitmap.Config.ARGB_8888, true) ?: bitmap
            } else {
                bitmap
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error ensuring software bitmap", e)
            bitmap
        }
    }

    /**
     * Saves an optimized compressed reference version of the captured recipe photo
     * to internal storage for long-term reference and visual comparison with transcribed recipe steps.
     */
    fun saveLowResReferenceImage(context: Context, bitmap: Bitmap): String? {
        return try {
            val photosDir = File(context.filesDir, "recipe_photos").apply {
                if (!exists()) mkdirs()
            }
            val filename = "recipe_ref_${System.currentTimeMillis()}.jpg"
            val file = File(photosDir, filename)

            val maxDim = maxOf(bitmap.width, bitmap.height)
            val finalBmp = if (maxDim > LOW_RES_MAX_DIMENSION) {
                val ratio = LOW_RES_MAX_DIMENSION.toFloat() / maxDim
                val targetWidth = (bitmap.width * ratio).toInt().coerceAtLeast(1)
                val targetHeight = (bitmap.height * ratio).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
            } else {
                bitmap
            }

            FileOutputStream(file).use { outStream ->
                finalBmp.compress(Bitmap.CompressFormat.JPEG, 85, outStream)
                outStream.flush()
            }

            if (finalBmp != bitmap && !finalBmp.isRecycled) {
                finalBmp.recycle()
            }

            file.absolutePath
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to save reference image", e)
            null
        }
    }

    /**
     * Copies and safely processes an image from any content/gallery Uri into the persistent dish_photos folder.
     */
    fun saveImageFromUri(context: Context, uri: Uri): String? {
        return try {
            val bitmap = loadAndDownscaleBitmap(context, uri) ?: return null
            val photosDir = File(context.filesDir, "dish_photos").apply {
                if (!exists()) mkdirs()
            }
            val filename = "custom_dish_${System.currentTimeMillis()}.jpg"
            val file = File(photosDir, filename)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                out.flush()
            }
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
            file.absolutePath
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to save image from URI: ${e.message}", e)
            null
        }
    }

    /**
     * Rotates a bitmap by a specified number of degrees (e.g. 90, 180, 270).
     */
    fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        if (degrees == 0f) return bitmap
        return try {
            val matrix = Matrix().apply { postRotate(degrees) }
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            rotated
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to rotate bitmap by $degrees degrees: ${e.message}")
            bitmap
        }
    }

    /**
     * Rotates an on-disk image file in-place by a specified number of degrees (e.g. 90, 180, 270).
     */
    fun rotateImageFile(filePath: String, degrees: Float): Boolean {
        if (degrees == 0f || filePath.isBlank()) return false
        return try {
            val file = File(filePath)
            if (!file.exists()) return false
            val bmp = BitmapFactory.decodeFile(filePath) ?: return false
            val rotated = rotateBitmap(bmp, degrees)
            FileOutputStream(file).use { out ->
                rotated.compress(Bitmap.CompressFormat.JPEG, 90, out)
                out.flush()
            }
            if (rotated != bmp && !rotated.isRecycled) {
                rotated.recycle()
            }
            if (!bmp.isRecycled) {
                bmp.recycle()
            }
            true
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to rotate image file $filePath: ${e.message}")
            false
        }
    }

    /**
     * Crops and saves ONLY the detected dish/food photo from the scanned page.
     * Prevents raw handwritten text pages from being smeared onto recipe cards.
     */
    fun cropAndSaveFoodPhoto(
        context: Context,
        sourceBitmap: Bitmap,
        ymin: Int,
        xmin: Int,
        ymax: Int,
        xmax: Int
    ): String? {
        return try {
            val bmpWidth = sourceBitmap.width
            val bmpHeight = sourceBitmap.height

            val left = ((xmin.coerceIn(0, 1000) / 1000f) * bmpWidth).toInt().coerceIn(0, bmpWidth - 1)
            val top = ((ymin.coerceIn(0, 1000) / 1000f) * bmpHeight).toInt().coerceIn(0, bmpHeight - 1)
            val right = ((xmax.coerceIn(0, 1000) / 1000f) * bmpWidth).toInt().coerceIn(left + 1, bmpWidth)
            val bottom = ((ymax.coerceIn(0, 1000) / 1000f) * bmpHeight).toInt().coerceIn(top + 1, bmpHeight)

            val cropWidth = right - left
            val cropHeight = bottom - top

            if (cropWidth < 40 || cropHeight < 40) {
                AppLogger.w(TAG, "Cropped food bounding box too small: ${cropWidth}x${cropHeight}")
                return null
            }

            val croppedBmp = Bitmap.createBitmap(sourceBitmap, left, top, cropWidth, cropHeight)
            val photosDir = File(context.filesDir, "dish_photos").apply {
                if (!exists()) mkdirs()
            }
            val filename = "dish_${System.currentTimeMillis()}.jpg"
            val file = File(photosDir, filename)

            FileOutputStream(file).use { outStream ->
                croppedBmp.compress(Bitmap.CompressFormat.JPEG, 90, outStream)
                outStream.flush()
            }

            if (croppedBmp != sourceBitmap && !croppedBmp.isRecycled) {
                croppedBmp.recycle()
            }

            file.absolutePath
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to crop food photo", e)
            null
        }
    }

    /**
     * Safely decodes a bitmap from a file with automatic memory subsampling to prevent OOM errors.
     */
    fun decodeSampledBitmapFromFile(filePath: String, reqWidth: Int = 1024, reqHeight: Int = 1024): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(filePath, options)
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            BitmapFactory.decodeFile(filePath, options)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Safely decodes a bitmap from byte array with automatic memory subsampling.
     */
    fun decodeSampledBitmapFromBytes(bytes: ByteArray, reqWidth: Int = 1024, reqHeight: Int = 1024): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        } catch (_: Exception) {
            null
        }
    }


    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

}
