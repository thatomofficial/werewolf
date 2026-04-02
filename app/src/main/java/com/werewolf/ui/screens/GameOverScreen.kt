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
import com.werewolf.model.Role
import com.werewolf.model.Team
import com.werewolf.ui.components.GameButton
import com.werewolf.ui.components.InfoCard
import com.werewolf.ui.theme.*

@Composable
fun GameOverScreen(
    winner: Team,
    players: List<Player>,
    onPlayAgain: () -> Unit
) {
    val isVillageWin = winner == Team.VILLAGE

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isVillageWin) "\uD83C\uDFE0" else "\uD83D\uDC3A",
            fontSize = 72.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isVillageWin) "VILLAGE WINS!" else "WEREWOLVES WIN!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = if (isVillageWin) VillageGreen else WerewolfRed,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (isVillageWin)
                "All werewolves have been eliminated!\nPeace returns to the village."
            else
                "The werewolves have overrun the village.\nDarkness descends forever.",
            fontSize = 14.sp,
            color = TextDim,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        InfoCard {
            Text(
                text = "Final Roles",
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                fontSize = 18.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(players) { player ->
                val roleColor = when (player.role) {
                    Role.WEREWOLF -> WerewolfRed
                    Role.SEER -> SeerCyan
                    Role.DOCTOR -> DoctorBlue
                    Role.HUNTER -> HunterAmber
                    Role.VILLAGER -> VillageGreen
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${player.role.emoji} ${player.name}",
                        color = if (player.alive) TextWhite else DeathGray,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = player.role.name,
                        color = roleColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (player.alive) "Alive" else "Dead",
                        color = if (player.alive) VillageGreen else WerewolfRed,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        GameButton(
            text = "PLAY AGAIN",
            onClick = onPlayAgain,
            color = if (isVillageWin) VillageGreen else WerewolfRed
        )
    }
}
