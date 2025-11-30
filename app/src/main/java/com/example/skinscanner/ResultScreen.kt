package com.example.skinscanner

import android.net.Uri
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

//Screen displayed selected image and machine learning prediction
@Composable
fun ResultScreen(photoUri: Uri, onNext: () -> Unit) {
    val context = LocalContext.current
    //Initializing for image processing and classification
    val analyzer = remember { SkinAnalyzer(context) }

    //Holding analysis result
    var analysisResult by remember { mutableStateOf("Analyzing...") }

    //Launching image analysis when screen is processed
    LaunchedEffect(photoUri) {
        analysisResult = analyzer.analyzeImage(photoUri)
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

            /*Button(onClick = onNext) {
                Text("Next")
            }*/
        }
    }
}
