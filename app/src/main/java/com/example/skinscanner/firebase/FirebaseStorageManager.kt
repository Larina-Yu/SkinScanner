package com.example.skinscanner.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

object FirebaseStorageManager {

    private val storage = FirebaseStorage.getInstance().reference

    fun uploadImage(imageUri: Uri, callback: (Boolean, String?) -> Unit) {
        val fileRef = storage.child("lesions/${UUID.randomUUID()}.jpg")

        fileRef.putFile(imageUri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { url ->
                    callback(true, url.toString())
                }
            }
            .addOnFailureListener { err ->
                callback(false, err.message)
            }
    }
}


