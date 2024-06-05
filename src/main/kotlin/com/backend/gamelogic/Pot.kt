package com.backend.gamelogic

data class Pot (
    val players: MutableSet<Player> = mutableSetOf(),
    var amount: Int = 0
)