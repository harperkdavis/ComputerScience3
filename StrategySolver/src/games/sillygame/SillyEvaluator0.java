package games.sillygame;

// Author: Andrew Merrill

import main.*;
public class SillyEvaluator0 implements Evaluator {

    @Override
    public int evaluate(State state) {
        SillyState board = (SillyState) state;
        return 0;
    }

    public String toString() {
        return "Silly 0";
    }

}