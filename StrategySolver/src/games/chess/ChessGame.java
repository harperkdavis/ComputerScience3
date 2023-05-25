package games.chess;

// Author: Andrew Merrill

import graphics.GamePanel;
import main.Evaluator;
import main.Game;
import main.Side;
import main.State;

public class ChessGame implements Game {

    private final GamePanel gamePanel;

    private final Evaluator[] evaluators = new Evaluator[]{
             new ChessEvaluator0(),
             new HarperChessEvaluator1()
    };

    public ChessGame() {
        gamePanel = new ChessPanel();
    }

    @Override
    public String toString() {
        return "Chess";
    }

    @Override
    public String getNameForSide(Side side) {
        if (side == Side.ONE) return "White";
        if (side == Side.TWO) return "Black";
        throw new IllegalStateException("No name for side " + side);
    }

    @Override
    public String getNameForTie() {
        return "draw";
    }

    @Override
    public State getInitialState() {
        return new ChessState();
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
