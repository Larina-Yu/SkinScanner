package com.example.skinscanner

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

object MLAnalyzer {

    //TensorFlow Lite intepreter instance
    private var interpreter: Interpreter? = null
    private const val MODEL_FILE = "skin_lesion_model.tflite"

    // Initialize interpreter once
    fun initialize(context: Context) {
        if (interpreter == null) {
            val assetFileDescriptor = context.assets.openFd(MODEL_FILE)
            val inputStream = assetFileDescriptor.createInputStream()
            val buffer = ByteArray(assetFileDescriptor.length.toInt())
            inputStream.read(buffer)
            val byteBuffer = ByteBuffer.allocateDirect(buffer.size)
            byteBuffer.order(ByteOrder.nativeOrder())
            byteBuffer.put(buffer)
            interpreter = Interpreter(byteBuffer)
        }
    }

    //Preprocess bitmap for model input
    private fun preprocessBitmap(bitmap: Bitmap): ByteBuffer {
        val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        //Buffer allocation for model input
        val inputBuffer = ByteBuffer.allocateDirect(1 * 224 * 224 * 3 * 4)
        inputBuffer.order(ByteOrder.nativeOrder())

        //Extract pixel data and normalise to [0,1]
        val intValues = IntArray(224 * 224)
        resized.getPixels(intValues, 0, 224, 0, 0, 224, 224)

        for (pixel in intValues) {
            val r = ((pixel shr 16) and 0xFF) / 255.0f
            val g = ((pixel shr 8) and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f
            inputBuffer.putFloat(r)
            inputBuffer.putFloat(g)
            inputBuffer.putFloat(b)
        }
        inputBuffer.rewind()
        return inputBuffer
    }

    //Run prediction on bitmap
    suspend fun predict(bitmap: Bitmap): String {
        if (interpreter == null) {
            return "Interpreter not initialized"
        }

        val input = preprocessBitmap(bitmap)
        val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 1), org.tensorflow.lite.DataType.FLOAT32)

        interpreter!!.run(input, outputBuffer.buffer.rewind())

        val score = outputBuffer.floatArray[0]
        return if (score >= 0.5) "Malignant lesion detected (Score: %.2f)".format(score)
        else "Benign lesion detected (Score: %.2f)".format(score)
    }
}
