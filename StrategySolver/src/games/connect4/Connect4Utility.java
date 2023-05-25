package games.connect4;

// Author: Andrew Merrill

class Connect4Utility {

    // returns the number of connected runs that have the given number of pieces
    //  of each type.  The inputs should always add to length.
    static int countRuns(Connect4State state, int length, int black, int red, int empty) {
        int count = 0;
        // horizontal
        for (int r = 0; r < Connect4State.ROWS; r++)
            for (int c = 0; c <= Connect4State.COLS - length; c++)
                if (checkRun(state, length, r, c, 0, 1, black, red, empty))
                    count++;
        // vertical
        for (int r = 0; r <= Connect4State.ROWS - length; r++)
            for (int c = 0; c < Connect4State.COLS; c++)
                if (checkRun(state, length, r, c, 1, 0, black, red, empty))
                    count++;
        // diagonal nw-se
        for (int r = 0; r <= Connect4State.ROWS - length; r++)
            for (int c = 0; c <= Connect4State.COLS - length; c++)
                if (checkRun(state, length, r, c, 1, 1, black, red, empty))
                    count++;
        // diagonal sw-ne
        for (int r = Connect4State.ROWS - 1; r >= (length - 1); r--)
            for (int c = 0; c <= Connect4State.COLS - length; c++)
                if (checkRun(state, length, r, c, -1, 1, black, red, empty))
                    count++;
        return count;
    }

    // returns true if the run that starts at (r,c) and continues for length cells has
    // exactly the given numbers of black, red, and empty cells.
    // The direction of the run is indicated by deltaR and deltaC
    private static boolean checkRun(Connect4State state, int length, int r, int c, int deltaR, int deltaC, int black, int red, int empty) {
        int countBlack = 0, countRed = 0, countEmpty = 0;
        for (int i = 0; i < length; i++) {
            if (state.isBlackCell(r, c))
                countBlack++;
            else if (state.isRedCell(r, c))
                countRed++;
            else
                countEmpty++;
            r += deltaR;
            c += deltaC;
        }
        return (countBlack == black && countRed == red && countEmpty == empty);
    }
}
  