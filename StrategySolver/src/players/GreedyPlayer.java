package players;

import java.util.ArrayList;
import java.util.List;

import main.*;

public class GreedyPlayer extends Player {
    final Evaluator evaluator;

    public GreedyPlayer(Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    public SearchNode pickMove(State currentState) {
        // Use the given Evaluator to select the child with the best evaluation
        // You may want to use:
        //    List<SearchNode> listChildren() from the State class
        //    Side getSide() from the Player class
        //    int evalSign() from the Side class

        int sign = currentState.getSideToPlay().evalSign();
        int best = -sign * Integer.MAX_VALUE;
        List<SearchNode> bestNodes = new ArrayList<>();

        for (SearchNode child : currentState.listChildren()) {
            int eval = evaluator.evaluate(child.state);
            if (sign == 1) {
                if (eval >= best) {
                    if (eval > best) {
                        bestNodes.clear();
                    }
                    best = eval;
                    bestNodes.add(child);
                }
            } else {
                if (eval <= best) {
                    if (eval < best) {
                        bestNodes.clear();
                    }
                    best = eval;
                    bestNodes.add(child);
                }
            }
        }

        System.out.println(best);
        SearchNode pick = bestNodes.get(MyRandom.nextIntInRange(0, bestNodes.size() - 1));
        return new SearchNode(pick.action, pick.state);
    }


    public String toString() {
        return "Greedy:" + evaluator.toString();
    }
}