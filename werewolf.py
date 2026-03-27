#!/usr/bin/env python3
"""
WEREWOLF - A Terminal-Based Social Deduction Party Game
=======================================================
Classic Werewolf (Mafia) game for 6-15 players.
Pass-and-play on a single computer.

Roles: Werewolf, Villager, Seer, Doctor, Hunter
"""

import os
import sys
import random
import time
import shutil

# ─── ANSI Colors ────────────────────────────────────────────────────────────

RESET = "\033[0m"
BOLD = "\033[1m"
DIM = "\033[2m"
RED = "\033[91m"
GREEN = "\033[92m"
YELLOW = "\033[93m"
BLUE = "\033[94m"
MAGENTA = "\033[95m"
CYAN = "\033[96m"
WHITE = "\033[97m"
BG_RED = "\033[41m"
BG_BLUE = "\033[44m"

# ─── ASCII Art ──────────────────────────────────────────────────────────────

TITLE_ART = rf"""
{RED}{BOLD}
 __        __                             _  __
 \ \      / /__ _ __ _____      _____  | |/ _|
  \ \ /\ / / _ \ '__/ _ \ \ /\ / / _ \ | | |_
   \ V  V /  __/ | |  __/\ V  V / (_) || |  _|
    \_/\_/ \___|_|  \___| \_/\_/ \___/ |_|_|
{RESET}
{DIM}  ╔══════════════════════════════════════════╗
  ║   A Social Deduction Game for 6-15 Players  ║
  ╚══════════════════════════════════════════╝{RESET}
"""

NIGHT_ART = f"""
{BLUE}{BOLD}
        ___---___
     .--         --.
   ./   ()      .-. \\.
  /   o    .   (   )  \\
 / .            '-'    \\
| ()    .  O         . |
|                      |
|    o       ()        |
 \\             o      /
  \\   ()          .  /
   `\\.          .  /'
     `--.______.--'
{DIM}    ☆  ·  ✦  ·  ☆  ·  ✦
{RESET}
"""

DAY_ART = f"""
{YELLOW}{BOLD}
       \\   |   /
        \\  |  /
     --- (( )) ---
        /  |  \\
       /   |   \\
{RESET}
{YELLOW}  ═══ The Village Awakens ═══{RESET}
"""

DEATH_ART = f"""
{RED}{DIM}
      ___________
     /           \\
    |   R.I.P.   |
    |             |
    |             |
    |_____________|
   /_______________\\
{RESET}
"""

WEREWOLF_WIN_ART = f"""
{RED}{BOLD}
  ╔══════════════════════════════════════╗
  ║                                      ║
  ║     🐺  THE WEREWOLVES WIN!  🐺     ║
  ║                                      ║
  ║   The village has been overrun...    ║
  ║   Darkness descends forever.         ║
  ║                                      ║
  ╚══════════════════════════════════════╝
{RESET}
"""

VILLAGE_WIN_ART = f"""
{GREEN}{BOLD}
  ╔══════════════════════════════════════╗
  ║                                      ║
  ║     🏘  THE VILLAGE WINS!  🏘       ║
  ║                                      ║
  ║   All werewolves have been found     ║
  ║   and eliminated! Peace returns.     ║
  ║                                      ║
  ╚══════════════════════════════════════╝
{RESET}
"""

# ─── Role Definitions ──────────────────────────────────────────────────────

ROLES = {
    "Werewolf": {
        "team": "werewolf",
        "color": RED,
        "symbol": "🐺",
        "description": "Each night, you and your pack choose a villager to eliminate.",
        "night_action": "kill",
    },
    "Seer": {
        "team": "village",
        "color": CYAN,
        "symbol": "🔮",
        "description": "Each night, you may investigate one player to learn if they are a Werewolf.",
        "night_action": "investigate",
    },
    "Doctor": {
        "team": "village",
        "color": BLUE,
        "symbol": "💉",
        "description": "Each night, you may protect one player from the werewolves' attack.",
        "night_action": "protect",
    },
    "Hunter": {
        "team": "village",
        "color": YELLOW,
        "symbol": "🏹",
        "description": "When you die, you may take one other player down with you.",
        "night_action": None,
    },
    "Villager": {
        "team": "village",
        "color": GREEN,
        "symbol": "🏘",
        "description": "You have no special power, but your vote is your weapon. Use it wisely.",
        "night_action": None,
    },
}


