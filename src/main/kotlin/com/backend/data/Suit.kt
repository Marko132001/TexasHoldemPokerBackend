package com.backend.data

enum class Suit(val value: Int, val label: String) {
    HEARTS(0x2000,"hearts"),
    DIAMONDS(0x4000,"diamonds"),
    CLUBS(0x8000,"clubs"),
    SPADES(0x1000,"spades")
}