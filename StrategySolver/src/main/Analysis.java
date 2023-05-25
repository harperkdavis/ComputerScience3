package main;

// Author: Andrew Merrill

// Static functions for analyzing players and evaluators

import players.*;

import java.util.concurrent.*;
import java.text.*;

public class Analysis {
    private static Referee referee = new Referee();

    private static int NUM_THREADS = 4;

    static LinkedBlockingQueue<Job> jobsQ = new LinkedBlockingQueue<Job>();
    static LinkedBlockingQueue<Results> resultsQ = new LinkedBlockingQueue<Results>();
    static final boolean verbose = false;
    static final String format = "%12s";
    private static final DecimalFormat percentFormat = new DecimalFormat("###.0%");

    // displays a matrix showing win percentage of player at the beginning of each row against player in each column
    static void compareEvaluatorsForGame(Game game, int numTrials, int maxDepth) {
        System.out.println(game + " with " + numTrials + " trials, showing win % of row player against column player");
        Evaluator[] evaluators = game.getEvaluators();
        Player[] players = new Player[evaluators.length];
        for (int i = 0; i < evaluators.length; i++) {
            players[i] = new AlphaBetaPlayer(maxDepth, evaluators[i]);   // edit to use a different Player
        }
        playerMatrixThreaded(game.getInitialState(), players, numTrials, NUM_THREADS);
    }

    // displays a matrix showing win percentage of player at the beginning of each row against player in each column
    static void comparePlayersForGame(Game game, Player[] players, int numTrials) {
        System.out.println(game + " with " + numTrials + " trials");
        playerMatrixThreaded(game.getInitialState(), players, numTrials, NUM_THREADS);
    }


    // displays the average time (in milliseconds) a player takes to pick a move
    static void averageTime(State startState, Player player, int numTrials) {
        long time = 0;

        for (int trial = 0; trial < numTrials; trial++) {
            int initialMoves = MyRandom.nextIntInRange(0, 20);
            State state = randomMoves(startState, initialMoves);
            player.setSide(state.getSideToPlay());
            long start = System.nanoTime();
            SearchNode node = player.pickMove(state);
            long stop = System.nanoTime();
            time += (stop - start);
        }
        System.out.println(player + "\t" + String.format("%9.3f", (time / (double) numTrials / 1e6)) + " ms");
    }


    // displays the average time (in milliseconds) each player in an array takes to pick a move
    static void averageTimes(State startState, Player[] players, int numTrials) {
        long[] times = new long[players.length];
        for (int t = 0; t < times.length; t++)
            times[t] = 0;

        for (int trial = 0; trial < numTrials; trial++) {
            int initialMoves = MyRandom.nextIntInRange(0, 20);
            State state = randomMoves(startState, initialMoves);
            for (int p = 0; p < players.length; p++) {
                Player player = players[p];
                player.setSide(state.getSideToPlay());
                long start = System.nanoTime();
                SearchNode node = player.pickMove(state);
                long stop = System.nanoTime();
                times[p] += (stop - start);
            }
        }
        for (int p = 0; p < players.length; p++) {
            System.out.println(players[p] + "\t" + String.format("%9.3f", (times[p] / (double) numTrials / 1e6)) + " ms");
        }
    }


