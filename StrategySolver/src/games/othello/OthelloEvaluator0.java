package games.othello;

// Author: Andrew Merrill

import main.*;

public class OthelloEvaluator0 implements Evaluator {

    // You may want to use these functions from OthelloState:
    //    boolean isGameOver()
    //    Side getWinner()   // only call this when isGameOver() returns True
    //    int countBlackPieces()
    //    int countWhitePieces()
    //    Side getCell(int row, int col)
    //    boolean isBlackCell(int row, int col)
    //    boolean isWhiteCell(int row, int col)
    //    Side getSideToPlay()
    //    Side getOtherSide()


    @Override
    public int evaluate(State state) {
        OthelloState board = (OthelloState) state;
        return board.countBlackPieces() - board.countWhitePieces();
    }

    public String toString() {
        return "O-0";
    }

}