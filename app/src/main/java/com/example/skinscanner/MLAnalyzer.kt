package com.example.skinscanner

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class Prediction(
    val label: String,
    val confidence: Float
)

object MLAnalyzer {

    private var interpreter: Interpreter? = null
    private const val MODEL_FILE = "skin_model_updated.tflite"

    fun initialize(context: Context) {
        if (interpreter == null) {
            try {
                val assetFileDescriptor = context.assets.openFd(MODEL_FILE)
                val inputStream = assetFileDescriptor.createInputStream()
                val buffer = ByteArray(assetFileDescriptor.length.toInt())
                inputStream.read(buffer)

                val byteBuffer = ByteBuffer.allocateDirect(buffer.size)
                byteBuffer.order(ByteOrder.nativeOrder())
                byteBuffer.put(buffer)

                interpreter = Interpreter(byteBuffer)
                Log.d("MLAnalyzer", "Interpreter initialized successfully")

            } catch (e: Exception) {
                Log.e("MLAnalyzer", "Error initializing interpreter: ${e.message}")
            }
        }
    }

    private fun preprocessBitmap(bitmap: Bitmap): ByteBuffer {
        val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

        val inputBuffer = ByteBuffer.allocateDirect(1 * 224 * 224 * 3 * 4)
        inputBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(224 * 224)
        resized.getPixels(intValues, 0, 224, 0, 0, 224, 224)

        for (pixel in intValues) {
            val r = (pixel shr 16 and 0xFF) / 255f
            val g = (pixel shr 8 and 0xFF) / 255f
            val b = (pixel and 0xFF) / 255f

            inputBuffer.putFloat(r)
            inputBuffer.putFloat(g)
            inputBuffer.putFloat(b)
        }

        inputBuffer.rewind()
        return inputBuffer
    }

    suspend fun predict(bitmap: Bitmap): Prediction {
        if (interpreter == null) {
            Log.e("MLAnalyzer", "Interpreter not initialized")
            return Prediction("Unknown", 0f)
        }

        val input = preprocessBitmap(bitmap)
        val output = Array(1) { FloatArray(7) }

        interpreter?.run(input, output)

        // ✅ Softmax
        val logits = output[0]
        val exp = FloatArray(logits.size)
        var sumExp = 0.0

        for (i in logits.indices) {
            exp[i] = Math.exp(logits[i].toDouble()).toFloat()
            sumExp += exp[i]
        }

        val confidences = FloatArray(logits.size)
        for (i in exp.indices) {
            confidences[i] = (exp[i] / sumExp).toFloat()
        }

        // ✅ CORRECT LABEL ORDER
        val labels = listOf(
            "Actinic Keratosis",
            "Basal Cell Carcinoma",
            "Benign Keratosis",
            "Dermatofibroma",
            "Melanoma",
            "Nevus",
            "Vascular Lesion"
        )

        val maxIndex = confidences.indices.maxByOrNull { confidences[it] } ?: -1

        val finalLabel = if (maxIndex != -1) labels[maxIndex] else "Unknown"
        val finalConfidence = if (maxIndex != -1) confidences[maxIndex] else 0f

        Log.d("MLAnalyzer", "Softmax Output: ${confidences.joinToString()}")

        return Prediction(finalLabel, finalConfidence)
    }
}