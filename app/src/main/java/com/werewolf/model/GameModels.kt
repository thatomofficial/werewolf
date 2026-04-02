package com.werewolf.model

enum class Team { WEREWOLF, VILLAGE }

enum class Role(
    val team: Team,
    val emoji: String,
    val description: String,
    val hasNightAction: Boolean
) {
    WEREWOLF(
        Team.WEREWOLF, "\uD83D\uDC3A",
        "Each night, you and your pack choose a villager to eliminate.",
        true
    ),
    SEER(
        Team.VILLAGE, "\uD83D\uDD2E",
        "Each night, you may investigate one player to learn if they are a Werewolf.",
        true
    ),
    DOCTOR(
        Team.VILLAGE, "\uD83D\uDC89",
        "Each night, you may protect one player from the werewolves' attack.",
        true
    ),
    HUNTER(
        Team.VILLAGE, "\uD83C\uDFF9",
        "When you die, you may take one other player down with you.",
        false
    ),
    VILLAGER(
        Team.VILLAGE, "\uD83C\uDFE0",
        "You have no special power, but your vote is your weapon.",
        false
    );
}

data class Player(
    val name: String,
    val role: Role,
    var alive: Boolean = true
)

fun getRoleDistribution(numPlayers: Int): List<Role> {
    val (wolves, hunter) = when {
        numPlayers <= 7 -> 2 to 0
        numPlayers <= 9 -> 2 to 1
        numPlayers <= 12 -> 3 to 1
        else -> 4 to 1
    }
    val roles = mutableListOf<Role>()
    repeat(wolves) { roles.add(Role.WEREWOLF) }
    roles.add(Role.SEER)
    roles.add(Role.DOCTOR)
    repeat(hunter) { roles.add(Role.HUNTER) }
    val villagers = numPlayers - roles.size
    repeat(villagers) { roles.add(Role.VILLAGER) }
    return roles.shuffled()
}
