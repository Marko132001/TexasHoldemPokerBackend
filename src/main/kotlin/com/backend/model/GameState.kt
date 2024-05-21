package com.backend.model

import com.backend.data.GameRound
import com.backend.gamelogic.Player
import kotlinx.serialization.Serializable

@Serializable
data class GameState(
    val round: GameRound = GameRound.PREFLOP,
    val playerSeatPositions: Array<String?> = arrayOfNulls(5),
    val potAmount: Int = 0,
    val bigBlind: Int = 0,
    val currentHighBet: Int = 0,
    val dealerButtonPos: Int = 0,
    val players: List<PlayerDataState> = listOf(),
    val currentPlayerIndex: Int = 0,
    val communityCards: List<String> = listOf(),
    val isRaiseEnabled: Boolean = true,
    val isCallEnabled: Boolean = true,
    val isCheckEnabled: Boolean = false,
    val isFoldEnabled: Boolean = true
)
