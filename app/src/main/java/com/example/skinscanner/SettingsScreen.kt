package com.example.skinscanner

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    var fontSize by remember { mutableStateOf(16f) }
    var darkMode by remember { mutableStateOf(false) }


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
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
            Slider(
                value = fontSize,
                onValueChange = { fontSize = it },
                valueRange = 12f..24f
            )

            Divider()
            Text("Account & Accessibility settings coming soon…", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
