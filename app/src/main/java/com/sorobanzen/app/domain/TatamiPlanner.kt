package com.sorobanzen.app.domain

object TatamiPlanner {

    enum class Region(val labelResId: String, val matWidth: Double, val matLength: Double) {
        KYOTO("Kyoto (Kyouma)", 0.955, 1.91),
        NAGOYA("Nagoya (Ainoma)", 0.91, 1.82),
        TOKYO("Tokyo (Edoma)", 0.88, 1.76)
    }

    data class MatLayout(
        val x: Double,      // in meters
        val y: Double,      // in meters
        val width: Double,  // in meters
        val height: Double, // in meters
        val isHorizontal: Boolean
    )

    /**
     * Calculates the number of mats and provides a layout suggestion.
     */
    fun calculateLayout(roomWidth: Double, roomLength: Double, region: Region): List<MatLayout> {
        if (roomWidth <= 0 || roomLength <= 0) return emptyList()

        val w = region.matWidth
        val l = region.matLength

        val layouts = mutableListOf<MatLayout>()
        
        // Let's implement a simple layout generator.
        // We will try to partition the room into standard layouts if they resemble standard sizes,
        // otherwise, we will lay them out in a clean layout filling up the area.
        
        // Determine approximate dimensions in terms of mat widths.
        // Since length = 2 * width, we can check how many widths fit.
        val cols = (roomWidth / w).toInt()
        val rows = (roomLength / w).toInt()
        
        if (cols <= 0 || rows <= 0) return emptyList()

        // For common standard rooms:
        // 6 mats (approx 3m x 3.6m for Kyoto/Edoma/Ainoma, i.e. 3 widths x 4 widths)
        // Let's generate a beautiful auspicious (Syugi-biki) spiral-like grid if it matches.
        if (cols == 3 && rows == 4) {
            // Standard 6-tatami room layout
            // Two mats vertical on left, two mats vertical on right, two mats horizontal in center.
            layouts.add(MatLayout(0.0, 0.0, w, l, false))
            layouts.add(MatLayout(0.0, l, w, l, false))
            
            layouts.add(MatLayout(w, 0.0, l, w, true))
            layouts.add(MatLayout(w, l + w, l, w, true))
            
            layouts.add(MatLayout(w + l, 0.0, w, l, false))
            layouts.add(MatLayout(w + l, l, w, l, false))
            return layouts
        }
        
        if (cols == 4 && rows == 4) {
            // Standard 8-tatami room layout: spiral pattern.
            layouts.add(MatLayout(0.0, 0.0, l, w, true))
            layouts.add(MatLayout(l, 0.0, w, l, false))
            layouts.add(MatLayout(0.0, w, w, l, false))
            layouts.add(MatLayout(w, w, l, w, true))
            layouts.add(MatLayout(w, w + w, l, w, true))
            layouts.add(MatLayout(w + l, w, w, l, false))
            layouts.add(MatLayout(0.0, w + l, w, l, false))
            layouts.add(MatLayout(w, w + l + w, l, w, true))
            return layouts
        }

        // Default layout: Fill the space with horizontal/vertical mats in rows/columns.
        // If cols is even, we can place mats horizontally (which take 2 columns and 1 row).
        // Let's do a simple alternating grid that covers the room.
        var currentY = 0.0
        while (currentY + l <= roomLength) {
            var currentX = 0.0
            while (currentX + w <= roomWidth) {
                layouts.add(MatLayout(currentX, currentY, w, l, false))
                currentX += w
            }
            currentY += l
        }

        // Fill remaining horizontal strips with horizontal mats (length = l, width = w)
        if (currentY + w <= roomLength) {
            var currentX = 0.0
            while (currentX + l <= roomWidth) {
                layouts.add(MatLayout(currentX, currentY, l, w, true))
                currentX += l
            }
        }

        return layouts
    }
}
