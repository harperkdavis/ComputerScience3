package games.checkers;

// Author: Andrew Merrill

import main.Evaluator;
import main.Side;
import main.State;

public class CheckersEvaluator0 implements Evaluator {

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
                return 1000000;
            } else if (winningSide == CheckersState.RED) {
                return -1000000;
            }
        }
        return 0;
    }

    public String toString() {
        return "C-0";
    }
}
