/*package com.example.skinscanner.firebase

import com.google.firebase.firestore.FirebaseFirestore

object FirebaseFirestoreManager {

    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    fun saveUserData(uid: String, data: Map<String, Any>, callback: (Boolean, String?) -> Unit) {
        db.collection("users")
            .document(uid)
            .set(data)
            .addOnSuccessListener { callback(true, null) }
            .addOnFailureListener { err -> callback(false, err.message) }
    }

    fun getUserData(uid: String, callback: (Map<String, Any>?, String?) -> Unit) {
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                callback(snapshot.data, null)
            }
            .addOnFailureListener { err ->
                callback(null, err.message)
            }
    }
}
*/