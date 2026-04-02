package com.werewolf.model

/**
 * Represents every distinct screen/phase the game can be in.
 */
sealed class GamePhase {
    /** Title / start menu */
    object Title : GamePhase()

    /** Entering player names */
    data class Setup(val playerCount: Int) : GamePhase()

    /** Private role reveal — one player at a time */
    data class RoleReveal(
        val playerIndex: Int,
        val revealed: Boolean = false
    ) : GamePhase()

    /** "Pass device to X" handoff screen */
    data class Handoff(
        val playerName: String,
        val nextPhase: GamePhase
    ) : GamePhase()

    /** Night banner */
    data class NightBanner(val roundNumber: Int) : GamePhase()

    /** Werewolf choosing a target */
    data class WerewolfTurn(val wolfIndex: Int) : GamePhase()

    /** Seer investigating */
    data class SeerTurn(val result: SeerResult? = null) : GamePhase()

    /** Doctor protecting */
    object DoctorTurn : GamePhase()

    /** Dawn results announcement */
    data class DawnAnnouncement(
        val killedPlayer: Player?,
        val saved: Boolean
    ) : GamePhase()

    /** Hunter's revenge shot */
    data class HunterRevenge(val hunter: Player) : GamePhase()

    /** Day discussion */
    data class DayDiscussion(val roundNumber: Int) : GamePhase()

    /** Individual player voting (pass-and-play) */
    data class VotingTurn(val voterIndex: Int) : GamePhase()

    /** Vote results */
    data class VoteResults(
        val votes: Map<String, String?>,
        val eliminated: Player?,
        val isTie: Boolean
    ) : GamePhase()

    /** Game over */
    data class GameOver(val winner: Team) : GamePhase()
}

data class SeerResult(
    val targetName: String,
    val isWerewolf: Boolean
)
