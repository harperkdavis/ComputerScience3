package games.checkers;

// Author: Andrew Merrill

import main.Evaluator;
import main.Side;
import main.State;

public class HarperCheckersEvaluator1 implements Evaluator {

    // You may want to use these functions from CheckersState:
    //    boolean isGameOver()
    //    Side getWinner()     // only call this when isGameOver() returns True
    //    boolean isBlackCell(int row, int col)
    //    boolean isRedCell(int row, int col)
    //    boolean isBlackKing(int row, int col)
    //    boolean isRedKing(int row, int col)
    //    boolean isEmptyCell(int row, int col)
    //    Side getSideToPlay()
    //    Side getOtherSide()


    @Override
    public int evaluate(State state) {
        CheckersState board = (CheckersState) state;

        if (board.isGameOver()) {
            Side winningSide = board.getWinner();
            if (winningSide == CheckersState.BLACK) {
                return 10000000;
            } else if (winningSide == CheckersState.RED) {
                return -10000000;
            }
        }

        int blackPieceCount = 0;
        int blackKingCount = 0;

        int redPieceCount = 0;
        int redKingCount = 0;

        for (int i = 0; i < 8; i ++) {
            for (int j = 0; j < 8; j ++) {
                blackPieceCount += board.isBlackCell(i, j) ? 1 : 0;
                blackKingCount += board.isBlackKing(i, j) ? 1 : 0;

                redPieceCount += board.isRedCell(i, j) ? 1 : 0;
                redKingCount += board.isRedKing(i, j) ? 1 : 0;
            }
        }

        return blackPieceCount + blackKingCount * 2 - redPieceCount - redKingCount * 2;
    }

    public String toString() {
        return "C-HD-1";
    }
}
