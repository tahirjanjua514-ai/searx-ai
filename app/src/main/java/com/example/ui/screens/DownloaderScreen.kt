package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.DownloadQuality
import com.example.data.model.ParsedMedia
import com.example.data.model.PlatformType
import com.example.ui.components.PlatformBadge
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.SurfaceDarkM3
import com.example.ui.theme.SurfaceVariantM3
import com.example.ui.viewmodel.SearxViewModel

@Composable
fun DownloaderScreen(viewModel: SearxViewModel) {
    val context = LocalContext.current
    val inputUrl by viewModel.inputUrl.collectAsStateWithLifecycle()
    val isResolving by viewModel.isResolving.collectAsStateWithLifecycle()
    val resolveError by viewModel.resolveError.collectAsStateWithLifecycle()
    val activeMedia by viewModel.activeMedia.collectAsStateWithLifecycle()
    val selectedQuality by viewModel.selectedQuality.collectAsStateWithLifecycle()
    val detectedClipboard by viewModel.detectedClipboard.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero / Input Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDarkM3),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color(0x3300F0FF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("url_input_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Instant Media Downloader",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Download in FHD from YouTube, TikTok, Instagram & Facebook",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { viewModel.onUrlChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("link_input_field"),
                        placeholder = {
                            Text(
                                "Paste video link from YouTube, IG, TikTok, FB...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = CyanGlow
                            )
                        },
                        trailingIcon = {
                            Row {
                                if (inputUrl.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onUrlChange("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = Color.Gray
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = clipboard.primaryClip
                                        if (clip != null && clip.itemCount > 0) {
                                            val pasted = clip.getItemAt(0).text?.toString() ?: ""
                                            if (pasted.isNotBlank()) {
                                                viewModel.onUrlChange(pasted)
                                                viewModel.inspectUrl(pasted)
                                            }
                                        }
                                    },
                                    modifier = Modifier.testTag("paste_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = "Paste",
                                        tint = CyanGlow
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanGlow,
                            unfocusedBorderColor = Color(0xFF2C394F),
                            focusedContainerColor = SurfaceVariantM3,
                            unfocusedContainerColor = SurfaceVariantM3,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.inspectUrl() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("inspect_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanGlow),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isResolving
                    ) {
                        if (isResolving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Resolving Media...", color = Color.Black, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Inspect & Extract Video",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Detected Clipboard Banner
        if (detectedClipboard != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E283E)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ElectricViolet)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = null,
                            tint = ElectricViolet,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Video link found in clipboard",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = detectedClipboard ?: "",
                                fontSize = 11.sp,
                                color = Color(0xFFA5B4FC),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.applyDetectedClipboard() },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Inspect", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Resolve Error Banner
        if (resolveError != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x33EF4444)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF4444))
                ) {
                    Text(
                        text = resolveError ?: "",
                        color = Color(0xFFFCA5A5),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }

        // Active Media Card
        if (activeMedia != null) {
            val media = activeMedia!!
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDarkM3),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color(0x4400F0FF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inspected_media_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Thumbnail with Duration & Play Preview Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.Black)
                        ) {
                            AsyncImage(
                                model = media.thumbnailUrl,
                                contentDescription = media.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Top platform badge
                            Box(modifier = Modifier.padding(10.dp)) {
                                PlatformBadge(platform = media.platform)
                            }

                            // Center play button overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.65f))
                                    .clickable {
                                        viewModel.playInApp(media.title, media.directStreamUrl, media.thumbnailUrl)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Preview Video",
                                    tint = CyanGlow,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            // Bottom duration
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Black.copy(alpha = 0.75f),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = media.durationText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Title & Creator
                        Text(
                            text = media.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = media.author,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CyanGlow
                            )
                            Text(
                                text = "•",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = media.viewsCount,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quality Selection
                        Text(
                            text = "Select Download Quality:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DownloadQuality.values().forEach { quality ->
                                val isSelected = quality == selectedQuality
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) CyanGlow.copy(alpha = 0.15f) else SurfaceVariantM3,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) CyanGlow else Color(0xFF2C394F)
                                    ),
                                    modifier = Modifier.clickable { viewModel.setSelectedQuality(quality) }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = quality.label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) CyanGlow else Color.White
                                        )
                                        Text(
                                            text = quality.approxSize,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons: Download + AI Insights
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.startDownload() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("download_now_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanGlow),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Download",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            Button(
                                onClick = { viewModel.runAiAnalysis() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("ai_insights_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SEARX AI Hub",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section Title: Curated Trending Clips to test immediately
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔥 Trending Creator Clips",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Tap to Inspect",
                    fontSize = 11.sp,
                    color = CyanGlow
                )
            }
        }

        items(viewModel.curatedSamples) { sample ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDarkM3),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFF1E283E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectCuratedMedia(sample) }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black)
                    ) {
                        AsyncImage(
                            model = sample.thumbnailUrl,
                            contentDescription = sample.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.Black.copy(alpha = 0.8f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                        ) {
                            Text(
                                text = sample.durationText,
                                fontSize = 9.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        PlatformBadge(platform = sample.platform)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = sample.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${sample.author} • ${sample.viewsCount}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = { viewModel.selectCuratedMedia(sample) }) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Select",
                            tint = CyanGlow
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
