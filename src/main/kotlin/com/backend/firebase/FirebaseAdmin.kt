package com.backend.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.engine.ApplicationEngineEnvironment
import java.io.FileInputStream
import java.io.InputStream

class FirebaseAdmin(private val firebaseSecretsPath: String?) {
    private val serviceAccount: FileInputStream = FileInputStream(firebaseSecretsPath)

    private val options: FirebaseOptions = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .setDatabaseUrl("https://pokerapp-8f562.firebaseio.com")
        .build()

    fun init() {
        FirebaseApp.initializeApp(options)
    }
}