package graphics;

// Author: Andrew Merrill

import main.Evaluator;
import main.Player;
import main.Side;
import main.State;
import players.*;

import javax.swing.*;

public class PlayerPanel extends Box {
    private GraphicalMain gui;
    private Side side;
    private JComboBox evaluatorChooser;
    private JTextField maxDepthField;
    private JRadioButton humanButton, randomButton, greedyButton, minimaxButton, alphabetaButton;
    private ButtonGroup playerButtonGroup;
    private JLabel evaluationLabel;

    PlayerPanel(Side side, GraphicalMain gui) {
        super(BoxLayout.Y_AXIS);
        this.gui = gui;
        this.side = side;
        resetTitle();

        humanButton = new JRadioButton("Human");
        randomButton = new JRadioButton("Random");
        greedyButton = new JRadioButton("Greedy");
        minimaxButton = new JRadioButton("MiniMax");
        alphabetaButton = new JRadioButton("AlphaBeta");

        greedyButton.setEnabled(true);
        minimaxButton.setEnabled(true);
        alphabetaButton.setEnabled(true);

        playerButtonGroup = new ButtonGroup();
        playerButtonGroup.add(humanButton);
        playerButtonGroup.add(randomButton);
        playerButtonGroup.add(greedyButton);
        playerButtonGroup.add(minimaxButton);
        playerButtonGroup.add(alphabetaButton);

        JPanel maxDepthPanel = new JPanel();
        maxDepthPanel.add(new JLabel("Max Search Depth: "));
        maxDepthField = new JTextField("4", 4);
        maxDepthPanel.add(maxDepthField);

        evaluatorChooser = new JComboBox();
        updateEvaluatorList();
        JPanel evaluatorPanel = new JPanel();
        evaluatorPanel.add(new JLabel("Evaluator: "));
        evaluatorPanel.add(evaluatorChooser);

        evaluationLabel = new JLabel("Eval: ");

        add(humanButton);
        add(randomButton);
        add(greedyButton);
        add(minimaxButton);
        add(alphabetaButton);
        add(Box.createVerticalStrut(5));
        add(maxDepthPanel);
        add(evaluatorPanel);
        add(evaluationLabel);

        if (side == Side.ONE)
            humanButton.setSelected(true);
        else
            randomButton.setSelected(true);
    }

    void gameSwitched() {
        resetTitle();
        updateEvaluatorList();
    }

    void updateEvaluatorList() {
        evaluatorChooser.setModel(new DefaultComboBoxModel(gui.getCurrentGame().getEvaluators()));
    }

    void update(State gameState) {
        int eval = getCurrentEvaluator().evaluate(gameState);
        evaluationLabel.setText("Eval: " + eval);
    }

    void resetTitle() {
        setBorder(BorderFactory.createTitledBorder("Player " + gui.getCurrentGame().getNameForSide(side)));
    }

    Evaluator getCurrentEvaluator() {
        return (Evaluator) evaluatorChooser.getSelectedItem();
    }

    Player getPlayer() {
        Evaluator choosenEvaluator = getCurrentEvaluator();
        int maxDepth = Integer.parseInt(maxDepthField.getText());
        if (humanButton.isSelected()) return new HumanPlayer(gui.getCurrentGame().getPanel());
        if (randomButton.isSelected()) return new RandomPlayer();
        if (greedyButton.isSelected()) return new GreedyPlayer(choosenEvaluator);
        if (minimaxButton.isSelected()) return new MiniMaxPlayer(maxDepth, choosenEvaluator);
        if (alphabetaButton.isSelected()) return new AlphaBetaPlayer(maxDepth, choosenEvaluator);
        throw new IllegalStateException("You did not select a player!");
    }
}