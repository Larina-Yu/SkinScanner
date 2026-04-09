package com.example.skinscanner

import android.content.Context
import android.net.Uri
import android.net.http.HttpException
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.navigation.NavController
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.navigation.NavHostController
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


//Screen displayed selected image and machine learning prediction
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(photoUri: Uri, navController: NavHostController, onNext: () -> Unit) {
    val context = LocalContext.current

    var predictionLabel by remember { mutableStateOf("Analyzing...") }
    var predictionScore by remember { mutableStateOf(0f) }


    // Run TFLite prediction once the image URI is ready
    LaunchedEffect(photoUri) {
        try {
            val inputStream = context.contentResolver.openInputStream(photoUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) {
                predictionLabel = "Error loading image"
                predictionScore = 0f
                return@LaunchedEffect
            }

            val bitmap = Bitmap.createScaledBitmap(originalBitmap, 224, 224, true)

            // ✅ STEP 1: Skin detection
            val skinProportion = ImageProcessingUtils.calculateSkinProportion(bitmap)

            Log.d("SkinCheck", "Skin proportion: $skinProportion")

            if (skinProportion < 0.1) {
                predictionLabel = "Not enough skin detected"
                predictionScore = 0f
                return@LaunchedEffect
            }

            if (skinProportion < 0.05) {
                predictionLabel = "Image does not appear to be skin"
                predictionScore = 0f
                return@LaunchedEffect
            }

            // ✅ STEP 2: Prepare image for model
            val byteBuffer = ByteBuffer.allocateDirect(1 * 224 * 224 * 3 * 4)
            byteBuffer.order(ByteOrder.nativeOrder())

            val intValues = IntArray(224 * 224)
            bitmap.getPixels(intValues, 0, 224, 0, 0, 224, 224)

            for (pixel in intValues) {
                val r = (pixel shr 16 and 0xFF) / 255f
                val g = (pixel shr 8 and 0xFF) / 255f
                val b = (pixel and 0xFF) / 255f

                byteBuffer.putFloat(r)
                byteBuffer.putFloat(g)
                byteBuffer.putFloat(b)
            }

            byteBuffer.rewind()

            // ✅ STEP 3: Run model
            val model = loadModelFile(context, "skin_model_updated.tflite")
            val interpreter = Interpreter(model)

            // Map raw model output index to HAM10000 classes
            val labels = listOf(
                "Actinic Keratosis", // akiec
                "Basal Cell Carcinoma", // bcc
                "Benign Keratosis",  // bkl
                "Dermatofibroma",    // df
                "Melanoma",          // mel
                "Nevus",             // nv
                "Vascular Lesion"   // vasc
            )

            val output = Array(1) { FloatArray(labels.size) }

            interpreter.run(byteBuffer, output)

            val logits = output[0]
            val exp = FloatArray(logits.size)
            var sumExp = 0.0

            for(i in logits.indices) {
                    exp[i] = Math.exp(logits[i].toDouble()).toFloat()
                    sumExp += exp[i]
            }

            val confidences = FloatArray(logits.size)
            for (i in exp.indices) {
                confidences[i] = (exp[i] / sumExp).toFloat()
            }
            val maxIndex = confidences.indices.maxByOrNull { confidences[it] } ?: -1

            if (maxIndex != -1) {
                predictionLabel = labels[maxIndex]
                predictionScore = confidences[maxIndex]
            } else {
                predictionLabel = "Unknown"
                predictionScore = 0f
            }

            Log.d("ML", "Output: ${confidences.joinToString()}")

        } catch (e: Exception) {
            predictionLabel = "Error processing image"
            predictionScore = 0f
            Log.e("ML", "Error: ${e.message}")
        }
    }

    // UI
    Scaffold(topBar = { TopAppBar(title = { Text("Scan Result") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Selected Image:", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            Image(
                painter = rememberAsyncImagePainter(photoUri),
                contentDescription = "Selected Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(24.dp))

            if (predictionLabel.contains("skin", true) || predictionLabel.contains("Error", true)) {

                Text(predictionLabel, style = MaterialTheme.typography.titleMedium)

                Spacer(Modifier.height(16.dp))

                Button(onClick = { navController.navigate("camera") }) {
                    Text("Retake Image")
                }

                Spacer(Modifier.height(8.dp))

                Button(onClick = { navController.navigate("guidelines") }) {
                    Text("Image Guidelines")
                }

            } else {

                Text("Prediction: $predictionLabel", style = MaterialTheme.typography.titleMedium)

                Text("Confidence: ${(predictionScore * 100).toInt()}%")

                Spacer(Modifier.height(16.dp))

                val riskMessage = when {
                    predictionScore > 0.75 -> "High risk – consult a medical professional."
                    predictionScore > 0.5 -> "Moderate risk – consider getting this checked."
                    predictionScore > 0.25 -> "Low risk – monitor for changes."
                    else -> "Very low confidence – result may be unreliable."
                }

                Text(riskMessage)

                Spacer(Modifier.height(24.dp))

                Button(onClick = onNext) {
                    Text("Next / Account")
                }
            }

        }
    }
}

fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
    val fileDescriptor = context.assets.openFd(modelName)
    val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
    val fileChannel = inputStream.channel
    val startOffset = fileDescriptor.startOffset
    val declaredLength = fileDescriptor.declaredLength
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
}

/*

            //Upload image + prediction to backend
            auth.currentUser?.uid?.let { userId ->
                val inputStream = context.contentResolver.openInputStream(photoUri)
                val fileBytes = inputStream?.readBytes()
                inputStream?.close()

                if (fileBytes != null) {
                    val filePart = MultipartBody.Part.createFormData(
                        "file",
                        "image_${System.currentTimeMillis()}.jpg",
                        RequestBody.create("image/jpeg".toMediaType(), fileBytes)
                    )
                    val userIdPart = RequestBody.create("text/plain".toMediaType(), userId)
                    val lesionTypePart = RequestBody.create("text/plain".toMediaType(), analysisResult)

                    scope.launch {
                        try {
                            val response = ApiClient.apiService.uploadImage(
                                filePart, userIdPart, lesionTypePart
                            )
                            if (response.isSuccessful) {
                                Log.d("ResultScreen", "Upload successful")
                            } else {
                                Log.e("ResultScreen", "Upload failed: ${response.code()}")
                            }
                        } catch (e: Exception) {
                            Log.e("ResultScreen", "Upload exception: ${e.message}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            analysisResult = "Error processing image"
            Log.e("ResultScreen", "Error in LaunchedEffect: ${e.message}")
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            //Results section
            Text(
                text = "Here is your selected photo:",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            //Displaying selected image
            Image(
                painter = rememberAsyncImagePainter(photoUri),
                contentDescription = "Selected photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- PARSE RESULT ---
            val isNotSkin = analysisResult.contains("Not enough skin") || analysisResult.contains("Error")

            val parts = analysisResult.split(" ")
            val rawLabel = parts.getOrNull(0)?.lowercase() ?: "unknown"
            val score = parts.getOrNull(2)?.replace(")", "")?.toFloatOrNull() ?: 0f

            val cancerType = when (rawLabel) {
                "mel" -> "Melanoma"
                "nv" -> "Nevus"
                "bcc" -> "Basal Cell Carcinoma"
                "akiec" -> "Actinic Keratosis"
                "bkl" -> "Benign Keratosis"
                "df" -> "Dermatofibroma"
                "vasc" -> "Vascular Lesion"
                else -> "Unknown"
            }

            val riskMessage = when {
                score > 0.75 -> "High risk – please consult a medical professional."
                score > 0.5 -> "Moderate risk – consider getting this checked."
                score > 0.25 -> "Low risk – monitor for changes."
                else -> "Very low confidence – result may be unreliable."
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Conditional buttons ---
            if (isNotSkin) {
                Text(
                    text = "This doesn’t look like skin. Please take another photo.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.navigate("camera") }) {
                    Text("Retake Image")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { navController.navigate("guidelines") }) {
                    Text("Image Guidelines")
                }

            } else {
                Text("Prediction: $cancerType (${(score * 100).toInt()}%)", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(riskMessage, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onNext) { Text("Next") }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    val encodedLesion = Uri.encode(rawLabel)
                    navController.navigate("cancerInfo/$encodedLesion")
                }) {
                    Text("Learn About This Condition")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/*"
                        putExtra(Intent.EXTRA_STREAM, photoUri)
                        putExtra(Intent.EXTRA_TEXT, "Prediction: $cancerType (${(score * 100).toInt()}%)")
                    }
                    context.startActivity(Intent.createChooser(intent, "Share via"))
                }) {
                    Text("Share Result")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { navController.navigate("account") }) {
                    Text("Go to Account")
                }
            }
        }
    }
}
*/