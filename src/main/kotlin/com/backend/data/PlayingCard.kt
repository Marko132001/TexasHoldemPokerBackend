package com.backend.data

enum class PlayingCard(val rank: Rank, val suit: Suit) {
    ACE_OF_CLUBS(Rank.ACE, Suit.CLUBS),
    ACE_OF_HEARTS(Rank.ACE, Suit.HEARTS),
    ACE_OF_DIAMONDS(Rank.ACE, Suit.DIAMONDS),
    ACE_OF_SPADES(Rank.ACE, Suit.SPADES),

    TWO_OF_CLUBS(Rank.TWO, Suit.CLUBS),
    TWO_OF_HEARTS(Rank.TWO, Suit.HEARTS),
    TWO_OF_DIAMONDS(Rank.TWO, Suit.DIAMONDS),
    TWO_OF_SPADES(Rank.TWO, Suit.SPADES),

    THREE_OF_CLUBS(Rank.THREE, Suit.CLUBS),
    THREE_OF_HEARTS(Rank.THREE, Suit.HEARTS),
    THREE_OF_DIAMONDS(Rank.THREE, Suit.DIAMONDS),
    THREE_OF_SPADES(Rank.THREE, Suit.SPADES),

    FOUR_OF_CLUBS(Rank.FOUR, Suit.CLUBS),
    FOUR_OF_HEARTS(Rank.FOUR, Suit.HEARTS),
    FOUR_OF_DIAMONDS(Rank.FOUR, Suit.DIAMONDS),
    FOUR_OF_SPADES(Rank.FOUR, Suit.SPADES),

    FIVE_OF_CLUBS(Rank.FIVE, Suit.CLUBS),
    FIVE_OF_HEARTS(Rank.FIVE, Suit.HEARTS),
    FIVE_OF_DIAMONDS(Rank.FIVE, Suit.DIAMONDS),
    FIVE_OF_SPADES(Rank.FIVE, Suit.SPADES),

    SIX_OF_CLUBS(Rank.SIX, Suit.CLUBS),
    SIX_OF_HEARTS(Rank.SIX, Suit.HEARTS),
    SIX_OF_DIAMONDS(Rank.SIX, Suit.DIAMONDS),
    SIX_OF_SPADES(Rank.SIX, Suit.SPADES),

    SEVEN_OF_CLUBS(Rank.SEVEN, Suit.CLUBS),
    SEVEN_OF_HEARTS(Rank.SEVEN, Suit.HEARTS),
    SEVEN_OF_DIAMONDS(Rank.SEVEN, Suit.DIAMONDS),
    SEVEN_OF_SPADES(Rank.SEVEN, Suit.SPADES),

    EIGHT_OF_CLUBS(Rank.EIGHT, Suit.CLUBS),
    EIGHT_OF_HEARTS(Rank.EIGHT, Suit.HEARTS),
    EIGHT_OF_DIAMONDS(Rank.EIGHT, Suit.DIAMONDS),
    EIGHT_OF_SPADES(Rank.EIGHT, Suit.SPADES),

    NINE_OF_CLUBS(Rank.NINE, Suit.CLUBS),
    NINE_OF_HEARTS(Rank.NINE, Suit.HEARTS),
    NINE_OF_DIAMONDS(Rank.NINE, Suit.DIAMONDS),
    NINE_OF_SPADES(Rank.NINE, Suit.SPADES),

    TEN_OF_CLUBS(Rank.TEN, Suit.CLUBS),
    TEN_OF_HEARTS(Rank.TEN, Suit.HEARTS),
    TEN_OF_DIAMONDS(Rank.TEN, Suit.DIAMONDS),
    TEN_OF_SPADES(Rank.TEN, Suit.SPADES),

    JACK_OF_CLUBS(Rank.JACK, Suit.CLUBS),
    JACK_OF_HEARTS(Rank.JACK, Suit.HEARTS),
    JACK_OF_DIAMONDS(Rank.JACK, Suit.DIAMONDS),
    JACK_OF_SPADES(Rank.JACK, Suit.SPADES),

    QUEEN_OF_CLUBS(Rank.QUEEN, Suit.CLUBS),
    QUEEN_OF_HEARTS(Rank.QUEEN, Suit.HEARTS),
    QUEEN_OF_DIAMONDS(Rank.QUEEN, Suit.DIAMONDS),
    QUEEN_OF_SPADES(Rank.QUEEN, Suit.SPADES),

    KING_OF_CLUBS(Rank.KING, Suit.CLUBS),
    KING_OF_HEARTS(Rank.KING, Suit.HEARTS),
    KING_OF_DIAMONDS(Rank.KING, Suit.DIAMONDS),
    KING_OF_SPADES(Rank.KING, Suit.SPADES);

    val value: Int = (1 shl (rank.value + 16)) or suit.value or (rank.value shl 8) or
            Tables.PRIMES[rank.value].toInt()

    val cardLabel: String = suit.label + "_" + rank.label

}