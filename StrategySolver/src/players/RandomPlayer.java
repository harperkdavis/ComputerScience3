package players;

// Author: Andrew Merrill

import main.*;

import java.util.List;

public class RandomPlayer extends Player {

    public SearchNode pickMove(State currentState) {
        List<SearchNode> children = currentState.listChildren();
        if (children.isEmpty())
            return new SearchNode(null, null);
        else
            return children.get(MyRandom.random.nextInt(children.size()));
    }

    public String toString() {
        return "Random";
    }
}