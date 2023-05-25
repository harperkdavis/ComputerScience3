package main;

// Author: Andrew Merrill

public class SearchNode {
    public final Action action;  // this is the action used to get to this node
    public final State state;    // this is the state produced by that action
    public int eval;             // this is an optional field that sometimes contains a heuristic value for the state

    public SearchNode(Action action, State state) {
        this.state = state;
        this.action = action;
    }

    public SearchNode(Action action, State state, int eval) {
        this.state = state;
        this.action = action;
        this.eval = eval;
    }

}