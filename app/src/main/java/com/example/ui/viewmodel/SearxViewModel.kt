package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.DownloadEntity
import com.example.data.model.AiInsight
import com.example.data.model.DownloadQuality
import com.example.data.model.ParsedMedia
import com.example.data.model.PlatformType
import com.example.data.model.SearxSearchResult
import com.example.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlayerState(
    val isPlaying: Boolean = false,
    val title: String = "",
    val videoUrl: String = "",
    val thumbnailUrl: String = ""
)

class SearxViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MediaRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = MediaRepository(application, db.downloadDao())
    }

    val allDownloads: StateFlow<List<DownloadEntity>> = repository.allDownloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _inputUrl = MutableStateFlow("")
    val inputUrl: StateFlow<String> = _inputUrl.asStateFlow()

    private val _detectedClipboard = MutableStateFlow<String?>(null)
    val detectedClipboard: StateFlow<String?> = _detectedClipboard.asStateFlow()

    private val _selectedQuality = MutableStateFlow(DownloadQuality.HD_1080P)
    val selectedQuality: StateFlow<DownloadQuality> = _selectedQuality.asStateFlow()

    private val _isResolving = MutableStateFlow(false)
    val isResolving: StateFlow<Boolean> = _isResolving.asStateFlow()

    private val _resolveError = MutableStateFlow<String?>(null)
    val resolveError: StateFlow<String?> = _resolveError.asStateFlow()

    private val _activeMedia = MutableStateFlow<ParsedMedia?>(null)
    val activeMedia: StateFlow<ParsedMedia?> = _activeMedia.asStateFlow()

    private val _aiInsight = MutableStateFlow<AiInsight?>(null)
    val aiInsight: StateFlow<AiInsight?> = _aiInsight.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchPlatformFilter = MutableStateFlow(PlatformType.ALL)
    val searchPlatformFilter: StateFlow<PlatformType> = _searchPlatformFilter.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearxSearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearxSearchResult>> = _searchResults.asStateFlow()

    private val _playerState = MutableStateFlow<PlayerState?>(null)
    val playerState: StateFlow<PlayerState?> = _playerState.asStateFlow()

    val curatedSamples: List<ParsedMedia> = repository.getSampleClips()

    init {
        // Initial SearX results
        _searchResults.value = repository.searchSearx("", PlatformType.ALL)
        // Set first curated sample as default preview for rich first launch experience
        _activeMedia.value = curatedSamples.firstOrNull()
    }

    fun setTab(index: Int) {
        _selectedTab.value = index
    }

    fun onUrlChange(newUrl: String) {
        _inputUrl.value = newUrl
        _resolveError.value = null
    }

    fun onDetectedClipboard(url: String) {
        val lower = url.lowercase()
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            _detectedClipboard.value = url
        }
    }

    fun applyDetectedClipboard() {
        val detected = _detectedClipboard.value ?: return
        _inputUrl.value = detected
        _detectedClipboard.value = null
        inspectUrl(detected)
    }

    fun dismissDetectedClipboard() {
        _detectedClipboard.value = null
    }

    fun setSelectedQuality(quality: DownloadQuality) {
        _selectedQuality.value = quality
    }

    fun selectCuratedMedia(media: ParsedMedia) {
        _activeMedia.value = media
        _inputUrl.value = media.originalUrl
        _resolveError.value = null
        _aiInsight.value = null
    }

    fun inspectUrl(targetUrl: String? = null) {
        val url = targetUrl ?: _inputUrl.value
        if (url.isBlank()) {
            _resolveError.value = "Please enter or paste a valid video URL."
            return
        }

        viewModelScope.launch {
            _isResolving.value = true
            _resolveError.value = null
            _aiInsight.value = null
            try {
                val resolved = repository.resolveMedia(url)
                _activeMedia.value = resolved
            } catch (e: Exception) {
                _resolveError.value = "Failed to resolve link: ${e.message}"
            } finally {
                _isResolving.value = false
            }
        }
    }

    fun startDownload(targetQuality: DownloadQuality? = null) {
        val media = _activeMedia.value ?: return
        val quality = targetQuality ?: _selectedQuality.value

        viewModelScope.launch {
            repository.startDownload(media, quality, viewModelScope)
            // Switch to library tab or notify
        }
    }

    fun runAiAnalysis(media: ParsedMedia? = null) {
        val target = media ?: _activeMedia.value ?: return
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val insight = repository.analyzeWithAi(target)
                _aiInsight.value = insight
                _selectedTab.value = 2 // Switch to AI Insights tab
            } catch (e: Exception) {
                // Heuristic fallback already implemented in repo
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _searchResults.value = repository.searchSearx(query, _searchPlatformFilter.value)
    }

    fun setSearchPlatformFilter(platform: PlatformType) {
        _searchPlatformFilter.value = platform
        _searchResults.value = repository.searchSearx(_searchQuery.value, platform)
    }

    fun inspectSearchResult(item: SearxSearchResult) {
        _inputUrl.value = item.url
        _selectedTab.value = 0 // Switch to downloader tab
        inspectUrl(item.url)
    }

    fun deleteDownload(id: Long) {
        viewModelScope.launch {
            repository.deleteDownload(id)
        }
    }

    fun clearAllDownloads() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun openFile(filePath: String?, isAudio: Boolean) {
        if (!filePath.isNullOrBlank()) {
            repository.openFile(filePath, isAudio)
        }
    }

    fun shareFile(filePath: String?, title: String) {
        if (!filePath.isNullOrBlank()) {
            repository.shareFile(filePath, title)
        }
    }

    fun playInApp(title: String, url: String, thumbnail: String) {
        _playerState.value = PlayerState(
            isPlaying = true,
            title = title,
            videoUrl = url,
            thumbnailUrl = thumbnail
        )
    }

    fun dismissPlayer() {
        _playerState.value = null
    }
}
