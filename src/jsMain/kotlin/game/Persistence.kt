package game

import kotlinx.browser.localStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val KEY = "connect-four-state"

// encodeDefaults = true: write every field to JSON so the decoder never has to
// re-evaluate default expressions (e.g. board = emptyBoard(boardSize)).
private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

fun saveState(state: GameState) {
    try {
        val s = json.encodeToString(state)
        localStorage.setItem(KEY, s)
        console.log("[cf] saved (${s.length} chars, size=${state.boardSize}, win=${state.winLength})")
    } catch (t: Throwable) {
        console.warn("[cf] save failed: ${t.message}")
    }
}

fun loadState(): GameState? {
    val raw = localStorage.getItem(KEY)
    if (raw == null) {
        console.log("[cf] no saved state")
        return null
    }
    return try {
        val s = json.decodeFromString<GameState>(raw).copy(lastDropRow = -1, lastDropCol = -1)
        console.log("[cf] loaded (size=${s.boardSize}, win=${s.winLength}, winner=${s.winner})")
        s
    } catch (t: Throwable) {
        console.warn("[cf] load failed: ${t.message} — raw=${raw.take(120)}")
        null
    }
}
