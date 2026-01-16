// SPDX-License-Identifier: MIT
// Copyright (c) 2025-2026 Sourajit Karmakar

package com.sourajitk.ambient_music.playback

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PlaybackStatusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == MusicPlaybackService.ACTION_GET_PLAYBACK_STATUS) {
            Log.d("PlaybackStatusReceiver", "Received status request")
            val statusIntent = Intent(MusicPlaybackService.ACTION_PLAYBACK_STATUS).apply {
                putExtra("is_playing", MusicPlaybackService.isServiceCurrentlyPlaying)
                putExtra("genre", MusicPlaybackService.currentPlaylistGenre)
            }
            context.sendBroadcast(statusIntent)
        }
    }
}