def get_role_distribution(num_players):
    """Return a list of role names based on player count."""
    if num_players <= 7:
        wolves, seer, doctor, hunter = 2, 1, 1, 0
    elif num_players <= 9:
        wolves, seer, doctor, hunter = 2, 1, 1, 1
    elif num_players <= 12:
        wolves, seer, doctor, hunter = 3, 1, 1, 1
    else:
        wolves, seer, doctor, hunter = 4, 1, 1, 1

    roles = (
        ["Werewolf"] * wolves
        + ["Seer"] * seer
        + ["Doctor"] * doctor
        + ["Hunter"] * hunter
    )
    villagers = num_players - len(roles)
    roles += ["Villager"] * villagers
    return roles


# ─── Game State ─────────────────────────────────────────────────────────────


class Player:
    def __init__(self, name, role):
        self.name = name
        self.role = role
        self.alive = True

    @property
    def team(self):
        return ROLES[self.role]["team"]

    @property
    def color(self):
        return ROLES[self.role]["color"]

    @property
    def symbol(self):
        return ROLES[self.role]["symbol"]


class GameState:
    def __init__(self, players):
        self.players = players
        self.round_number = 0
        self.game_over = False
        self.winner = None
        self.last_protected = None  # Doctor can't protect same player twice in a row

    def alive_players(self):
        return [p for p in self.players if p.alive]

    def alive_werewolves(self):
        return [p for p in self.players if p.alive and p.role == "Werewolf"]

    def alive_villagers(self):
        return [p for p in self.players if p.alive and p.team == "village"]

    def get_alive_with_role(self, role):
        return [p for p in self.players if p.alive and p.role == role]


# ─── Utility Functions ──────────────────────────────────────────────────────


def clear_screen():
    os.system("cls" if os.name == "nt" else "clear")


def term_width():
    return shutil.get_terminal_size((80, 24)).columns


def center_text(text):
    w = term_width()
    lines = text.split("\n")
    return "\n".join(line.center(w) for line in lines)


def print_centered(text):
    print(center_text(text))


def print_separator():
    print(f"{DIM}{'─' * min(term_width(), 60)}{RESET}")


def dramatic_pause(seconds=1.5):
    time.sleep(seconds)


def press_enter(prompt="Press Enter to continue..."):
    input(f"\n{DIM}{prompt}{RESET}")


def player_handoff(player_name):
    """Secure pass-and-play transition between players."""
    clear_screen()
    print("\n" * 3)
    print_centered(f"{BOLD}Pass the device to:{RESET}")
    print()
    print_centered(f"{BOLD}{YELLOW}>>> {player_name} <<<{RESET}")
    print("\n" * 2)
    print_centered(f"{DIM}(Only {player_name} should be looking at the screen){RESET}")
    press_enter(f"{player_name}, press Enter when ready...")
    clear_screen()


def get_choice(prompt, options, allow_none=False):
    """Get a validated numeric choice from the player.

    options: list of (display_string, value) tuples
    Returns the value of the chosen option.
    """
    while True:
        print()
        for i, (display, _) in enumerate(options, 1):
            print(f"  {BOLD}{i}.{RESET} {display}")
        if allow_none:
            print(f"  {BOLD}0.{RESET} {DIM}Skip / Abstain{RESET}")
        print()
        try:
            raw = input(f"{prompt} ")
            choice = int(raw)
            if allow_none and choice == 0:
                return None
            if 1 <= choice <= len(options):
                return options[choice - 1][1]
        except (ValueError, EOFError):
            pass
        print(f"{RED}Invalid choice. Try again.{RESET}")


def show_alive_players(state, exclude=None):
    """Return list of (display_string, player) for alive players, optionally excluding one."""
    options = []
    for p in state.alive_players():
        if p is not exclude:
            options.append((p.name, p))
    return options


