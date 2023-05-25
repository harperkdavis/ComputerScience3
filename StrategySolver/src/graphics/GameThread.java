package graphics;

// Author: Andrew Merrill

import main.*;
import main.Action;

import javax.swing.*;

public class GameThread implements Runnable {
    private final Game currentGame;
    private final State gameState;
    private final PlayerPanel playerOnePanel, playerTwoPanel;
    private final Player playerOne, playerTwo;
    private final GamePanel gamePanel;
    private final Referee referee;
    private final int delayms;
    private boolean killed;

    GameThread(Game currentGame, State gameState, PlayerPanel playerOnePanel, PlayerPanel playerTwoPanel, GamePanel gamePanel, int delayms) {
        this.currentGame = currentGame;
        this.gameState = gameState;
        this.playerOnePanel = playerOnePanel;
        this.playerTwoPanel = playerTwoPanel;
        this.playerOne = playerOnePanel.getPlayer();
        this.playerTwo = playerTwoPanel.getPlayer();
        this.gamePanel = gamePanel;
        this.delayms = delayms;
        this.killed = false;
        this.referee = new Referee();
    }

    public void run() {
        playerOnePanel.update(gameState);
        playerTwoPanel.update(gameState);
        Side winner = referee.runGame(gameState, playerOne, playerTwo, this);
        if (killed) return;
        String message = "";
        if (winner == null) {
            message = "The game was a " + currentGame.getNameForTie();
        } else {
            String winningPlayer = currentGame.getNameForSide(winner);
            message = "The winner is player " + winningPlayer;
        }
        JOptionPane.showMessageDialog(gamePanel, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }

    public void die() {
        killed = true;
        referee.stopGame();
    }

    public void update(State gameState, Action gameAction) {
        gamePanel.setState(gameState);
        if (gameAction != null) {
            gamePanel.setRecentAction(gameAction);
            playerOnePanel.update(gameState);
            playerTwoPanel.update(gameState);
            if (delayms > 0) {
                try {
                    Thread.sleep(delayms);
                } catch (InterruptedException e) {
                }
            }
        }
    }


}