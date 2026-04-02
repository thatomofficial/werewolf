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
import com.werewolf.ui.components.GameButton
import com.werewolf.ui.theme.*

@Composable
fun HandoffScreen(playerName: String, onReady: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "\uD83D\uDCF1",
            fontSize = 64.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Pass the device to:",
            fontSize = 18.sp,
            color = TextDim,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = playerName,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = HunterAmber,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Only $playerName should\nbe looking at the screen!",
            fontSize = 14.sp,
            color = TextDim,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        GameButton(
            text = "I'M ${ playerName.uppercase() } — READY",
            onClick = onReady,
            color = NightBlue
        )
    }
}
