/*package com.example.skinscanner

import android.graphics.Bitmap
import kotlin.math.atan
import kotlin.math.PI

object ImageProcessingUtils {

    fun calculateITA(bitmap: Bitmap): Double {

        val width = bitmap.width
        val height = bitmap.height

        var totalITA = 0.0
        var pixelCount = 0

        for (x in 0 until width step 5) {
            for (y in 0 until height step 5) {

                val pixel = bitmap.getPixel(x, y)

                val r = (pixel shr 16 and 0xFF).toDouble()
                val g = (pixel shr 8 and 0xFF).toDouble()
                val b = (pixel and 0xFF).toDouble()

                // Convert RGB to CIE Lab (simplified)
                val L = 0.2126*r + 0.7152*g + 0.0722*b
                val B = b - g

                if (B != 0.0) {
                    val ita = (atan((L - 50) / B) * 180 / PI)
                    totalITA += ita
                    pixelCount++
                }
            }
        }

        return if (pixelCount > 0) totalITA / pixelCount else 0.0
    }

    fun itaToSkinType(ita: Double): String {
        return when {
            ita > 40 -> "I-II"
            ita > 28 -> "III"
            ita > 10 -> "IV"
            ita > -30 -> "V"
            else -> "VI"
        }
    }
}

//database store
val ita = ImageProcessingUtils.calculateITA(bitmap)
val skinType = ImageProcessingUtils.itaToSkinType(ita)

Log.d("ITA", "ITA Value: $ita")
Log.d("ITA", "Skin Type: $skinType")

 */