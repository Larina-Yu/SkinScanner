package com.example.skinscanner

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

val LESION_INFO = mapOf(
    "akiec" to "Actinic Keratoses: precancerous lesion, monitor closely.",
    "bcc" to "Basal Cell Carcinoma: slow-growing skin cancer, see a doctor.",
    "bkl" to "Benign Keratosis-like lesion: non-cancerous growth.",
    "df" to "Dermatofibroma: benign skin growth, usually harmless.",
    "mel" to "Melanoma: malignant, needs urgent medical attention!",
    "nv" to "Nevus: common mole, usually harmless.",
    "vasc" to "Vascular lesion: birthmark or hemangioma, non-cancerous."
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(navController: NavController, lesion: String) {
    val infoText = LESION_INFO[lesion] ?: "No information available"

    val fullName = when (lesion) {
        "mel" -> "Melanoma"
        "nv" -> "Melanocytic Nevus"
        "bcc" -> "Basal Cell Carcinoma"
        "akiec" -> "Actinic Keratosis"
        "bkl" -> "Benign Keratosis"
        "df" -> "Dermatofibroma"
        "vasc" -> "Vascular Lesion"
        else -> lesion
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Lesion Info") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Condition: $fullName", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(infoText, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "If you are concerned about this result, consider seeking medical advice.",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Options:")
        Text("• Visit a GP or dermatologist")
        Text("• Use online telehealth services")
        Text("• Contact local health services (e.g. HSE in Ireland)")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { navController.navigate("account") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Go to Account")
        }
    }
}