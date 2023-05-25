package games.othello;

import main.Evaluator;
import main.State;

public class HarperOthelloEvaluator1 implements Evaluator {

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

        int blackPieces = board.countBlackPieces();
        int whitePieces = board.countWhitePieces();

        int blackEdges = 0;
        int blackCorners = 0;
        int whiteEdges = 0;
        int whiteCorners = 0;

        for (int i = 0; i < 7; i ++) {
            blackEdges += board.isBlackCell(0, i) ? 1 : 0;
            whiteEdges += board.isWhiteCell(0, i) ? 1 : 0;

            blackEdges += board.isBlackCell(i, 7) ? 1 : 0;
            whiteEdges += board.isWhiteCell(i, 7) ? 1 : 0;

            blackEdges += board.isBlackCell(7, 7 - i) ? 1 : 0;
            whiteEdges += board.isWhiteCell(7, 7 - i) ? 1 : 0;

            blackEdges += board.isBlackCell(7 - i, 0) ? 1 : 0;
            whiteEdges += board.isWhiteCell(7 - i, 0) ? 1 : 0;
        }

        blackCorners += board.isBlackCell(0, 0) ? 1 : 0;
        blackCorners += board.isBlackCell(0, 7) ? 1 : 0;
        blackCorners += board.isBlackCell(7, 7) ? 1 : 0;
        blackCorners += board.isBlackCell(7, 0) ? 1 : 0;

        whiteCorners += board.isWhiteCell(0, 0) ? 1 : 0;
        whiteCorners += board.isWhiteCell(0, 7) ? 1 : 0;
        whiteCorners += board.isWhiteCell(7, 7) ? 1 : 0;
        whiteCorners += board.isWhiteCell(7, 0) ? 1 : 0;

        return blackPieces + blackEdges * 2 + blackCorners * 10 - whitePieces - whiteEdges * 2 - whiteCorners * 10;
    }

    public String toString() {
        return "O-HD-1";
    }

}
