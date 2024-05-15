package com.backend

import com.backend.gamelogic.Game
import com.backend.gamelogic.LOGGER
import com.backend.model.GameModel
import com.backend.plugins.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("unused")
fun Application.module() {
    val gameModel = GameModel()
    configureSockets()
    configureSerialization()
    configureMonitoring()
    configureRouting(gameModel)
}