def countdown_timer(seconds, label="Discussion time"):
    """Show a countdown timer. Press Enter to skip."""
    import select

    print(f"\n{YELLOW}{BOLD}  {label}: {seconds}s{RESET}")
    print(f"{DIM}  (Press Enter to skip the timer){RESET}\n")

    # Simple approach: just use a blocking wait since select may not work everywhere
    try:
        import threading

        skip_event = threading.Event()

        def wait_for_enter():
            try:
                input()
                skip_event.set()
            except EOFError:
                skip_event.set()

        t = threading.Thread(target=wait_for_enter, daemon=True)
        t.start()

        remaining = seconds
        while remaining > 0 and not skip_event.is_set():
            mins, secs = divmod(remaining, 60)
            timer_str = f"  ⏱  {mins:02d}:{secs:02d} remaining"
            print(f"\r{YELLOW}{timer_str}{RESET}    ", end="", flush=True)
            skip_event.wait(timeout=1)
            remaining -= 1

        print(f"\r{GREEN}  ⏱  Time's up!                    {RESET}")
        print()
    except Exception:
        # Fallback: just wait
        press_enter("Press Enter when discussion is over...")


# ─── Setup Phase ────────────────────────────────────────────────────────────


def setup_game():
    """Interactive game setup. Returns a GameState."""
    clear_screen()
    print(TITLE_ART)
    print()

    # Get player count
    while True:
        try:
            raw = input(f"  {BOLD}How many players? (6-15):{RESET} ")
            num = int(raw)
            if 6 <= num <= 15:
                break
            print(f"  {RED}Please enter a number between 6 and 15.{RESET}")
        except (ValueError, EOFError):
            print(f"  {RED}Please enter a valid number.{RESET}")

    print()
    print_separator()

    # Get player names
    names = []
    for i in range(1, num + 1):
        while True:
            name = input(f"  {BOLD}Player {i} name:{RESET} ").strip()
            if not name:
                print(f"  {RED}Name cannot be empty.{RESET}")
            elif name.lower() in [n.lower() for n in names]:
                print(f"  {RED}Name already taken.{RESET}")
            else:
                names.append(name)
                break

    # Assign roles
    role_list = get_role_distribution(num)
    random.shuffle(role_list)
    players = [Player(name, role) for name, role in zip(names, role_list)]

    print()
    print_separator()
    print(f"\n  {GREEN}{BOLD}All players registered!{RESET}")
    print(f"  {DIM}Roles have been secretly assigned.{RESET}")
    print(f"  {DIM}Each player will now privately view their role.{RESET}")
    press_enter()

    # Role reveal
    for player in players:
        player_handoff(player.name)
        role_info = ROLES[player.role]
        print(f"\n  {BOLD}Hello, {player.name}!{RESET}\n")
        print_separator()
        print(f"\n  Your role is: {role_info['color']}{BOLD}{role_info['symbol']}  {player.role}{RESET}")
        print(f"\n  {DIM}{role_info['description']}{RESET}")

        if player.role == "Werewolf":
            teammates = [p.name for p in players if p.role == "Werewolf" and p is not player]
            if teammates:
                print(f"\n  {RED}Your fellow werewolves: {', '.join(teammates)}{RESET}")

        print()
        print_separator()
        press_enter("Memorize your role, then press Enter...")

    state = GameState(players)
    return state


# ─── Night Phase ────────────────────────────────────────────────────────────


def night_phase(state):
    """Execute the night phase. Returns (killed_player_or_None, protected_player_or_None)."""
    state.round_number += 1

    clear_screen()
    print(NIGHT_ART)
    print_centered(f"{BLUE}{BOLD}══ NIGHT {state.round_number} ══{RESET}")
    print_centered(f"{DIM}The village falls asleep...{RESET}")
    dramatic_pause(2)

    kill_target = werewolf_turn(state)
    investigate_target = seer_turn(state)
    protect_target = doctor_turn(state)

    return kill_target, protect_target


