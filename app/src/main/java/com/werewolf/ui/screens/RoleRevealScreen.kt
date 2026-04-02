package com.werewolf.ui.screens

import androidx.compose.foundation.layout.*
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
import com.werewolf.ui.components.GameButton
import com.werewolf.ui.components.InfoCard
import com.werewolf.ui.theme.*

@Composable
fun RoleRevealScreen(
    player: Player,
    revealed: Boolean,
    teammates: List<Player>,
    onReveal: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Hello, ${player.name}!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (!revealed) {
            Text(
                text = "?",
                fontSize = 72.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            GameButton(
                text = "REVEAL MY ROLE",
                onClick = onReveal,
                color = NightBlue
            )
        } else {
            val color = when (player.role) {
                Role.WEREWOLF -> WerewolfRed
                Role.SEER -> SeerCyan
                Role.DOCTOR -> DoctorBlue
                Role.HUNTER -> HunterAmber
                Role.VILLAGER -> VillageGreen
            }

            Text(
                text = player.role.emoji,
                fontSize = 72.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = player.role.name,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = player.role.description,
                fontSize = 15.sp,
                color = TextDim,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            if (player.role == Role.WEREWOLF && teammates.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                InfoCard {
                    Text(
                        text = "Your pack:",
                        color = WerewolfRed,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    teammates.forEach { mate ->
                        Text(
                            text = "\uD83D\uDC3A ${mate.name}",
                            color = WerewolfRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            GameButton(
                text = "GOT IT",
                onClick = onNext,
                color = color
            )
        }
    }
}
