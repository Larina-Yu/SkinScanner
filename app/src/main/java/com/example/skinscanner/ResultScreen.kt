package com.example.skinscanner

import android.content.Context
import android.net.Uri
import android.net.http.HttpException
import android.os.Build
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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File


//Screen displayed selected image and machine learning prediction
@Composable
fun ResultScreen(photoUri: Uri, onNext: () -> Unit) {

    val context = LocalContext.current
    val auth = Firebase.auth
    val scope = rememberCoroutineScope()

    // ML Analyzer placeholder
    //val analyzer = remember { SkinAnalyzer(context) }

    // Analysis result state
    var analysisResult by remember { mutableStateOf("Analyzing...") }

    LaunchedEffect(photoUri) {
        // Example ML analysis
        analysisResult = withContext(Dispatchers.Default) {
            SkinAnalyzer(context).analyzeImage(photoUri)
        }

        // Upload image to backend
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
                            Toast.makeText(context, "Upload successful", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("ResultScreen", "Upload failed: ${response.code()}")
                        }
                    } catch (e: Exception) {
                        Log.e("ResultScreen", "Upload exception: ${e.message}")
                    }
                }
            }
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

            //Displaying analysis result
            Text(
                text = analysisResult,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onNext) {
                Text("Next")
            }
        }
    }
}
