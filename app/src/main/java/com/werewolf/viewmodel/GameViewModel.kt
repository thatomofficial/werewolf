package com.werewolf.viewmodel

import androidx.lifecycle.ViewModel
import com.werewolf.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel : ViewModel() {

    private val _phase = MutableStateFlow<GamePhase>(GamePhase.Title)
    val phase: StateFlow<GamePhase> = _phase.asStateFlow()

    var players: List<Player> = emptyList()
        private set

    var roundNumber: Int = 0
        private set

    // Night action tracking
    private var wolfVotes = mutableMapOf<String, Player>() // wolfName -> target
    private var protectedPlayer: Player? = null
    private var lastProtected: Player? = null
    private var killedThisNight: Player? = null

    // Day vote tracking
    private var dayVotes = mutableMapOf<String, String?>() // voterName -> targetName?

    // Track whether hunter revenge is after night or after day vote
    private var hunterRevengeAfterVote: Boolean = false

    // ── Setup ──────────────────────────────────────────────────────────

    fun startSetup(playerCount: Int) {
        _phase.value = GamePhase.Setup(playerCount)
    }

    fun submitPlayerNames(names: List<String>) {
        val roles = getRoleDistribution(names.size)
        players = names.zip(roles).map { (name, role) -> Player(name, role) }
        roundNumber = 0
        _phase.value = GamePhase.Handoff(
            playerName = players[0].name,
            nextPhase = GamePhase.RoleReveal(playerIndex = 0)
        )
    }

    fun proceedFromHandoff() {
        val handoff = _phase.value as? GamePhase.Handoff ?: return
        _phase.value = handoff.nextPhase
    }

    // ── Role Reveal ────────────────────────────────────────────────────

    fun revealRole() {
        val reveal = _phase.value as? GamePhase.RoleReveal ?: return
        _phase.value = reveal.copy(revealed = true)
    }

    fun nextRoleReveal() {
        val reveal = _phase.value as? GamePhase.RoleReveal ?: return
        val nextIndex = reveal.playerIndex + 1
        if (nextIndex < players.size) {
            _phase.value = GamePhase.Handoff(
                playerName = players[nextIndex].name,
                nextPhase = GamePhase.RoleReveal(playerIndex = nextIndex)
            )
        } else {
            startNight()
        }
    }

    // ── Night Phase ────────────────────────────────────────────────────

    private fun startNight() {
        roundNumber++
        wolfVotes.clear()
        protectedPlayer = null
        killedThisNight = null
        _phase.value = GamePhase.NightBanner(roundNumber)
    }

    fun proceedFromNightBanner() {
        val wolves = alivePlayers().filter { it.role == Role.WEREWOLF }
        if (wolves.isNotEmpty()) {
            _phase.value = GamePhase.Handoff(
                playerName = wolves[0].name,
                nextPhase = GamePhase.WerewolfTurn(wolfIndex = 0)
            )
        } else {
            proceedToSeer()
        }
    }

    fun submitWolfVote(target: Player) {
        val turn = _phase.value as? GamePhase.WerewolfTurn ?: return
        val wolves = alivePlayers().filter { it.role == Role.WEREWOLF }
        val currentWolf = wolves[turn.wolfIndex]
        wolfVotes[currentWolf.name] = target

        val nextIndex = turn.wolfIndex + 1
        if (nextIndex < wolves.size) {
            _phase.value = GamePhase.Handoff(
                playerName = wolves[nextIndex].name,
                nextPhase = GamePhase.WerewolfTurn(wolfIndex = nextIndex)
            )
        } else {
            proceedToSeer()
        }
    }

    private fun proceedToSeer() {
        val seer = alivePlayers().find { it.role == Role.SEER }
        if (seer != null) {
            _phase.value = GamePhase.Handoff(
                playerName = seer.name,
                nextPhase = GamePhase.SeerTurn()
            )
        } else {
            proceedToDoctor()
        }
    }

    fun submitSeerInvestigation(target: Player) {
        _phase.value = GamePhase.SeerTurn(
            result = SeerResult(
                targetName = target.name,
                isWerewolf = target.role == Role.WEREWOLF
            )
        )
    }

    fun proceedFromSeerResult() {
        proceedToDoctor()
    }

    private fun proceedToDoctor() {
        val doctor = alivePlayers().find { it.role == Role.DOCTOR }
        if (doctor != null) {
            _phase.value = GamePhase.Handoff(
                playerName = doctor.name,
                nextPhase = GamePhase.DoctorTurn
            )
        } else {
            resolveNight()
        }
    }

    fun submitDoctorProtection(target: Player) {
        protectedPlayer = target
        lastProtected = target
        resolveNight()
    }

    private fun resolveNight() {
        // Tally wolf votes
        val voteCounts = wolfVotes.values.groupingBy { it }.eachCount()
        val maxVotes = voteCounts.values.maxOrNull() ?: 0
        val topTargets = voteCounts.filter { it.value == maxVotes }.keys.toList()
        val killTarget = topTargets.randomOrNull()

        val saved = killTarget != null && killTarget == protectedPlayer
        if (killTarget != null && !saved) {
            killTarget.alive = false
            killedThisNight = killTarget
        }

        _phase.value = GamePhase.DawnAnnouncement(
            killedPlayer = if (saved) null else killedThisNight,
            saved = saved
        )
    }

    // ── Dawn / Hunter ──────────────────────────────────────────────────

    fun proceedFromDawn() {
        val dawn = _phase.value as? GamePhase.DawnAnnouncement ?: return
        val killed = dawn.killedPlayer

        if (killed != null && killed.role == Role.HUNTER) {
            hunterRevengeAfterVote = false
            _phase.value = GamePhase.HunterRevenge(hunter = killed)
            return
        }

        checkWinOrStartDay()
    }

    fun submitHunterRevenge(target: Player) {
        target.alive = false
        if (hunterRevengeAfterVote) {
            val winner = checkWinCondition()
            if (winner != null) {
                _phase.value = GamePhase.GameOver(winner)
            } else {
                startNight()
            }
        } else {
            checkWinOrStartDay()
        }
    }

    private fun checkWinOrStartDay() {
        val winner = checkWinCondition()
        if (winner != null) {
            _phase.value = GamePhase.GameOver(winner)
        } else {
            _phase.value = GamePhase.DayDiscussion(roundNumber)
        }
    }

    // ── Day Phase ──────────────────────────────────────────────────────

    fun startVoting() {
        dayVotes.clear()
        val alive = alivePlayers()
        if (alive.isNotEmpty()) {
            _phase.value = GamePhase.Handoff(
                playerName = alive[0].name,
                nextPhase = GamePhase.VotingTurn(voterIndex = 0)
            )
        }
    }

    fun submitVote(targetName: String?) {
        val turn = _phase.value as? GamePhase.VotingTurn ?: return
        val alive = alivePlayers()
        val voter = alive[turn.voterIndex]
        dayVotes[voter.name] = targetName

        val nextIndex = turn.voterIndex + 1
        if (nextIndex < alive.size) {
            _phase.value = GamePhase.Handoff(
                playerName = alive[nextIndex].name,
                nextPhase = GamePhase.VotingTurn(voterIndex = nextIndex)
            )
        } else {
            resolveVotes()
        }
    }

    private fun resolveVotes() {
        val voteCounts = dayVotes.values.filterNotNull().groupingBy { it }.eachCount()
        val maxVotes = voteCounts.values.maxOrNull() ?: 0
        val topTargets = voteCounts.filter { it.value == maxVotes }.keys.toList()

        val isTie = topTargets.size > 1
        val eliminatedName = if (!isTie && topTargets.isNotEmpty()) topTargets[0] else null
        val eliminated = eliminatedName?.let { name -> players.find { it.name == name } }

        if (eliminated != null) {
            eliminated.alive = false
        }

        _phase.value = GamePhase.VoteResults(
            votes = dayVotes.toMap(),
            eliminated = eliminated,
            isTie = isTie
        )
    }

    fun proceedFromVoteResults() {
        val results = _phase.value as? GamePhase.VoteResults ?: return
        val eliminated = results.eliminated

        if (eliminated != null && eliminated.role == Role.HUNTER) {
            hunterRevengeAfterVote = true
            _phase.value = GamePhase.HunterRevenge(hunter = eliminated)
            return
        }

        val winner = checkWinCondition()
        if (winner != null) {
            _phase.value = GamePhase.GameOver(winner)
        } else {
            startNight()
        }
    }

    // ── Win Condition ──────────────────────────────────────────────────

    private fun checkWinCondition(): Team? {
        val aliveWolves = alivePlayers().count { it.role == Role.WEREWOLF }
        val aliveVillage = alivePlayers().count { it.team == Team.VILLAGE }
        return when {
            aliveWolves == 0 -> Team.VILLAGE
            aliveWolves >= aliveVillage -> Team.WEREWOLF
            else -> null
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────

    fun alivePlayers(): List<Player> = players.filter { it.alive }

    fun aliveNonWolves(): List<Player> = alivePlayers().filter { it.role != Role.WEREWOLF }

    fun aliveWolves(): List<Player> = alivePlayers().filter { it.role == Role.WEREWOLF }

    fun getLastProtected(): Player? = lastProtected

    fun getWolfVotes(): Map<String, Player> = wolfVotes.toMap()

    fun playAgain() {
        players = emptyList()
        roundNumber = 0
        wolfVotes.clear()
        dayVotes.clear()
        protectedPlayer = null
        lastProtected = null
        killedThisNight = null
        _phase.value = GamePhase.Title
    }
}
