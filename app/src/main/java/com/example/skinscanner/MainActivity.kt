package com.example.skinscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import java.io.File
import android.net.Uri
import androidx.core.content.FileProvider
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.example.skinscanner.CameraScreen
import com.example.skinscanner.ResultScreen
import com.example.skinscanner.SkinScannerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SkinScannerTheme {
                AppFlow()
            }
        }
    }

    @Composable
    fun AppFlow() {
        var confirmedPhotoUri by remember { mutableStateOf<Uri?>(null) }
        var showNextStep by remember { mutableStateOf(false) }

        when {
            confirmedPhotoUri == null -> {
                CameraScreen(
                    onPhotoConfirmed = { uri -> confirmedPhotoUri = uri }
                )
            }
            !showNextStep -> {
                ResultScreen(
                    photoUri = confirmedPhotoUri!!,
                    onNext = { showNextStep = true }
                )
            }
            else -> {
                // Replace with your next feature screen
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Next Step Goes Here")
                }
            }
        }
    }

    @Composable
    fun CameraScreen(onPhotoConfirmed: (Uri) -> Unit) {
        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

        val galleryLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? -> selectedImageUri = uri }

        val tempFile = File(cacheDir, "temp_photo.jpg")
        val cameraUri: Uri = FileProvider.getUriForFile(
            this@MainActivity,
            "${packageName}.provider",
            tempFile
        )

        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { success: Boolean ->
            if (success) selectedImageUri = cameraUri
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
                verticalArrangement = Arrangement.Center
            ) {
                if (selectedImageUri == null) {
                    Button(
                        onClick = { cameraLauncher.launch(cameraUri) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Text("Take Photo")
                    }

                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Upload from Gallery")
                    }
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(selectedImageUri),
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { selectedImageUri = null },
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        ) {
                            Text("Retake / Choose Again")
                        }

                        Button(
                            onClick = { selectedImageUri?.let { onPhotoConfirmed(it) } },
                            modifier = Modifier.weight(1f).padding(start = 8.dp)
                        ) {
                            Text("Okay")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ResultScreen(photoUri: Uri, onNext: () -> Unit) {
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
                Text(
                    text = "Here is your selected photo:",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Image(
                    painter = rememberAsyncImagePainter(photoUri),
                    contentDescription = "Confirmed Photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Next, we can run analysis or show results here.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = { onNext() }) {
                    Text("Next")
                }
            }
        }
    }
}