def werewolf_turn(state):
    """Werewolves choose their victim. Returns the targeted player."""
    wolves = state.alive_werewolves()
    if not wolves:
        return None

    votes = {}
    for i, wolf in enumerate(wolves):
        player_handoff(wolf.name)
        role_info = ROLES["Werewolf"]
        print(f"  {RED}{BOLD}{role_info['symbol']}  WEREWOLF PHASE{RESET}")
        print_separator()
        print(f"\n  {BOLD}{wolf.name}{RESET}, you are a {RED}Werewolf{RESET}.")

        # Show teammates
        teammates = [w.name for w in wolves if w is not wolf]
        if teammates:
            print(f"  {DIM}Pack members: {', '.join(teammates)}{RESET}")

        # Show previous wolf votes this round
        if votes:
            print(f"\n  {DIM}Votes so far:{RESET}")
            for voter_name, target in votes.items():
                print(f"    {DIM}{voter_name} → {target.name}{RESET}")

        print(f"\n  {RED}Choose your victim:{RESET}")
        options = show_alive_players(state, exclude=None)
        # Remove werewolves from targets
        options = [(name, p) for name, p in options if p.role != "Werewolf"]
        target = get_choice(f"  {RED}>{RESET}", options)
        votes[wolf.name] = target
        press_enter()

    # Tally votes - most voted target wins, ties broken randomly
    from collections import Counter

    vote_counts = Counter(votes.values())
    max_votes = max(vote_counts.values())
    top_targets = [t for t, c in vote_counts.items() if c == max_votes]
    return random.choice(top_targets)


def seer_turn(state):
    """Seer investigates a player. Returns None."""
    seers = state.get_alive_with_role("Seer")
    if not seers:
        return None

    seer = seers[0]
    player_handoff(seer.name)
    role_info = ROLES["Seer"]
    print(f"  {CYAN}{BOLD}{role_info['symbol']}  SEER PHASE{RESET}")
    print_separator()
    print(f"\n  {BOLD}{seer.name}{RESET}, you are the {CYAN}Seer{RESET}.")
    print(f"  {DIM}Choose a player to investigate.{RESET}")
    print(f"\n  {CYAN}Who do you want to investigate?{RESET}")

    options = show_alive_players(state, exclude=seer)
    target = get_choice(f"  {CYAN}>{RESET}", options)

    print()
    print_separator()
    if target.role == "Werewolf":
        print(f"\n  {RED}{BOLD}  ⚠  {target.name} is a WEREWOLF!  ⚠{RESET}")
    else:
        print(f"\n  {GREEN}{BOLD}  ✓  {target.name} is NOT a Werewolf.  ✓{RESET}")
    print()
    print_separator()
    press_enter("Remember this information. Press Enter...")
    return None


def doctor_turn(state):
    """Doctor protects a player. Returns the protected player."""
    doctors = state.get_alive_with_role("Doctor")
    if not doctors:
        return None

    doctor = doctors[0]
    player_handoff(doctor.name)
    role_info = ROLES["Doctor"]
    print(f"  {BLUE}{BOLD}{role_info['symbol']}  DOCTOR PHASE{RESET}")
    print_separator()
    print(f"\n  {BOLD}{doctor.name}{RESET}, you are the {BLUE}Doctor{RESET}.")
    print(f"  {DIM}Choose a player to protect tonight.{RESET}")

    if state.last_protected:
        print(f"  {DIM}(You cannot protect {state.last_protected.name} again){RESET}")

    options = show_alive_players(state)
    # Remove last protected
    if state.last_protected and state.last_protected.alive:
        options = [(name, p) for name, p in options if p is not state.last_protected]

    print(f"\n  {BLUE}Who do you want to protect?{RESET}")
    target = get_choice(f"  {BLUE}>{RESET}", options)

    print(f"\n  {GREEN}You will protect {BOLD}{target.name}{RESET}{GREEN} tonight.{RESET}")
    state.last_protected = target
    press_enter()
    return target


# ─── Dawn / Night Resolution ───────────────────────────────────────────────


def resolve_night(state, kill_target, protect_target):
    """Resolve the night. Returns list of players who died."""
    deaths = []

    if kill_target is None:
        return deaths

    if protect_target is not None and kill_target is protect_target:
        # Doctor saved them!
        return deaths

    # Player dies
    kill_target.alive = False
    deaths.append(kill_target)
    return deaths


