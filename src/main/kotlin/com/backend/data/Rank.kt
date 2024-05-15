package com.backend.data

enum class Rank(val value: Int, val label: String) {
    TWO(0, "2"),
    THREE(1, "3"),
    FOUR(2, "4"),
    FIVE(3, "5"),
    SIX(4, "6"),
    SEVEN(5, "7"),
    EIGHT(6, "8"),
    NINE(7, "9"),
    TEN(8, "10"),
    JACK(9, "jack"),
    QUEEN(10, "queen"),
    KING(11, "king"),
    ACE(12, "ace")
}