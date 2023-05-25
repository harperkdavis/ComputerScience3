package games.othello;

// Author: Andrew Merrill

import main.*;

class OthelloAction implements Action {
    final int row, col;
    final Side side;

    OthelloAction(int row, int col, Side side) {
        this.row = row;
        this.col = col;
        this.side = side;
    }

    public boolean equals(Object o) {
        OthelloAction other = (OthelloAction) o;
        return (row == other.row && col == other.col && side == other.side);
    }

    public String toString() {
        return "player " + side + " moves to (" + row + "," + col + ")";
    }
}