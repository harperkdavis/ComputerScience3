package games.chess;

// Author: Andrew Merrill

import main.Evaluator;
import main.Side;
import main.State;

public class HarperChessEvaluator1 implements Evaluator {

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

        int whiteCentipawns = 0;
        int blackCentipawns = 0;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ChessPiece piece = board.getPiece(i, j);
                if (piece == null) continue;
                
                int value = 0;
                if (piece.isPawn()) {
                    value = 100;
                } else if (piece.isBishop()) {
                    value = 300;
                } else if (piece.isKnight()) {
                    value = 300;
                } else if (piece.isRook()) {
                    value = 500;
                } else if (piece.isQueen()) {
                    value = 900;
                }

                boolean hanging = !board.attackChecker(piece.side, i, j);

                value += hanging ? - value / 2 : 10;
                if (piece.side == Side.ONE) {
                    whiteCentipawns += value;
                } else {
                    blackCentipawns += value;
                }
            }
        }

        return whiteCentipawns - blackCentipawns;        
    }

    public String toString() {
        return "C-HD-1";
    }
}
