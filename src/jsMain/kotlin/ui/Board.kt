package ui

import androidx.compose.runtime.Composable
import game.GameState
import game.PLAYER_1
import org.jetbrains.compose.web.dom.Div

@Composable
fun Board(state: GameState, onDrop: (col: Int) -> Unit) {
    Div({
        classes("board")
        style {
            property("--cols", state.boardSize.toString())
            property("--rows", state.boardSize.toString())
        }
    }) {
        // Layer 1 (behind): cell-slots that hold the discs.
        Div({ classes("disc-layer") }) {
            for (r in 0 until state.boardSize) {
                for (c in 0 until state.boardSize) {
                    Cell(state, r, c, onDrop)
                }
            }
        }
        // Layer 2 (in front): solid frame with circular cutouts via a tiled mask.
        Div({ classes("frame-layer") }) {}
    }
}

@Composable
private fun Cell(state: GameState, row: Int, col: Int, onDrop: (Int) -> Unit) {
    val player = state.board[row][col]
    val isWin = state.winCells.any { it[0] == row && it[1] == col }
    val isLastDrop = state.lastDropRow == row && state.lastDropCol == col

    Div({
        classes("cell-slot")
        onClick { onDrop(col) }
    }) {
        if (player != 0) {
            Div({
                val cls = mutableListOf("disc", if (player == PLAYER_1) "p1" else "p2")
                if (isWin) cls += "win"
                if (isLastDrop) cls += "dropping"
                classes(*cls.toTypedArray())
                if (isLastDrop) {
                    style {
                        // Start (row+1) cells above the resting position so the
                        // disc enters the board from above and falls through every
                        // cutout on its way down.
                        property("--drop-from", "calc(-1 * (var(--cell) + var(--gap)) * ${row + 1})")
                        property("--drop-duration", "${280 + row * 55}ms")
                    }
                }
            }) {}
        }
    }
}
