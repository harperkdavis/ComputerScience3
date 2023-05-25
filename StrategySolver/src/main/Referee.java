package main;

// Author: Andrew Merrill

import graphics.GameThread;

import java.util.List;

public class Referee {
    private boolean stopGame = false;

    public void stopGame() {
        stopGame = true;
    }

    // gameThread can be null if you are running analysis and don't want graphics
    public Side runGame(State gameState, Player playerOne, Player playerTwo, GameThread gameThread) {
        stopGame = false;
        playerOne.setSide(Side.ONE);
        playerTwo.setSide(Side.TWO);
        if (gameThread != null)
            gameThread.update(gameState, null);

        while (!gameState.isGameOver()) {
            Side sideToPlay = gameState.getSideToPlay();
            List<SearchNode> children = gameState.listChildren();
            SearchNode selectedNode;
            if (sideToPlay == playerOne.getSide())
                selectedNode = playerOne.pickMove(gameState);
            else
                selectedNode = playerTwo.pickMove(gameState);

            boolean legalMove = false;
            for (SearchNode child : children) {
                if (child.state.equals(selectedNode.state) && (child.action == null || child.action.equals(selectedNode.action))) {
                    legalMove = true;
                    break;
                }
            }
            if (! legalMove) {
                throw new IllegalStateException("Side " + sideToPlay + " chose an illegal move:  " + selectedNode.action);
            }


            gameState = selectedNode.state;
            if (gameThread != null) {
                gameThread.update(gameState, selectedNode.action);
            }
            if (stopGame) return null;
        }
        return gameState.getWinner();
    }
}