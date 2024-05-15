package com.backend.model

import com.backend.data.HandRankings
import com.backend.data.PlayerState
import com.backend.data.PlayingCard
import kotlinx.serialization.Serializable

@Serializable
data class PlayerDataState(
    val username: String,
    val chipBuyInAmount: Int,
    val holeCards: Pair<PlayingCard, PlayingCard>,
    val playerHandRank: Pair<HandRankings, Int>,
    val playerState: PlayerState,
    var playerBet: Int
)
