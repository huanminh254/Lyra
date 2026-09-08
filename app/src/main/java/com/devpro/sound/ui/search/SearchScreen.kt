package com.devpro.sound.ui.search

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
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
private val FieldBackground = Color(0xFF1D1D24)
private val PanelBackground = Color(0xFF191920)
private val MutedText = Color(0xFF9B9BA3)
private val AccentYellow = Color(0xFFFFDF34)

@Composable
fun SearchRoute(
    viewModel: NowPlayingViewModel,
    onBackClick: () -> Unit = {}
) {
    SearchScreen(
        uiState = viewModel.uiState,
        onSongClick = viewModel::onSongClick,
        onBackClick = onBackClick
    )
}

@Composable
fun SearchScreen(
    uiState: NowPlayingUiState,
    onSongClick: (Song) -> Unit,
    onBackClick: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val songs = uiState.songs
    val results = remember(query, songs) {
        if (query.isBlank()) {
            songs.take(10)
        } else {
            songs.filter { song ->
                song.title.contains(query, ignoreCase = true) ||
                    song.artist.contains(query, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            SearchHeader(onBackClick = onBackClick)
            SearchInput(
                query = query,
                onQueryChange = { query = it },
                onClearClick = { query = "" }
            )
        }

        when {
            uiState.isLoading -> {
                item { LoadingRow() }
            }

            uiState.errorMessage != null -> {
                item {
                    MessageRow(text = uiState.errorMessage)
                }
            }

            query.isBlank() -> {
                item {
                    RecentSearches()
                    SectionTitle(title = "Popular Tracks")
                }

                items(results, key = { song -> song.id }) { song ->
                    SearchSongItem(song = song, onSongClick = onSongClick)
                }
            }

            results.isEmpty() -> {
                item {
                    MessageRow(text = "Không tìm thấy bài hát")
                }
            }

            else -> {
                item {
                    SectionTitle(title = "${results.size} results")
                }

                items(results, key = { song -> song.id }) { song ->
                    SearchSongItem(song = song, onSongClick = onSongClick)
                }
            }
        }
    }
}

@Composable
private fun SearchHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.White,
            modifier = Modifier
                .size(32.dp)
                .clickable(onClick = onBackClick)
        )

        Spacer(modifier = Modifier.width(18.dp))

        Text(
            text = "Search",
            color = Color.White,
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun SearchInput(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(FieldBackground)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = MutedText,
            modifier = Modifier.size(26.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                Box {
                    if (query.isEmpty()) {
                        Text(
                            text = "Songs, artists, albums",
                            color = MutedText,
                            fontSize = 18.sp
                        )
                    }
                    innerTextField()
                }
            }
        )

        if (query.isNotEmpty()) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Clear",
                tint = MutedText,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onClearClick)
            )
        }
    }
}

@Composable
private fun RecentSearches() {
    Column {
        SectionTitle(title = "Recent Searches")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(listOf("V-Pop", "Sơn Tùng", "Remix", "Acoustic")) { label ->
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(PanelBackground)
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontSize = 24.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 10.dp)
    )
}

@Composable
private fun SearchSongItem(
    song: Song,
    onSongClick: (Song) -> Unit
) {
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
                .size(72.dp)
                .clip(RoundedCornerShape(18.dp))
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = song.artist,
                color = MutedText,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play",
            tint = AccentYellow,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Icon(
            imageVector = Icons.Default.MoreHoriz,
            contentDescription = "More",
            tint = MutedText,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
private fun LoadingRow() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color.White)
    }
}

@Composable
private fun MessageRow(text: String) {
    Text(
        text = text,
        color = MutedText,
        fontSize = 16.sp,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 36.dp)
    )
}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun SearchScreenPreview() {
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
        SearchScreen(
            uiState = NowPlayingUiState(
                song = songs.first(),
                songs = songs
            ),
            onSongClick = {},
            onBackClick = {}
        )
    }
}
