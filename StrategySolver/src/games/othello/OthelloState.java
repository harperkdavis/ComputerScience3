package games.othello;

// Author: Andrew Merrill

import main.*;
import java.util.*;

public class OthelloState extends State {
    static final int SIZE = 8;  // only works for 4, 6, or 8
    static final Side EMPTY = null;
    static final Side BLACK = Side.ONE;  // Side.ONE is Max Side
    static final Side WHITE = Side.TWO;  // Side.TWO is Min Side
    static final Side TIE = null;

    private long black, white; // bitmasks: 1 for placed pieces, 0 for blank cells

    final private byte numPassedMoves;

    public OthelloState() {
        super(BLACK);
        black = 0;
        white = 0;
        numPassedMoves = 0;
        if (SIZE == 8) {
            setCell(3, 3, WHITE);
            setCell(4, 4, WHITE);
            setCell(3, 4, BLACK);
            setCell(4, 3, BLACK);
            //black = 34628173824L;
            //white = 68853694464L;
        } else if (SIZE == 6) {
            setCell(2, 2, WHITE);
            setCell(3, 3, WHITE);
            setCell(2, 3, BLACK);
            setCell(3, 2, BLACK);
            //black = 1081344L;
            //white = 2113536L;
        } else if (SIZE == 4) {
            setCell(1, 1, WHITE);
            setCell(2, 2, WHITE);
            setCell(1, 2, BLACK);
            setCell(2, 1, BLACK);
            //black = 576L;
            //white = 1056L;
        }
    }

    private OthelloState(Side newSideToPlay, int newNumPassedMoves, OthelloState oldBoard) {
        super(newSideToPlay);
        this.black = oldBoard.black;
        this.white = oldBoard.white;
        this.numPassedMoves = (byte) newNumPassedMoves;
    }

    public boolean equals(Object other) {
        OthelloState otherState = (OthelloState) other;
        return (black == otherState.black && white == otherState.white &&
                numPassedMoves == otherState.numPassedMoves);
    }


    public boolean isGameOver() {
        if (numPassedMoves >= 2) return true;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (getCell(r, c) == EMPTY) return false;
            }
        }
        return true;
    }

    public Side getWinner()  // assumes that the game is over
    {
        int blackcount = countBlackPieces();
        int whitecount = countWhitePieces();
        if (blackcount > whitecount) {
            return BLACK;
        } else if (whitecount > blackcount) {
            return WHITE;
        } else {
            return TIE;
        }
    }

    private OthelloState newBoard;

    public List<SearchNode> listChildren() {
        ArrayList<SearchNode> moves = new ArrayList<SearchNode>();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (getCell(r, c) == EMPTY) {
                    newBoard = null;
                    checkForFlips(r, c, 0, 1);
                    checkForFlips(r, c, -1, 1);
                    checkForFlips(r, c, -1, 0);
                    checkForFlips(r, c, -1, -1);
                    checkForFlips(r, c, 0, -1);
                    checkForFlips(r, c, 1, -1);
                    checkForFlips(r, c, 1, 0);
                    checkForFlips(r, c, 1, 1);
                    if (newBoard != null) {
                        newBoard.setCell(r, c, getSideToPlay());
                        moves.add(new SearchNode(new OthelloAction(r, c, getSideToPlay()), newBoard));
                    }
                }
            }
        }
        if (moves.isEmpty()) {
            moves.add(new SearchNode(null, new OthelloState(getOtherSide(), numPassedMoves + 1, this)));
        }
        return moves;
    }

    private void checkForFlips(int row, int col, int rowdir, int coldir) {
        int r = row + rowdir;
        int c = col + coldir;
        int flips = 0;
        while (r >= 0 && c >= 0 && r < SIZE && c < SIZE) {
            Side cell = getCell(r, c);
            if (cell == EMPTY)
                return;
            else if (cell == getSideToPlay()) {
                if (flips == 0)
                    return;
                if (newBoard == null)
                    newBoard = new OthelloState(getOtherSide(), 0, this);
                do {
                    r -= rowdir;
                    c -= coldir;
                    newBoard.setCell(r, c, getSideToPlay());
                    flips--;
                } while (flips > 0);
                return;
            } else {
                r += rowdir;
                c += coldir;
                flips++;
            }
        }
    }


    int countBlackPieces() {
        int count = 0;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (isBlackCell(r, c)) count++;
            }
        }
        return count;
    }

    int countWhitePieces() {
        int count = 0;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (isWhiteCell(r, c)) count++;
            }
        }
        return count;
    }

    private void setCell(int row, int col, Side color) {
        if (color == BLACK) {
            black |= (1L << (row * SIZE + col));
            white &= ~(1L << (row * SIZE + col));
        } else {
            white |= (1L << (row * SIZE + col));
            black &= ~(1L << (row * SIZE + col));
        }
    }

    boolean isBlackCell(int row, int col) {
        return ((black & (1L << (row * SIZE + col))) != 0);
    }

    boolean isWhiteCell(int row, int col) {
        return ((white & (1L << (row * SIZE + col))) != 0);
    }

    Side getCell(int row, int col) {
        if (isBlackCell(row, col))
            return BLACK;
        else if (isWhiteCell(row, col))
            return WHITE;
        else
            return EMPTY;
    }

    private void display() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Side cell = getCell(r, c);
                if (cell == EMPTY)
                    System.out.print(".");
                else if (cell == BLACK)
                    System.out.print("X");
                else if (cell == WHITE)
                    System.out.print("O");
            }
            System.out.println();
        }
    }

}