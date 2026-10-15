package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.InstagramPurple
import com.example.ui.theme.TikTokPink
import com.example.ui.theme.YouTubeRed

enum class PlatformType(
    val displayName: String,
    val brandColor: Color,
    val iconKey: String
) {
    ALL("All Platforms", Color(0xFF00F0FF), "globe"),
    YOUTUBE("YouTube", YouTubeRed, "youtube"),
    TIKTOK("TikTok", TikTokPink, "tiktok"),
    INSTAGRAM("Instagram", InstagramPurple, "instagram"),
    FACEBOOK("Facebook", FacebookBlue, "facebook"),
    GENERIC("Web Media", Color(0xFF38BDF8), "link");

    companion object {
        fun fromUrl(url: String): PlatformType {
            val lower = url.lowercase()
            return when {
                lower.contains("youtube.com") || lower.contains("youtu.be") -> YOUTUBE
                lower.contains("tiktok.com") -> TIKTOK
                lower.contains("instagram.com") -> INSTAGRAM
                lower.contains("facebook.com") || lower.contains("fb.watch") || lower.contains("fb.com") -> FACEBOOK
                else -> GENERIC
            }
        }
    }
}

enum class DownloadQuality(
    val label: String,
    val ext: String,
    val approxSize: String,
    val isAudioOnly: Boolean = false
) {
    HD_1080P("1080p FHD", "mp4", "~28 MB"),
    SD_720P("720p HD", "mp4", "~15 MB"),
    SD_480P("480p SD", "mp4", "~8 MB"),
    AUDIO_MP3("Audio MP3", "mp3", "~4 MB", isAudioOnly = true)
}

data class ParsedMedia(
    val id: String,
    val originalUrl: String,
    val title: String,
    val author: String,
    val durationText: String,
    val thumbnailUrl: String,
    val platform: PlatformType,
    val availableQualities: List<DownloadQuality> = DownloadQuality.values().toList(),
    val directStreamUrl: String,
    val viewsCount: String = "128K views",
    val likesCount: String = "14.2K likes"
)

data class AiInsight(
    val summary: String,
    val viralScore: Int,
    val suggestedHooks: List<String>,
    val hashtags: List<String>,
    val targetAudience: String,
    val keyTakeaways: List<String>
)

data class SearxSearchResult(
    val id: String,
    val title: String,
    val platform: PlatformType,
    val creator: String,
    val thumbnailUrl: String,
    val url: String,
    val duration: String,
    val views: String
)
