package com.backend.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import java.io.FileInputStream
import java.io.InputStream

object FirebaseAdmin {
    private val serviceAccount: FileInputStream = FileInputStream("/app/pokerapp-8f562-firebase-adminsdk-7n239-13a746135f.json")

    private val options: FirebaseOptions = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .setDatabaseUrl("https://pokerapp-8f562.firebaseio.com")
        .build()

    fun init() {
        FirebaseApp.initializeApp(options)
    }
}