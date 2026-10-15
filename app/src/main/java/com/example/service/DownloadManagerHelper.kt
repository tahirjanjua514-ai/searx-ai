package com.example.service

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.data.model.DownloadQuality
import com.example.data.model.ParsedMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class DownloadManagerHelper(private val context: Context) {
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    // Sanitizes file name
    private fun sanitizeFileName(title: String, ext: String): String {
        val clean = title.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .take(50)
            .trim('_')
        return "SEARX_${clean}_${System.currentTimeMillis()}.$ext"
    }

    // Queues download in system DownloadManager
    fun enqueueSystemDownload(
        media: ParsedMedia,
        quality: DownloadQuality
    ): Long {
        val fileName = sanitizeFileName(media.title, quality.ext)
        val uri = Uri.parse(media.directStreamUrl)

        val request = DownloadManager.Request(uri).apply {
            setTitle("SEARX AI: ${media.title}")
            setDescription("Downloading from ${media.platform.displayName} ($quality.label)")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            setAllowedOverRoaming(true)
            try {
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            } catch (e: Exception) {
                setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
            }
        }

        return downloadManager.enqueue(request)
    }

    // Direct background download with live progress callback
    suspend fun directDownloadToFile(
        media: ParsedMedia,
        quality: DownloadQuality,
        onProgress: (Int) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        val fileName = sanitizeFileName(media.title, quality.ext)
        val targetDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir, "searx_media")
        if (!targetDir.exists()) targetDir.mkdirs()

        val destFile = File(targetDir, fileName)

        try {
            val req = Request.Builder().url(media.directStreamUrl).build()
            client.newCall(req).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body ?: return@withContext null
                val contentLength = body.contentLength()

                body.byteStream().use { input ->
                    FileOutputStream(destFile).use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead: Int
                        var totalRead = 0L

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalRead += bytesRead
                            if (contentLength > 0) {
                                val progress = ((totalRead * 100) / contentLength).toInt().coerceIn(0, 100)
                                onProgress(progress)
                            }
                        }
                        output.flush()
                    }
                }
            }
            onProgress(100)
            destFile
        } catch (e: Exception) {
            destFile.delete()
            null
        }
    }

    fun openMediaFile(context: Context, filePath: String, isAudio: Boolean) {
        val file = File(filePath)
        if (!file.exists()) return

        val uri: Uri = try {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            Uri.fromFile(file)
        }

        val mimeType = if (isAudio) "audio/*" else "video/*"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback chooser
            val chooser = Intent.createChooser(intent, "Open with")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    }

    fun shareMediaFile(context: Context, filePath: String, title: String) {
        val file = File(filePath)
        if (!file.exists()) return

        val uri: Uri = try {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            Uri.fromFile(file)
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = if (file.extension.equals("mp3", true)) "audio/*" else "video/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Downloaded via SEARX AI: $title")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(shareIntent, "Share Media via")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
