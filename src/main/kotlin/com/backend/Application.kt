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
    FirebaseAdmin.init()
    val gameModel = GameModel()
    configureSockets()
    configureSerialization()
    configureMonitoring()
    configureRouting(gameModel)
}
