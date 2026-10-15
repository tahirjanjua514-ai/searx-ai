package com.example

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.SearxTopBar
import com.example.ui.screens.AiHubScreen
import com.example.ui.screens.DownloaderScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.SearxSearchScreen
import com.example.ui.screens.VideoPlayerDialog
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SurfaceDarkM3
import com.example.ui.viewmodel.SearxViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: SearxViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleShareIntent(intent)

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShareIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        checkClipboardForMediaLink()
    }

    private fun handleShareIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrBlank()) {
                val urlRegex = Regex("(https?://\\S+)")
                val match = urlRegex.find(sharedText)
                val extractedUrl = match?.value ?: sharedText.trim()
                viewModel.onUrlChange(extractedUrl)
                viewModel.setTab(0)
                viewModel.inspectUrl(extractedUrl)
            }
        }
    }

    private fun checkClipboardForMediaLink() {
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).text?.toString() ?: ""
                val lower = text.lowercase().trim()
                if (lower.startsWith("http://") || lower.startsWith("https://")) {
                    if (lower.contains("youtube.com") || lower.contains("youtu.be") ||
                        lower.contains("tiktok.com") || lower.contains("instagram.com") ||
                        lower.contains("facebook.com") || lower.contains("fb.watch") ||
                        lower.endsWith(".mp4") || lower.endsWith(".mp3")
                    ) {
                        viewModel.onDetectedClipboard(text.trim())
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore clipboard permission errors
        }
    }
}

@Composable
fun MainAppContent(viewModel: SearxViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()

    // Handle system back navigation when not on home tab
    if (selectedTab != 0) {
        BackHandler {
            viewModel.setTab(0)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SearxTopBar()
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .background(SurfaceDarkM3)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = SurfaceDarkM3,
                tonalElevation = 8.dp
            ) {
                val tabs = listOf(
                    Triple(0, "Downloader", Pair(Icons.Filled.Download, Icons.Outlined.Download)),
                    Triple(1, "SearX Search", Pair(Icons.Filled.Search, Icons.Outlined.Search)),
                    Triple(2, "AI Hub", Pair(Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)),
                    Triple(3, "Library", Pair(Icons.Filled.VideoLibrary, Icons.Outlined.VideoLibrary))
                )

                tabs.forEach { (index, title, icons) ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setTab(index) },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) icons.first else icons.second,
                                contentDescription = title
                            )
                        },
                        label = {
                            Text(
                                text = title,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = CyanGlow,
                            indicatorColor = CyanGlow,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("tab_item_$index")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> DownloaderScreen(viewModel = viewModel)
                1 -> SearxSearchScreen(viewModel = viewModel)
                2 -> AiHubScreen(viewModel = viewModel)
                3 -> LibraryScreen(viewModel = viewModel)
            }
        }
    }

    // In-app video preview player dialog
    if (playerState != null) {
        VideoPlayerDialog(
            playerState = playerState!!,
            onDismiss = { viewModel.dismissPlayer() }
        )
    }
}
