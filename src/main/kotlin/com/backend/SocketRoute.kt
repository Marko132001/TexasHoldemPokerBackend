package com.backend

import com.backend.data.PlayerState
import com.backend.gamelogic.LOGGER
import com.backend.gamelogic.Player
import com.backend.gamelogic.User
import com.backend.model.GameModel
import com.backend.model.PlayerAction
import com.backend.model.UserData
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.lang.Exception

fun Route.socket(gameModel: GameModel) {
    route("/play") {
        webSocket {
            var player: Player? = null

            try {
                incoming.consumeEach { frame ->
                    if(frame is Frame.Text) {
                        val message = frame.readText()
                        val type = message.substringBefore("#")
                        val body = message.substringAfter("#")
                        if(type == "make_turn"){
                            val action: PlayerAction = Json.decodeFromString(body)

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
                        else if(type == "user_data"){
                            val userData: UserData = Json.decodeFromString(body)

                            player = gameModel.connectPlayer(this, userData)

                            if(player == null) {
                                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT,
                                    "5 players already connected"))
                                return@webSocket
                            }
                        }
                        else if(type == "user_rebuy"){
                            val userData: UserData = Json.decodeFromString(body)

                            gameModel.rebuyPlayerChips(userData)
                        }
                    }
                }
            } catch(e: Exception){
                e.printStackTrace()
            } finally {
                if (player != null) {
                    gameModel.disconnectPlayer(player!!)
                }
            }
        }
    }
}