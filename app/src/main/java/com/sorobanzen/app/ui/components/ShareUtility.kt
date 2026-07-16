package com.sorobanzen.app.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
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
        val width = 1200
        val height = 750
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // --- 1. Draw Background Card ---
        val bgPaint = Paint().apply {
            color = Color.parseColor("#FAF9F6") // Warm white paper
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Draw an elegant outer border
        val borderPaint = Paint().apply {
            color = Color.parseColor("#E5E2D9")
            style = Paint.Style.STROKE
            strokeWidth = 20f
        }
        canvas.drawRect(10f, 10f, (width - 10).toFloat(), (height - 10).toFloat(), borderPaint)

        // --- 2. Title & Value Text ---
        val titlePaint = Paint().apply {
            color = Color.parseColor("#8A9A86") // Moss Green Accent
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
        canvas.drawText(String.format(Locale.ROOT, "%,d", value), (width / 2).toFloat(), 160f, valuePaint)

        val readingPaint = Paint().apply {
            color = Color.parseColor("#5E6F54") // Soft dark moss
            textSize = 36f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(kanjiReading, (width / 2).toFloat(), 220f, readingPaint)

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
        
        val beadHeight = (upperHeight - 15f) * 0.8f
        val rodWidth = abacusWidth / rodsCount

        // Draw abacus wood frame (outer dark brown rectangle)
        val woodPaint = Paint().apply {
            color = Color.parseColor("#5C4033") // Wood brown
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRect(abacusLeft, abacusTop, abacusLeft + abacusWidth, abacusTop + abacusHeight, woodPaint)

        // Draw inner metal/rod background
        val innerBgPaint = Paint().apply {
            color = Color.parseColor("#F4F0E6")
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
            color = Color.parseColor("#E5E2D9")
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
            color = Color.parseColor("#8A8A8A")
            strokeWidth = 4f
        }
        
        val beadPaint = Paint().apply {
            color = Color.parseColor("#8B5A2B") // Polished wood bead color
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        
        val dotPaint = Paint().apply {
            color = Color.parseColor("#1A1A1A")
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
            val valForRod = rodValues.getOrElse(i) { 0 }
            val heavenActive = valForRod >= 5
            val earthActiveCount = valForRod % 5

            // Draw heaven bead
            val minHeavenY = topY + beadHeight / 2 + 5f
            val maxHeavenY = beamTopY - beadHeight / 2 - 5f
            val heavenY = if (heavenActive) maxHeavenY else minHeavenY
            drawBiConicalBead(canvas, rodX, heavenY, rodWidth * 0.8f, beadHeight, beadPaint)

            // Draw earth beads
            for (b in 0..3) {
                val beadActive = b < earthActiveCount
                val activeY = beamBottomY + beadHeight / 2 + 5f + (b * (beadHeight + 2f))
                val inactiveY = bottomY - beadHeight / 2 - 5f - ((3 - b) * (beadHeight + 2f))
                val beadY = if (beadActive) activeY else inactiveY
                drawBiConicalBead(canvas, rodX, beadY, rodWidth * 0.8f, beadHeight, beadPaint)
            }
        }

        // --- 4. Save and prepare share intent ---
        val cachePath = File(context.cacheDir, "shared_images").apply { mkdirs() }
        val imageFile = File(cachePath, "soroban_state.png")
        FileOutputStream(imageFile).use { stream ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                "そろばん画像を保存できませんでした"
            }
        }
        bitmap.recycle()

        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
        Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(contentUri, "image/png")
            putExtra(Intent.EXTRA_STREAM, contentUri)
        }
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
            moveTo(centerX, top)
            lineTo(right, midY - beadHeight * 0.1f)
            lineTo(right, midY + beadHeight * 0.1f)
            lineTo(centerX, bottom)
            lineTo(left, midY + beadHeight * 0.1f)
            lineTo(left, midY - beadHeight * 0.1f)
            close()
        }
        canvas.drawPath(path, paint)
    }
}
