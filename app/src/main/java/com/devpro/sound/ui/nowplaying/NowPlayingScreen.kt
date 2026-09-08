package com.devpro.sound.ui.nowplaying

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devpro.sound.R
import com.devpro.sound.data.model.Song
import com.devpro.sound.ui.components.SongCoverImage
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun NowPlayingRoute(
    viewModel: NowPlayingViewModel
) {
    NowPlayingScreen(
        uiState = viewModel.uiState,
        onPlayPauseClick = viewModel::onPlayPauseClick,
        onNextClick = viewModel::onNextClick,
        onPrevious = viewModel::onPreviousClick
    )
}

@Composable
fun NowPlayingScreen(
    uiState: NowPlayingUiState,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPrevious: () -> Unit
) {
    val song = uiState.song

    when {
        uiState.isLoading -> {
            ScreenMessage(text = "Đang tải bài hát...", showLoading = true)
            return
        }

        uiState.errorMessage != null -> {
            ScreenMessage(text = uiState.errorMessage, showLoading = false)
            return
        }

        song == null -> {
            ScreenMessage(text = "Chưa có bài hát", showLoading = false)
            return
        }
    }
    val offsetX = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val screenWidthPx = with(LocalDensity.current) {
        LocalConfiguration.current.screenWidthDp.dp.toPx()
    }
    val currentIndex = uiState.songs.indexOfFirst { it.id == song.id }
    val nextSong = uiState.songs.getOrNull(currentIndex + 1)
    val previousSong = uiState.songs.getOrNull(currentIndex - 1)
    val backGroundSong = when {
        offsetX.value < 0 -> nextSong
        offsetX.value > 0 -> previousSong
        else -> null
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        backGroundSong?.let { behindSong ->
            NowPlayingContent(
                song = behindSong,
                uiState = uiState,
                onPlayPauseClick = onPlayPauseClick,
                onNextClick = onNextClick,
                onPrevious = onPrevious,
                modifier = Modifier.fillMaxSize()
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(currentIndex) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                when {
                                    offsetX.value < -220f && nextSong != null -> {
                                        offsetX.animateTo(-screenWidthPx, animationSpec = tween(220))
                                        onNextClick()
                                        offsetX.snapTo(0f)
                                    }

                                    offsetX.value > 220f && previousSong != null -> {
                                        offsetX.animateTo(screenWidthPx, animationSpec = tween(220))
                                        onPrevious()
                                        offsetX.snapTo(0f)
                                    }

                                    else -> {
                                        offsetX.animateTo(0f, animationSpec = tween(220))
                                    }
                                }
                            }
                        }
                    )
                    { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount)
                        }
                    }
                }
        ) {
            NowPlayingContent(
                song = song,
                uiState = uiState,
                onPlayPauseClick = onPlayPauseClick,
                onNextClick = onNextClick,
                onPrevious = onPrevious,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

}

@Composable
private fun ScreenMessage(text: String, showLoading: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (showLoading) {
                CircularProgressIndicator(color = Color.Black)
                Spacer(modifier = Modifier.height(16.dp))
            }
            Text(
                text = text,
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NowPlayingTopBar(contentColor: Color = Color.White) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.back),
            contentDescription = "Back",
            modifier = Modifier.size(28.dp),
            colorFilter = ColorFilter.tint(contentColor)
        )

        Text(
            text = "Now Playing",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = contentColor
        )

        Image(
            painter = painterResource(id = R.drawable.more),
            contentDescription = "More",
            modifier = Modifier.size(28.dp),
            colorFilter = ColorFilter.tint(contentColor)
        )
    }
}
@Composable
private fun NowPlayingContent(
    song: Song,
    uiState: NowPlayingUiState,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        FullScreenAlbumBackground(coverUrl = song.coverUrl)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.48f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NowPlayingTopBar()

            Spacer(modifier = Modifier.weight(1f))

            SongInfo(
                title = song.title,
                artist = song.artist,
                titleColor = Color.White,
                artistColor = Color.White.copy(alpha = 0.78f)
            )

            Spacer(modifier = Modifier.height(38.dp))
            WaveformProgress(
                uiState = uiState,
                textColor = Color.White,
                inactiveTextColor = Color.White.copy(alpha = 0.72f),
                playedBarColor = Color.White,
                remainingBarColor = Color.White.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(52.dp))
            PlayerControls(
                isPlaying = uiState.isPlaying,
                onPlayPauseClick = onPlayPauseClick,
                onNextClick = onNextClick,
                onPreviousClick = onPrevious,
                iconColor = Color.White
            )

            Spacer(modifier = Modifier.height(54.dp))
            SongsHandle(contentColor = Color.White)
        }
    }
}
@Composable
private fun FullScreenAlbumBackground(coverUrl: String?) {
    SongCoverImage(
        coverUrl = coverUrl,
        contentDescription = "Cover",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun SongInfo(
    title: String,
    artist: String,
    titleColor: Color = Color.Black,
    artistColor: Color = Color(0xFF77787E)
) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .basicMarquee(iterations = Int.MAX_VALUE),
        color = titleColor,
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = artist,
        color = artistColor,
        fontSize = 18.sp
    )
}
private fun formatTime(timeMs: Long): String {
    if (timeMs <= 0) return "00:00"

    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return "%02d:%02d".format(minutes, seconds)
}
@Composable
private fun WaveformProgress(
    uiState: NowPlayingUiState,
    textColor: Color = Color.Black,
    inactiveTextColor: Color = Color(0xFF77787E),
    playedBarColor: Color = Color.Black,
    remainingBarColor: Color = Color(0xFFE5E5E5)
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatTime(uiState.currentPositionMs),
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Waveform(
            playedBarColor = playedBarColor,
            remainingBarColor = remainingBarColor,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        )

        Text(
            text = formatTime(uiState.durationMs),
            color = inactiveTextColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun Waveform(
    modifier: Modifier = Modifier,
    playedBarColor: Color = Color.Black,
    remainingBarColor: Color = Color(0xFFE5E5E5)
) {
    val bars = listOf(18, 26, 34, 42, 28, 36, 24, 32, 40, 50, 36, 46, 58, 34, 28, 40, 32, 26, 34, 30, 24, 28)

    Row(
        modifier = modifier.widthIn(max = 260.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        bars.forEachIndexed { index, height ->
            val color = when {
                index == 12 -> Color(0xFFFFDF34)
                index < 12 -> playedBarColor
                else -> remainingBarColor
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .width(4.dp)
                    .height(height.dp)
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
    }
}

@Composable
private fun PlayerControls(
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    iconColor: Color = Color.Black
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 64.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.rewind),
            contentDescription = "Previous",
            modifier = Modifier.size(30.dp).clickable(onClick =  onPreviousClick),
            colorFilter = ColorFilter.tint(iconColor)
        )

        Box(
            modifier = Modifier
                .size(74.dp)
                .clickable(onClick = onPlayPauseClick),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = if (isPlaying) R.drawable.pause else R.drawable.resume),
                contentDescription = "Play/Pause",
                modifier = Modifier.size(55.dp).clickable(onClick = onPlayPauseClick)
            )
        }

        Image(
            painter = painterResource(id = R.drawable.fastforwad),
            contentDescription = "Next",
            modifier = Modifier.size(30.dp).clickable(onClick = onNextClick),
            colorFilter = ColorFilter.tint(iconColor)
        )
    }
}

@Composable
private fun SongsHandle(contentColor: Color = Color.Black) {
    Text(
        text = "^",
        color = contentColor.copy(alpha = 0.72f),
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "SONGS",
        color = contentColor,
        fontSize = 14.sp,
        fontWeight = FontWeight.ExtraBold
    )
}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun NowPlayingScreenPreview() {
    MaterialTheme {
        NowPlayingScreen(
            uiState = NowPlayingUiState(
                song = Song(
                    title = "Chúng Ta Không Thuộc Về Nhau",
                    artist = "Sơn Tùng M-TP",
                    currentTime = "01:32",
                    duration = "03:20",
                    coverResId = R.drawable.ic_launcher_background,
                    audioPath = "music/vpop/chung_ta_khong_thuoc_ve_nhau.mp3",
                    genre = "V-Pop",
                    year = "2026"
                )
            ),
            onPlayPauseClick = {},
            onNextClick = {},
            onPrevious = {}
        )
    }
}
