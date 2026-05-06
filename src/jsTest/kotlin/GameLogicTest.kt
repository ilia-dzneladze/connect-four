import game.applyMove
import game.checkWin
import game.drop
import game.emptyBoard
import game.isDraw
import game.newGame
import game.PLAYER_1
import game.PLAYER_2
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameLogicTest {

    @Test
    fun dropLandsOnBottomOfEmptyColumn() {
        val board = emptyBoard(7)
        val result = drop(board, col = 3, player = PLAYER_1)
        assertNotNull(result)
        assertEquals(6, result.row)
        assertEquals(PLAYER_1, result.board[6][3])
    }

    @Test
    fun gravityStacksPieces() {
        var board = emptyBoard(7)
        board = drop(board, 2, PLAYER_1)!!.board
        board = drop(board, 2, PLAYER_2)!!.board
        val third = drop(board, 2, PLAYER_1)!!
        assertEquals(4, third.row)
        assertEquals(PLAYER_2, third.board[5][2])
        assertEquals(PLAYER_1, third.board[6][2])
    }

    @Test
    fun droppingInFullColumnReturnsNull() {
        var board = emptyBoard(4)
        repeat(4) { board = drop(board, 0, PLAYER_1)!!.board }
        assertNull(drop(board, 0, PLAYER_2))
    }

    @Test
    fun horizontalWinIsDetected() {
        var state = newGame(7, 4)
        // P1 plays 0..3 along the bottom; P2 plays harmless filler in col 6.
        listOf(0, 6, 1, 6, 2, 6, 3).forEach { state = applyMove(state, it) }
        assertEquals(PLAYER_1, state.winner)
        assertEquals(4, state.winCells.size)
    }

    @Test
    fun diagonalWinIsDetected() {
        var state = newGame(7, 4)
        // Build a /-diagonal for P1 at (6,0), (5,1), (4,2), (3,3).
        val moves = listOf(
            0,           // P1 -> (6,0)
            1,           // P2 -> (6,1)
            1,           // P1 -> (5,1)
            2,           // P2 -> (6,2)
            3,           // P1 -> (6,3)  filler so col 2 stack works
            2,           // P2 -> (5,2)
            2,           // P1 -> (4,2)
            3,           // P2 -> (5,3)
            6,           // P1 -> (6,6)  filler
            3,           // P2 -> (4,3)
            3,           // P1 -> (3,3)  -> wins
        )
        moves.forEach { state = applyMove(state, it) }
        assertEquals(PLAYER_1, state.winner, "winner should be P1; board=${state.board}")
    }

    @Test
    fun drawOnSmallFullBoardWithNoLineOfFour() {
        // 4x4 board, win=4. A pattern that fills the board with no four-in-a-row.
        val board = listOf(
            listOf(1, 2, 1, 2),
            listOf(1, 2, 1, 2),
            listOf(2, 1, 2, 1),
            listOf(2, 1, 2, 1),
        )
        assertTrue(isDraw(board))
        assertNull(checkWin(board, 4))
    }

    @Test
    fun cannotMoveAfterAWin() {
        var state = newGame(7, 4)
        listOf(0, 6, 1, 6, 2, 6, 3).forEach { state = applyMove(state, it) }
        val frozen = state
        val after = applyMove(state, 4)
        assertEquals(frozen, after)
    }
}
