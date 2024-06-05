package com.backend.gamelogic

import com.backend.data.GameRound
import com.backend.data.HandRankings
import com.backend.data.PlayerState
import com.backend.data.PlayingCard
import com.backend.model.GameModel
import io.ktor.util.logging.KtorSimpleLogger
import kotlin.math.min

internal val LOGGER = KtorSimpleLogger("com.example.RequestTracePlugin")

class Game() {

    var players: MutableList<Player> = mutableListOf()
    var potAmount: Int = 0
    val pots: MutableList<Pot> = mutableListOf()
    var currentHighBet: Int = 0
    val smallBlind: Int = 25
    val bigBlind: Int = 50
    private var communityCards: MutableList<PlayingCard> = mutableListOf()
    var dealerButtonPos: Int = -1
    var currentPlayerIndex: Int = -1
    var raiseFlag: Boolean = false

    companion object CardConstants {
        const val COMMUNITY_CARDS = 5
        const val HOLE_CARDS = 2
        const val FLOP_CARDS_START = 0
        const val FLOP_CARDS_END = 3
        const val TURN_CARD_INDEX = 4
        const val RIVER_CARD_INDEX = 5
        const val HAND_COMBINATION = 3
    }

    fun preflopRoundInit(): Boolean {

        LOGGER.trace("Initializing preflop round...")

        var countInactivePlayers = 0
        players.forEach {
            player ->
                if(player.chipBuyInAmount == 0){
                    player.playerState = PlayerState.SPECTATOR
                }
                else {
                    player.playerState = PlayerState.INACTIVE
                    countInactivePlayers++
                }
                player.playerHandRank = Pair(HandRankings.HIGH_CARD, 7462)
                player.playerBet = 0
        }

        if(countInactivePlayers < 2){
            return false
        }

        raiseFlag = false
        currentPlayerIndex = -1
        dealerButtonPos = getPlayerRolePosition(dealerButtonPos)

        val cards: List<PlayingCard> = shuffleCardsDeck()
        generateHoleCards(cards)
        generateCommunityCards(cards)

        pots.clear()
        pots.add(Pot())

        val smallBlindIndex = getPlayerRolePosition(dealerButtonPos)
        val bigBlindIndex = getPlayerRolePosition(smallBlindIndex)
        currentPlayerIndex = getPlayerRolePosition(bigBlindIndex)

        potAmount = 0
        currentHighBet = 0

        LOGGER.trace("Small blind index: $smallBlindIndex")
        LOGGER.trace("Big blind index: $bigBlindIndex")
        LOGGER.trace("Current player index: $currentPlayerIndex")

        updatePot(
            players[smallBlindIndex]
                .paySmallBlind(smallBlind)
        )

        updatePot(
            players[bigBlindIndex]
                .payBigBlind(bigBlind)
        )
        currentHighBet = bigBlind

        return true
    }

    fun nextRoundInit(round: GameRound): GameRound {

        if(showdownEdgeCases()){
            addChipsToPot()
            players.firstOrNull { it.playerBet > 0 }?.let { player ->
                println("Returned ${player.playerBet} chips to ${player.username}.")
                player.assignChips(player.playerBet)
            }
            LOGGER.trace("Moving to showdown round. No more player actions available.")
            return GameRound.SHOWDOWN
        }

        var countInactivePlayers = 0
        players.forEach {
                player ->
                    if(player.playerState == PlayerState.INACTIVE){
                        countInactivePlayers++
                    }
        }

        currentPlayerIndex = getPlayerRolePosition(currentPlayerIndex)
        LOGGER.trace("Current player index: $currentPlayerIndex")

        if((countInactivePlayers > 0 && !raiseFlag) || checkPlayerHighBet(currentPlayerIndex)){
            LOGGER.trace("Current round is not finished yet.")
            return round
        }

        LOGGER.trace("Initializing next round...")

        var countPlayersWithActions = 0
        players.forEach {
                player ->
                    if(player.playerState != PlayerState.FOLD
                        && player.playerState != PlayerState.ALL_IN
                        && player.playerState != PlayerState.SPECTATOR
                    ){
                        player.playerState = PlayerState.INACTIVE
                        countPlayersWithActions++
                    }
        }

        addChipsToPot()

        if(countPlayersWithActions <= 1){
            players.firstOrNull { it.playerBet > 0 }?.let { player ->
                println("Returned ${player.playerBet} chips to ${player.username}.")
                player.assignChips(player.playerBet)
            }
            LOGGER.trace("Moving to showdown round. No more player actions available.")
            return GameRound.SHOWDOWN
        }

        raiseFlag = false
        currentPlayerIndex = -1

        LOGGER.trace("Updating current player index...")

        currentPlayerIndex = getPlayerRolePosition(dealerButtonPos)

        LOGGER.trace("Current player index: $currentPlayerIndex")

        currentHighBet = 0

        LOGGER.trace("Next round: ${round.nextRound().name}")

        return round.nextRound()
    }

    private fun showdownEdgeCases(): Boolean {
        LOGGER.trace("Checking for showdown round edge cases...")
        val countFolds = players.count {
            it.playerState == PlayerState.FOLD
                    || it.playerState == PlayerState.SPECTATOR
        }

        if(countFolds == players.size - 1){
            return true
        }

        return players.count {
            it.playerState == PlayerState.FOLD
                    || it.playerState == PlayerState.ALL_IN
                    || it.playerState == PlayerState.SPECTATOR
        } == players.size

    }

