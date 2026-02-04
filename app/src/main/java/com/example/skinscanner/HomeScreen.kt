package com.example.skinscanner

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.skinscanner.firebase.FirebaseAuthManager
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun HomeScreen(onStartScan: () -> Unit, navController: NavController) {
    val currentUser = FirebaseAuthManager.currentUser()
    val isLoggedIn = currentUser != null

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

            if (isLoggedIn) {
                Text("Logged in as: ${currentUser?.email}")
            } else {
                Text("Guest Mode - Data will not be saved")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onStartScan) {
                Text("Start Scan")
            }

            if (isLoggedIn) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    FirebaseAuthManager.logout()
                    navController.navigate("auth") {
                        popUpTo("auth") { inclusive = true }
                    }
                }) {
                    Text("Logout")
                }
            }
        }
    }
}
