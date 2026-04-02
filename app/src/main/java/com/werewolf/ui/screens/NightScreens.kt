package com.werewolf.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.werewolf.model.Player
import com.werewolf.model.SeerResult
import com.werewolf.ui.components.*
import com.werewolf.ui.theme.*

@Composable
fun NightBannerScreen(roundNumber: Int, onProceed: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "\uD83C\uDF19", fontSize = 72.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "NIGHT $roundNumber",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = NightBlue,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "The village falls asleep...",
            fontSize = 16.sp,
            color = TextDim,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        GameButton(text = "BEGIN NIGHT", onClick = onProceed, color = NightBlue)
    }
}

@Composable
fun WerewolfTurnScreen(
    wolf: Player,
    teammates: List<Player>,
    targets: List<Player>,
    previousVotes: Map<String, Player>,
    onSelectTarget: (Player) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        PhaseHeader(
            emoji = "\uD83D\uDC3A",
            title = "Werewolf Phase",
            subtitle = "${wolf.name}, choose your victim",
            color = WerewolfRed
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (teammates.isNotEmpty()) {
            InfoCard {
                Text(text = "Pack: ${teammates.joinToString { it.name }}", color = WerewolfRed, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (previousVotes.isNotEmpty()) {
            InfoCard {
                Text(text = "Votes so far:", color = TextDim, fontSize = 13.sp)
                previousVotes.forEach { (voter, target) ->
                    Text(text = "$voter \u2192 ${target.name}", color = WerewolfRed, fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(targets) { target ->
                PlayerSelectButton(
                    name = target.name,
                    onClick = { onSelectTarget(target) },
                    borderColor = WerewolfRed
                )
            }
        }
    }
}

@Composable
fun SeerTurnScreen(
    seer: Player,
    targets: List<Player>,
    result: SeerResult?,
    onInvestigate: (Player) -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PhaseHeader(
            emoji = "\uD83D\uDD2E",
            title = "Seer Phase",
            subtitle = "${seer.name}, investigate a player",
            color = SeerCyan
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (result == null) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(targets) { target ->
                    PlayerSelectButton(
                        name = target.name,
                        onClick = { onInvestigate(target) },
                        borderColor = SeerCyan
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(24.dp))

            InfoCard {
                if (result.isWerewolf) {
                    Text(
                        text = "\u26A0\uFE0F ${result.targetName}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = WerewolfRed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "is a WEREWOLF!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = WerewolfRed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "\u2705 ${result.targetName}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = VillageGreen,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "is NOT a Werewolf",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = VillageGreen,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            GameButton(
                text = "GOT IT",
                onClick = onDone,
                color = SeerCyan
            )
        }
    }
}

@Composable
fun DoctorTurnScreen(
    doctor: Player,
    targets: List<Player>,
    lastProtected: Player?,
    onProtect: (Player) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        PhaseHeader(
            emoji = "\uD83D\uDC89",
            title = "Doctor Phase",
            subtitle = "${doctor.name}, protect someone",
            color = DoctorBlue
        )

        if (lastProtected != null) {
            Spacer(modifier = Modifier.height(8.dp))
            InfoCard {
                Text(
                    text = "You cannot protect ${lastProtected.name} again",
                    color = TextDim,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            val validTargets = if (lastProtected != null) {
                targets.filter { it.name != lastProtected.name }
            } else {
                targets
            }
            items(validTargets) { target ->
                PlayerSelectButton(
                    name = target.name,
                    onClick = { onProtect(target) },
                    borderColor = DoctorBlue
                )
            }
        }
    }
}
