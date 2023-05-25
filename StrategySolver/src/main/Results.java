package main;

// Author: Andrew Merrill

import java.text.*;

class Results {
    int wins, losses, ties;
    int playerAindex, playerBindex;

    Results() {
        wins = 0;
        losses = 0;
        ties = 0;
    }

    Results(int w, int l, int t) {
        wins = w;
        losses = l;
        ties = t;
    }

    void setPlayers(int a, int b) {
        playerAindex = a;
        playerBindex = b;
    }

    private static final DecimalFormat percentFormat = new DecimalFormat("###.0%");

    public String toString() {
        if (wins == 0 && losses == 0 && ties == 0) return "N/A";
        double pct = (wins + 0.5 * ties) / (double) (wins + losses + ties);
        return percentFormat.format(pct);
    }
}
