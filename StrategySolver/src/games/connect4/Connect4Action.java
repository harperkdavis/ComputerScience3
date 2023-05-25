package games.connect4;

// Author: Andrew Merrill

import main.*;

class Connect4Action implements Action {
    final int col;
    final Side side;

    Connect4Action(int col, Side side) {
        this.col = col;
        this.side = side;
    }

    public boolean equals(Object o) {
        Connect4Action other = (Connect4Action) o;
        return (col == other.col && side == other.side);
    }

    public String toString() {
        return "player " + side + " moves to (" + col + ")";
    }

}