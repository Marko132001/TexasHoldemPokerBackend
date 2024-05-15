package com.backend.data

enum class HandRankings(val label: String) {
    ROYAL_FLUSH("ROYAL FLUSH"),
    STRAIGHT_FLUSH("STRAIGHT FLUSH"),
    FOUR_OF_A_KIND("FOUR OF A KIND"),
    FULL_HOUSE("FULL HOUSE"),
    FLUSH("FLUSH"),
    STRAIGHT("STRAIGHT"),
    THREE_OF_A_KIND("THREE OF A KIND"),
    TWO_PAIR("TWO PAIR"),
    ONE_PAIR("ONE PAIR"),
    HIGH_CARD("HIGH CARD")
}