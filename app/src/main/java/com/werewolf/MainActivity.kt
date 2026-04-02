package com.werewolf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.werewolf.model.GamePhase
import com.werewolf.model.Role
import com.werewolf.ui.screens.*
import com.werewolf.ui.theme.DarkBackground
import com.werewolf.ui.theme.WerewolfTheme
import com.werewolf.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WerewolfTheme {
                WerewolfApp()
            }
        }
    }
}

@Composable
fun WerewolfApp(vm: GameViewModel = viewModel()) {
    val phase by vm.phase.collectAsState()

    Modifier
        .fillMaxSize()
        .background(DarkBackground)
        .let { modifier ->
            when (val p = phase) {
                is GamePhase.Title -> {
                    TitleScreen(onStart = { count -> vm.startSetup(count) })
                }

                is GamePhase.Setup -> {
                    SetupScreen(
                        playerCount = p.playerCount,
                        onSubmit = { names -> vm.submitPlayerNames(names) }
                    )
                }

                is GamePhase.Handoff -> {
                    HandoffScreen(
                        playerName = p.playerName,
                        onReady = { vm.proceedFromHandoff() }
                    )
                }

                is GamePhase.RoleReveal -> {
                    val player = vm.players[p.playerIndex]
                    val teammates = vm.players.filter {
                        it.role == Role.WEREWOLF && it !== player
                    }
                    RoleRevealScreen(
                        player = player,
                        revealed = p.revealed,
                        teammates = teammates,
                        onReveal = { vm.revealRole() },
                        onNext = { vm.nextRoleReveal() }
                    )
                }

                is GamePhase.NightBanner -> {
                    NightBannerScreen(
                        roundNumber = p.roundNumber,
                        onProceed = { vm.proceedFromNightBanner() }
                    )
                }

                is GamePhase.WerewolfTurn -> {
                    val wolves = vm.aliveWolves()
                    val currentWolf = wolves[p.wolfIndex]
                    val teammates = wolves.filter { it !== currentWolf }
                    WerewolfTurnScreen(
                        wolf = currentWolf,
                        teammates = teammates,
                        targets = vm.aliveNonWolves(),
                        previousVotes = vm.getWolfVotes(),
                        onSelectTarget = { target -> vm.submitWolfVote(target) }
                    )
                }

                is GamePhase.SeerTurn -> {
                    val seer = vm.alivePlayers().first { it.role == Role.SEER }
                    SeerTurnScreen(
                        seer = seer,
                        targets = vm.alivePlayers().filter { it !== seer },
                        result = p.result,
                        onInvestigate = { target -> vm.submitSeerInvestigation(target) },
                        onDone = { vm.proceedFromSeerResult() }
                    )
                }

                is GamePhase.DoctorTurn -> {
                    val doctor = vm.alivePlayers().first { it.role == Role.DOCTOR }
                    DoctorTurnScreen(
                        doctor = doctor,
                        targets = vm.alivePlayers(),
                        lastProtected = vm.getLastProtected(),
                        onProtect = { target -> vm.submitDoctorProtection(target) }
                    )
                }

                is GamePhase.DawnAnnouncement -> {
                    DawnAnnouncementScreen(
                        killedPlayer = p.killedPlayer,
                        saved = p.saved,
                        onProceed = { vm.proceedFromDawn() }
                    )
                }

                is GamePhase.HunterRevenge -> {
                    HunterRevengeScreen(
                        hunter = p.hunter,
                        targets = vm.alivePlayers(),
                        onShoot = { target ->
                            // Determine context: after night or after vote
                            vm.submitHunterRevenge(target)
                        }
                    )
                }

                is GamePhase.DayDiscussion -> {
                    DayDiscussionScreen(
                        roundNumber = p.roundNumber,
                        alivePlayers = vm.alivePlayers(),
                        onStartVoting = { vm.startVoting() }
                    )
                }

                is GamePhase.VotingTurn -> {
                    val alive = vm.alivePlayers()
                    val voter = alive[p.voterIndex]
                    VotingTurnScreen(
                        voter = voter,
                        targets = alive.filter { it !== voter },
                        onVote = { targetName -> vm.submitVote(targetName) }
                    )
                }

                is GamePhase.VoteResults -> {
                    VoteResultsScreen(
                        votes = p.votes,
                        eliminated = p.eliminated,
                        isTie = p.isTie,
                        onProceed = { vm.proceedFromVoteResults() }
                    )
                }

                is GamePhase.GameOver -> {
                    GameOverScreen(
                        winner = p.winner,
                        players = vm.players,
                        onPlayAgain = { vm.playAgain() }
                    )
                }
            }
        }
}
