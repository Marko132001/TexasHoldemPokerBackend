package com.backend.model

import com.backend.data.PlayerState
import kotlinx.serialization.Serializable

@Serializable
data class PlayerDataState(
    val userId: String,
    val username: String,
    val avatarUrl: String?,
    val chipBuyInAmount: Int,
    val holeCards: Pair<String, String>,
    val playerHandRank: String,
    val playerState: PlayerState,
    val playerBet: Int
)
