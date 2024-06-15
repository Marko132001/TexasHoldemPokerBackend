package com.backend

import com.backend.firebase.FirebaseAdmin
import com.backend.model.GameModel
import com.backend.plugins.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("unused")
fun Application.module() {
    val firebaseSecrets = environment.config.propertyOrNull("ktor.deployment.firebase_secrets_path")?.getString()
    val firebaseAdmin = FirebaseAdmin(firebaseSecrets)
    firebaseAdmin.init()
    val gameModel = GameModel()
    configureSockets()
    configureSerialization()
    configureMonitoring()
    configureRouting(gameModel)
}
