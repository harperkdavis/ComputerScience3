package games.chess;

// Author: Andrew Merrill

import main.Action;
import main.Side;

public class ChessAction implements Action {
    final byte oldrow, oldcol;  // the starting location of the piece that is moving
    final byte newrow, newcol;  // the ending location of the piece that is moving
    final Side side;            // the side (WHITE or BLACK) whose piece is moving

    ChessAction(int oldrow, int oldcol, int newrow, int newcol, Side side) {
        this.oldrow = (byte) oldrow;
        this.oldcol = (byte) oldcol;
        this.newrow = (byte) newrow;
        this.newcol = (byte) newcol;
        this.side = side;
    }

    public boolean equals(Object o) {
        ChessAction other = (ChessAction) o;
        return (other != null &&
                oldrow == other.oldrow && oldcol == other.oldcol &&
                newrow == other.newrow && newcol == other.newcol &&
                side == other.side);
    }

    public String toString() {
        return String.format("player %s moves from %d,%d to %d,%d", side, oldrow, oldcol, newrow, newcol);
    }
}
