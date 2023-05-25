package games.connect4;

import main.*;

public class HarperConnect4Evaluator2 implements Evaluator {

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
            return 10000000;
        else if (board.redWon())
            return -10000000;

        int blackTwos = Connect4Utility.countRuns(board, 2, 2, 0, 0);
        int blackThrees = Connect4Utility.countRuns(board, 3, 3, 0, 0);

        int redTwos = Connect4Utility.countRuns(board, 2, 0, 2, 0);
        int redThrees = Connect4Utility.countRuns(board, 3, 0, 3, 0);

        int structure = blackTwos + blackThrees * 10 - redTwos - redThrees * 10;
        
        int blackTwosOpen = Connect4Utility.countRuns(board, 3, 2, 0, 1);
        int blackThreesOpen = Connect4Utility.countRuns(board, 4, 3, 0, 1);
        
        int redTwosOpen = Connect4Utility.countRuns(board, 3, 0, 2, 1);
        int redThreesOpen = Connect4Utility.countRuns(board, 4, 0, 3, 1);

        int blackThreesDoubleOpen = Connect4Utility.countRuns(board, 5, 3, 0, 2);
        int redThreesDoubleOpen = Connect4Utility.countRuns(board, 5, 0, 3, 2);
        
        int opportunity = blackTwosOpen + blackThreesOpen * 10 + blackThreesDoubleOpen * 4 - redTwosOpen - redThreesOpen * 10 - redThreesDoubleOpen * 4;

        return structure * 10 + opportunity * 100;
    }

    public String toString() {
        return "C4-HD-2";
    }

}
