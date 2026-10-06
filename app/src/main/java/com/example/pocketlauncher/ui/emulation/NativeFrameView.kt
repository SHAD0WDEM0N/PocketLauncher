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
import com.example.pocketlauncher.engine.VideoEffectMode
import com.example.pocketlauncher.engine.VideoFilterMode
import com.example.pocketlauncher.engine.VideoScaleMode

class NativeFrameView(context: Context) : View(context), Choreographer.FrameCallback {

    private val paint = Paint().apply { isFilterBitmap = false; isAntiAlias = false }
    var scaleMode: VideoScaleMode = VideoScaleMode.FIT
        set(value) {
            field = value
            invalidate()
        }
    var filterMode: VideoFilterMode = VideoFilterMode.SHARP
        set(value) {
            field = value
            paint.isFilterBitmap = value == VideoFilterMode.SMOOTH
            paint.isAntiAlias = value == VideoFilterMode.SMOOTH
            invalidate()
        }

    var effectMode: VideoEffectMode = VideoEffectMode.OFF
        set(value) {
            if (field != value) {
                field = value
                overlayBitmap?.recycle()
                overlayBitmap = null
                overlayKey = null
                invalidate()
            }
        }
    private val debugPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 28f
        setShadowLayer(4f, 1f, 1f, android.graphics.Color.BLACK)
    }
    private var bitmap: Bitmap? = null
    private var overlayBitmap: Bitmap? = null
    private var overlayKey: String? = null
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
        val dst = if (scaleMode == VideoScaleMode.STRETCH) {
            Rect(0, 0, width, height)
        } else {
            val scale = when (scaleMode) {
                VideoScaleMode.FIT -> maxScale
                VideoScaleMode.INTEGER -> kotlin.math.floor(maxScale).coerceAtLeast(1f)
                VideoScaleMode.STRETCH -> maxScale
            }

            val drawWidth = (current.width * scale).toInt()
            val drawHeight = (current.height * scale).toInt()
            val left = (width - drawWidth) / 2
            val top = (height - drawHeight) / 2
            Rect(left, top, left + drawWidth, top + drawHeight)
        }

        canvas.drawBitmap(current, src, dst, paint)

        if (effectMode != VideoEffectMode.OFF) {
            val overlay = overlayFor(dst, current.width, current.height)
            if (overlay != null) {
                canvas.drawBitmap(overlay, dst.left.toFloat(), dst.top.toFloat(), null)
            }
        }

        canvas.drawText(
            "CORE %.1f  DISPLAY %.1f".format(coreFps, displayFps),
            20f,
            38f,
            debugPaint,
        )
    }

    private fun overlayFor(dst: Rect, sourceWidth: Int, sourceHeight: Int): Bitmap? {
        if (dst.width() <= 0 || dst.height() <= 0 || sourceWidth <= 0 || sourceHeight <= 0) return null

        val key = "${effectMode.name}:${dst.width()}x${dst.height()}:${sourceWidth}x${sourceHeight}"
        if (overlayBitmap != null && overlayKey == key) return overlayBitmap

        overlayBitmap?.recycle()
        val overlay = Bitmap.createBitmap(dst.width(), dst.height(), Bitmap.Config.ARGB_8888)
        val c = Canvas(overlay)
        val p = Paint().apply {
            isAntiAlias = false
            style = Paint.Style.FILL
        }

        val pixelW = dst.width().toFloat() / sourceWidth.toFloat()
        val pixelH = dst.height().toFloat() / sourceHeight.toFloat()

        when (effectMode) {
            VideoEffectMode.OFF -> Unit

            VideoEffectMode.SCANLINES -> {
                p.color = android.graphics.Color.argb(58, 0, 0, 0)
                val thickness = maxOf(1f, pixelH * 0.28f)
                for (row in 0 until sourceHeight) {
                    val y = (row + 1) * pixelH - thickness
                    c.drawRect(0f, y, dst.width().toFloat(), y + thickness, p)
                }
            }

            VideoEffectMode.LCD_GRID -> {
                p.color = android.graphics.Color.argb(38, 0, 0, 0)
                val hThickness = maxOf(1f, pixelH * 0.16f)
                val vThickness = maxOf(1f, pixelW * 0.16f)

                for (row in 1 until sourceHeight) {
                    val y = row * pixelH - hThickness / 2f
                    c.drawRect(0f, y, dst.width().toFloat(), y + hThickness, p)
                }
                for (col in 1 until sourceWidth) {
                    val x = col * pixelW - vThickness / 2f
                    c.drawRect(x, 0f, x + vThickness, dst.height().toFloat(), p)
                }
            }

            VideoEffectMode.PIXEL_GRID -> {
                p.color = android.graphics.Color.argb(54, 0, 0, 0)
                val hThickness = maxOf(1f, pixelH * 0.22f)
                val vThickness = maxOf(1f, pixelW * 0.22f)

                for (row in 1 until sourceHeight) {
                    val y = row * pixelH - hThickness / 2f
                    c.drawRect(0f, y, dst.width().toFloat(), y + hThickness, p)
                }
                for (col in 1 until sourceWidth) {
                    val x = col * pixelW - vThickness / 2f
                    c.drawRect(x, 0f, x + vThickness, dst.height().toFloat(), p)
                }
            }
        }

        overlayBitmap = overlay
        overlayKey = key
        return overlay
    }

    override fun onDetachedFromWindow() {
        running = false
        Choreographer.getInstance().removeFrameCallback(this)
        bitmap?.recycle()
        bitmap = null
        overlayBitmap?.recycle()
        overlayBitmap = null
        overlayKey = null
        super.onDetachedFromWindow()
    }
}
