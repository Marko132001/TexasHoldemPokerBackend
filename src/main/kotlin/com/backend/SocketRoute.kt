package com.backend

import com.backend.data.PlayerState
import com.backend.gamelogic.LOGGER
import com.backend.model.GameModel
import com.backend.model.PlayerAction
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json
import java.lang.Exception

fun Route.socket(gameModel: GameModel) {
    route("/play") {
        webSocket {
            val player = gameModel.connectPlayer(this)

            if(player == null) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT,
                    "5 players already connected"))
                return@webSocket
            }

            try {
                incoming.consumeEach { frame ->
                    if(frame is Frame.Text) {
                        val action = extractAction(frame.readText())
                        LOGGER.trace(action.playerState)
                        when(action.playerState){
                            PlayerState.CALL.name ->
                                gameModel.handleCallAction()
                            PlayerState.CHECK.name ->
                                gameModel.handleCheckAction()
                            PlayerState.FOLD.name ->
                                gameModel.handleFoldAction()
                            PlayerState.RAISE.name ->
                                gameModel.handleRaiseAction(action.raiseAmount)
                        }
                    }
                }
            } catch(e: Exception){
                e.printStackTrace()
            } finally {
                gameModel.disconnectPlayer(player)
            }
        }
    }
}

private fun extractAction(message: String): PlayerAction {
    return Json.decodeFromString(message)
}