package com.werewolf.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.werewolf.ui.components.GameButton
import com.werewolf.ui.theme.*

@Composable
fun TitleScreen(onStart: (Int) -> Unit) {
    var playerCountText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    fun submit() {
        val count = playerCountText.toIntOrNull()
        if (count == null || count < 6 || count > 15) {
            error = "Enter a number between 6 and 15"
        } else {
            error = null
            onStart(count)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "\uD83D\uDC3A",
            fontSize = 72.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "WEREWOLF",
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            color = WerewolfRed,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "A Social Deduction Game",
            fontSize = 16.sp,
            color = TextDim,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = playerCountText,
            onValueChange = {
                playerCountText = it.filter { c -> c.isDigit() }.take(2)
                error = null
            },
            label = { Text("Number of Players (6-15)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Go
            ),
            keyboardActions = KeyboardActions(onGo = { submit() }),
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = WerewolfRed,
                cursorColor = WerewolfRed,
                focusedLabelColor = WerewolfRed
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        GameButton(
            text = "START GAME",
            onClick = { submit() },
            enabled = playerCountText.isNotBlank()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = DarkCard.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Roles",
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(8.dp))
                RoleInfo("\uD83D\uDC3A Werewolf", "Eliminate villagers at night", WerewolfRed)
                RoleInfo("\uD83D\uDD2E Seer", "Investigate one player each night", SeerCyan)
                RoleInfo("\uD83D\uDC89 Doctor", "Protect one player each night", DoctorBlue)
                RoleInfo("\uD83C\uDFF9 Hunter", "Shoot someone when you die", HunterAmber)
                RoleInfo("\uD83C\uDFE0 Villager", "Vote to find the werewolves", VillageGreen)
            }
        }
    }
}

@Composable
private fun RoleInfo(label: String, desc: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = color, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Text(text = " — $desc", color = TextDim, fontSize = 13.sp)
    }
}
