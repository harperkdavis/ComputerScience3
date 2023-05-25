package main;

// Author: Andrew Merrill

// Side ONE is the Maxi side (high heuristic evaluations when they are ahead)
// Side TWO is the Mini side (low heuristic evaluations when they are ahead)

public enum Side {
    ONE(1), TWO(-1);

    private final int evalSign;

    private Side(final int sign) {
        evalSign = sign;
    }

    public Side otherSide() {
        if (this == ONE)
            return TWO;
        else
            return ONE;
    }

    public int evalSign() {
        return evalSign;
    }

}