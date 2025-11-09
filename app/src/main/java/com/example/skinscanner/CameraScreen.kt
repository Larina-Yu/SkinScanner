package com.example.skinscanner

import android.Manifest
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker

@Composable
fun CameraScreen(navController: NavController) {
    val context = LocalContext.current
    var imageCapture: ImageCapture? = remember { null }

    // Request Camera Permission if not granted
    LaunchedEffect(Unit) {
        if (PermissionChecker.checkSelfPermission(context, Manifest.permission.CAMERA) !=
            PermissionChecker.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as android.app.Activity,
                arrayOf(Manifest.permission.CAMERA),
                0
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        val previewView = remember { PreviewView(context) }
        AndroidView(factory = { previewView }, modifier = Modifier.weight(1f)) { view ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = androidx.camera.core.Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                imageCapture = ImageCapture.Builder().build()
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        context as androidx.lifecycle.LifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture
                    )
                } catch (exc: Exception) {
                    exc.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(context))
        }

        Button(
            onClick = {
                val fileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                    .format(System.currentTimeMillis()) + ".jpg"
                val file = File(context.cacheDir, fileName)
                imageCapture?.takePicture(
                    androidx.camera.core.ImageCapture.OutputFileOptions.Builder(file).build(),
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            val imageUri: Uri = Uri.fromFile(file)
                            // Placeholder: Run analysis
                            val result = analyzeSkinImage(imageUri)
                            navController.navigate("result/$result")
                        }

                        override fun onError(exception: ImageCaptureException) {
                            exception.printStackTrace()
                        }
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Capture Image")
        }
    }
}

// Placeholder analysis function
fun analyzeSkinImage(imageUri: Uri): String {
    // TODO: Replace with actual ML analysis
    return "Skin looks healthy!"
}
