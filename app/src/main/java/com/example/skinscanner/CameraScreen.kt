package com.example.skinscanner

import android.Manifest
import android.net.Uri
import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner

@Composable
fun CameraScreen(navController: NavController) {
    val context = LocalContext.current
    var imageCapture: ImageCapture? = remember { null }

    // Request Camera Permission on first launch
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
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

        //Camera preview setup
        AndroidView(factory = { previewView }, modifier = Modifier.weight(1f)) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                //Setup preview
                val preview = Preview.Builder()
                    .setTargetResolution(Size(1280, 960)) // full resolution
                    .build()
                    .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                //Setup image capture
                imageCapture = ImageCapture.Builder()
                    .setTargetResolution(Size(1280, 960))
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .setJpegQuality(95)
                    .build()

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        context as LifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture
                    )
                } catch (exc: Exception) {
                    exc.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(context))
        }

        //capture button
        Button(
            onClick = {
                val fileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                    .format(System.currentTimeMillis()) + ".jpg"
                val file = File(context.cacheDir, fileName)

                imageCapture?.takePicture(
                    ImageCapture.OutputFileOptions.Builder(file).build(),
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            val imageUri: Uri = Uri.fromFile(file)
                            // Navigate to ResultsScreen with full file URI
                            navController.navigate("result/${imageUri}")
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
