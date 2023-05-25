package main;

// Author: Andrew Merrill

import java.util.*;

public abstract class State {
    private final Side sideToPlay;

    public State(Side sideToPlay) {
        this.sideToPlay = sideToPlay;
    }

    /////////////////////////////////////////////////////////////////////////////

    public abstract List<SearchNode> listChildren();

    // returns true if the game is over
    public abstract boolean isGameOver();

    // returns main.Side that won, or null if it is a tie
    // this function will only be called when isGameOver() is true
    public abstract Side getWinner();

    ///////////////////////////////////////////////////////////////////////////

    public Side getSideToPlay() {
        return sideToPlay;
    }

    public Side getOtherSide() {
        return sideToPlay.otherSide();
    }

}
