package com.backend.model

import com.backend.data.GameRound
import com.backend.gamelogic.Player
import kotlinx.serialization.Serializable

@Serializable
data class GameState(
    val round: GameRound = GameRound.PREFLOP,
    val isEnoughPlayers: Boolean = false,
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
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameState

        if (round != other.round) return false
        if (isEnoughPlayers != other.isEnoughPlayers) return false
        if (!playerSeatPositions.contentEquals(other.playerSeatPositions)) return false
        if (potAmount != other.potAmount) return false
        if (bigBlind != other.bigBlind) return false
        if (currentHighBet != other.currentHighBet) return false
        if (dealerButtonPos != other.dealerButtonPos) return false
        if (players != other.players) return false
        if (currentPlayerIndex != other.currentPlayerIndex) return false
        if (communityCards != other.communityCards) return false
        if (isRaiseEnabled != other.isRaiseEnabled) return false
        if (isCallEnabled != other.isCallEnabled) return false
        if (isCheckEnabled != other.isCheckEnabled) return false
        if (isFoldEnabled != other.isFoldEnabled) return false

        return true
    }

    override fun hashCode(): Int {
        var result = round.hashCode()
        result = 31 * result + isEnoughPlayers.hashCode()
        result = 31 * result + playerSeatPositions.contentHashCode()
        result = 31 * result + potAmount
        result = 31 * result + bigBlind
        result = 31 * result + currentHighBet
        result = 31 * result + dealerButtonPos
        result = 31 * result + players.hashCode()
        result = 31 * result + currentPlayerIndex
        result = 31 * result + communityCards.hashCode()
        result = 31 * result + isRaiseEnabled.hashCode()
        result = 31 * result + isCallEnabled.hashCode()
        result = 31 * result + isCheckEnabled.hashCode()
        result = 31 * result + isFoldEnabled.hashCode()
        return result
    }
}
