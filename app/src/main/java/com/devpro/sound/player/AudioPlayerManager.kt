package com.devpro.sound.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer

class AudioPlayerManager (
    context: Context
){
    private val loadControl = DefaultLoadControl.Builder()
        .setBufferDurationsMs(
            10_000,
            50_000,
            1_500,
            3_000
        ).build()
    private val player = ExoPlayer.Builder(context)
        .setLoadControl(loadControl)
        .build()
    fun hasCurrentSong(): Boolean{
        return player.currentMediaItem != null
    }
    fun play(audioUrl: String){
        val mediaItem = MediaItem.fromUri(audioUrl)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }
    fun clear(){
        player.clearMediaItems()
    }
    fun setPlayList(audioUrls: List<String>){
        val mediaItems = audioUrls.map{audioUrl ->
            MediaItem.fromUri(audioUrl) }
        player.setMediaItems(mediaItems)
        player.prepare()
    }
    fun playNext(){
        player.seekToNextMediaItem()
    }
    fun playPrevious(){
        player.seekToPreviousMediaItem()
    }
    fun hasNext(): Boolean{ // Kiểm tra có bài hát tiếp theo hay không
        return player.hasNextMediaItem()
    }
    fun hasPrevious(): Boolean{ // Kiểm tra có bài hát trước đó hay không
        return player.hasPreviousMediaItem()
    }
    fun getCurrentSongIndex(): Int{ // Trả về vị trí bài hát hiện tại
        return player.currentMediaItemIndex
    }
    fun pause(){
        player.pause()
    }
    fun release(){
        player.release()
    }
    fun resume(){
        player.play()
    }
    fun isPlaying(): Boolean{
        return player.isPlaying
    }
    fun seekTo(positionMs: Long){
        player.seekTo(positionMs)
    }
    fun getCurrentPosition(): Long{
        return player.currentPosition
    }
    fun getDuration(): Long{
        return player.duration
    }
    fun addListener(
        onIsPlayingChanged: (Boolean) -> Unit, // Trạng thái phát nhạc
        onPlaybackStateChanged: (Int) -> Unit // Trạng thái player
    ){
        player.addListener(
            object: Player.Listener{
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    onIsPlayingChanged(isPlaying)
                }
                override fun onPlaybackStateChanged(playbackState: Int) {
                    onPlaybackStateChanged(playbackState)
                }
            }
        )
    }
    fun isBuffering(): Boolean{
        return player.playbackState == Player.STATE_BUFFERING
    }
    fun isEnded(): Boolean{
        return player.playbackState == Player.STATE_ENDED
    }
    fun stop(){
        player.stop()
    }
    fun setVolume(volume: Float){
        player.volume = volume
    }
    fun getVolume(): Float{
        return player.volume
    }
    fun playAt(index: Int){
        player.seekToDefaultPosition(index)
        player.play()
    }
}