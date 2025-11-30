/*package com.example.skinscanner.firebase

import com.google.firebase.auth.FirebaseAuth

object FirebaseAuthManager {

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    fun register(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { err ->
                callback(false, err.message)
            }
    }

    fun login(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { err ->
                callback(false, err.message)
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun currentUser() = auth.currentUser
}
*/