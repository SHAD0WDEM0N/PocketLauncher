package com.example.pocketlauncher.ui.emulation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.SystemClock
import android.view.Choreographer
import android.view.View
import com.example.pocketlauncher.engine.PocketEngine
import com.example.pocketlauncher.engine.VideoBorderMode
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

    var borderMode: VideoBorderMode = VideoBorderMode.OFF
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    var platformKey: String = "GBA"
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(38, 40, 46)
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD_ITALIC)
    }
    private val borderSmallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(54, 57, 64)
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
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
        val borderActive =
            borderMode == VideoBorderMode.AUTO &&
                platformKey.equals("GBA", ignoreCase = true) &&
                scaleMode != VideoScaleMode.STRETCH

        val dst = if (scaleMode == VideoScaleMode.STRETCH) {
            Rect(0, 0, width, height)
        } else if (borderActive) {
            val gameAspect = current.width.toFloat() / current.height.toFloat()
            val viewAspect = width.toFloat() / height.toFloat()
            val scale = when (scaleMode) {
                VideoScaleMode.INTEGER -> kotlin.math.floor(maxScale).coerceAtLeast(1f)
                else -> maxScale
            }
            val drawWidth = (current.width * scale).toInt()
            val drawHeight = (current.height * scale).toInt()

            if (viewAspect >= gameAspect) {
                val left = (width - drawWidth) / 2
                Rect(left, 0, left + drawWidth, drawHeight.coerceAtMost(height))
            } else {
                Rect(0, 0, drawWidth.coerceAtMost(width), drawHeight)
            }
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

        if (borderActive) {
            drawGbaAutoBorder(canvas, dst, current.width.toFloat() / current.height.toFloat())
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

    private fun drawGbaAutoBorder(canvas: Canvas, dst: Rect, gameAspect: Float) {
        canvas.drawColor(android.graphics.Color.BLACK)

        val viewAspect = if (height > 0) width.toFloat() / height.toFloat() else gameAspect
        if (viewAspect >= gameAspect) {
            drawGbaWideSidePanels(canvas, dst)
        } else {
            drawGbaSpBottomBanner(canvas, dst)
        }
    }

    private fun drawGbaWideSidePanels(canvas: Canvas, dst: Rect) {
        val leftWidth = dst.left.coerceAtLeast(0)
        val rightStart = dst.right.coerceAtMost(width)
        val rightWidth = (width - rightStart).coerceAtLeast(0)
        val silverLight = android.graphics.Color.rgb(198, 201, 207)
        val silverMid = android.graphics.Color.rgb(132, 136, 145)
        val silverDark = android.graphics.Color.rgb(58, 61, 68)

        if (leftWidth > 0) {
            borderPaint.shader = LinearGradient(
                0f, 0f, leftWidth.toFloat(), 0f,
                silverDark, silverLight, Shader.TileMode.CLAMP
            )
            canvas.drawRect(0f, 0f, leftWidth.toFloat(), height.toFloat(), borderPaint)
            borderPaint.shader = null
            borderPaint.color = silverMid
            canvas.drawRect(leftWidth - 3f, 0f, leftWidth.toFloat(), height.toFloat(), borderPaint)

            borderTextPaint.textSize = (leftWidth * 0.13f).coerceIn(18f, 34f)
            canvas.drawText("GAME BOY", leftWidth * 0.50f, height * 0.34f, borderTextPaint)
            canvas.drawText("ADVANCE", leftWidth * 0.50f, height * 0.39f, borderTextPaint)

            drawSpeakerDots(canvas, leftWidth * 0.50f, height * 0.76f, leftWidth * 0.035f)
        }

        if (rightWidth > 0) {
            borderPaint.shader = LinearGradient(
                rightStart.toFloat(), 0f, width.toFloat(), 0f,
                silverLight, silverDark, Shader.TileMode.CLAMP
            )
            canvas.drawRect(rightStart.toFloat(), 0f, width.toFloat(), height.toFloat(), borderPaint)
            borderPaint.shader = null
            borderPaint.color = silverMid
            canvas.drawRect(rightStart.toFloat(), 0f, rightStart + 3f, height.toFloat(), borderPaint)

            val cx = rightStart + rightWidth * 0.50f
            val ledRadius = (rightWidth * 0.035f).coerceIn(4f, 9f)
            borderPaint.color = android.graphics.Color.rgb(104, 255, 34)
            canvas.drawCircle(cx - rightWidth * 0.10f, height * 0.18f, ledRadius, borderPaint)
            borderSmallPaint.textSize = (rightWidth * 0.11f).coerceIn(16f, 28f)
            canvas.drawText("POWER", cx + rightWidth * 0.10f, height * 0.19f, borderSmallPaint)

            drawSpeakerDots(canvas, cx, height * 0.76f, rightWidth * 0.035f)
        }
    }

    private fun drawSpeakerDots(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
        borderPaint.color = android.graphics.Color.rgb(8, 9, 25)
        val r = radius.coerceIn(2.5f, 6f)
        val gap = r * 3.0f
        for (row in -2..2) {
            for (col in -2..2) {
                canvas.drawCircle(centerX + col * gap, centerY + row * gap, r, borderPaint)
            }
        }
    }

    private fun drawGbaSpBottomBanner(canvas: Canvas, dst: Rect) {
        if (dst.bottom >= height) return

        val top = dst.bottom.toFloat()
        borderPaint.shader = LinearGradient(
            0f, top, 0f, height.toFloat(),
            android.graphics.Color.rgb(30, 31, 35),
            android.graphics.Color.rgb(2, 2, 4),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, top, width.toFloat(), height.toFloat(), borderPaint)
        borderPaint.shader = null

        // Thin glossy lip like the real SP display bezel.
        borderPaint.color = android.graphics.Color.rgb(78, 80, 86)
        canvas.drawRect(0f, top, width.toFloat(), top + 2f, borderPaint)
        borderPaint.color = android.graphics.Color.argb(70, 255, 255, 255)
        canvas.drawRect(0f, top + 2f, width.toFloat(), top + 4f, borderPaint)

        val bannerHeight = height - dst.bottom
        borderTextPaint.color = android.graphics.Color.rgb(232, 232, 235)
        borderSmallPaint.color = android.graphics.Color.rgb(205, 207, 212)
        borderTextPaint.textSize = (bannerHeight * 0.31f).coerceIn(18f, 38f)
        val baseline = top + bannerHeight * 0.60f
        canvas.drawText("GAME BOY ADVANCE", width * 0.48f, baseline, borderTextPaint)

        borderSmallPaint.textSize = (bannerHeight * 0.26f).coerceIn(16f, 32f)
        canvas.drawText("SP", width * 0.78f, baseline, borderSmallPaint)

        // Restore darker print colour used by the silver side rails.
        borderTextPaint.color = android.graphics.Color.rgb(38, 40, 46)
        borderSmallPaint.color = android.graphics.Color.rgb(54, 57, 64)
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
                p.color = android.graphics.Color.argb(92, 0, 0, 0)
                val thickness = maxOf(1.5f, pixelH * 0.38f)
                for (row in 0 until sourceHeight) {
                    val y = (row + 1) * pixelH - thickness
                    c.drawRect(0f, y, dst.width().toFloat(), y + thickness, p)
                }
            }

            VideoEffectMode.LCD_GRID -> {
                p.color = android.graphics.Color.argb(72, 0, 0, 0)
                val hThickness = maxOf(1.25f, pixelH * 0.24f)
                val vThickness = maxOf(1.25f, pixelW * 0.24f)

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
                p.color = android.graphics.Color.argb(96, 0, 0, 0)
                val hThickness = maxOf(1.5f, pixelH * 0.32f)
                val vThickness = maxOf(1.5f, pixelW * 0.32f)

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
