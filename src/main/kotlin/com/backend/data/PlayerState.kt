package com.backend.data

enum class PlayerState(val label: String) {
    CHECK("CHECK"),
    FOLD("FOLD"),
    ALL_IN("ALL IN"),
    RAISE("RAISE"),
    CALL("CALL"),
    INACTIVE("INACTIVE"),
    SPECTATOR("SPECTATOR"),
    WINNER("WINNER")
}