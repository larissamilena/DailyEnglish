package com.larissamilena.dailyenglish.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.larissamilena.dailyenglish.data.WordRepository

/**
 * Renders the current word directly on the home/lock screen background.
 * The word is picked deterministically from the day of year and AM/PM half,
 * so it changes automatically every 12 hours without the app ever needing to be opened.
 */
class DailyWordWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = DailyWordEngine()

    private inner class DailyWordEngine : Engine() {

        private val handler = Handler(Looper.getMainLooper())
        private var visible = false

        private val redrawRunnable = Runnable {
            drawFrame()
            scheduleNextCheck()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) {
                drawFrame()
                scheduleNextCheck()
            } else {
                handler.removeCallbacks(redrawRunnable)
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            drawFrame()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            visible = false
            handler.removeCallbacks(redrawRunnable)
        }

        private fun scheduleNextCheck() {
            handler.removeCallbacks(redrawRunnable)
            if (visible) {
                handler.postDelayed(redrawRunnable, CHECK_INTERVAL_MS)
            }
        }

        private fun drawFrame() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                canvas?.let { render(it) }
            } finally {
                canvas?.let { holder.unlockCanvasAndPost(it) }
            }
        }

        private fun render(canvas: Canvas) {
            val width = canvas.width
            val height = canvas.height
            if (width <= 0 || height <= 0) return

            val backgroundPaint = Paint().apply {
                shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    Color.parseColor("#1B1035"), Color.parseColor("#3A1C71"),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

            val word = WordRepository.getCurrentWord(applicationContext)
            val centerX = width / 2f
            val maxTextWidth = width * 0.82f

            val wordPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textAlign = Paint.Align.CENTER
                textSize = width * 0.14f
                isFakeBoldText = true
            }
            val translationPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E0D6FF")
                textAlign = Paint.Align.CENTER
                textSize = width * 0.06f
            }
            val examplePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#C9C0E8")
                textAlign = Paint.Align.CENTER
                textSize = width * 0.042f
                textSkewX = -0.2f
            }
            val exampleTranslationPaint = Paint(examplePaint).apply {
                color = Color.parseColor("#9E93C4")
            }

            var y = height * 0.4f
            canvas.drawText(word.word, centerX, y, wordPaint)

            y += wordPaint.textSize * 1.3f
            canvas.drawText(word.translation, centerX, y, translationPaint)

            y += translationPaint.textSize * 2f
            y = drawWrappedText(canvas, word.example, centerX, y, maxTextWidth, examplePaint)

            y += examplePaint.textSize * 0.6f
            drawWrappedText(canvas, word.exampleTranslation, centerX, y, maxTextWidth, exampleTranslationPaint)
        }

        private fun drawWrappedText(
            canvas: Canvas,
            text: String,
            centerX: Float,
            startY: Float,
            maxWidth: Float,
            paint: Paint
        ): Float {
            var y = startY
            var line = StringBuilder()
            for (token in text.split(" ")) {
                val candidate = if (line.isEmpty()) token else "$line $token"
                if (line.isNotEmpty() && paint.measureText(candidate) > maxWidth) {
                    canvas.drawText(line.toString(), centerX, y, paint)
                    y += paint.textSize * 1.3f
                    line = StringBuilder(token)
                } else {
                    line = StringBuilder(candidate)
                }
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line.toString(), centerX, y, paint)
                y += paint.textSize * 1.3f
            }
            return y
        }
    }

    companion object {
        private const val CHECK_INTERVAL_MS = 15 * 60 * 1000L
    }
}