def dawn_announcement(state, deaths):
    """Announce the results of the night. Returns any additional deaths (hunter revenge)."""
    clear_screen()
    print(DAY_ART)

    if not deaths:
        print_centered(f"{GREEN}{BOLD}The sun rises... everyone survived the night!{RESET}")
        print_centered(f"{DIM}The Doctor must have been busy.{RESET}")
        dramatic_pause(2)
    else:
        print_centered(f"{RED}{BOLD}The sun rises... but someone is missing.{RESET}")
        dramatic_pause(2)

        for dead in deaths:
            print(DEATH_ART)
            role_info = ROLES[dead.role]
            print_centered(
                f"{RED}{BOLD}{dead.name}{RESET} was killed in the night!"
            )
            print_centered(
                f"They were a {role_info['color']}{BOLD}{role_info['symbol']} {dead.role}{RESET}."
            )
            dramatic_pause(2)

    press_enter()

    # Check for hunter revenge
    additional_deaths = []
    for dead in deaths:
        if dead.role == "Hunter":
            revenge_death = hunter_revenge(state, dead)
            if revenge_death:
                additional_deaths.append(revenge_death)

    return additional_deaths


def hunter_revenge(state, hunter):
    """The Hunter takes someone down with them. Returns killed player or None."""
    clear_screen()
    print(f"\n  {YELLOW}{BOLD}🏹  HUNTER'S REVENGE  🏹{RESET}")
    print_separator()
    print(f"\n  {BOLD}{hunter.name}{RESET} was the {YELLOW}Hunter{RESET}!")
    print(f"  {DIM}With their dying breath, they raise their crossbow...{RESET}")
    dramatic_pause(1)

    print(f"\n  {YELLOW}{hunter.name}, choose someone to take with you:{RESET}")

    # Everyone sees this - it's a public event, but hunter chooses
    # In pass-and-play, we ask the hunter player to make the choice
    options = show_alive_players(state)
    if not options:
        print(f"\n  {DIM}There's no one left to shoot.{RESET}")
        press_enter()
        return None

    target = get_choice(f"  {YELLOW}>{RESET}", options)

    target.alive = False
    print()
    print_separator()
    role_info = ROLES[target.role]
    print(f"\n  {RED}{BOLD}💀 {target.name}{RESET} has been shot by the Hunter!")
    print(
        f"  They were a {role_info['color']}{BOLD}{role_info['symbol']} {target.role}{RESET}."
    )
    dramatic_pause(2)
    press_enter()
    return target


# ─── Day Phase ──────────────────────────────────────────────────────────────


def day_phase(state):
    """Execute the day phase. Returns the eliminated player or None."""
    clear_screen()
    print(f"\n  {YELLOW}{BOLD}══ DAY {state.round_number} ══{RESET}\n")
    print_separator()
    print(f"\n  {BOLD}Surviving players:{RESET}\n")
    for p in state.alive_players():
        print(f"    • {p.name}")
    print()
    print_separator()

    # Discussion phase
    alive_count = len(state.alive_players())
    if alive_count <= 7:
        discussion_time = 60
    elif alive_count <= 10:
        discussion_time = 90
    else:
        discussion_time = 120

    print(f"\n  {BOLD}Discussion phase!{RESET}")
    print(f"  {DIM}Talk among yourselves. Who is suspicious?{RESET}")
    countdown_timer(discussion_time, "Discussion time")

    # Voting phase
    print_separator()
    print(f"\n  {BOLD}{RED}VOTING TIME{RESET}")
    print(f"  {DIM}Each player will secretly cast their vote.{RESET}")
    press_enter()

    votes = {}
    for player in state.alive_players():
        player_handoff(player.name)
        print(f"  {BOLD}🗳  VOTING  🗳{RESET}")
        print_separator()
        print(f"\n  {BOLD}{player.name}{RESET}, cast your vote.")
        print(f"  {DIM}Who do you want to eliminate?{RESET}\n")

        options = show_alive_players(state, exclude=player)
        target = get_choice(f"  {RED}Vote>{RESET}", options, allow_none=True)
        votes[player] = target
        if target:
            print(f"\n  {DIM}You voted for {target.name}.{RESET}")
        else:
            print(f"\n  {DIM}You abstained.{RESET}")
        press_enter()

    # Tally and announce
    return resolve_votes(state, votes)


