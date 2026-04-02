package com.werewolf.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.werewolf.model.Player
import com.werewolf.model.Role
import com.werewolf.ui.components.*
import com.werewolf.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun DawnAnnouncementScreen(
    killedPlayer: Player?,
    saved: Boolean,
    onProceed: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "\u2600\uFE0F", fontSize = 64.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Dawn Breaks",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = DawnGold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (killedPlayer != null) {
            InfoCard {
                Text(
                    text = "\uD83D\uDC80",
                    fontSize = 48.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = killedPlayer.name,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = WerewolfRed,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "was killed in the night!",
                    fontSize = 16.sp,
                    color = TextDim,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                val roleColor = when (killedPlayer.role) {
                    Role.WEREWOLF -> WerewolfRed
                    Role.SEER -> SeerCyan
                    Role.DOCTOR -> DoctorBlue
                    Role.HUNTER -> HunterAmber
                    Role.VILLAGER -> VillageGreen
                }
                Text(
                    text = "They were a ${killedPlayer.role.emoji} ${killedPlayer.role.name}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = roleColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else if (saved) {
            InfoCard {
                Text(
                    text = "\uD83D\uDC89",
                    fontSize = 48.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Everyone survived!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = VillageGreen,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "The Doctor saved someone tonight.",
                    fontSize = 14.sp,
                    color = TextDim,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            InfoCard {
                Text(
                    text = "Everyone survived the night.",
                    fontSize = 18.sp,
                    color = VillageGreen,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        GameButton(text = "CONTINUE", onClick = onProceed, color = DawnGold)
    }
}

@Composable
fun HunterRevengeScreen(
    hunter: Player,
    targets: List<Player>,
    onShoot: (Player) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PhaseHeader(
            emoji = "\uD83C\uDFF9",
            title = "Hunter's Revenge!",
            subtitle = "${hunter.name} takes a final shot...",
            color = HunterAmber
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Choose someone to take down with you:",
            color = TextDim,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(targets) { target ->
                PlayerSelectButton(
                    name = target.name,
                    onClick = { onShoot(target) },
                    borderColor = HunterAmber
                )
            }
        }
    }
}

@Composable
fun DayDiscussionScreen(
    roundNumber: Int,
    alivePlayers: List<Player>,
    onStartVoting: () -> Unit
) {
    var timeLeft by remember { mutableStateOf(60) }
    var timerRunning by remember { mutableStateOf(true) }

    LaunchedEffect(timerRunning) {
        while (timerRunning && timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PhaseHeader(
            emoji = "\u2600\uFE0F",
            title = "Day $roundNumber",
            subtitle = "Discuss who might be a werewolf!",
            color = DawnGold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Timer
        val mins = timeLeft / 60
        val secs = timeLeft % 60
        Text(
            text = "\u23F1 %d:%02d".format(mins, secs),
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = if (timeLeft <= 10) WerewolfRed else DawnGold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        InfoCard {
            Text(
                text = "Surviving Players (${alivePlayers.size})",
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            alivePlayers.forEach { p ->
                Text(
                    text = "\u2022 ${p.name}",
                    color = TextWhite,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        GameButton(
            text = if (timeLeft <= 0) "TIME'S UP — VOTE NOW" else "SKIP TO VOTING",
            onClick = {
                timerRunning = false
                onStartVoting()
            },
            color = WerewolfRed
        )
    }
}

@Composable
fun VotingTurnScreen(
    voter: Player,
    targets: List<Player>,
    onVote: (String?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PhaseHeader(
            emoji = "\uD83D\uDDF3\uFE0F",
            title = "Vote",
            subtitle = "${voter.name}, who do you want to eliminate?",
            color = WerewolfRed
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(targets) { target ->
                PlayerSelectButton(
                    name = target.name,
                    onClick = { onVote(target.name) },
                    borderColor = WerewolfRed
                )
            }
            item {
                PlayerSelectButton(
                    name = "Abstain",
                    subtitle = "Don't vote this round",
                    onClick = { onVote(null) },
                    borderColor = DeathGray
                )
            }
        }
    }
}

@Composable
fun VoteResultsScreen(
    votes: Map<String, String?>,
    eliminated: Player?,
    isTie: Boolean,
    onProceed: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PhaseHeader(
            emoji = "\uD83D\uDDF3\uFE0F",
            title = "Vote Results",
            color = TextWhite
        )

        Spacer(modifier = Modifier.height(16.dp))

        InfoCard {
            votes.forEach { (voter, target) ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = voter, color = TextWhite, fontSize = 14.sp)
                    Text(text = " \u2192 ", color = TextDim, fontSize = 14.sp)
                    Text(
                        text = target ?: "Abstain",
                        color = if (target != null) WerewolfRed else TextDim,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (eliminated != null) {
            Text(text = "\uD83D\uDC80", fontSize = 48.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = eliminated.name,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = WerewolfRed,
                textAlign = TextAlign.Center
            )
            Text(
                text = "has been eliminated!",
                color = TextDim,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            val roleColor = when (eliminated.role) {
                Role.WEREWOLF -> WerewolfRed
                Role.SEER -> SeerCyan
                Role.DOCTOR -> DoctorBlue
                Role.HUNTER -> HunterAmber
                Role.VILLAGER -> VillageGreen
            }
            Text(
                text = "${eliminated.role.emoji} ${eliminated.role.name}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = roleColor,
                textAlign = TextAlign.Center
            )
        } else if (isTie) {
            Text(
                text = "It's a tie!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = HunterAmber,
                textAlign = TextAlign.Center
            )
            Text(
                text = "No one is eliminated.",
                color = TextDim,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = "No votes were cast.",
                fontSize = 20.sp,
                color = TextDim,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        GameButton(text = "CONTINUE", onClick = onProceed)
    }
}
