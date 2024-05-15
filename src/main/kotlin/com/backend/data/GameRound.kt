package com.backend.data

enum class GameRound {
    PREFLOP, FLOP, TURN, RIVER, SHOWDOWN;

    fun nextRound(): GameRound {
        return GameRound.entries[
            (this.ordinal + 1) % GameRound.entries.size
        ]
    }
}