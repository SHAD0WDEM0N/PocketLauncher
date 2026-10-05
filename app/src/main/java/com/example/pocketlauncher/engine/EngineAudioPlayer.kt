package com.example.pocketlauncher.engine

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlin.math.max

class EngineAudioPlayer {

    private var track: AudioTrack? = null
    private var sampleRate: Int = 0

    fun start(sampleRateHz: Double) {
        stop()

        val rate = sampleRateHz.toInt().coerceAtLeast(8000)
        sampleRate = rate

        val minBuffer = AudioTrack.getMinBufferSize(
            rate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT,
        ).coerceAtLeast(rate / 4)

        track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(rate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()
            )
            .setBufferSizeInBytes(max(minBuffer, 4096))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
            .also { it.play() }
    }

    fun write(samples: ShortArray) {
        if (samples.isEmpty()) return
        val current = track ?: return
        current.write(samples, 0, samples.size, AudioTrack.WRITE_NON_BLOCKING)
    }

    fun stop() {
        track?.let { current ->
            runCatching { current.pause() }
            runCatching { current.flush() }
            runCatching { current.stop() }
            runCatching { current.release() }
        }
        track = null
        sampleRate = 0
    }
}
