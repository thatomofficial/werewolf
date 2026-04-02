package com.werewolf.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.werewolf.ui.components.GameButton
import com.werewolf.ui.theme.*

@Composable
fun SetupScreen(playerCount: Int, onSubmit: (List<String>) -> Unit) {
    val names = remember { mutableStateListOf(*Array(playerCount) { "" }) }
    var error by remember { mutableStateOf<String?>(null) }

    fun submit() {
        val trimmed = names.map { it.trim() }
        when {
            trimmed.any { it.isBlank() } -> error = "All names are required"
            trimmed.distinct().size != trimmed.size -> error = "Names must be unique"
            else -> onSubmit(trimmed)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Enter Player Names",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )
        Text(
            text = "$playerCount players",
            fontSize = 14.sp,
            color = TextDim
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (error != null) {
            Text(
                text = error!!,
                color = WerewolfRed,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(names) { index, name ->
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        names[index] = it
                        error = null
                    },
                    label = { Text("Player ${index + 1}") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = if (index == playerCount - 1) ImeAction.Done else ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WerewolfRed,
                        cursorColor = WerewolfRed,
                        focusedLabelColor = WerewolfRed
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        GameButton(
            text = "ASSIGN ROLES",
            onClick = { submit() },
            color = VillageGreen
        )
    }
}
