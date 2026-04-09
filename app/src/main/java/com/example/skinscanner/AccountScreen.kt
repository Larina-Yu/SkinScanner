package com.example.skinscanner

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.skinscanner.firebase.FirebaseAuthManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(navController: NavHostController) {

    var images by remember { mutableStateOf<List<ImageData>>(emptyList()) }

    val firebaseUser = Firebase.auth.currentUser
    firebaseUser?.let { user ->
        RetrofitClient.instance.getImages(user.uid)
            .enqueue(object : Callback<List<ImageData>> {
                override fun onResponse(
                    call: Call<List<ImageData>>,
                    response: Response<List<ImageData>>
                ) {
                    if (response.isSuccessful) {
                        images = (response.body() ?: emptyList()).reversed()
                    }
                }

                override fun onFailure(call: Call<List<ImageData>>, t: Throwable) {
                    t.printStackTrace()
                }
            })
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Account") })
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            items(images) { image ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Image(
                            painter = rememberAsyncImagePainter(
                                //"http://10.156.37.22:5000/${image.filename}"
                                "http://192.168.1.4:5000/images/${image.filename}"
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Prediction: ${image.lesion_type}")
                        Text("Uploaded: ${image.upload_date}")
                    }

                    Button(onClick = {
                        RetrofitClient.instance.deleteImage(image.filename)
                            .enqueue(object : Callback<Void> {
                                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                    images = images.filter { it.filename != image.filename }
                                }

                                override fun onFailure(call: Call<Void>, t: Throwable) {
                                    t.printStackTrace()
                                }
                            })
                    }) {
                        Text("Delete")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val user = FirebaseAuthManager.currentUser()
                        user?.let {
                            // Call backend delete
                            RetrofitClient.instance.deleteUser(it.uid)
                                .enqueue(object : Callback<Void> {
                                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                        FirebaseAuthManager.logout()
                                        navController.navigate("auth") {
                                            popUpTo("auth") { inclusive = true }
                                        }
                                    }

                                    override fun onFailure(call: Call<Void>, t: Throwable) {
                                        t.printStackTrace()
                                    }
                                })
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete Account")
                }
            }
        }
    }
}
