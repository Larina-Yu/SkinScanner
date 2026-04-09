package com.example.skinscanner

import android.graphics.Bitmap
import kotlin.math.abs

object ImageProcessingUtils {

    fun calculateSkinProportion(bitmap: Bitmap): Double {
        var skinPixels = 0
        val totalPixels = bitmap.width * bitmap.height

        val pixels = IntArray(totalPixels)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

            val isSkin = r > 95 && g > 40 && b > 20 &&
                    r > g && r > b &&
                    abs(r - g) > 15 &&
                    !(r < 60 && g < 60 && b < 60)

            if (isSkin) skinPixels++
        }

        return if (totalPixels > 0) skinPixels.toDouble() / totalPixels else 0.0
    }
}