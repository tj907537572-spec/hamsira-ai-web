package com.example.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.model.MediaAttachment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

private const val TAG = "MediaAttachmentHelper"
private const val MAX_INLINE_BYTES = 15L * 1024L * 1024L // 15 MB safe limit for Gemini inline_data
private const val MAX_IMAGE_DIMENSION = 1600

sealed interface AttachmentImportResult {
    data class Success(val attachment: MediaAttachment) : AttachmentImportResult
    data object FileTooLarge : AttachmentImportResult
    data object UnsupportedOrFailed : AttachmentImportResult
}

object MediaAttachmentHelper {

    fun createCameraOutputUri(context: Context, isVideo: Boolean): Pair<Uri, File> {
        val mediaDir = File(context.cacheDir, "media").apply { mkdirs() }
        val ext = if (isVideo) "mp4" else "jpg"
        val file = File(mediaDir, "capture_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.$ext")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return uri to file
    }

    suspend fun importFromUri(
        context: Context,
        sourceUri: Uri,
        forcedIsVideo: Boolean? = null
    ): AttachmentImportResult = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val detectedMime = resolver.getType(sourceUri)
            val isVideo = forcedIsVideo ?: (detectedMime?.startsWith("video/") == true)
            val mimeType = when {
                !detectedMime.isNullOrBlank() -> detectedMime
                isVideo -> "video/mp4"
                else -> "image/jpeg"
            }

            val attachmentsDir = File(context.filesDir, "attachments").apply { mkdirs() }
            val displayName = queryDisplayName(context, sourceUri)
                ?: if (isVideo) "video_${System.currentTimeMillis()}.mp4" else "photo_${System.currentTimeMillis()}.jpg"

            if (!isVideo) {
                val compressedBytes = compressImageIfNeeded(context, sourceUri)
                if (compressedBytes != null) {
                    if (compressedBytes.size > MAX_INLINE_BYTES) {
                        return@withContext AttachmentImportResult.FileTooLarge
                    }
                    val destFile = File(attachmentsDir, "img_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")
                    FileOutputStream(destFile).use { it.write(compressedBytes) }
                    return@withContext AttachmentImportResult.Success(
                        MediaAttachment(
                            uriString = Uri.fromFile(destFile).toString(),
                            mimeType = "image/jpeg",
                            isVideo = false,
                            displayName = displayName,
                            sizeBytes = destFile.length()
                        )
                    )
                }
            }

            // Video or fallback raw copy
            val rawSize = queryFileSize(context, sourceUri)
            if (rawSize > MAX_INLINE_BYTES) {
                return@withContext AttachmentImportResult.FileTooLarge
            }

            val ext = if (isVideo) "mp4" else "jpg"
            val destFile = File(attachmentsDir, "att_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.$ext")
            resolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    val buffer = ByteArray(8192)
                    var total = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        total += read
                        if (total > MAX_INLINE_BYTES) {
                            destFile.delete()
                            return@withContext AttachmentImportResult.FileTooLarge
                        }
                        output.write(buffer, 0, read)
                    }
                }
            } ?: return@withContext AttachmentImportResult.UnsupportedOrFailed

            if (destFile.length() == 0L) {
                destFile.delete()
                return@withContext AttachmentImportResult.UnsupportedOrFailed
            }

            AttachmentImportResult.Success(
                MediaAttachment(
                    uriString = Uri.fromFile(destFile).toString(),
                    mimeType = mimeType,
                    isVideo = isVideo,
                    displayName = displayName,
                    sizeBytes = destFile.length()
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import attachment from URI", e)
            AttachmentImportResult.UnsupportedOrFailed
        }
    }

    fun readBase64Bytes(uriString: String): String? {
        return try {
            val uri = Uri.parse(uriString)
            val file = if (uri.scheme == "file") uri.path?.let { File(it) } else null
            val bytes = if (file != null && file.exists()) {
                file.readBytes()
            } else {
                null
            }
            bytes?.let { Base64.encodeToString(it, Base64.NO_WRAP) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to encode attachment bytes to Base64", e)
            null
        }
    }

    fun loadThumbnailBitmap(context: Context, uriString: String, isVideo: Boolean): Bitmap? {
        return try {
            val uri = Uri.parse(uriString)
            if (isVideo) {
                val retriever = MediaMetadataRetriever()
                try {
                    if (uri.scheme == "file" && uri.path != null) {
                        retriever.setDataSource(uri.path)
                    } else {
                        retriever.setDataSource(context, uri)
                    }
                    retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                } finally {
                    retriever.release()
                }
            } else {
                val options = BitmapFactory.Options().apply { inSampleSize = 2 }
                if (uri.scheme == "file" && uri.path != null) {
                    BitmapFactory.decodeFile(uri.path, options)
                } else {
                    context.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it, null, options)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to decode thumbnail for $uriString", e)
            null
        }
    }

    fun loadFullBitmap(context: Context, uriString: String): Bitmap? {
        return try {
            val uri = Uri.parse(uriString)
            if (uri.scheme == "file" && uri.path != null) {
                BitmapFactory.decodeFile(uri.path)
            } else {
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to decode full bitmap for $uriString", e)
            null
        }
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0L) return "0 KB"
        val kb = bytes / 1024.0
        return if (kb < 1024.0) {
            "${kb.toInt().coerceAtLeast(1)} KB"
        } else {
            String.format("%.1f MB", kb / 1024.0)
        }
    }

    private fun compressImageIfNeeded(context: Context, uri: Uri): ByteArray? {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sampleSize = 1
        while (bounds.outWidth / sampleSize > MAX_IMAGE_DIMENSION * 2 ||
            bounds.outHeight / sampleSize > MAX_IMAGE_DIMENSION * 2
        ) {
            sampleSize *= 2
        }

        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, decodeOpts)
        } ?: return null

        val scaled = scaleBitmapDown(decoded, MAX_IMAGE_DIMENSION)
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
        if (scaled !== decoded) scaled.recycle()
        decoded.recycle()
        return out.toByteArray()
    }

    private fun scaleBitmapDown(bitmap: Bitmap, maxDim: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDim && height <= maxDim) return bitmap
        val ratio = minOf(maxDim.toFloat() / width, maxDim.toFloat() / height)
        val newW = (width * ratio).toInt().coerceAtLeast(1)
        val newH = (height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, newW, newH, true)
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun queryFileSize(context: Context, uri: Uri): Long {
        if (uri.scheme == "file" && uri.path != null) {
            return File(uri.path!!).length()
        }
        return try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getLong(0) else -1L
            } ?: -1L
        } catch (_: Exception) {
            -1L
        }
    }
}
