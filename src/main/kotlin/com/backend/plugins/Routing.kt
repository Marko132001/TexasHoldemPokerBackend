package com.backend.plugins

import com.backend.model.GameModel
import com.backend.socket
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(gameModel: GameModel) {
    routing {
        socket(gameModel)
    }
}
