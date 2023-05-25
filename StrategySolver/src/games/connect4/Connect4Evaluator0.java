package games.connect4;

// Author: Andrew Merrill

import main.*;

public class Connect4Evaluator0 implements Evaluator {

    // You may want to use these functions from Connect4State:
    //    boolean blackWon()
    //    boolean redWon()
    //    Side getCell(int row, int col)
    //    boolean isBlackCell(int row, int col)
    //    boolean isRedCell(int row, int col)
    //    Side getSideToPlay()
    //    Side getOtherSide()

    // You may also want to use this function from Connect4Utility:
    //    static int countRuns(Connect4State state, int length, int black, int red, int empty)


        @Override
    public int evaluate(State state) {
        Connect4State board = (Connect4State) state;
        if (board.blackWon())
            return 1000000;
        else if (board.redWon())
            return -1000000;
        else
            return 0;
    }

    public String toString() {
        return "C4-0";
    }

}
