package com.backend.model

import com.backend.data.PlayerState
import kotlinx.serialization.Serializable

@Serializable
data class PlayerAction(val playerState: String, val raiseAmount: Int)
