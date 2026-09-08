package com.devpro.sound.ui.discover

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
private val PanelBackground = Color(0xE61B1B22)
private val MutedText = Color(0xFF9B9BA3)

@Composable
fun DiscoverRoute(
    viewModel: NowPlayingViewModel,
    onMiniPlayerClick: () -> Unit = {}
) {
    DiscoverScreen(
        uiState = viewModel.uiState,
        onSongClick = viewModel::onSongClick,
        onPlayPauseClick = viewModel::onPlayPauseClick,
        onMiniPlayerClick = onMiniPlayerClick
    )
}

@Composable
fun DiscoverScreen(
    uiState: NowPlayingUiState,
    onSongClick: (Song) -> Unit,
    onPlayPauseClick: () -> Unit,
    onMiniPlayerClick: () -> Unit = {}
) {
    val songs = uiState.songs
    val currentSong = uiState.song ?: songs.firstOrNull()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
    ) {
        when {
            uiState.isLoading -> {
                LoadingContent()
            }

            uiState.errorMessage != null -> {
                MessageContent(text = uiState.errorMessage)
            }

            songs.isEmpty() -> {
                MessageContent(text = "Chưa có bài hát")
            }

            else -> {
                DiscoverContent(
                    songs = songs,
                    currentSong = currentSong,
                    isPlaying = uiState.isPlaying,
                    onSongClick = onSongClick,
                    onPlayPauseClick = onPlayPauseClick,
                    onMiniPlayerClick = onMiniPlayerClick
                )
            }
        }
    }
}

@Composable
private fun DiscoverContent(
    songs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onSongClick: (Song) -> Unit,
    onPlayPauseClick: () -> Unit,
    onMiniPlayerClick: () -> Unit = {}
) {
    val featuredSongs = remember(songs) { songs.take(6) }
    val topSongs = remember(songs) { songs.take(12) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 190.dp)
        ) {
            item {
                DiscoverHeader()
            }

            item {
                FeaturedSongs(
                    songs = featuredSongs,
                    onSongClick = onSongClick
                )
            }

            item {
                Spacer(modifier = Modifier.height(34.dp))
                Text(
                    text = "Top Songs",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 28.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            items(
                items = topSongs,
                key = { song -> song.id },
                contentType = { "top_song" }
            ) { song ->
                TopSongItem(
                    song = song,
                    onSongClick = onSongClick
                )
            }
        }

        currentSong?.let { song ->
            MiniPlayer(
                song = song,
                isPlaying = isPlaying,
                onPlayPauseClick = onPlayPauseClick,
                onClick = onMiniPlayerClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 28.dp)
                    .padding(bottom = 5.dp)
            )
        }
    }
}

@Composable
private fun DiscoverHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 28.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Discover",
            color = Color.White,
            fontSize = 44.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = Color.White,
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
private fun FeaturedSongs(
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(horizontal = 28.dp)
    ){
        items(songs, key = {song -> song.id}){song ->
            FeaturedSongCard(
                song = song,
                onClick = { onSongClick(song) }
            )
        }
    }
}

@Composable
private fun FeaturedSongCard(
    song: Song,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(280.dp)
            .height(390.dp)
            .clip(RoundedCornerShape(34.dp))
            .clickable(onClick = onClick)
    ) {
        SongCoverImage(
            coverUrl = song.coverUrl,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.18f),
                            Color.Black.copy(alpha = 0.82f)
                        )
                    )
                )
        )

        Text(
            text = song.title,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 22.dp, end = 22.dp, bottom = 104.dp)
        )

        FeaturedActions(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        )
    }
}

@Composable
private fun FeaturedActions(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White.copy(alpha = 0.28f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayButton(size = 58.dp, iconSize = 36.dp, onClick = {})
        StatIcon(icon = Icons.Default.PlayArrow, value = "243")
        VerticalDivider()
        StatIcon(icon = Icons.Default.CloudDownload, value = "243")
        VerticalDivider()
        StatIcon(icon = Icons.Default.Favorite, value = "193")
    }
}

@Composable
private fun StatIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .height(42.dp)
            .width(1.dp)
            .background(Color.White.copy(alpha = 0.24f))
    )
}

@Composable
private fun TopSongItem(
    song: Song,
    onSongClick: (Song) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSongClick(song) }
            .padding(horizontal = 28.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SongCoverImage(
            coverUrl = song.coverUrl,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                color = MutedText,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.Default.MoreHoriz,
            contentDescription = "More",
            tint = MutedText,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
private fun MiniPlayer(
    song: Song,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .background(PanelBackground)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SongCoverImage(
            coverUrl = song.coverUrl,
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(14.dp))
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        PlayButton(
            size = 44.dp,
            iconSize = 30.dp,
            isPlaying = isPlaying,
            onClick = onPlayPauseClick
        )

        Spacer(modifier = Modifier.width(18.dp))

        Icon(
            imageVector = Icons.Default.SkipNext,
            contentDescription = "Next",
            tint = MutedText,
            modifier = Modifier.size(34.dp)
        )
    }
}

@Composable
private fun PlayButton(
    size: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp,
    isPlaying: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = Color.Black,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color.White)
    }
}

@Composable
private fun MessageContent(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun DiscoverScreenPreview() {
    val songs = listOf(
        Song(
            id = "song_1",
            title = "Ekon Tui Kita Korte",
            artist = "Andu Bandu",
            currentTime = "01:32",
            duration = "03:20",
            coverResId = R.drawable.ic_launcher_background
        ),
        Song(
            id = "song_2",
            title = "Matio Na",
            artist = "Aurora Rhythms",
            currentTime = "00:18",
            duration = "04:12",
            coverResId = R.drawable.ic_launcher_background
        )
    )

    MaterialTheme {
        DiscoverScreen(
            uiState = NowPlayingUiState(
                song = songs.first(),
                songs = songs
            ),
            onSongClick = {},
            onPlayPauseClick = {}
        )
    }
}
