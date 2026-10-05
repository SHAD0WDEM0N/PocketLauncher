package com.example.pocketlauncher.ui.emulation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.SystemClock
import android.view.Choreographer
import android.view.View
import com.example.pocketlauncher.engine.PocketEngine

class NativeFrameView(context: Context) : View(context), Choreographer.FrameCallback {

    private val paint = Paint().apply { isFilterBitmap = false; isAntiAlias = false }
    private val debugPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 28f
        setShadowLayer(4f, 1f, 1f, android.graphics.Color.BLACK)
    }
    private var bitmap: Bitmap? = null
    private var lastFrameCount = -1L
    private var running = true

    private var fpsWindowStartMs = SystemClock.elapsedRealtime()
    private var fpsWindowCoreStart = 0L
    private var presentedFrames = 0
    private var coreFps = 0f
    private var displayFps = 0f

    init {
        isFocusable = false
        Choreographer.getInstance().postFrameCallback(this)
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!running) return
        presentedFrames++

        val nowMs = SystemClock.elapsedRealtime()
        val elapsedMs = nowMs - fpsWindowStartMs
        if (elapsedMs >= 1000L) {
            val currentCoreFrame = PocketEngine.frameCount()
            val elapsedSeconds = elapsedMs / 1000f
            coreFps = (currentCoreFrame - fpsWindowCoreStart) / elapsedSeconds
            displayFps = presentedFrames / elapsedSeconds

            fpsWindowStartMs = nowMs
            fpsWindowCoreStart = currentCoreFrame
            presentedFrames = 0
        }

        invalidate()
        Choreographer.getInstance().postFrameCallback(this)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val frameCount = PocketEngine.frameCount()
        if (frameCount != lastFrameCount) {
            val width = PocketEngine.frameWidth()
            val height = PocketEngine.frameHeight()
            val pixels = PocketEngine.copyFrameRgba()

            if (width > 0 && height > 0 && pixels.size == width * height) {
                var current = bitmap
                if (current == null || current.width != width || current.height != height) {
                    current = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    bitmap = current
                }
                current.setPixels(pixels, 0, width, 0, 0, width, height)
                lastFrameCount = frameCount
            }
        }

        val current = bitmap ?: return
        val src = Rect(0, 0, current.width, current.height)

        val maxScale = minOf(
            width.toFloat() / current.width.toFloat(),
            height.toFloat() / current.height.toFloat(),
        )

        // Prefer an integer scale so source pixels map cleanly to whole display
        // pixels. Fall back to fit scaling only if the surface is smaller than
        // the native framebuffer.
        val integerScale = kotlin.math.floor(maxScale).coerceAtLeast(1f)
        val scale = if (maxScale >= 1f) integerScale else maxScale

        val drawWidth = (current.width * scale).toInt()
        val drawHeight = (current.height * scale).toInt()
        val left = (width - drawWidth) / 2
        val top = (height - drawHeight) / 2
        val dst = Rect(left, top, left + drawWidth, top + drawHeight)

        canvas.drawBitmap(current, src, dst, paint)

        canvas.drawText(
            "CORE %.1f  DISPLAY %.1f".format(coreFps, displayFps),
            20f,
            38f,
            debugPaint,
        )
    }

    override fun onDetachedFromWindow() {
        running = false
        Choreographer.getInstance().removeFrameCallback(this)
        bitmap?.recycle()
        bitmap = null
        super.onDetachedFromWindow()
    }
}
