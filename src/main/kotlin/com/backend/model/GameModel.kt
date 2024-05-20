package com.backend.model

import com.backend.data.GameRound
import com.backend.gamelogic.Game
import com.backend.gamelogic.LOGGER
import com.backend.gamelogic.Player
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

class GameModel() {
    private val gameState = MutableStateFlow(GameState())

    companion object {
        val game: Game = Game()
    }

    private val playerSockets = ConcurrentHashMap<String, WebSocketSession>()

    private val gameScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var delayGameJob: Job? = null

    private lateinit var round: GameRound

    init {
        gameState.onEach(::broadcast).launchIn(gameScope)
        println("INIT BLOCK")
    }

    fun connectPlayer(session: WebSocketSession, userData: UserData): Player? {
        println("CONNECTING PLAYER")
        var player: Player? = null

        if(game.players.size == 5){
            return null
        }

        if(!playerSockets.containsKey(userData.userId)){
            player = Player(userData.userId, userData.username,
                userData.chipAmount, userData.avatarUrl)
            game.players.add(player)
            LOGGER.trace("CONNECTED USER ID: ${player.userId}")
            playerSockets[userData.userId] = session
        }

        if(game.players.size == 2){
            resetGame()
        }
        else if(game.players.size > 2) {
            gameState.update { currentState ->
                currentState.copy(
                    players = serializePlayerData(game.players)
                )
            }
        }

        return player
    }

    fun disconnectPlayer(player: Player) {
        game.players.remove(player)
        playerSockets.remove(player.username)

        println("PLAYER ${player.username} DISCONNECTED")
        println("NUMBER OF PLAYERS ${game.players.size}")

        gameState.update { currentState ->
            currentState.copy(
                players = serializePlayerData(game.players)
            )
        }
    }

    private suspend fun broadcast(state: GameState) {
        playerSockets.values.forEach {socket ->
            println("BROADCASTING STATE")
            socket.send(
                Json.encodeToString(state)
            )
        }

        if(game.players.size > 1 && round != GameRound.SHOWDOWN){
            delayGameJob = gameScope.launch {
                launchPlayerTimer()
            }
        }
    }

    private fun resetGame() {
        round = GameRound.PREFLOP

        game.preflopRoundInit()

        gameState.update { currentState ->
            currentState.copy(
                round = round,
                potAmount = game.potAmount,
                bigBlind = game.bigBlind,
                currentHighBet = game.currentHighBet,
                dealerButtonPos = game.dealerButtonPos,
                players = serializePlayerData(game.players),
                currentPlayerIndex = game.currentPlayerIndex,
                communityCards = game.showStreet(round).map { it.cardLabel },
                isRaiseEnabled = true,
                isCallEnabled = true,
                isCheckEnabled = false,
                isFoldEnabled = true
            )
        }
    }

    private fun updateBettingRound() {
        delayGameJob?.cancel()
        val nextRound = game.nextRoundInit(round)

        if(nextRound == GameRound.SHOWDOWN){
            round = nextRound
            game.assignChipsToWinner(game.rankCardHands())

            gameState.update { currentState ->
                currentState.copy(
                    round = round,
                    communityCards = game.showStreet(round).map { it.cardLabel }
                )
            }

            delayGameJob = gameScope.launch {
                delay(4000)
                delayGameJob?.cancel()
                resetGame()
            }

        }
        else if(nextRound != round) {
            round = nextRound

            gameState.update { currentState ->
                currentState.copy(
                    round = round,
                    communityCards = game.showStreet(round).map { it.cardLabel },
                    isRaiseEnabled = true
                )
            }
        }

        updateAvailableActions()
    }

    private fun updateAvailableActions() {
        gameState.update { currentState ->
            currentState.copy(
                potAmount = game.potAmount,
                players = serializePlayerData(game.players),
                currentHighBet = game.currentHighBet,
                currentPlayerIndex = game.currentPlayerIndex,
                isCheckEnabled =
                game.currentHighBet <= game.players[game.currentPlayerIndex].playerBet
                        && round != GameRound.SHOWDOWN,
                isRaiseEnabled =
                game.currentHighBet < game.players[game.currentPlayerIndex].chipBuyInAmount
                        && round != GameRound.SHOWDOWN,
                isCallEnabled =
                game.currentHighBet > game.players[game.currentPlayerIndex].playerBet
                        && round != GameRound.SHOWDOWN,
                isFoldEnabled = round != GameRound.SHOWDOWN
            )
        }
    }

    fun handleCallAction() {
        game.updatePot(game.players[game.currentPlayerIndex].call(game.currentHighBet))

        updateBettingRound()
    }

    fun handleRaiseAction(raiseAmount: Int) {
        game.updatePot(game.players[game.currentPlayerIndex]
            .raise(game.currentHighBet, raiseAmount)
        )
        game.currentHighBet = game.players[game.currentPlayerIndex].playerBet
        game.raiseFlag = true

        updateBettingRound()
    }

    fun handleCheckAction() {
        game.players[game.currentPlayerIndex].check()

        updateBettingRound()
    }

    fun handleFoldAction() {
        game.players[game.currentPlayerIndex].fold()

        updateBettingRound()
    }

    private suspend fun launchPlayerTimer(){
        delay(11000)
        delayGameJob?.cancel()

        if(gameState.value.isCheckEnabled && game.players.size > 1){
            handleCheckAction()
        }
        else if(gameState.value.isFoldEnabled && game.players.size > 1){
            handleFoldAction()
        }
    }

    private fun serializePlayerData(players:  MutableList<Player>): MutableList<PlayerDataState> {
        val playerData: MutableList<PlayerDataState> = mutableListOf()

        players.forEach { player ->
            playerData.add(
                PlayerDataState(
                    userId = player.userId,
                    username = player.username,
                    avatarUrl = player.avatarUrl,
                    chipBuyInAmount = player.chipBuyInAmount,
                    holeCards = player.getHoleCardsLabels(),
                    playerHandRank = player.playerHandRank.first.label,
                    playerState = player.playerState,
                    playerBet = player.playerBet
                )
            )
        }

        return playerData
    }
}