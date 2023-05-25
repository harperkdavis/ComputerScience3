package games.connect4;

// Author: Andrew Merrill

import main.*;
import java.util.*;

public class Connect4State extends State {
    static final int ROWS = 6;
    static final int COLS = 7;
    static final Side EMPTY = null;
    static final Side BLACK = Side.ONE;  // Side.ONE is Max Side
    static final Side RED = Side.TWO;    // Side.TWO is Min Side
    static final Side TIE = null;

    private long black, red;  // bitmasks: 1 for placed pieces, 0 for blank cells

    public Connect4State() {
        super(BLACK);
        black = 0;
        red = 0;
    }

    private Connect4State(Side newSideToPlay, Connect4State oldBoard) {
        super(newSideToPlay);
        this.black = oldBoard.black;
        this.red = oldBoard.red;
    }


    public boolean equals(Object other) {
        Connect4State otherState = (Connect4State) other;
        return (black == otherState.black && red == otherState.red);
    }

    public boolean isGameOver() {
        if (blackWon() || redWon()) return true;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (getCell(r, c) == EMPTY) return false;
            }
        }
        return true;
    }

    public Side getWinner() {
        if (blackWon()) {
            return BLACK;
        } else if (redWon()) {
            return RED;
        } else {
            return TIE;
        }
    }

    boolean blackWon() {
        return (Connect4Utility.countRuns(this, 4, 4, 0, 0)) > 0;
    }

    boolean redWon() {
        return (Connect4Utility.countRuns(this, 4, 0, 4, 0)) > 0;
    }


    public List<SearchNode> listChildren() {
        ArrayList<SearchNode> moves = new ArrayList<SearchNode>();
        for (int c = 0; c < COLS; c++) {
            int r;
            for (r = 0; r < ROWS; r++) {
                if (getCell(r, c) != EMPTY)
                    break;
            }
            if (r != 0) {
                Connect4State newState = new Connect4State(getOtherSide(), this);
                newState.setCell(r - 1, c, getSideToPlay());
                moves.add(new SearchNode(new Connect4Action(c, getSideToPlay()), newState));
            }
        }
        return moves;
    }

    private void display() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Side cell = getCell(r, c);
                if (cell == EMPTY)
                    System.out.print(".");
                else if (cell == BLACK)
                    System.out.print("X");
                else if (cell == RED)
                    System.out.print("O");
            }
            System.out.println();
        }
    }


    private void setCell(int row, int col, Side color) {
        if (color == BLACK) {
            black |= (1L << (row * COLS + col));
            red &= ~(1L << (row * COLS + col));
        } else {
            red |= (1L << (row * COLS + col));
            black &= ~(1L << (row * COLS + col));
        }
    }

    boolean isBlackCell(int row, int col) {
        return ((black & (1L << (row * COLS + col))) != 0);
    }

    boolean isRedCell(int row, int col) {
        return ((red & (1L << (row * COLS + col))) != 0);
    }

    Side getCell(int row, int col) {
        if (isBlackCell(row, col))
            return BLACK;
        else if (isRedCell(row, col))
            return RED;
        else
            return EMPTY;
    }


}