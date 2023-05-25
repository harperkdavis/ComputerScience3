package games.connect4;

// Author: Andrew Merrill

import graphics.GamePanel;
import main.*;

public class Connect4Game implements Game {
    private final GamePanel gamePanel;

    private final Evaluator[] evaluators = new Evaluator[]{
             new Connect4Evaluator0(),
             new HarperConnect4Evaluator1(),
             new HarperConnect4Evaluator2()
    };

    public Connect4Game() {
        gamePanel = new Connect4Panel();
    }

    public String toString() {
        return "Connect4";
    }

    public String getNameForSide(Side side) {
        if (side == Side.ONE) return "Black";
        if (side == Side.TWO) return "Red";
        throw new IllegalStateException("No name for side " + side);
    }


    public State getInitialState() {
        return new Connect4State();
    }

    public GamePanel getPanel() {
        return gamePanel;
    }

    public Evaluator[] getEvaluators() {
        return evaluators;
    }
}