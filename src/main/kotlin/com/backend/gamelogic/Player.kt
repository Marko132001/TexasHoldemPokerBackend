package com.backend.gamelogic

import com.backend.data.HandRankings
import com.backend.data.PlayerState
import com.backend.data.PlayingCard
import kotlinx.serialization.Serializable


class Player(
    val userId: String,
    val username: String,
    var chipBuyInAmount: Int,
    val avatarUrl: String?
) {

    private var holeCards: Pair<PlayingCard, PlayingCard> = Pair(PlayingCard.TWO_OF_CLUBS, PlayingCard.ACE_OF_DIAMONDS)
    var playerHandRank: Pair<HandRankings, Int> = Pair(HandRankings.HIGH_CARD, 7462)
    var playerState: PlayerState = PlayerState.INACTIVE
    var playerBet: Int = 0

    fun assignHoleCards(holeCardsAssigned: Pair<PlayingCard, PlayingCard>){
        holeCards = holeCardsAssigned
    }

    fun getHoleCards(): Pair<PlayingCard, PlayingCard> {

        return holeCards
    }

    fun getHoleCardsLabels(): Pair<String, String> {
        return Pair(holeCards.first.cardLabel, holeCards.second.cardLabel)
    }

    fun assignChips(chipAmount: Int){
        chipBuyInAmount += chipAmount
    }

    fun call(currentHighBet: Int): Int {
        val betDifference = currentHighBet - playerBet
        if(betDifference >= chipBuyInAmount){
            playerState = PlayerState.ALL_IN
            val allInCall = chipBuyInAmount
            chipBuyInAmount = 0
            playerBet += allInCall

            LOGGER.trace("${username} went ALL IN for ${allInCall} chips")

            return allInCall
        }

        playerState = PlayerState.CALL
        playerBet += betDifference
        chipBuyInAmount -= betDifference

        LOGGER.trace("${username} CALLED for ${betDifference} chips")

        return betDifference
    }

    fun raise(currentHighBet: Int, raiseAmount: Int): Int {
        val betDifference = (currentHighBet - playerBet)
        if(raiseAmount == chipBuyInAmount - betDifference) {
            playerState = PlayerState.ALL_IN

            LOGGER.trace("${username} went ALL IN for ${betDifference + raiseAmount} chips")
        }
        else {
            playerState = PlayerState.RAISE

            LOGGER.trace("${username} made a BET for ${betDifference + raiseAmount} chips")
        }

        playerBet += (betDifference + raiseAmount)
        chipBuyInAmount -= (betDifference + raiseAmount)

        return betDifference + raiseAmount
    }

    fun check() {
        playerState = PlayerState.CHECK

        LOGGER.trace("${username} CHECKED")
    }

    fun fold() {
        playerState = PlayerState.FOLD

        LOGGER.trace("${username} FOLDED")
    }

    fun paySmallBlind(smallBlindValue: Int): Int {
        chipBuyInAmount -= smallBlindValue
        playerBet = smallBlindValue

        LOGGER.trace("${username} is SMALL BLIND")

        return smallBlindValue
    }

    fun payBigBlind(bigBlindValue: Int): Int {
        chipBuyInAmount -= bigBlindValue
        playerBet = bigBlindValue

        LOGGER.trace("${username} is BIG BLIND")

        return bigBlindValue
    }

    override fun toString(): String {
        return "Player(chipAmount=$chipBuyInAmount, playerBet=$playerBet), playerState=$playerState"
    }
}