    // returns fraction of the time that the given Players agree
    // 1.0 means they agree all of the time; 0.0 means none of the time
    static double playersAgree(State startState, Player playerA, Player playerB, int numTrials) {
        int agrees = 0;
        Side side = Side.TWO;
        for (int trial = 0; trial < numTrials; trial++) {
            int initialMoves = MyRandom.nextIntInRange(2, 10) * 2;
            State state = randomMoves(startState, initialMoves);
            playerA.setSide(side);
            playerB.setSide(side);
            SearchNode nodeA = playerA.pickMove(state);
            SearchNode nodeB = playerB.pickMove(state);
            side = side.otherSide();

            if (nodeA.state.equals(nodeB.state) && nodeA.action.equals(nodeB.action))
                agrees++;
        }
        return agrees / (double) numTrials;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    static class Job {
        final boolean poison;  // a poison job is used to kill the worker who takes it
        final State startState;
        final Player playerA, playerB;
        final int playerAindex, playerBindex;
        final int numTrials;

        Job(State startState, Player playerA, Player playerB, int playerAindex, int playerBindex, int numTrials) {
            this.startState = startState;
            if (startState != null) {
                this.playerA = playerA.getClone();
                this.playerB = playerB.getClone();
                this.numTrials = numTrials;
                this.playerAindex = playerAindex;
                this.playerBindex = playerBindex;
                this.poison = false;
            } else {
                this.playerA = null;
                this.playerB = null;
                this.playerAindex = -1;
                this.playerBindex = -1;
                this.numTrials = -1;
                this.poison = true;
            }
        }
    }


    static class Worker extends Thread {
        final int id;

        Worker(int id) {
            this.id = id;
        }

        public void run() {
            try {
                if (verbose) System.out.println("worker " + id + " started");
                while (true) {
                    Job job = jobsQ.take();  // blocks
                    if (job.poison) {
                        if (verbose) System.out.println("worker " + id + " ended");
                        return;
                    }
                    Results results = faceOff(job.startState, job.playerA, job.playerB, job.numTrials);
                    results.setPlayers(job.playerAindex, job.playerBindex);
                    resultsQ.add(results);
                }
            } catch (InterruptedException ie) {
                if (verbose) System.out.println("worker " + id + " interupted!");
            }
        }
    }

    // displays the results of running every player in an array against each other
    static void playerMatrixThreaded(State startState, Player[] players, int numTrials, int numThreads) {
        for (int pA = 0; pA < players.length; pA++) {
            for (int pB = 0; pB < players.length; pB++) {
                if (pA != pB) {
                    jobsQ.add(new Job(startState, players[pA], players[pB], pA, pB, numTrials));
                }
            }
        }
        for (int w = 0; w < numThreads; w++) {
            jobsQ.add(new Job(null, null, null, 0, 0, 0));
        }

        Worker[] workers = new Worker[numThreads];
        for (int w = 0; w < numThreads; w++) {
            workers[w] = new Worker(w);
            workers[w].start();
        }
        System.out.printf(format, "Player");
        for (int p = 0; p < players.length; p++) {
            System.out.printf(format, players[p].toString());
        }
        System.out.println();
        Results[][] allResults = new Results[players.length][players.length];
        boolean done = false;
        int nextA = 0, nextB = 0;
        System.out.printf(format, players[0].toString());
        while (!done) {
            try {
                Results newResults = resultsQ.take();
                allResults[newResults.playerAindex][newResults.playerBindex] = newResults;
            } catch (InterruptedException ie) {
            }
            while (nextA == nextB || allResults[nextA][nextB] != null) {
                if (nextA == nextB)
                    System.out.printf(format, "self");
                else
                    System.out.printf(format, allResults[nextA][nextB]);
                nextB++;
                if (nextB >= players.length) {
                    System.out.println();
                    nextA++;
                    nextB = 0;
                    if (nextA >= players.length) {
                        done = true;
                        break;
                    }
                    System.out.printf(format, players[nextA].toString());
                }
            }
        }
        System.out.println();
        System.out.printf(format, "Win %");
        for (int pA = 0; pA < players.length; pA++) {
            int games = 0;
            int wins = 0;
            for (int pB = 0; pB < players.length; pB++) {
                if (pA != pB) {
                    Results results = allResults[pA][pB];
                    games += results.wins + results.losses + results.ties;
                    wins += results.wins + 0.5 * results.ties;
                }
            }
            double winPercent = ((double) wins) / games;
            String winPercentStr = percentFormat.format(winPercent);
            System.out.printf(format, winPercentStr);
        }
        System.out.println();

    }


////////////////////////////////////////////////////////////////////////////////////////////////////////


    // performs the given number of random moves and returns the resulting State
    public static State randomMoves(State startState, int numMoves) {
        Player randomPlayer = new RandomPlayer();
        State state = null;
        do {
            state = startState;
            for (int m = 0; m < numMoves && state != null; m++) {
                state = randomPlayer.pickMove(state).state;
            }
        } while (state == null || state.isGameOver());
        return state;
    }

    // returns the results of playing one player against another the given number of times
    static Results faceOff(State startState, Player playerA, Player playerB, int numTrials) {
        try {
            Results results = new Results();
            for (int trial = 0; trial < numTrials; trial++) {
                int initialMoves = 2;
                State state = randomMoves(startState, initialMoves);
                Side winner = referee.runGame(state, playerA, playerB, null);
                if (winner == playerA.getSide()) {
                    results.wins++;
                } else if (winner == playerB.getSide()) {
                    results.losses++;
                } else {
                    results.ties++;
                }
                winner = referee.runGame(state, playerB, playerA, null);
                if (winner == playerA.getSide()) {
                    results.wins++;
                } else if (winner == playerB.getSide()) {
                    results.losses++;
                } else {
                    results.ties++;
                }
            }
            return results;
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
            return new Results();
        }
    }


    // displays the results of running one player against every player in an array
    static void playerAgainstField(State startState, Player player, Player[] players, int numTrials) {
        System.out.printf(format, player);
        for (Player p : players) {
            if (player == p) {
                System.out.printf(format, "self");
            } else {
                Results result = faceOff(startState, player, p, numTrials);
                System.out.printf(format, result);
            }
        }
        System.out.println();
    }

    // displays the results of running every player in an array against each other
    static void playerMatrix(State startState, Player[] players, int numTrials) {
        System.out.printf(format, "Player");
        for (Player p : players) {
            System.out.printf(format, p.toString());
        }
        System.out.println();

        for (Player p : players) {
            playerAgainstField(startState,p, players, numTrials);
        }
    }

}
