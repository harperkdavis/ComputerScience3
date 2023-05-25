package games.sillygame;

// Author: Andrew Merrill

import main.*;
class SillyAction implements Action {
    final int oldr, oldc, newr, newc;
    final Side side;

    SillyAction(int oldr, int oldc, int newr, int newc, Side side) {
        this.oldr = oldr;
        this.oldc = oldc;
        this.newr = newr;
        this.newc = newc;
        this.side = side;
    }

    public boolean equals(Object o) {
        SillyAction other = (SillyAction) o;
        return (other != null && oldr == other.oldr && oldc == other.oldc && newr == other.newr && newc == other.newc && side == other.side);
    }

    public String toString() {
        return String.format("player %s moves from (%d,%d) to (%d,%d)", side, oldr, oldc, newr, newc);
    }

}