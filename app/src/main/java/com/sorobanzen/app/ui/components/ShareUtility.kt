package com.sorobanzen.app.ui.components

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object ShareUtility {

    /**
     * Draws the Soroban state onto a Bitmap, saves it, and shares it.
     */
    suspend fun createSorobanShareIntent(
        context: Context,
        value: Long,
        kanjiReading: String,
        rodsCount: Int,
        rodValues: IntArray
    ): Intent = withContext(Dispatchers.IO) {
        require(rodsCount in 7..17) { "Rod count must be between 7 and 17" }
        val safeRodValues = rodValues.copyOf(rodsCount)
        val width = 1200
        val height = 750
        val cachePath = File(context.cacheDir, "shared_images").apply { mkdirs() }
        val imageFile = File(cachePath, "soroban_state.png")
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(bitmap)
            // --- 1. Draw Background Card ---
            val bgPaint = Paint().apply {
                color = Color.parseColor("#F4F0E7")
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // Draw an elegant outer border
            val borderPaint = Paint().apply {
                color = Color.parseColor("#D8D1C4")
                style = Paint.Style.STROKE
                strokeWidth = 20f
            }
            canvas.drawRect(10f, 10f, (width - 10).toFloat(), (height - 10).toFloat(), borderPaint)

            // --- 2. Title & Value Text ---
            val titlePaint = Paint().apply {
                color = Color.parseColor("#586A55")
                textSize = 32f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("そろばん禅", (width / 2).toFloat(), 70f, titlePaint)

            val valuePaint = Paint().apply {
                color = Color.parseColor("#1A1A1A") // Charcoal Text
                textSize = 72f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            drawCenteredText(
                canvas = canvas,
                text = String.format(Locale.ROOT, "%,d", value),
                centerX = (width / 2).toFloat(),
                baselineY = 160f,
                maxWidth = width - 160f,
                minTextSize = 48f,
                paint = valuePaint
            )

            val readingPaint = Paint().apply {
                color = Color.parseColor("#586A55")
                textSize = 36f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            drawCenteredText(
                canvas = canvas,
                text = kanjiReading,
                centerX = (width / 2).toFloat(),
                baselineY = 220f,
                maxWidth = width - 160f,
                minTextSize = 23f,
                paint = readingPaint
            )

            // --- 3. Draw Soroban Abacus ---
            val abacusLeft = 100f
            val abacusTop = 285f
            val abacusWidth = width - 200f
            val abacusHeight = 350f

            val frameThickness = 20f
            val upperHeight = abacusHeight * 0.25f
            val beamHeight = 12f

            val topY = abacusTop + frameThickness
            val beamTopY = topY + upperHeight
            val beamBottomY = beamTopY + beamHeight
            val bottomY = abacusTop + abacusHeight - frameThickness

            val rodWidth = abacusWidth / rodsCount
            val beadHeight = (abacusHeight * 0.08f).coerceAtLeast(16f)
            val beadWidth = rodWidth * 0.72f

            // Draw the pale hinoki frame used by the interactive soroban.
            val woodPaint = Paint().apply {
                color = Color.parseColor("#E7D2A8")
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawRect(abacusLeft, abacusTop, abacusLeft + abacusWidth, abacusTop + abacusHeight, woodPaint)

            // Draw inner metal/rod background
            val innerBgPaint = Paint().apply {
                color = Color.parseColor("#F0ECE3")
                style = Paint.Style.FILL
            }
            canvas.drawRect(
                abacusLeft + frameThickness,
                abacusTop + frameThickness,
                abacusLeft + abacusWidth - frameThickness,
                abacusTop + abacusHeight - frameThickness,
                innerBgPaint
            )

            // Draw Divider Beam (Horizontal)
            val beamPaint = Paint().apply {
                color = Color.parseColor("#DEC69B")
                style = Paint.Style.FILL
            }
            canvas.drawRect(
                abacusLeft + frameThickness,
                beamTopY,
                abacusLeft + abacusWidth - frameThickness,
                beamBottomY,
                beamPaint
            )

            // Paint definitions for Rods and Beads
            val rodPaint = Paint().apply {
                color = Color.parseColor("#92918B")
                strokeWidth = 4f
            }

            val beadPaint = Paint().apply {
                color = Color.parseColor("#254A7A")
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            val dotPaint = Paint().apply {
                color = Color.parseColor("#315A8D")
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            // Draw alignment dots on every 4th rod
            for (i in 0 until rodsCount) {
                val rodX = abacusLeft + (rodWidth * i) + (rodWidth / 2)
                // Draw rod
                canvas.drawLine(rodX, abacusTop + frameThickness, rodX, abacusTop + abacusHeight - frameThickness, rodPaint)

                if ((rodsCount - 1 - i) % 4 == 3) {
                    canvas.drawCircle(rodX, beamTopY + (beamHeight / 2), 4f, dotPaint)
                }

                // Draw beads for this rod
                val valForRod = safeRodValues[i].coerceIn(0, 9)
                val heavenActive = valForRod >= 5
                val earthActiveCount = valForRod % 5

                // Draw heaven bead
                val minHeavenY = topY + beadHeight / 2 + 5f
                val maxHeavenY = beamTopY - beadHeight / 2 - 5f
                val heavenY = if (heavenActive) maxHeavenY else minHeavenY
                drawBiConicalBead(canvas, rodX, heavenY, beadWidth, beadHeight, beadPaint)

                // Draw earth beads
                for (b in 0..3) {
                    val beadActive = b < earthActiveCount
                    val activeY = beamBottomY + beadHeight / 2 + 5f + (b * (beadHeight + 2f))
                    val inactiveY = bottomY - beadHeight / 2 - 5f - ((3 - b) * (beadHeight + 2f))
                    val beadY = if (beadActive) activeY else inactiveY
                    drawBiConicalBead(canvas, rodX, beadY, beadWidth, beadHeight, beadPaint)
                }
            }

            // --- 4. Save and prepare share intent ---
            FileOutputStream(imageFile).use { stream ->
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                    "そろばん画像を保存できませんでした"
                }
            }
        } finally {
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }

        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
        Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(contentUri, "image/png")
            clipData = ClipData.newUri(context.contentResolver, "そろばん画像", contentUri)
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(
                Intent.EXTRA_TEXT,
                "${String.format(Locale.ROOT, "%,d", value)}（$kanjiReading）"
            )
        }
    }

    private fun drawCenteredText(
        canvas: Canvas,
        text: String,
        centerX: Float,
        baselineY: Float,
        maxWidth: Float,
        minTextSize: Float,
        paint: Paint
    ) {
        val originalSize = paint.textSize
        val measuredWidth = paint.measureText(text)
        if (measuredWidth > maxWidth && measuredWidth > 0f) {
            paint.textSize = (originalSize * maxWidth / measuredWidth).coerceAtLeast(minTextSize)
        }
        canvas.drawText(text, centerX, baselineY, paint)
        paint.textSize = originalSize
    }

    private fun drawBiConicalBead(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        beadWidth: Float,
        beadHeight: Float,
        paint: Paint
    ) {
        val left = centerX - beadWidth / 2
        val right = centerX + beadWidth / 2
        val top = centerY - beadHeight / 2
        val bottom = centerY + beadHeight / 2
        val midY = centerY

        val path = Path().apply {
            moveTo(left + beadWidth * 0.29f, top)
            quadTo(
                left + beadWidth * 0.22f,
                top,
                left + beadWidth * 0.16f,
                top + beadHeight * 0.17f
            )
            lineTo(left + beadWidth * 0.035f, midY - beadHeight * 0.1f)
            quadTo(left, midY, left + beadWidth * 0.035f, midY + beadHeight * 0.1f)
            lineTo(left + beadWidth * 0.16f, bottom - beadHeight * 0.17f)
            quadTo(left + beadWidth * 0.22f, bottom, left + beadWidth * 0.29f, bottom)
            lineTo(right - beadWidth * 0.29f, bottom)
            quadTo(
                right - beadWidth * 0.22f,
                bottom,
                right - beadWidth * 0.16f,
                bottom - beadHeight * 0.17f
            )
            lineTo(right - beadWidth * 0.035f, midY + beadHeight * 0.1f)
            quadTo(right, midY, right - beadWidth * 0.035f, midY - beadHeight * 0.1f)
            lineTo(right - beadWidth * 0.16f, top + beadHeight * 0.17f)
            quadTo(right - beadWidth * 0.22f, top, right - beadWidth * 0.29f, top)
            close()
        }
        canvas.drawPath(path, paint)
    }
}
