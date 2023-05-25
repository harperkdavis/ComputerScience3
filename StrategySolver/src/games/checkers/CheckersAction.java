package games.checkers;

// Author: Andrew Merrill

import main.Action;
import main.Side;

public class CheckersAction implements Action {
    final int oldindex, newindex;
    final int oldrow, oldcol, newrow, newcol;
    final Side side;
    final CheckersAction nextAction;
    CheckersAction prevAction = null;

    CheckersAction(int oldindex, int newindex, Side side) {
        this(oldindex, newindex, side, null);
    }
    CheckersAction(int oldindex, int newindex, Side side, CheckersAction nextAction) {
        this.oldindex = oldindex;
        this.newindex = newindex;
        this.side = side;
        this.nextAction = nextAction;

        oldrow = oldindex / 4;
        oldcol = (oldindex % 4) * 2 + (oldrow % 2);

        newrow = newindex / 4;
        newcol = (newindex % 4) * 2 + (newrow % 2);
    }

    public boolean equals(Object o) {
        CheckersAction other = (CheckersAction) o;
        if (other != null && oldindex == other.oldindex && newindex == other.newindex && side == other.side) {
            if (nextAction == null && other.nextAction == null)
                return true;
            if (nextAction != null && other.nextAction != null && nextAction.equals(other.nextAction))
                return true;
        }
        return false;
    }

    public String toString() {
        return String.format("player %s moves from %d to %d", side, oldindex, newindex);
    }
}
