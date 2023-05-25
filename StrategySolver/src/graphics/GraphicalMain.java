package graphics;

// Author: Andrew Merrill

import games.chess.ChessGame;
import games.checkers.CheckersGame;
import games.connect4.Connect4Game;
import games.othello.OthelloGame;
import games.sillygame.SillyGame;
import main.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class GraphicalMain {
    private final Game[] games = new Game[]{
            new Connect4Game(), new ChessGame(), new CheckersGame(), new OthelloGame() //, new SillyGame()
    };

    private Game currentGame;

    private final JPanel mainPanel;
    private final JComboBox gameChooser;
    private final JTextField randomMovesField;
    private final JTextField delayField;

    private final PlayerPanel playerOnePanel, playerTwoPanel;

    private GameThread gameThread = null;


    public static void main(String[] args) {
        new GraphicalMain();
    }


    GraphicalMain() {
        currentGame = games[0];

        gameChooser = new GameChooser();
        JPanel chooserPanel = new JPanel();
        chooserPanel.add(new JLabel("Game: "));
        chooserPanel.add(gameChooser);

        playerOnePanel = new PlayerPanel(Side.ONE, this);
        playerTwoPanel = new PlayerPanel(Side.TWO, this);


        JButton startGameButton = new JButton("Start A Game");
        startGameButton.addActionListener(new StartGameListener());

        randomMovesField = new JTextField("0", 4);
        JPanel randomMovesPanel = new JPanel();
        randomMovesPanel.add(new JLabel("Initial Random Moves: "));
        randomMovesPanel.add(randomMovesField);

        delayField = new JTextField("100", 4);
        JPanel delayPanel = new JPanel();
        delayPanel.add(new JLabel("Delay Between Moves: "));
        delayPanel.add(delayField);
        delayPanel.add(new JLabel("ms"));

        Box controlPanel = new Box(BoxLayout.Y_AXIS);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        controlPanel.add(new JLabel("<html><h2>Games!</h2></html>"));
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(chooserPanel);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(playerOnePanel);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(playerTwoPanel);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(randomMovesPanel);
        controlPanel.add(delayPanel);
        controlPanel.add(startGameButton);
        controlPanel.add(Box.createVerticalGlue());


        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(controlPanel, BorderLayout.WEST);
        mainPanel.add(currentGame.getPanel(), BorderLayout.CENTER);


        JFrame mainFrame = new JFrame("Games");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.getContentPane().add(mainPanel);
        mainFrame.setSize(900, 700);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
        mainFrame.toFront();
    }

    private void switchToGame(Game game) {
        mainPanel.remove(currentGame.getPanel());
        currentGame = game;
        mainPanel.add(game.getPanel(), BorderLayout.CENTER);
        playerOnePanel.gameSwitched();
        playerTwoPanel.gameSwitched();
        mainPanel.repaint();
    }

    Game getCurrentGame() {
        return currentGame;
    }

    private void startGame() {
        if (gameThread != null) {
            gameThread.die();
        }

        GamePanel gamePanel = currentGame.getPanel();
        for (MouseListener mouseListener : gamePanel.getMouseListeners()) {
            gamePanel.removeMouseListener(mouseListener);
        }
        gamePanel.setRecentAction(null);

        State initialState = currentGame.getInitialState();
        int randomMoves = Integer.parseInt(randomMovesField.getText());
        if (randomMoves > 0) {
            initialState = Analysis.randomMoves(initialState, randomMoves);
        }
        int delay = Integer.parseInt(delayField.getText());

        gameThread = new GameThread(currentGame,
                initialState,
                playerOnePanel,
                playerTwoPanel,
                currentGame.getPanel(),
                delay);
        Thread thread = new Thread(gameThread);
        thread.start();
    }

    class StartGameListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            startGame();
        }
    }

    class GameChooser extends JComboBox implements ItemListener {
        GameChooser() {
            super(games);
            addItemListener(this);
        }

        public void itemStateChanged(ItemEvent event) {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                switchToGame((Game) event.getItem());
            }
        }
    }

}
