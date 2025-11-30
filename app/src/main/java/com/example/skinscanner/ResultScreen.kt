package com.example.skinscanner

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.Alignment

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

            // Show full image, no cropping
            Image(
                painter = rememberAsyncImagePainter(photoUri),
                contentDescription = "Confirmed Photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(), // lets the image take natural height
                contentScale = ContentScale.Fit // preserves the whole image
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Next, we can run analysis or show results here.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onNext) {
                Text("Next")
            }
        }
    }
}
