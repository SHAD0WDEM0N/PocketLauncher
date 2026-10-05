package com.example.pocketlauncher.engine

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.math.max

class EngineAudioPlayer {

    private var track: AudioTrack? = null
    private val running = AtomicBoolean(false)
    private val queue = LinkedBlockingQueue<ShortArray>(12)
    private var worker: Thread? = null

    fun start(sampleRateHz: Double) {
        stop()

        val rate = sampleRateHz.toInt().coerceAtLeast(8000)
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
            .setBufferSizeInBytes(max(minBuffer * 2, 8192))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
            .also { it.play() }

        running.set(true)
        worker = thread(name = "PocketAudio", priority = Thread.MAX_PRIORITY) {
            while (running.get()) {
                val samples = runCatching { queue.take() }.getOrNull() ?: continue
                val current = track ?: continue
                var offset = 0
                while (running.get() && offset < samples.size) {
                    val written = current.write(
                        samples,
                        offset,
                        samples.size - offset,
                        AudioTrack.WRITE_BLOCKING,
                    )
                    if (written <= 0) break
                    offset += written
                }
            }
        }
    }

    fun enqueue(samples: ShortArray) {
        if (samples.isEmpty() || !running.get()) return
        if (!queue.offer(samples)) {
            queue.poll()
            queue.offer(samples)
        }
    }

    fun stop() {
        running.set(false)
        worker?.interrupt()
        worker = null
        queue.clear()

        track?.let { current ->
            runCatching { current.pause() }
            runCatching { current.flush() }
            runCatching { current.stop() }
            runCatching { current.release() }
        }
        track = null
    }
}
