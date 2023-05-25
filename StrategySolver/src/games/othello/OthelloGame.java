package games.othello;

// Author: Andrew Merrill

import graphics.GamePanel;
import main.*;

public class OthelloGame implements Game {
    private final GamePanel gamePanel;
    private final Evaluator[] evaluators = new Evaluator[]{
             new OthelloEvaluator0(),
             new HarperOthelloEvaluator1()
    };

    public OthelloGame() {
        gamePanel = new OthelloPanel();
    }

    public String toString() {
        return "Othello";
    }

    public String getNameForSide(Side side) {
        if (side == Side.ONE) return "Black";
        if (side == Side.TWO) return "White";
        throw new IllegalStateException("No name for side " + side);
    }


    public State getInitialState() {
        return new OthelloState();
    }

    public GamePanel getPanel() {
        return gamePanel;
    }

    public Evaluator[] getEvaluators() {
        return evaluators;
    }
}