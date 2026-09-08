package com.devpro.sound.ui.downloads

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devpro.sound.R
import com.devpro.sound.data.model.Song
import com.devpro.sound.ui.components.SongCoverImage
import com.devpro.sound.ui.nowplaying.NowPlayingUiState
import com.devpro.sound.ui.nowplaying.NowPlayingViewModel

private val ScreenBackground = Color(0xFF101014)
private val PanelBackground = Color(0xFF1A1A22)
private val MutedText = Color(0xFF9B9BA3)
private val AccentYellow = Color(0xFFFFDF34)

@Composable
fun DownloadsRoute(
    viewModel: NowPlayingViewModel
) {
    DownloadsScreen(
        uiState = viewModel.uiState,
        onSongClick = viewModel::onSongClick
    )
}

@Composable
fun DownloadsScreen(
    uiState: NowPlayingUiState,
    onSongClick: (Song) -> Unit
) {
    val downloadedSongs = remember(uiState.songs) { uiState.songs.take(16) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 130.dp)
        ) {
            item {
                DownloadsHeader(downloadedCount = downloadedSongs.size)
                StorageCard()
                SectionTitle(title = "Downloaded Tracks")
            }

            when {
                uiState.isLoading -> item { LoadingContent() }
                uiState.errorMessage != null -> item { MessageContent(text = uiState.errorMessage) }
                downloadedSongs.isEmpty() -> item { MessageContent(text = "Chưa có bài tải xuống") }
                else -> {
                    items(
                        items = downloadedSongs,
                        key = { song -> song.id },
                        contentType = { "download_song" }
                    ) { song ->
                        DownloadSongItem(song = song, onSongClick = onSongClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadsHeader(downloadedCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Downloads", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
            Text("$downloadedCount tracks saved offline", color = MutedText, fontSize = 16.sp)
        }
        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
    }
}

@Composable
private fun StorageCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(PanelBackground)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Storage, contentDescription = null, tint = AccentYellow, modifier = Modifier.size(30.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Offline Storage", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Text("915 MB used from your synced library", color = MutedText, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.14f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.62f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(AccentYellow)
                )
            }
        }
    }
}

@Composable
private fun DownloadSongItem(song: Song, onSongClick: (Song) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSongClick(song) }
            .padding(horizontal = 24.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SongCoverImage(song.coverUrl, Modifier.size(66.dp).clip(RoundedCornerShape(18.dp)))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(song.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artist, color = MutedText, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Default.CheckCircle, contentDescription = "Downloaded", tint = AccentYellow, modifier = Modifier.size(26.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Icon(Icons.Default.MoreHoriz, contentDescription = "More", tint = MutedText, modifier = Modifier.size(30.dp))
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(text = title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 24.dp, top = 30.dp, bottom = 12.dp))
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color.White)
    }
}

@Composable
private fun MessageContent(text: String) {
    Text(text = text, color = MutedText, fontSize = 16.sp, modifier = Modifier.padding(24.dp))
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DownloadsScreenPreview() {
    val songs = listOf(
        Song("song_1", "Ekon Tui Kita Korte", "Andu Bandu", "01:32", "03:20", R.drawable.ic_launcher_background),
        Song("song_2", "Matio Na", "Aurora Rhythms", "00:18", "04:12", R.drawable.ic_launcher_background)
    )
    MaterialTheme {
        DownloadsScreen(uiState = NowPlayingUiState(song = songs.first(), songs = songs), onSongClick = {})
    }
}
