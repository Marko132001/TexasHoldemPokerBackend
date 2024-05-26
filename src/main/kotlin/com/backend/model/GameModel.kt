package com.backend.model

import com.backend.data.GameRound
import com.backend.data.PlayerState
import com.backend.gamelogic.Game
import com.backend.gamelogic.LOGGER
import com.backend.gamelogic.Player
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

class GameModel() {
    private val gameState = MutableStateFlow(GameState())

    companion object {
        val game: Game = Game()
        val playerSeatPositions: Array<String?> = arrayOfNulls(5)
    }

    private val playerSockets = ConcurrentHashMap<String, WebSocketSession>()

    private val gameScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var delayGameJob: Job? = null
    private var updateBackendJob: Job? = null

    private lateinit var round: GameRound

    init {
        gameState.onEach(::broadcast).launchIn(gameScope)
        println("INIT BLOCK")
    }

    suspend fun connectPlayer(session: WebSocketSession, userData: UserData): Player? {
        updateBackendJob?.join()
        delayGameJob?.join()

        println("CONNECTING PLAYER")
        var player: Player? = null

        if(game.players.size == 5 || playerSockets.containsKey(userData.userId)){
            return null
        }

        player = Player(userData.userId, userData.username,
            userData.chipAmount, userData.avatarUrl)

        game.players.add(player)

        LOGGER.trace("CONNECTED USER ID: ${player.userId}")
        playerSockets[userData.userId] = session

        addPlayerToEmptySeat(player.userId)


        if(game.players.size == 2){
            resetGame()
        }
        else if(game.players.size > 2) {
            gameState.update { currentState ->
                currentState.copy(
                    playerSeatPositions = playerSeatPositions,
                    players = serializePlayerData(game.players)
                )
            }
        }

        return player
    }

    suspend fun disconnectPlayer(player: Player) {
        updateBackendJob?.join()
        delayGameJob?.join()

        val playerIndex = game.players.indexOf(player)
        game.players.removeAt(playerIndex)

        if(game.dealerButtonPos >= playerIndex) {
            if(game.dealerButtonPos == 0){
                game.dealerButtonPos = game.players.size - 1
            }
            else{
                game.dealerButtonPos--
            }
        }
        if(game.currentPlayerIndex >= playerIndex){
            if(game.currentPlayerIndex == 0){
                game.currentPlayerIndex = game.players.size - 1
            }
            else{
                game.currentPlayerIndex--
            }
        }

        playerSockets.remove(player.userId)
        removePlayerFromTable(player.userId)

        if (game.players.size < 2) {
            updateBackendJob?.cancel()
            delayGameJob?.cancel()
        }

        println("PLAYER ${player.username} DISCONNECTED")
        println("NUMBER OF PLAYERS ${game.players.size}")

        gameState.update { currentState ->
            currentState.copy(
                playerSeatPositions = playerSeatPositions,
                players = serializePlayerData(game.players),
                dealerButtonPos = game.dealerButtonPos,
                currentPlayerIndex = game.currentPlayerIndex
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
        updateBackendJob?.cancel()
        round = GameRound.PREFLOP

        game.preflopRoundInit()

        gameState.update { currentState ->
            currentState.copy(
                round = round,
                playerSeatPositions = playerSeatPositions,
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

    private suspend fun updateBettingRound() {
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
                playerSeatPositions = playerSeatPositions,
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

        updateBackendJob = gameScope.launch {
            updateBettingRound()
        }
    }

    fun handleRaiseAction(raiseAmount: Int) {
        game.updatePot(game.players[game.currentPlayerIndex]
            .raise(game.currentHighBet, raiseAmount)
        )
        game.currentHighBet = game.players[game.currentPlayerIndex].playerBet
        game.raiseFlag = true

        updateBackendJob = gameScope.launch {
            updateBettingRound()
        }
    }

    fun handleCheckAction() {
        game.players[game.currentPlayerIndex].check()

        updateBackendJob = gameScope.launch {
            updateBettingRound()
        }
    }

    fun handleFoldAction() {
        game.players[game.currentPlayerIndex].fold()

        updateBackendJob = gameScope.launch {
            updateBettingRound()
        }
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

    private fun addPlayerToEmptySeat(userId: String){
        playerSeatPositions.forEachIndexed { index, playerUserId ->
            if(playerUserId == null){
                playerSeatPositions[index] = userId
                return
            }
        }
    }

    private fun removePlayerFromTable(userId: String){
        playerSeatPositions.forEachIndexed { index, playerUserId ->
            if(playerUserId == userId){
                playerSeatPositions[index] = null
                return
            }
        }
    }

    private fun serializePlayerData(players:  List<Player>): List<PlayerDataState> {
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