def resolve_votes(state, votes):
    """Tally votes and eliminate a player. Returns eliminated player or None."""
    clear_screen()
    print(f"\n  {BOLD}🗳  VOTE RESULTS  🗳{RESET}\n")
    print_separator()

    # Show all votes
    from collections import Counter

    vote_counts = Counter()
    for voter, target in votes.items():
        if target:
            print(f"  {voter.name} voted for {RED}{BOLD}{target.name}{RESET}")
            vote_counts[target] += 1
        else:
            print(f"  {voter.name} {DIM}abstained{RESET}")

    print()
    print_separator()
    dramatic_pause(2)

    if not vote_counts:
        print(f"\n  {DIM}No votes were cast. No one is eliminated.{RESET}")
        press_enter()
        return None

    # Find the top vote-getter
    max_votes = max(vote_counts.values())
    top_targets = [t for t, c in vote_counts.items() if c == max_votes]

    if len(top_targets) > 1:
        # Tie - no elimination
        tied_names = ", ".join(t.name for t in top_targets)
        print(f"\n  {YELLOW}{BOLD}It's a tie between: {tied_names}{RESET}")
        print(f"  {DIM}No one is eliminated.{RESET}")
        press_enter()
        return None

    # Elimination
    eliminated = top_targets[0]
    eliminated.alive = False

    print(DEATH_ART)
    role_info = ROLES[eliminated.role]
    print_centered(
        f"{RED}{BOLD}{eliminated.name}{RESET} has been eliminated by the village!"
    )
    print_centered(
        f"They were a {role_info['color']}{BOLD}{role_info['symbol']} {eliminated.role}{RESET}."
    )
    dramatic_pause(2)
    press_enter()
    return eliminated


# ─── Win Condition ──────────────────────────────────────────────────────────


def check_win_condition(state):
    """Check if the game is over. Sets state.game_over and state.winner."""
    alive_wolves = len(state.alive_werewolves())
    alive_village = len(state.alive_villagers())

    if alive_wolves == 0:
        state.game_over = True
        state.winner = "village"
    elif alive_wolves >= alive_village:
        state.game_over = True
        state.winner = "werewolves"


def show_game_over(state):
    """Display the game over screen with final role reveal."""
    clear_screen()

    if state.winner == "village":
        print(VILLAGE_WIN_ART)
    else:
        print(WEREWOLF_WIN_ART)

    dramatic_pause(2)

    print(f"\n  {BOLD}Final Role Reveal:{RESET}\n")
    print_separator()
    for p in state.players:
        role_info = ROLES[p.role]
        status = f"{GREEN}Survived{RESET}" if p.alive else f"{RED}Dead{RESET}"
        print(
            f"  {role_info['color']}{role_info['symbol']} {p.name:<20}{RESET}"
            f" - {role_info['color']}{BOLD}{p.role:<12}{RESET}"
            f" [{status}]"
        )
    print()
    print_separator()
    print(f"\n  {DIM}Thanks for playing Werewolf!{RESET}\n")


# ─── Main Game Loop ────────────────────────────────────────────────────────


def game_loop(state):
    """Main game loop: night → dawn → day → repeat."""
    while not state.game_over:
        # Night Phase
        kill_target, protect_target = night_phase(state)
        deaths = resolve_night(state, kill_target, protect_target)

        # Dawn
        additional_deaths = dawn_announcement(state, deaths)
        # Hunter revenge deaths already handled

        # Check win after night
        check_win_condition(state)
        if state.game_over:
            break

        # Check win after hunter revenge
        if additional_deaths:
            check_win_condition(state)
            if state.game_over:
                break

        # Day Phase
        eliminated = day_phase(state)

        # Check for hunter revenge during day
        if eliminated and eliminated.role == "Hunter":
            revenge_death = hunter_revenge(state, eliminated)

        # Check win after day
        check_win_condition(state)

    show_game_over(state)


def main():
    """Entry point."""
    try:
        state = setup_game()
        game_loop(state)
    except KeyboardInterrupt:
        print(f"\n\n{DIM}Game interrupted. Goodbye!{RESET}\n")
        sys.exit(0)

    # Play again?
    print()
    again = input(f"  {BOLD}Play again? (y/n):{RESET} ").strip().lower()
    if again in ("y", "yes"):
        main()


if __name__ == "__main__":
    main()
