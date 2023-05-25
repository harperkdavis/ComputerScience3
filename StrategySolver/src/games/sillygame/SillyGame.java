package games.sillygame;

// Author: Andrew Merrill

import graphics.GamePanel;
import main.*;

public class SillyGame implements Game {
    private final GamePanel gamePanel;
    private final Evaluator[] evaluators = new Evaluator[]{
            new SillyEvaluator0()
    };

    public SillyGame() {
        gamePanel = new SillyPanel();
    }

    public String toString() {
        return "Silly";
    }

    public String getNameForSide(Side side) {
        if (side == Side.ONE) return "Red";
        if (side == Side.TWO) return "Blue";
        throw new IllegalStateException("No name for side " + side);
    }


    public State getInitialState() {
        return new SillyState();
    }

    public GamePanel getPanel() {
        return gamePanel;
    }

    public Evaluator[] getEvaluators() {
        return evaluators;
    }
}