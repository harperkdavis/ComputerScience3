package games.sillygame;

// Author: Andrew Merrill

import main.*;
import java.util.*;

public class SillyState extends State {
    final static int ROWS = 2;
    final static int COLS = 6;
    final static Side EMPTY = null;
    final static Side RED = Side.ONE;
    final static Side BLUE = Side.TWO;
    final static Side TIE = null;

    private long red, blue;

    public SillyState() {
        super(RED);
        red = 0;
        blue = 0;
        setCell(0, 0, BLUE);
        setCell(ROWS - 1, COLS - 1, RED);
    }


    private SillyState(Side newSideToPlay, SillyState oldBoard) {
        super(newSideToPlay);
        this.blue = oldBoard.blue;
        this.red = oldBoard.red;
    }


    public boolean equals(Object other) {
        SillyState otherState = (SillyState) other;
        return (blue == otherState.blue && red == otherState.red);
    }

    public boolean isGameOver() {
        if (blueWon() || redWon())
            return true;
        else
            return false;
    }

    public Side getWinner() {
        if (blueWon()) {
            return BLUE;
        } else if (redWon()) {
            return RED;
        } else {
            return TIE;
        }
    }

    boolean blueWon() {
        for (int r = 0; r < ROWS; r++) {
            if (getCell(r, COLS - 1) == BLUE) return true;
        }
        return false;
    }

    boolean redWon() {
        for (int r = 0; r < ROWS; r++) {
            if (getCell(r, 0) == RED) return true;
        }
        return false;
    }


    public List<SearchNode> listChildren() {
        List<SearchNode> moves = new ArrayList<SearchNode>();
        for (int c = 0; c < COLS; c++) {
            for (int r = 0; r < ROWS; r++) {
                if (getCell(r, c) == getSideToPlay()) {
                    addIfLegal(moves, r, c, r + 1, c);
                    addIfLegal(moves, r, c, r - 1, c);
                    addIfLegal(moves, r, c, r, c + 1);
                    addIfLegal(moves, r, c, r, c - 1);
                }
            }
        }
        return moves;
    }

    private void addIfLegal(List<SearchNode> moves, int oldr, int oldc, int newr, int newc) {
        if (newr < 0 || newc < 0 || newr >= ROWS || newc >= COLS) return;
        if (getCell(newr, newc) != EMPTY) return;
        SillyState newState = new SillyState(getOtherSide(), this);
        newState.setCell(newr, newc, getCell(oldr, oldc));
        newState.setCell(oldr, oldc, EMPTY);
        moves.add(new SearchNode(new SillyAction(oldr, oldc, newr, newc, getSideToPlay()), newState));
    }

    private void setCell(int row, int col, Side color) {
        if (color == BLUE) {
            blue |= (1L << (row * COLS + col));
            red &= ~(1L << (row * COLS + col));
        } else if (color == RED) {
            red |= (1L << (row * COLS + col));
            blue &= ~(1L << (row * COLS + col));
        } else {
            red &= ~(1L << (row * COLS + col));
            blue &= ~(1L << (row * COLS + col));
        }
    }

    boolean isBlueCell(int row, int col) {
        return ((blue & (1L << (row * COLS + col))) != 0);
    }

    boolean isRedCell(int row, int col) {
        return ((red & (1L << (row * COLS + col))) != 0);
    }

    Side getCell(int row, int col) {
        if (isBlueCell(row, col))
            return BLUE;
        else if (isRedCell(row, col))
            return RED;
        else
            return EMPTY;
    }


}