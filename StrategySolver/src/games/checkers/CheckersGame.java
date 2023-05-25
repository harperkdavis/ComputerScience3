package games.checkers;

// Author: Andrew Merrill

import graphics.GamePanel;
import main.Evaluator;
import main.Game;
import main.Side;
import main.State;

public class CheckersGame implements Game {

    private final GamePanel gamePanel;

    private final Evaluator[] evaluators = new Evaluator[]{
             new CheckersEvaluator0(),
             new HarperCheckersEvaluator1()
    };

    public CheckersGame() {
        gamePanel = new CheckersPanel();
    }

    @Override
    public String toString() {
        return "Checkers";
    }

    @Override
    public String getNameForSide(Side side) {
        if (side == Side.ONE) return "Black";
        if (side == Side.TWO) return "Red";
        throw new IllegalStateException("No name for side " + side);
    }

    @Override
    public State getInitialState() {
        return new CheckersState();
    }

    @Override
    public GamePanel getPanel() {
        return gamePanel;
    }

    @Override
    public Evaluator[] getEvaluators() {
        return evaluators;
    }
}
