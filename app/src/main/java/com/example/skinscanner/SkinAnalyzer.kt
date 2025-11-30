package com.example.skinscanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import org.tensorflow.lite.Interpreter
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

//Class to analyse skin lesion images using TensorFlow Lite model
class SkinAnalyzer(private val context: Context) {

    private val modelPath = "skin_lesion_model.tflite"

    //Model is only loaded when first needed
    private val interpreter: Interpreter by lazy {
        Interpreter(loadModelFile())
    }

    private fun loadModelFile(): ByteBuffer {
        val inputStream: InputStream = context.assets.open(modelPath)
        val bytes = inputStream.readBytes()
        //Allocates a direct ByteBuffer with native byte order - efficient for memory access
        return ByteBuffer.allocateDirect(bytes.size)
            .order(ByteOrder.nativeOrder())
            .apply { put(bytes) }
    }

    //Converts uri to bitmap
    private fun uriToBitmap(uri: Uri): Bitmap {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)
            ?: throw IllegalArgumentException("Could not decode bitmap from URI")
    }

    //Preprocessed the bitmap into a bytebuffer for tensorflow lite input
    private fun preprocess(bmp: Bitmap): ByteBuffer {
        val resized = Bitmap.createScaledBitmap(bmp, 224, 224, true)
        val buffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
        buffer.order(ByteOrder.nativeOrder())

        //Iterating over each pixel in the resized bit map
        for (y in 0 until 224) {
            for (x in 0 until 224) {
                val pixel = resized.getPixel(x, y)
                //Extracting RGB components
                buffer.putFloat(((pixel shr 16 and 0xFF) / 255f))
                buffer.putFloat(((pixel shr 8 and 0xFF) / 255f))
                buffer.putFloat(((pixel and 0xFF) / 255f))
            }
        }

        buffer.rewind()
        return buffer
    }

    //Analysing image using tensorflow lite model
    fun analyzeImage(uri: Uri): String {
        val bitmap = uriToBitmap(uri)
        val input = preprocess(bitmap)

        val output = Array(1) { FloatArray(1) } // adjust shape if your model outputs differently
        interpreter.run(input, output)
        val score = output[0][0]

        //Returning result based on confidence score
        return if (score > 0.5f) {
            "Possible skin issue detected (score: ${"%.2f".format(score)})"
        } else {
            "Looks normal (score: ${"%.2f".format(score)})"
        }
    }
}
