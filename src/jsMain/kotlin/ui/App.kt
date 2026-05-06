package ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import game.GameState
import game.PLAYER_1
import game.applyMove
import game.loadState
import game.newGame
import game.saveState
import org.jetbrains.compose.web.attributes.max
import org.jetbrains.compose.web.attributes.min
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.NumberInput
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun App() {
    var state by remember { mutableStateOf(loadState() ?: newGame(7, 4)) }

    // Synchronous: every state transition writes to localStorage *before*
    // recomposition, so a refresh immediately after a move is never lossy.
    fun update(next: GameState) {
        state = next
        saveState(next)
    }

    Div({ classes("app") }) {
        Board(state) { col ->
            val next = applyMove(state, col)
            if (next !== state) playDropSound(state.currentPlayer)
            update(next)
        }
        Div({ classes("sidebar") }) {
            Status(state)
            Controls(
                initialSize = state.boardSize,
                initialWin = state.winLength,
                onApply = { size, win -> update(newGame(size, win)) },
            )
        }
    }
}

@Composable
private fun Status(state: GameState) {
    val cls = mutableListOf("status")
    when {
        state.winner != 0 -> {
            cls += "win"
            cls += if (state.winner == PLAYER_1) "p1" else "p2"
        }
        state.draw -> cls += "draw"
    }
    Div({ classes(*cls.toTypedArray()) }) {
        when {
            state.winner != 0 -> {
                Span({ classes("dot", if (state.winner == PLAYER_1) "p1" else "p2") }) {}
                Text(" Player ${state.winner} wins!")
            }
            state.draw -> Text("Draw")
            else -> {
                Span({ classes("dot", if (state.currentPlayer == PLAYER_1) "p1" else "p2") }) {}
                Text(" Player ${state.currentPlayer}'s turn")
            }
        }
    }
}

@Composable
private fun Controls(
    initialSize: Int,
    initialWin: Int,
    onApply: (size: Int, win: Int) -> Unit,
) {
    // Local pending values — typing is free-form; clamping happens on Apply.
    var size by remember { mutableStateOf(initialSize) }
    var win by remember { mutableStateOf(initialWin) }

    Div({ classes("controls") }) {
        Div({ classes("row") }) {
            Label { Text("Board size") }
            NumberInput(value = size) {
                min("6"); max("15")
                onInput { e -> e.value?.toInt()?.let { size = it } }
            }
        }
        Div({ classes("row") }) {
            Label { Text("Win length") }
            NumberInput(value = win) {
                min("3"); max("10")
                onInput { e -> e.value?.toInt()?.let { win = it } }
            }
        }
        Button(attrs = {
            classes("apply")
            onClick {
                val s = size.coerceIn(6, 15)
                val w = win.coerceIn(3, minOf(s, 10))
                size = s; win = w
                onApply(s, w)
            }
        }) { Text("New Game") }
        Div({ classes("hint") }) { Text("New Game applies the values above") }
    }
}
