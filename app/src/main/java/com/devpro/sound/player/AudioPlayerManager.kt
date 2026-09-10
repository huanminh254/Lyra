package com.devpro.sound.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer

interface AudioPlayer {
    fun hasCurrentSong(): Boolean
    fun play(audioUrl: String)
    fun setPlayList(audioUrls: List<String>)
    fun playAt(index: Int)
    fun playNext()
    fun playPrevious()
    fun hasNext(): Boolean
    fun hasPrevious(): Boolean
    fun getCurrentSongIndex(): Int
    fun pause()
    fun resume()
    fun isPlaying(): Boolean
    fun seekTo(positionMs: Long)
    fun getCurrentPosition(): Long
    fun getDuration(): Long
    fun addListener(
        onIsPlayingChanged: (Boolean) -> Unit,
        onPlaybackStateChanged: (Int) -> Unit,
        onMediaItemTransition: (Int) -> Unit = {}
    )
    fun release()
}

@OptIn(markerClass = [UnstableApi::class])
class AudioPlayerManager(context: Context) : AudioPlayer {
    private val loadControl = DefaultLoadControl.Builder()
        .setBufferDurationsMs(10_000, 50_000, 1_500, 3_000)
        .build()

    private val player = ExoPlayer.Builder(context)
        .setLoadControl(loadControl)
        .build()

    override fun hasCurrentSong(): Boolean = player.currentMediaItem != null

    override fun play(audioUrl: String) {
        player.setMediaItem(MediaItem.fromUri(audioUrl))
        player.prepare()
        player.play()
    }

    override fun setPlayList(audioUrls: List<String>) {
        player.setMediaItems(audioUrls.map(MediaItem::fromUri))
        player.prepare()
    }

    override fun playAt(index: Int) {
        player.seekToDefaultPosition(index)
        player.play()
    }

    override fun playNext() = player.seekToNextMediaItem()

    override fun playPrevious() = player.seekToPreviousMediaItem()

    override fun hasNext(): Boolean = player.hasNextMediaItem()

    override fun hasPrevious(): Boolean = player.hasPreviousMediaItem()

    override fun getCurrentSongIndex(): Int = player.currentMediaItemIndex

    override fun pause() = player.pause()

    override fun resume() = player.play()

    override fun isPlaying(): Boolean = player.isPlaying

    override fun seekTo(positionMs: Long) = player.seekTo(positionMs)

    override fun getCurrentPosition(): Long = player.currentPosition

    override fun getDuration(): Long = player.duration

    override fun addListener(
        onIsPlayingChanged: (Boolean) -> Unit,
        onPlaybackStateChanged: (Int) -> Unit,
        onMediaItemTransition: (Int) -> Unit
    ) {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                onIsPlayingChanged(isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                onPlaybackStateChanged(playbackState)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                onMediaItemTransition(player.currentMediaItemIndex)
            }
        })
    }

    override fun release() = player.release()
}
