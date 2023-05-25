package players;

// Author: Andrew Merrill

import graphics.GamePanel;
import graphics.HumanActionSynchronizer;
import main.*;
import javax.swing.*;
import java.util.List;

public class HumanPlayer extends Player {
    private final GamePanel gamePanel;

    public HumanPlayer(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public SearchNode pickMove(State currentState) {
        List<SearchNode> children = currentState.listChildren();
        if (children.size() == 1 && children.get(0).action == null) {
            return children.get(0);
        }
        while (true) {
            HumanActionSynchronizer humanActionSynchronizer = new HumanActionSynchronizer();

            //Tell the gamePanel that we are ready for the human to select an action.
            //Typically, this means the gamePanel should start listening for mouse events.
            //The gamePanel will store the selected action in the humanActionSynchronizer.
            gamePanel.readyForAction(getSide(), children, humanActionSynchronizer);

            //Wait for the humanActionSynchronizer to receive an action from the gamePanel.
            //The call to getAction() will block until there is an action to return.
            main.Action humanAction = humanActionSynchronizer.getAction();

            //Tell the gamePanel that an action was selected
            gamePanel.actionSelected();

            for (SearchNode node : children) {
                if (node.action.equals(humanAction)) {
                    return node;
                }
            }
            JOptionPane.showMessageDialog(gamePanel, "That was not a valid action.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public String toString() {
        return "Human";
    }

}