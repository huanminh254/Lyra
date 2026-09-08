package com.devpro.sound.ui.favorites

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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
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
private val MutedText = Color(0xFF9B9BA3)
private val AccentYellow = Color(0xFFFFDF34)

@Composable
fun FavoritesRoute(
    viewModel: NowPlayingViewModel
) {
    FavoritesScreen(
        uiState = viewModel.uiState,
        onSongClick = viewModel::onSongClick
    )
}

@Composable
fun FavoritesScreen(
    uiState: NowPlayingUiState,
    onSongClick: (Song) -> Unit
) {
    val favoriteSongs = remember(uiState.songs) { uiState.songs.take(18) }

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
                FavoritesHeader(count = favoriteSongs.size)
            }

            when {
                uiState.isLoading -> item { LoadingContent() }
                uiState.errorMessage != null -> item { MessageContent(text = uiState.errorMessage) }
                favoriteSongs.isEmpty() -> item { MessageContent(text = "Chưa có bài hát yêu thích") }
                else -> {
                    item {
                        FeaturedFavorite(song = favoriteSongs.first(), onSongClick = onSongClick)
                        SectionTitle(title = "Liked Tracks")
                    }

                    items(
                        items = favoriteSongs,
                        key = { song -> song.id },
                        contentType = { "favorite_song" }
                    ) { song ->
                        FavoriteSongItem(song = song, onSongClick = onSongClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoritesHeader(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Favorites",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "$count liked songs",
                color = MutedText,
                fontSize = 16.sp
            )
        }

        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = Color.White,
            modifier = Modifier.size(34.dp)
        )
    }
}

@Composable
private fun FeaturedFavorite(
    song: Song,
    onSongClick: (Song) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(30.dp))
            .clickable { onSongClick(song) }
    ) {
        SongCoverImage(coverUrl = song.coverUrl, modifier = Modifier.fillMaxSize())
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Black.copy(alpha = 0.88f), Color.Transparent)
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = AccentYellow,
                modifier = Modifier.size(34.dp)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = song.title,
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
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
    }
}

@Composable
private fun FavoriteSongItem(song: Song, onSongClick: (Song) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSongClick(song) }
            .padding(horizontal = 24.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SongCoverImage(
            coverUrl = song.coverUrl,
            modifier = Modifier
                .size(66.dp)
                .clip(RoundedCornerShape(18.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                color = MutedText,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Liked",
            tint = AccentYellow,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Icon(
            imageVector = Icons.Default.MoreHoriz,
            contentDescription = "More",
            tint = MutedText,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontSize = 24.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 30.dp, bottom = 12.dp)
    )
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
fun FavoritesScreenPreview() {
    val songs = previewSongs()
    MaterialTheme {
        FavoritesScreen(
            uiState = NowPlayingUiState(song = songs.first(), songs = songs),
            onSongClick = {}
        )
    }
}

private fun previewSongs(): List<Song> {
    return listOf(
        Song("song_1", "Ekon Tui Kita Korte", "Andu Bandu", "01:32", "03:20", R.drawable.ic_launcher_background),
        Song("song_2", "Matio Na", "Aurora Rhythms", "00:18", "04:12", R.drawable.ic_launcher_background),
        Song("song_3", "For Zeta Ichhakor", "Sound Cloud", "00:42", "02:58", R.drawable.ic_launcher_background)
    )
}
