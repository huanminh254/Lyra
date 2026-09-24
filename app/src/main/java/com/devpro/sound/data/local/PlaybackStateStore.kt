package com.devpro.sound.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface PlaybackStateStore {
    fun saveSongId(songId: String)
    fun getSongId(): String?
    fun clearSongId()
}

@Singleton
class SharedPreferencesPlaybackStateStore @Inject constructor(
    @ApplicationContext context: Context
) : PlaybackStateStore {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun saveSongId(songId: String) {
        preferences.edit().putString(LAST_SONG_ID_KEY, songId).apply()
    }

    override fun getSongId(): String? = preferences.getString(LAST_SONG_ID_KEY, null)

    override fun clearSongId() {
        preferences.edit().remove(LAST_SONG_ID_KEY).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "playback_state"
        const val LAST_SONG_ID_KEY = "last_song_id"
    }
}
