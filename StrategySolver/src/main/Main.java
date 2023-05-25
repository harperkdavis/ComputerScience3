package main;

// Author: Andrew Merrill

import games.connect4.*;
import players.AlphaBetaPlayer;
import players.GreedyPlayer;
import players.MiniMaxPlayer;
import players.RandomPlayer;

public class Main {
    public static void main(String[] args) {
        compareEvaluators();
        //timeEvaluators();
        //timePlayers();
        //comparePlayers();
        //playerMatrix();
    }

    static void compareEvaluators() {
        Analysis.compareEvaluatorsForGame(new Connect4Game(), 100, 4);
    }

    static void timeEvaluators() {
        Game game = new Connect4Game();
        Evaluator[] evaluators = game.getEvaluators();
        int maxDepth = 3;

        for (Evaluator evaluator : evaluators) {
            for (int d = 1; d <= maxDepth; d++) {
                Player p = new MiniMaxPlayer(d, evaluator);
                Analysis.averageTime(game.getInitialState(), p, 20);
            }
        }
    }

    static void timePlayers() {
        for (int depth=1; depth<=10; depth++) {
            Analysis.averageTimes(new Connect4State(),
                    new Player[]{
                            new MiniMaxPlayer(depth, new Connect4Evaluator0()),
                            new AlphaBetaPlayer(depth, new Connect4Evaluator0())
                    },
                    50);
            System.out.println();
        }
    }

    static void comparePlayers() {
        System.out.println("fraction of games that players agree is: " +
                Analysis.playersAgree(
                    new Connect4State(),
                    new MiniMaxPlayer(4, new Connect4Evaluator0()),
                    new AlphaBetaPlayer(4, new Connect4Evaluator0()),
                    100));
    }

    static void playerMatrix() {
        int maxDepth = 4;
        Player[] players = new Player[maxDepth + 2];
        for (int d=1; d<=maxDepth; d++) {
            players[d-1] = new MiniMaxPlayer(d, new Connect4Evaluator0());
        }
        players[maxDepth] = new GreedyPlayer(new Connect4Evaluator0());
        players[maxDepth+1] = new RandomPlayer();
        Analysis.comparePlayersForGame(new Connect4Game(), players, 100);
    }

}