    private fun checkPlayerHighBet(playerIndex: Int): Boolean {
        LOGGER.trace("Checking player high bet.")
        LOGGER.trace("Player bet: ${players[playerIndex].playerBet}; High bet: $currentHighBet")
        return players[playerIndex].playerBet != currentHighBet && raiseFlag
    }

    private fun getPlayerRolePosition(playerRoleStart: Int): Int {
        var playerRoleIndex = playerRoleStart
        do {
            playerRoleIndex = (playerRoleIndex + 1) % players.size
        }while(
            players[playerRoleIndex].playerState == PlayerState.FOLD
            || players[playerRoleIndex].playerState == PlayerState.ALL_IN
            || players[playerRoleIndex].playerState == PlayerState.SPECTATOR
        )
        return playerRoleIndex
    }

    private fun shuffleCardsDeck(): List<PlayingCard> {
        return PlayingCard.entries.shuffled()
    }

    private fun generateCommunityCards(cards: List<PlayingCard>) {
        communityCards = cards.drop(CardConstants.HOLE_CARDS * players.size)
            .take(CardConstants.COMMUNITY_CARDS).toMutableList()
    }

    private fun generateHoleCards(cards: List<PlayingCard>) {
        val generatedHoleCards = cards.take(CardConstants.HOLE_CARDS * players.size)

        for((index, player) in players.withIndex()){
            player.assignHoleCards(
                Pair(generatedHoleCards[index*2], generatedHoleCards[index*2 + 1])
            )
        }
    }

    fun showStreet(gameRound: GameRound): MutableList<PlayingCard> {
        return when(gameRound){
            GameRound.PREFLOP -> mutableListOf()
            GameRound.FLOP -> communityCards
                .subList(CardConstants.FLOP_CARDS_START, CardConstants.FLOP_CARDS_END)
            GameRound.TURN -> communityCards
                .subList(CardConstants.FLOP_CARDS_START, CardConstants.TURN_CARD_INDEX)
            GameRound.RIVER -> communityCards
                .subList(CardConstants.FLOP_CARDS_START, CardConstants.RIVER_CARD_INDEX)
            else -> communityCards
        }
    }

    fun updatePot(playerBet: Int) {
        potAmount += playerBet
    }

    private fun addChipsToPot() {
        val currentPotIndex = pots.size - 1

        val lowestPlayerBet = players.filter {
            it.playerBet > 0
        }.let { playerList ->
            playerList.minOfOrNull {
                player -> player.playerBet
            }
        } ?: return

        players.forEach { player ->
            if(player.playerBet > 0){
                pots[currentPotIndex].amount += lowestPlayerBet
                pots[currentPotIndex].players.add(player)
                player.playerBet -= lowestPlayerBet
            }
        }

        val playersWithRemainingBet = players.count { it.playerBet > 0 }

        if(playersWithRemainingBet > 1){
            pots.add(Pot())
            addChipsToPot()
        }
        else{
            return
        }
    }

    private fun combinationUtil(
        comCards: MutableList<PlayingCard>, tmpCardComb: Array<PlayingCard?>, start: Int,
        end: Int, index: Int, r: Int, handEvaluator: CardHandEvaluator,
        player: Player, cardCombinations: MutableList<PlayingCard>
    ) {

        if (index == r) {
            for (j in 0 until r) {
                tmpCardComb[j]?.let { cardCombinations.add(it) }
            }
            cardCombinations.add(player.getHoleCards().first)
            cardCombinations.add(player.getHoleCards().second)

            val combinationHandRank = handEvaluator.getHandRanking(cardCombinations)
            if(combinationHandRank.second < player.playerHandRank.second){
                player.playerHandRank = combinationHandRank
            }
            cardCombinations.clear()
            return
        }

        var i = start
        while (i <= end && end - i + 1 >= r - index) {
            tmpCardComb[index] = comCards[i]
            combinationUtil(
                comCards, tmpCardComb, i + 1, end,
                index + 1, r, handEvaluator, player, cardCombinations
            )
            i++
        }
    }

    private fun rankCardHands(potPlayers: MutableSet<Player>): MutableSet<Player> {
        val tmpCardComb = arrayOfNulls<PlayingCard>(CardConstants.HAND_COMBINATION)
        val cardCombinations = mutableListOf<PlayingCard>()
        val handEvaluator = CardHandEvaluator()
        val winners: MutableSet<Player> = mutableSetOf()
        var winningScore: Int = 7462
        potPlayers.forEach {
            player ->
                if(player.playerState != PlayerState.FOLD
                    && player.playerState != PlayerState.SPECTATOR) {
                    combinationUtil(
                        communityCards, tmpCardComb, 0,
                        CardConstants.COMMUNITY_CARDS - 1, 0,
                        CardConstants.HAND_COMBINATION, handEvaluator,
                        player, cardCombinations
                    )

                    if(player.playerHandRank.second < winningScore){
                        winners.clear()
                        winners.add(player)
                        winningScore = player.playerHandRank.second
                    }
                    else if(player.playerHandRank.second == winningScore) {
                        winners.add(player)
                    }
                }
        }

        return winners
    }

    fun assignChipsToWinner() {
        pots.forEach { pot ->
            var winners = mutableSetOf<Player>()

            if(pot.players.size == 1){
                winners.add(pot.players.first())
            }
            else{
                winners = rankCardHands(pot.players)
            }

            val splitPot = pot.amount / winners.size

            winners.forEach { winner ->
                println("Player ${winner.username} won $splitPot chips. Total pot: $potAmount")
                winner.playerState = PlayerState.WINNER
                winner.assignChips(splitPot)
            }
        }
    }

    override fun toString(): String {
        return "Game(potAmount=$potAmount, currentHighBet=$currentHighBet)"
    }
}
