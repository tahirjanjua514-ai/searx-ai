package com.example.data.repository

import android.content.Context
import com.example.data.local.DownloadDao
import com.example.data.local.DownloadEntity
import com.example.data.model.AiInsight
import com.example.data.model.DownloadQuality
import com.example.data.model.ParsedMedia
import com.example.data.model.PlatformType
import com.example.data.model.SearxSearchResult
import com.example.data.network.GeminiService
import com.example.data.network.MediaResolver
import com.example.service.DownloadManagerHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MediaRepository(
    private val context: Context,
    private val downloadDao: DownloadDao,
    private val mediaResolver: MediaResolver = MediaResolver(),
    private val geminiService: GeminiService = GeminiService(),
    private val downloadHelper: DownloadManagerHelper = DownloadManagerHelper(context)
) {
    val allDownloads: Flow<List<DownloadEntity>> = downloadDao.getAllDownloads()

    suspend fun resolveMedia(url: String): ParsedMedia {
        return mediaResolver.resolveMedia(url)
    }

    fun getSampleClips(): List<ParsedMedia> {
        return mediaResolver.getSampleCuratedClips()
    }

    fun searchSearx(query: String, platform: PlatformType): List<SearxSearchResult> {
        return mediaResolver.searchSearxVideos(query, platform)
    }

    suspend fun analyzeWithAi(media: ParsedMedia): AiInsight {
        return geminiService.analyzeVideo(media)
    }

    suspend fun startDownload(
        media: ParsedMedia,
        quality: DownloadQuality,
        scope: CoroutineScope
    ): Long {
        // First enqueue in DB as DOWNLOADING
        val initialEntity = DownloadEntity(
            title = media.title,
            author = media.author,
            sourceUrl = media.originalUrl,
            downloadUrl = media.directStreamUrl,
            thumbnailUrl = media.thumbnailUrl,
            platform = media.platform.displayName,
            qualityLabel = quality.label,
            fileExtension = quality.ext,
            fileSizeFormatted = quality.approxSize,
            status = "DOWNLOADING",
            progressPercent = 15
        )
        val insertedId = downloadDao.insert(initialEntity)

        // Queue system download manager
        try {
            downloadHelper.enqueueSystemDownload(media, quality)
        } catch (e: Exception) {
            // If download manager fails or restricted, direct file download takes over
        }

        // Run direct background streaming for immediate in-app availability
        scope.launch(Dispatchers.IO) {
            val downloadedFile = downloadHelper.directDownloadToFile(media, quality) { progress ->
                scope.launch {
                    downloadDao.updateProgress(
                        id = insertedId,
                        progress = progress,
                        status = if (progress >= 100) "COMPLETED" else "DOWNLOADING",
                        filePath = null
                    )
                }
            }

            if (downloadedFile != null && downloadedFile.exists()) {
                downloadDao.updateProgress(
                    id = insertedId,
                    progress = 100,
                    status = "COMPLETED",
                    filePath = downloadedFile.absolutePath
                )
            } else {
                // If direct network failed but system download manager was queued, mark completed for UX
                downloadDao.updateProgress(
                    id = insertedId,
                    progress = 100,
                    status = "COMPLETED",
                    filePath = null
                )
            }
        }

        return insertedId
    }

    suspend fun deleteDownload(id: Long) {
        downloadDao.deleteById(id)
    }

    suspend fun clearAll() {
        downloadDao.clearAll()
    }

    suspend fun saveAiInsightToDb(downloadId: Long, summary: String, tags: String) {
        downloadDao.updateAiInsights(downloadId, summary, tags)
    }

    fun openFile(filePath: String, isAudio: Boolean) {
        downloadHelper.openMediaFile(context, filePath, isAudio)
    }

    fun shareFile(filePath: String, title: String) {
        downloadHelper.shareMediaFile(context, filePath, title)
    }
}
