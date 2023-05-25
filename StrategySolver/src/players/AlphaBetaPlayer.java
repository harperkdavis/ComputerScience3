package players;

import java.util.ArrayList;
import java.util.List;

import main.*;

public class AlphaBetaPlayer extends Player {
    final Evaluator evaluator;
    final int maxDepth;

    public AlphaBetaPlayer(int maxDepth, Evaluator evaluator) {
        this.evaluator = evaluator;
        this.maxDepth = maxDepth;
    }

    public SearchNode pickMove(State currentState) {
        int side = currentState.getSideToPlay().evalSign();
        List<SearchNode> bestNodes = new ArrayList<>();

        System.out.println("---");

        if (side == 1) {
            int best = Integer.MIN_VALUE;
            for (SearchNode child : currentState.listChildren()) {
                int eval = minChildEval(child.state, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
                System.out.println(child.action.toString() + " -- eval -- " + eval);
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
                int eval = maxChildEval(child.state, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
                System.out.println(child.action.toString() + " -- eval -- " + eval);
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

    private int maxChildEval(State state, int depth, int alpha, int beta) {
        if (depth > maxDepth || state.isGameOver()) {
            int eval = evaluator.evaluate(state);
            return state.isGameOver() ? eval - Integer.signum(eval) * depth : eval;
        }
        int best = Integer.MIN_VALUE;
        int newAlpha = alpha;
        for (SearchNode child : state.listChildren()) {
            int eval = minChildEval(child.state, depth + 1, newAlpha, beta);
            best = Integer.max(best, eval);
            if (eval >= beta) {
                break;
            }
            if (eval > newAlpha) {
                newAlpha = eval;
            }  
        }
        return best;
    }

    private int minChildEval(State state, int depth, int alpha, int beta) {
        if (depth > maxDepth || state.isGameOver()) {
            int eval = evaluator.evaluate(state);
            return state.isGameOver() ? eval - Integer.signum(eval) * depth : eval;
        }
        int best = Integer.MAX_VALUE;
        int newBeta = beta;
        for (SearchNode child : state.listChildren()) {
            int eval = maxChildEval(child.state, depth + 1, alpha, newBeta);
            best = Integer.min(best, eval);
            if (eval <= alpha) {
                break;
            }
            if (eval < newBeta) {
                newBeta = eval;
            }
        }
        return best;
    }

    public String toString() {
        return "AB" + maxDepth + ":" + evaluator;
    }
}
