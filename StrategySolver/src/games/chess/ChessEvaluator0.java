package games.chess;

// Author: Andrew Merrill

import main.Evaluator;
import main.Side;
import main.State;

public class ChessEvaluator0 implements Evaluator {

    // You may want to use these functions from ChessState:
    //    boolean isGameOver()
    //    Side getWinner()     // only call this when isGameOver() returns True
    //    ChessPiece getPiece(int row, int col)
    //    boolean inCheck(Side side)
    //    boolean attackChecker(Side targetSide, int targetRow, int targetCol)
    //    int getKingRow(Side side)
    //    int getKingCol(Side side)
    //    Side getSideToPlay()
    //    Side getOtherSide()


    @Override
    public int evaluate(State state) {
        ChessState board = (ChessState) state;
        if (board.isGameOver()) {
            Side winningSide = board.getWinner();
            if (winningSide == ChessState.WHITE) {
                return 10_000_000;
            } else if (winningSide == ChessState.BLACK) {
                return -10_000_000;
            }
        }
        return 0;
    }

    public String toString() {
        return "C-0";
    }
}
