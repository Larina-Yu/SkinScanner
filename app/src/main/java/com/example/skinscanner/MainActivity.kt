package com.example.skinscanner

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import java.io.File
import androidx.compose.material3.CenterAlignedTopAppBar
//import com.google.firebase.FirebaseApp
//import com.google.firebase.auth.ktx.auth
//import com.google.firebase.ktx.Firebase


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //FirebaseApp.initializeApp(this)
        setContent {
            SkinScannerTheme {
                AppFlow()
            }
        }
    }

    // Main navigation flow with dropdown
    @Composable
    fun AppFlow() {
        var currentScreen by remember { mutableStateOf("home") }
        var confirmedPhotoUri by remember { mutableStateOf<Uri?>(null) }

        Scaffold(
            topBar = {
                TopAppBarWithMenu(
                    currentScreen = currentScreen,
                    onNavigate = { destination ->
                        currentScreen = destination
                        confirmedPhotoUri = null // reset when leaving scan flow
                    }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (currentScreen) {
                    "home" -> HomeScreen(onStartScan = { currentScreen = "camera" })
                    "camera" -> CameraScreen(onPhotoConfirmed = { uri ->
                        confirmedPhotoUri = uri
                        currentScreen = "result"
                    })

                    "result" -> confirmedPhotoUri?.let {
                        ResultScreen(photoUri = it, onNext = { currentScreen = "settings" })
                    }

                    "settings" -> SettingsScreen()
                }
            }
        }
    }

    // Simple Top Bar with Dropdown Menu
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopAppBarWithMenu(currentScreen: String, onNavigate: (String) -> Unit) {
        var expanded by remember { mutableStateOf(false) }

        CenterAlignedTopAppBar(
            title = { Text("SkinScanner") },
            actions = {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu"
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Home") },
                        onClick = {
                            expanded = false
                            onNavigate("home")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Scan") },
                        onClick = {
                            expanded = false
                            onNavigate("camera")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Settings") },
                        onClick = {
                            expanded = false
                            onNavigate("settings")
                        }
                    )
                }
            }
        )
    }

    // Home Screen (Landing Page)
    @Composable
    fun HomeScreen(onStartScan: () -> Unit) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Welcome to SkinScanner", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(24.dp))
                Button(onClick = onStartScan) {
                    Text("Start Scan")
                }
            }
        }
    }

    // Camera + Upload Screen
    @Composable
    fun CameraScreen(onPhotoConfirmed: (Uri) -> Unit) {
        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

        val galleryLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri // this is usually full-res
            }
        }

        val tempFile = File(this.cacheDir, "full_photo_${System.currentTimeMillis()}.jpg")
        val cameraUri: Uri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            tempFile
        )

        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { success: Boolean ->
            if (success) {
                selectedImageUri = cameraUri // full-res image
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
                verticalArrangement = Arrangement.Center
            ) {
                if (selectedImageUri == null) {
                    Button(
                        onClick = { cameraLauncher.launch(cameraUri) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
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
                            .heightIn(max = 600.dp),
                        contentScale = ContentScale.Fit // preserves full image
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

    // Results Screen
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
                        .wrapContentHeight(), // lets the image take natural height
                    contentScale = ContentScale.Fit // preserves the whole image
                )

                Spacer(modifier = Modifier.height(24.dp))
                Text("Next, we can run analysis or show results here.")
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onNext) { Text("Next") }
            }
        }
    }

    // Settings Screen
    @Composable
    fun SettingsScreen() {
        var darkMode by remember { mutableStateOf(false) }
        var fontSize by remember { mutableStateOf(16f) }

        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Settings", style = MaterialTheme.typography.titleLarge)

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Dark Mode")
                    Switch(checked = darkMode, onCheckedChange = { darkMode = it })
                }

                Text("Font Size: ${fontSize.toInt()}sp")
                Slider(value = fontSize, onValueChange = { fontSize = it }, valueRange = 12f..24f)

                Divider()
                Text("Accessibility and account options coming soon…")
            }
        }
    }

}
