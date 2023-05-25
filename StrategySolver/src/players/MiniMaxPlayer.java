package players;

import java.util.ArrayList;
import java.util.List;

import main.*;

public class MiniMaxPlayer extends Player {
    final Evaluator evaluator;
    final int maxDepth;

    public MiniMaxPlayer(int maxDepth, Evaluator evaluator) {
        this.evaluator = evaluator;
        this.maxDepth = maxDepth;
    }

    public SearchNode pickMove(State currentState) {
        int side = currentState.getSideToPlay().evalSign();
        List<SearchNode> bestNodes = new ArrayList<>();

        // System.out.println("---");

        if (side == 1) {
            int best = Integer.MIN_VALUE;
            for (SearchNode child : currentState.listChildren()) {
                int eval = minChildEval(child.state, 0);
                // System.out.println(child.action.toString() + " -- eval -- " + eval);
                if (eval >= best) {
                    if (eval > best) {
                        bestNodes.clear();
                    }
                    best = eval;
                    bestNodes.add(child);
                }
            }
        } else {
            int best = Integer.MAX_VALUE;
            for (SearchNode child : currentState.listChildren()) {
                int eval = maxChildEval(child.state, 0);
                // System.out.println(child.action.toString() + " -- eval -- " + eval);
                if (eval <= best) {
                    if (eval < best) {
                        bestNodes.clear();
                    }
                    best = eval;
                    bestNodes.add(child);
                }
            }
        }
        
        return bestNodes.get(MyRandom.nextIntInRange(0, bestNodes.size() - 1));
    }

    private int maxChildEval(State state, int depth) {
        if (depth > maxDepth || state.isGameOver()) {
            int eval = evaluator.evaluate(state);
            return state.isGameOver() ? eval - Integer.signum(eval) * depth : eval;
        }
        int best = Integer.MIN_VALUE;
        for (SearchNode child : state.listChildren()) {
            int eval = minChildEval(child.state, depth + 1);
            best = Integer.max(best, eval);
        }
        return best;
    }

    private int minChildEval(State state, int depth) {
        if (depth > maxDepth || state.isGameOver()) {
            int eval = evaluator.evaluate(state);
            return state.isGameOver() ? eval - Integer.signum(eval) * depth : eval;
        }
        int best = Integer.MAX_VALUE;
        for (SearchNode child : state.listChildren()) {
            int eval = maxChildEval(child.state, depth + 1);
            best = Integer.min(best, eval);
        }
        return best;
    }

    public String toString() {
        return "MM:" + maxDepth + " " + evaluator;
    }
}
