package main;

// Author: Andrew Merrill

import graphics.GamePanel;

public interface Game {
    public State getInitialState();

    public GamePanel getPanel();

    public Evaluator[] getEvaluators();

    public String toString();

    public String getNameForSide(Side side);

    public default String getNameForTie() {
        return "tie";
    }

}