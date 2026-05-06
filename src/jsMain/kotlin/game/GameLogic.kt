package game

import kotlinx.serialization.Serializable

const val EMPTY = 0
const val PLAYER_1 = 1
const val PLAYER_2 = 2

@Serializable
data class GameState(
    val boardSize: Int = 7,
    val winLength: Int = 4,
    val board: List<List<Int>> = emptyBoard(boardSize),
    val currentPlayer: Int = PLAYER_1,
    val winner: Int = EMPTY,
    val winCells: List<List<Int>> = emptyList(),
    val draw: Boolean = false,
    val lastDropRow: Int = -1,
    val lastDropCol: Int = -1,
)

data class DropResult(val board: List<List<Int>>, val row: Int)

fun emptyBoard(size: Int): List<List<Int>> = List(size) { List(size) { EMPTY } }

fun drop(board: List<List<Int>>, col: Int, player: Int): DropResult? {
    if (col !in board.indices) return null
    for (row in board.size - 1 downTo 0) {
        if (board[row][col] == EMPTY) {
            val newBoard = board.mapIndexed { r, rowList ->
                if (r == row) rowList.toMutableList().also { it[col] = player } else rowList
            }
            return DropResult(newBoard, row)
        }
    }
    return null
}

fun checkWin(board: List<List<Int>>, winLength: Int): List<Pair<Int, Int>>? {
    val n = board.size
    val dirs = listOf(0 to 1, 1 to 0, 1 to 1, 1 to -1)
    for (r in 0 until n) for (c in 0 until n) {
        val player = board[r][c]
        if (player == EMPTY) continue
        for ((dr, dc) in dirs) {
            val cells = (0 until winLength).map { i -> r + dr * i to c + dc * i }
            if (cells.all { (rr, cc) ->
                    rr in 0 until n && cc in 0 until n && board[rr][cc] == player
                }) return cells
        }
    }
    return null
}

fun isDraw(board: List<List<Int>>): Boolean = board.all { row -> row.none { it == EMPTY } }

fun applyMove(state: GameState, col: Int): GameState {
    if (state.winner != EMPTY || state.draw) return state
    val result = drop(state.board, col, state.currentPlayer) ?: return state
    val win = checkWin(result.board, state.winLength)
    val draw = win == null && isDraw(result.board)
    return state.copy(
        board = result.board,
        currentPlayer = if (win == null && !draw) other(state.currentPlayer) else state.currentPlayer,
        winner = if (win != null) state.currentPlayer else EMPTY,
        winCells = win?.map { listOf(it.first, it.second) } ?: emptyList(),
        draw = draw,
        lastDropRow = result.row,
        lastDropCol = col,
    )
}

fun other(player: Int): Int = if (player == PLAYER_1) PLAYER_2 else PLAYER_1

fun newGame(size: Int, winLength: Int): GameState {
    val s = size.coerceIn(6, 15)
    val w = winLength.coerceIn(3, minOf(s, 10))
    return GameState(boardSize = s, winLength = w)
}
