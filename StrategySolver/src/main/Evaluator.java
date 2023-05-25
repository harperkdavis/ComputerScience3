package main;

// Author: Andrew Merrill

public interface Evaluator {
    /**
     * Computes the heuristic evaluation of a state,
     * from the point of view of the player whose Side is ONE.
     *
     * This function should return a higher (positive) number if the state is
     * favorable to Side ONE, and a lower (negative) number
     * if the state is favorable to Side TWO.
     */
    public int evaluate(State state);
}